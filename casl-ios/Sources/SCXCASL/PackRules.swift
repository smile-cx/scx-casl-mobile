// Copyright (c) 2026 Smile.CX Srl
// SPDX-License-Identifier: MIT
//
// This file is part of a native Swift port of CASL (https://github.com/stalniy/casl)
// by Sergii Stotskyi, used under the MIT License. See the NOTICE file for details.

import Foundation

/// Pack rules into a compact array format.
/// Format: [actionStr, subjectStr, conditions|0, inverted(1|0), fieldsStr|0, reason]
/// Trailing falsy values are trimmed.
public func packRules(_ rules: [RawRule], packSubject: ((String) -> String)? = nil) -> [[Any]] {
    return rules.map { rule in
        let actionStr = rule.action.values.joined(separator: ",")
        let subjects = rule.subject?.values ?? []
        let subjectStr: String
        if let pack = packSubject {
            subjectStr = subjects.map { pack($0) }.joined(separator: ",")
        } else {
            subjectStr = subjects.joined(separator: ",")
        }

        // Filter __caslSubjectType__ from conditions before packing.
        // In JS, __caslSubjectType__ is set via Object.defineProperty (non-enumerable),
        // so it never appears in JSON serialization. In Swift dictionaries, we must
        // explicitly filter it out to maintain parity.
        let conditions: Any
        if var conds = rule.conditions {
            conds.removeValue(forKey: "__caslSubjectType__")
            conditions = conds.isEmpty ? 0 : conds
        } else {
            conditions = 0
        }
        let inverted: Any = rule.inverted ? 1 : 0
        let fieldsStr: Any
        if let fields = rule.fields?.values, !fields.isEmpty {
            fieldsStr = fields.joined(separator: ",")
        } else {
            fieldsStr = 0
        }
        let reason: Any = rule.reason ?? 0

        var packed: [Any] = [actionStr, subjectStr, conditions, inverted, fieldsStr, reason]

        // Trim trailing falsy values
        while let last = packed.last {
            if isFalsy(last) {
                packed.removeLast()
            } else {
                break
            }
        }

        return packed
    }
}

/// Unpack rules from compact array format back to RawRule array.
public func unpackRules(_ packedRules: [[Any]], unpackSubject: ((String) -> String)? = nil) -> [RawRule] {
    return packedRules.map { packed in
        let actionStr = packed.count > 0 ? "\(packed[0])" : ""
        let subjectStr = packed.count > 1 ? "\(packed[1])" : ""
        let conditionsRaw = packed.count > 2 ? packed[2] : 0
        let invertedRaw = packed.count > 3 ? packed[3] : 0
        let fieldsRaw = packed.count > 4 ? packed[4] : 0
        let reasonRaw = packed.count > 5 ? packed[5] : 0

        let actions = actionStr.split(separator: ",").map(String.init)
        let action: StringOrArray = actions.count == 1 ? .single(actions[0]) : .array(actions)

        var subject: StringOrArray?
        if !subjectStr.isEmpty {
            var subjects = subjectStr.split(separator: ",").map(String.init)
            if let unpack = unpackSubject {
                subjects = subjects.map { unpack($0) }
            }
            subject = subjects.count == 1 ? .single(subjects[0]) : .array(subjects)
        }

        var conditions: [String: Any]?
        if let dict = conditionsRaw as? [String: Any] {
            conditions = dict
        }

        let inverted: Bool
        if let num = invertedRaw as? Int {
            inverted = num == 1
        } else if let num = invertedRaw as? NSNumber {
            inverted = num.intValue == 1
        } else {
            inverted = false
        }

        var fields: StringOrArray?
        if let str = fieldsRaw as? String, !str.isEmpty {
            let fieldArr = str.split(separator: ",").map(String.init)
            fields = fieldArr.count == 1 ? .single(fieldArr[0]) : .array(fieldArr)
        }

        var reason: String?
        if let str = reasonRaw as? String, !str.isEmpty {
            reason = str
        }

        return RawRule(
            action: action,
            subject: subject,
            fields: fields,
            conditions: conditions,
            inverted: inverted,
            reason: reason
        )
    }
}

private func isFalsy(_ value: Any) -> Bool {
    if let num = value as? Int, num == 0 { return true }
    if let str = value as? String, str.isEmpty { return true }
    if value is NSNull { return true }
    return false
}
