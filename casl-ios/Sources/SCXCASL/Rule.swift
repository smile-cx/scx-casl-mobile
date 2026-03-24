// Copyright (c) 2026 [PROJECT OR COMPANY NAME]
// SPDX-License-Identifier: MIT
//
// This file is part of a native Swift port of CASL (https://github.com/stalniy/casl)
// by Sergii Stotskyi, used under the MIT License. See the NOTICE file for details.

import Foundation

public struct RuleOptions {
    public var conditionsMatcher: (([String: Any]) -> ([String: Any]) -> Bool)?
    public var fieldMatcher: (([String]) -> (String) -> Bool)?
    public var resolveAction: (([String]) -> [String])?

    public init(
        conditionsMatcher: (([String: Any]) -> ([String: Any]) -> Bool)? = nil,
        fieldMatcher: (([String]) -> (String) -> Bool)? = nil,
        resolveAction: (([String]) -> [String])? = nil
    ) {
        self.conditionsMatcher = conditionsMatcher
        self.fieldMatcher = fieldMatcher
        self.resolveAction = resolveAction
    }
}

public class Rule {
    public let action: [String]
    public let subject: [String]?
    public let inverted: Bool
    public let conditions: [String: Any]?
    public let fields: [String]?
    public let reason: String?
    public let origin: RawRule
    public let priority: Int

    private var _matchesConditions: (([String: Any]) -> Bool)?
    private var _matchesField: ((String) -> Bool)?

    public init(rawRule: RawRule, options: RuleOptions, priority: Int = 0) {
        let actions = rawRule.action.values
        self.action = options.resolveAction?(actions) ?? actions
        self.subject = rawRule.subject?.values
        self.inverted = rawRule.inverted
        self.conditions = rawRule.conditions
        self.fields = rawRule.fields?.values
        self.reason = rawRule.reason
        self.origin = rawRule
        self.priority = priority

        if let conditions = rawRule.conditions, let matcher = options.conditionsMatcher {
            self._matchesConditions = matcher(conditions)
        }
        if let fields = rawRule.fields?.values, let matcher = options.fieldMatcher {
            self._matchesField = matcher(fields)
        }
    }

    public func matchesConditions(_ object: Any?) -> Bool {
        if conditions == nil {
            return true
        }
        if object == nil || object is String {
            return !inverted
        }
        if let dict = object as? [String: Any], let matcher = _matchesConditions {
            return matcher(dict)
        }
        return !inverted
    }

    public func matchesField(_ field: String?) -> Bool {
        if fields == nil {
            return true
        }
        guard let field = field else {
            return !inverted
        }
        if let matcher = _matchesField {
            return matcher(field)
        }
        // Fallback: simple contains
        return fields?.contains(field) ?? false
    }
}
