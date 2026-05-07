// Copyright (c) 2026 Smile.CX Srl
// SPDX-License-Identifier: MIT
//
// This file is part of a native Swift port of CASL (https://github.com/stalniy/casl)
// by Sergii Stotskyi, used under the MIT License. See the NOTICE file for details.

import Foundation

// MARK: - PermittedFieldsOf

/// Returns the permitted fields for a given action and subject.
/// Iterates possible rules from end to start (lowest priority first),
/// adding fields for regular rules and removing fields for inverted ones.
public func permittedFieldsOf(_ ability: Ability, action: String, subject: Any, fieldsFrom: (Rule) -> [String]) -> [String] {
    let subjectType = ability.detectSubjectType(subject)
    let rules = ability.possibleRulesFor(action, subjectType)

    var fieldSet = Set<String>()
    var i = rules.count

    while i > 0 {
        i -= 1
        let rule = rules[i]
        if rule.matchesConditions(subject) {
            let toggle: (String) -> Void = rule.inverted
                ? { fieldSet.remove($0) }
                : { fieldSet.insert($0) }
            for f in fieldsFrom(rule) {
                toggle(f)
            }
        }
    }

    return Array(fieldSet)
}

// MARK: - AccessibleFields

/// Helper class that wraps `permittedFieldsOf` with a `getAllFields` extractor.
/// Mirrors the JS `AccessibleFields` class from `extra/permittedFieldsOf.ts`.
public class AccessibleFields {
    private let _ability: Ability
    private let _action: String
    private let _getAllFields: (String) -> [String]

    public init(ability: Ability, action: String, getAllFields: @escaping (String) -> [String]) {
        self._ability = ability
        self._action = action
        self._getAllFields = getAllFields
    }

    /// Returns accessible fields for a subject type string.
    public func ofType(_ subjectType: String) -> [String] {
        return permittedFieldsOf(_ability, action: _action, subject: subjectType) { rule in
            rule.fields ?? self._getAllFields(subjectType)
        }
    }

    /// Returns accessible fields for a particular subject instance (dictionary).
    public func of(_ subject: Any) -> [String] {
        let subjectType = _ability.detectSubjectType(subject)
        return permittedFieldsOf(_ability, action: _action, subject: subject) { rule in
            rule.fields ?? self._getAllFields(subjectType)
        }
    }
}

// MARK: - RulesToFields

/// Extracts simple values from conditions of relevant rules.
/// Supports dot notation -> nested dict. Skips values that are dictionaries (query expressions).
public func rulesToFields(_ ability: Ability, action: String, subjectType: String) -> [String: Any] {
    let rules = ability.rulesFor(action, subjectType)

    var result: [String: Any] = [:]

    for rule in rules {
        if rule.inverted || rule.conditions == nil {
            continue
        }

        for (key, value) in rule.conditions! {
            // Skip query expressions (dict values like {"$eq": ...})
            if value is [String: Any] {
                continue
            }

            let parts = key.split(separator: ".").map(String.init)
            if parts.contains(where: { forbiddenProperties.contains($0) }) {
                continue
            }
            if parts.count == 1 {
                result[key] = value
            } else {
                setNestedValue(&result, parts: parts, value: value)
            }
        }
    }

    return result
}

private let forbiddenProperties: Set<String> = ["__proto__", "constructor", "prototype"]

private func setNestedValue(_ dict: inout [String: Any], parts: [String], value: Any) {
    guard let key = parts.first, !forbiddenProperties.contains(key) else { return }
    if parts.count == 1 {
        dict[key] = value
        return
    }
    var nested = (dict[key] as? [String: Any]) ?? [:]
    setNestedValue(&nested, parts: Array(parts.dropFirst()), value: value)
    dict[key] = nested
}

// MARK: - RulesToCondition

/// Converts CASL's priority-ordered rules into flat boolean logic for database queries.
///
/// Each `can` branch is bounded by all higher-priority `cannot` conditions (AND NOT).
/// An unconditional `cannot` stops evaluation; an unconditional `can` produces an
/// "allow all" branch bounded only by preceding `cannot` conditions.
///
/// Returns `nil` when not allowed at all, or `empty()` when allowed without conditions.
public func rulesToCondition<R>(
    _ rules: [Rule],
    convert: (Rule) -> R,
    and: ([R]) -> R,
    or: ([R]) -> R,
    empty: () -> R
) -> R? {
    var higherCannots: [R] = []
    var orConditions: [R] = []
    var hasUnconditionalCan = false

    for rule in rules {
        if rule.inverted {
            guard rule.conditions != nil else { break }
            higherCannots.append(convert(rule))
        } else {
            guard rule.conditions != nil else {
                hasUnconditionalCan = true
                break
            }
            let converted = convert(rule)
            if higherCannots.isEmpty {
                orConditions.append(converted)
            } else {
                orConditions.append(and([converted] + higherCannots))
            }
        }
    }

    if hasUnconditionalCan {
        if higherCannots.isEmpty { return empty() }
        if orConditions.isEmpty { return and(higherCannots) }
        orConditions.append(and(higherCannots))
    }

    if orConditions.isEmpty { return nil }
    return or(orConditions)
}

// MARK: - RulesToQuery (legacy flat-query shape)

public struct AbilityQuery<R> {
    public var or: [R]?
    public var and: [R]?

    public init(or: [R]? = nil, and: [R]? = nil) {
        self.or = or
        self.and = and
    }
}

