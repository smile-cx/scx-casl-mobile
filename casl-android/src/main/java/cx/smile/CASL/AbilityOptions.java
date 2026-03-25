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

public class AbilityOptions {
    private Function<Map<String, Object>, Predicate<Map<String, Object>>> conditionsMatcher;
    private Function<List<String>, Predicate<String>> fieldMatcher;
    private Function<List<String>, List<String>> resolveAction;
    private Function<Object, String> detectSubjectType;
    private String anyAction;
    private String anySubjectType;

    private AbilityOptions() {}

    public Function<Map<String, Object>, Predicate<Map<String, Object>>> getConditionsMatcher() {
        return conditionsMatcher;
    }

    public Function<List<String>, Predicate<String>> getFieldMatcher() {
        return fieldMatcher;
    }

    public Function<List<String>, List<String>> getResolveAction() {
        return resolveAction;
    }

    public Function<Object, String> getDetectSubjectType() {
        return detectSubjectType;
    }

    public String getAnyAction() {
        return anyAction;
    }

    public String getAnySubjectType() {
        return anySubjectType;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private AbilityOptions options = new AbilityOptions();

        public Builder conditionsMatcher(Function<Map<String, Object>, Predicate<Map<String, Object>>> conditionsMatcher) {
            options.conditionsMatcher = conditionsMatcher;
            return this;
        }

        public Builder fieldMatcher(Function<List<String>, Predicate<String>> fieldMatcher) {
            options.fieldMatcher = fieldMatcher;
            return this;
        }

        public Builder resolveAction(Function<List<String>, List<String>> resolveAction) {
            options.resolveAction = resolveAction;
            return this;
        }

        public Builder detectSubjectType(Function<Object, String> detectSubjectType) {
            options.detectSubjectType = detectSubjectType;
            return this;
        }

        public Builder anyAction(String anyAction) {
            options.anyAction = anyAction;
            return this;
        }

        public Builder anySubjectType(String anySubjectType) {
            options.anySubjectType = anySubjectType;
            return this;
        }

        public AbilityOptions build() {
            return options;
        }
    }
}
