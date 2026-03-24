import XCTest
@testable import SCXCASL

final class JSONTests: XCTestCase {

    private func defaultOptions() -> AbilityOptions {
        return AbilityOptions(
            conditionsMatcher: ConditionsMatcher.match,
            fieldMatcher: FieldMatcher.match
        )
    }

    // MARK: - Ability from JSON String

    func testCreateAbilityFromJSONString() throws {
        let json = """
        [
            {"action": "read", "subject": "Post"},
            {"action": "update", "subject": "Post", "conditions": {"author": "me"}}
        ]
        """
        let ability = try Ability(json: json, options: defaultOptions())

        XCTAssertTrue(ability.can("read", "Post"))
        XCTAssertTrue(ability.can("update", "Post"))
        XCTAssertFalse(ability.can("delete", "Post"))
    }

    func testCreateAbilityFromJSONStringWithArrayActions() throws {
        let json = """
        [{"action": ["read", "update"], "subject": "Post"}]
        """
        let ability = try Ability(json: json, options: defaultOptions())

        XCTAssertTrue(ability.can("read", "Post"))
        XCTAssertTrue(ability.can("update", "Post"))
        XCTAssertFalse(ability.can("delete", "Post"))
    }

    func testCreateAbilityFromJSONStringWithInvertedRule() throws {
        let json = """
        [
            {"action": "read", "subject": "Post"},
            {"action": "read", "subject": "Post", "conditions": {"private": true}, "inverted": true}
        ]
        """
        let ability = try Ability(json: json, options: defaultOptions())

        let publicPost: [String: Any] = ["__caslSubjectType__": "Post", "private": false]
        let privatePost: [String: Any] = ["__caslSubjectType__": "Post", "private": true]

        XCTAssertTrue(ability.can("read", publicPost))
        XCTAssertFalse(ability.can("read", privatePost))
    }

    func testCreateAbilityFromJSONStringWithFields() throws {
        let json = """
        [{"action": "read", "subject": "Post", "fields": ["title", "body"]}]
        """
        let ability = try Ability(json: json, options: defaultOptions())

        XCTAssertTrue(ability.can("read", "Post", field: "title"))
        XCTAssertTrue(ability.can("read", "Post", field: "body"))
        XCTAssertFalse(ability.can("read", "Post", field: "secret"))
    }

    func testCreateAbilityFromJSONStringWithReason() throws {
        let json = """
        [{"action": "delete", "subject": "Post", "inverted": true, "reason": "Not allowed"}]
        """
        let ability = try Ability(json: json, options: defaultOptions())

        XCTAssertTrue(ability.cannot("delete", "Post"))
        XCTAssertEqual(ability.rules[0].reason, "Not allowed")
    }

    // MARK: - Ability from JSON Data

    func testCreateAbilityFromJSONData() throws {
        let json = """
        [{"action": "read", "subject": "Post"}]
        """
        let data = json.data(using: .utf8)!
        let ability = try Ability(jsonData: data, options: defaultOptions())

        XCTAssertTrue(ability.can("read", "Post"))
    }

    // MARK: - Ability from [[String: Any]]

    func testCreateAbilityFromRulesArray() throws {
        let rulesArray: [[String: Any]] = [
            ["action": "read", "subject": "Post"],
            ["action": "update", "subject": "Post", "conditions": ["author": "me"]]
        ]
        let ability = try Ability(rulesArray: rulesArray, options: defaultOptions())

        XCTAssertTrue(ability.can("read", "Post"))
        XCTAssertTrue(ability.can("update", "Post"))
        XCTAssertFalse(ability.can("delete", "Post"))
    }

    func testCreateAbilityFromRulesArrayWithConditions() throws {
        let rulesArray: [[String: Any]] = [
            ["action": "read", "subject": "Post", "conditions": ["author": "john"]]
        ]
        let ability = try Ability(rulesArray: rulesArray, options: defaultOptions())

        let post1: [String: Any] = ["__caslSubjectType__": "Post", "author": "john"]
        let post2: [String: Any] = ["__caslSubjectType__": "Post", "author": "jane"]

        XCTAssertTrue(ability.can("read", post1))
        XCTAssertFalse(ability.can("read", post2))
    }

    // MARK: - Update from JSON

