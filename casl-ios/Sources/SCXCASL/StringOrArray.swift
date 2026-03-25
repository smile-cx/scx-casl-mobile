// Copyright (c) 2026 Smile.CX Srl
// SPDX-License-Identifier: MIT
//
// This file is part of a native Swift port of CASL (https://github.com/stalniy/casl)
// by Sergii Stotskyi, used under the MIT License. See the NOTICE file for details.

import Foundation

public enum StringOrArray: Equatable {
    case single(String)
    case array([String])

    public var values: [String] {
        switch self {
        case .single(let s):
            return [s]
        case .array(let arr):
            return arr
        }
    }
}

extension StringOrArray: ExpressibleByStringLiteral {
    public init(stringLiteral value: String) {
        self = .single(value)
    }
}

extension StringOrArray: ExpressibleByArrayLiteral {
    public init(arrayLiteral elements: String...) {
        self = .array(elements)
    }
}

extension StringOrArray: Codable {
    public init(from decoder: Decoder) throws {
        let container = try decoder.singleValueContainer()
        if let str = try? container.decode(String.self) {
            self = .single(str)
        } else if let arr = try? container.decode([String].self) {
            self = .array(arr)
        } else {
            throw DecodingError.typeMismatch(
                StringOrArray.self,
                DecodingError.Context(codingPath: decoder.codingPath, debugDescription: "Expected String or [String]")
            )
        }
    }

    public func encode(to encoder: Encoder) throws {
        var container = encoder.singleValueContainer()
        switch self {
        case .single(let str):
            try container.encode(str)
        case .array(let arr):
            try container.encode(arr)
        }
    }
}
