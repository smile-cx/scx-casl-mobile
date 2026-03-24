import XCTest
@testable import SCXCASL

final class RawRuleCodableTests: XCTestCase {

    // MARK: - Encoding

    func testEncodeSimpleRule() throws {
        let rule = RawRule(action: "read", subject: "Post")
        let data = try JSONEncoder().encode(rule)
        let json = try JSONSerialization.jsonObject(with: data) as! [String: Any]

        XCTAssertEqual(json["action"] as? String, "read")
        XCTAssertEqual(json["subject"] as? String, "Post")
        XCTAssertNil(json["inverted"])  // false is omitted
        XCTAssertNil(json["conditions"])
        XCTAssertNil(json["fields"])
        XCTAssertNil(json["reason"])
    }

    func testEncodeRuleWithArrayActions() throws {
        let rule = RawRule(action: .array(["read", "update"]), subject: "Post")
        let data = try JSONEncoder().encode(rule)
        let json = try JSONSerialization.jsonObject(with: data) as! [String: Any]

        XCTAssertEqual(json["action"] as? [String], ["read", "update"])
    }

    func testEncodeRuleWithConditions() throws {
        let rule = RawRule(action: "read", subject: "Post", conditions: ["author": "john", "published": true])
        let data = try JSONEncoder().encode(rule)
        let json = try JSONSerialization.jsonObject(with: data) as! [String: Any]

        let conditions = json["conditions"] as! [String: Any]
        XCTAssertEqual(conditions["author"] as? String, "john")
        XCTAssertEqual(conditions["published"] as? Bool, true)
    }

    func testEncodeRuleWithNestedConditions() throws {
        let rule = RawRule(
            action: "read",
            subject: "Post",
            conditions: ["views": ["$gt": 100]]
        )
        let data = try JSONEncoder().encode(rule)
        let json = try JSONSerialization.jsonObject(with: data) as! [String: Any]

        let conditions = json["conditions"] as! [String: Any]
        let views = conditions["views"] as! [String: Any]
        XCTAssertEqual(views["$gt"] as? Int, 100)
    }

    func testEncodeInvertedRule() throws {
        let rule = RawRule(action: "delete", subject: "Post", inverted: true, reason: "Not allowed")
        let data = try JSONEncoder().encode(rule)
        let json = try JSONSerialization.jsonObject(with: data) as! [String: Any]

        XCTAssertEqual(json["inverted"] as? Bool, true)
        XCTAssertEqual(json["reason"] as? String, "Not allowed")
    }

    func testEncodeRuleWithFields() throws {
        let rule = RawRule(action: "read", subject: "Post", fields: .array(["title", "body"]))
        let data = try JSONEncoder().encode(rule)
        let json = try JSONSerialization.jsonObject(with: data) as! [String: Any]

        XCTAssertEqual(json["fields"] as? [String], ["title", "body"])
    }

    // MARK: - Decoding

    func testDecodeSimpleRule() throws {
        let jsonString = """
        {"action": "read", "subject": "Post"}
        """
        let data = jsonString.data(using: .utf8)!
        let rule = try JSONDecoder().decode(RawRule.self, from: data)

        XCTAssertEqual(rule.action, .single("read"))
        XCTAssertEqual(rule.subject, .single("Post"))
        XCTAssertFalse(rule.inverted)
        XCTAssertNil(rule.conditions)
        XCTAssertNil(rule.fields)
        XCTAssertNil(rule.reason)
    }

    func testDecodeRuleWithArrayActions() throws {
        let jsonString = """
        {"action": ["read", "update"], "subject": "Post"}
        """
        let data = jsonString.data(using: .utf8)!
        let rule = try JSONDecoder().decode(RawRule.self, from: data)

        XCTAssertEqual(rule.action, .array(["read", "update"]))
    }

