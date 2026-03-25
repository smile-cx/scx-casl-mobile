/*
 * Copyright (c) 2026 Smile.CX Srl
 * SPDX-License-Identifier: MIT
 *
 * This file is part of a native Java port of CASL (https://github.com/stalniy/casl)
 * by Sergii Stotskyi, used under the MIT License. See the NOTICE file for details.
 */
package cx.smile.CASL;

import java.util.*;
import java.util.function.Function;

public class CaslUtils {

    @SuppressWarnings("unchecked")
    public static Function<List<String>, List<String>> createAliasResolver(Map<String, Object> aliasMap) {
        return createAliasResolver(aliasMap, null);
    }

    @SuppressWarnings("unchecked")
    public static Function<List<String>, List<String>> createAliasResolver(Map<String, Object> aliasMap, AliasResolverOptions options) {
        String reservedAction = "manage";
        if (options != null && options.getAnyAction() != null) {
            reservedAction = options.getAnyAction();
        }

        // JS: if (!options || options.skipValidate !== false) → validate
        // Validation is skipped ONLY when skipValidate is explicitly false
        boolean shouldValidate = (options == null || !Boolean.FALSE.equals(options.getSkipValidate()));

        if (shouldValidate) {
            validateForCycles(aliasMap, reservedAction);
        }

        final Map<String, Object> finalAliasMap = aliasMap;
        return (actions) -> expandActions(finalAliasMap, actions, CaslUtils::defaultAliasMerge);
    }

    @SuppressWarnings("unchecked")
    private static void validateForCycles(Map<String, Object> aliasMap, String reservedAction) {
        if (aliasMap.containsKey(reservedAction)) {
            throw new IllegalArgumentException("Cannot use \"" + reservedAction + "\" as an alias because it's reserved action.");
        }

        final String reserved = reservedAction;
        AliasMerge mergeAndDetect = (actions, action) -> {
            String duplicate = findDuplicate(actions, action);
            if (duplicate != null) {
                throw new IllegalArgumentException("Detected cycle " + duplicate + " -> " + String.join(", ", actions));
            }

            boolean isUsingReserved = false;
            if (action instanceof String && action.equals(reserved)) {
                isUsingReserved = true;
            }
            if (actions.contains(reserved)) {
                isUsingReserved = true;
            }
            if (action instanceof List && ((List<String>) action).contains(reserved)) {
                isUsingReserved = true;
            }
            if (isUsingReserved) {
                throw new IllegalArgumentException("Cannot make an alias to \"" + reserved + "\" because this is reserved action");
            }

            return concatAction(actions, action);
        };

        for (String key : aliasMap.keySet()) {
            expandActions(aliasMap, Collections.singletonList(key), mergeAndDetect);
        }
    }

    @SuppressWarnings("unchecked")
    private static String findDuplicate(List<String> actions, Object actionToFind) {
        if (actionToFind instanceof String) {
            if (actions.contains(actionToFind)) {
                return (String) actionToFind;
            }
        } else if (actionToFind instanceof List) {
            for (String a : (List<String>) actionToFind) {
                if (actions.contains(a)) {
                    return a;
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static List<String> expandActions(Map<String, Object> aliasMap, List<String> rawActions, AliasMerge merge) {
        List<String> actions = new ArrayList<>(rawActions);
        int i = 0;
        while (i < actions.size()) {
            String action = actions.get(i);
            i++;
            if (aliasMap.containsKey(action)) {
                Object aliasValue = aliasMap.get(action);
                actions = merge.merge(actions, aliasValue);
            }
        }
        return actions;
    }

    @SuppressWarnings("unchecked")
    private static List<String> defaultAliasMerge(List<String> actions, Object action) {
        return concatAction(actions, action);
    }

    @SuppressWarnings("unchecked")
    private static List<String> concatAction(List<String> actions, Object action) {
        List<String> result = new ArrayList<>(actions);
        if (action instanceof String) {
            result.add((String) action);
        } else if (action instanceof List) {
            result.addAll((List<String>) action);
        }
        return result;
    }

    @FunctionalInterface
    private interface AliasMerge {
        List<String> merge(List<String> actions, Object action);
    }

    // Subject helpers

    public static Map<String, Object> subject(String type, Map<String, Object> object) {
        if (object == null) return null;
        Object existing = object.get("__caslSubjectType__");
        if (existing != null) {
            if (!type.equals(existing)) {
                throw new IllegalStateException(
                        "Trying to cast object to subject type " + type + " but previously it was casted to " + existing);
            }
            return object;
        }
        // Mutate the original map directly (matches JS Object.defineProperty behavior)
        object.put("__caslSubjectType__", type);
        return object;
    }

    public static String detectSubjectType(Map<String, Object> object) {
        if (object == null) return null;
        Object type = object.get("__caslSubjectType__");
        if (type instanceof String) {
            return (String) type;
        }
        return null;
    }
}
