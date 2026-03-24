import XCTest
@testable import SCXCASL

final class JSONEndToEndTests: XCTestCase {

    private func defaultOptions() -> AbilityOptions {
        return AbilityOptions(
            conditionsMatcher: ConditionsMatcher.match,
            fieldMatcher: FieldMatcher.match
        )
    }

    // MARK: - Scenario 1: Blog Platform RBAC

    func testBlogPlatformEditorRole() throws {
        // Simulate receiving JSON permissions from an API for an "editor" role
        let json = """
        [
            {"action": "read", "subject": "Post"},
            {"action": "read", "subject": "Comment"},
            {"action": ["create", "update"], "subject": "Post", "conditions": {"authorId": "user123"}},
            {"action": "delete", "subject": "Post", "conditions": {"authorId": "user123", "published": false}},
            {"action": ["create", "update", "delete"], "subject": "Comment", "conditions": {"authorId": "user123"}},
            {"action": "manage", "subject": "Tag"},
            {"action": "read", "subject": "User", "fields": ["name", "email", "avatar"]}
        ]
        """

        let ability = try Ability(json: json, options: defaultOptions())

        // Can read any Post (no conditions on read rule)
        XCTAssertTrue(ability.can("read", "Post"))

        // Can create Post with authorId=user123
        let ownPost = subject(fromDictionary: ["authorId": "user123"], type: "Post")
        XCTAssertTrue(ability.can("create", ownPost))

        // Cannot create Post with authorId=other
        let otherPost = subject(fromDictionary: ["authorId": "other"], type: "Post")
        XCTAssertFalse(ability.can("create", otherPost))

        // Can delete own unpublished post
        let ownUnpublished = subject(fromDictionary: ["authorId": "user123", "published": false], type: "Post")
        XCTAssertTrue(ability.can("delete", ownUnpublished))

        // Cannot delete own published post
        let ownPublished = subject(fromDictionary: ["authorId": "user123", "published": true], type: "Post")
        XCTAssertFalse(ability.can("delete", ownPublished))

        // Cannot delete others' post
        let othersPost = subject(fromDictionary: ["authorId": "other", "published": false], type: "Post")
        XCTAssertFalse(ability.can("delete", othersPost))

        // Can manage (create/read/update/delete) Tags
        XCTAssertTrue(ability.can("create", "Tag"))
        XCTAssertTrue(ability.can("read", "Tag"))
        XCTAssertTrue(ability.can("update", "Tag"))
        XCTAssertTrue(ability.can("delete", "Tag"))

        // Can read User name field
        XCTAssertTrue(ability.can("read", "User", field: "name"))

        // Cannot read User password field
        XCTAssertFalse(ability.can("read", "User", field: "password"))
    }

    // MARK: - Scenario 2: Multi-tenant SaaS

    func testMultiTenantSaaS() throws {
        // Simulate receiving JSON permissions from an API for a "team member"
        let json = """
        [
            {"action": "manage", "subject": "all", "conditions": {"tenantId": "tenant1"}},
            {"action": "manage", "subject": "Billing", "inverted": true},
            {"action": "read", "subject": "Billing", "conditions": {"tenantId": "tenant1"}},
            {"action": "read", "subject": "AuditLog"}
        ]
        """

        let ability = try Ability(json: json, options: defaultOptions())

        // Can read Post in tenant1
        let postTenant1 = subject(fromDictionary: ["tenantId": "tenant1", "title": "Hello"], type: "Post")
        XCTAssertTrue(ability.can("read", postTenant1))

        // Cannot read Post in tenant2
        let postTenant2 = subject(fromDictionary: ["tenantId": "tenant2", "title": "Hello"], type: "Post")
        XCTAssertFalse(ability.can("read", postTenant2))

        // Cannot create Billing (inverted manage)
        XCTAssertFalse(ability.can("create", "Billing"))

        // Can read Billing in tenant1
        let billingTenant1 = subject(fromDictionary: ["tenantId": "tenant1"], type: "Billing")
        XCTAssertTrue(ability.can("read", billingTenant1))

        // Cannot update Billing
        XCTAssertFalse(ability.can("update", "Billing"))

        // Can read AuditLog (no conditions)
        XCTAssertTrue(ability.can("read", "AuditLog"))
    }

    // MARK: - Scenario 3: E-commerce with Nested Conditions

