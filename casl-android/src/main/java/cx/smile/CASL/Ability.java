/*
 * Copyright (c) 2026 Smile.CX Srl
 * SPDX-License-Identifier: MIT
 *
 * This file is part of a native Java port of CASL (https://github.com/stalniy/casl)
 * by Sergii Stotskyi, used under the MIT License. See the NOTICE file for details.
 */
package cx.smile.CASL;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class Ability {
    private List<RawRule> rules;
    private final RuleOptions ruleOptions;
    private final String anyAction;
    private final String anySubjectType;
    private Function<Object, String> detectSubjectTypeFn;
    private Map<String, Map<String, ActionEntry>> indexedRules;
    private boolean hasPerFieldRules;
    private Map<String, LinkedItem<Consumer<UpdateEvent>>> events;

    static class ActionEntry {
        List<Rule> rules;
        boolean merged;

        ActionEntry() {
            this.rules = new ArrayList<>();
            this.merged = false;
        }
    }

    static class LinkedItem<T> {
        T value;
        LinkedItem<T> prev;
        LinkedItem<T> next;

        LinkedItem(T value, LinkedItem<T> prev) {
            this.value = value;
            this.prev = prev;
            this.next = null;
            if (prev != null) {
                prev.next = this;
            }
        }

        static <T> LinkedItem<T> clone(LinkedItem<T> item) {
            if (item == null) return null;
            LinkedItem<T> cloned = new LinkedItem<>(item.value, null);
            cloned.prev = item.prev;
            cloned.next = item.next;
            return cloned;
        }

        static <T> void unlink(LinkedItem<T> item) {
            if (item.next != null) {
                item.next.prev = item.prev;
            }
            if (item.prev != null) {
                item.prev.next = item.next;
            }
            item.next = null;
            item.prev = null;
        }
    }

    // ----------------------------------------------------------------
    // Convenience static factory methods
    // ----------------------------------------------------------------

    /**
     * Create an Ability from a list of RawRule with default matchers.
     */
    public static Ability fromRules(List<RawRule> rules) {
        return JsonSupport.createAbility(rules);
    }

    /**
     * Create an Ability from a list of RawRule with custom options.
     */
    public static Ability fromRules(List<RawRule> rules, AbilityOptions options) {
        return JsonSupport.createAbility(rules, options);
    }

    /**
     * Create an Ability from a JSON string containing an array of rules.
     */
    public static Ability fromJson(String json) throws JSONException {
        return JsonSupport.createAbility(json);
    }

    /**
     * Create an Ability from a JSONArray of rules.
     */
    public static Ability fromJson(JSONArray json) throws JSONException {
        return JsonSupport.createAbility(json);
    }

    public Ability() {
        this(new ArrayList<>(), null);
    }

    public Ability(List<RawRule> rules) {
        this(rules, null);
    }

    @SuppressWarnings("unchecked")
    public Ability(List<RawRule> rules, AbilityOptions options) {
        if (options == null) {
            options = AbilityOptions.builder().build();
        }

        RuleOptions.Builder ruleOptsBuilder = RuleOptions.builder();
        if (options.getConditionsMatcher() != null) {
            ruleOptsBuilder.conditionsMatcher(options.getConditionsMatcher());
        }
        if (options.getFieldMatcher() != null) {
            ruleOptsBuilder.fieldMatcher(options.getFieldMatcher());
        }
        if (options.getResolveAction() != null) {
            ruleOptsBuilder.resolveAction(options.getResolveAction());
        } else {
            ruleOptsBuilder.resolveAction(actions -> actions);
        }
        this.ruleOptions = ruleOptsBuilder.build();

        this.anyAction = options.getAnyAction() != null ? options.getAnyAction() : "manage";
        this.anySubjectType = options.getAnySubjectType() != null ? options.getAnySubjectType() : "all";
        this.detectSubjectTypeFn = options.getDetectSubjectType();
        this.rules = rules;
        this.indexedRules = new HashMap<>();
        this.hasPerFieldRules = false;
        this.events = new HashMap<>();
        indexAndAnalyzeRules(rules);
    }

    public List<RawRule> getRules() {
        return rules;
    }

    public Ability update(List<RawRule> rules) {
        UpdateEvent event = new UpdateEvent(this, rules);
        emit("update", event);
        this.rules = rules;
        this.hasPerFieldRules = false;
        indexAndAnalyzeRules(rules);
        emit("updated", event);
        return this;
    }

    public boolean can(String action, Object subject) {
        return can(action, subject, null);
    }

    public boolean can(String action, Object subject, String field) {
        if (action == null) return false;
        Rule rule = relevantRuleFor(action, subject, field);
        return rule != null && !rule.isInverted();
    }

    public boolean cannot(String action, Object subject) {
        return cannot(action, subject, null);
    }

    public boolean cannot(String action, Object subject, String field) {
        return !can(action, subject, field);
    }

    // ----------------------------------------------------------------
    // Frontend-style API: check(subject, action, ...)
    // These mirror the frontend calling convention where subject comes first:
    //   user.can('Application', 'query', null, { id: 'smart-connect' })
    // ----------------------------------------------------------------

    /**
     * Frontend-style permission check (subject first, action second).
     * Equivalent to {@code can(action, subject)}.
     *
     * @param subject the subject type string (e.g. "Application")
     * @param action  the action string (e.g. "query")
     * @return true if the action is allowed on the subject
     */
    public boolean check(String subject, String action) {
        return check(subject, action, null, null);
    }

    /**
     * Frontend-style permission check with field (subject first, action second).
     * Equivalent to {@code can(action, subject, field)}.
     *
     * @param subject the subject type string
     * @param action  the action string
     * @param field   optional field to check (may be null)
     * @return true if the action is allowed on the subject for the given field
     */
    public boolean check(String subject, String action, String field) {
        return check(subject, action, field, null);
    }

    /**
     * Frontend-style permission check with field and conditions (subject first, action second).
     * If conditions are provided, builds a typed subject map with {@code __caslSubjectType__}
     * and delegates to {@code can(action, conditionsMap, field)}.
     * If conditions are null/empty, delegates to {@code can(action, subject, field)}.
     *
     * @param subject    the subject type string (e.g. "Application")
     * @param action     the action string (e.g. "query")
     * @param field      optional field to check (may be null)
     * @param conditions optional conditions dictionary (e.g. {"id": "smart-connect"})
     * @return true if the action is allowed
     */
    public boolean check(String subject, String action, String field, Map<String, Object> conditions) {
        if (conditions == null || conditions.isEmpty()) {
            return can(action, subject, field);
        }
        Map<String, Object> typedSubject = new HashMap<>(conditions);
        typedSubject.put("__caslSubjectType__", subject);
        return can(action, typedSubject, field);
    }

    /**
     * Inverse of {@link #check(String, String)}.
     */
    public boolean checkNot(String subject, String action) {
        return !check(subject, action);
    }

    /**
     * Inverse of {@link #check(String, String, String)}.
     */
    public boolean checkNot(String subject, String action, String field) {
        return !check(subject, action, field);
    }

    /**
     * Inverse of {@link #check(String, String, String, Map)}.
     */
    public boolean checkNot(String subject, String action, String field, Map<String, Object> conditions) {
        return !check(subject, action, field, conditions);
    }

    public Rule relevantRuleFor(String action, Object subject, String field) {
        String subjectType = detectSubjectType(subject);
        List<Rule> rules = rulesFor(action, subjectType, field);

        for (Rule rule : rules) {
            if (rule.matchesConditions(subject)) {
                return rule;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public String detectSubjectType(Object subject) {
        if (subject == null) {
            return anySubjectType;
        }
        if (subject instanceof String) {
            return (String) subject;
        }
        if (subject instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) subject;
            Object typeField = map.get("__caslSubjectType__");
            if (typeField instanceof String) {
                return (String) typeField;
            }
            if (detectSubjectTypeFn != null) {
                return detectSubjectTypeFn.apply(subject);
            }
            return "Map";
        }
        if (detectSubjectTypeFn != null) {
            return detectSubjectTypeFn.apply(subject);
        }
        return subject.getClass().getSimpleName();
    }

    public List<Rule> possibleRulesFor(String action, String subjectType) {
        if (subjectType == null) {
            subjectType = anySubjectType;
        }

        Map<String, ActionEntry> subjectRules = getOrDefaultSubject(subjectType);
        ActionEntry actionEntry = getOrDefaultAction(subjectRules, action);

        if (actionEntry.merged) {
            return actionEntry.rules;
        }

        List<Rule> anyActionRules = null;
        if (!action.equals(anyAction) && subjectRules.containsKey(anyAction)) {
            anyActionRules = subjectRules.get(anyAction).rules;
        }

        List<Rule> rules = mergePrioritized(actionEntry.rules, anyActionRules);

        if (!subjectType.equals(anySubjectType)) {
            rules = mergePrioritized(rules, possibleRulesFor(action, anySubjectType));
        }

        actionEntry.rules = rules;
        actionEntry.merged = true;

        return rules;
    }

    public List<Rule> rulesFor(String action, String subjectType) {
        return rulesFor(action, subjectType, null);
    }

    public List<Rule> rulesFor(String action, String subjectType, String field) {
        List<Rule> rules = possibleRulesFor(action, subjectType);

        if (!hasPerFieldRules) {
            return rules;
        }

        List<Rule> filtered = new ArrayList<>();
        for (Rule rule : rules) {
            if (rule.matchesField(field)) {
                filtered.add(rule);
            }
        }
        return filtered;
    }

    public List<String> actionsFor(String subjectType) {
        if (subjectType == null) {
            throw new IllegalArgumentException(
                    "\"actionsFor\" accepts only subject types (i.e., string) as a parameter");
        }
        Set<String> actions = new LinkedHashSet<>();

        Map<String, ActionEntry> subjectRules = indexedRules.get(subjectType);
        if (subjectRules != null) {
            actions.addAll(subjectRules.keySet());
        }

        if (!subjectType.equals(anySubjectType)) {
            Map<String, ActionEntry> anySubjectRules = indexedRules.get(anySubjectType);
            if (anySubjectRules != null) {
                actions.addAll(anySubjectRules.keySet());
            }
        }

        return new ArrayList<>(actions);
    }

    public Runnable on(String event, Consumer<UpdateEvent> handler) {
        LinkedItem<Consumer<UpdateEvent>> tail = events.containsKey(event) ? events.get(event) : null;
        LinkedItem<Consumer<UpdateEvent>> item = new LinkedItem<>(handler, tail);
        events.put(event, item);

        return () -> {
            LinkedItem<Consumer<UpdateEvent>> currentTail = events.get(event);

            if (item.next == null && item.prev == null && currentTail == item) {
                events.remove(event);
            } else if (item == currentTail) {
                events.put(event, item.prev);
            }

            LinkedItem.unlink(item);
        };
    }

    private void emit(String name, UpdateEvent payload) {
        if (!events.containsKey(name)) return;

        LinkedItem<Consumer<UpdateEvent>> current = events.get(name);
        while (current != null) {
            LinkedItem<Consumer<UpdateEvent>> prev = current.prev != null ? LinkedItem.clone(current.prev) : null;
            current.value.accept(payload);
            current = prev;
        }
    }

    private void indexAndAnalyzeRules(List<RawRule> rawRules) {
        Map<String, Map<String, ActionEntry>> indexed = new HashMap<>();

        for (int i = rawRules.size() - 1; i >= 0; i--) {
            int priority = rawRules.size() - i - 1;
            Rule rule = new Rule(rawRules.get(i), ruleOptions, priority);
            List<String> actions = rule.getAction();
            List<String> subjects = rule.getSubject() != null ? rule.getSubject() : Collections.singletonList(anySubjectType);

            if (!hasPerFieldRules && rule.getFields() != null) {
                hasPerFieldRules = true;
            }

            for (String subject : subjects) {
                Map<String, ActionEntry> subjectRules = indexed.get(subject);
                if (subjectRules == null) {
                    subjectRules = new LinkedHashMap<>();
                    indexed.put(subject, subjectRules);
                }

                for (String action : actions) {
                    ActionEntry actionEntry = subjectRules.get(action);
                    if (actionEntry == null) {
                        actionEntry = new ActionEntry();
                        subjectRules.put(action, actionEntry);
                    }
                    actionEntry.rules.add(rule);
                }
            }
        }

        this.indexedRules = indexed;
    }

    private Map<String, ActionEntry> getOrDefaultSubject(String subjectType) {
        Map<String, ActionEntry> subjectRules = indexedRules.get(subjectType);
        if (subjectRules == null) {
            subjectRules = new LinkedHashMap<>();
            indexedRules.put(subjectType, subjectRules);
        }
        return subjectRules;
    }

    private ActionEntry getOrDefaultAction(Map<String, ActionEntry> subjectRules, String action) {
        ActionEntry entry = subjectRules.get(action);
        if (entry == null) {
            entry = new ActionEntry();
            subjectRules.put(action, entry);
        }
        return entry;
    }

    private static List<Rule> mergePrioritized(List<Rule> array, List<Rule> anotherArray) {
        if (array == null || array.isEmpty()) {
            return anotherArray != null ? anotherArray : new ArrayList<>();
        }
        if (anotherArray == null || anotherArray.isEmpty()) {
            return array;
        }

        int i = 0;
        int j = 0;
        List<Rule> merged = new ArrayList<>();

        while (i < array.size() && j < anotherArray.size()) {
            if (array.get(i).getPriority() < anotherArray.get(j).getPriority()) {
                merged.add(array.get(i));
                i++;
            } else {
                merged.add(anotherArray.get(j));
                j++;
            }
        }

        while (i < array.size()) {
            merged.add(array.get(i));
            i++;
        }
        while (j < anotherArray.size()) {
            merged.add(anotherArray.get(j));
            j++;
        }

        return merged;
    }
}
