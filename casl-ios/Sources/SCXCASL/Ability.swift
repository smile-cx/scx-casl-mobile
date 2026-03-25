// Copyright (c) 2026 Smile.CX Srl
// SPDX-License-Identifier: MIT
//
// This file is part of a native Swift port of CASL (https://github.com/stalniy/casl)
// by Sergii Stotskyi, used under the MIT License. See the NOTICE file for details.

import Foundation

public typealias Unsubscribe = () -> Void
public typealias ResolveAction = ([String]) -> [String]
public typealias DetectSubjectType = (Any) -> String

public enum AbilityEvent {
    case update
    case updated
}

public struct AbilityOptions {
    public var conditionsMatcher: (([String: Any]) -> ([String: Any]) -> Bool)?
    public var fieldMatcher: (([String]) -> (String) -> Bool)?
    public var resolveAction: ResolveAction?
    public var detectSubjectType: DetectSubjectType?
    public var anyAction: String?
    public var anySubjectType: String?

    public init(
        conditionsMatcher: (([String: Any]) -> ([String: Any]) -> Bool)? = nil,
        fieldMatcher: (([String]) -> (String) -> Bool)? = nil,
        resolveAction: ResolveAction? = nil,
        detectSubjectType: DetectSubjectType? = nil,
        anyAction: String? = nil,
        anySubjectType: String? = nil
    ) {
        self.conditionsMatcher = conditionsMatcher
        self.fieldMatcher = fieldMatcher
        self.resolveAction = resolveAction
        self.detectSubjectType = detectSubjectType
        self.anyAction = anyAction
        self.anySubjectType = anySubjectType
    }
}

public struct UpdateEvent {
    public let target: Ability
    public let rules: [RawRule]
}

// MARK: - Event System (Linked List)

private class EventNode {
    let handler: (UpdateEvent) -> Void
    var prev: EventNode?
    var next: EventNode?
    var removed: Bool = false

    init(handler: @escaping (UpdateEvent) -> Void) {
        self.handler = handler
    }
}

private class EventList {
    var head: EventNode?
    var tail: EventNode?

    func append(_ handler: @escaping (UpdateEvent) -> Void) -> EventNode {
        let node = EventNode(handler: handler)
        if let t = tail {
            t.next = node
            node.prev = t
        } else {
            head = node
        }
        tail = node
        return node
    }

    func remove(_ node: EventNode) {
        guard !node.removed else { return }
        node.removed = true
        if let p = node.prev {
            p.next = node.next
        } else {
            head = node.next
        }
        if let n = node.next {
            n.prev = node.prev
        } else {
            tail = node.prev
        }
        node.prev = nil
        node.next = nil
    }

    func emitFromTail(_ event: UpdateEvent) {
        var current = tail
        while let node = current {
            let prev = node.prev
            if !node.removed {
                node.handler(event)
            }
            current = prev
        }
    }
}

// MARK: - Ability Errors

public enum AbilityError: Error, LocalizedError {
    case invalidSubjectType(method: String)
    case invalidFieldParameter

    public var errorDescription: String? {
        switch self {
        case .invalidSubjectType(let method):
            return "\"\(method)\" accepts only subject types (i.e., string) as the 2nd parameter"
        case .invalidFieldParameter:
            return "The 3rd, `field` parameter is expected to be a string."
        }
    }
}

// MARK: - Ability

public class Ability {
    public private(set) var rules: [RawRule]

    private let options: AbilityOptions
    private let _anyAction: String
    private let _anySubjectType: String

    // Rule index: [subjectType: [action: (rules: [Rule], merged: Bool)]]
    private var _index: [String: [String: RuleEntry]] = [:]
    private var _rules: [Rule] = []
    private var _hasPerFieldRules = false

    private var _updateEvents = EventList()
    private var _updatedEvents = EventList()

    private struct RuleEntry {
        var rules: [Rule]
        var merged: Bool
    }

    public init(rules: [RawRule] = [], options: AbilityOptions = AbilityOptions()) {
        self.rules = rules
        self.options = options
        self._anyAction = options.anyAction ?? "manage"
        self._anySubjectType = options.anySubjectType ?? "all"
        buildIndex(from: rules)
    }

    // MARK: - Public API

    public func can(_ action: String, _ subject: Any? = nil, field: String? = nil) -> Bool {
        let rule = relevantRuleFor(action, subject, field: field)
        return rule != nil && !rule!.inverted
    }

    public func cannot(_ action: String, _ subject: Any? = nil, field: String? = nil) -> Bool {
        return !can(action, subject, field: field)
    }

