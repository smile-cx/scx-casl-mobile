// Copyright (c) 2026 Smile.CX Srl
// SPDX-License-Identifier: MIT
//
// This file is part of a native Swift port of CASL (https://github.com/stalniy/casl)
// by Sergii Stotskyi, used under the MIT License. See the NOTICE file for details.

import Foundation

// MARK: - JSON Errors

public enum CASLJSONError: Error, LocalizedError {
    case invalidJSON(String)
    case invalidJSONData
    case invalidRuleDictionary(String)
    case missingRequiredField(String)

    public var errorDescription: String? {
        switch self {
        case .invalidJSON(let detail):
            return "Invalid JSON: \(detail)"
        case .invalidJSONData:
            return "Invalid JSON data"
        case .invalidRuleDictionary(let detail):
            return "Invalid rule dictionary: \(detail)"
        case .missingRequiredField(let field):
            return "Missing required field: \(field)"
        }
    }
}

// MARK: - Ability JSON Convenience

public extension Ability {

    /// Create Ability from a JSON string containing an array of rules.
    convenience init(json: String, options: AbilityOptions = AbilityOptions()) throws {
        guard let data = json.data(using: .utf8) else {
            throw CASLJSONError.invalidJSON("Unable to convert string to UTF-8 data")
        }
        try self.init(jsonData: data, options: options)
    }

    /// Create Ability from JSON Data containing an array of rules.
    convenience init(jsonData data: Data, options: AbilityOptions = AbilityOptions()) throws {
        let decoder = JSONDecoder()
        let rules: [RawRule]
        do {
            rules = try decoder.decode([RawRule].self, from: data)
        } catch {
            throw CASLJSONError.invalidJSON(error.localizedDescription)
        }
        self.init(rules: rules, options: options)
    }

    /// Create Ability from an array of dictionaries (e.g., from JSONSerialization).
    convenience init(rulesArray: [[String: Any]], options: AbilityOptions = AbilityOptions()) throws {
        let rules = try rulesArray.map { try RawRule(dictionary: $0) }
        self.init(rules: rules, options: options)
    }

    /// Update rules from a JSON string.
    func update(json: String) throws {
        guard let data = json.data(using: .utf8) else {
            throw CASLJSONError.invalidJSON("Unable to convert string to UTF-8 data")
        }
        try update(jsonData: data)
    }

    /// Update rules from JSON Data.
    func update(jsonData data: Data) throws {
        let decoder = JSONDecoder()
        let rules: [RawRule]
        do {
            rules = try decoder.decode([RawRule].self, from: data)
        } catch {
            throw CASLJSONError.invalidJSON(error.localizedDescription)
        }
        update(rules)
    }

    /// Update rules from an array of dictionaries.
    func update(rulesArray: [[String: Any]]) throws {
        let rules = try rulesArray.map { try RawRule(dictionary: $0) }
        update(rules)
    }

    /// Export current rules as a JSON string.
    func rulesToJSON() throws -> String {
        let data = try rulesToJSONData()
        guard let string = String(data: data, encoding: .utf8) else {
            throw CASLJSONError.invalidJSONData
        }
        return string
    }

    /// Export current rules as JSON Data.
    func rulesToJSONData() throws -> Data {
        let encoder = JSONEncoder()
        encoder.outputFormatting = [.sortedKeys]
        return try encoder.encode(rules)
    }
}

// MARK: - RawRule from Dictionary

public extension RawRule {

    /// Create a RawRule from a `[String: Any]` dictionary.
    init(dictionary dict: [String: Any]) throws {
        // action (required)
        guard let actionValue = dict["action"] else {
            throw CASLJSONError.missingRequiredField("action")
        }
        let action: StringOrArray
        if let str = actionValue as? String {
            action = .single(str)
        } else if let arr = actionValue as? [String] {
            action = .array(arr)
        } else {
            throw CASLJSONError.invalidRuleDictionary("'action' must be a string or array of strings")
        }

        // subject (optional)
        var subject: StringOrArray?
        if let subjectValue = dict["subject"] {
            if let str = subjectValue as? String {
                subject = .single(str)
            } else if let arr = subjectValue as? [String] {
                subject = .array(arr)
            } else {
                throw CASLJSONError.invalidRuleDictionary("'subject' must be a string or array of strings")
            }
        }

        // fields (optional)
        var fields: StringOrArray?
        if let fieldsValue = dict["fields"] {
            if let str = fieldsValue as? String {
                fields = .single(str)
            } else if let arr = fieldsValue as? [String] {
                fields = .array(arr)
            } else {
                throw CASLJSONError.invalidRuleDictionary("'fields' must be a string or array of strings")
            }
        }

        // conditions (optional)
        var conditions: [String: Any]?
        if let conditionsValue = dict["conditions"] as? [String: Any] {
            conditions = conditionsValue
        }

        // inverted (optional, defaults to false)
        let inverted: Bool
        if let inv = dict["inverted"] as? Bool {
            inverted = inv
        } else if let inv = dict["inverted"] as? Int {
            inverted = inv != 0
        } else {
            inverted = false
        }

        // reason (optional)
        let reason = dict["reason"] as? String

        self.init(
            action: action,
            subject: subject,
            fields: fields,
            conditions: conditions,
            inverted: inverted,
            reason: reason
        )
    }
}

