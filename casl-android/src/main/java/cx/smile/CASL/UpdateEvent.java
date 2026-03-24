/*
 * Copyright (c) 2026 [PROJECT OR COMPANY NAME]
 * SPDX-License-Identifier: MIT
 *
 * This file is part of a native Java port of CASL (https://github.com/stalniy/casl)
 * by Sergii Stotskyi, used under the MIT License. See the NOTICE file for details.
 */
package cx.smile.CASL;

import java.util.List;

public class UpdateEvent {
    private final Ability target;
    private final List<RawRule> rules;

    public UpdateEvent(Ability target, List<RawRule> rules) {
        this.target = target;
        this.rules = rules;
    }

    public Ability getTarget() {
        return target;
    }

    public Ability getAbility() {
        return target;
    }

    public List<RawRule> getRules() {
        return rules;
    }
}
