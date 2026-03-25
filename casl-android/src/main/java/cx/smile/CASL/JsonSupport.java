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
import org.json.JSONObject;

import java.util.*;

/**
 * Convenience methods for working with JSON (org.json) in CASL.
 * Provides parsing, conversion, and factory methods so users can work
 * directly with JSON strings and JSONObject/JSONArray from API responses.
 */
public class JsonSupport {

    // ----------------------------------------------------------------
    // Parsing: JSON -> RawRule / List<RawRule>
    // ----------------------------------------------------------------

    /**
     * Parse a JSON string containing an array of rules into a List of RawRule.
     */
    public static List<RawRule> parseRules(String json) throws JSONException {
        JSONArray jsonArray = new JSONArray(json);
        return parseRules(jsonArray);
    }

    /**
     * Parse a JSONArray of rule objects into a List of RawRule.
     */
    public static List<RawRule> parseRules(JSONArray jsonArray) throws JSONException {
        List<RawRule> rules = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            rules.add(parseRule(jsonArray.getJSONObject(i)));
        }
        return rules;
    }

    /**
     * Parse a single JSONObject into a RawRule.
     */
    public static RawRule parseRule(JSONObject jsonObject) throws JSONException {
        RawRule.Builder builder = RawRule.builder();

        // action (required) - string or array
        if (jsonObject.has("action")) {
            Object actionVal = jsonObject.get("action");
            if (actionVal instanceof JSONArray) {
                builder.action(toStringList((JSONArray) actionVal));
            } else {
                builder.action(actionVal.toString());
            }
        }

        // subject - string or array
        if (jsonObject.has("subject")) {
            Object subjectVal = jsonObject.get("subject");
            if (subjectVal instanceof JSONArray) {
                builder.subject(toStringList((JSONArray) subjectVal));
            } else {
                builder.subject(subjectVal.toString());
            }
        }

        // fields - string or array
        if (jsonObject.has("fields")) {
            Object fieldsVal = jsonObject.get("fields");
            if (fieldsVal instanceof JSONArray) {
                builder.fields(toStringList((JSONArray) fieldsVal));
            } else {
                builder.fields(fieldsVal.toString());
            }
        }

        // conditions - object
        if (jsonObject.has("conditions") && !jsonObject.isNull("conditions")) {
            builder.conditions(toMap(jsonObject.getJSONObject("conditions")));
        }

        // inverted - boolean
        if (jsonObject.has("inverted")) {
            builder.inverted(jsonObject.getBoolean("inverted"));
        }

        // reason - string
        if (jsonObject.has("reason") && !jsonObject.isNull("reason")) {
            builder.reason(jsonObject.getString("reason"));
        }

        return builder.build();
    }

    // ----------------------------------------------------------------
    // Ability factory: JSON -> Ability
    // ----------------------------------------------------------------

    /**
     * Create an Ability from a JSON string containing an array of rules.
     */
    public static Ability createAbility(String json) throws JSONException {
        return createAbility(json, null);
    }

    /**
     * Create an Ability from a JSON string containing an array of rules,
     * with custom AbilityOptions.
     */
    public static Ability createAbility(String json, AbilityOptions options) throws JSONException {
        List<RawRule> rules = parseRules(json);
        return buildAbility(rules, options);
    }

    /**
     * Create an Ability from a JSONArray of rules.
     */
    public static Ability createAbility(JSONArray rules) throws JSONException {
        return createAbility(rules, null);
    }

    /**
     * Create an Ability from a JSONArray of rules, with custom AbilityOptions.
     */
    public static Ability createAbility(JSONArray rules, AbilityOptions options) throws JSONException {
        List<RawRule> parsed = parseRules(rules);
        return buildAbility(parsed, options);
    }

    /**
     * Create an Ability from a list of RawRule with default matchers.
     */
    public static Ability createAbility(List<RawRule> rules) {
        return buildAbility(rules, null);
    }

    /**
     * Create an Ability from a list of RawRule with custom options.
     */
    public static Ability createAbility(List<RawRule> rules, AbilityOptions options) {
        return buildAbility(rules, options);
    }

    private static Ability buildAbility(List<RawRule> rules, AbilityOptions options) {
        if (options == null) {
            options = AbilityOptions.builder()
                    .conditionsMatcher(ConditionsMatcher::match)
                    .fieldMatcher(FieldMatcher::match)
                    .build();
        } else {
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

    // ----------------------------------------------------------------
    // Serialization: RawRule / List<RawRule> -> JSON
    // ----------------------------------------------------------------

    /**
     * Convert a RawRule to a JSONObject.
     */
    public static JSONObject ruleToJSON(RawRule rule) throws JSONException {
        JSONObject obj = new JSONObject();

        if (rule.getAction() != null) {
            if (rule.getAction().size() == 1) {
                obj.put("action", rule.getAction().get(0));
            } else {
                obj.put("action", new JSONArray(rule.getAction()));
            }
        }

        if (rule.getSubject() != null) {
            if (rule.getSubject().size() == 1) {
                obj.put("subject", rule.getSubject().get(0));
            } else {
                obj.put("subject", new JSONArray(rule.getSubject()));
            }
        }

        if (rule.getFields() != null) {
            obj.put("fields", new JSONArray(rule.getFields()));
        }

        if (rule.getConditions() != null) {
            obj.put("conditions", toJSONObject(rule.getConditions()));
        }

        if (rule.isInverted()) {
            obj.put("inverted", true);
        }

        if (rule.getReason() != null) {
            obj.put("reason", rule.getReason());
        }

        return obj;
    }

    /**
     * Convert a List of RawRule to a JSONArray.
     */
    public static JSONArray rulesToJSON(List<RawRule> rules) throws JSONException {
        JSONArray arr = new JSONArray();
        for (RawRule rule : rules) {
            arr.put(ruleToJSON(rule));
        }
        return arr;
    }

    /**
     * Export the rules of an Ability to a JSON string.
     */
    public static String exportRules(Ability ability) throws JSONException {
        return rulesToJSON(ability.getRules()).toString();
    }

    // ----------------------------------------------------------------
    // Map/JSONObject conversions
    // ----------------------------------------------------------------

    /**
     * Convert a JSONObject to a Map, recursively handling nested
     * JSONObject, JSONArray, and JSONObject.NULL.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> toMap(JSONObject jsonObject) throws JSONException {
        Map<String, Object> map = new LinkedHashMap<>();
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = jsonObject.get(key);
            map.put(key, fromJSONValue(value));
        }
        return map;
    }

    /**
     * Convert a Map to a JSONObject, recursively handling nested Maps,
     * Lists, and null values.
     */
    @SuppressWarnings("unchecked")
    public static JSONObject toJSONObject(Map<String, Object> map) throws JSONException {
        JSONObject obj = new JSONObject();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            obj.put(entry.getKey(), toJSONValue(entry.getValue()));
        }
        return obj;
    }

    // ----------------------------------------------------------------
    // Subject helpers
    // ----------------------------------------------------------------

    /**
     * Create a typed subject Map from a JSONObject by adding
     * the __caslSubjectType__ field.
     */
    public static Map<String, Object> subject(JSONObject jsonObject, String type) throws JSONException {
        Map<String, Object> map = toMap(jsonObject);
        map.put("__caslSubjectType__", type);
        return map;
    }

    // ----------------------------------------------------------------
    // Ability convenience: can/cannot with JSONObject
    // ----------------------------------------------------------------

    /**
     * Check if the ability allows the given action on a JSONObject subject.
     */
    public static boolean can(Ability ability, String action, JSONObject subject) throws JSONException {
        return ability.can(action, toMap(subject));
    }

    /**
     * Check if the ability allows the given action on a JSONObject subject for a specific field.
     */
    public static boolean can(Ability ability, String action, JSONObject subject, String field) throws JSONException {
        return ability.can(action, toMap(subject), field);
    }

    /**
     * Check if the ability forbids the given action on a JSONObject subject.
     */
    public static boolean cannot(Ability ability, String action, JSONObject subject) throws JSONException {
        return ability.cannot(action, toMap(subject));
    }

    // ----------------------------------------------------------------
    // Ability update from JSON
    // ----------------------------------------------------------------

    /**
     * Update an Ability's rules from a JSON string.
     */
    public static void update(Ability ability, String json) throws JSONException {
        ability.update(parseRules(json));
    }

    /**
     * Update an Ability's rules from a JSONArray.
     */
    public static void update(Ability ability, JSONArray rules) throws JSONException {
        ability.update(parseRules(rules));
    }

    // ----------------------------------------------------------------
    // PackRules JSON support
    // ----------------------------------------------------------------

    /**
     * Pack rules to a JSON string (using PackRules internally).
     */
    public static String packRulesToJSON(List<RawRule> rules) throws JSONException {
        List<List<Object>> packed = PackRules.packRules(rules);
        JSONArray outer = new JSONArray();
        for (List<Object> inner : packed) {
            JSONArray innerArr = new JSONArray();
            for (Object item : inner) {
                innerArr.put(toJSONValue(item));
            }
            outer.put(innerArr);
        }
        return outer.toString();
    }

    /**
     * Unpack rules from a JSON string (using PackRules internally).
     */
    @SuppressWarnings("unchecked")
    public static List<RawRule> unpackRulesFromJSON(String json) throws JSONException {
        JSONArray outer = new JSONArray(json);
        List<List<Object>> packed = new ArrayList<>();
        for (int i = 0; i < outer.length(); i++) {
            JSONArray innerArr = outer.getJSONArray(i);
            List<Object> inner = new ArrayList<>();
            for (int j = 0; j < innerArr.length(); j++) {
                Object val = innerArr.get(j);
                inner.add(fromJSONValue(val));
            }
            packed.add(inner);
        }
        return PackRules.unpackRules(packed);
    }

    // ----------------------------------------------------------------
    // Internal helpers
    // ----------------------------------------------------------------

    private static List<String> toStringList(JSONArray jsonArray) throws JSONException {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            list.add(jsonArray.getString(i));
        }
        return list;
    }

    /**
     * Convert a JSON value (from org.json) to its Java equivalent.
     * Handles JSONObject -> Map, JSONArray -> List, JSONObject.NULL -> null.
     */
    @SuppressWarnings("unchecked")
    private static Object fromJSONValue(Object value) throws JSONException {
        if (value == null || value == JSONObject.NULL) {
            return null;
        }
        if (value instanceof JSONObject) {
            return toMap((JSONObject) value);
        }
        if (value instanceof JSONArray) {
            return toList((JSONArray) value);
        }
        // int, long, double, String, boolean pass through
        return value;
    }

    /**
     * Convert a JSONArray to a List, recursively.
     */
    private static List<Object> toList(JSONArray jsonArray) throws JSONException {
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            list.add(fromJSONValue(jsonArray.get(i)));
        }
        return list;
    }

    /**
     * Convert a Java value to its JSON equivalent for org.json.
     * Handles Map -> JSONObject, List -> JSONArray, null -> JSONObject.NULL.
     */
    @SuppressWarnings("unchecked")
    private static Object toJSONValue(Object value) throws JSONException {
        if (value == null) {
            return JSONObject.NULL;
        }
        if (value instanceof Map) {
            return toJSONObject((Map<String, Object>) value);
        }
        if (value instanceof List) {
            JSONArray arr = new JSONArray();
            for (Object item : (List<?>) value) {
                arr.put(toJSONValue(item));
            }
            return arr;
        }
        // primitives, Strings, Numbers, Booleans pass through
        return value;
    }
}
