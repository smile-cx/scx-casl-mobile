import XCTest
@testable import SCXCASL

final class RulesToFieldsTests: XCTestCase {

    private func defaultOptions() -> AbilityOptions {
        return AbilityOptions(
            conditionsMatcher: ConditionsMatcher.match,
            fieldMatcher: FieldMatcher.match
        )
    }

    func testEmptyAbilityReturnsEmpty() {
        let ability = Ability(rules: [], options: defaultOptions())
        let fields = rulesToFields(ability, action: "read", subjectType: "Post")
        XCTAssertTrue(fields.isEmpty)
    }

    func testInvertedRulesReturnEmpty() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", conditions: ["author": "john"], inverted: true)
        ], options: defaultOptions())

        let fields = rulesToFields(ability, action: "read", subjectType: "Post")
        XCTAssertTrue(fields.isEmpty)
    }

    func testNoConditionsReturnsEmpty() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post")
        ], options: defaultOptions())

        let fields = rulesToFields(ability, action: "read", subjectType: "Post")
        XCTAssertTrue(fields.isEmpty)
    }

    func testExtractsValues() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", conditions: ["author": "john", "published": true])
        ], options: defaultOptions())

        let fields = rulesToFields(ability, action: "read", subjectType: "Post")
        XCTAssertEqual(fields["author"] as? String, "john")
        // Note: booleans in [String: Any] are NSNumber
        XCTAssertEqual(fields["published"] as? Bool, true)
    }

    func testDotNotationCreatesNestedDict() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", conditions: ["author.name": "john"])
        ], options: defaultOptions())

        let fields = rulesToFields(ability, action: "read", subjectType: "Post")
        let author = fields["author"] as? [String: Any]
        XCTAssertNotNil(author)
        XCTAssertEqual(author?["name"] as? String, "john")
    }

    func testSkipsQueryExpressions() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", conditions: [
                "author": "john",
                "views": ["$gt": 100] as [String: Any]
            ])
        ], options: defaultOptions())

        let fields = rulesToFields(ability, action: "read", subjectType: "Post")
        XCTAssertEqual(fields["author"] as? String, "john")
        XCTAssertNil(fields["views"]) // Should be skipped because it's a dict
    }
}
