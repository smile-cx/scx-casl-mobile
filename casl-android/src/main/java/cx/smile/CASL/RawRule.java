/*
 * Copyright (c) 2026 [PROJECT OR COMPANY NAME]
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

public class RawRule {
    private List<String> action;
    private List<String> subject;
    private List<String> fields;
    private Map<String, Object> conditions;
    private boolean inverted;
    private String reason;

    private RawRule() {}

    public List<String> getAction() { return action; }
    public List<String> getSubject() { return subject; }
    public List<String> getFields() { return fields; }
    public Map<String, Object> getConditions() { return conditions; }
    public boolean isInverted() { return inverted; }
    public String getReason() { return reason; }

    public void setReason(String reason) { this.reason = reason; }

    public static Builder builder() { return new Builder(); }

    // ----------------------------------------------------------------
    // Convenience static factory methods (delegate to JsonSupport)
    // ----------------------------------------------------------------

    /**
     * Parse a JSON string containing an array of rules into a List of RawRule.
     */
    public static List<RawRule> listFromJson(String json) throws JSONException {
        return JsonSupport.parseRules(json);
    }

    /**
     * Parse a JSONArray of rule objects into a List of RawRule.
     */
    public static List<RawRule> listFromJson(JSONArray json) throws JSONException {
        return JsonSupport.parseRules(json);
    }

    /**
     * Parse a single rule from a JSON string.
     */
    public static RawRule fromJson(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        return JsonSupport.parseRule(jsonObject);
    }

    /**
     * Parse a single rule from a JSONObject.
     */
    public static RawRule fromJson(JSONObject json) throws JSONException {
        return JsonSupport.parseRule(json);
    }

    public static class Builder {
        private RawRule rule = new RawRule();

        public Builder action(String action) { rule.action = Collections.singletonList(action); return this; }
        public Builder action(String... actions) { rule.action = Arrays.asList(actions); return this; }
        public Builder action(List<String> actions) { rule.action = new ArrayList<>(actions); return this; }
        public Builder subject(String subject) { rule.subject = Collections.singletonList(subject); return this; }
        public Builder subject(String... subjects) { rule.subject = Arrays.asList(subjects); return this; }
        public Builder subject(List<String> subjects) { rule.subject = new ArrayList<>(subjects); return this; }
        public Builder fields(String field) { rule.fields = Collections.singletonList(field); return this; }
        public Builder fields(String... fields) { rule.fields = Arrays.asList(fields); return this; }
        public Builder fields(List<String> fields) { rule.fields = new ArrayList<>(fields); return this; }
        public Builder conditions(Map<String, Object> conditions) { rule.conditions = conditions; return this; }
        public Builder inverted(boolean inverted) { rule.inverted = inverted; return this; }
        public Builder reason(String reason) { rule.reason = reason; return this; }
        public RawRule build() {
            if (rule.action == null) throw new IllegalStateException("action is required");
            return rule;
        }
    }
}
