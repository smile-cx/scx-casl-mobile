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
import java.util.function.Predicate;

public class RuleOptions {
    private Function<Map<String, Object>, Predicate<Map<String, Object>>> conditionsMatcher;
    private Function<List<String>, Predicate<String>> fieldMatcher;
    private Function<List<String>, List<String>> resolveAction;

    private RuleOptions() {}

    public Function<Map<String, Object>, Predicate<Map<String, Object>>> getConditionsMatcher() {
        return conditionsMatcher;
    }

    public Function<List<String>, Predicate<String>> getFieldMatcher() {
        return fieldMatcher;
    }

    public Function<List<String>, List<String>> getResolveAction() {
        return resolveAction;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private RuleOptions options = new RuleOptions();

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

        public RuleOptions build() {
            return options;
        }
    }
}
