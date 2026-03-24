package cx.smile.CASL;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class RulesToFieldsTest {

    @Test
    public void testEmptyAbilityReturnsEmpty() {
        Ability ability = new Ability(new ArrayList<>(), AbilityOptions.builder()
                .conditionsMatcher(ConditionsMatcher::match)
                .fieldMatcher(FieldMatcher::match)
                .build());

        Map<String, Object> result = RulesToFields.rulesToFields(ability, "read", "Post");
        assertTrue(result.isEmpty());
    }

    @Test
    public void testInvertedRulesReturnEmpty() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.cannot("read", "Post", Collections.singletonMap("id", (Object) 5));
            b.cannot("read", "Post", Collections.singletonMap("private", (Object) true));
        });

        Map<String, Object> result = RulesToFields.rulesToFields(ability, "read", "Post");
        assertTrue(result.isEmpty());
    }

    @Test
    public void testNoConditionsReturnEmpty() {
        Ability ability = AbilityBuilder.defineAbility(b -> b.can("read", "Post"));

        Map<String, Object> result = RulesToFields.rulesToFields(ability, "read", "Post");
        assertTrue(result.isEmpty());
    }

    @Test
    public void testExtractsFieldValues() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("read", "Post", Collections.singletonMap("id", (Object) 5));
            b.can("read", "Post", Collections.singletonMap("private", (Object) true));
        });

        Map<String, Object> result = RulesToFields.rulesToFields(ability, "read", "Post");
        Map<String, Object> expected = new LinkedHashMap<>();
        expected.put("id", 5);
        expected.put("private", true);
        assertEquals(expected, result);
    }

    @Test
    public void testDotNotation() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("read", "Post", Collections.singletonMap("id", (Object) 5));
            b.can("read", "Post", Collections.singletonMap("state.private", (Object) true));
        });

        Map<String, Object> result = RulesToFields.rulesToFields(ability, "read", "Post");

        assertEquals(5, result.get("id"));
        @SuppressWarnings("unchecked")
        Map<String, Object> state = (Map<String, Object>) result.get("state");
        assertNotNull(state);
        assertEquals(true, state.get("private"));
    }

    @Test
    public void testSkipsExpressions() {
        Map<String, Object> cond1 = new LinkedHashMap<>();
        Map<String, Object> inExpr = new LinkedHashMap<>();
        inExpr.put("$in", Arrays.asList("draft", "review"));
        cond1.put("state", inExpr);

        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("read", "Post", cond1);
            b.can("read", "Post", Collections.singletonMap("private", (Object) true));
        });

        Map<String, Object> result = RulesToFields.rulesToFields(ability, "read", "Post");
        assertEquals(Collections.singletonMap("private", (Object) true), result);
    }
}
