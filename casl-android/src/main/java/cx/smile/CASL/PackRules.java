/*
 * Copyright (c) 2026 [PROJECT OR COMPANY NAME]
 * SPDX-License-Identifier: MIT
 *
 * This file is part of a native Java port of CASL (https://github.com/stalniy/casl)
 * by Sergii Stotskyi, used under the MIT License. See the NOTICE file for details.
 */
package cx.smile.CASL;

import java.util.*;
import java.util.function.Function;

public class PackRules {

    public static List<List<Object>> packRules(List<RawRule> rules) {
        return packRules(rules, null);
    }

    public static List<List<Object>> packRules(List<RawRule> rules, Function<String, String> packSubject) {
        List<List<Object>> result = new ArrayList<>();

        for (RawRule rule : rules) {
            String actionStr = joinList(rule.getAction());

            String subjectStr;
            if (packSubject != null && rule.getSubject() != null) {
                List<String> mapped = new ArrayList<>();
                for (String s : rule.getSubject()) {
                    mapped.add(packSubject.apply(s));
                }
                subjectStr = String.join(",", mapped);
            } else {
                subjectStr = rule.getSubject() != null ? joinList(rule.getSubject()) : "";
            }

            Object conditions = rule.getConditions() != null ? filterSubjectType(rule.getConditions()) : 0;
            int inverted = rule.isInverted() ? 1 : 0;
            Object fields = rule.getFields() != null ? joinList(rule.getFields()) : 0;
            String reason = rule.getReason() != null ? rule.getReason() : "";

            List<Object> packed = new ArrayList<>();
            packed.add(actionStr);
            packed.add(subjectStr);
            packed.add(conditions);
            packed.add(inverted);
            packed.add(fields);
            packed.add(reason);

            // Trim trailing falsy values
            while (packed.size() > 0) {
                Object last = packed.get(packed.size() - 1);
                if (isFalsy(last)) {
                    packed.remove(packed.size() - 1);
                } else {
                    break;
                }
            }

            result.add(packed);
        }

        return result;
    }

    public static List<RawRule> unpackRules(List<List<Object>> packedRules) {
        return unpackRules(packedRules, null);
    }

    @SuppressWarnings("unchecked")
    public static List<RawRule> unpackRules(List<List<Object>> packedRules, Function<String, String> unpackSubject) {
        List<RawRule> result = new ArrayList<>();

        for (List<Object> packed : packedRules) {
            String actionStr = packed.size() > 0 ? packed.get(0).toString() : "";
            String subjectStr = packed.size() > 1 ? packed.get(1).toString() : "";
            Object conditionsRaw = packed.size() > 2 ? packed.get(2) : null;
            Object invertedRaw = packed.size() > 3 ? packed.get(3) : null;
            Object fieldsRaw = packed.size() > 4 ? packed.get(4) : null;
            Object reasonRaw = packed.size() > 5 ? packed.get(5) : null;

            List<String> actions = Arrays.asList(actionStr.split(","));
            List<String> subjects = Arrays.asList(subjectStr.split(","));

            if (unpackSubject != null) {
                List<String> mappedSubjects = new ArrayList<>();
                for (String s : subjects) {
                    mappedSubjects.add(unpackSubject.apply(s));
                }
                subjects = mappedSubjects;
            }

            boolean inverted = false;
            if (invertedRaw instanceof Number) {
                inverted = ((Number) invertedRaw).intValue() != 0;
            } else if (invertedRaw instanceof Boolean) {
                inverted = (Boolean) invertedRaw;
            }

            RawRule.Builder builder = RawRule.builder()
                    .action(actions)
                    .subject(subjects)
                    .inverted(inverted);

            // Conditions
            if (conditionsRaw != null && conditionsRaw instanceof Map) {
                builder.conditions((Map<String, Object>) conditionsRaw);
            }

            // Fields
            if (fieldsRaw != null && fieldsRaw instanceof String && !((String) fieldsRaw).isEmpty()) {
                builder.fields(Arrays.asList(((String) fieldsRaw).split(",")));
            }

            // Reason
            if (reasonRaw != null && reasonRaw instanceof String && !((String) reasonRaw).isEmpty()) {
                builder.reason((String) reasonRaw);
            }

            result.add(builder.build());
        }

        return result;
    }

    private static String joinList(List<String> list) {
        if (list == null) return "";
        return String.join(",", list);
    }

    private static Map<String, Object> filterSubjectType(Map<String, Object> conditions) {
        if (conditions == null || !conditions.containsKey("__caslSubjectType__")) {
            return conditions;
        }
        Map<String, Object> filtered = new LinkedHashMap<>(conditions);
        filtered.remove("__caslSubjectType__");
        return filtered;
    }

    private static boolean isFalsy(Object value) {
        if (value == null) return true;
        if (value instanceof Number && ((Number) value).intValue() == 0) return true;
        if (value instanceof String && ((String) value).isEmpty()) return true;
        if (value instanceof Boolean && !(Boolean) value) return true;
        return false;
    }
}
