/*
 * Copyright (c) 2026 [PROJECT OR COMPANY NAME]
 * SPDX-License-Identifier: MIT
 *
 * This file is part of a native Java port of CASL (https://github.com/stalniy/casl)
 * by Sergii Stotskyi, used under the MIT License. See the NOTICE file for details.
 */
package cx.smile.CASL;

/**
 * Represents a single field-level condition in the AST.
 * For example: { field: "age", operator: "$gt", value: 18 }
 */
public class ConditionNode {
    private final String field;
    private final String operator;
    private final Object value;

    public ConditionNode(String field, String operator, Object value) {
        this.field = field;
        this.operator = operator;
        this.value = value;
    }

    public String getField() { return field; }
    public String getOperator() { return operator; }
    public Object getValue() { return value; }

    @Override
    public String toString() {
        return "ConditionNode{field='" + field + "', operator='" + operator + "', value=" + value + "}";
    }
}
