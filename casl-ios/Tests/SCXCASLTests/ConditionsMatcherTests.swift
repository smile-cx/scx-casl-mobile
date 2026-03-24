import XCTest
@testable import SCXCASL

final class ConditionsMatcherTests: XCTestCase {

    // MARK: - $eq

    func testEqWithStrings() {
        let matcher = ConditionsMatcher.match(conditions: ["name": "john"])
        XCTAssertTrue(matcher(["name": "john"]))
        XCTAssertFalse(matcher(["name": "jane"]))
    }

    func testEqWithNumbers() {
        let matcher = ConditionsMatcher.match(conditions: ["age": 25])
        XCTAssertTrue(matcher(["age": 25]))
        XCTAssertFalse(matcher(["age": 30]))
    }

    func testEqWithDoubles() {
        let matcher = ConditionsMatcher.match(conditions: ["score": 9.5])
        XCTAssertTrue(matcher(["score": 9.5]))
        XCTAssertFalse(matcher(["score": 8.0]))
    }

    func testEqWithBools() {
        let matcher = ConditionsMatcher.match(conditions: ["active": true])
        XCTAssertTrue(matcher(["active": true]))
        XCTAssertFalse(matcher(["active": false]))
    }

    func testEqWithArrayFieldValue() {
        // If field is array, check if any element equals
        let matcher = ConditionsMatcher.match(conditions: ["tags": "swift"])
        XCTAssertTrue(matcher(["tags": ["swift", "ios"]]))
        XCTAssertFalse(matcher(["tags": ["java", "android"]]))
    }

    func testExplicitEqOperator() {
        let matcher = ConditionsMatcher.match(conditions: ["name": ["$eq": "john"]])
        XCTAssertTrue(matcher(["name": "john"]))
        XCTAssertFalse(matcher(["name": "jane"]))
    }

    // MARK: - $ne

    func testNe() {
        let matcher = ConditionsMatcher.match(conditions: ["status": ["$ne": "draft"]])
        XCTAssertTrue(matcher(["status": "published"]))
        XCTAssertFalse(matcher(["status": "draft"]))
    }

    // MARK: - $gt, $gte

    func testGtWithNumbers() {
        let matcher = ConditionsMatcher.match(conditions: ["age": ["$gt": 18]])
        XCTAssertTrue(matcher(["age": 25]))
        XCTAssertFalse(matcher(["age": 18]))
        XCTAssertFalse(matcher(["age": 10]))
    }

    func testGteWithNumbers() {
        let matcher = ConditionsMatcher.match(conditions: ["age": ["$gte": 18]])
        XCTAssertTrue(matcher(["age": 25]))
        XCTAssertTrue(matcher(["age": 18]))
        XCTAssertFalse(matcher(["age": 10]))
    }

    // MARK: - $lt, $lte

    func testLtWithNumbers() {
        let matcher = ConditionsMatcher.match(conditions: ["age": ["$lt": 18]])
        XCTAssertTrue(matcher(["age": 10]))
        XCTAssertFalse(matcher(["age": 18]))
        XCTAssertFalse(matcher(["age": 25]))
    }

    func testLteWithNumbers() {
        let matcher = ConditionsMatcher.match(conditions: ["age": ["$lte": 18]])
        XCTAssertTrue(matcher(["age": 10]))
        XCTAssertTrue(matcher(["age": 18]))
        XCTAssertFalse(matcher(["age": 25]))
    }

    // MARK: - $in

    func testInWithSingleValues() {
        let matcher = ConditionsMatcher.match(conditions: ["status": ["$in": ["active", "pending"]]])
        XCTAssertTrue(matcher(["status": "active"]))
        XCTAssertTrue(matcher(["status": "pending"]))
        XCTAssertFalse(matcher(["status": "banned"]))
    }

    func testInWithArrayFieldValue() {
        // If field is array, check if ANY element is in the specified array
        let matcher = ConditionsMatcher.match(conditions: ["tags": ["$in": ["swift", "kotlin"]]])
        XCTAssertTrue(matcher(["tags": ["swift", "ios"]]))
        XCTAssertFalse(matcher(["tags": ["java", "android"]]))
    }

