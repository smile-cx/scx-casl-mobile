import XCTest
@testable import SCXCASL

final class FieldMatcherTests: XCTestCase {

    // MARK: - Exact Match

    func testExactMatch() {
        let matcher = FieldMatcher.match(fields: ["title"])
        XCTAssertTrue(matcher("title"))
        XCTAssertFalse(matcher("body"))
    }

    func testMultipleExactMatch() {
        let matcher = FieldMatcher.match(fields: ["title", "body"])
        XCTAssertTrue(matcher("title"))
        XCTAssertTrue(matcher("body"))
        XCTAssertFalse(matcher("author"))
    }

    // MARK: - author.* Pattern (single level)

    func testAuthorStarPattern() {
        let matcher = FieldMatcher.match(fields: ["author.*"])
        XCTAssertTrue(matcher("author.name"))
        XCTAssertTrue(matcher("author.email"))
        XCTAssertFalse(matcher("author.pub.name"))
        XCTAssertFalse(matcher("author"))
    }

    // MARK: - author.** Pattern (multi level)

    func testAuthorDoubleStarPattern() {
        let matcher = FieldMatcher.match(fields: ["author.**"])
        XCTAssertTrue(matcher("author.name"))
        XCTAssertTrue(matcher("author.pub.name"))
        XCTAssertTrue(matcher("author.pub.sub.name"))
    }

    // MARK: - author.*.name Pattern

    func testAuthorStarNamePattern() {
        let matcher = FieldMatcher.match(fields: ["author.*.name"])
        XCTAssertTrue(matcher("author.pub.name"))
        XCTAssertFalse(matcher("author.pub.sub.name"))
        XCTAssertFalse(matcher("author.name"))
    }

    // MARK: - author.**.name Pattern

    func testAuthorDoubleStarNamePattern() {
        let matcher = FieldMatcher.match(fields: ["author.**.name"])
        XCTAssertTrue(matcher("author.pub.name"))
        XCTAssertTrue(matcher("author.pub.sub.name"))
        XCTAssertFalse(matcher("author.name"))
    }

    // MARK: - *.name and **.name Patterns

    func testStarNamePattern() {
        let matcher = FieldMatcher.match(fields: ["*.name"])
        XCTAssertTrue(matcher("author.name"))
        XCTAssertFalse(matcher("author.pub.name"))
    }

    func testDoubleStarNamePattern() {
        let matcher = FieldMatcher.match(fields: ["**.name"])
        XCTAssertTrue(matcher("author.name"))
        XCTAssertTrue(matcher("author.pub.name"))
        XCTAssertTrue(matcher("a.b.c.name"))
    }

    // MARK: - Trailing Star

    func testTrailingStar() {
        let matcher = FieldMatcher.match(fields: ["street*"])
        XCTAssertTrue(matcher("street"))
        XCTAssertTrue(matcher("street1"))
        XCTAssertTrue(matcher("street2"))
        XCTAssertTrue(matcher("streetAddress"))
        XCTAssertFalse(matcher("avenue"))
    }

    // MARK: - Special Characters

    func testSpecialCharsEscaped() {
        let matcher = FieldMatcher.match(fields: ["field?name"])
        XCTAssertTrue(matcher("field?name"))
        XCTAssertFalse(matcher("fieldXname"))
    }

    func testPlusCharEscaped() {
        let matcher = FieldMatcher.match(fields: ["field+name"])
        XCTAssertTrue(matcher("field+name"))
        XCTAssertFalse(matcher("fieldname"))
    }

    // MARK: - Combined Patterns

    func testCombinedPatterns() {
        let matcher = FieldMatcher.match(fields: ["title", "author.*", "meta.**"])
        XCTAssertTrue(matcher("title"))
        XCTAssertTrue(matcher("author.name"))
        XCTAssertFalse(matcher("author.pub.name"))
        XCTAssertTrue(matcher("meta.tags"))
        XCTAssertTrue(matcher("meta.deep.nested"))
        XCTAssertFalse(matcher("body"))
    }

    // MARK: - No Wildcards Uses Simple Contains

    func testNoWildcardsSimpleContains() {
        let matcher = FieldMatcher.match(fields: ["title", "body", "author.name"])
        XCTAssertTrue(matcher("title"))
        XCTAssertTrue(matcher("body"))
        XCTAssertTrue(matcher("author.name"))
        XCTAssertFalse(matcher("author"))
        XCTAssertFalse(matcher("secret"))
    }
}
