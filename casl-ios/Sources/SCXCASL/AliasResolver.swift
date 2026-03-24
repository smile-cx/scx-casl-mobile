// Copyright (c) 2026 [PROJECT OR COMPANY NAME]
// SPDX-License-Identifier: MIT
//
// This file is part of a native Swift port of CASL (https://github.com/stalniy/casl)
// by Sergii Stotskyi, used under the MIT License. See the NOTICE file for details.

import Foundation

public struct AliasResolverOptions {
    public var skipValidate: Bool?
    public var anyAction: String?

    public init(skipValidate: Bool? = nil, anyAction: String? = nil) {
        self.skipValidate = skipValidate
        self.anyAction = anyAction
    }
}

public enum AliasResolverError: Error, LocalizedError {
    case reservedAlias(String)
    case reservedTarget(String)
    case cycle(String)

    public var errorDescription: String? {
        switch self {
        case .reservedAlias(let action):
            return "Cannot create alias for reserved action \"\(action)\""
        case .reservedTarget(let action):
            return "Cannot use reserved action \"\(action)\" as alias target"
        case .cycle(let action):
            return "Circular alias detected for action \"\(action)\""
        }
    }
}

public func createAliasResolver(_ aliasMap: [String: StringOrArray], options: AliasResolverOptions? = nil) throws -> ([String]) -> [String] {
    let anyAction = options?.anyAction ?? "manage"
    let skipValidate = options?.skipValidate ?? false

    // Build expanded map: String -> [String]
    var expandedMap: [String: [String]] = [:]
    for (key, value) in aliasMap {
        expandedMap[key] = value.values
    }

    if !skipValidate {
        // Validate
        for (key, values) in expandedMap {
            if key == anyAction {
                throw AliasResolverError.reservedAlias(anyAction)
            }
            if values.contains(anyAction) {
                throw AliasResolverError.reservedTarget(anyAction)
            }
        }

        // Check for cycles
        for key in expandedMap.keys {
            var visited = Set<String>()
            var queue = [key]
            while !queue.isEmpty {
                let current = queue.removeFirst()
                if let expanded = expandedMap[current] {
                    for action in expanded {
                        if action == key {
                            throw AliasResolverError.cycle(key)
                        }
                        if !visited.contains(action) {
                            visited.insert(action)
                            queue.append(action)
                        }
                    }
                }
            }
        }
    }

    return { actions in
        // Match JS expandActions: start with the input actions, iterate,
        // and when an action is an alias, concat its expansions to the end.
        var result = actions
        var i = 0
        while i < result.count {
            let action = result[i]
            i += 1
            if let expanded = expandedMap[action] {
                result.append(contentsOf: expanded)
            }
        }
        return result
    }
}
