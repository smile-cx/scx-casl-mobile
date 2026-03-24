package cx.smile.CASL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class JsonSupportTest {

    // ----------------------------------------------------------------
    // createAbility from JSON string
    // ----------------------------------------------------------------

    @Test
    public void testCreateAbilityFromJsonString() throws JSONException {
        String json = "[{\"action\":\"read\",\"subject\":\"Post\"},{\"action\":\"update\",\"subject\":\"Post\"}]";
        Ability ability = JsonSupport.createAbility(json);

        assertTrue(ability.can("read", "Post"));
        assertTrue(ability.can("update", "Post"));
        assertFalse(ability.can("delete", "Post"));
    }

    @Test
    public void testCreateAbilityFromJsonStringWithConditions() throws JSONException {
        String json = "[{\"action\":\"read\",\"subject\":\"Post\",\"conditions\":{\"published\":true}}]";
        Ability ability = JsonSupport.createAbility(json);

        Map<String, Object> publishedPost = new LinkedHashMap<>();
        publishedPost.put("__caslSubjectType__", "Post");
        publishedPost.put("published", true);

        Map<String, Object> draftPost = new LinkedHashMap<>();
        draftPost.put("__caslSubjectType__", "Post");
        draftPost.put("published", false);

        assertTrue(ability.can("read", publishedPost));
        assertFalse(ability.can("read", draftPost));
    }

    @Test
    public void testCreateAbilityFromJsonStringWithOptions() throws JSONException {
        String json = "[{\"action\":\"read\",\"subject\":\"Post\"}]";
        AbilityOptions options = AbilityOptions.builder()
                .conditionsMatcher(ConditionsMatcher::match)
                .fieldMatcher(FieldMatcher::match)
                .build();
        Ability ability = JsonSupport.createAbility(json, options);

        assertTrue(ability.can("read", "Post"));
    }

    // ----------------------------------------------------------------
    // createAbility from JSONArray
    // ----------------------------------------------------------------

    @Test
    public void testCreateAbilityFromJSONArray() throws JSONException {
        JSONArray rulesArray = new JSONArray();
        JSONObject rule1 = new JSONObject();
        rule1.put("action", "read");
        rule1.put("subject", "Article");
        rulesArray.put(rule1);

        JSONObject rule2 = new JSONObject();
        rule2.put("action", "delete");
        rule2.put("subject", "Article");
        rulesArray.put(rule2);

        Ability ability = JsonSupport.createAbility(rulesArray);

        assertTrue(ability.can("read", "Article"));
        assertTrue(ability.can("delete", "Article"));
        assertFalse(ability.can("update", "Article"));
    }

    @Test
    public void testCreateAbilityFromJSONArrayWithOptions() throws JSONException {
        JSONArray rulesArray = new JSONArray();
        JSONObject rule = new JSONObject();
        rule.put("action", "manage");
        rule.put("subject", "all");
        rulesArray.put(rule);

        AbilityOptions options = AbilityOptions.builder()
                .conditionsMatcher(ConditionsMatcher::match)
                .fieldMatcher(FieldMatcher::match)
                .build();

        Ability ability = JsonSupport.createAbility(rulesArray, options);
        assertTrue(ability.can("read", "Post"));
        assertTrue(ability.can("update", "Comment"));
    }

    // ----------------------------------------------------------------
    // can() with JSONObject subject
    // ----------------------------------------------------------------

    @Test
    public void testCanWithJSONObjectSubject() throws JSONException {
        String json = "[{\"action\":\"read\",\"subject\":\"Post\",\"conditions\":{\"authorId\":1}}]";
        Ability ability = JsonSupport.createAbility(json);

        JSONObject subject = new JSONObject();
        subject.put("__caslSubjectType__", "Post");
        subject.put("authorId", 1);

        assertTrue(JsonSupport.can(ability, "read", subject));
    }

    @Test
    public void testCanWithJSONObjectSubjectField() throws JSONException {
        String json = "[{\"action\":\"read\",\"subject\":\"Post\",\"fields\":[\"title\",\"body\"]}]";
        Ability ability = JsonSupport.createAbility(json);

        JSONObject subject = new JSONObject();
        subject.put("__caslSubjectType__", "Post");

        assertTrue(JsonSupport.can(ability, "read", subject, "title"));
        assertFalse(JsonSupport.can(ability, "read", subject, "secret"));
    }

    @Test
    public void testCannotWithJSONObjectSubject() throws JSONException {
        String json = "[{\"action\":\"read\",\"subject\":\"Post\",\"conditions\":{\"authorId\":1}}]";
        Ability ability = JsonSupport.createAbility(json);

        JSONObject subject = new JSONObject();
        subject.put("__caslSubjectType__", "Post");
        subject.put("authorId", 2);

        assertTrue(JsonSupport.cannot(ability, "read", subject));
    }

    // ----------------------------------------------------------------
    // update from JSON string / JSONArray
    // ----------------------------------------------------------------

    @Test
    public void testUpdateFromJsonString() throws JSONException {
        Ability ability = JsonSupport.createAbility("[{\"action\":\"read\",\"subject\":\"Post\"}]");
        assertTrue(ability.can("read", "Post"));
        assertFalse(ability.can("delete", "Post"));

        JsonSupport.update(ability, "[{\"action\":\"delete\",\"subject\":\"Post\"}]");
        assertFalse(ability.can("read", "Post"));
        assertTrue(ability.can("delete", "Post"));
    }

    @Test
    public void testUpdateFromJSONArray() throws JSONException {
        Ability ability = JsonSupport.createAbility("[{\"action\":\"read\",\"subject\":\"Post\"}]");

        JSONArray newRules = new JSONArray();
        JSONObject rule = new JSONObject();
        rule.put("action", "update");
        rule.put("subject", "Post");
        newRules.put(rule);

        JsonSupport.update(ability, newRules);
        assertFalse(ability.can("read", "Post"));
        assertTrue(ability.can("update", "Post"));
    }

    // ----------------------------------------------------------------
    // rulesToJSON / parseRules round-trip
    // ----------------------------------------------------------------

    @Test
    public void testRulesToJSONAndParseRulesRoundTrip() throws JSONException {
        List<RawRule> original = Arrays.asList(
                RawRule.builder().action("read").subject("Post").build(),
                RawRule.builder().action("update", "delete").subject("Post", "Comment")
                        .conditions(Collections.singletonMap("authorId", (Object) 1))
                        .fields("title", "body")
                        .inverted(true)
                        .reason("not allowed")
                        .build()
        );

        JSONArray jsonArray = JsonSupport.rulesToJSON(original);
        List<RawRule> parsed = JsonSupport.parseRules(jsonArray);

        assertEquals(original.size(), parsed.size());

        // First rule
        assertEquals(original.get(0).getAction(), parsed.get(0).getAction());
        assertEquals(original.get(0).getSubject(), parsed.get(0).getSubject());
        assertNull(parsed.get(0).getConditions());
        assertFalse(parsed.get(0).isInverted());

        // Second rule
        assertEquals(original.get(1).getAction(), parsed.get(1).getAction());
        assertEquals(original.get(1).getSubject(), parsed.get(1).getSubject());
        assertEquals(1, parsed.get(1).getConditions().get("authorId"));
        assertEquals(Arrays.asList("title", "body"), parsed.get(1).getFields());
        assertTrue(parsed.get(1).isInverted());
        assertEquals("not allowed", parsed.get(1).getReason());
    }

    @Test
    public void testRulesToJSONStringRoundTrip() throws JSONException {
        List<RawRule> original = Collections.singletonList(
                RawRule.builder().action("read").subject("Post")
                        .conditions(Collections.singletonMap("status", (Object) "published"))
                        .build()
        );

        String jsonStr = JsonSupport.rulesToJSON(original).toString();
        List<RawRule> parsed = JsonSupport.parseRules(jsonStr);

        assertEquals(1, parsed.size());
        assertEquals(Collections.singletonList("read"), parsed.get(0).getAction());
        assertEquals(Collections.singletonList("Post"), parsed.get(0).getSubject());
        assertEquals("published", parsed.get(0).getConditions().get("status"));
    }

    // ----------------------------------------------------------------
    // subject from JSONObject with type
    // ----------------------------------------------------------------

    @Test
    public void testSubjectFromJSONObjectWithType() throws JSONException {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("title", "Hello");
        jsonObj.put("authorId", 42);

        Map<String, Object> subj = JsonSupport.subject(jsonObj, "Post");

        assertEquals("Post", subj.get("__caslSubjectType__"));
        assertEquals("Hello", subj.get("title"));
        assertEquals(42, subj.get("authorId"));
    }

    @Test
    public void testSubjectUsedWithAbility() throws JSONException {
        Ability ability = JsonSupport.createAbility(
                "[{\"action\":\"read\",\"subject\":\"Post\",\"conditions\":{\"authorId\":42}}]");

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("title", "Hello");
        jsonObj.put("authorId", 42);

        Map<String, Object> subj = JsonSupport.subject(jsonObj, "Post");
        assertTrue(ability.can("read", subj));
    }

    // ----------------------------------------------------------------
    // packRules / unpackRules JSON round-trip
    // ----------------------------------------------------------------

    @Test
    public void testPackRulesUnpackRulesJSONRoundTrip() throws JSONException {
        List<RawRule> original = Arrays.asList(
                RawRule.builder().action("read").subject("Post").build(),
                RawRule.builder().action("update").subject("Post")
                        .conditions(Collections.singletonMap("authorId", (Object) 1))
                        .build(),
                RawRule.builder().action("delete").subject("Post").inverted(true)
                        .reason("admins only")
                        .build()
        );

        String packedJson = JsonSupport.packRulesToJSON(original);
        assertNotNull(packedJson);

        List<RawRule> unpacked = JsonSupport.unpackRulesFromJSON(packedJson);
        assertEquals(original.size(), unpacked.size());

        assertEquals(Collections.singletonList("read"), unpacked.get(0).getAction());
        assertEquals(Collections.singletonList("Post"), unpacked.get(0).getSubject());

        assertEquals(Collections.singletonList("update"), unpacked.get(1).getAction());
        assertNotNull(unpacked.get(1).getConditions());

        assertEquals(Collections.singletonList("delete"), unpacked.get(2).getAction());
        assertTrue(unpacked.get(2).isInverted());
        assertEquals("admins only", unpacked.get(2).getReason());
    }

    // ----------------------------------------------------------------
    // toMap / toJSONObject conversion
    // ----------------------------------------------------------------

    @Test
    public void testToMapBasicTypes() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("name", "Alice");
        obj.put("age", 30);
        obj.put("active", true);
        obj.put("score", 9.5);
        obj.put("nothing", JSONObject.NULL);

        Map<String, Object> map = JsonSupport.toMap(obj);
        assertEquals("Alice", map.get("name"));
        assertEquals(30, map.get("age"));
        assertEquals(true, map.get("active"));
        assertEquals(9.5, map.get("score"));
        assertNull(map.get("nothing"));
    }

    @Test
    public void testToMapNestedObject() throws JSONException {
        JSONObject inner = new JSONObject();
        inner.put("city", "Rome");

        JSONObject outer = new JSONObject();
        outer.put("address", inner);

        Map<String, Object> map = JsonSupport.toMap(outer);
        assertTrue(map.get("address") instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> addressMap = (Map<String, Object>) map.get("address");
        assertEquals("Rome", addressMap.get("city"));
    }

    @Test
    public void testToMapNestedArray() throws JSONException {
        JSONArray tags = new JSONArray();
        tags.put("java");
        tags.put("android");

        JSONObject obj = new JSONObject();
        obj.put("tags", tags);

        Map<String, Object> map = JsonSupport.toMap(obj);
        assertTrue(map.get("tags") instanceof List);
        @SuppressWarnings("unchecked")
        List<Object> tagList = (List<Object>) map.get("tags");
        assertEquals(Arrays.asList("java", "android"), tagList);
    }

    @Test
    public void testToJSONObjectBasicTypes() throws JSONException {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", "Bob");
        map.put("age", 25);
        map.put("active", false);
        map.put("nothing", null);

        JSONObject obj = JsonSupport.toJSONObject(map);
        assertEquals("Bob", obj.getString("name"));
        assertEquals(25, obj.getInt("age"));
        assertFalse(obj.getBoolean("active"));
        assertTrue(obj.isNull("nothing"));
    }

    @Test
    public void testToJSONObjectNestedMapAndList() throws JSONException {
        Map<String, Object> inner = new LinkedHashMap<>();
        inner.put("key", "value");

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("nested", inner);
        map.put("items", Arrays.asList(1, 2, 3));

        JSONObject obj = JsonSupport.toJSONObject(map);

        JSONObject nestedObj = obj.getJSONObject("nested");
        assertEquals("value", nestedObj.getString("key"));

        JSONArray arr = obj.getJSONArray("items");
        assertEquals(3, arr.length());
        assertEquals(1, arr.getInt(0));
    }

    @Test
    public void testToMapToJSONObjectRoundTrip() throws JSONException {
        Map<String, Object> original = new LinkedHashMap<>();
        original.put("str", "hello");
        original.put("num", 42);
        original.put("bool", true);
        original.put("nil", null);

        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("x", 1);
        original.put("nested", nested);
        original.put("list", Arrays.asList("a", "b"));

        JSONObject jsonObj = JsonSupport.toJSONObject(original);
        Map<String, Object> restored = JsonSupport.toMap(jsonObj);

        assertEquals("hello", restored.get("str"));
        assertEquals(42, restored.get("num"));
        assertEquals(true, restored.get("bool"));
        assertNull(restored.get("nil"));

        @SuppressWarnings("unchecked")
        Map<String, Object> restoredNested = (Map<String, Object>) restored.get("nested");
        assertEquals(1, restoredNested.get("x"));

        @SuppressWarnings("unchecked")
        List<Object> restoredList = (List<Object>) restored.get("list");
        assertEquals(Arrays.asList("a", "b"), restoredList);
    }

    // ----------------------------------------------------------------
    // Invalid JSON handling
    // ----------------------------------------------------------------

    @Test(expected = JSONException.class)
    public void testParseRulesInvalidJson() throws JSONException {
        JsonSupport.parseRules("not valid json");
    }

    @Test(expected = JSONException.class)
    public void testCreateAbilityInvalidJson() throws JSONException {
        JsonSupport.createAbility("{\"not\": \"an array\"}");
    }

    @Test(expected = IllegalStateException.class)
    public void testParseRuleMissingAction() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("subject", "Post");
        // action is required; builder.build() should throw IllegalStateException
        JsonSupport.parseRule(obj);
    }

    @Test(expected = JSONException.class)
    public void testUnpackRulesFromInvalidJSON() throws JSONException {
        JsonSupport.unpackRulesFromJSON("garbage");
    }

    // ----------------------------------------------------------------
    // Conditions with MongoDB operators from JSON
    // ----------------------------------------------------------------

    @Test
    public void testConditionsWithGteOperator() throws JSONException {
        String json = "[{\"action\":\"read\",\"subject\":\"Post\",\"conditions\":{\"views\":{\"$gte\":100}}}]";
        Ability ability = JsonSupport.createAbility(json);

        Map<String, Object> popular = new LinkedHashMap<>();
        popular.put("__caslSubjectType__", "Post");
        popular.put("views", 150);

        Map<String, Object> unpopular = new LinkedHashMap<>();
        unpopular.put("__caslSubjectType__", "Post");
        unpopular.put("views", 50);

        assertTrue(ability.can("read", popular));
        assertFalse(ability.can("read", unpopular));
    }

    @Test
    public void testConditionsWithInOperator() throws JSONException {
        String json = "[{\"action\":\"read\",\"subject\":\"Post\",\"conditions\":{\"status\":{\"$in\":[\"published\",\"archived\"]}}}]";
        Ability ability = JsonSupport.createAbility(json);

        Map<String, Object> published = new LinkedHashMap<>();
        published.put("__caslSubjectType__", "Post");
        published.put("status", "published");

        Map<String, Object> draft = new LinkedHashMap<>();
        draft.put("__caslSubjectType__", "Post");
        draft.put("status", "draft");

        assertTrue(ability.can("read", published));
        assertFalse(ability.can("read", draft));
    }

    @Test
    public void testConditionsWithNeOperator() throws JSONException {
        String json = "[{\"action\":\"delete\",\"subject\":\"Post\",\"conditions\":{\"status\":{\"$ne\":\"protected\"}}}]";
        Ability ability = JsonSupport.createAbility(json);

        Map<String, Object> normal = new LinkedHashMap<>();
        normal.put("__caslSubjectType__", "Post");
        normal.put("status", "draft");

        Map<String, Object> protectedPost = new LinkedHashMap<>();
        protectedPost.put("__caslSubjectType__", "Post");
        protectedPost.put("status", "protected");

        assertTrue(ability.can("delete", normal));
        assertFalse(ability.can("delete", protectedPost));
    }

    @Test
    public void testConditionsWithOrOperator() throws JSONException {
        String json = "[{\"action\":\"read\",\"subject\":\"Post\",\"conditions\":{\"$or\":[{\"authorId\":1},{\"published\":true}]}}]";
        Ability ability = JsonSupport.createAbility(json);

        Map<String, Object> ownPost = new LinkedHashMap<>();
        ownPost.put("__caslSubjectType__", "Post");
        ownPost.put("authorId", 1);
        ownPost.put("published", false);

        Map<String, Object> publishedPost = new LinkedHashMap<>();
        publishedPost.put("__caslSubjectType__", "Post");
        publishedPost.put("authorId", 2);
        publishedPost.put("published", true);

        Map<String, Object> otherDraft = new LinkedHashMap<>();
        otherDraft.put("__caslSubjectType__", "Post");
        otherDraft.put("authorId", 2);
        otherDraft.put("published", false);

        assertTrue(ability.can("read", ownPost));
        assertTrue(ability.can("read", publishedPost));
        assertFalse(ability.can("read", otherDraft));
    }

    // ----------------------------------------------------------------
    // exportRules
    // ----------------------------------------------------------------

    @Test
    public void testExportRules() throws JSONException {
        Ability ability = JsonSupport.createAbility("[{\"action\":\"read\",\"subject\":\"Post\"}]");
        String exported = JsonSupport.exportRules(ability);

        // Should be parseable and produce an equivalent ability
        Ability restored = JsonSupport.createAbility(exported);
        assertTrue(restored.can("read", "Post"));
        assertFalse(restored.can("delete", "Post"));
    }

    // ----------------------------------------------------------------
    // Multi-value action/subject arrays in JSON
    // ----------------------------------------------------------------

    @Test
    public void testParseRuleWithArrayActions() throws JSONException {
        String json = "[{\"action\":[\"read\",\"update\"],\"subject\":[\"Post\",\"Comment\"]}]";
        Ability ability = JsonSupport.createAbility(json);

        assertTrue(ability.can("read", "Post"));
        assertTrue(ability.can("update", "Post"));
        assertTrue(ability.can("read", "Comment"));
        assertTrue(ability.can("update", "Comment"));
        assertFalse(ability.can("delete", "Post"));
    }

    // ----------------------------------------------------------------
    // Inverted rules from JSON
    // ----------------------------------------------------------------

    @Test
    public void testInvertedRuleFromJSON() throws JSONException {
        String json = "[{\"action\":\"manage\",\"subject\":\"all\"},{\"action\":\"delete\",\"subject\":\"Post\",\"inverted\":true}]";
        Ability ability = JsonSupport.createAbility(json);

        assertTrue(ability.can("read", "Post"));
        assertTrue(ability.can("update", "Post"));
        assertFalse(ability.can("delete", "Post"));
    }
}