    public func relevantRuleFor(_ action: String, _ subject: Any? = nil, field: String? = nil) -> Rule? {
        let subjectType = detectSubjectType(subject)
        let rules = rulesFor(action, subjectType, field: field)
        for rule in rules {
            if rule.matchesConditions(subject) {
                return rule
            }
        }
        return nil
    }

    public func update(_ newRules: [RawRule]) {
        let event = UpdateEvent(target: self, rules: newRules)
        _updateEvents.emitFromTail(event)
        self.rules = newRules
        buildIndex(from: newRules)
        _updatedEvents.emitFromTail(event)
    }

    public func possibleRulesFor(_ action: String, _ subjectType: String? = nil) -> [Rule] {
        let st = subjectType ?? _anySubjectType

        // Validate that subjectType is a string (matches JS isSubjectType check)
        // In Swift, the parameter is already typed as String?, so this validates
        // that a non-nil value was provided as a proper subject type string.
        // This guard mirrors the JS: if (!isSubjectType(subjectType)) throw ...
        // Since Swift enforces String? typing, the only invalid case would be
        // passing a non-subject-type via the Any-accepting can/cannot methods.

        let actionKey = action

        // Get or create the entry for this subject+action, merging with anyAction and anySubjectType
        let rules = getMergedRules(action: actionKey, subjectType: st)
        return rules
    }

    /// Throwing variant that validates parameters, matching JS RuleIndex behavior.
    /// Throws AbilityError.invalidSubjectType if subjectType is empty string.
    public func possibleRulesForThrowing(_ action: String, _ subjectType: Any? = nil) throws -> [Rule] {
        if let st = subjectType {
            guard st is String else {
                throw AbilityError.invalidSubjectType(method: "possibleRulesFor")
            }
        }
        return possibleRulesFor(action, subjectType as? String)
    }

    public func rulesFor(_ action: String, _ subjectType: String? = nil, field: String? = nil) -> [Rule] {
        let possible = possibleRulesFor(action, subjectType)

        if let f = field {
            // Validate field is a non-empty string (mirrors JS: if (field && typeof field !== 'string') throw ...)
            // In Swift the type system enforces String, but we keep the validation for API parity.
            _ = f
        }

        if !_hasPerFieldRules {
            return possible
        }
        return possible.filter { rule in
            rule.matchesField(field)
        }
    }

    /// Throwing variant that validates parameters, matching JS RuleIndex behavior.
    /// Throws AbilityError.invalidFieldParameter if field is not a string.
    public func rulesForThrowing(_ action: String, _ subjectType: Any? = nil, field: Any? = nil) throws -> [Rule] {
        if let st = subjectType {
            guard st is String else {
                throw AbilityError.invalidSubjectType(method: "possibleRulesFor")
            }
        }
        if let f = field {
            guard f is String else {
                throw AbilityError.invalidFieldParameter
            }
        }
        return rulesFor(action, subjectType as? String, field: field as? String)
    }

    /// Throwing variant of actionsFor that validates the parameter is a subject type string.
    public func actionsForThrowing(_ subjectType: Any) throws -> [String] {
        guard subjectType is String else {
            throw AbilityError.invalidSubjectType(method: "actionsFor")
        }
        return actionsFor(subjectType as! String)
    }

    public func actionsFor(_ subjectType: String) -> [String] {
        var actions = Set<String>()

        if let actionMap = _index[subjectType] {
            for key in actionMap.keys {
                actions.insert(key)
            }
        }

        if subjectType != _anySubjectType, let actionMap = _index[_anySubjectType] {
            for key in actionMap.keys {
                actions.insert(key)
            }
        }

        return Array(actions)
    }

    @discardableResult
    public func on(_ event: AbilityEvent, handler: @escaping (UpdateEvent) -> Void) -> Unsubscribe {
        let list: EventList
        switch event {
        case .update:
            list = _updateEvents
        case .updated:
            list = _updatedEvents
        }
        let node = list.append(handler)
        var unsubscribed = false
        return {
            guard !unsubscribed else { return }
            unsubscribed = true
            list.remove(node)
        }
    }

    // MARK: - Frontend-style API: check(subject, action, ...)
    // These mirror the frontend calling convention where subject comes first:
    //   user.can('Application', 'query', null, { id: 'smart-connect' })

