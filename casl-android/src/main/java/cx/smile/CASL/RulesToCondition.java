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

/**
 * Converts CASL's priority-ordered rules into flat boolean logic for database queries.
 *
 * Each {@code can} branch is bounded by all higher-priority {@code cannot} conditions (AND NOT).
 * An unconditional {@code cannot} stops evaluation; an unconditional {@code can} produces an
 * "allow all" branch bounded only by preceding {@code cannot} conditions.
 *
 * Returns {@code null} when not allowed at all, or {@code hooks.empty()} when allowed without conditions.
 *
 * Mirrors the JS {@code rulesToCondition} from {@code @casl/ability/extra}.
 */
public class RulesToCondition {

    public interface Hooks<R> {
        R and(List<R> conditions);
        R or(List<R> conditions);
        R empty();
    }

    public static <R> R rulesToCondition(
            List<Rule> rules,
            Function<Rule, R> convert,
            Hooks<R> hooks
    ) {
        List<R> higherCannots = new ArrayList<>();
        List<R> orConditions = new ArrayList<>();
        boolean hasUnconditionalCan = false;

        for (Rule rule : rules) {
            if (rule.isInverted()) {
                if (rule.getConditions() == null) {
                    break; // unconditional cannot — stop
                }
                higherCannots.add(convert.apply(rule));
            } else {
                if (rule.getConditions() == null) {
                    hasUnconditionalCan = true;
                    break; // unconditional can — bounded by preceding cannots
                }
                R converted = convert.apply(rule);
                if (higherCannots.isEmpty()) {
                    orConditions.add(converted);
                } else {
                    List<R> andParts = new ArrayList<>();
                    andParts.add(converted);
                    andParts.addAll(higherCannots);
                    orConditions.add(hooks.and(andParts));
                }
            }
        }

        if (hasUnconditionalCan) {
            if (higherCannots.isEmpty()) return hooks.empty();
            if (orConditions.isEmpty()) return hooks.and(higherCannots);
            orConditions.add(hooks.and(higherCannots));
        }

        if (orConditions.isEmpty()) return null;
        return hooks.or(orConditions);
    }
}
