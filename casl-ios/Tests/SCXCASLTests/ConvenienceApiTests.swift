import XCTest
@testable import SCXCASL

final class ConvenienceApiTests: XCTestCase {

    private func defaultOptions() -> AbilityOptions {
        return AbilityOptions(
            conditionsMatcher: ConditionsMatcher.match,
            fieldMatcher: FieldMatcher.match
        )
    }

    // MARK: - RawRule.listFromJSON(String)

    func testListFromJSONString() throws {
        let json = """
        [
            {"action": "read", "subject": "Post"},
            {"action": "update", "subject": "Post"}
        ]
        """
        let rules = try RawRule.listFromJSON(json)

        XCTAssertEqual(rules.count, 2)
        XCTAssertEqual(rules[0].action, .single("read"))
        XCTAssertEqual(rules[0].subject, .single("Post"))
        XCTAssertEqual(rules[1].action, .single("update"))
    }

    // MARK: - RawRule.listFromJSON(data:)

    func testListFromJSONData() throws {
        let json = """
        [{"action": "read", "subject": "Post"}]
        """
        let data = json.data(using: .utf8)!
        let rules = try RawRule.listFromJSON(data: data)

        XCTAssertEqual(rules.count, 1)
        XCTAssertEqual(rules[0].action, .single("read"))
        XCTAssertEqual(rules[0].subject, .single("Post"))
    }

    func testListFromJSONInvalidStringThrows() {
        XCTAssertThrowsError(try RawRule.listFromJSON("not json")) { error in
            XCTAssertTrue(error is CASLJSONError)
        }
    }

    // MARK: - Ability.fromRules

    func testAbilityFromRules() {
        let rules = [
            RawRule(action: "read", subject: "Post"),
            RawRule(action: "update", subject: "Post")
        ]
        let ability = Ability.fromRules(rules)

        XCTAssertTrue(ability.can("read", "Post"))
        XCTAssertTrue(ability.can("update", "Post"))
        XCTAssertFalse(ability.can("delete", "Post"))
    }

    func testAbilityFromRulesWithOptions() {
        let rules = [
            RawRule(action: "read", subject: "Post", conditions: ["published": true])
        ]
        let ability = Ability.fromRules(rules, options: defaultOptions())

        let publishedPost: [String: Any] = ["__caslSubjectType__": "Post", "published": true]
        let draftPost: [String: Any] = ["__caslSubjectType__": "Post", "published": false]

        XCTAssertTrue(ability.can("read", publishedPost))
        XCTAssertFalse(ability.can("read", draftPost))
    }

    // MARK: - Ability.fromJSON

    func testAbilityFromJSON() throws {
        let json = """
        [{"action": "read", "subject": "Post"}, {"action": "delete", "subject": "Post"}]
        """
        let ability = try Ability.fromJSON(json)

        XCTAssertTrue(ability.can("read", "Post"))
        XCTAssertTrue(ability.can("delete", "Post"))
        XCTAssertFalse(ability.can("update", "Post"))
    }

    func testAbilityFromJSONWithOptions() throws {
        let json = """
        [{"action": "read", "subject": "Post", "conditions": {"published": true}}]
        """
        let ability = try Ability.fromJSON(json, options: defaultOptions())

        let publishedPost: [String: Any] = ["__caslSubjectType__": "Post", "published": true]
        let draftPost: [String: Any] = ["__caslSubjectType__": "Post", "published": false]

        XCTAssertTrue(ability.can("read", publishedPost))
        XCTAssertFalse(ability.can("read", draftPost))
    }

    func testAbilityFromJSONInvalidThrows() {
        XCTAssertThrowsError(try Ability.fromJSON("not json")) { error in
            XCTAssertTrue(error is CASLJSONError)
        }
    }

    // MARK: - Full API pattern (matches user's existing usage)

    func testFullApiPattern() throws {
        let permsString = """
        [{"action": "read", "subject": "Post"}, {"action": "update", "subject": "Comment"}]
        """

        let rules = try RawRule.listFromJSON(permsString)
        let ability = Ability.fromRules(rules)

        XCTAssertTrue(ability.can("read", "Post"))
        XCTAssertTrue(ability.can("update", "Comment"))
        XCTAssertFalse(ability.can("delete", "Post"))
    }

}
