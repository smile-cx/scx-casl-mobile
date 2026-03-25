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
import java.util.function.Predicate;

public class Rule {
    private final List<String> action;
    private final List<String> subject;
    private final boolean inverted;
    private final Map<String, Object> conditions;
    private final List<String> fields;
    private final String reason;
    private final RawRule origin;
    private final int priority;
    private final RuleOptions options;
    private Predicate<Map<String, Object>> matchConditions;
    private Predicate<String> matchField;

    @SuppressWarnings("unchecked")
    public Rule(RawRule rawRule, RuleOptions options, int priority) {
        if (rawRule.getFields() != null && rawRule.getFields().isEmpty()) {
            throw new IllegalArgumentException("`rawRule.fields` cannot be an empty array. https://bit.ly/390miLa");
        }
        if (rawRule.getFields() != null && options.getFieldMatcher() == null) {
            throw new IllegalArgumentException("You need to pass \"fieldMatcher\" option in order to restrict access by fields");
        }
        if (rawRule.getConditions() != null && options.getConditionsMatcher() == null) {
            throw new IllegalArgumentException("You need to pass \"conditionsMatcher\" option in order to restrict access by conditions");
        }

        Function<List<String>, List<String>> resolveAction = options.getResolveAction();
        if (resolveAction != null) {
            this.action = resolveAction.apply(rawRule.getAction());
        } else {
            this.action = rawRule.getAction();
        }
        this.subject = rawRule.getSubject();
        this.inverted = rawRule.isInverted();
        this.conditions = rawRule.getConditions();
        this.reason = rawRule.getReason();
        this.origin = rawRule;
        this.fields = rawRule.getFields();
        this.priority = priority;
        this.options = options;
    }

    @SuppressWarnings("unchecked")
    public boolean matchesConditions(Object object) {
        if (this.conditions == null) {
            return true;
        }

        if (object == null || object instanceof String) {
            return !this.inverted;
        }

        if (object instanceof Map) {
            if (this.matchConditions == null && this.options.getConditionsMatcher() != null) {
                this.matchConditions = this.options.getConditionsMatcher().apply(this.conditions);
            }
            if (this.matchConditions != null) {
                return this.matchConditions.test((Map<String, Object>) object);
            }
            return false;
        }

        return !this.inverted;
    }

    public boolean matchesField(String field) {
        if (this.fields == null) {
            return true;
        }

        if (field == null) {
            return !this.inverted;
        }

        if (this.matchField == null && this.options.getFieldMatcher() != null) {
            this.matchField = this.options.getFieldMatcher().apply(this.fields);
        }

        if (this.matchField != null) {
            return this.matchField.test(field);
        }

        return false;
    }

    public List<String> getAction() { return action; }
    public List<String> getSubject() { return subject; }
    public boolean isInverted() { return inverted; }
    public Map<String, Object> getConditions() { return conditions; }
    public List<String> getFields() { return fields; }
    public String getReason() { return reason; }
    public RawRule getOrigin() { return origin; }
    public int getPriority() { return priority; }
    public RuleOptions getOptions() { return options; }
}
