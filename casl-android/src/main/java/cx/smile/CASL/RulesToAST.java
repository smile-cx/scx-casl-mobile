/*
 * Copyright (c) 2026 [PROJECT OR COMPANY NAME]
 * SPDX-License-Identifier: MIT
 *
 * This file is part of a native Java port of CASL (https://github.com/stalniy/casl)
 * by Sergii Stotskyi, used under the MIT License. See the NOTICE file for details.
 */
package cx.smile.CASL;

import java.util.*;

/**
 * Converts ability rules into an AST tree, mirroring the JS
 * {@code rulesToAST} from {@code @casl/ability/extra}.
 * <p>
 * Uses {@link RulesToQuery#rulesToQuery} internally and converts
 * each rule's conditions into an AST of {@link ConditionNode}
 * and {@link CompoundNode} objects.
 */
public class RulesToAST {

    /**
     * Builds an AST from the ability rules for the given action and subject type.
     *
     * @return a CompoundNode/ConditionNode tree, or null if the user is not allowed
     */
    @SuppressWarnings("unchecked")
    public static Object rulesToAST(Ability ability, String action, String subjectType) {
        AbilityQuery<Object> query = RulesToQuery.rulesToQuery(ability, action, subjectType,
                RulesToAST::ruleToAST);

        if (query == null) {
            return null;
        }

        List<Object> andNodes = query.getAnd();
        List<Object> orNodes = query.getOr();

        if (andNodes == null || andNodes.isEmpty()) {
            if (orNodes != null && !orNodes.isEmpty()) {
                return buildOr(orNodes);
            }
            // Empty query means all allowed
            return buildAnd(Collections.emptyList());
        }

        List<Object> finalAnd = new ArrayList<>(andNodes);
        if (orNodes != null && !orNodes.isEmpty()) {
            finalAnd.add(buildOr(orNodes));
        }

        return buildAnd(finalAnd);
    }

    /**
     * Converts a single rule to its AST representation.
     * Inverted rules are wrapped in a "not" CompoundNode.
     */
    static Object ruleToAST(Rule rule) {
        Map<String, Object> conditions = rule.getConditions();
        if (conditions == null) {
            throw new IllegalStateException(
                    "Ability rule does not have conditions. Cannot be used to generate AST");
        }

        Object ast = conditionsToAST(conditions);
        if (rule.isInverted()) {
            return new CompoundNode("not", Collections.singletonList(ast));
        }
        return ast;
    }

    /**
     * Converts a conditions map into an AST node (or compound node).
     */
    @SuppressWarnings("unchecked")
    static Object conditionsToAST(Map<String, Object> conditions) {
        List<Object> nodes = new ArrayList<>();

        for (Map.Entry<String, Object> entry : conditions.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (key.equals("__caslSubjectType__")) {
                continue;
            }

            if (key.equals("$and")) {
                List<Map<String, Object>> list = (List<Map<String, Object>>) value;
                List<Object> children = new ArrayList<>();
                for (Map<String, Object> sub : list) {
                    children.add(conditionsToAST(sub));
                }
                nodes.add(new CompoundNode("and", children));
            } else if (key.equals("$or")) {
                List<Map<String, Object>> list = (List<Map<String, Object>>) value;
                List<Object> children = new ArrayList<>();
                for (Map<String, Object> sub : list) {
                    children.add(conditionsToAST(sub));
                }
                nodes.add(new CompoundNode("or", children));
            } else if (key.equals("$not")) {
                Map<String, Object> sub = (Map<String, Object>) value;
                nodes.add(new CompoundNode("not", Collections.singletonList(conditionsToAST(sub))));
            } else {
                // Field-level condition
                if (value instanceof Map) {
                    Map<String, Object> ops = (Map<String, Object>) value;
                    boolean hasOperators = false;
                    for (String opKey : ops.keySet()) {
                        if (opKey.startsWith("$")) {
                            hasOperators = true;
                            break;
                        }
                    }
                    if (hasOperators) {
                        for (Map.Entry<String, Object> opEntry : ops.entrySet()) {
                            if (opEntry.getKey().equals("$options")) continue;
                            nodes.add(new ConditionNode(key, opEntry.getKey(), opEntry.getValue()));
                        }
                    } else {
                        nodes.add(new ConditionNode(key, "$eq", value));
                    }
                } else {
                    nodes.add(new ConditionNode(key, "$eq", value));
                }
            }
        }

        if (nodes.size() == 1) {
            return nodes.get(0);
        }
        return new CompoundNode("and", nodes);
    }

    private static Object buildAnd(List<Object> children) {
        if (children.size() == 1) {
            return children.get(0);
        }
        return new CompoundNode("and", children);
    }

    private static Object buildOr(List<Object> children) {
        if (children.size() == 1) {
            return children.get(0);
        }
        return new CompoundNode("or", children);
    }
}