    func testEcommerceNestedConditions() throws {
        // Simulate receiving JSON permissions from an API
        let json = """
        [
            {"action": "read", "subject": "Product"},
            {"action": "update", "subject": "Product", "conditions": {"seller.id": "seller1", "status": {"$in": ["draft", "pending"]}}},
            {"action": "read", "subject": "Order", "conditions": {"$or": [{"buyerId": "user1"}, {"seller.id": "seller1"}]}},
            {"action": "update", "subject": "Order", "conditions": {"seller.id": "seller1", "status": {"$ne": "completed"}}},
            {"action": "read", "subject": "Review", "conditions": {"rating": {"$gte": 1}}}
        ]
        """

        let ability = try Ability(json: json, options: defaultOptions())

        // Can read any Product
        XCTAssertTrue(ability.can("read", "Product"))

        // Can update own draft product (nested seller.id)
        let ownDraftProduct = subject(fromDictionary: [
            "seller": ["id": "seller1"],
            "status": "draft"
        ], type: "Product")
        XCTAssertTrue(ability.can("update", ownDraftProduct))

        // Cannot update others' product
        let othersProduct = subject(fromDictionary: [
            "seller": ["id": "seller2"],
            "status": "draft"
        ], type: "Product")
        XCTAssertFalse(ability.can("update", othersProduct))

        // Cannot update published product (status not in [draft, pending])
        let publishedProduct = subject(fromDictionary: [
            "seller": ["id": "seller1"],
            "status": "published"
        ], type: "Product")
        XCTAssertFalse(ability.can("update", publishedProduct))

        // Can read order as buyer ($or condition)
        let orderAsBuyer = subject(fromDictionary: [
            "buyerId": "user1",
            "seller": ["id": "seller2"]
        ], type: "Order")
        XCTAssertTrue(ability.can("read", orderAsBuyer))

        // Can read order as seller ($or condition)
        let orderAsSeller = subject(fromDictionary: [
            "buyerId": "user2",
            "seller": ["id": "seller1"]
        ], type: "Order")
        XCTAssertTrue(ability.can("read", orderAsSeller))

        // Cannot read order as neither buyer nor seller
        let orderAsNeither = subject(fromDictionary: [
            "buyerId": "user2",
            "seller": ["id": "seller2"]
        ], type: "Order")
        XCTAssertFalse(ability.can("read", orderAsNeither))

        // Can update pending order as seller
        let pendingOrder = subject(fromDictionary: [
            "seller": ["id": "seller1"],
            "status": "pending"
        ], type: "Order")
        XCTAssertTrue(ability.can("update", pendingOrder))

        // Cannot update completed order
        let completedOrder = subject(fromDictionary: [
            "seller": ["id": "seller1"],
            "status": "completed"
        ], type: "Order")
        XCTAssertFalse(ability.can("update", completedOrder))

        // Can read review with rating >= 1
        let review = subject(fromDictionary: ["rating": 3], type: "Review")
        XCTAssertTrue(ability.can("read", review))
    }

    // MARK: - Scenario 4: Dynamic Rule Updates from JSON

    func testDynamicRuleUpdates() throws {
        // Start with basic rules
        let initialJson = """
        [{"action": "read", "subject": "Post"}]
        """

        let ability = try Ability(json: initialJson, options: defaultOptions())

        // Verify initial state
        XCTAssertTrue(ability.can("read", "Post"))
        XCTAssertFalse(ability.can("create", "Post"))

        // Receive an updated JSON from API
        let updatedJson = """
        [{"action": "read", "subject": "Post"}, {"action": "create", "subject": "Post"}]
        """

        // Update the ability with new rules
        try ability.update(json: updatedJson)

        // Verify new permissions apply
        XCTAssertTrue(ability.can("read", "Post"))
        XCTAssertTrue(ability.can("create", "Post"))
    }

    // MARK: - Scenario 5: Pack/Unpack Round-trip from JSON

    func testPackUnpackRoundTrip() throws {
        // Start with the blog platform rules JSON
        let json = """
        [
            {"action": "read", "subject": "Post"},
            {"action": "read", "subject": "Comment"},
            {"action": ["create", "update"], "subject": "Post", "conditions": {"authorId": "user123"}},
            {"action": "delete", "subject": "Post", "conditions": {"authorId": "user123", "published": false}},
            {"action": ["create", "update", "delete"], "subject": "Comment", "conditions": {"authorId": "user123"}},
            {"action": "manage", "subject": "Tag"},
            {"action": "read", "subject": "User", "fields": ["name", "email", "avatar"]}
        ]
        """

        // Create ability from JSON
        let ability1 = try Ability(json: json, options: defaultOptions())

        // Export to packed JSON string
        let packedJson = try packRulesToJSON(ability1.rules)

        // Unpack from packed JSON string
        let unpackedRules = try unpackRulesFromJSON(packedJson)

        // Create new ability from unpacked rules
        let ability2 = Ability(rules: unpackedRules, options: defaultOptions())

        // Verify same permissions: can read any Post
        XCTAssertTrue(ability2.can("read", "Post"))

        // Can create Post with authorId=user123
        let ownPost = subject(fromDictionary: ["authorId": "user123"], type: "Post")
        XCTAssertTrue(ability2.can("create", ownPost))

        // Cannot create Post with authorId=other
        let otherPost = subject(fromDictionary: ["authorId": "other"], type: "Post")
        XCTAssertFalse(ability2.can("create", otherPost))

        // Can manage Tags
        XCTAssertTrue(ability2.can("create", "Tag"))
        XCTAssertTrue(ability2.can("read", "Tag"))
        XCTAssertTrue(ability2.can("update", "Tag"))
        XCTAssertTrue(ability2.can("delete", "Tag"))

        // Can read User name field
        XCTAssertTrue(ability2.can("read", "User", field: "name"))

        // Cannot read User password field
        XCTAssertFalse(ability2.can("read", "User", field: "password"))

        // Can delete own unpublished post
        let ownUnpublished = subject(fromDictionary: ["authorId": "user123", "published": false], type: "Post")
        XCTAssertTrue(ability2.can("delete", ownUnpublished))

        // Cannot delete own published post
        let ownPublished = subject(fromDictionary: ["authorId": "user123", "published": true], type: "Post")
        XCTAssertFalse(ability2.can("delete", ownPublished))
    }
}
