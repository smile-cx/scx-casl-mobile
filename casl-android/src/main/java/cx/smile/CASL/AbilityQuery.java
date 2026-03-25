/*
 * Copyright (c) 2026 Smile.CX Srl
 * SPDX-License-Identifier: MIT
 *
 * This file is part of a native Java port of CASL (https://github.com/stalniy/casl)
 * by Sergii Stotskyi, used under the MIT License. See the NOTICE file for details.
 */
package cx.smile.CASL;

import java.util.List;

public class AbilityQuery<R> {
    private final List<R> or;
    private final List<R> and;

    public AbilityQuery(List<R> or, List<R> and) {
        this.or = or;
        this.and = and;
    }

    public List<R> getOr() {
        return or;
    }

    public List<R> getAnd() {
        return and;
    }

    public boolean isEmpty() {
        return (or == null || or.isEmpty()) && (and == null || and.isEmpty());
    }
}
