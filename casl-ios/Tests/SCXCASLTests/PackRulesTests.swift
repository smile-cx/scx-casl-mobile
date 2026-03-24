import XCTest
@testable import SCXCASL

final class PackRulesTests: XCTestCase {

    // MARK: - packRules

    func testPackRulesConvertsToArrays() {
        let rules = [
            RawRule(action: "read", subject: "Post")
        ]
        let packed = packRules(rules)
        XCTAssertEqual(packed.count, 1)
        XCTAssertEqual(packed[0][0] as? String, "read")
        XCTAssertEqual(packed[0][1] as? String, "Post")
    }

    func testPackRulesJoinsActions() {
        let rules = [
            RawRule(action: .array(["read", "update"]), subject: "Post")
        ]
        let packed = packRules(rules)
        XCTAssertEqual(packed[0][0] as? String, "read,update")
    }

    func testPackRulesJoinsSubjects() {
        let rules = [
            RawRule(action: "read", subject: .array(["Post", "Comment"]))
        ]
        let packed = packRules(rules)
        XCTAssertEqual(packed[0][1] as? String, "Post,Comment")
    }

    func testPackRulesWithConditions() {
        let rules = [
            RawRule(action: "read", subject: "Post", conditions: ["author": "john"])
        ]
        let packed = packRules(rules)
        XCTAssertEqual(packed[0].count, 3)
        let conditions = packed[0][2] as? [String: Any]
        XCTAssertNotNil(conditions)
        XCTAssertEqual(conditions?["author"] as? String, "john")
    }

    func testPackRulesWithInverted() {
        let rules = [
            RawRule(action: "delete", subject: "Post", inverted: true)
        ]
        let packed = packRules(rules)
        // [action, subject, 0(conditions), 1(inverted)]
        XCTAssertEqual(packed[0].count, 4)
        XCTAssertEqual(packed[0][3] as? Int, 1)
    }

    func testPackRulesWithFields() {
        let rules = [
            RawRule(action: "read", subject: "Post", fields: .array(["title", "body"]))
        ]
        let packed = packRules(rules)
        // [action, subject, 0(conditions), 0(inverted), "title,body"]
        XCTAssertEqual(packed[0].count, 5)
        XCTAssertEqual(packed[0][4] as? String, "title,body")
    }

    func testPackRulesWithReason() {
        let rules = [
            RawRule(action: "delete", subject: "Post", inverted: true, reason: "Not allowed")
        ]
        let packed = packRules(rules)
        XCTAssertEqual(packed[0].count, 6)
        XCTAssertEqual(packed[0][5] as? String, "Not allowed")
    }

    // JS: "puts 0 in place of fields when reason is provided and fields are not"
    func testPackRulesPutsZeroForFieldsWhenReasonProvidedAndFieldsAreNot() {
        let rules = [
            RawRule(action: "read", subject: "Post", reason: "forbidden reason")
        ]
        let packed = packRules(rules)
        // [action, subject, 0(conditions), 0(inverted), 0(fields), reason]
        XCTAssertEqual(packed[0].count, 6)
        XCTAssertEqual(packed[0][4] as? Int, 0) // fields placeholder
        XCTAssertEqual(packed[0][5] as? String, "forbidden reason")
    }

    func testPackRulesTrailingTrimming() {
        let rules = [
            RawRule(action: "read", subject: "Post")
        ]
        let packed = packRules(rules)
        // Should only have [action, subject] since trailing values are falsy
        XCTAssertEqual(packed[0].count, 2)
    }

    func testPackRulesWithPackSubject() {
        let rules = [
            RawRule(action: "read", subject: "Post")
        ]
        let packed = packRules(rules, packSubject: { $0.lowercased() })
        XCTAssertEqual(packed[0][1] as? String, "post")
    }

    // MARK: - unpackRules

    func testUnpackRulesConvertsBack() {
        let packed: [[Any]] = [["read", "Post"]]
        let rules = unpackRules(packed)
        XCTAssertEqual(rules.count, 1)
        XCTAssertEqual(rules[0].action, .single("read"))
        XCTAssertEqual(rules[0].subject, .single("Post"))
    }

    func testUnpackRulesSplitsActions() {
        let packed: [[Any]] = [["read,update", "Post"]]
        let rules = unpackRules(packed)
        XCTAssertEqual(rules[0].action, .array(["read", "update"]))
    }

    func testUnpackRulesSplitsSubjects() {
        let packed: [[Any]] = [["read", "Post,Comment"]]
        let rules = unpackRules(packed)
        XCTAssertEqual(rules[0].subject, .array(["Post", "Comment"]))
    }

    func testUnpackRulesWithConditions() {
        let packed: [[Any]] = [["read", "Post", ["author": "john"] as [String: Any]]]
        let rules = unpackRules(packed)
        XCTAssertNotNil(rules[0].conditions)
        XCTAssertEqual(rules[0].conditions?["author"] as? String, "john")
    }

