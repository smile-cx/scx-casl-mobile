/*
 * Copyright (c) 2026 Smile.CX Srl
 * SPDX-License-Identifier: MIT
 *
 * This file is part of a native Java port of CASL (https://github.com/stalniy/casl)
 * by Sergii Stotskyi, used under the MIT License. See the NOTICE file for details.
 */
package cx.smile.CASL;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class FieldMatcher {

    private static final String REGEXP_SPECIAL_CHARS_PATTERN = "[-/\\\\^$+?.()|\\[\\]{}]";
    private static final Pattern REGEXP_SPECIAL_CHARS = Pattern.compile(REGEXP_SPECIAL_CHARS_PATTERN);
    private static final Pattern REGEXP_ANY = Pattern.compile("\\.?\\*+\\.?");
    private static final Pattern REGEXP_STARS = Pattern.compile("\\*+");
    private static final Pattern REGEXP_DOT = Pattern.compile("\\.");

    public static Predicate<String> match(List<String> fields) {
        boolean hasWildcard = false;
        for (String f : fields) {
            if (f.indexOf('*') != -1) {
                hasWildcard = true;
                break;
            }
        }

        if (!hasWildcard) {
            return field -> fields.contains(field);
        }

        Pattern pattern = createPattern(fields);
        return field -> pattern.matcher(field).matches();
    }

    private static Pattern createPattern(List<String> fields) {
        List<String> patterns = new ArrayList<>();
        for (String field : fields) {
            patterns.add(processField(field));
        }

        String combined;
        if (patterns.size() > 1) {
            StringBuilder sb = new StringBuilder("(?:");
            for (int i = 0; i < patterns.size(); i++) {
                if (i > 0) sb.append("|");
                sb.append(patterns.get(i));
            }
            sb.append(")");
            combined = sb.toString();
        } else {
            combined = patterns.get(0);
        }

        return Pattern.compile("^" + combined + "$");
    }

    private static String processField(String field) {
        // First escape special regex chars (except * and . next to *)
        StringBuilder escaped = new StringBuilder();
        for (int i = 0; i < field.length(); i++) {
            char c = field.charAt(i);
            if (c == '*') {
                escaped.append(c);
            } else if (c == '.' && (i > 0 && field.charAt(i - 1) == '*' || i < field.length() - 1 && field.charAt(i + 1) == '*')) {
                escaped.append(c);
            } else if (isSpecialRegexChar(c)) {
                escaped.append('\\');
                escaped.append(c);
            } else {
                escaped.append(c);
            }
        }

        String result = escaped.toString();

        // Now process wildcard patterns (.?*+.?)
        // We need to find and replace patterns like: optional-dot + stars + optional-dot
        result = replaceWildcardPatterns(result, field);

        return result;
    }

    private static boolean isSpecialRegexChar(char c) {
        return c == '-' || c == '/' || c == '\\' || c == '^' || c == '$'
                || c == '+' || c == '?' || c == '(' || c == ')' || c == '|'
                || c == '[' || c == ']' || c == '{' || c == '}';
    }

    private static String replaceWildcardPatterns(String escaped, String original) {
        // Process the original field to find wildcard segments and build regex
        // Approach: split by wildcard segments (sequences of dots and stars)
        // A wildcard segment is: optional-dot, one or more stars, optional-dot
        java.util.regex.Matcher m = REGEXP_ANY.matcher(original);
        StringBuilder sb = new StringBuilder();
        int lastEnd = 0;

        while (m.find()) {
            // Append the literal part before this match
            String before = original.substring(lastEnd, m.start());
            sb.append(escapeForRegex(before));

            String match = m.group();
            int matchStart = m.start();
            int matchEnd = m.end();

            // Determine quantifier
            boolean startsAtBeginning = matchStart == 0 && match.charAt(0) == '*';
            boolean hasDotBefore = match.charAt(0) == '.';
            boolean hasDotAfter = match.charAt(match.length() - 1) == '.';
            boolean dotOnBothSides = hasDotBefore && hasDotAfter;

            String quantifier;
            if (startsAtBeginning || dotOnBothSides) {
                quantifier = "+";
            } else {
                quantifier = "*";
            }

            boolean isDoublestar = match.contains("**");
            String matcher = isDoublestar ? "." : "[^.]";

            // Build the pattern for stars (replace dots with escaped dots, stars with matcher+quantifier)
            String starPart = match;
            starPart = REGEXP_DOT.matcher(starPart).replaceAll("\\\\.");
            starPart = REGEXP_STARS.matcher(starPart).replaceFirst(matcher + quantifier);

            // If this is at the end of the string, make it optional
            if (matchEnd == original.length()) {
                starPart = "(?:" + starPart + ")?";
            }

            sb.append(starPart);
            lastEnd = matchEnd;
        }

        // Append remaining literal part
        if (lastEnd < original.length()) {
            sb.append(escapeForRegex(original.substring(lastEnd)));
        }

        return sb.toString();
    }

    private static String escapeForRegex(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (isSpecialRegexChar(c)) {
                sb.append('\\');
            }
            sb.append(c);
        }
        return sb.toString();
    }
}
