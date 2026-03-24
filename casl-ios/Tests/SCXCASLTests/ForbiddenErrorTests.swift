import XCTest
@testable import SCXCASL

final class ForbiddenErrorTests: XCTestCase {

    private func defaultOptions() -> AbilityOptions {
        return AbilityOptions(
            conditionsMatcher: ConditionsMatcher.match,
            fieldMatcher: FieldMatcher.match
        )
    }

    // MARK: - throwUnlessCan

    func testThrowUnlessCanThrowsOnDisallowed() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post")
        ], options: defaultOptions())

        let error = ForbiddenError.from(ability)
        XCTAssertThrowsError(try error.throwUnlessCan("delete", "Post"))
    }

    func testThrowUnlessCanDoesNotThrowOnAllowed() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post")
        ], options: defaultOptions())

        let error = ForbiddenError.from(ability)
        XCTAssertNoThrow(try error.throwUnlessCan("read", "Post"))
    }

    // MARK: - Error context info

    func testErrorHasContextInfo() {
        let ability = Ability(rules: [], options: defaultOptions())
        let error = ForbiddenError.from(ability)
        let result = error.unlessCan("delete", "Post")

        XCTAssertNotNil(result)
        XCTAssertEqual(result?.action, "delete")
        XCTAssertEqual(result?.subjectType, "Post")
    }

    func testErrorWithField() {
        let ability = Ability(rules: [], options: defaultOptions())
        let error = ForbiddenError.from(ability)
        let result = error.unlessCan("update", "Post", field: "title")

        XCTAssertNotNil(result)
        XCTAssertEqual(result?.field, "title")
    }

    // MARK: - Reason from rule

    func testReasonFromRule() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post"),
            RawRule(action: "delete", subject: "Post", inverted: true, reason: "Deletion is prohibited")
        ], options: defaultOptions())

        let error = ForbiddenError.from(ability)
        let result = error.unlessCan("delete", "Post")

        XCTAssertNotNil(result)
        XCTAssertEqual(result?.message, "Deletion is prohibited")
    }

    // MARK: - Custom message via setMessage

    func testSetMessage() {
        let ability = Ability(rules: [], options: defaultOptions())
        let error = ForbiddenError.from(ability)
        error.setMessage("Custom error message")
        let result = error.unlessCan("delete", "Post")

        XCTAssertNotNil(result)
        XCTAssertEqual(result?.message, "Custom error message")
    }

    // MARK: - Default error message

    func testDefaultErrorMessage() {
        let ability = Ability(rules: [], options: defaultOptions())
        let error = ForbiddenError.from(ability)
        let result = error.unlessCan("delete", "Post")

        XCTAssertNotNil(result)
        XCTAssertEqual(result?.message, "Cannot execute \"delete\" on \"Post\"")
    }

    func testSetDefaultMessageFunction() {
        let original = ForbiddenError.defaultErrorMessage
        defer { ForbiddenError.defaultErrorMessage = original }

        ForbiddenError.defaultErrorMessage = { error in
            "You are not allowed to \(error.action) \(error.subjectType)"
        }

        let ability = Ability(rules: [], options: defaultOptions())
        let error = ForbiddenError.from(ability)
        let result = error.unlessCan("delete", "Post")

        XCTAssertNotNil(result)
        XCTAssertEqual(result?.message, "You are not allowed to delete Post")
    }

    // MARK: - setDefaultMessage with constant string

    func testSetDefaultMessageWithStringConstant() {
        let original = ForbiddenError.defaultErrorMessage
        defer { ForbiddenError.defaultErrorMessage = original }

        ForbiddenError.defaultErrorMessage = { _ in "Access denied" }

        let ability = Ability(rules: [], options: defaultOptions())
        let error = ForbiddenError.from(ability)
        let result = error.unlessCan("delete", "Post")

        XCTAssertEqual(result?.message, "Access denied")
    }

    // MARK: - from() factory

    func testFromFactory() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post")
        ], options: defaultOptions())

        let error = ForbiddenError.from(ability)
        XCTAssertTrue(error.ability === ability)
    }

    // MARK: - description

    func testDescription() {
        let ability = Ability(rules: [], options: defaultOptions())
        let error = ForbiddenError.from(ability)
        _ = error.unlessCan("delete", "Post")
        XCTAssertEqual(error.description, "Cannot execute \"delete\" on \"Post\"")
    }
}
