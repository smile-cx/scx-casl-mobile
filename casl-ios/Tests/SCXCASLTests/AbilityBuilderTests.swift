import XCTest
@testable import SCXCASL

final class AbilityBuilderTests: XCTestCase {

    private func defaultOptions() -> AbilityOptions {
        return AbilityOptions(
            conditionsMatcher: ConditionsMatcher.match,
            fieldMatcher: FieldMatcher.match
        )
    }

    // MARK: - can / cannot with single action/subject

    func testCanWithSingleActionAndSubject() {
        let builder = AbilityBuilder()
        builder.can("read", "Post")
        XCTAssertEqual(builder.rules.count, 1)
        XCTAssertEqual(builder.rules[0].action, .single("read"))
        XCTAssertEqual(builder.rules[0].subject, .single("Post"))
        XCTAssertFalse(builder.rules[0].inverted)
    }

    func testCannotWithSingleActionAndSubject() {
        let builder = AbilityBuilder()
        builder.cannot("delete", "Post")
        XCTAssertEqual(builder.rules.count, 1)
        XCTAssertEqual(builder.rules[0].action, .single("delete"))
        XCTAssertEqual(builder.rules[0].subject, .single("Post"))
        XCTAssertTrue(builder.rules[0].inverted)
    }

    // MARK: - Multiple actions/subjects

    func testCanWithMultipleActions() {
        let builder = AbilityBuilder()
        builder.can(["read", "update"], "Post")
        XCTAssertEqual(builder.rules[0].action, .array(["read", "update"]))
    }

    func testCanWithMultipleSubjects() {
        let builder = AbilityBuilder()
        builder.can("read", ["Post", "Comment"])
        XCTAssertEqual(builder.rules[0].subject, .array(["Post", "Comment"]))
    }

    // MARK: - Conditions

    func testCanWithConditions() {
        let builder = AbilityBuilder()
        builder.can("read", "Post", conditions: ["author": "john"])
        XCTAssertNotNil(builder.rules[0].conditions)
        XCTAssertEqual(builder.rules[0].conditions?["author"] as? String, "john")
    }

    // MARK: - Fields

    func testCanWithFields() {
        let builder = AbilityBuilder()
        builder.can("read", "Post", fields: "title")
        XCTAssertEqual(builder.rules[0].fields, .single("title"))
    }

    func testCanWithFieldsAndConditions() {
        let builder = AbilityBuilder()
        builder.can("read", "Post", fields: .array(["title", "body"]), conditions: ["published": true])
        XCTAssertEqual(builder.rules[0].fields, .array(["title", "body"]))
        XCTAssertNotNil(builder.rules[0].conditions)
    }

    // MARK: - Claim-based rules

    func testClaimBasedRule() {
        let builder = AbilityBuilder()
        builder.can("read")
        XCTAssertEqual(builder.rules[0].action, .single("read"))
        XCTAssertNil(builder.rules[0].subject)
    }

    // MARK: - because() reason

    func testBecauseReason() {
        let builder = AbilityBuilder()
        builder.cannot("delete", "Post").because("Not allowed to delete posts")
        XCTAssertEqual(builder.rules[0].reason, "Not allowed to delete posts")
    }

    // MARK: - build() creates Ability

    func testBuildCreatesAbility() {
        let builder = AbilityBuilder()
        builder.can("read", "Post")
        builder.cannot("delete", "Post")

        let ability = builder.build(options: defaultOptions())
        XCTAssertTrue(ability.can("read", "Post"))
        XCTAssertFalse(ability.can("delete", "Post"))
    }

    // MARK: - defineAbility sync

    func testDefineAbilitySync() {
        let ability = defineAbility({ builder in
            builder.can("read", "Post")
            builder.cannot("delete", "Post")
        }, options: defaultOptions())

        XCTAssertTrue(ability.can("read", "Post"))
        XCTAssertFalse(ability.can("delete", "Post"))
    }

    // MARK: - defineAbility async

    func testDefineAbilityAsync() async {
        let ability = await defineAbility({ builder async in
            builder.can("read", "Post")
            builder.can("update", "Post")
        }, options: defaultOptions())

        XCTAssertTrue(ability.can("read", "Post"))
        XCTAssertTrue(ability.can("update", "Post"))
    }

    // MARK: - defineAbility with custom options

    // JS: "accepts options for Ability instance as the 2nd parameter"
    func testDefineAbilityWithCustomOptions() {
        // JS test uses a custom detectSubjectType that reads `ModelName` property
        let detectSubjectType: DetectSubjectType = { subject in
            if let str = subject as? String { return str }
            if let dict = subject as? [String: Any], let modelName = dict["ModelName"] as? String {
                return modelName
            }
            return String(describing: type(of: subject))
        }

        var opts = defaultOptions()
        opts.detectSubjectType = detectSubjectType

        let ability = defineAbility({ builder in
            builder.can("read", "Book")
        }, options: opts)

        // Instance with "ModelName" property should be detected as "Book"
        let book: [String: Any] = ["ModelName": "Book"]
        XCTAssertTrue(ability.can("read", book))
    }
}