    func testUpdateFromJSONString() throws {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post")
        ], options: defaultOptions())

        XCTAssertTrue(ability.can("read", "Post"))
        XCTAssertFalse(ability.can("update", "Post"))

        let json = """
        [{"action": "update", "subject": "Post"}]
        """
        try ability.update(json: json)

        XCTAssertFalse(ability.can("read", "Post"))
        XCTAssertTrue(ability.can("update", "Post"))
    }

    func testUpdateFromJSONData() throws {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post")
        ], options: defaultOptions())

        let json = """
        [{"action": "update", "subject": "Post"}]
        """
        try ability.update(jsonData: json.data(using: .utf8)!)

        XCTAssertFalse(ability.can("read", "Post"))
        XCTAssertTrue(ability.can("update", "Post"))
    }

    func testUpdateFromRulesArray() throws {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post")
        ], options: defaultOptions())

        try ability.update(rulesArray: [
            ["action": "update", "subject": "Post"]
        ])

        XCTAssertFalse(ability.can("read", "Post"))
        XCTAssertTrue(ability.can("update", "Post"))
    }

    // MARK: - can() with Dictionary Subject from JSON

    func testCanWithDictionarySubjectFromJSON() throws {
        let json = """
        [{"action": "read", "subject": "Post", "conditions": {"author": "me"}}]
        """
        let ability = try Ability(json: json, options: defaultOptions())

        let subjectJSON = """
        {"author": "me", "title": "Hello"}
        """
        let post = try subject(fromJSON: subjectJSON, type: "Post")

        XCTAssertTrue(ability.can("read", post))
    }

    func testCanWithDictionarySubjectFromDictionary() throws {
        let json = """
        [{"action": "read", "subject": "Post", "conditions": {"status": "published"}}]
        """
        let ability = try Ability(json: json, options: defaultOptions())

        let post = subject(fromDictionary: ["status": "published", "title": "Hi"], type: "Post")

        XCTAssertTrue(ability.can("read", post))
        XCTAssertEqual(post["__caslSubjectType__"] as? String, "Post")
    }

    func testSubjectFromJSONInvalidJSON() {
        XCTAssertThrowsError(try subject(fromJSON: "not json", type: "Post")) { error in
            XCTAssertTrue(error is CASLJSONError)
        }
    }

    func testSubjectFromJSONNotDictionary() {
        XCTAssertThrowsError(try subject(fromJSON: "[1,2,3]", type: "Post")) { error in
            guard let jsonError = error as? CASLJSONError else {
                XCTFail("Expected CASLJSONError")
                return
            }
            if case .invalidJSON(let detail) = jsonError {
                XCTAssertTrue(detail.contains("dictionary"))
            } else {
                XCTFail("Expected invalidJSON error")
            }
        }
    }

    // MARK: - rulesToJSON Round-trip

    func testRulesToJSONRoundTrip() throws {
        let originalRules = [
            RawRule(action: "read", subject: "Post"),
            RawRule(action: "update", subject: "Post", conditions: ["author": "me"]),
            RawRule(action: "delete", subject: "Post", inverted: true, reason: "Forbidden")
        ]
        let ability = Ability(rules: originalRules, options: defaultOptions())

        let jsonString = try ability.rulesToJSON()

        // Round-trip: create a new Ability from the exported JSON
        let ability2 = try Ability(json: jsonString, options: defaultOptions())

        XCTAssertTrue(ability2.can("read", "Post"))
        XCTAssertTrue(ability2.can("update", "Post"))
        XCTAssertTrue(ability2.cannot("delete", "Post"))
        XCTAssertEqual(ability2.rules.count, originalRules.count)
    }

    func testRulesToJSONData() throws {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post")
        ], options: defaultOptions())

        let data = try ability.rulesToJSONData()
        let ability2 = try Ability(jsonData: data, options: defaultOptions())

        XCTAssertTrue(ability2.can("read", "Post"))
    }

    func testRulesToJSONWithConditionsRoundTrip() throws {
        let rules = [
            RawRule(action: "read", subject: "Post", conditions: ["status": ["$in": ["published", "draft"]]])
        ]
        let ability = Ability(rules: rules, options: defaultOptions())

        let jsonString = try ability.rulesToJSON()
        let ability2 = try Ability(json: jsonString, options: defaultOptions())

        let pub: [String: Any] = ["__caslSubjectType__": "Post", "status": "published"]
        let archived: [String: Any] = ["__caslSubjectType__": "Post", "status": "archived"]

        XCTAssertTrue(ability2.can("read", pub))
        XCTAssertFalse(ability2.can("read", archived))
    }

    // MARK: - PackRules / UnpackRules JSON Round-trip

    func testPackUnpackRulesJSONRoundTrip() throws {
        let rules = [
            RawRule(action: "read", subject: "Post"),
            RawRule(action: "update", subject: "Post", conditions: ["author": "me"]),
            RawRule(action: "delete", subject: "Post", fields: "id", inverted: true, reason: "Nope")
        ]

        let packed = try packRulesToJSON(rules)
        let unpacked = try unpackRulesFromJSON(packed)

        XCTAssertEqual(unpacked.count, rules.count)
        XCTAssertEqual(unpacked[0].action, .single("read"))
        XCTAssertEqual(unpacked[0].subject, .single("Post"))
        XCTAssertEqual(unpacked[1].action, .single("update"))
        XCTAssertEqual(unpacked[2].inverted, true)
        XCTAssertEqual(unpacked[2].reason, "Nope")
        XCTAssertEqual(unpacked[2].fields, .single("id"))
    }

    func testPackRulesToJSONProducesValidJSON() throws {
        let rules = [
            RawRule(action: "read", subject: "Post")
        ]
        let jsonString = try packRulesToJSON(rules)

        // Verify it's valid JSON
        let data = jsonString.data(using: .utf8)!
        let parsed = try JSONSerialization.jsonObject(with: data)
        XCTAssertTrue(parsed is [[Any]])
    }

    // MARK: - Invalid JSON Handling

    func testInvalidJSONStringThrows() {
        XCTAssertThrowsError(try Ability(json: "not json", options: defaultOptions())) { error in
            XCTAssertTrue(error is CASLJSONError)
        }
    }

    func testInvalidJSONDataThrows() {
        let data = "not json".data(using: .utf8)!
        XCTAssertThrowsError(try Ability(jsonData: data, options: defaultOptions())) { error in
            XCTAssertTrue(error is CASLJSONError)
        }
    }

    func testJSONObjectInsteadOfArrayThrows() {
        let json = """
        {"action": "read", "subject": "Post"}
        """
        XCTAssertThrowsError(try Ability(json: json, options: defaultOptions())) { error in
            XCTAssertTrue(error is CASLJSONError)
        }
    }

    func testUpdateInvalidJSONThrows() {
        let ability = Ability(rules: [], options: defaultOptions())
        XCTAssertThrowsError(try ability.update(json: "bad json")) { error in
            XCTAssertTrue(error is CASLJSONError)
        }
    }

    func testUnpackRulesFromInvalidJSONThrows() {
        XCTAssertThrowsError(try unpackRulesFromJSON("not json")) { error in
            XCTAssertTrue(error is CASLJSONError)
        }
    }

    func testUnpackRulesFromNonArrayJSONThrows() {
        XCTAssertThrowsError(try unpackRulesFromJSON("{}")) { error in
            XCTAssertTrue(error is CASLJSONError)
        }
    }

    // MARK: - RawRule from Dictionary

    func testRawRuleFromDictionary() throws {
        let dict: [String: Any] = [
            "action": "read",
            "subject": "Post",
            "conditions": ["author": "me"],
            "inverted": false,
            "reason": "Because"
        ]
        let rule = try RawRule(dictionary: dict)

        XCTAssertEqual(rule.action, .single("read"))
        XCTAssertEqual(rule.subject, .single("Post"))
        XCTAssertFalse(rule.inverted)
        XCTAssertEqual(rule.reason, "Because")
        XCTAssertNotNil(rule.conditions)
    }

    func testRawRuleFromDictionaryWithArrays() throws {
        let dict: [String: Any] = [
            "action": ["read", "update"],
            "subject": ["Post", "Comment"],
            "fields": ["title", "body"]
        ]
        let rule = try RawRule(dictionary: dict)

        XCTAssertEqual(rule.action, .array(["read", "update"]))
        XCTAssertEqual(rule.subject, .array(["Post", "Comment"]))
        XCTAssertEqual(rule.fields, .array(["title", "body"]))
    }

    func testRawRuleFromDictionaryMissingAction() {
        let dict: [String: Any] = ["subject": "Post"]
        XCTAssertThrowsError(try RawRule(dictionary: dict)) { error in
            guard let jsonError = error as? CASLJSONError else {
                XCTFail("Expected CASLJSONError")
                return
            }
            if case .missingRequiredField(let field) = jsonError {
                XCTAssertEqual(field, "action")
            } else {
                XCTFail("Expected missingRequiredField error")
            }
        }
    }

    func testRawRuleFromDictionaryInvalidActionType() {
        let dict: [String: Any] = ["action": 42]
        XCTAssertThrowsError(try RawRule(dictionary: dict)) { error in
            XCTAssertTrue(error is CASLJSONError)
        }
    }

    func testRawRuleFromDictionaryMinimal() throws {
        let dict: [String: Any] = ["action": "read"]
        let rule = try RawRule(dictionary: dict)

        XCTAssertEqual(rule.action, .single("read"))
        XCTAssertNil(rule.subject)
        XCTAssertNil(rule.fields)
        XCTAssertNil(rule.conditions)
        XCTAssertFalse(rule.inverted)
        XCTAssertNil(rule.reason)
    }

    func testRawRuleFromDictionaryInvertedAsInt() throws {
        let dict: [String: Any] = ["action": "read", "inverted": 1]
        let rule = try RawRule(dictionary: dict)
        XCTAssertTrue(rule.inverted)

        let dict2: [String: Any] = ["action": "read", "inverted": 0]
        let rule2 = try RawRule(dictionary: dict2)
        XCTAssertFalse(rule2.inverted)
    }
}
