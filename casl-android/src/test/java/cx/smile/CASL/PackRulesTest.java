package cx.smile.CASL;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class PackRulesTest {

    // ---- packRules ----

    @Test
    public void testPackConvertsToArrays() {
        List<RawRule> rules = Arrays.asList(
                RawRule.builder().action("read").subject("Post").build(),
                RawRule.builder().action("delete").subject("Post").build()
        );

        List<List<Object>> packed = PackRules.packRules(rules);
        assertEquals(2, packed.size());
        assertTrue(packed.get(0) instanceof List);
        assertTrue(packed.get(1) instanceof List);
    }

    @Test
    public void testPackActionsAs1stElement() {
        List<RawRule> rules = Collections.singletonList(
                RawRule.builder().action("read").subject("Post").build()
        );

        List<List<Object>> packed = PackRules.packRules(rules);
        assertEquals("read", packed.get(0).get(0));
        assertEquals(2, packed.get(0).size());
    }

    @Test
    public void testPackJoinsActionsWithComma() {
        List<RawRule> rules = Collections.singletonList(
                RawRule.builder().action("read", "update").subject("Post").build()
        );

        List<List<Object>> packed = PackRules.packRules(rules);
        assertEquals("read,update", packed.get(0).get(0));
        assertEquals(2, packed.get(0).size());
    }

    @Test
    public void testPackSubjectAs2ndElement() {
        List<RawRule> rules = Collections.singletonList(
                RawRule.builder().action("read").subject("Post").build()
        );

        List<List<Object>> packed = PackRules.packRules(rules);
        assertEquals("Post", packed.get(0).get(1));
        assertEquals(2, packed.get(0).size());
    }

    @Test
    public void testPackJoinsSubjectsWithComma() {
        List<RawRule> rules = Collections.singletonList(
                RawRule.builder().action("read").subject("Post", "Comment").build()
        );

        List<List<Object>> packed = PackRules.packRules(rules);
        assertEquals("Post,Comment", packed.get(0).get(1));
    }

    @Test
    public void testPackConditionsAs3rdElement() {
        Map<String, Object> conditions = Collections.singletonMap("private", (Object) true);
        List<RawRule> rules = Collections.singletonList(
                RawRule.builder().action("read").subject("Post").conditions(conditions).build()
        );

        List<List<Object>> packed = PackRules.packRules(rules);
        assertEquals(conditions, packed.get(0).get(2));
        assertEquals(3, packed.get(0).size());
    }

    @Test
    public void testPackPuts0ForMissingConditionsWhenFieldsDefined() {
        List<RawRule> rules = Collections.singletonList(
                RawRule.builder().action("read").subject("Post").fields("title").build()
        );

        List<List<Object>> packed = PackRules.packRules(rules);
        assertEquals(0, packed.get(0).get(2));
        assertEquals(5, packed.get(0).size());
    }

    @Test
    public void testPackInvertedAs4thElement() {
        List<RawRule> rules = Collections.singletonList(
                RawRule.builder().action("read").subject("Post").inverted(true).build()
        );

        List<List<Object>> packed = PackRules.packRules(rules);
        assertEquals(1, packed.get(0).get(3));
        assertEquals(4, packed.get(0).size());
    }

    @Test
    public void testPackFieldsAs5thElement() {
        List<RawRule> rules = Collections.singletonList(
                RawRule.builder().action("read").subject("Post").fields("title", "description").build()
        );

        List<List<Object>> packed = PackRules.packRules(rules);
        assertEquals("title,description", packed.get(0).get(4));
        assertEquals(5, packed.get(0).size());
    }

    @Test
    public void testPackPuts0ForMissingFieldsWhenReasonProvided() {
        String reason = "forbidden reason";
        List<RawRule> rules = Collections.singletonList(
                RawRule.builder().action("read").subject("Post").reason(reason).build()
        );

        List<List<Object>> packed = PackRules.packRules(rules);
        assertEquals(0, packed.get(0).get(4));
        assertEquals(6, packed.get(0).size());
    }

    @Test
    public void testPackReasonAs6thElement() {
        String reason = "forbidden reason";
        List<RawRule> rules = Collections.singletonList(
                RawRule.builder().action("read").subject("Post").reason(reason).build()
        );

        List<List<Object>> packed = PackRules.packRules(rules);
        assertEquals(reason, packed.get(0).get(5));
        assertEquals(6, packed.get(0).size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPackFiltersOutCaslSubjectTypeFromConditions() {
        Map<String, Object> conditions = new LinkedHashMap<>();
        conditions.put("__caslSubjectType__", "Post");
        conditions.put("status", "active");

        List<RawRule> rules = Collections.singletonList(
                RawRule.builder().action("read").subject("Post").conditions(conditions).build()
        );

        List<List<Object>> packed = PackRules.packRules(rules);
        Map<String, Object> packedConditions = (Map<String, Object>) packed.get(0).get(2);
        assertFalse(packedConditions.containsKey("__caslSubjectType__"));
        assertEquals("active", packedConditions.get("status"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPackPreservesConditionsWithoutCaslSubjectType() {
        Map<String, Object> conditions = Collections.singletonMap("status", (Object) "active");
        List<RawRule> rules = Collections.singletonList(
                RawRule.builder().action("read").subject("Post").conditions(conditions).build()
        );

        List<List<Object>> packed = PackRules.packRules(rules);
        Map<String, Object> packedConditions = (Map<String, Object>) packed.get(0).get(2);
        assertEquals("active", packedConditions.get("status"));
    }

    @Test
    public void testPackTrailingTrimming() {
        // A rule with only action+subject and no other fields should pack to exactly 2 elements
        List<RawRule> rules = Collections.singletonList(
                RawRule.builder().action("read").subject("Post").build()
        );
        List<List<Object>> packed = PackRules.packRules(rules);
        assertEquals(2, packed.get(0).size());
    }

    @Test
    public void testPackConditionsAsZeroWhenFieldsDefined() {
        // When fields are defined but no conditions, conditions slot should be 0
        List<RawRule> rules = Collections.singletonList(
                RawRule.builder().action("read").subject("Post").fields("title").build()
        );
        List<List<Object>> packed = PackRules.packRules(rules);
        assertEquals(0, packed.get(0).get(2));
    }

    // ---- unpackRules ----

    @Test
    public void testUnpackConvertsToObjects() {
        List<List<Object>> packed = Arrays.asList(
                Arrays.asList("read", "Post"),
                Arrays.asList("delete", "Post")
        );

        List<RawRule> rules = PackRules.unpackRules(packed);
        assertEquals(2, rules.size());
    }

    @Test
    public void testUnpackActionSplits() {
        List<List<Object>> packed = Collections.singletonList(
                Arrays.asList("read,update", "Post")
        );

        List<RawRule> rules = PackRules.unpackRules(packed);
        assertEquals(Arrays.asList("read", "update"), rules.get(0).getAction());
    }

    @Test
    public void testUnpackSingleActionToArray() {
        List<List<Object>> packed = Collections.singletonList(
                Arrays.asList("read", "Post")
        );

        List<RawRule> rules = PackRules.unpackRules(packed);
        assertEquals(Collections.singletonList("read"), rules.get(0).getAction());
    }

    @Test
    public void testUnpackSubjectSplits() {
        List<List<Object>> packed = Collections.singletonList(
                Arrays.asList("read", "Post,Comment")
        );

        List<RawRule> rules = PackRules.unpackRules(packed);
        assertEquals(Arrays.asList("Post", "Comment"), rules.get(0).getSubject());
    }

    @Test
    public void testUnpackSingleSubjectToArray() {
        List<List<Object>> packed = Collections.singletonList(
                Arrays.asList("read", "Post")
        );

        List<RawRule> rules = PackRules.unpackRules(packed);
        assertEquals(Collections.singletonList("Post"), rules.get(0).getSubject());
    }

    @Test
    public void testUnpackConditions() {
        Map<String, Object> conditions = Collections.singletonMap("private", (Object) true);
        List<List<Object>> packed = Collections.singletonList(
                Arrays.asList("read", "Post,Comment", conditions)
        );

        List<RawRule> rules = PackRules.unpackRules(packed);
        assertEquals(conditions, rules.get(0).getConditions());
    }

    @Test
    public void testUnpackConditions0ToNull() {
        List<List<Object>> packed = Collections.singletonList(
                Arrays.asList("read", "Post,Comment", 0, 1)
        );

        List<RawRule> rules = PackRules.unpackRules(packed);
        assertNull(rules.get(0).getConditions());
    }

    @Test
    public void testUnpackInvertedToBoolean() {
        List<List<Object>> packed = Collections.singletonList(
                Arrays.asList("read", "Post,Comment", 0, 1)
        );

        List<RawRule> rules = PackRules.unpackRules(packed);
        assertTrue(rules.get(0).isInverted());
    }

    @Test
    public void testUnpackInvertedDefaultFalse() {
        List<List<Object>> packed = Collections.singletonList(
                Arrays.asList("read", "Post,Comment")
        );

        List<RawRule> rules = PackRules.unpackRules(packed);
        assertFalse(rules.get(0).isInverted());
    }

    @Test
    public void testUnpackFieldsSplits() {
        List<List<Object>> packed = Collections.singletonList(
                Arrays.asList("read", "Post,Comment", 1, 0, "title,description")
        );

        List<RawRule> rules = PackRules.unpackRules(packed);
        assertEquals(Arrays.asList("title", "description"), rules.get(0).getFields());
    }

    @Test
    public void testUnpackFieldsEmptyToNull() {
        List<List<Object>> packed = Collections.singletonList(
                Arrays.asList("read", "Post,Comment", 1, 0, "")
        );

        List<RawRule> rules = PackRules.unpackRules(packed);
        assertNull(rules.get(0).getFields());
    }

    @Test
    public void testUnpackReason() {
        String reason = "forbidden reason";
        List<List<Object>> packed = Collections.singletonList(
                Arrays.asList("read", "Post,Comment", 1, 0, 0, reason)
        );

        List<RawRule> rules = PackRules.unpackRules(packed);
        assertEquals(reason, rules.get(0).getReason());
    }

    @Test
    public void testUnpackReasonEmptyToNull() {
        List<List<Object>> packed = Collections.singletonList(
                Arrays.asList("read", "Post,Comment", 1, 0, 0, "")
        );

        List<RawRule> rules = PackRules.unpackRules(packed);
        assertNull(rules.get(0).getReason());
    }
}
