// Copyright (c) 2026 Smile.CX Srl
// SPDX-License-Identifier: MIT
//
// This file is part of a native Swift port of CASL (https://github.com/stalniy/casl)
// by Sergii Stotskyi, used under the MIT License. See the NOTICE file for details.

import Foundation

public struct RawRule: Equatable {
    public var action: StringOrArray
    public var subject: StringOrArray?
    public var fields: StringOrArray?
    public var conditions: [String: Any]?
    public var inverted: Bool
    public var reason: String?

    public init(
        action: StringOrArray,
        subject: StringOrArray? = nil,
        fields: StringOrArray? = nil,
        conditions: [String: Any]? = nil,
        inverted: Bool = false,
        reason: String? = nil
    ) {
        self.action = action
        self.subject = subject
        self.fields = fields
        self.conditions = conditions
        self.inverted = inverted
        self.reason = reason
    }

    public static func == (lhs: RawRule, rhs: RawRule) -> Bool {
        if lhs.action != rhs.action { return false }
        if lhs.subject != rhs.subject { return false }
        if lhs.fields != rhs.fields { return false }
        if lhs.inverted != rhs.inverted { return false }
        if lhs.reason != rhs.reason { return false }

        // Compare conditions using JSON serialization
        switch (lhs.conditions, rhs.conditions) {
        case (nil, nil):
            return true
        case (nil, _), (_, nil):
            return false
        case (let a?, let b?):
            guard let aData = try? JSONSerialization.data(withJSONObject: a, options: .sortedKeys),
                  let bData = try? JSONSerialization.data(withJSONObject: b, options: .sortedKeys) else {
                return false
            }
            return aData == bData
        }
    }
}

// MARK: - Codable

extension RawRule: Codable {
    enum CodingKeys: String, CodingKey {
        case action, subject, fields, conditions, inverted, reason
    }

    public init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        action = try container.decode(StringOrArray.self, forKey: .action)
        subject = try container.decodeIfPresent(StringOrArray.self, forKey: .subject)
        fields = try container.decodeIfPresent(StringOrArray.self, forKey: .fields)
        inverted = try container.decodeIfPresent(Bool.self, forKey: .inverted) ?? false
        reason = try container.decodeIfPresent(String.self, forKey: .reason)

        // conditions
        if container.contains(.conditions) {
            let conditionsValue = try container.decode(AnyCodableValue.self, forKey: .conditions)
            if case .dictionary(let dict) = conditionsValue {
                conditions = dict
            } else {
                conditions = nil
            }
        } else {
            conditions = nil
        }
    }

    public func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        try container.encode(action, forKey: .action)
        try container.encodeIfPresent(subject, forKey: .subject)
        try container.encodeIfPresent(fields, forKey: .fields)
        if let conditions = conditions {
            try container.encode(AnyCodableValue.dictionary(conditions), forKey: .conditions)
        }
        if inverted {
            try container.encode(inverted, forKey: .inverted)
        }
        try container.encodeIfPresent(reason, forKey: .reason)
    }
}

// MARK: - AnyCodableValue

/// A recursive Codable wrapper for `Any` values used in conditions dictionaries.
public enum AnyCodableValue: Codable, Equatable {
    case null
    case bool(Bool)
    case int(Int)
    case double(Double)
    case string(String)
    case array([AnyCodableValue])
    case dictionary([String: Any])

    public init(from decoder: Decoder) throws {
        let container = try decoder.singleValueContainer()

        if container.decodeNil() {
            self = .null
            return
        }
        // Try Bool before Int/Double since Bool decodes as Int in JSON
        if let val = try? container.decode(Bool.self) {
            self = .bool(val)
            return
        }
        if let val = try? container.decode(Int.self) {
            self = .int(val)
            return
        }
        if let val = try? container.decode(Double.self) {
            self = .double(val)
            return
        }
        if let val = try? container.decode(String.self) {
            self = .string(val)
            return
        }
        if let arr = try? container.decode([AnyCodableValue].self) {
            self = .array(arr)
            return
        }
        if let dict = try? container.decode([String: AnyCodableValue].self) {
            self = .dictionary(AnyCodableValue.toAnyDict(dict))
            return
        }
        throw DecodingError.typeMismatch(
            AnyCodableValue.self,
            DecodingError.Context(codingPath: decoder.codingPath, debugDescription: "Cannot decode AnyCodableValue")
        )
    }

    public func encode(to encoder: Encoder) throws {
        var container = encoder.singleValueContainer()
        switch self {
        case .null:
            try container.encodeNil()
        case .bool(let val):
            try container.encode(val)
        case .int(let val):
            try container.encode(val)
        case .double(let val):
            try container.encode(val)
        case .string(let val):
            try container.encode(val)
        case .array(let arr):
            try container.encode(arr)
        case .dictionary(let dict):
            let codableDict = AnyCodableValue.fromAnyDict(dict)
            try container.encode(codableDict)
        }
    }

    public static func == (lhs: AnyCodableValue, rhs: AnyCodableValue) -> Bool {
        switch (lhs, rhs) {
        case (.null, .null): return true
        case (.bool(let a), .bool(let b)): return a == b
        case (.int(let a), .int(let b)): return a == b
        case (.double(let a), .double(let b)): return a == b
        case (.string(let a), .string(let b)): return a == b
        case (.array(let a), .array(let b)): return a == b
        case (.dictionary(let a), .dictionary(let b)):
            guard let aData = try? JSONSerialization.data(withJSONObject: a, options: .sortedKeys),
                  let bData = try? JSONSerialization.data(withJSONObject: b, options: .sortedKeys) else {
                return false
            }
            return aData == bData
        default: return false
        }
    }

    // Convert [String: AnyCodableValue] -> [String: Any]
    static func toAnyDict(_ dict: [String: AnyCodableValue]) -> [String: Any] {
        var result: [String: Any] = [:]
        for (key, value) in dict {
            result[key] = value.toAny()
        }
        return result
    }

    // Convert [String: Any] -> [String: AnyCodableValue]
    static func fromAnyDict(_ dict: [String: Any]) -> [String: AnyCodableValue] {
        var result: [String: AnyCodableValue] = [:]
        for (key, value) in dict {
            result[key] = AnyCodableValue.from(value)
        }
        return result
    }

    func toAny() -> Any {
        switch self {
        case .null: return NSNull()
        case .bool(let val): return val
        case .int(let val): return val
        case .double(let val): return val
        case .string(let val): return val
        case .array(let arr): return arr.map { $0.toAny() }
        case .dictionary(let dict): return dict
        }
    }

    static func from(_ value: Any) -> AnyCodableValue {
        // Check Bool before numeric types
        if let val = value as? Bool { return .bool(val) }
        if let val = value as? Int { return .int(val) }
        if let val = value as? Double { return .double(val) }
        if let val = value as? String { return .string(val) }
        if let arr = value as? [Any] { return .array(arr.map { from($0) }) }
        if let dict = value as? [String: Any] { return .dictionary(dict) }
        if value is NSNull { return .null }
        return .null
    }
}
