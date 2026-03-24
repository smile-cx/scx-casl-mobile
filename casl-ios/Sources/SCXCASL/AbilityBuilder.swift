// Copyright (c) 2026 [PROJECT OR COMPANY NAME]
// SPDX-License-Identifier: MIT
//
// This file is part of a native Swift port of CASL (https://github.com/stalniy/casl)
// by Sergii Stotskyi, used under the MIT License. See the NOTICE file for details.

import Foundation

public class RuleBuilder {
    private var ruleIndex: Int
    private weak var builder: AbilityBuilder?

    init(builder: AbilityBuilder, ruleIndex: Int) {
        self.builder = builder
        self.ruleIndex = ruleIndex
    }

    @discardableResult
    public func because(_ reason: String) -> RuleBuilder {
        builder?.rules[ruleIndex].reason = reason
        return self
    }
}

public class AbilityBuilder {
    public var rules: [RawRule] = []

    public init() {}

    @discardableResult
    public func can(_ action: StringOrArray, _ subject: StringOrArray? = nil, fields: StringOrArray? = nil, conditions: [String: Any]? = nil) -> RuleBuilder {
        let rule = RawRule(
            action: action,
            subject: subject,
            fields: fields,
            conditions: conditions,
            inverted: false
        )
        rules.append(rule)
        return RuleBuilder(builder: self, ruleIndex: rules.count - 1)
    }

    /// Shorthand: can(action, subject, conditions:) without fields
    @discardableResult
    public func can(_ action: StringOrArray, _ subject: StringOrArray, conditions: [String: Any]) -> RuleBuilder {
        return can(action, subject, fields: nil, conditions: conditions)
    }

    @discardableResult
    public func cannot(_ action: StringOrArray, _ subject: StringOrArray? = nil, fields: StringOrArray? = nil, conditions: [String: Any]? = nil) -> RuleBuilder {
        let rule = RawRule(
            action: action,
            subject: subject,
            fields: fields,
            conditions: conditions,
            inverted: true
        )
        rules.append(rule)
        return RuleBuilder(builder: self, ruleIndex: rules.count - 1)
    }

    /// Shorthand: cannot(action, subject, conditions:) without fields
    @discardableResult
    public func cannot(_ action: StringOrArray, _ subject: StringOrArray, conditions: [String: Any]) -> RuleBuilder {
        return cannot(action, subject, fields: nil, conditions: conditions)
    }

    public func build(options: AbilityOptions = AbilityOptions()) -> Ability {
        var opts = options
        if opts.conditionsMatcher == nil {
            opts.conditionsMatcher = ConditionsMatcher.match
        }
        if opts.fieldMatcher == nil {
            opts.fieldMatcher = FieldMatcher.match
        }
        return Ability(rules: rules, options: opts)
    }
}

public func defineAbility(_ define: (AbilityBuilder) -> Void, options: AbilityOptions = AbilityOptions()) -> Ability {
    let builder = AbilityBuilder()
    define(builder)
    return builder.build(options: options)
}

public func defineAbility(_ define: (AbilityBuilder) async -> Void, options: AbilityOptions = AbilityOptions()) async -> Ability {
    let builder = AbilityBuilder()
    await define(builder)
    return builder.build(options: options)
}
