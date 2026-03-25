// Copyright (c) 2026 Smile.CX Srl
// SPDX-License-Identifier: MIT
//
// This file is part of a native Swift port of CASL (https://github.com/stalniy/casl)
// by Sergii Stotskyi, used under the MIT License. See the NOTICE file for details.

import Foundation

/// Custom operator handler type.
/// Takes (fieldValue, expectedOperandValue) and returns a Bool.
public typealias CustomOperatorHandler = (_ fieldValue: Any?, _ expected: Any) -> Bool

public struct ConditionsMatcher {

    /// Registry of custom operator handlers, keyed by operator name (e.g., "$myOp").
    /// These are checked BEFORE the built-in operators, allowing overrides.
    private static var _customOperators: [String: CustomOperatorHandler] = [:]

    /// Registers a custom operator handler. Matches JS `buildMongoQueryMatcher` extensibility.
    /// Example: `ConditionsMatcher.registerOperator("$startsWith") { fv, exp in ... }`
    public static func registerOperator(_ name: String, handler: @escaping CustomOperatorHandler) {
        _customOperators[name] = handler
    }

    /// Removes a previously registered custom operator.
    public static func unregisterOperator(_ name: String) {
        _customOperators.removeValue(forKey: name)
    }

    /// Removes all custom operators.
    public static func clearCustomOperators() {
        _customOperators.removeAll()
    }

    /// Creates a new match function with additional custom operators merged with defaults.
    /// This mirrors JS `buildMongoQueryMatcher(instructions, interpreters)`.
    public static func buildMatcher(
        customOperators: [String: CustomOperatorHandler]
    ) -> ([String: Any]) -> ([String: Any]) -> Bool {
        return { conditions in
            return { object in
                matchObject(conditions: conditions, object: object, extraOperators: customOperators)
            }
        }
    }

    public static func match(conditions: [String: Any]) -> ([String: Any]) -> Bool {
        return { object in
            matchObject(conditions: conditions, object: object)
        }
    }

    // MARK: - Private

    private static func matchObject(conditions: [String: Any], object: [String: Any], extraOperators: [String: CustomOperatorHandler]? = nil) -> Bool {
        let ops = mergedOperators(extraOperators)
        for (key, conditionValue) in conditions {
            // Skip __caslSubjectType__ — it's metadata, not a condition
            if key == "__caslSubjectType__" { continue }

            switch key {
            case "$and":
                guard let arr = conditionValue as? [[String: Any]] else { return false }
                if !arr.allSatisfy({ matchObject(conditions: $0, object: object, extraOperators: extraOperators) }) {
                    return false
                }
            case "$or":
                guard let arr = conditionValue as? [[String: Any]] else { return false }
                if !arr.contains(where: { matchObject(conditions: $0, object: object, extraOperators: extraOperators) }) {
                    return false
                }
            case "$not":
                guard let sub = conditionValue as? [String: Any] else { return false }
                if matchObject(conditions: sub, object: object, extraOperators: extraOperators) {
                    return false
                }
            default:
                if !matchField(key: key, conditionValue: conditionValue, object: object, customOps: ops) {
                    return false
                }
            }
        }
        return true
    }

    private static func mergedOperators(_ extra: [String: CustomOperatorHandler]?) -> [String: CustomOperatorHandler] {
        if let extra = extra {
            return _customOperators.merging(extra) { _, new in new }
        }
        return _customOperators
    }

    private static func matchField(key: String, conditionValue: Any, object: [String: Any], customOps: [String: CustomOperatorHandler] = [:]) -> Bool {
        let fieldValue = resolveField(key: key, in: object)
        if let operators = conditionValue as? [String: Any], hasOperatorKeys(operators) {
            return matchOperators(operators: operators, fieldValue: fieldValue, customOps: customOps)
        } else {
            // Default is $eq
            return evalEq(fieldValue: fieldValue, expected: conditionValue)
        }
    }

    private static func hasOperatorKeys(_ dict: [String: Any]) -> Bool {
        return dict.keys.contains(where: { $0.hasPrefix("$") })
    }

    private static func resolveField(key: String, in object: [String: Any]) -> Any? {
        let parts = key.split(separator: ".").map(String.init)
        var current: Any? = object
        for part in parts {
            if let dict = current as? [String: Any] {
                current = dict[part]
            } else if let arr = current as? [Any], let index = Int(part), index >= 0, index < arr.count {
                current = arr[index]
            } else {
                return nil
            }
        }
        return current
    }

    // MARK: - Operators

