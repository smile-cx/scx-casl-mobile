package cx.smile.CASL;

import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static org.junit.Assert.*;

public class AbilityTest {

    // Helper to create a Map (like a Post object) with __caslSubjectType__ = "Post"
    private static Map<String, Object> post(Object... keyValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("__caslSubjectType__", "Post");
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put((String) keyValues[i], keyValues[i + 1]);
        }
        return map;
    }

    // ---- Action Aliases ----

    @Test
    public void testAllowsToAddAliasForActions() {
        Map<String, Object> aliasMap = new HashMap<>();
        aliasMap.put("modify", Arrays.asList("update", "delete"));
        Function<List<String>, List<String>> resolveAction = CaslUtils.createAliasResolver(aliasMap);

        AbilityOptions options = AbilityOptions.builder().resolveAction(resolveAction).build();
        Ability ability = AbilityBuilder.defineAbility(b -> b.can("modify", "Post"), options);

        assertTrue(ability.can("modify", "Post"));
        assertTrue(ability.can("update", "Post"));
        assertTrue(ability.can("delete", "Post"));
    }

    @Test
    public void testAllowsDeeplyNestedAliasedActions() {
        Map<String, Object> aliasMap = new LinkedHashMap<>();
        aliasMap.put("sort", "increment");
        aliasMap.put("modify", "sort");
        Function<List<String>, List<String>> resolveAction = CaslUtils.createAliasResolver(aliasMap);

        AbilityOptions options = AbilityOptions.builder().resolveAction(resolveAction).build();
        Ability ability = AbilityBuilder.defineAbility(b -> b.can("modify", "all"), options);

        assertTrue(ability.can("increment", "Post"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCycleDetectionThrows() {
        Map<String, Object> aliasMap = new HashMap<>();
        aliasMap.put("a", Arrays.asList("b"));
        aliasMap.put("b", Arrays.asList("a"));
        CaslUtils.createAliasResolver(aliasMap);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThrowsExceptionWhenTryingToDefineManageAlias() {
        Map<String, Object> aliasMap = new HashMap<>();
        aliasMap.put("manage", "crud");
        CaslUtils.createAliasResolver(aliasMap);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThrowsExceptionWhenTryingToMakeManagePartOfAlias() {
        Map<String, Object> aliasMap = new HashMap<>();
        aliasMap.put("modify", Arrays.asList("crud", "manage"));
        CaslUtils.createAliasResolver(aliasMap);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThrowsExceptionWhenAliasingActionToItself() {
        Map<String, Object> aliasMap = new HashMap<>();
        aliasMap.put("sort", "sort");
        CaslUtils.createAliasResolver(aliasMap);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThrowsExceptionWhenAliasingActionToItselfInArray() {
        Map<String, Object> aliasMap = new HashMap<>();
        aliasMap.put("sort", Arrays.asList("sort", "order"));
        CaslUtils.createAliasResolver(aliasMap);
    }

    @Test
    public void testSkipValidateFalseSkipsValidation() {
        // In JS, skipValidate: false means "yes, skip validation"
        // This cyclic alias map would throw if validation ran
        Map<String, Object> aliasMap = new HashMap<>();
        aliasMap.put("sort", "sort"); // cyclic - would fail validation
        AliasResolverOptions options = AliasResolverOptions.builder()
                .skipValidate(false)
                .build();
        // Should NOT throw because validation is skipped
        CaslUtils.createAliasResolver(aliasMap, options);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSkipValidateTrueStillValidates() {
        // In JS, skipValidate: true means validation still runs (confusing JS behavior)
        Map<String, Object> aliasMap = new HashMap<>();
        aliasMap.put("sort", "sort"); // cyclic
        AliasResolverOptions options = AliasResolverOptions.builder()
                .skipValidate(true)
                .build();
        // Should throw because validation runs
        CaslUtils.createAliasResolver(aliasMap, options);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSkipValidateNullStillValidates() {
        // In JS, skipValidate: undefined means validation runs (default)
        Map<String, Object> aliasMap = new HashMap<>();
        aliasMap.put("sort", "sort"); // cyclic
        AliasResolverOptions options = AliasResolverOptions.builder()
                .build();
        // Should throw because validation runs
        CaslUtils.createAliasResolver(aliasMap, options);
    }

    // ---- can/cannot methods ----

    @Test
    public void testProvidesCanAndCannotMethods() {
        Ability ability = AbilityBuilder.defineAbility(b -> b.can("read", "Post"));

        assertTrue(ability.can("read", "Post"));
        assertFalse(ability.cannot("read", "Post"));
    }

    // ---- Rule listing ----

    @Test
    public void testListsAllRules() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("crud", "all");
            b.can("learn", "Range");
            b.cannot("read", "String");
            b.cannot("read", "Hash");
            b.cannot("preview", "Array");
        });

        List<RawRule> rules = ability.getRules();
        assertEquals(5, rules.size());
        assertEquals(Collections.singletonList("crud"), rules.get(0).getAction());
        assertEquals(Collections.singletonList("all"), rules.get(0).getSubject());
        assertFalse(rules.get(0).isInverted());

        assertTrue(rules.get(2).isInverted());
        assertEquals(Collections.singletonList("read"), rules.get(2).getAction());
        assertEquals(Collections.singletonList("String"), rules.get(2).getSubject());
    }

    // ---- Rule updates ----

    @Test
    public void testAllowsToUpdateRules() {
        Ability ability = AbilityBuilder.defineAbility(b -> b.can("read", Arrays.asList("Post", "User")));

        assertTrue(ability.can("read", "Post"));

        ability.update(new ArrayList<>());

        assertTrue(ability.getRules().isEmpty());
        assertFalse(ability.can("read", "Post"));
        assertFalse(ability.can("read", "User"));
    }

    // ---- Claim-based rules ----

    @Test
    public void testAllowsToCheckAbilitiesOnlyByAction() {
        List<RawRule> rules = new ArrayList<>();
        rules.add(RawRule.builder().action("read").build());
        Ability ability = new Ability(rules, AbilityOptions.builder()
                .conditionsMatcher(ConditionsMatcher::match)
                .fieldMatcher(FieldMatcher::match)
                .build());

        assertTrue(ability.can("read", (Object) null));
    }

    // ---- Custom anyAction/anySubjectType ----

    @Test
    public void testCustomAnyActionAndAnySubjectType() {
        List<RawRule> rules = new ArrayList<>();
        rules.add(RawRule.builder().action("*").subject("*").build());
        Ability ability = new Ability(rules, AbilityOptions.builder()
                .anyAction("*")
                .anySubjectType("*")
                .conditionsMatcher(ConditionsMatcher::match)
                .fieldMatcher(FieldMatcher::match)
                .build());

        assertTrue(ability.can("read", "Post"));
        assertTrue(ability.can("update", "Post"));
        assertTrue(ability.can("doAnythingWith", "Post"));
        assertTrue(ability.can("*", "*"));
        assertTrue(ability.can("*", "Post"));
        assertTrue(ability.can("read", "*"));
    }

    // ---- Default behavior ----

    @Test
    public void testAllowsToPerformSpecifiedActionsOnTargetInstance() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("test", "all");
            b.can(Arrays.asList("read", "update"), "Post");
            b.can("delete", "Post", mapOf("creator", "admin"));
            b.cannot("publish", "Post");
        });

        assertTrue(ability.can("read", post()));
        assertTrue(ability.can("update", post()));
    }

    @Test
    public void testAllowsToPerformSpecifiedActionsOnTargetTypeString() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can(Arrays.asList("read", "update"), "Post");
        });

        assertTrue(ability.can("read", "Post"));
        assertTrue(ability.can("update", "Post"));
    }

    @Test
    public void testDisallowsToPerformUnspecifiedActionOnTarget() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can(Arrays.asList("read", "update"), "Post");
        });

        assertFalse(ability.can("archive", "Post"));
        assertFalse(ability.can("archive", post()));
    }

    @Test
    public void testDisallowsToPerformActionIfActionParameterIsFalsy() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("read", "Post");
        });

        assertFalse(ability.can(null, "Post"));
    }

    @Test
    public void testChecksByAllSubjectIfSubjectParameterIsNull() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("test", "all");
        });

        assertTrue(ability.can("test", (Object) null));
    }

    @Test
    public void testDisallowsToPerformActionOnUnspecifiedTargetType() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("read", "Post");
        });

        assertFalse(ability.can("read", "User"));
    }

    @Test
    public void testAllowsActionIfTargetTypeMatchesRuleWithOrWithoutConditions() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("delete", "Post", mapOf("creator", "admin"));
        });

        assertTrue(ability.can("delete", "Post"));
    }

    @Test
    public void testAllowsActionIfTargetInstanceMatchesConditions() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("delete", "Post", mapOf("creator", "admin"));
        });

        assertTrue(ability.can("delete", post("creator", "admin")));
    }

    @Test
    public void testDisallowsActionIfTargetInstanceDoesNotMatchConditions() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("delete", "Post", mapOf("creator", "admin"));
        });

        assertFalse(ability.can("delete", post("creator", "user")));
    }

    @Test
    public void testDisallowsActionForInvertedRuleWhenChecksBySubjectType() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("read", "Post");
            b.cannot("publish", "Post");
        });

        assertFalse(ability.can("publish", "Post"));
    }

    // ---- Conditions ----

    @Test
    public void testEqualityConditions() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("read", "Post", mapOf("creator", "me"));
        });

        assertTrue(ability.can("read", post("creator", "me")));
        assertFalse(ability.can("read", post("creator", "someoneelse")));
    }

    @Test
    public void testNeCondition() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("read", "Post", mapOf("creator", mapOf("$ne", "me")));
        });

        assertFalse(ability.can("read", post("creator", "me")));
        assertTrue(ability.can("read", post("creator", "someoneelse")));
    }

    @Test
    public void testInCondition() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("read", "Post", mapOf("state", mapOf("$in", Arrays.asList("shared", "draft"))));
        });

        assertTrue(ability.can("read", post("state", "draft")));
        assertTrue(ability.can("read", post("state", "shared")));
        assertTrue(ability.can("read", post("state", Arrays.asList("shared", "public"))));
    }

    @Test
    public void testAllCondition() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("read", "Post", mapOf("state", mapOf("$all", Arrays.asList("shared", "draft"))));
        });

        assertFalse(ability.can("read", post("state", "draft")));
        assertFalse(ability.can("read", post("state", "shared")));
        assertTrue(ability.can("read", post("state", Arrays.asList("shared", "draft"))));
    }

    @Test
    public void testGtAndGteCondition() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("update", "Post", mapOf("views", mapOf("$gt", 10)));
            b.cannot("update", "Post", mapOf("views", mapOf("$gte", 1000)));
        });

        assertFalse(ability.can("update", post("views", 9)));
        assertTrue(ability.can("update", post("views", 100)));
        assertFalse(ability.can("update", post("views", 1001)));
    }

    @Test
    public void testLtAndLteCondition() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("update", "Post", mapOf("views", mapOf("$lt", 5)));
            b.cannot("update", "Post", mapOf("views", mapOf("$lte", 2)));
        });

        assertFalse(ability.can("update", post("views", 2)));
        assertTrue(ability.can("update", post("views", 3)));
    }

    @Test
    public void testExistsCondition() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("read", "Post", mapOf("views", mapOf("$exists", true)));
        });

        assertFalse(ability.can("read", post()));
        assertTrue(ability.can("read", post("views", 3)));
    }

    @Test
    public void testDotNotationConditions() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("delete", "Post", mapOf("authors.0", mapOf("$exists", false)));
            b.can("update", "Post", mapOf("comments.author", "Ted"));
        });

        assertFalse(ability.can("delete", post("authors", Arrays.asList("me", "someoneelse"))));
        assertTrue(ability.can("delete", post("authors", new ArrayList<>())));
        assertTrue(ability.can("update", post("comments", Arrays.asList(
                mapOf("author", "Ted"), mapOf("author", "John")))));
        assertFalse(ability.can("update", post("comments", Arrays.asList(
                mapOf("author", "John")))));
    }

    @Test
    public void testRegexCondition() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("delete", "Post", mapOf("title", mapOf("$regex", "\\[DELETED\\]")));
        });

        assertFalse(ability.can("delete", post("title", "public")));
        assertFalse(ability.can("delete", post("title", "[deleted] title")));
        assertTrue(ability.can("delete", post("title", "[DELETED] title")));
    }

    @Test
    public void testElemMatchCondition() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("delete", "Post", mapOf("authors", mapOf("$elemMatch", mapOf("id", "me-id"))));
        });

        assertFalse(ability.can("delete", post("authors", Arrays.asList(mapOf("id", "someone-else-id")))));
        assertTrue(ability.can("delete", post("authors", Arrays.asList(mapOf("id", "me-id")))));
        assertTrue(ability.can("delete", post("authors", Arrays.asList(
                mapOf("id", "someone-else-id"), mapOf("id", "me-id")))));
    }

    @Test
    public void testReturnsTrueForInvertedRuleAndSubjectString() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("read", "Post");
            b.cannot("read", "Post", mapOf("private", true));
        });

        assertTrue(ability.can("read", "Post"));
    }

    // ---- Per-field abilities ----

    @Test
    public void testPerFieldRulesSingleField() {
        Ability ability = AbilityBuilder.defineAbility(b -> b.can("read", "Post", "title"));

        assertTrue(ability.can("read", "Post"));
        assertTrue(ability.can("read", "Post", "title"));
        assertFalse(ability.can("read", "Post", "description"));
    }

    @Test
    public void testPerFieldRulesMultipleFields() {
        Ability ability = AbilityBuilder.defineAbility(b ->
                b.can("read", "Post", Arrays.asList("title", "id")));

        assertTrue(ability.can("read", "Post"));
        assertTrue(ability.can("read", "Post", "title"));
        assertTrue(ability.can("read", "Post", "id"));
        assertFalse(ability.can("read", "Post", "description"));
    }

    @Test
    public void testPerFieldInvertedRules() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("read", "Post");
            b.cannot("read", "Post", "description");
        });

        assertTrue(ability.can("read", "Post"));
        assertTrue(ability.can("read", "Post", "title"));
        assertFalse(ability.can("read", "Post", "description"));
    }

    @Test
    public void testNoFieldMeansAllFields() {
        Ability ability = AbilityBuilder.defineAbility(b -> b.can("read", "Post"));

        assertTrue(ability.can("read", "Post", "title"));
        assertTrue(ability.can("read", "Post", "description"));
    }

    // ---- Field patterns ----

    @Test
    public void testFieldPatternSingleStar() {
        Ability ability = AbilityBuilder.defineAbility(b -> b.can("read", "Post", "author.*"));

        assertTrue(ability.can("read", "Post", "author"));
        assertTrue(ability.can("read", "Post", "author.*"));
        assertTrue(ability.can("read", "Post", "author.name"));
        assertTrue(ability.can("read", "Post", "author.age"));
        assertFalse(ability.can("read", "Post", "author.publication.name"));
    }

    @Test
    public void testFieldPatternDoubleStar() {
        Ability ability = AbilityBuilder.defineAbility(b -> b.can("read", "Post", "author.**"));

        assertTrue(ability.can("read", "Post", "author"));
        assertTrue(ability.can("read", "Post", "author.**"));
        assertTrue(ability.can("read", "Post", "author.name"));
        assertTrue(ability.can("read", "Post", "author.age"));
        assertTrue(ability.can("read", "Post", "author.publication.name"));
    }

    @Test
    public void testFieldPatternStarInMiddle() {
        Ability ability = AbilityBuilder.defineAbility(b -> b.can("read", "Post", "author.*.name"));

        assertFalse(ability.can("read", "Post", "author"));
        assertTrue(ability.can("read", "Post", "author.*.name"));
        assertTrue(ability.can("read", "Post", "author.publication.name"));
        assertFalse(ability.can("read", "Post", "author.publication.startDate"));
        assertFalse(ability.can("read", "Post", "author.publication.country.name"));
    }

    @Test
    public void testFieldPatternDoubleStarInMiddle() {
        Ability ability = AbilityBuilder.defineAbility(b -> b.can("read", "Post", "author.**.name"));

        assertFalse(ability.can("read", "Post", "author"));
        assertTrue(ability.can("read", "Post", "author.**.name"));
        assertTrue(ability.can("read", "Post", "author.publication.name"));
        assertFalse(ability.can("read", "Post", "author.publication.startDate"));
        assertTrue(ability.can("read", "Post", "author.publication.country.name"));
    }

    @Test
    public void testFieldPatternStarAtStart() {
        Ability ability = AbilityBuilder.defineAbility(b -> b.can("read", "Post", "*.name"));

        assertTrue(ability.can("read", "Post", "author.name"));
        assertTrue(ability.can("read", "Post", "*.name"));
        assertFalse(ability.can("read", "Post", "author.publication.name"));
    }

    @Test
    public void testFieldPatternDoubleStarAtStart() {
        Ability ability = AbilityBuilder.defineAbility(b -> b.can("read", "Post", "**.name"));

        assertTrue(ability.can("read", "Post", "author.name"));
        assertTrue(ability.can("read", "Post", "**.name"));
        assertTrue(ability.can("read", "Post", "author.publication.name"));
    }

    @Test
    public void testFieldPatternTrailingStar() {
        Ability ability = AbilityBuilder.defineAbility(b -> b.can("read", "Post", "author.address.street*"));

        assertTrue(ability.can("read", "Post", "author.address.street"));
        assertTrue(ability.can("read", "Post", "author.address.street1"));
        assertTrue(ability.can("read", "Post", "author.address.street2"));
        assertFalse(ability.can("read", "Post", "author.address"));
    }

    @Test
    public void testFieldPatternSpecialRegexpChars() {
        Ability ability = AbilityBuilder.defineAbility(b -> b.can("read", "Post", "author?.address+.street*"));

        assertTrue(ability.can("read", "Post", "author?.address+.street"));
        assertTrue(ability.can("read", "Post", "author?.address+.street1"));
        assertTrue(ability.can("read", "Post", "author?.address+.street2"));
        assertFalse(ability.can("read", "Post", "author?.address+"));
    }

    @Test
    public void testFieldPatternMiddleStarComplex() {
        Ability ability = AbilityBuilder.defineAbility(b -> b.can("read", "Post", "vehicle.*.generic.*"));

        assertTrue(ability.can("read", "Post", "vehicle.profile.generic.item"));
        assertTrue(ability.can("read", "Post", "vehicle.*.generic.signal"));
        assertTrue(ability.can("read", "Post", "vehicle.profile.generic.*"));
        assertFalse(ability.can("read", "Post", "vehicle.*.user.*"));
    }

    // ---- Fields + conditions ----

    @Test
    public void testFieldsWithConditionsOnString() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("read", "Post", Arrays.asList("title", "description"),
                    mapOf("author", "me", "published", true));
        });

        assertTrue(ability.can("read", "Post"));
    }

    @Test
    public void testFieldsWithConditionsOnFields() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("read", "Post", Arrays.asList("title", "description"),
                    mapOf("author", "me", "published", true));
        });

        assertTrue(ability.can("read", "Post", "title"));
        assertTrue(ability.can("read", "Post", "description"));
    }

    @Test
    public void testFieldsWithConditionsDoesNotAllowMismatch() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("read", "Post", Arrays.asList("title", "description"),
                    mapOf("author", "me", "published", true));
        });

        assertFalse(ability.can("read", post()));
    }

    @Test
    public void testFieldsWithConditionsAllowsMatchingInstance() {
        Map<String, Object> myPost = post("author", "me", "published", true);
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("read", "Post", Arrays.asList("title", "description"),
                    mapOf("author", "me", "published", true));
        });

        assertTrue(ability.can("read", myPost));
    }

    @Test
    public void testFieldsWithConditionsOnInstanceField() {
        Map<String, Object> myPost = post("author", "me", "published", true);
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("read", "Post", Arrays.asList("title", "description"),
                    mapOf("author", "me", "published", true));
        });

        assertTrue(ability.can("read", myPost, "title"));
        assertTrue(ability.can("read", myPost, "description"));
    }

    @Test
    public void testFieldsWithConditionsDoesNotAllowWrongField() {
        Map<String, Object> myPost = post("author", "me", "published", true);
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("read", "Post", Arrays.asList("title", "description"),
                    mapOf("author", "me", "published", true));
        });

        assertFalse(ability.can("read", myPost, "id"));
    }

    @Test
    public void testEnsuresBothConditionsAreMet() {
        Map<String, Object> myPost = post("author", "me", "published", true);
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("read", "Post", Arrays.asList("title", "description"),
                    mapOf("author", "me", "published", true));
        });

        assertTrue(ability.can("read", myPost));
        assertFalse(ability.can("read", post("author", "me", "active", false)));
    }

    @Test
    public void testActionsForReturnsAllSubjectActionsWhenNoSpecificActions() {
        Ability ability = AbilityBuilder.defineAbility(b -> b.can("read", "all"));

        List<String> actions = ability.actionsFor("Post");
        assertEquals(1, actions.size());
        assertEquals("read", actions.get(0));
    }

    // ---- manage action ----

    @Test
    public void testManageIsAliasForAnyAction() {
        Ability ability = AbilityBuilder.defineAbility(b -> b.can("manage", "all"));

        assertTrue(ability.can("read", "post"));
        assertTrue(ability.can("do_whatever_anywhere", "post"));
    }

    @Test
    public void testManageHonoursCannotRules() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("manage", "all");
            b.cannot("read", "post");
        });

        assertFalse(ability.can("read", "post"));
        assertTrue(ability.can("update", "post"));
    }

    @Test
    public void testCannotManageAll() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("read", "post");
            b.cannot("manage", "all");
        });

        assertFalse(ability.can("read", "post"));
        assertFalse(ability.can("delete", "post"));
    }

    @Test
    public void testManageFieldSpecific() {
        Ability ability = AbilityBuilder.defineAbility(b -> b.can("manage", "all", "subject"));

        assertTrue(ability.can("read", "post", "subject"));
    }

    // ---- rulesFor ----

    @Test
    public void testRulesForReturnsCorrectRules() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("read", "Post");
            b.can("update", "Post");
            b.cannot("read", "Post", mapOf("private", true));
        });

        List<Rule> rules = ability.rulesFor("read", "Post");
        assertEquals(2, rules.size());
        assertTrue(rules.get(0).isInverted());
        assertNotNull(rules.get(0).getConditions());
        assertFalse(rules.get(1).isInverted());
    }

    @Test
    public void testRulesForFiltersInvertedFields() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("read", "Post");
            b.cannot("read", "Post", "title");
        });

        List<Rule> rules = ability.rulesFor("read", "Post");
        // Without field specified, inverted rules with fields should be filtered out (matchesField(null) returns !inverted=false)
        assertEquals(1, rules.size());
        assertFalse(rules.get(0).isInverted());
    }

    @Test
    public void testRulesForFieldSpecific() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("read", "Post");
            b.cannot("read", "Post", "title");
        });

        List<Rule> rules = ability.rulesFor("read", "Post", "title");
        assertEquals(2, rules.size());
        assertTrue(rules.get(0).isInverted());
        assertNotNull(rules.get(0).getFields());
        assertFalse(rules.get(1).isInverted());
    }

    // ---- actionsFor ----

    @Test
    public void testActionsForSpecificSubject() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("read", "Post");
            b.can("update", "Post");
            b.cannot("read", "Post", mapOf("private", true));
        });

        List<String> actions = ability.actionsFor("Post");
        assertTrue(actions.contains("read"));
        assertTrue(actions.contains("update"));
    }

    @Test
    public void testActionsForWithAliases() {
        Map<String, Object> aliasMap = new HashMap<>();
        aliasMap.put("modify", Arrays.asList("read", "update"));
        Function<List<String>, List<String>> resolveAction = CaslUtils.createAliasResolver(aliasMap);
        AbilityOptions options = AbilityOptions.builder().resolveAction(resolveAction).build();

        Ability ability = AbilityBuilder.defineAbility(b -> b.can("modify", "Post"), options);

        List<String> actions = ability.actionsFor("Post");
        assertTrue(actions.contains("modify"));
        assertTrue(actions.contains("read"));
        assertTrue(actions.contains("update"));
    }

    @Test
    public void testActionsForWithAll() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("read", "all");
            b.can("update", "Post");
        });

        List<String> actions = ability.actionsFor("Post");
        assertTrue(actions.contains("update"));
        assertTrue(actions.contains("read"));
    }

    @Test
    public void testActionsForEmpty() {
        Ability ability = new Ability(new ArrayList<>(), AbilityOptions.builder()
                .conditionsMatcher(ConditionsMatcher::match)
                .fieldMatcher(FieldMatcher::match)
                .build());

        List<String> actions = ability.actionsFor("Post");
        assertEquals(0, actions.size());
    }

    // ---- Precedence ----

    @Test
    public void testChecksEveryRuleUsingLogicalOR() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("delete", "Post", mapOf("creator", "me"));
            b.can("delete", "Post", mapOf("sharedWith", mapOf("$in", Collections.singletonList("me"))));
        });

        assertTrue(ability.can("delete", post("creator", "me")));
        assertTrue(ability.can("delete", post("sharedWith", "me")));
    }

    @Test
    public void testChecksRulesInInverseOrder() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("delete", "Post", mapOf("creator", "me"));
            b.cannot("delete", "Post", mapOf("archived", true));
        });

        assertFalse(ability.can("delete", post("creator", "me", "archived", true)));
        assertTrue(ability.can("delete", post("creator", "me")));
    }

    @Test
    public void testShadowsRuleWithConditionsByRuleWithout() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("delete", "Post");
            b.can("delete", "Post", mapOf("creator", "me"));
        });

        assertTrue(ability.can("delete", post("creator", "someoneelse")));
        assertTrue(ability.can("delete", post("creator", "me")));
    }

    @Test
    public void testDoesNotShadowRuleWithConditionsIfCannotBetween() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("manage", "Post");
            b.cannot("delete", "Post");
            b.can("delete", "Post", mapOf("creator", "me"));
        });

        assertFalse(ability.can("delete", post("creator", "someoneelse")));
        assertTrue(ability.can("delete", post("creator", "me")));
    }

    @Test
    public void testShadowsInvertedRuleByRegularOne() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.cannot("delete", "Post", mapOf("creator", "me"));
            b.can("delete", "Post", mapOf("creator", "me"));
        });

        assertTrue(ability.can("delete", post("creator", "me")));
    }

    @Test
    public void testShadowsAllSubjectRuleBySpecificOne() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("delete", "all");
            b.cannot("delete", "Post");
        });

        assertFalse(ability.can("delete", "Post"));
        assertTrue(ability.can("delete", "User"));
    }

    // ---- Events ----

    @Test
    public void testUpdateEventFires() {
        Ability ability = AbilityBuilder.defineAbility(b -> b.can("read", "Post"));

        AtomicBoolean called = new AtomicBoolean(false);
        ability.on("update", event -> called.set(true));
        ability.update(new ArrayList<>());

        assertTrue(called.get());
    }

    @Test
    public void testUpdatedEventFires() {
        Ability ability = AbilityBuilder.defineAbility(b -> b.can("read", "Post"));

        AtomicBoolean called = new AtomicBoolean(false);
        ability.on("updated", event -> called.set(true));
        ability.update(new ArrayList<>());

        assertTrue(called.get());
    }

    @Test
    public void testUnsubscribe() {
        Ability ability = AbilityBuilder.defineAbility(b -> b.can("read", "Post"));

        AtomicBoolean called = new AtomicBoolean(false);
        Runnable unsub = ability.on("update", event -> called.set(true));
        unsub.run();
        ability.update(new ArrayList<>());

        assertFalse(called.get());
    }

    @Test
    public void testDoubleUnsubscribeSafe() {
        Ability ability = AbilityBuilder.defineAbility(b -> b.can("read", "Post"));

        AtomicBoolean called1 = new AtomicBoolean(false);
        AtomicBoolean called2 = new AtomicBoolean(false);
        Runnable unsub = ability.on("update", event -> called1.set(true));
        ability.on("update", event -> called2.set(true));
        unsub.run();
        unsub.run();
        ability.update(new ArrayList<>());

        assertFalse(called1.get());
        assertTrue(called2.get());
    }

    @Test
    public void testSelfUnsubscribeDuringEmission() {
        Ability ability = AbilityBuilder.defineAbility(b -> b.can("read", "Post"));

        AtomicBoolean handler0Called = new AtomicBoolean(false);
        AtomicBoolean handler1Called = new AtomicBoolean(false);
        final Runnable[] unsub = new Runnable[1];
        unsub[0] = ability.on("updated", event -> {
            handler0Called.set(true);
            unsub[0].run();
        });
        ability.on("updated", event -> handler1Called.set(true));

        ability.update(new ArrayList<>());

        assertTrue(handler0Called.get());
        assertTrue(handler1Called.get());
    }

    @Test
    public void testUnsubscribeOtherDuringEmission() {
        Ability ability = AbilityBuilder.defineAbility(b -> b.can("read", "Post"));

        List<Integer> results = new ArrayList<>();
        final Runnable[] unsub = new Runnable[3];

        unsub[0] = ability.on("updated", event -> results.add(0));
        unsub[1] = ability.on("updated", event -> results.add(1));
        unsub[2] = ability.on("updated", event -> {
            results.add(2);
            unsub[1].run();
        });

        ability.update(new ArrayList<>());

        assertEquals(Arrays.asList(2, 1, 0), results);

        results.clear();
        ability.update(Collections.singletonList(RawRule.builder().action("read").subject("all").build()));

        assertEquals(Arrays.asList(2, 0), results);
    }

    @Test
    public void testUnsubscribeLast() {
        Ability ability = AbilityBuilder.defineAbility(b -> b.can("read", "Post"));

        List<Integer> results = new ArrayList<>();
        final Runnable[] unsub = new Runnable[3];

        unsub[0] = ability.on("updated", event -> results.add(0));
        unsub[1] = ability.on("updated", event -> results.add(1));
        unsub[2] = ability.on("updated", event -> results.add(2));
        unsub[2].run();

        ability.update(new ArrayList<>());

        assertEquals(Arrays.asList(1, 0), results);
    }

    // ---- Input validation ----

    @Test(expected = IllegalArgumentException.class)
    public void testActionsForThrowsOnNullSubjectType() {
        Ability ability = AbilityBuilder.defineAbility(b -> b.can("read", "Post"));
        ability.actionsFor(null);
    }

    // ---- detectSubjectType fallback for non-Map objects ----

    @Test
    public void testDetectSubjectTypeReturnsClassSimpleNameForNonMapObjects() {
        Ability ability = new Ability(new ArrayList<>(), AbilityOptions.builder()
                .conditionsMatcher(ConditionsMatcher::match)
                .fieldMatcher(FieldMatcher::match)
                .build());

        // For a plain Java object, detectSubjectType should return class simple name
        // (matching JS constructor.name behavior)
        assertEquals("Integer", ability.detectSubjectType(42));
        assertEquals("ArrayList", ability.detectSubjectType(new ArrayList<>()));
        // String subjects are treated as subject type names, returned as-is
        assertEquals("Post", ability.detectSubjectType("Post"));
    }

    @Test
    public void testDetectSubjectTypeReturnsNullSubjectTypeForNull() {
        Ability ability = new Ability(new ArrayList<>(), AbilityOptions.builder()
                .conditionsMatcher(ConditionsMatcher::match)
                .fieldMatcher(FieldMatcher::match)
                .build());

        assertEquals("all", ability.detectSubjectType(null));
    }

    @Test
    public void testDetectSubjectTypeUsesCustomFnForNonMapObjects() {
        Ability ability = new Ability(new ArrayList<>(), AbilityOptions.builder()
                .conditionsMatcher(ConditionsMatcher::match)
                .fieldMatcher(FieldMatcher::match)
                .detectSubjectType(obj -> "CustomType")
                .build());

        assertEquals("CustomType", ability.detectSubjectType(42));
    }

    // ---- Helper methods ----

    @SuppressWarnings("unchecked")
    private static Map<String, Object> mapOf(Object... keyValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put((String) keyValues[i], keyValues[i + 1]);
        }
        return map;
    }
}