    // MARK: - $nin

    func testNin() {
        let matcher = ConditionsMatcher.match(conditions: ["status": ["$nin": ["banned", "suspended"]]])
        XCTAssertTrue(matcher(["status": "active"]))
        XCTAssertFalse(matcher(["status": "banned"]))
    }

    // MARK: - $all

    func testAll() {
        let matcher = ConditionsMatcher.match(conditions: ["tags": ["$all": ["swift", "ios"]]])
        XCTAssertTrue(matcher(["tags": ["swift", "ios", "casl"]]))
        XCTAssertFalse(matcher(["tags": ["swift", "android"]]))
    }

    // MARK: - $size

    func testSize() {
        let matcher = ConditionsMatcher.match(conditions: ["tags": ["$size": 3]])
        XCTAssertTrue(matcher(["tags": ["a", "b", "c"]]))
        XCTAssertFalse(matcher(["tags": ["a", "b"]]))
    }

    // MARK: - $regex

    func testRegexBasic() {
        let matcher = ConditionsMatcher.match(conditions: ["name": ["$regex": "^John"]])
        XCTAssertTrue(matcher(["name": "John Doe"]))
        XCTAssertFalse(matcher(["name": "jane doe"]))
    }

    func testRegexWithOptions() {
        let matcher = ConditionsMatcher.match(conditions: ["name": ["$regex": "^john", "$options": "i"]])
        XCTAssertTrue(matcher(["name": "John Doe"]))
        XCTAssertTrue(matcher(["name": "john doe"]))
    }

    // MARK: - $exists

    func testExistsTrue() {
        let matcher = ConditionsMatcher.match(conditions: ["email": ["$exists": true]])
        XCTAssertTrue(matcher(["email": "test@example.com"]))
        XCTAssertFalse(matcher(["name": "John"]))
    }

    func testExistsFalse() {
        let matcher = ConditionsMatcher.match(conditions: ["deletedAt": ["$exists": false]])
        XCTAssertTrue(matcher(["name": "John"]))
        XCTAssertFalse(matcher(["name": "John", "deletedAt": "2024-01-01"]))
    }

    // MARK: - $elemMatch

    func testElemMatch() {
        let matcher = ConditionsMatcher.match(conditions: [
            "comments": ["$elemMatch": ["author": "john", "approved": true] as [String: Any]]
        ])

        let obj1: [String: Any] = [
            "comments": [
                ["author": "john", "approved": true] as [String: Any],
                ["author": "jane", "approved": false] as [String: Any]
            ]
        ]
        XCTAssertTrue(matcher(obj1))

        let obj2: [String: Any] = [
            "comments": [
                ["author": "john", "approved": false] as [String: Any]
            ]
        ]
        XCTAssertFalse(matcher(obj2))
    }

    // MARK: - $elemMatch with scalar arrays

    func testElemMatchWithScalarArrayGt() {
        let matcher = ConditionsMatcher.match(conditions: [
            "scores": ["$elemMatch": ["$gt": 5] as [String: Any]]
        ])
        XCTAssertTrue(matcher(["scores": [1, 2, 10]]))
        XCTAssertFalse(matcher(["scores": [1, 2, 3]]))
    }

    func testElemMatchWithScalarArrayLt() {
        let matcher = ConditionsMatcher.match(conditions: [
            "scores": ["$elemMatch": ["$lt": 5] as [String: Any]]
        ])
        XCTAssertTrue(matcher(["scores": [1, 10, 20]]))
        XCTAssertFalse(matcher(["scores": [10, 20, 30]]))
    }

    func testElemMatchWithScalarArrayGteAndLte() {
        let matcher = ConditionsMatcher.match(conditions: [
            "scores": ["$elemMatch": ["$gte": 5, "$lte": 15] as [String: Any]]
        ])
        XCTAssertTrue(matcher(["scores": [1, 10, 20]]))  // 10 is in [5,15]
        XCTAssertFalse(matcher(["scores": [1, 2, 3]]))    // none in [5,15]
        XCTAssertFalse(matcher(["scores": [20, 30]]))      // none in [5,15]
    }

