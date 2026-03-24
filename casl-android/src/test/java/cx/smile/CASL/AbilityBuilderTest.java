package cx.smile.CASL;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class AbilityBuilderTest {

    @Test
    public void testCanAndCannotSingleAction() {
        AbilityBuilder b = new AbilityBuilder();
        b.can("read", "Post");
        b.cannot("read", "User");

        List<RawRule> rules = b.getRules();
        assertEquals(2, rules.size());

        assertEquals(Collections.singletonList("read"), rules.get(0).getAction());
        assertEquals(Collections.singletonList("Post"), rules.get(0).getSubject());
        assertFalse(rules.get(0).isInverted());

        assertEquals(Collections.singletonList("read"), rules.get(1).getAction());
        assertEquals(Collections.singletonList("User"), rules.get(1).getSubject());
        assertTrue(rules.get(1).isInverted());
    }

    @Test
    public void testMultipleActions() {
        AbilityBuilder b = new AbilityBuilder();
        b.can(Arrays.asList("read", "update"), "Post");

        List<RawRule> rules = b.getRules();
        assertEquals(1, rules.size());
        assertEquals(Arrays.asList("read", "update"), rules.get(0).getAction());
        assertEquals(Collections.singletonList("Post"), rules.get(0).getSubject());
    }

    @Test
    public void testMultipleSubjects() {
        AbilityBuilder b = new AbilityBuilder();
        b.can("read", Arrays.asList("Post", "User"));

        List<RawRule> rules = b.getRules();
        assertEquals(1, rules.size());
        assertEquals(Collections.singletonList("read"), rules.get(0).getAction());
        assertEquals(Arrays.asList("Post", "User"), rules.get(0).getSubject());
    }

    @Test
    public void testConditions() {
        AbilityBuilder b = new AbilityBuilder();
        Map<String, Object> cond = new HashMap<>();
        cond.put("author", "me");
        b.can("read", "Post", cond);
        b.cannot("read", "Post", Collections.singletonMap("private", (Object) true));

        List<RawRule> rules = b.getRules();
        assertEquals(2, rules.size());
        assertEquals(cond, rules.get(0).getConditions());
        assertFalse(rules.get(0).isInverted());

        assertTrue(rules.get(1).isInverted());
        assertNotNull(rules.get(1).getConditions());
    }

    @Test
    public void testFields() {
        AbilityBuilder b = new AbilityBuilder();
        b.can("read", "Post", Arrays.asList("title", "id"));

        List<RawRule> rules = b.getRules();
        assertEquals(1, rules.size());
        assertEquals(Arrays.asList("title", "id"), rules.get(0).getFields());
    }

    @Test
    public void testFieldsAndConditions() {
        AbilityBuilder b = new AbilityBuilder();
        Map<String, Object> cond = Collections.singletonMap("private", (Object) true);
        b.can("read", "Post", Collections.singletonList("title"), cond);

        List<RawRule> rules = b.getRules();
        assertEquals(1, rules.size());
        assertEquals(Collections.singletonList("title"), rules.get(0).getFields());
        assertEquals(cond, rules.get(0).getConditions());
    }

    @Test
    public void testClaimBasedRules() {
        AbilityBuilder b = new AbilityBuilder();
        b.can("read");
        b.can("write");
        b.cannot("delete");

        List<RawRule> rules = b.getRules();
        assertEquals(3, rules.size());
        assertEquals(Collections.singletonList("read"), rules.get(0).getAction());
        assertNull(rules.get(0).getSubject());

        assertTrue(rules.get(2).isInverted());
    }

    @Test
    public void testBecauseReason() {
        AbilityBuilder b = new AbilityBuilder();
        b.can("read", "Book");
        b.cannot("read", "Book", Collections.singletonMap("private", (Object) true)).because("is private");

        List<RawRule> rules = b.getRules();
        assertEquals(2, rules.size());
        assertNull(rules.get(0).getReason());
        assertEquals("is private", rules.get(1).getReason());
    }

    @Test
    public void testDefineAbilityDSL() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("read", "Book");
            b.cannot("read", "Book", Collections.singletonMap("private", (Object) true));
        });

        assertNotNull(ability);
        assertEquals(2, ability.getRules().size());
        assertTrue(ability.can("read", "Book"));
    }

    @Test
    public void testDefineAbilityWithOptions() {
        AbilityOptions options = AbilityOptions.builder()
                .detectSubjectType(subject -> {
                    if (subject instanceof String) return (String) subject;
                    if (subject instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> map = (Map<String, Object>) subject;
                        Object modelName = map.get("ModelName");
                        if (modelName instanceof String) return (String) modelName;
                    }
                    return "all";
                })
                .build();

        Ability ability = AbilityBuilder.defineAbility(b -> b.can("read", "Book"), options);

        Map<String, Object> obj = new HashMap<>();
        obj.put("ModelName", "Book");
        assertTrue(ability.can("read", obj));
    }

    @Test
    public void testBuildMethod() {
        AbilityBuilder b = new AbilityBuilder();
        b.can("read", "Post");
        Ability ability = b.build();

        assertNotNull(ability);
        assertTrue(ability.can("read", "Post"));
    }

    @Test
    public void testBuildWithOptions() {
        AbilityBuilder b = new AbilityBuilder();
        b.can("read", "Post");
        AbilityOptions options = AbilityOptions.builder().build();
        Ability ability = b.build(options);

        assertNotNull(ability);
        assertTrue(ability.can("read", "Post"));
    }
}
