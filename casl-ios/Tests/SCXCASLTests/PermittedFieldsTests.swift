import XCTest
@testable import SCXCASL

final class PermittedFieldsTests: XCTestCase {

    private func defaultOptions() -> AbilityOptions {
        return AbilityOptions(
            conditionsMatcher: ConditionsMatcher.match,
            fieldMatcher: FieldMatcher.match
        )
    }

    func testEmptyRulesReturnsEmpty() {
        let ability = Ability(rules: [], options: defaultOptions())
        let fields = permittedFieldsOf(ability, action: "read", subject: "Post") { _ in
            ["title", "body"]
        }
        XCTAssertTrue(fields.isEmpty)
    }

    func testNoFieldRulesReturnsDefaultFromCallback() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post")
        ], options: defaultOptions())

        let fields = permittedFieldsOf(ability, action: "read", subject: "Post") { _ in
            ["title", "body", "author"]
        }
        XCTAssertEqual(Set(fields), Set(["title", "body", "author"]))
    }

    func testUniqueFieldsAcrossRules() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", fields: .array(["title", "body"])),
            RawRule(action: "read", subject: "Post", fields: .array(["body", "author"]))
        ], options: defaultOptions())

        let fields = permittedFieldsOf(ability, action: "read", subject: "Post") { rule in
            rule.fields ?? []
        }
        XCTAssertEqual(Set(fields), Set(["title", "body", "author"]))
    }

    func testInvertedRulesRemoveFields() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", fields: .array(["title", "body", "secret"])),
            RawRule(action: "read", subject: "Post", fields: .array(["secret"]), inverted: true)
        ], options: defaultOptions())

        let fields = permittedFieldsOf(ability, action: "read", subject: "Post") { rule in
            rule.fields ?? []
        }
        XCTAssertTrue(fields.contains("title"))
        XCTAssertTrue(fields.contains("body"))
        XCTAssertFalse(fields.contains("secret"))
    }

    func testCustomFieldsFrom() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", fields: .array(["title"]))
        ], options: defaultOptions())

        let fields = permittedFieldsOf(ability, action: "read", subject: "Post") { _ in
            ["custom_title", "custom_body"]
        }
        XCTAssertEqual(Set(fields), Set(["custom_title", "custom_body"]))
    }

    // MARK: - AccessibleFields

    func testAccessibleFieldsOfType() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", fields: .array(["title", "body"]))
        ], options: defaultOptions())

        let allFields: [String: [String]] = ["Post": ["title", "body", "author", "secret"]]
        let accessible = AccessibleFields(ability: ability, action: "read") { type in
            allFields[type] ?? []
        }

        let fields = accessible.ofType("Post")
        XCTAssertEqual(Set(fields), Set(["title", "body"]))
    }

    func testAccessibleFieldsOfTypeNoFieldsRule() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post")
        ], options: defaultOptions())

        let allFields: [String: [String]] = ["Post": ["title", "body", "author"]]
        let accessible = AccessibleFields(ability: ability, action: "read") { type in
            allFields[type] ?? []
        }

        let fields = accessible.ofType("Post")
        XCTAssertEqual(Set(fields), Set(["title", "body", "author"]))
    }

    func testAccessibleFieldsOfInstance() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", fields: .array(["title", "body"]),
                    conditions: ["published": true])
        ], options: defaultOptions())

        let allFields: [String: [String]] = ["Post": ["title", "body", "author"]]
        let accessible = AccessibleFields(ability: ability, action: "read") { type in
            allFields[type] ?? []
        }

        let publishedPost: [String: Any] = ["__caslSubjectType__": "Post", "published": true]
        let draftPost: [String: Any] = ["__caslSubjectType__": "Post", "published": false]

        let fields1 = accessible.of(publishedPost)
        XCTAssertEqual(Set(fields1), Set(["title", "body"]))

        let fields2 = accessible.of(draftPost)
        XCTAssertTrue(fields2.isEmpty)
    }

    func testAccessibleFieldsWithInverted() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post"),
            RawRule(action: "read", subject: "Post", fields: .array(["secret"]), inverted: true)
        ], options: defaultOptions())

        let allFields: [String: [String]] = ["Post": ["title", "body", "secret"]]
        let accessible = AccessibleFields(ability: ability, action: "read") { type in
            allFields[type] ?? []
        }

        let fields = accessible.ofType("Post")
        XCTAssertTrue(fields.contains("title"))
        XCTAssertTrue(fields.contains("body"))
        XCTAssertFalse(fields.contains("secret"))
    }

    func testInstanceSpecificWithConditions() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", fields: .array(["title", "body"]),
                    conditions: ["published": true])
        ], options: defaultOptions())

        let publishedPost: [String: Any] = ["__caslSubjectType__": "Post", "published": true]
        let draftPost: [String: Any] = ["__caslSubjectType__": "Post", "published": false]

        let fields1 = permittedFieldsOf(ability, action: "read", subject: publishedPost) { rule in
            rule.fields ?? []
        }
        XCTAssertEqual(Set(fields1), Set(["title", "body"]))

        let fields2 = permittedFieldsOf(ability, action: "read", subject: draftPost) { rule in
            rule.fields ?? []
        }
        XCTAssertTrue(fields2.isEmpty)
    }
}
