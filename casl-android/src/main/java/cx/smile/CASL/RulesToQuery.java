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

public class RulesToQuery {

    public static <R> AbilityQuery<R> rulesToQuery(Ability ability, String action, String subjectType,
                                                    Function<Rule, R> convert) {
        List<R> and = new ArrayList<>();
        List<R> or = new ArrayList<>();
        List<Rule> rules = ability.rulesFor(action, subjectType);

        for (Rule rule : rules) {
            if (rule.getConditions() == null) {
                if (rule.isInverted()) {
                    // Stop if inverted rule without conditions
                    break;
                } else {
                    // Regular rule without conditions - allows all
                    return and.isEmpty() ? new AbilityQuery<>(null, null) : new AbilityQuery<>(null, and);
                }
            } else {
                if (rule.isInverted()) {
                    and.add(convert.apply(rule));
                } else {
                    or.add(convert.apply(rule));
                }
            }
        }

        // If no regular conditions, user is not allowed
        if (or.isEmpty()) {
            return null;
        }

        if (and.isEmpty()) {
            return new AbilityQuery<>(or, null);
        }
        return new AbilityQuery<>(or, and);
    }
}
