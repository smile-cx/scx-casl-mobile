import XCTest
@testable import SCXCASL

final class SubjectHelperTests: XCTestCase {

    func testSetSubjectTypeDefinesType() throws {
        let obj: [String: Any] = ["name": "Test"]
        let result = try setSubjectType("Post", on: obj)
        XCTAssertEqual(result["__caslSubjectType__"] as? String, "Post")
    }

    func testSameTypeDoesNotThrow() throws {
        let obj: [String: Any] = ["__caslSubjectType__": "Post", "name": "Test"]
        let result = try setSubjectType("Post", on: obj)
        XCTAssertEqual(result["__caslSubjectType__"] as? String, "Post")
    }

    func testDifferentTypeThrows() {
        let obj: [String: Any] = ["__caslSubjectType__": "Post", "name": "Test"]
        XCTAssertThrowsError(try setSubjectType("Comment", on: obj)) { error in
            XCTAssertTrue(error is SubjectHelperError)
        }
    }

    func testDetectSubjectTypeReturnsSetType() {
        let obj: [String: Any] = ["__caslSubjectType__": "Post"]
        XCTAssertEqual(detectSubjectType(obj), "Post")
    }

    func testDetectSubjectTypeReturnsDictionaryWhenNoType() {
        let obj: [String: Any] = ["name": "Test"]
        XCTAssertEqual(detectSubjectType(obj), "Dictionary")
    }

    func testSetSubjectTypePreservesExistingFields() throws {
        // In Swift, dictionaries are value types — setSubjectType returns a new copy
        // with the type key added, preserving all original fields.
        let obj: [String: Any] = ["name": "test", "count": 42]
        let result = try setSubjectType("Article", on: obj)
        XCTAssertEqual(result["__caslSubjectType__"] as? String, "Article")
        XCTAssertEqual(result["name"] as? String, "test")
        XCTAssertEqual(result["count"] as? Int, 42)
    }

    // JS: "ignores falsy subjects" — subject('Test', null) and subject('Test', undefined) should not throw
    func testIgnoresFalsySubjects() {
        // In Swift, passing nil to setSubjectType is not directly possible since
        // the function expects [String: Any]. We test with an empty dictionary
        // which is the closest Swift equivalent of a "falsy" object context.
        // The key behavior: setSubjectType should not throw for empty dictionaries.
        XCTAssertNoThrow(try setSubjectType("Test", on: [:]))
        let result = try! setSubjectType("Test", on: [:])
        XCTAssertEqual(result["__caslSubjectType__"] as? String, "Test")
    }
}
