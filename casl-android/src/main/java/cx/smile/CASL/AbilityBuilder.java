/*
 * Copyright (c) 2026 Smile.CX Srl
 * SPDX-License-Identifier: MIT
 *
 * This file is part of a native Java port of CASL (https://github.com/stalniy/casl)
 * by Sergii Stotskyi, used under the MIT License. See the NOTICE file for details.
 */
package cx.smile.CASL;

import java.util.*;
import java.util.function.Consumer;

public class AbilityBuilder {
    private final List<RawRule> rules = new ArrayList<>();

    // --- can methods ---

    public RuleBuilder can(String action) {
        return addRule(Collections.singletonList(action), null, null, null, false);
    }

    public RuleBuilder can(String action, String subject) {
        return addRule(Collections.singletonList(action), Collections.singletonList(subject), null, null, false);
    }

    public RuleBuilder can(String action, String subject, Map<String, Object> conditions) {
        return addRule(Collections.singletonList(action), Collections.singletonList(subject), null, conditions, false);
    }

    public RuleBuilder can(String action, String subject, List<String> fields) {
        return addRule(Collections.singletonList(action), Collections.singletonList(subject), fields, null, false);
    }

    public RuleBuilder can(String action, String subject, String field) {
        return addRule(Collections.singletonList(action), Collections.singletonList(subject), Collections.singletonList(field), null, false);
    }

    public RuleBuilder can(String action, String subject, List<String> fields, Map<String, Object> conditions) {
        return addRule(Collections.singletonList(action), Collections.singletonList(subject), fields, conditions, false);
    }

    public RuleBuilder can(String action, String subject, String field, Map<String, Object> conditions) {
        return addRule(Collections.singletonList(action), Collections.singletonList(subject), Collections.singletonList(field), conditions, false);
    }

    public RuleBuilder can(List<String> actions, String subject) {
        return addRule(actions, Collections.singletonList(subject), null, null, false);
    }

    public RuleBuilder can(List<String> actions, List<String> subjects) {
        return addRule(actions, subjects, null, null, false);
    }

    public RuleBuilder can(String action, List<String> subjects) {
        return addRule(Collections.singletonList(action), subjects, null, null, false);
    }

    public RuleBuilder can(List<String> actions, String subject, Map<String, Object> conditions) {
        return addRule(actions, Collections.singletonList(subject), null, conditions, false);
    }

    public RuleBuilder can(List<String> actions, String subject, List<String> fields) {
        return addRule(actions, Collections.singletonList(subject), fields, null, false);
    }

    public RuleBuilder can(List<String> actions, String subject, List<String> fields, Map<String, Object> conditions) {
        return addRule(actions, Collections.singletonList(subject), fields, conditions, false);
    }

    public RuleBuilder can(String action, List<String> subjects, Map<String, Object> conditions) {
        return addRule(Collections.singletonList(action), subjects, null, conditions, false);
    }

    public RuleBuilder can(List<String> actions, List<String> subjects, Map<String, Object> conditions) {
        return addRule(actions, subjects, null, conditions, false);
    }

    // --- cannot methods ---

    public RuleBuilder cannot(String action) {
        return addRule(Collections.singletonList(action), null, null, null, true);
    }

    public RuleBuilder cannot(String action, String subject) {
        return addRule(Collections.singletonList(action), Collections.singletonList(subject), null, null, true);
    }

    public RuleBuilder cannot(String action, String subject, Map<String, Object> conditions) {
        return addRule(Collections.singletonList(action), Collections.singletonList(subject), null, conditions, true);
    }

    public RuleBuilder cannot(String action, String subject, List<String> fields) {
        return addRule(Collections.singletonList(action), Collections.singletonList(subject), fields, null, true);
    }

    public RuleBuilder cannot(String action, String subject, String field) {
        return addRule(Collections.singletonList(action), Collections.singletonList(subject), Collections.singletonList(field), null, true);
    }

    public RuleBuilder cannot(String action, String subject, List<String> fields, Map<String, Object> conditions) {
        return addRule(Collections.singletonList(action), Collections.singletonList(subject), fields, conditions, true);
    }

    public RuleBuilder cannot(String action, String subject, String field, Map<String, Object> conditions) {
        return addRule(Collections.singletonList(action), Collections.singletonList(subject), Collections.singletonList(field), conditions, true);
    }

    public RuleBuilder cannot(List<String> actions, String subject) {
        return addRule(actions, Collections.singletonList(subject), null, null, true);
    }

    public RuleBuilder cannot(List<String> actions, List<String> subjects) {
        return addRule(actions, subjects, null, null, true);
    }

    public RuleBuilder cannot(String action, List<String> subjects) {
        return addRule(Collections.singletonList(action), subjects, null, null, true);
    }

    public RuleBuilder cannot(List<String> actions, String subject, Map<String, Object> conditions) {
        return addRule(actions, Collections.singletonList(subject), null, conditions, true);
    }

    public RuleBuilder cannot(List<String> actions, String subject, List<String> fields) {
        return addRule(actions, Collections.singletonList(subject), fields, null, true);
    }

    public RuleBuilder cannot(String action, List<String> subjects, Map<String, Object> conditions) {
        return addRule(Collections.singletonList(action), subjects, null, conditions, true);
    }

    public RuleBuilder cannot(List<String> actions, List<String> subjects, Map<String, Object> conditions) {
        return addRule(actions, subjects, null, conditions, true);
    }

    // --- common ---

    private RuleBuilder addRule(List<String> actions, List<String> subjects, List<String> fields,
                                Map<String, Object> conditions, boolean inverted) {
        RawRule.Builder builder = RawRule.builder().action(actions);

        if (subjects != null) {
            builder.subject(subjects);
        }
        if (fields != null) {
            builder.fields(fields);
        }
        if (conditions != null) {
            builder.conditions(conditions);
        }
        if (inverted) {
            builder.inverted(true);
        }

        RawRule rule = builder.build();
        rules.add(rule);
        return new RuleBuilder(rule);
    }

    public List<RawRule> getRules() {
        return rules;
    }

    public Ability build() {
        return build(null);
    }

    public Ability build(AbilityOptions options) {
        if (options == null) {
            options = AbilityOptions.builder()
                    .conditionsMatcher(ConditionsMatcher::match)
                    .fieldMatcher(FieldMatcher::match)
                    .build();
        } else {
            // Ensure conditionsMatcher and fieldMatcher are set
            AbilityOptions.Builder b = AbilityOptions.builder();
            b.conditionsMatcher(options.getConditionsMatcher() != null ? options.getConditionsMatcher() : ConditionsMatcher::match);
            b.fieldMatcher(options.getFieldMatcher() != null ? options.getFieldMatcher() : FieldMatcher::match);
            if (options.getResolveAction() != null) b.resolveAction(options.getResolveAction());
            if (options.getDetectSubjectType() != null) b.detectSubjectType(options.getDetectSubjectType());
            if (options.getAnyAction() != null) b.anyAction(options.getAnyAction());
            if (options.getAnySubjectType() != null) b.anySubjectType(options.getAnySubjectType());
            options = b.build();
        }

        return new Ability(rules, options);
    }

    public static Ability defineAbility(Consumer<AbilityBuilder> define) {
        return defineAbility(define, null);
    }

    public static Ability defineAbility(Consumer<AbilityBuilder> define, AbilityOptions options) {
        AbilityBuilder builder = new AbilityBuilder();
        define.accept(builder);
        return builder.build(options);
    }
}
