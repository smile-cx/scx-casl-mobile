package cx.smile.CASL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Tests for convenience static methods on RawRule and Ability.
 */
public class ConvenienceApiTest {

    // ----------------------------------------------------------------
    // RawRule.listFromJson(String)
    // ----------------------------------------------------------------

    @Test
    public void testListFromJsonString() throws JSONException {
        String json = "[{\"action\":\"read\",\"subject\":\"Post\"},{\"action\":\"update\",\"subject\":\"Post\"}]";
        List<RawRule> rules = RawRule.listFromJson(json);

        assertEquals(2, rules.size());
        assertEquals(Collections.singletonList("read"), rules.get(0).getAction());
        assertEquals(Collections.singletonList("Post"), rules.get(0).getSubject());
        assertEquals(Collections.singletonList("update"), rules.get(1).getAction());
    }

    // ----------------------------------------------------------------
    // RawRule.listFromJson(JSONArray)
    // ----------------------------------------------------------------

    @Test
    public void testListFromJsonArray() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        JSONObject rule1 = new JSONObject();
        rule1.put("action", "read");
        rule1.put("subject", "Post");
        jsonArray.put(rule1);

        JSONObject rule2 = new JSONObject();
        rule2.put("action", "delete");
        rule2.put("subject", "Comment");
        jsonArray.put(rule2);

        List<RawRule> rules = RawRule.listFromJson(jsonArray);

        assertEquals(2, rules.size());
        assertEquals(Collections.singletonList("read"), rules.get(0).getAction());
        assertEquals(Collections.singletonList("delete"), rules.get(1).getAction());
    }

    // ----------------------------------------------------------------
    // RawRule.fromJson(String)
    // ----------------------------------------------------------------

    @Test
    public void testFromJsonString() throws JSONException {
        String json = "{\"action\":\"read\",\"subject\":\"Post\",\"conditions\":{\"published\":true}}";
        RawRule rule = RawRule.fromJson(json);

        assertEquals(Collections.singletonList("read"), rule.getAction());
        assertEquals(Collections.singletonList("Post"), rule.getSubject());
        assertNotNull(rule.getConditions());
        assertEquals(true, rule.getConditions().get("published"));
    }

    // ----------------------------------------------------------------
    // RawRule.fromJson(JSONObject)
    // ----------------------------------------------------------------

    @Test
    public void testFromJsonObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("action", "update");
        obj.put("subject", "Post");

        RawRule rule = RawRule.fromJson(obj);

        assertEquals(Collections.singletonList("update"), rule.getAction());
        assertEquals(Collections.singletonList("Post"), rule.getSubject());
    }

    // ----------------------------------------------------------------
    // Ability.fromRules(List<RawRule>)
    // ----------------------------------------------------------------

    @Test
    public void testAbilityFromRules() {
        List<RawRule> rules = Arrays.asList(
                RawRule.builder().action("read").subject("Post").build(),
                RawRule.builder().action("update").subject("Post").build()
        );

        Ability ability = Ability.fromRules(rules);

        assertTrue(ability.can("read", "Post"));
        assertTrue(ability.can("update", "Post"));
        assertFalse(ability.can("delete", "Post"));
    }

    @Test
    public void testAbilityFromRulesWithOptions() {
        List<RawRule> rules = Collections.singletonList(
                RawRule.builder().action("read").subject("Post")
                        .conditions(Collections.singletonMap("published", (Object) true))
                        .build()
        );

        AbilityOptions options = AbilityOptions.builder()
                .conditionsMatcher(ConditionsMatcher::match)
                .fieldMatcher(FieldMatcher::match)
                .build();

        Ability ability = Ability.fromRules(rules, options);

        Map<String, Object> publishedPost = new LinkedHashMap<>();
        publishedPost.put("__caslSubjectType__", "Post");
        publishedPost.put("published", true);

        Map<String, Object> draftPost = new LinkedHashMap<>();
        draftPost.put("__caslSubjectType__", "Post");
        draftPost.put("published", false);

        assertTrue(ability.can("read", publishedPost));
        assertFalse(ability.can("read", draftPost));
    }

    // ----------------------------------------------------------------
    // Ability.fromJson(String)
    // ----------------------------------------------------------------

    @Test
    public void testAbilityFromJsonString() throws JSONException {
        String json = "[{\"action\":\"read\",\"subject\":\"Post\"},{\"action\":\"delete\",\"subject\":\"Post\"}]";
        Ability ability = Ability.fromJson(json);

        assertTrue(ability.can("read", "Post"));
        assertTrue(ability.can("delete", "Post"));
        assertFalse(ability.can("update", "Post"));
    }

    // ----------------------------------------------------------------
    // Ability.fromJson(JSONArray)
    // ----------------------------------------------------------------

    @Test
    public void testAbilityFromJsonArray() throws JSONException {
        JSONArray rulesArray = new JSONArray();
        JSONObject rule = new JSONObject();
        rule.put("action", "manage");
        rule.put("subject", "all");
        rulesArray.put(rule);

        Ability ability = Ability.fromJson(rulesArray);

        assertTrue(ability.can("read", "Post"));
        assertTrue(ability.can("delete", "Comment"));
    }

    // ----------------------------------------------------------------
    // Full API pattern test (matches user's existing usage)
    // ----------------------------------------------------------------

    @Test
    public void testFullApiPattern() throws JSONException {
        String permsString = "[{\"action\":\"read\",\"subject\":\"Post\"},{\"action\":\"update\",\"subject\":\"Comment\"}]";

        List<RawRule> rules = RawRule.listFromJson(permsString);
        Ability ability = Ability.fromRules(rules);

        assertTrue(ability.can("read", "Post"));
        assertTrue(ability.can("update", "Comment"));
        assertFalse(ability.can("delete", "Post"));
    }

}
