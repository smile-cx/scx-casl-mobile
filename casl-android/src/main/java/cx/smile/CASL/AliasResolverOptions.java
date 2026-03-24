/*
 * Copyright (c) 2026 [PROJECT OR COMPANY NAME]
 * SPDX-License-Identifier: MIT
 *
 * This file is part of a native Java port of CASL (https://github.com/stalniy/casl)
 * by Sergii Stotskyi, used under the MIT License. See the NOTICE file for details.
 */
package cx.smile.CASL;

public class AliasResolverOptions {
    private Boolean skipValidate;
    private String anyAction;

    private AliasResolverOptions() {}

    public Boolean getSkipValidate() {
        return skipValidate;
    }

    public String getAnyAction() {
        return anyAction;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private AliasResolverOptions options = new AliasResolverOptions();

        public Builder skipValidate(Boolean skipValidate) {
            options.skipValidate = skipValidate;
            return this;
        }

        public Builder anyAction(String anyAction) {
            options.anyAction = anyAction;
            return this;
        }

        public AliasResolverOptions build() {
            return options;
        }
    }
}