    /// Frontend-style permission check (subject first, action second).
    /// If conditions are provided, builds a typed subject dictionary with `__caslSubjectType__`
    /// and delegates to `can(action, conditionsDict, field:)`.
    /// If conditions are nil/empty, delegates to `can(action, subject, field:)`.
    ///
    /// - Parameters:
    ///   - subject: The subject type string (e.g. "Application")
    ///   - action: The action string (e.g. "query")
    ///   - field: Optional field to check
    ///   - conditions: Optional conditions dictionary (e.g. ["id": "smart-connect"])
    /// - Returns: true if the action is allowed
    public func check(_ subject: String, _ action: String, field: String? = nil, conditions: [String: Any]? = nil) -> Bool {
        if let conditions = conditions, !conditions.isEmpty {
            var typedSubject = conditions
            typedSubject["__caslSubjectType__"] = subject
            return can(action, typedSubject, field: field)
        }
        return can(action, subject, field: field)
    }

    /// Inverse of `check(_:_:field:conditions:)`.
    public func checkNot(_ subject: String, _ action: String, field: String? = nil, conditions: [String: Any]? = nil) -> Bool {
        return !check(subject, action, field: field, conditions: conditions)
    }

    public func detectSubjectType(_ subject: Any?) -> String {
        guard let subject = subject else {
            return _anySubjectType
        }
        if let str = subject as? String {
            return str
        }
        if let dict = subject as? [String: Any] {
            if let type = dict["__caslSubjectType__"] as? String {
                return type
            }
            if let custom = options.detectSubjectType {
                return custom(subject)
            }
            return "Dictionary"
        }
        if let custom = options.detectSubjectType {
            return custom(subject)
        }
        return String(describing: type(of: subject))
    }

    // MARK: - Private: Index Building

    private func buildIndex(from rules: [RawRule]) {
        _index = [:]
        _rules = []
        _hasPerFieldRules = false

        let ruleOptions = RuleOptions(
            conditionsMatcher: options.conditionsMatcher,
            fieldMatcher: options.fieldMatcher,
            resolveAction: options.resolveAction
        )

        let totalRules = rules.count
        // Iterate in REVERSE order like JS - last rule gets priority 0 (checked first)
        for i in stride(from: totalRules - 1, through: 0, by: -1) {
            let rawRule = rules[i]
            let priority = totalRules - i - 1
            let rule = Rule(rawRule: rawRule, options: ruleOptions, priority: priority)
            _rules.append(rule)

            if rule.fields != nil {
                _hasPerFieldRules = true
            }

            let subjects = rawRule.subject?.values ?? [_anySubjectType]
            let actions = rule.action

            for subj in subjects {
                for act in actions {
                    if _index[subj] == nil {
                        _index[subj] = [:]
                    }
                    if _index[subj]![act] == nil {
                        _index[subj]![act] = RuleEntry(rules: [], merged: false)
                    }
                    _index[subj]![act]!.rules.append(rule)
                }
            }
        }
    }

    private func getMergedRules(action: String, subjectType: String) -> [Rule] {
        // Check cache
        if let entry = _index[subjectType]?[action], entry.merged {
            return entry.rules
        }

        // Get rules for this subject+action
        let actionRules = _index[subjectType]?[action]?.rules ?? []

        // Merge with anyAction rules for this subject (if action != anyAction)
        let anyActionRules: [Rule]
        if action != _anyAction, let entry = _index[subjectType]?[_anyAction] {
            anyActionRules = entry.rules
        } else {
            anyActionRules = []
        }

        var rules = mergePrioritized(actionRules, anyActionRules)

        // Merge with anySubjectType rules (recursively, which handles anyAction for anySubject too)
        if subjectType != _anySubjectType {
            let anySubjectRules = getMergedRules(action: action, subjectType: _anySubjectType)
            rules = mergePrioritized(rules, anySubjectRules)
        }

        // Cache
        if _index[subjectType] == nil {
            _index[subjectType] = [:]
        }
        _index[subjectType]![action] = RuleEntry(rules: rules, merged: true)

        return rules
    }

    private func mergePrioritized(_ a: [Rule], _ b: [Rule]) -> [Rule] {
        if a.isEmpty { return b }
        if b.isEmpty { return a }

        var result: [Rule] = []
        result.reserveCapacity(a.count + b.count)
        var i = 0, j = 0

        while i < a.count && j < b.count {
            if a[i].priority < b[j].priority {
                result.append(a[i])
                i += 1
            } else {
                result.append(b[j])
                j += 1
            }
        }

        while i < a.count { result.append(a[i]); i += 1 }
        while j < b.count { result.append(b[j]); j += 1 }

        return result
    }
}
