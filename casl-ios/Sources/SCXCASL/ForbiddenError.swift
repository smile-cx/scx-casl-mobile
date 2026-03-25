// Copyright (c) 2026 Smile.CX Srl
// SPDX-License-Identifier: MIT
//
// This file is part of a native Swift port of CASL (https://github.com/stalniy/casl)
// by Sergii Stotskyi, used under the MIT License. See the NOTICE file for details.

import Foundation

public class ForbiddenError: Error, CustomStringConvertible {
    public let ability: Ability
    public private(set) var action: String
    public private(set) var subject: Any?
    public private(set) var subjectType: String
    public private(set) var field: String?
    public private(set) var message: String

    public static var defaultErrorMessage: (ForbiddenError) -> String = { error in
        "Cannot execute \"\(error.action)\" on \"\(error.subjectType)\""
    }

    public var description: String {
        return message
    }

    private init(ability: Ability) {
        self.ability = ability
        self.action = ""
        self.subject = nil
        self.subjectType = ""
        self.field = nil
        self.message = ""
    }

    public static func from(_ ability: Ability) -> ForbiddenError {
        return ForbiddenError(ability: ability)
    }

    @discardableResult
    public func setMessage(_ message: String) -> ForbiddenError {
        self.message = message
        return self
    }

    public func throwUnlessCan(_ action: String, _ subject: Any? = nil, field: String? = nil) throws {
        if let error = unlessCan(action, subject, field: field) {
            throw error
        }
    }

    public func unlessCan(_ action: String, _ subject: Any? = nil, field: String? = nil) -> ForbiddenError? {
        let rule = ability.relevantRuleFor(action, subject, field: field)
        if let rule = rule, !rule.inverted {
            return nil
        }

        self.action = action
        self.subject = subject
        self.subjectType = ability.detectSubjectType(subject)
        self.field = field

        if let rule = rule, rule.inverted, let reason = rule.reason {
            self.message = reason
        } else if self.message.isEmpty {
            self.message = ForbiddenError.defaultErrorMessage(self)
        }

        return self
    }
}