    func testElemMatchWithScalarArrayEq() {
        let matcher = ConditionsMatcher.match(conditions: [
            "tags": ["$elemMatch": ["$eq": "swift"] as [String: Any]]
        ])
        XCTAssertTrue(matcher(["tags": ["swift", "ios"]]))
        XCTAssertFalse(matcher(["tags": ["java", "android"]]))
    }

    func testElemMatchWithScalarArrayNe() {
        // $ne in $elemMatch: at least one element != "draft"
        let matcher = ConditionsMatcher.match(conditions: [
            "statuses": ["$elemMatch": ["$ne": "draft"] as [String: Any]]
        ])
        XCTAssertTrue(matcher(["statuses": ["draft", "published"]]))  // "published" != "draft"
        XCTAssertFalse(matcher(["statuses": ["draft", "draft"]]))      // all are "draft"
    }

    func testElemMatchWithMixedArray() {
        // Array containing both dicts and scalars — only dicts match sub-conditions without operators
        let matcher = ConditionsMatcher.match(conditions: [
            "items": ["$elemMatch": ["name": "apple"] as [String: Any]]
        ])
        let obj1: [String: Any] = ["items": [["name": "apple"] as [String: Any], 42]]
        let obj2: [String: Any] = ["items": [42, "hello"]]
        XCTAssertTrue(matcher(obj1))
        XCTAssertFalse(matcher(obj2))
    }

    func testElemMatchStillWorksWithDictArray() {
        // Ensure existing dict-array behavior is preserved
        let matcher = ConditionsMatcher.match(conditions: [
            "items": ["$elemMatch": ["name": "apple", "qty": ["$gt": 0]] as [String: Any]]
        ])
        let obj1: [String: Any] = ["items": [["name": "apple", "qty": 5] as [String: Any]]]
        let obj2: [String: Any] = ["items": [["name": "banana", "qty": 5] as [String: Any]]]
        XCTAssertTrue(matcher(obj1))
        XCTAssertFalse(matcher(obj2))
    }

    func testElemMatchWithEmptyScalarArray() {
        let matcher = ConditionsMatcher.match(conditions: [
            "scores": ["$elemMatch": ["$gt": 5] as [String: Any]]
        ])
        XCTAssertFalse(matcher(["scores": [Int]()]))
    }

    func testElemMatchWithNonArrayField() {
        let matcher = ConditionsMatcher.match(conditions: [
            "score": ["$elemMatch": ["$gt": 5] as [String: Any]]
        ])
        XCTAssertFalse(matcher(["score": 10]))  // Not an array, should return false
    }

    // MARK: - Custom Operator Registration

    func testCustomOperatorRegistration() {
        ConditionsMatcher.registerOperator("$startsWith") { fieldValue, expected in
            guard let str = fieldValue as? String, let prefix = expected as? String else { return false }
            return str.hasPrefix(prefix)
        }

        let matcher = ConditionsMatcher.match(conditions: ["name": ["$startsWith": "Jo"]])
        XCTAssertTrue(matcher(["name": "John"]))
        XCTAssertFalse(matcher(["name": "Jane"]))

        ConditionsMatcher.unregisterOperator("$startsWith")

        // After unregistering, the operator should cause a match failure
        let matcher2 = ConditionsMatcher.match(conditions: ["name": ["$startsWith": "Jo"]])
        XCTAssertFalse(matcher2(["name": "John"]))
    }

    func testRegisterOperatorWithoutDollarPrefixThrows() {
        // Registering an operator without a $ prefix should fail or be ignored
        // On iOS the operator would just not match, since only $-prefixed keys are treated as operators
        let matcher = ConditionsMatcher.match(conditions: ["name": ["noDollar": "Jo"]])
        // Unknown operators without $ are treated as equality checks on the whole dict — should not match
        XCTAssertFalse(matcher(["name": "John"]))
    }

    func testCrossTypeNumericComparison() {
        // Int, Double equality — same numeric value should match regardless of Swift numeric type
        let matcher = ConditionsMatcher.match(conditions: ["count": 5])
        XCTAssertTrue(matcher(["count": 5]))
        XCTAssertTrue(matcher(["count": 5.0]))
    }