    static func matchOperators(operators: [String: Any], fieldValue: Any?, customOps: [String: CustomOperatorHandler] = [:]) -> Bool {
        for (op, expected) in operators {
            // Check custom operators first
            if let handler = customOps[op] {
                if !handler(fieldValue, expected) { return false }
                continue
            }
            switch op {
            case "$eq":
                if !evalEq(fieldValue: fieldValue, expected: expected) { return false }
            case "$ne":
                if evalEq(fieldValue: fieldValue, expected: expected) { return false }
            case "$gt":
                if !evalCompare(fieldValue: fieldValue, expected: expected, compare: >) { return false }
            case "$gte":
                if !evalCompare(fieldValue: fieldValue, expected: expected, compare: >=) { return false }
            case "$lt":
                if !evalCompare(fieldValue: fieldValue, expected: expected, compare: <) { return false }
            case "$lte":
                if !evalCompare(fieldValue: fieldValue, expected: expected, compare: <=) { return false }
            case "$in":
                if let arr = expected as? [Any] {
                    if !evalIn(fieldValue: fieldValue, array: arr) { return false }
                } else {
                    return false
                }
            case "$nin":
                if let arr = expected as? [Any] {
                    if evalIn(fieldValue: fieldValue, array: arr) { return false }
                } else {
                    return false
                }
            case "$all":
                if let expectedArr = expected as? [Any] {
                    if !evalAll(fieldValue: fieldValue, expected: expectedArr) { return false }
                } else {
                    return false
                }
            case "$size":
                if let expectedSize = toInt(expected) {
                    if let arr = fieldValue as? [Any] {
                        if arr.count != expectedSize { return false }
                    } else {
                        return false
                    }
                } else {
                    return false
                }
            case "$regex":
                let options = (operators["$options"] as? String) ?? ""
                if !evalRegex(fieldValue: fieldValue, pattern: expected, options: options) { return false }
            case "$options":
                // handled by $regex
                continue
            case "$exists":
                let shouldExist = expected as? Bool ?? true
                let exists = fieldValue != nil && !(fieldValue is NSNull)
                if shouldExist != exists { return false }
            case "$elemMatch":
                if let conditions = expected as? [String: Any] {
                    if !evalElemMatch(fieldValue: fieldValue, conditions: conditions) { return false }
                } else {
                    return false
                }
            default:
                return false
            }
        }
        return true
    }

    private static func evalEq(fieldValue: Any?, expected: Any) -> Bool {
        // If fieldValue is array, check if any element equals
        if let arr = fieldValue as? [Any] {
            return arr.contains(where: { anyEquals($0, expected) })
        }
        return anyEquals(fieldValue, expected)
    }

    static func anyEquals(_ a: Any?, _ b: Any?) -> Bool {
        if a == nil && b == nil { return true }
        guard let a = a, let b = b else { return false }

        if let a = a as? String, let b = b as? String { return a == b }
        if let a = toDouble(a), let b = toDouble(b) { return a == b }
        if let a = a as? Bool, let b = b as? Bool { return a == b }
        // Compare NSNumber bools carefully
        if let a = a as? NSNumber, let b = b as? NSNumber { return a == b }

        return false
    }

    private static func evalCompare(fieldValue: Any?, expected: Any, compare: (Double, Double) -> Bool) -> Bool {
        guard let fv = toDouble(fieldValue), let ev = toDouble(expected) else { return false }
        return compare(fv, ev)
    }

    private static func evalIn(fieldValue: Any?, array: [Any]) -> Bool {
        if let fieldArr = fieldValue as? [Any] {
            // If field is array, check if ANY element of field is in the specified array
            return fieldArr.contains(where: { fv in
                array.contains(where: { anyEquals(fv, $0) })
            })
        }
        return array.contains(where: { anyEquals(fieldValue, $0) })
    }

    private static func evalAll(fieldValue: Any?, expected: [Any]) -> Bool {
        guard let fieldArr = fieldValue as? [Any] else { return false }
        return expected.allSatisfy { exp in
            fieldArr.contains(where: { anyEquals($0, exp) })
        }
    }

    private static func evalRegex(fieldValue: Any?, pattern: Any, options: String) -> Bool {
        guard let str = fieldValue as? String, let patternStr = pattern as? String else { return false }
        var regexOptions: NSRegularExpression.Options = []
        if options.contains("i") {
            regexOptions.insert(.caseInsensitive)
        }
        if options.contains("m") {
            regexOptions.insert(.anchorsMatchLines)
        }
        if options.contains("s") {
            regexOptions.insert(.dotMatchesLineSeparators)
        }
        guard let regex = try? NSRegularExpression(pattern: patternStr, options: regexOptions) else { return false }
        let range = NSRange(str.startIndex..., in: str)
        return regex.firstMatch(in: str, range: range) != nil
    }

    private static func evalElemMatch(fieldValue: Any?, conditions: [String: Any]) -> Bool {
        // Case 1: Array of dictionaries - match sub-conditions against each dict element
        if let arr = fieldValue as? [[String: Any]] {
            return arr.contains(where: { matchObject(conditions: conditions, object: $0) })
        }

        // Case 2: Array of scalars with operator conditions (e.g., {"$gt": 5})
        // When conditions use operators like $gt, $lt, $eq, etc., treat each scalar
        // element as the field value and evaluate operators against it.
        if let arr = fieldValue as? [Any], hasOperatorKeys(conditions) {
            return arr.contains(where: { element in
                matchOperators(operators: conditions, fieldValue: element)
            })
        }

        // Case 3: Mixed array (dicts + scalars) with field-level conditions —
        // filter to dict elements only and check each against the conditions.
        if let arr = fieldValue as? [Any] {
            return arr.compactMap({ $0 as? [String: Any] })
                      .contains(where: { matchObject(conditions: conditions, object: $0) })
        }

        return false
    }

    private static func toDouble(_ value: Any?) -> Double? {
        if let i = value as? Int { return Double(i) }
        if let d = value as? Double { return d }
        if let n = value as? NSNumber {
            // Avoid treating bools as numbers
            if n === kCFBooleanTrue || n === kCFBooleanFalse { return nil }
            return n.doubleValue
        }
        return nil
    }

    private static func toInt(_ value: Any?) -> Int? {
        if let i = value as? Int { return i }
        if let d = value as? Double { return Int(d) }
        if let n = value as? NSNumber { return n.intValue }
        return nil
    }
}