    func testDecodeRuleWithArraySubjects() throws {
        let jsonString = """
        {"action": "read", "subject": ["Post", "Comment"]}
        """
        let data = jsonString.data(using: .utf8)!
        let rule = try JSONDecoder().decode(RawRule.self, from: data)

        XCTAssertEqual(rule.subject, .array(["Post", "Comment"]))
    }

    func testDecodeRuleWithConditions() throws {
        let jsonString = """
        {"action": "read", "subject": "Post", "conditions": {"author": "john", "published": true}}
        """
        let data = jsonString.data(using: .utf8)!
        let rule = try JSONDecoder().decode(RawRule.self, from: data)

        XCTAssertNotNil(rule.conditions)
        XCTAssertEqual(rule.conditions?["author"] as? String, "john")
        XCTAssertEqual(rule.conditions?["published"] as? Bool, true)
    }

    func testDecodeRuleWithNestedConditions() throws {
        let jsonString = """
        {"action": "read", "subject": "Post", "conditions": {"views": {"$gt": 100}}}
        """
        let data = jsonString.data(using: .utf8)!
        let rule = try JSONDecoder().decode(RawRule.self, from: data)

        let views = rule.conditions?["views"] as? [String: Any]
        XCTAssertNotNil(views)
        XCTAssertEqual(views?["$gt"] as? Int, 100)
    }

    func testDecodeInvertedRuleWithReason() throws {
        let jsonString = """
        {"action": "delete", "subject": "Post", "inverted": true, "reason": "Not allowed"}
        """
        let data = jsonString.data(using: .utf8)!
        let rule = try JSONDecoder().decode(RawRule.self, from: data)

        XCTAssertTrue(rule.inverted)
        XCTAssertEqual(rule.reason, "Not allowed")
    }

    func testDecodeRuleWithFields() throws {
        let jsonString = """
        {"action": "read", "subject": "Post", "fields": ["title", "body"]}
        """
        let data = jsonString.data(using: .utf8)!
        let rule = try JSONDecoder().decode(RawRule.self, from: data)

        XCTAssertEqual(rule.fields, .array(["title", "body"]))
    }

    func testDecodeRuleWithSingleField() throws {
        let jsonString = """
        {"action": "read", "subject": "Post", "fields": "title"}
        """
        let data = jsonString.data(using: .utf8)!
        let rule = try JSONDecoder().decode(RawRule.self, from: data)

        XCTAssertEqual(rule.fields, .single("title"))
    }

    // MARK: - Round-trip

    func testRoundTrip() throws {
        let original = RawRule(
            action: .array(["read", "update"]),
            subject: .array(["Post", "Comment"]),
            fields: .array(["title", "body"]),
            conditions: ["author": "john", "published": true],
            inverted: true,
            reason: "Testing"
        )

        let data = try JSONEncoder().encode(original)
        let decoded = try JSONDecoder().decode(RawRule.self, from: data)

        XCTAssertEqual(decoded.action, original.action)
        XCTAssertEqual(decoded.subject, original.subject)
        XCTAssertEqual(decoded.fields, original.fields)
        XCTAssertEqual(decoded.inverted, original.inverted)
        XCTAssertEqual(decoded.reason, original.reason)
        XCTAssertEqual(decoded.conditions?["author"] as? String, "john")
        XCTAssertEqual(decoded.conditions?["published"] as? Bool, true)
    }

    func testDecodeArrayOfRules() throws {
        let jsonString = """
        [
            {"action": "read", "subject": "Post"},
            {"action": "update", "subject": "Post", "conditions": {"author": "me"}},
            {"action": "delete", "subject": "Post", "inverted": true, "reason": "Cannot delete"}
        ]
        """
        let data = jsonString.data(using: .utf8)!
        let rules = try JSONDecoder().decode([RawRule].self, from: data)

        XCTAssertEqual(rules.count, 3)
        XCTAssertEqual(rules[0].action, .single("read"))
        XCTAssertNotNil(rules[1].conditions)
        XCTAssertTrue(rules[2].inverted)
        XCTAssertEqual(rules[2].reason, "Cannot delete")
    }
}