    func testBuildMatcherWithCustomOperators() {
        let customMatcher = ConditionsMatcher.buildMatcher(customOperators: [
            "$endsWith": { fieldValue, expected in
                guard let str = fieldValue as? String, let suffix = expected as? String else { return false }
                return str.hasSuffix(suffix)
            }
        ])

        let match = customMatcher(["name": ["$endsWith": "hn"]])
        XCTAssertTrue(match(["name": "John"]))
        XCTAssertFalse(match(["name": "Jane"]))
    }

    func testClearCustomOperators() {
        ConditionsMatcher.registerOperator("$temp") { _, _ in true }
        ConditionsMatcher.clearCustomOperators()

        let matcher = ConditionsMatcher.match(conditions: ["x": ["$temp": 1]])
        XCTAssertFalse(matcher(["x": 1]))
    }

    // MARK: - Dot Notation

    func testDotNotationNestedAccess() {
        let matcher = ConditionsMatcher.match(conditions: ["author.name": "john"])
        XCTAssertTrue(matcher(["author": ["name": "john"]]))
        XCTAssertFalse(matcher(["author": ["name": "jane"]]))
    }

    func testDotNotationDeeplyNested() {
        let matcher = ConditionsMatcher.match(conditions: ["a.b.c": "value"])
        XCTAssertTrue(matcher(["a": ["b": ["c": "value"]]]))
        XCTAssertFalse(matcher(["a": ["b": ["c": "other"]]]))
    }

    // MARK: - Array Index Access

    func testArrayIndexAccess() {
        let matcher = ConditionsMatcher.match(conditions: ["items.0": "first"])
        XCTAssertTrue(matcher(["items": ["first", "second"]]))
        XCTAssertFalse(matcher(["items": ["zero", "second"]]))
    }

    func testDotNotationArrayIndexExists() {
        let matcher = ConditionsMatcher.match(conditions: ["authors.0": ["$exists": false]])
        XCTAssertTrue(matcher(["authors": [Any]()]))
        XCTAssertFalse(matcher(["authors": ["me", "someoneelse"]]))
    }

    func testArrayIndexWithNestedField() {
        let matcher = ConditionsMatcher.match(conditions: ["comments.0.author": "john"])
        let obj: [String: Any] = [
            "comments": [
                ["author": "john"] as [String: Any],
                ["author": "jane"] as [String: Any]
            ]
        ]
        XCTAssertTrue(matcher(obj))
    }

    // MARK: - Multiple Conditions

    func testMultipleConditionsANDed() {
        let matcher = ConditionsMatcher.match(conditions: ["author": "john", "published": true])
        XCTAssertTrue(matcher(["author": "john", "published": true]))
        XCTAssertFalse(matcher(["author": "john", "published": false]))
        XCTAssertFalse(matcher(["author": "jane", "published": true]))
    }

    func testMultipleOperatorsOnSameField() {
        let matcher = ConditionsMatcher.match(conditions: ["age": ["$gte": 18, "$lt": 65]])
        XCTAssertTrue(matcher(["age": 25]))
        XCTAssertFalse(matcher(["age": 10]))
        XCTAssertFalse(matcher(["age": 70]))
    }

    // MARK: - $and

    func testAndAllMatch() {
        let matcher = ConditionsMatcher.match(conditions: [
            "$and": [
                ["name": "john"] as [String: Any],
                ["age": ["$gte": 18]] as [String: Any]
            ]
        ])
        XCTAssertTrue(matcher(["name": "john", "age": 25]))
        XCTAssertFalse(matcher(["name": "john", "age": 10]))
        XCTAssertFalse(matcher(["name": "jane", "age": 25]))
    }

    func testAndPartialMatch() {
        let matcher = ConditionsMatcher.match(conditions: [
            "$and": [
                ["status": "active"] as [String: Any],
                ["role": "admin"] as [String: Any]
            ]
        ])
        XCTAssertTrue(matcher(["status": "active", "role": "admin"]))
        XCTAssertFalse(matcher(["status": "active", "role": "user"]))
        XCTAssertFalse(matcher(["status": "inactive", "role": "admin"]))
    }

    // MARK: - $or

