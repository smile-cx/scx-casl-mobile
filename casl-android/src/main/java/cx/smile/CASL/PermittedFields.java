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

public class PermittedFields {

    public static List<String> permittedFieldsOf(Ability ability, String action, Object subject,
                                                  Function<Rule, List<String>> fieldsFrom) {
        String subjectType = ability.detectSubjectType(subject);
        List<Rule> rules = ability.possibleRulesFor(action, subjectType);

        Set<String> uniqueFields = new LinkedHashSet<>();
        // Process rules in reverse order (lowest priority first)
        for (int i = rules.size() - 1; i >= 0; i--) {
            Rule rule = rules.get(i);
            if (rule.matchesConditions(subject)) {
                List<String> ruleFields = fieldsFrom.apply(rule);
                if (ruleFields != null) {
                    if (rule.isInverted()) {
                        for (String f : ruleFields) {
                            uniqueFields.remove(f);
                        }
                    } else {
                        uniqueFields.addAll(ruleFields);
                    }
                }
            }
        }

        return new ArrayList<>(uniqueFields);
    }

    /**
     * Helper class to make custom accessibleFieldsBy helper function.
     * Wraps permittedFieldsOf with a getAllFields extractor.
     */
    public static class AccessibleFields {
        private final Ability ability;
        private final String action;
        private final Function<String, List<String>> getAllFields;

        public AccessibleFields(Ability ability, String action, Function<String, List<String>> getAllFields) {
            this.ability = ability;
            this.action = action;
            this.getAllFields = getAllFields;
        }

        /**
         * Returns accessible fields for a subject type (string).
         */
        public List<String> ofType(String subjectType) {
            return permittedFieldsOf(ability, action, subjectType, getRuleFields(subjectType));
        }

        /**
         * Returns accessible fields for a particular document (Map subject).
         */
        public List<String> of(Object subject) {
            String subjectType = ability.detectSubjectType(subject);
            return permittedFieldsOf(ability, action, subject, getRuleFields(subjectType));
        }

        private Function<Rule, List<String>> getRuleFields(String type) {
            return rule -> rule.getFields() != null ? rule.getFields() : getAllFields.apply(type);
        }
    }
}
