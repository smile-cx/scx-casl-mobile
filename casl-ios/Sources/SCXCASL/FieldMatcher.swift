// Copyright (c) 2026 Smile.CX Srl
// SPDX-License-Identifier: MIT
//
// This file is part of a native Swift port of CASL (https://github.com/stalniy/casl)
// by Sergii Stotskyi, used under the MIT License. See the NOTICE file for details.

import Foundation

public struct FieldMatcher {

    public static func match(fields: [String]) -> (String) -> Bool {
        // If no wildcards in any field, use simple contains
        let hasWildcard = fields.contains(where: { $0.contains("*") })
        if !hasWildcard {
            return { field in
                fields.contains(field)
            }
        }

        // Build regex patterns
        let patterns: [NSRegularExpression] = fields.compactMap { fieldPattern in
            let regexStr = fieldPatternToRegex(fieldPattern)
            return try? NSRegularExpression(pattern: "^\(regexStr)$")
        }

        return { field in
            // Also check exact match
            if fields.contains(field) { return true }
            return patterns.contains(where: { regex in
                let range = NSRange(field.startIndex..., in: field)
                return regex.firstMatch(in: field, range: range) != nil
            })
        }
    }

    private static func fieldPatternToRegex(_ pattern: String) -> String {
        // We need to handle:
        // "**" -> matches any depth (any chars including dots)
        // "*" at a segment boundary -> matches single segment (no dots)
        // trailing "*" in a segment like "street*" -> matches remaining chars in that segment

        // Split by dots to get segments
        let segments = pattern.split(separator: ".", omittingEmptySubsequences: false).map(String.init)
        var regexParts: [String] = []

        for segment in segments {
            if segment == "**" {
                // Match any number of segments (including zero)
                regexParts.append("(.+)")
            } else if segment == "*" {
                // Match exactly one segment (no dots)
                regexParts.append("([^.]+)")
            } else if segment.contains("*") {
                // Trailing or embedded star within a segment, e.g., "street*"
                // Escape special regex chars in the non-star parts, replace * with [^.]*
                let escaped = escapeRegexExceptStar(segment)
                let replaced = escaped.replacingOccurrences(of: "*", with: "[^.]*")
                regexParts.append(replaced)
            } else {
                // Literal segment - escape special regex chars
                regexParts.append(NSRegularExpression.escapedPattern(for: segment))
            }
        }

        // Join segments, but "**" should match dots too
        // We need a smarter join: if a part is the "**" pattern, it already includes dots
        // Actually, let's build differently
        var result = ""
        for (i, part) in regexParts.enumerated() {
            if i > 0 {
                // Check if previous or current is the ** pattern
                if regexParts[i - 1] == "(.+)" {
                    // ** already matches dots, add a dot before this segment
                    result += "\\."
                } else if part == "(.+)" {
                    result += "\\."
                } else {
                    result += "\\."
                }
            }
            result += part
        }

        return result
    }

    private static func escapeRegexExceptStar(_ str: String) -> String {
        var result = ""
        for char in str {
            if char == "*" {
                result.append(char)
            } else if "\\^$.|?+()[]{}".contains(char) {
                result.append("\\")
                result.append(char)
            } else {
                result.append(char)
            }
        }
        return result
    }
}