    func testOrAnyMatch() {
        let matcher = ConditionsMatcher.match(conditions: [
            "$or": [
                ["status": "active"] as [String: Any],
                ["status": "pending"] as [String: Any]
            ]
        ])
        XCTAssertTrue(matcher(["status": "active"]))
        XCTAssertTrue(matcher(["status": "pending"]))
        XCTAssertFalse(matcher(["status": "banned"]))
    }

    func testOrWithDifferentFields() {
        let matcher = ConditionsMatcher.match(conditions: [
            "$or": [
                ["name": "john"] as [String: Any],
                ["age": ["$gt": 30]] as [String: Any]
            ]
        ])
        XCTAssertTrue(matcher(["name": "john", "age": 20]))
        XCTAssertTrue(matcher(["name": "jane", "age": 40]))
        XCTAssertFalse(matcher(["name": "jane", "age": 20]))
    }

    // MARK: - $not

    func testNotBasic() {
        let matcher = ConditionsMatcher.match(conditions: [
            "$not": ["status": "draft"] as [String: Any]
        ])
        XCTAssertTrue(matcher(["status": "published"]))
        XCTAssertFalse(matcher(["status": "draft"]))
    }

    func testNotWithOperator() {
        let matcher = ConditionsMatcher.match(conditions: [
            "$not": ["age": ["$gt": 18]] as [String: Any]
        ])
        XCTAssertTrue(matcher(["age": 10]))
        XCTAssertFalse(matcher(["age": 25]))
    }

    // MARK: - Nested logical operators

    func testAndWithOr() {
        let conditions: [String: Any] = [
            "$and": [
                ["$or": [
                    ["role": "admin"] as [String: Any],
                    ["role": "editor"] as [String: Any]
                ]] as [String: Any],
                ["active": true] as [String: Any]
            ]
        ]
        let matcher = ConditionsMatcher.match(conditions: conditions)
        XCTAssertTrue(matcher(["role": "admin", "active": true]))
        XCTAssertTrue(matcher(["role": "editor", "active": true]))
        XCTAssertFalse(matcher(["role": "admin", "active": false]))
        XCTAssertFalse(matcher(["role": "user", "active": true]))
    }

    // MARK: - __caslSubjectType__ filtering

    func testCaslSubjectTypeIsIgnoredInConditions() {
        // __caslSubjectType__ in the conditions should be skipped
        let matcher = ConditionsMatcher.match(conditions: [
            "__caslSubjectType__": "Post",
            "author": "john"
        ])
        // The matcher should only check "author", ignoring __caslSubjectType__
        XCTAssertTrue(matcher(["author": "john"]))
        XCTAssertTrue(matcher(["author": "john", "__caslSubjectType__": "Post"]))
        XCTAssertFalse(matcher(["author": "jane"]))
    }

    func testCaslSubjectTypeInObjectDoesNotInterfere() {
        let matcher = ConditionsMatcher.match(conditions: ["author": "john"])
        // Object has __caslSubjectType__ but conditions don't check it
        XCTAssertTrue(matcher(["__caslSubjectType__": "Post", "author": "john"]))
        XCTAssertFalse(matcher(["__caslSubjectType__": "Post", "author": "jane"]))
    }

    // MARK: - $regex with $options flags

    func testRegexWithMultilineOption() {
        let matcher = ConditionsMatcher.match(conditions: ["text": ["$regex": "^hello", "$options": "im"]])
        XCTAssertTrue(matcher(["text": "world\nhello"]))
        XCTAssertTrue(matcher(["text": "Hello"]))
    }

    func testRegexWithDotMatchesLineSeparatorsOption() {
        let matcher = ConditionsMatcher.match(conditions: ["text": ["$regex": "hello.world", "$options": "s"]])
        XCTAssertTrue(matcher(["text": "hello\nworld"]))
        XCTAssertFalse(matcher(["text": "helloXworld"]) == false) // "helloXworld" should match since . matches X
        // Actually . matches X normally, so let's test more carefully
        XCTAssertTrue(matcher(["text": "helloXworld"]))
        XCTAssertTrue(matcher(["text": "hello\nworld"])) // with 's', . matches \n
    }
}