// MARK: - RawRule Convenience Static Methods

public extension RawRule {

    /// Parse a JSON string containing an array of rules into [RawRule].
    static func listFromJSON(_ json: String) throws -> [RawRule] {
        guard let data = json.data(using: .utf8) else {
            throw CASLJSONError.invalidJSON("Unable to convert string to UTF-8 data")
        }
        return try listFromJSON(data: data)
    }

    /// Parse JSON Data containing an array of rules into [RawRule].
    static func listFromJSON(data: Data) throws -> [RawRule] {
        let decoder = JSONDecoder()
        do {
            return try decoder.decode([RawRule].self, from: data)
        } catch {
            throw CASLJSONError.invalidJSON(error.localizedDescription)
        }
    }
}

// MARK: - Ability Convenience Static Methods

public extension Ability {

    /// Create an Ability from a list of RawRule with default matchers.
    /// Automatically provides ConditionsMatcher and FieldMatcher if not set in options.
    static func fromRules(_ rules: [RawRule], options: AbilityOptions = AbilityOptions()) -> Ability {
        var opts = options
        if opts.conditionsMatcher == nil {
            opts.conditionsMatcher = ConditionsMatcher.match
        }
        if opts.fieldMatcher == nil {
            opts.fieldMatcher = FieldMatcher.match
        }
        return Ability(rules: rules, options: opts)
    }

    /// Create an Ability from a JSON string containing an array of rules.
    /// Automatically provides ConditionsMatcher and FieldMatcher if not set in options.
    static func fromJSON(_ json: String, options: AbilityOptions = AbilityOptions()) throws -> Ability {
        let rules = try RawRule.listFromJSON(json)
        return fromRules(rules, options: options)
    }
}

// MARK: - Subject from JSON

/// Create a typed subject dictionary from a JSON string.
/// Parses the JSON and sets the `__caslSubjectType__` key.
public func subject(fromJSON json: String, type: String) throws -> [String: Any] {
    guard let data = json.data(using: .utf8) else {
        throw CASLJSONError.invalidJSON("Unable to convert string to UTF-8 data")
    }
    let parsed: Any
    do {
        parsed = try JSONSerialization.jsonObject(with: data)
    } catch {
        throw CASLJSONError.invalidJSON(error.localizedDescription)
    }
    guard let dict = parsed as? [String: Any] else {
        throw CASLJSONError.invalidJSON("JSON must be a dictionary")
    }
    return subject(fromDictionary: dict, type: type)
}

/// Create a typed subject dictionary from an existing dictionary.
/// Sets the `__caslSubjectType__` key to the given type.
public func subject(fromDictionary dict: [String: Any], type: String) -> [String: Any] {
    var result = dict
    result["__caslSubjectType__"] = type
    return result
}

// MARK: - PackRules JSON Support

/// Pack rules and return the result as a JSON string.
public func packRulesToJSON(_ rules: [RawRule]) throws -> String {
    let packed = packRules(rules)
    let data = try JSONSerialization.data(withJSONObject: packed, options: [.sortedKeys])
    guard let string = String(data: data, encoding: .utf8) else {
        throw CASLJSONError.invalidJSONData
    }
    return string
}

/// Unpack rules from a JSON string.
public func unpackRulesFromJSON(_ json: String) throws -> [RawRule] {
    guard let data = json.data(using: .utf8) else {
        throw CASLJSONError.invalidJSON("Unable to convert string to UTF-8 data")
    }
    let parsed: Any
    do {
        parsed = try JSONSerialization.jsonObject(with: data)
    } catch {
        throw CASLJSONError.invalidJSON(error.localizedDescription)
    }
    guard let packed = parsed as? [[Any]] else {
        throw CASLJSONError.invalidJSON("Expected an array of arrays")
    }
    return unpackRules(packed)
}