/// Converts ability rules into a flat query structure.
/// Returns nil if not allowed; returns AbilityQuery() (empty) if allowed without conditions.
///
/// Note: this uses a simplified flat model. For correct priority-respecting query generation
/// use `rulesToCondition` directly (matches JS `rulesToCondition` from `@casl/ability/extra`).
public func rulesToQuery<R>(_ ability: Ability, action: String, subjectType: String, convert: (Rule) -> R) -> AbilityQuery<R>? {
    let rules = ability.rulesFor(action, subjectType)
    var orRules: [R] = []
    var andRules: [R] = []

    for rule in rules {
        if rule.conditions == nil {
            if rule.inverted { break }
            return andRules.isEmpty ? AbilityQuery() : AbilityQuery(and: andRules)
        }
        if rule.inverted {
            andRules.append(convert(rule))
        } else {
            orRules.append(convert(rule))
        }
    }

    if orRules.isEmpty { return nil }
    return AbilityQuery(or: orRules, and: andRules.isEmpty ? nil : andRules)
}

// MARK: - RulesToAST

/// AST node types that mirror @ucast/mongo2js Condition types.
/// Used by `rulesToAST` to convert ability rules into an abstract syntax tree.

public indirect enum ConditionAST: Equatable {
    /// A field-level condition: field, operator, value
    case condition(field: String, op: String, value: ASTValue)
    /// A compound condition: operator (and/or/not), children
    case compound(op: String, children: [ConditionAST])

    public static func == (lhs: ConditionAST, rhs: ConditionAST) -> Bool {
        switch (lhs, rhs) {
        case let (.condition(f1, o1, v1), .condition(f2, o2, v2)):
            return f1 == f2 && o1 == o2 && v1 == v2
        case let (.compound(o1, c1), .compound(o2, c2)):
            return o1 == o2 && c1 == c2
        default:
            return false
        }
    }
}

/// A simple wrapper for AST values that supports Equatable.
public enum ASTValue: Equatable {
    case string(String)
    case int(Int)
    case double(Double)
    case bool(Bool)
    case array([ASTValue])
    case null

    public static func from(_ value: Any) -> ASTValue {
        if let s = value as? String { return .string(s) }
        if let b = value as? Bool { return .bool(b) }
        if let i = value as? Int { return .int(i) }
        if let d = value as? Double { return .double(d) }
        if let n = value as? NSNumber {
            if n === kCFBooleanTrue { return .bool(true) }
            if n === kCFBooleanFalse { return .bool(false) }
            return .double(n.doubleValue)
        }
        if let arr = value as? [Any] {
            return .array(arr.map { ASTValue.from($0) })
        }
        return .null
    }
}

/// Converts a rule's conditions dictionary into a ConditionAST.
/// Returns nil if the rule has no conditions.
public func ruleToAST(_ rule: Rule) -> ConditionAST? {
    guard let conditions = rule.conditions else { return nil }
    let ast = conditionsToAST(conditions)
    if rule.inverted {
        return .compound(op: "not", children: [ast])
    }
    return ast
}

/// Converts a conditions dictionary into an AST tree.
private func conditionsToAST(_ conditions: [String: Any]) -> ConditionAST {
    var children: [ConditionAST] = []

    for (key, value) in conditions {
        if key == "__caslSubjectType__" { continue }

        switch key {
        case "$and":
            if let arr = value as? [[String: Any]] {
                let subChildren = arr.map { conditionsToAST($0) }
                children.append(.compound(op: "and", children: subChildren))
            }
        case "$or":
            if let arr = value as? [[String: Any]] {
                let subChildren = arr.map { conditionsToAST($0) }
                children.append(.compound(op: "or", children: subChildren))
            }
        case "$not":
            if let sub = value as? [String: Any] {
                children.append(.compound(op: "not", children: [conditionsToAST(sub)]))
            }
        default:
            if let operators = value as? [String: Any], operators.keys.contains(where: { $0.hasPrefix("$") }) {
                for (op, opValue) in operators {
                    if op == "$options" { continue } // handled by $regex
                    children.append(.condition(field: key, op: op, value: ASTValue.from(opValue)))
                }
            } else {
                children.append(.condition(field: key, op: "$eq", value: ASTValue.from(value)))
            }
        }
    }

    if children.count == 1 {
        return children[0]
    }
    return .compound(op: "and", children: children)
}

/// Converts ability rules into an AST tree, mirroring the JS `rulesToAST` function.
/// Returns nil if the user is not allowed to perform the action.
public func rulesToAST(_ ability: Ability, action: String, subjectType: String) -> ConditionAST? {
    let rules = ability.rulesFor(action, subjectType)
    return rulesToCondition(
        rules,
        convert: { rule -> ConditionAST in
            ruleToAST(rule) ?? .compound(op: "and", children: [])
        },
        and: { children in .compound(op: "and", children: children) },
        or: { children in .compound(op: "or", children: children) },
        empty: { .compound(op: "and", children: []) }
    )
}

// MARK: - Subject Helper

private let caslSubjectTypeKey = "__caslSubjectType__"

public enum SubjectHelperError: Error, LocalizedError {
    case typeMismatch(existing: String, new: String)

    public var errorDescription: String? {
        switch self {
        case .typeMismatch(let existing, let new):
            return "Cannot redefine subject type from \"\(existing)\" to \"\(new)\""
        }
    }
}

public func setSubjectType(_ type: String, on object: [String: Any]) throws -> [String: Any] {
    if let existing = object[caslSubjectTypeKey] as? String {
        if existing != type {
            throw SubjectHelperError.typeMismatch(existing: existing, new: type)
        }
        return object
    }
    var result = object
    result[caslSubjectTypeKey] = type
    return result
}

public func detectSubjectType(_ object: [String: Any]) -> String {
    if let type = object[caslSubjectTypeKey] as? String {
        return type
    }
    return "Dictionary"
}