    func testUnpackRulesWithInverted() {
        let packed: [[Any]] = [["delete", "Post", 0, 1]]
        let rules = unpackRules(packed)
        XCTAssertTrue(rules[0].inverted)
    }

    func testUnpackRulesInvertedFalse() {
        let packed: [[Any]] = [["read", "Post", 0, 0]]
        let rules = unpackRules(packed)
        XCTAssertFalse(rules[0].inverted)
    }

    func testUnpackRulesWithFields() {
        let packed: [[Any]] = [["read", "Post", 0, 0, "title,body"]]
        let rules = unpackRules(packed)
        XCTAssertEqual(rules[0].fields, .array(["title", "body"]))
    }

    func testUnpackRulesWithReason() {
        let packed: [[Any]] = [["delete", "Post", 0, 1, 0, "Not allowed"]]
        let rules = unpackRules(packed)
        XCTAssertEqual(rules[0].reason, "Not allowed")
    }

    func testUnpackRulesHandlesZeroValues() {
        let packed: [[Any]] = [["read", "Post", 0, 0, 0, 0]]
        let rules = unpackRules(packed)
        XCTAssertNil(rules[0].conditions)
        XCTAssertFalse(rules[0].inverted)
        XCTAssertNil(rules[0].fields)
        XCTAssertNil(rules[0].reason)
    }

    func testUnpackRulesWithUnpackSubject() {
        let packed: [[Any]] = [["read", "post"]]
        let rules = unpackRules(packed, unpackSubject: { $0.capitalized })
        XCTAssertEqual(rules[0].subject, .single("Post"))
    }

    // MARK: - __caslSubjectType__ filtering

    func testPackConditionsAreZeroWhenFieldsWithNoConditions() {
        // When fields are defined but no conditions, conditions slot should be 0
        let rules = [
            RawRule(action: "read", subject: "Post", fields: "title")
        ]
        let packed = packRules(rules)
        // [action, subject, 0(conditions), 0(inverted), "title"]
        XCTAssertEqual(packed[0][2] as? Int, 0)
        XCTAssertEqual(packed[0].count, 5)
    }

    func testPackPreservesNonCaslConditions() {
        // Conditions without __caslSubjectType__ should be preserved as-is
        let rules = [
            RawRule(action: "read", subject: "Post", conditions: ["status": "active"])
        ]
        let packed = packRules(rules)
        let conditions = packed[0][2] as? [String: Any]
        XCTAssertNotNil(conditions)
        XCTAssertEqual(conditions?["status"] as? String, "active")
    }

    func testUnpackEmptyFieldsToNil() {
        // An empty string for fields should unpack as nil
        let packed: [[Any]] = [["read", "Post", 0, 0, ""]]
        let rules = unpackRules(packed)
        XCTAssertNil(rules[0].fields)
    }

    func testPackRulesFiltersCaslSubjectTypeFromConditions() {
        let rules = [
            RawRule(action: "read", subject: "Post", conditions: [
                "__caslSubjectType__": "Post",
                "author": "john"
            ])
        ]
        let packed = packRules(rules)
        let conditions = packed[0][2] as? [String: Any]
        XCTAssertNotNil(conditions)
        XCTAssertNil(conditions?["__caslSubjectType__"])
        XCTAssertEqual(conditions?["author"] as? String, "john")
    }

    func testPackRulesConditionsOnlyCaslSubjectTypeBecomesZero() {
        // If the only condition key is __caslSubjectType__, conditions should be 0 (empty)
        let rules = [
            RawRule(action: "read", subject: "Post", conditions: [
                "__caslSubjectType__": "Post"
            ])
        ]
        let packed = packRules(rules)
        // Should be trimmed since conditions becomes 0
        XCTAssertEqual(packed[0].count, 2) // just [action, subject]
    }

    // MARK: - Round-trip

    func testRoundTrip() {
        let originalRules = [
            RawRule(action: .array(["read", "update"]), subject: .array(["Post", "Comment"]),
                    fields: .array(["title", "body"]), conditions: ["author": "john"],
                    inverted: false, reason: nil),
            RawRule(action: "delete", subject: "Post", inverted: true, reason: "Not allowed")
        ]
        let packed = packRules(originalRules)
        let unpacked = unpackRules(packed)

        XCTAssertEqual(unpacked.count, 2)
        XCTAssertEqual(unpacked[0].action, .array(["read", "update"]))
        XCTAssertEqual(unpacked[0].subject, .array(["Post", "Comment"]))
        XCTAssertEqual(unpacked[1].inverted, true)
        XCTAssertEqual(unpacked[1].reason, "Not allowed")
    }
}
