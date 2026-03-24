package cx.smile.CASL;

import org.junit.Test;

import java.util.*;
import java.util.function.Function;

import static org.junit.Assert.*;

public class PermittedFieldsTest {

    private static final Function<Rule, List<String>> defaultFieldsFrom =
            rule -> rule.getFields() != null ? rule.getFields() : Arrays.asList("title", "description");

    @Test
    public void testEmptyRulesReturnsEmpty() {
        Ability ability = new Ability(new ArrayList<>(), AbilityOptions.builder()
                .conditionsMatcher(ConditionsMatcher::match)
                .fieldMatcher(FieldMatcher::match)
                .build());

        List<String> fields = PermittedFields.permittedFieldsOf(ability, "read", "Post", defaultFieldsFrom);
        assertTrue(fields.isEmpty());
    }

    @Test
    public void testNoFieldsReturnsDefaults() {
        Ability ability = AbilityBuilder.defineAbility(b -> b.can("read", "Post"));

        List<String> fields = PermittedFields.permittedFieldsOf(ability, "read", "Post", defaultFieldsFrom);
        assertEquals(Arrays.asList("title", "description"), fields);
    }

    @Test
    public void testUniqueFields() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("read", "Post", Collections.singletonList("title"));
            b.can("read", "Post", Arrays.asList("title", "description"),
                    Collections.singletonMap("id", (Object) 1));
        });

        List<String> fields = PermittedFields.permittedFieldsOf(ability, "read", "Post", defaultFieldsFrom);
        assertEquals(2, fields.size());
        assertTrue(fields.contains("title"));
        assertTrue(fields.contains("description"));
    }

    @Test
    public void testInvertedRemovesFields() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("read", "Post", Arrays.asList("title", "description"));
            b.cannot("read", "Post", Collections.singletonList("description"));
        });

        List<String> fields = PermittedFields.permittedFieldsOf(ability, "read", "Post", defaultFieldsFrom);
        assertEquals(1, fields.size());
        assertTrue(fields.contains("title"));
    }

    @Test
    public void testCustomFieldsFrom() {
        Ability ability = AbilityBuilder.defineAbility(b -> b.can("read", "Post"));

        List<String> fields = PermittedFields.permittedFieldsOf(ability, "read", "Post",
                rule -> rule.getFields() != null ? rule.getFields() : Collections.singletonList("title"));

        assertEquals(Collections.singletonList("title"), fields);
    }

    // ---- AccessibleFields tests ----

    @Test
    public void testAccessibleFieldsOfType() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("read", "Post", Arrays.asList("title", "description"));
        });

        Function<String, List<String>> getAllFields = type -> Arrays.asList("title", "description", "content", "author");
        PermittedFields.AccessibleFields accessibleFields = new PermittedFields.AccessibleFields(ability, "read", getAllFields);

        List<String> fields = accessibleFields.ofType("Post");
        assertEquals(2, fields.size());
        assertTrue(fields.contains("title"));
        assertTrue(fields.contains("description"));
    }

    @Test
    public void testAccessibleFieldsOfTypeWithNoFieldsReturnsAllFields() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("read", "Post");
        });

        Function<String, List<String>> getAllFields = type -> Arrays.asList("title", "description", "content");
        PermittedFields.AccessibleFields accessibleFields = new PermittedFields.AccessibleFields(ability, "read", getAllFields);

        List<String> fields = accessibleFields.ofType("Post");
        assertEquals(3, fields.size());
        assertTrue(fields.contains("title"));
        assertTrue(fields.contains("description"));
        assertTrue(fields.contains("content"));
    }

    @Test
    public void testAccessibleFieldsOfSubject() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("read", "Post", Arrays.asList("title", "description"),
                    Collections.singletonMap("published", (Object) true));
        });

        Function<String, List<String>> getAllFields = type -> Arrays.asList("title", "description", "content");
        PermittedFields.AccessibleFields accessibleFields = new PermittedFields.AccessibleFields(ability, "read", getAllFields);

        Map<String, Object> post = new LinkedHashMap<>();
        post.put("__caslSubjectType__", "Post");
        post.put("published", true);
        List<String> fields = accessibleFields.of(post);
        assertEquals(2, fields.size());
        assertTrue(fields.contains("title"));
        assertTrue(fields.contains("description"));
    }

    @Test
    public void testAccessibleFieldsOfSubjectNotMatching() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("read", "Post", Arrays.asList("title", "description"),
                    Collections.singletonMap("published", (Object) true));
        });

        Function<String, List<String>> getAllFields = type -> Arrays.asList("title", "description", "content");
        PermittedFields.AccessibleFields accessibleFields = new PermittedFields.AccessibleFields(ability, "read", getAllFields);

        Map<String, Object> post = new LinkedHashMap<>();
        post.put("__caslSubjectType__", "Post");
        post.put("published", false);
        List<String> fields = accessibleFields.of(post);
        assertTrue(fields.isEmpty());
    }

    @Test
    public void testInstanceConditions() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("read", "Post", Collections.singletonList("title"));
            b.can("read", "Post", Arrays.asList("title", "description"),
                    Collections.singletonMap("id", (Object) 1));
            b.cannot("read", "Post", Collections.singletonList("description"),
                    Collections.singletonMap("private", (Object) true));
        });

        // Subject that does not match conditions
        Map<String, Object> postNoMatch = new LinkedHashMap<>();
        postNoMatch.put("__caslSubjectType__", "Post");
        postNoMatch.put("title", "does not match");
        List<String> fields1 = PermittedFields.permittedFieldsOf(ability, "read", postNoMatch, defaultFieldsFrom);
        assertEquals(Collections.singletonList("title"), fields1);

        // Subject that matches conditions
        Map<String, Object> postMatch = new LinkedHashMap<>();
        postMatch.put("__caslSubjectType__", "Post");
        postMatch.put("id", 1);
        postMatch.put("title", "matches conditions");
        List<String> fields2 = PermittedFields.permittedFieldsOf(ability, "read", postMatch, defaultFieldsFrom);
        assertEquals(Arrays.asList("title", "description"), fields2);
    }
}
