/*
 * Copyright (c) 2026 Smile.CX Srl
 * SPDX-License-Identifier: MIT
 *
 * This file is part of a native Java port of CASL (https://github.com/stalniy/casl)
 * by Sergii Stotskyi, used under the MIT License. See the NOTICE file for details.
 */
package cx.smile.CASL;

import java.util.*;

public class RulesToFields {

    @SuppressWarnings("unchecked")
    public static Map<String, Object> rulesToFields(Ability ability, String action, String subjectType) {
        List<Rule> rules = ability.rulesFor(action, subjectType);
        Map<String, Object> values = new LinkedHashMap<>();

        for (Rule rule : rules) {
            if (rule.isInverted() || rule.getConditions() == null) {
                continue;
            }

            for (Map.Entry<String, Object> entry : rule.getConditions().entrySet()) {
                String fieldName = entry.getKey();
                Object value = entry.getValue();

                // Skip plain object values (query expressions like {$in: [...]})
                if (value instanceof Map) {
                    continue;
                }

                setByPath(values, fieldName, value);
            }
        }

        return values;
    }

    @SuppressWarnings("unchecked")
    private static void setByPath(Map<String, Object> object, String path, Object value) {
        if (!path.contains(".")) {
            object.put(path, value);
            return;
        }

        String[] keys = path.split("\\.");
        Map<String, Object> ref = object;

        for (int i = 0; i < keys.length - 1; i++) {
            String key = keys[i];
            Object existing = ref.get(key);
            if (!(existing instanceof Map)) {
                existing = new LinkedHashMap<String, Object>();
                ref.put(key, existing);
            }
            ref = (Map<String, Object>) existing;
        }

        ref.put(keys[keys.length - 1], value);
    }
}
