/*
 * Copyright (c) 2026 [PROJECT OR COMPANY NAME]
 * SPDX-License-Identifier: MIT
 *
 * This file is part of a native Java port of CASL (https://github.com/stalniy/casl)
 * by Sergii Stotskyi, used under the MIT License. See the NOTICE file for details.
 */
package cx.smile.CASL;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class ConditionsMatcher {

    private static final Map<String, BiFunction<Object, Object, Boolean>> customOperators = new HashMap<>();

    /**
     * Registers a custom operator that can be used in conditions matching.
     * This mirrors the JS buildMongoQueryMatcher extensibility, allowing users
     * to add custom operators beyond the built-in set.
     *
     * @param name    the operator name, including the $ prefix (e.g., "$myOp")
     * @param handler a function that takes (fieldValue, operatorValue) and returns true/false
     */
    public static void registerOperator(String name, BiFunction<Object, Object, Boolean> handler) {
        if (name == null || !name.startsWith("$")) {
            throw new IllegalArgumentException("Operator name must start with '$'");
        }
        if (handler == null) {
            throw new IllegalArgumentException("Handler must not be null");
        }
        customOperators.put(name, handler);
    }

    /**
     * Unregisters a previously registered custom operator.
     */
    public static void unregisterOperator(String name) {
        customOperators.remove(name);
    }

    /**
     * Clears all registered custom operators.
     */
    public static void clearCustomOperators() {
        customOperators.clear();
    }

    @SuppressWarnings("unchecked")
    public static Predicate<Map<String, Object>> match(Map<String, Object> conditions) {
        return object -> matchConditions(conditions, object);
    }

    @SuppressWarnings("unchecked")
    private static boolean matchConditions(Map<String, Object> conditions, Map<String, Object> object) {
        for (Map.Entry<String, Object> entry : conditions.entrySet()) {
            String field = entry.getKey();
            Object conditionValue = entry.getValue();

            // Top-level logical operators
            if (field.equals("$and")) {
                List<Map<String, Object>> andList = (List<Map<String, Object>>) conditionValue;
                for (Map<String, Object> subCondition : andList) {
                    if (!matchConditions(subCondition, object)) {
                        return false;
                    }
                }
                continue;
            }
            if (field.equals("$or")) {
                List<Map<String, Object>> orList = (List<Map<String, Object>>) conditionValue;
                boolean anyMatch = false;
                for (Map<String, Object> subCondition : orList) {
                    if (matchConditions(subCondition, object)) {
                        anyMatch = true;
                        break;
                    }
                }
                if (!anyMatch) {
                    return false;
                }
                continue;
            }
            if (field.equals("$not")) {
                Map<String, Object> notCondition = (Map<String, Object>) conditionValue;
                if (matchConditions(notCondition, object)) {
                    return false;
                }
                continue;
            }

            // Skip __caslSubjectType__ in conditions matching
            if (field.equals("__caslSubjectType__")) {
                continue;
            }

            Object fieldValue = getFieldValue(object, field);

            if (!matchField(fieldValue, conditionValue)) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private static boolean matchField(Object fieldValue, Object conditionValue) {
        if (conditionValue instanceof Map) {
            Map<String, Object> condMap = (Map<String, Object>) conditionValue;
            // Check if it contains any operators
            boolean hasOperators = false;
            for (String key : condMap.keySet()) {
                if (key.startsWith("$")) {
                    hasOperators = true;
                    break;
                }
            }
            if (hasOperators) {
                return matchOperators(fieldValue, condMap);
            }
            // plain object equality - treat as implicit $eq
            return Objects.equals(fieldValue, conditionValue);
        }
        // implicit $eq
        return matchEq(fieldValue, conditionValue);
    }

    @SuppressWarnings("unchecked")
    private static boolean matchOperators(Object fieldValue, Map<String, Object> operators) {
        String regexOptions = null;
        if (operators.containsKey("$options")) {
            regexOptions = (String) operators.get("$options");
        }

        for (Map.Entry<String, Object> entry : operators.entrySet()) {
            String op = entry.getKey();
            Object opValue = entry.getValue();

            if (op.equals("$options")) {
                continue;
            }

            boolean result;
            switch (op) {
                case "$eq":
                    result = matchEq(fieldValue, opValue);
                    break;
                case "$ne":
                    result = !matchEq(fieldValue, opValue);
                    break;
                case "$gt":
                    result = compareValues(fieldValue, opValue) > 0;
                    break;
                case "$gte":
                    result = compareValues(fieldValue, opValue) >= 0;
                    break;
                case "$lt":
                    result = compareValues(fieldValue, opValue) < 0;
                    break;
                case "$lte":
                    result = compareValues(fieldValue, opValue) <= 0;
                    break;
                case "$in":
                    result = matchIn(fieldValue, (List<Object>) opValue);
                    break;
                case "$nin":
                    result = !matchIn(fieldValue, (List<Object>) opValue);
                    break;
                case "$all":
                    result = matchAll(fieldValue, (List<Object>) opValue);
                    break;
                case "$size":
                    result = matchSize(fieldValue, opValue);
                    break;
                case "$regex":
                    result = matchRegex(fieldValue, (String) opValue, regexOptions);
                    break;
                case "$exists":
                    result = matchExists(fieldValue, opValue);
                    break;
                case "$elemMatch":
                    result = matchElemMatch(fieldValue, (Map<String, Object>) opValue);
                    break;
                default:
                    BiFunction<Object, Object, Boolean> customHandler = customOperators.get(op);
                    if (customHandler != null) {
                        result = customHandler.apply(fieldValue, opValue);
                    } else {
                        result = false;
                    }
                    break;
            }

            if (!result) {
                return false;
            }
        }
        return true;
    }

    private static boolean matchEq(Object fieldValue, Object conditionValue) {
        if (fieldValue instanceof List) {
            List<?> list = (List<?>) fieldValue;
            for (Object item : list) {
                if (valuesEqual(item, conditionValue)) {
                    return true;
                }
            }
            return false;
        }
        return valuesEqual(fieldValue, conditionValue);
    }

    private static boolean valuesEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        // Handle numeric comparisons across types
        if (a instanceof Number && b instanceof Number) {
            return ((Number) a).doubleValue() == ((Number) b).doubleValue();
        }
        return a.equals(b);
    }

    @SuppressWarnings("unchecked")
    private static int compareValues(Object a, Object b) {
        if (a == null || b == null) {
            if (a == null && b == null) return 0;
            return a == null ? -1 : 1;
        }
        if (a instanceof Number && b instanceof Number) {
            double da = ((Number) a).doubleValue();
            double db = ((Number) b).doubleValue();
            return Double.compare(da, db);
        }
        if (a instanceof Comparable && b instanceof Comparable) {
            return ((Comparable<Object>) a).compareTo(b);
        }
        return 0;
    }

    private static boolean matchIn(Object fieldValue, List<Object> list) {
        if (fieldValue instanceof List) {
            List<?> fieldList = (List<?>) fieldValue;
            for (Object item : fieldList) {
                for (Object listItem : list) {
                    if (valuesEqual(item, listItem)) {
                        return true;
                    }
                }
            }
            return false;
        }
        for (Object item : list) {
            if (valuesEqual(fieldValue, item)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchAll(Object fieldValue, List<Object> list) {
        if (!(fieldValue instanceof List)) {
            return false;
        }
        List<?> fieldList = (List<?>) fieldValue;
        for (Object required : list) {
            boolean found = false;
            for (Object item : fieldList) {
                if (valuesEqual(item, required)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    private static boolean matchSize(Object fieldValue, Object sizeValue) {
        if (!(fieldValue instanceof List)) {
            return false;
        }
        int expectedSize;
        if (sizeValue instanceof Number) {
            expectedSize = ((Number) sizeValue).intValue();
        } else {
            return false;
        }
        return ((List<?>) fieldValue).size() == expectedSize;
    }

    private static boolean matchRegex(Object fieldValue, String regex, String options) {
        if (fieldValue == null) return false;
        String str = fieldValue.toString();
        int flags = 0;
        if (options != null) {
            if (options.contains("i")) {
                flags |= Pattern.CASE_INSENSITIVE;
            }
            if (options.contains("m")) {
                flags |= Pattern.MULTILINE;
            }
            if (options.contains("s")) {
                flags |= Pattern.DOTALL;
            }
        }
        Pattern pattern = Pattern.compile(regex, flags);
        return pattern.matcher(str).find();
    }

    private static boolean matchExists(Object fieldValue, Object existsValue) {
        boolean shouldExist;
        if (existsValue instanceof Boolean) {
            shouldExist = (Boolean) existsValue;
        } else {
            shouldExist = true;
        }
        boolean exists = fieldValue != null;
        return shouldExist == exists;
    }

    @SuppressWarnings("unchecked")
    private static boolean matchElemMatch(Object fieldValue, Map<String, Object> conditions) {
        if (!(fieldValue instanceof List)) {
            return false;
        }
        List<?> list = (List<?>) fieldValue;

        // Check if conditions use operator expressions ($-prefixed keys)
        boolean hasOperatorKeys = false;
        for (String key : conditions.keySet()) {
            if (key.startsWith("$")) {
                hasOperatorKeys = true;
                break;
            }
        }

        for (Object item : list) {
            if (item instanceof Map) {
                // Array of Maps: match sub-conditions against each Map element
                if (matchConditions(conditions, (Map<String, Object>) item)) {
                    return true;
                }
            } else if (hasOperatorKeys) {
                // Array of scalars with operator conditions: evaluate operators against each scalar
                if (matchOperators(item, conditions)) {
                    return true;
                }
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public static Object getFieldValue(Map<String, Object> object, String path) {
        if (object == null) return null;
        if (!path.contains(".")) {
            return object.get(path);
        }

        String[] parts = path.split("\\.");
        Object current = object;

        for (String part : parts) {
            if (current == null) return null;

            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(part);
            } else if (current instanceof List) {
                List<?> list = (List<?>) current;
                // Try numeric index
                try {
                    int index = Integer.parseInt(part);
                    if (index >= 0 && index < list.size()) {
                        current = list.get(index);
                    } else {
                        return null;
                    }
                } catch (NumberFormatException e) {
                    // Field access on list items - check each element
                    List<Object> results = new ArrayList<>();
                    for (Object item : list) {
                        if (item instanceof Map) {
                            Object val = ((Map<String, Object>) item).get(part);
                            if (val != null) {
                                results.add(val);
                            }
                        }
                    }
                    if (results.isEmpty()) {
                        return null;
                    }
                    current = results;
                }
            } else {
                return null;
            }
        }

        return current;
    }
}
