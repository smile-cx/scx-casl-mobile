/*
 * Copyright (c) 2026 [PROJECT OR COMPANY NAME]
 * SPDX-License-Identifier: MIT
 *
 * This file is part of a native Java port of CASL (https://github.com/stalniy/casl)
 * by Sergii Stotskyi, used under the MIT License. See the NOTICE file for details.
 */
package cx.smile.CASL;

import java.util.function.Function;

public class ForbiddenError extends RuntimeException {
    private final Ability ability;
    private String action = "";
    private Object subject;
    private String subjectType = "";
    private String field;
    private String customMessage;

    private static Function<ForbiddenError, String> defaultErrorMessage =
            e -> "Cannot execute \"" + e.getAction() + "\" on \"" + e.getSubjectType() + "\"";

    public static void setDefaultMessage(String message) {
        defaultErrorMessage = e -> message;
    }

    public static void setDefaultMessage(Function<ForbiddenError, String> fn) {
        defaultErrorMessage = fn;
    }

    public static Function<ForbiddenError, String> getDefaultErrorMessage() {
        return defaultErrorMessage;
    }

    private ForbiddenError(Ability ability) {
        super("");
        this.ability = ability;
    }

    public static ForbiddenError from(Ability ability) {
        return new ForbiddenError(ability);
    }

    public ForbiddenError setMessage(String msg) {
        this.customMessage = msg;
        return this;
    }

    public void throwUnlessCan(String action, Object subject) throws ForbiddenError {
        throwUnlessCan(action, subject, null);
    }

    public void throwUnlessCan(String action, Object subject, String field) throws ForbiddenError {
        ForbiddenError error = unlessCan(action, subject, field);
        if (error != null) throw error;
    }

    public ForbiddenError unlessCan(String action, Object subject, String field) {
        Rule rule = ability.relevantRuleFor(action, subject, field);

        if (rule != null && !rule.isInverted()) {
            return null;
        }

        this.action = action;
        this.subject = subject;
        this.subjectType = ability.detectSubjectType(subject);
        this.field = field;

        String reason = rule != null ? rule.getReason() : "";
        if (this.customMessage == null) {
            if (reason != null && !reason.isEmpty()) {
                this.customMessage = reason;
            }
        }

        return this;
    }

    @Override
    public String getMessage() {
        if (customMessage != null) {
            return customMessage;
        }
        return defaultErrorMessage.apply(this);
    }

    public Ability getAbility() { return ability; }
    public String getAction() { return action; }
    public Object getSubject() { return subject; }
    public String getSubjectType() { return subjectType; }
    public String getField() { return field; }
}
