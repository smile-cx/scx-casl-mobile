/*
 * Copyright (c) 2026 Smile.CX Srl
 * SPDX-License-Identifier: MIT
 *
 * This file is part of a native Java port of CASL (https://github.com/stalniy/casl)
 * by Sergii Stotskyi, used under the MIT License. See the NOTICE file for details.
 */
package cx.smile.CASL;

import java.util.List;

/**
 * Represents a compound (logical) node in the AST.
 * Operator is one of "and", "or", "not".
 * Children are either ConditionNode or CompoundNode instances.
 */
public class CompoundNode {
    private final String operator;
    private final List<Object> children;

    public CompoundNode(String operator, List<Object> children) {
        this.operator = operator;
        this.children = children;
    }

    public String getOperator() { return operator; }
    public List<Object> getChildren() { return children; }

    @Override
    public String toString() {
        return "CompoundNode{operator='" + operator + "', children=" + children + "}";
    }
}
