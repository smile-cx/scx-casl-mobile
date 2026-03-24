package cx.smile.CASL;

import org.junit.Test;

import java.util.*;
import java.util.function.Function;

import static org.junit.Assert.*;

public class RulesToQueryTest {

    private Function<Rule, Map<String, Object>> convert = rule -> {
        if (rule.isInverted()) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("$not", rule.getConditions());
            return result;
        }
        return rule.getConditions();
    };

    private AbilityQuery<Map<String, Object>> toQuery(Ability ability, String action, String subject) {
        return RulesToQuery.rulesToQuery(ability, action, subject, convert);
    }

    @Test
    public void testReturnsEmptyObjectIfNoConditions() {
        Ability ability = AbilityBuilder.defineAbility(b -> b.can("read", "Post"));

        AbilityQuery<Map<String, Object>> query = toQuery(ability, "read", "Post");
        assertNotNull(query);
        assertTrue(query.isEmpty());
    }

    @Test
    public void testReturnsEmptyOrIfAtLeastOneRegularRuleNoConditions() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("read", "Post", Collections.singletonMap("author", (Object) 123));
            b.can("read", "Post");
        });

        AbilityQuery<Map<String, Object>> query = toQuery(ability, "read", "Post");
        assertNotNull(query);
        assertTrue(query.isEmpty());
    }

    @Test
    public void testReturnsEmptyOrIfRuleWithConditionsDefinedLast() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("read", "Post");
            b.can("read", "Post", Collections.singletonMap("author", (Object) 123));
        });

        AbilityQuery<Map<String, Object>> query = toQuery(ability, "read", "Post");
        assertNotNull(query);
        assertTrue(query.isEmpty());
    }

    @Test
    public void testReturnsNullForEmptyAbility() {
        Ability ability = new Ability(new ArrayList<>(), AbilityOptions.builder()
                .conditionsMatcher(ConditionsMatcher::match)
                .fieldMatcher(FieldMatcher::match)
                .build());

        AbilityQuery<Map<String, Object>> query = toQuery(ability, "read", "Post");
        assertNull(query);
    }

    @Test
    public void testReturnsNullForOnlyInvertedRules() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.cannot("read", "Post", Collections.singletonMap("private", (Object) true));
        });

        AbilityQuery<Map<String, Object>> query = toQuery(ability, "read", "Post");
        assertNull(query);
    }

    @Test
    public void testReturnsNullIfInvertedWithoutConditions() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.cannot("read", "Post", Collections.singletonMap("author", (Object) 123));
            b.cannot("read", "Post");
        });

        AbilityQuery<Map<String, Object>> query = toQuery(ability, "read", "Post");
        assertNull(query);
    }

    @Test
    public void testReturnsNullIfInvertedWithoutConditionsEvenIfDirectRuleExists() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("read", "Post", Collections.singletonMap("public", (Object) true));
            b.cannot("read", "Post", Collections.singletonMap("author", (Object) 321));
            b.cannot("read", "Post");
        });

        AbilityQuery<Map<String, Object>> query = toQuery(ability, "read", "Post");
        assertNull(query);
    }

    @Test
    public void testReturnsQueryIfRegularRuleAfterLastInvertedWithoutConditions() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("read", "Post", Collections.singletonMap("public", (Object) true));
            b.cannot("read", "Post", Collections.singletonMap("author", (Object) 321));
            b.cannot("read", "Post");
            b.can("read", "Post", Collections.singletonMap("author", (Object) 123));
        });

        AbilityQuery<Map<String, Object>> query = toQuery(ability, "read", "Post");
        assertNotNull(query);
        assertNotNull(query.getOr());
        assertEquals(1, query.getOr().size());
        assertEquals(Collections.singletonMap("author", (Object) 123), query.getOr().get(0));
        assertNull(query.getAnd());
    }

    @Test
    public void testReturnsEmptyQueryIfInvertedWithConditionsBeforeRegularWithout() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("read", "Post", Collections.singletonMap("author", (Object) 123));
            b.cannot("read", "Post", Collections.singletonMap("private", (Object) true));
            b.can("read", "Post");
        });

        AbilityQuery<Map<String, Object>> query = toQuery(ability, "read", "Post");
        assertNotNull(query);
        assertTrue(query.isEmpty());
    }

    @Test
    public void testORsConditionsForRegularRules() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            Map<String, Object> c1 = new LinkedHashMap<>();
            c1.put("status", "draft");
            c1.put("createdBy", "someoneelse");
            b.can("read", "Post", c1);

            Map<String, Object> c2 = new LinkedHashMap<>();
            c2.put("status", "published");
            c2.put("createdBy", "me");
            b.can("read", "Post", c2);
        });

        AbilityQuery<Map<String, Object>> query = toQuery(ability, "read", "Post");
        assertNotNull(query);
        assertNotNull(query.getOr());
        assertEquals(2, query.getOr().size());
        assertNull(query.getAnd());
    }

    @Test
    public void testANDsConditionsForInvertedRules() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("read", "Post");
            Map<String, Object> c1 = new LinkedHashMap<>();
            c1.put("status", "draft");
            c1.put("createdBy", "someoneelse");
            b.cannot("read", "Post", c1);

            Map<String, Object> c2 = new LinkedHashMap<>();
            c2.put("status", "published");
            c2.put("createdBy", "me");
            b.cannot("read", "Post", c2);
        });

        AbilityQuery<Map<String, Object>> query = toQuery(ability, "read", "Post");
        assertNotNull(query);
        assertNotNull(query.getAnd());
        assertEquals(2, query.getAnd().size());
    }

    @Test
    public void testMixedRegularAndInvertedRules() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("read", "Post", Collections.singletonMap("_id", (Object) "mega"));
            b.can("read", "Post", Collections.singletonMap("state", (Object) "draft"));
            b.cannot("read", "Post", Collections.singletonMap("private", (Object) true));
            b.cannot("read", "Post", Collections.singletonMap("state", (Object) "archived"));
        });

        AbilityQuery<Map<String, Object>> query = toQuery(ability, "read", "Post");
        assertNotNull(query);
        assertNotNull(query.getOr());
        assertNotNull(query.getAnd());
        assertEquals(2, query.getOr().size());
        assertEquals(2, query.getAnd().size());
    }

    @Test
    public void testIgnoresInvertedRulesWithFields() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("read", "Post", Collections.singletonMap("author", (Object) 123));
            b.cannot("read", "Post", "description", Collections.singletonMap("private", (Object) true));
        });

        AbilityQuery<Map<String, Object>> query = toQuery(ability, "read", "Post");
        assertNotNull(query);
        assertNotNull(query.getOr());
        assertEquals(1, query.getOr().size());
        assertEquals(Collections.singletonMap("author", (Object) 123), query.getOr().get(0));
    }

    @Test
    public void testIgnoresInvertedRulesWithFieldsWithoutConditions() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("read", "Post", Collections.singletonMap("author", (Object) 123));
            b.cannot("read", "Post", "description");
        });

        AbilityQuery<Map<String, Object>> query = toQuery(ability, "read", "Post");
        assertNotNull(query);
        assertNotNull(query.getOr());
        assertEquals(1, query.getOr().size());
    }
}
