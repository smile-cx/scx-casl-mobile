package cx.smile.CASL;

import org.json.JSONException;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class JsonEndToEndTest {

    private static Map<String, Object> makeSubject(String type, Object... keyValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("__caslSubjectType__", type);
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put((String) keyValues[i], keyValues[i + 1]);
        }
        return map;
    }

    // ----------------------------------------------------------------
    // Scenario 1: Blog Platform RBAC
    // ----------------------------------------------------------------

    @Test
    public void testBlogPlatformEditorRole() throws JSONException {
        // Simulate receiving JSON permissions from an API for an "editor" role
        String json = "["
                + "{\"action\": \"read\", \"subject\": \"Post\"},"
                + "{\"action\": \"read\", \"subject\": \"Comment\"},"
                + "{\"action\": [\"create\", \"update\"], \"subject\": \"Post\", \"conditions\": {\"authorId\": \"user123\"}},"
                + "{\"action\": \"delete\", \"subject\": \"Post\", \"conditions\": {\"authorId\": \"user123\", \"published\": false}},"
                + "{\"action\": [\"create\", \"update\", \"delete\"], \"subject\": \"Comment\", \"conditions\": {\"authorId\": \"user123\"}},"
                + "{\"action\": \"manage\", \"subject\": \"Tag\"},"
                + "{\"action\": \"read\", \"subject\": \"User\", \"fields\": [\"name\", \"email\", \"avatar\"]}"
                + "]";

        Ability ability = JsonSupport.createAbility(json);

        // Can read any Post (no conditions on read rule)
        assertTrue(ability.can("read", "Post"));

        // Can create Post with authorId=user123
        Map<String, Object> ownPost = makeSubject("Post", "authorId", "user123");
        assertTrue(ability.can("create", ownPost));

        // Cannot create Post with authorId=other
        Map<String, Object> otherPost = makeSubject("Post", "authorId", "other");
        assertFalse(ability.can("create", otherPost));

        // Can delete own unpublished post
        Map<String, Object> ownUnpublished = makeSubject("Post", "authorId", "user123", "published", false);
        assertTrue(ability.can("delete", ownUnpublished));

        // Cannot delete own published post
        Map<String, Object> ownPublished = makeSubject("Post", "authorId", "user123", "published", true);
        assertFalse(ability.can("delete", ownPublished));

        // Cannot delete others' post
        Map<String, Object> othersPost = makeSubject("Post", "authorId", "other", "published", false);
        assertFalse(ability.can("delete", othersPost));

        // Can manage (create/read/update/delete) Tags
        assertTrue(ability.can("create", "Tag"));
        assertTrue(ability.can("read", "Tag"));
        assertTrue(ability.can("update", "Tag"));
        assertTrue(ability.can("delete", "Tag"));

        // Can read User name field
        assertTrue(ability.can("read", "User", "name"));

        // Cannot read User password field
        assertFalse(ability.can("read", "User", "password"));
    }

    // ----------------------------------------------------------------
    // Scenario 2: Multi-tenant SaaS
    // ----------------------------------------------------------------

    @Test
    public void testMultiTenantSaaS() throws JSONException {
        // Simulate receiving JSON permissions from an API for a "team member"
        String json = "["
                + "{\"action\": \"manage\", \"subject\": \"all\", \"conditions\": {\"tenantId\": \"tenant1\"}},"
                + "{\"action\": \"manage\", \"subject\": \"Billing\", \"inverted\": true},"
                + "{\"action\": \"read\", \"subject\": \"Billing\", \"conditions\": {\"tenantId\": \"tenant1\"}},"
                + "{\"action\": \"read\", \"subject\": \"AuditLog\"}"
                + "]";

        Ability ability = JsonSupport.createAbility(json);

        // Can read Post in tenant1
        Map<String, Object> postTenant1 = makeSubject("Post", "tenantId", "tenant1", "title", "Hello");
        assertTrue(ability.can("read", postTenant1));

        // Cannot read Post in tenant2
        Map<String, Object> postTenant2 = makeSubject("Post", "tenantId", "tenant2", "title", "Hello");
        assertFalse(ability.can("read", postTenant2));

        // Cannot create Billing (inverted manage)
        assertFalse(ability.can("create", "Billing"));

        // Can read Billing in tenant1
        Map<String, Object> billingTenant1 = makeSubject("Billing", "tenantId", "tenant1");
        assertTrue(ability.can("read", billingTenant1));

        // Cannot update Billing
        assertFalse(ability.can("update", "Billing"));

        // Can read AuditLog (no conditions)
        assertTrue(ability.can("read", "AuditLog"));
    }

    // ----------------------------------------------------------------
    // Scenario 3: E-commerce with Nested Conditions
    // ----------------------------------------------------------------

    @Test
    public void testEcommerceNestedConditions() throws JSONException {
        // Simulate receiving JSON permissions from an API
        String json = "["
                + "{\"action\": \"read\", \"subject\": \"Product\"},"
                + "{\"action\": \"update\", \"subject\": \"Product\", \"conditions\": {\"seller.id\": \"seller1\", \"status\": {\"$in\": [\"draft\", \"pending\"]}}},"
                + "{\"action\": \"read\", \"subject\": \"Order\", \"conditions\": {\"$or\": [{\"buyerId\": \"user1\"}, {\"seller.id\": \"seller1\"}]}},"
                + "{\"action\": \"update\", \"subject\": \"Order\", \"conditions\": {\"seller.id\": \"seller1\", \"status\": {\"$ne\": \"completed\"}}},"
                + "{\"action\": \"read\", \"subject\": \"Review\", \"conditions\": {\"rating\": {\"$gte\": 1}}}"
                + "]";

        Ability ability = JsonSupport.createAbility(json);

        // Can read any Product
        assertTrue(ability.can("read", "Product"));

        // Can update own draft product (nested seller.id)
        Map<String, Object> sellerMap1 = new LinkedHashMap<>();
        sellerMap1.put("id", "seller1");
        Map<String, Object> ownDraftProduct = makeSubject("Product", "seller", sellerMap1, "status", "draft");
        assertTrue(ability.can("update", ownDraftProduct));

        // Cannot update others' product
        Map<String, Object> sellerMap2 = new LinkedHashMap<>();
        sellerMap2.put("id", "seller2");
        Map<String, Object> othersProduct = makeSubject("Product", "seller", sellerMap2, "status", "draft");
        assertFalse(ability.can("update", othersProduct));

        // Cannot update published product (status not in [draft, pending])
        Map<String, Object> sellerMap3 = new LinkedHashMap<>();
        sellerMap3.put("id", "seller1");
        Map<String, Object> publishedProduct = makeSubject("Product", "seller", sellerMap3, "status", "published");
        assertFalse(ability.can("update", publishedProduct));

        // Can read order as buyer ($or condition)
        Map<String, Object> sellerMapB = new LinkedHashMap<>();
        sellerMapB.put("id", "seller2");
        Map<String, Object> orderAsBuyer = makeSubject("Order", "buyerId", "user1", "seller", sellerMapB);
        assertTrue(ability.can("read", orderAsBuyer));

        // Can read order as seller ($or condition)
        Map<String, Object> sellerMapS = new LinkedHashMap<>();
        sellerMapS.put("id", "seller1");
        Map<String, Object> orderAsSeller = makeSubject("Order", "buyerId", "user2", "seller", sellerMapS);
        assertTrue(ability.can("read", orderAsSeller));

        // Cannot read order as neither buyer nor seller
        Map<String, Object> sellerMapN = new LinkedHashMap<>();
        sellerMapN.put("id", "seller2");
        Map<String, Object> orderAsNeither = makeSubject("Order", "buyerId", "user2", "seller", sellerMapN);
        assertFalse(ability.can("read", orderAsNeither));

        // Can update pending order as seller
        Map<String, Object> sellerMapP = new LinkedHashMap<>();
        sellerMapP.put("id", "seller1");
        Map<String, Object> pendingOrder = makeSubject("Order", "seller", sellerMapP, "status", "pending");
        assertTrue(ability.can("update", pendingOrder));

        // Cannot update completed order
        Map<String, Object> sellerMapC = new LinkedHashMap<>();
        sellerMapC.put("id", "seller1");
        Map<String, Object> completedOrder = makeSubject("Order", "seller", sellerMapC, "status", "completed");
        assertFalse(ability.can("update", completedOrder));

        // Can read review with rating >= 1
        Map<String, Object> review = makeSubject("Review", "rating", 3);
        assertTrue(ability.can("read", review));
    }

    // ----------------------------------------------------------------
    // Scenario 4: Dynamic Rule Updates from JSON
    // ----------------------------------------------------------------

    @Test
    public void testDynamicRuleUpdates() throws JSONException {
        // Start with basic rules
        String initialJson = "[{\"action\": \"read\", \"subject\": \"Post\"}]";

        Ability ability = JsonSupport.createAbility(initialJson);

        // Verify initial state
        assertTrue(ability.can("read", "Post"));
        assertFalse(ability.can("create", "Post"));

        // Receive an updated JSON from API
        String updatedJson = "[{\"action\": \"read\", \"subject\": \"Post\"}, {\"action\": \"create\", \"subject\": \"Post\"}]";

        // Update the ability with new rules
        JsonSupport.update(ability, updatedJson);

        // Verify new permissions apply
        assertTrue(ability.can("read", "Post"));
        assertTrue(ability.can("create", "Post"));
    }

    // ----------------------------------------------------------------
    // Scenario 5: Pack/Unpack Round-trip from JSON
    // ----------------------------------------------------------------

    @Test
    public void testPackUnpackRoundTrip() throws JSONException {
        // Start with the blog platform rules JSON
        String json = "["
                + "{\"action\": \"read\", \"subject\": \"Post\"},"
                + "{\"action\": \"read\", \"subject\": \"Comment\"},"
                + "{\"action\": [\"create\", \"update\"], \"subject\": \"Post\", \"conditions\": {\"authorId\": \"user123\"}},"
                + "{\"action\": \"delete\", \"subject\": \"Post\", \"conditions\": {\"authorId\": \"user123\", \"published\": false}},"
                + "{\"action\": [\"create\", \"update\", \"delete\"], \"subject\": \"Comment\", \"conditions\": {\"authorId\": \"user123\"}},"
                + "{\"action\": \"manage\", \"subject\": \"Tag\"},"
                + "{\"action\": \"read\", \"subject\": \"User\", \"fields\": [\"name\", \"email\", \"avatar\"]}"
                + "]";

        // Create ability from JSON
        Ability ability1 = JsonSupport.createAbility(json);

        // Export to packed JSON string
        String packedJson = JsonSupport.packRulesToJSON(ability1.getRules());

        // Unpack from packed JSON string
        List<RawRule> unpackedRules = JsonSupport.unpackRulesFromJSON(packedJson);

        // Create new ability from unpacked rules
        AbilityOptions options = AbilityOptions.builder()
                .conditionsMatcher(ConditionsMatcher::match)
                .fieldMatcher(FieldMatcher::match)
                .build();
        Ability ability2 = new Ability(unpackedRules, options);

        // Verify same permissions: can read any Post
        assertTrue(ability2.can("read", "Post"));

        // Can create Post with authorId=user123
        Map<String, Object> ownPost = makeSubject("Post", "authorId", "user123");
        assertTrue(ability2.can("create", ownPost));

        // Cannot create Post with authorId=other
        Map<String, Object> otherPost = makeSubject("Post", "authorId", "other");
        assertFalse(ability2.can("create", otherPost));

        // Can manage Tags
        assertTrue(ability2.can("create", "Tag"));
        assertTrue(ability2.can("read", "Tag"));
        assertTrue(ability2.can("update", "Tag"));
        assertTrue(ability2.can("delete", "Tag"));

        // Can read User name field
        assertTrue(ability2.can("read", "User", "name"));

        // Cannot read User password field
        assertFalse(ability2.can("read", "User", "password"));

        // Can delete own unpublished post
        Map<String, Object> ownUnpublished = makeSubject("Post", "authorId", "user123", "published", false);
        assertTrue(ability2.can("delete", ownUnpublished));

        // Cannot delete own published post
        Map<String, Object> ownPublished = makeSubject("Post", "authorId", "user123", "published", true);
        assertFalse(ability2.can("delete", ownPublished));
    }
}
