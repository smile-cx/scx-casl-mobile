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

    func testSkipsForbiddenProperties() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", conditions: [
                "__proto__.__pollutedValue__": 1,
                "constructor": 1,
                "prototype": 2
            ])
        ], options: defaultOptions())

        let fields = rulesToFields(ability, action: "read", subjectType: "Post")
        // Forbidden top-level keys and any path containing a forbidden segment are blocked
        XCTAssertNil(fields["__proto__"])
        XCTAssertNil(fields["constructor"])
        XCTAssertNil(fields["prototype"])
        XCTAssertNil(fields["__pollutedValue__"])
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
