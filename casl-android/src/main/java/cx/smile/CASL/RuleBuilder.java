/*
 * Copyright (c) 2026 [PROJECT OR COMPANY NAME]
 * SPDX-License-Identifier: MIT
 *
 * This file is part of a native Java port of CASL (https://github.com/stalniy/casl)
 * by Sergii Stotskyi, used under the MIT License. See the NOTICE file for details.
 */
package cx.smile.CASL;

public class RuleBuilder {
    private final RawRule rule;

    public RuleBuilder(RawRule rule) {
        this.rule = rule;
    }

    public RuleBuilder because(String reason) {
        rule.setReason(reason);
        return this;
    }
}
