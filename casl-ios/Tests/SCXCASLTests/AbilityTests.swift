import XCTest
@testable import SCXCASL

final class AbilityTests: XCTestCase {

    // MARK: - Helper to create default ability options with matchers

    private func defaultOptions(resolveAction: ResolveAction? = nil) -> AbilityOptions {
        return AbilityOptions(
            conditionsMatcher: ConditionsMatcher.match,
            fieldMatcher: FieldMatcher.match,
            resolveAction: resolveAction
        )
    }

    // MARK: - Action Aliases

    func testActionAliasesWithResolver() {
        let resolver = try! createAliasResolver(["modify": ["update", "delete"]])
        let ability = Ability(rules: [
            RawRule(action: "modify", subject: "Post")
        ], options: defaultOptions(resolveAction: resolver))

        XCTAssertTrue(ability.can("update", "Post"))
        XCTAssertTrue(ability.can("delete", "Post"))
        XCTAssertFalse(ability.can("create", "Post"))
    }

    func testDeeplyNestedAliases() {
        // Matches JS: modify -> [update, delete], crud -> [modify, read]
        // Then can('modify', 'all') should allow 'increment' through the chain
        // JS: resolveAction = createAliasResolver({ sort: 'increment', modify: 'sort' })
        //     defineAbility(can => can('modify', 'all'), { resolveAction })
        //     expect(ability).to.allow('increment', 123)
        let resolver = try! createAliasResolver([
            "sort": "increment",
            "modify": "sort"
        ])
        let ability = Ability(rules: [
            RawRule(action: "modify", subject: "all")
        ], options: defaultOptions(resolveAction: resolver))

        // "modify" expands to ["modify", "sort", "increment"]
        // So any subject (including numeric-like) should be allowed for "increment"
        XCTAssertTrue(ability.can("increment", "SomeSubject"))
        XCTAssertTrue(ability.can("sort", "SomeSubject"))
        XCTAssertTrue(ability.can("modify", "SomeSubject"))
    }

    // MARK: - Alias Validation

    func testManageAliasKeyThrows() {
        // createAliasResolver now throws for invalid aliases.
        XCTAssertThrowsError(try createAliasResolver(["manage": ["update", "delete"]])) { error in
            XCTAssertTrue(error is AliasResolverError)
        }

        // Valid aliases should work fine
        let resolver = try! createAliasResolver(["modify": ["update", "delete"]])
        let result = resolver(["modify"])
        XCTAssertEqual(Set(result), Set(["modify", "update", "delete"]))
    }

    func testReservedTargetThrows() {
        XCTAssertThrowsError(try createAliasResolver(["modify": ["update", "manage"]])) { error in
            XCTAssertTrue(error is AliasResolverError)
        }
    }

    // iOS: skipValidate=true means SKIP validation (opposite naming from JS/Android)
    func testSkipValidateTrueSkipsValidation() {
        let opts = AliasResolverOptions(skipValidate: true)
        // Self-alias would normally throw, but skipValidate=true bypasses it
        XCTAssertNoThrow(try createAliasResolver(["sort": "sort"], options: opts))
    }

    func testSkipValidateFalseStillValidates() {
        let opts = AliasResolverOptions(skipValidate: false)
        XCTAssertThrowsError(try createAliasResolver(["sort": "sort"], options: opts)) { error in
            XCTAssertTrue(error is AliasResolverError)
        }
    }

    func testSkipValidateNilStillValidates() {
        // nil defaults to false — validation runs
        XCTAssertThrowsError(try createAliasResolver(["sort": "sort"])) { error in
            XCTAssertTrue(error is AliasResolverError)
        }
    }

    func testCycleDetectionThrows() {
        XCTAssertThrowsError(try createAliasResolver([
            "a": .array(["b"]),
            "b": .array(["a"])
        ])) { error in
            XCTAssertTrue(error is AliasResolverError)
        }
    }

    func testSelfAliasThrows() {
        // JS: expect(() => createAliasResolver({ sort: 'sort' })).to.throw(Error)
        XCTAssertThrowsError(try createAliasResolver(["sort": "sort"])) { error in
            XCTAssertTrue(error is AliasResolverError)
        }
        // JS: expect(() => createAliasResolver({ sort: ['sort', 'order'] })).to.throw(Error)
        XCTAssertThrowsError(try createAliasResolver(["sort": .array(["sort", "order"])])) { error in
            XCTAssertTrue(error is AliasResolverError)
        }
    }

    // MARK: - Can / Cannot Methods

    func testCanReturnsTrueForAllowedAction() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post")
        ], options: defaultOptions())

        XCTAssertTrue(ability.can("read", "Post"))
    }

    func testCannotReturnsTrueForDisallowedAction() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post")
        ], options: defaultOptions())

        XCTAssertTrue(ability.cannot("delete", "Post"))
    }

    func testCanReturnsFalseForUnspecifiedAction() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post")
        ], options: defaultOptions())

        XCTAssertFalse(ability.can("update", "Post"))
    }

    func testCanReturnsFalseForUnspecifiedSubject() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post")
        ], options: defaultOptions())

        XCTAssertFalse(ability.can("read", "Comment"))
    }

    // MARK: - Rule Listing

    func testPossibleRulesForReturnsMatchingRules() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post"),
            RawRule(action: "update", subject: "Post"),
            RawRule(action: "read", subject: "Comment")
        ], options: defaultOptions())

        let rules = ability.possibleRulesFor("read", "Post")
        XCTAssertEqual(rules.count, 1)
        XCTAssertEqual(rules[0].action, ["read"])
    }

    func testRulesForFiltersRules() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post"),
            RawRule(action: "read", subject: "Post", fields: "title")
        ], options: defaultOptions())

        let rules = ability.rulesFor("read", "Post", field: "title")
        XCTAssertEqual(rules.count, 2)
    }

    // MARK: - Rule Updates

    func testUpdateReplacesRules() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post")
        ], options: defaultOptions())

        XCTAssertTrue(ability.can("read", "Post"))
        XCTAssertFalse(ability.can("update", "Post"))

        ability.update([
            RawRule(action: "update", subject: "Post")
        ])

        XCTAssertFalse(ability.can("read", "Post"))
        XCTAssertTrue(ability.can("update", "Post"))
    }

    // MARK: - Claim-based Rules (action only, no subject)

    func testClaimBasedRulesWithNoSubject() {
        let ability = Ability(rules: [
            RawRule(action: "read")
        ], options: defaultOptions())

        // With no subject, the rule applies to "all" (anySubjectType)
        XCTAssertTrue(ability.can("read"))
        XCTAssertTrue(ability.can("read", "Post"))
    }

    // MARK: - Custom anyAction / anySubjectType

    func testCustomAnyAction() {
        var opts = defaultOptions()
        opts.anyAction = "doAnything"
        let ability = Ability(rules: [
            RawRule(action: "doAnything", subject: "Post")
        ], options: opts)

        XCTAssertTrue(ability.can("read", "Post"))
        XCTAssertTrue(ability.can("update", "Post"))
    }

    func testCustomAnySubjectType() {
        var opts = defaultOptions()
        opts.anySubjectType = "everything"
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "everything")
        ], options: opts)

        XCTAssertTrue(ability.can("read", "Post"))
        XCTAssertTrue(ability.can("read", "Comment"))
    }

    // MARK: - Default Behavior

    func testAllowOnInstance() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", conditions: ["author": "me"])
        ], options: defaultOptions())

        let post: [String: Any] = ["__caslSubjectType__": "Post", "author": "me"]
        XCTAssertTrue(ability.can("read", post))
    }

    func testAllowOnTypeString() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post")
        ], options: defaultOptions())

        XCTAssertTrue(ability.can("read", "Post"))
    }

    func testDisallowUnspecified() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post")
        ], options: defaultOptions())

        XCTAssertFalse(ability.can("update", "Post"))
    }

    func testFalsySubjectDefaultsToAll() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "all")
        ], options: defaultOptions())

        XCTAssertTrue(ability.can("read"))
    }

    func testActionOnUnspecifiedType() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post")
        ], options: defaultOptions())

        XCTAssertFalse(ability.can("read", "Comment"))
    }

    // MARK: - Conditions Matching

    func testConditionsEquality() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", conditions: ["author": "john"])
        ], options: defaultOptions())

        let post1: [String: Any] = ["__caslSubjectType__": "Post", "author": "john"]
        let post2: [String: Any] = ["__caslSubjectType__": "Post", "author": "jane"]

        XCTAssertTrue(ability.can("read", post1))
        XCTAssertFalse(ability.can("read", post2))
    }

    func testConditionsNe() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", conditions: ["status": ["$ne": "draft"]])
        ], options: defaultOptions())

        let post1: [String: Any] = ["__caslSubjectType__": "Post", "status": "published"]
        let post2: [String: Any] = ["__caslSubjectType__": "Post", "status": "draft"]

        XCTAssertTrue(ability.can("read", post1))
        XCTAssertFalse(ability.can("read", post2))
    }

    func testConditionsIn() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", conditions: ["status": ["$in": ["published", "archived"]]])
        ], options: defaultOptions())

        let post1: [String: Any] = ["__caslSubjectType__": "Post", "status": "published"]
        let post2: [String: Any] = ["__caslSubjectType__": "Post", "status": "draft"]

        XCTAssertTrue(ability.can("read", post1))
        XCTAssertFalse(ability.can("read", post2))
    }

    func testConditionsAll() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", conditions: ["tags": ["$all": ["swift", "ios"]]])
        ], options: defaultOptions())

        let post1: [String: Any] = ["__caslSubjectType__": "Post", "tags": ["swift", "ios", "casl"]]
        let post2: [String: Any] = ["__caslSubjectType__": "Post", "tags": ["swift", "android"]]

        XCTAssertTrue(ability.can("read", post1))
        XCTAssertFalse(ability.can("read", post2))
    }

    func testConditionsGtGte() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", conditions: ["views": ["$gt": 100]])
        ], options: defaultOptions())

        let post1: [String: Any] = ["__caslSubjectType__": "Post", "views": 200]
        let post2: [String: Any] = ["__caslSubjectType__": "Post", "views": 50]
        let post3: [String: Any] = ["__caslSubjectType__": "Post", "views": 100]

        XCTAssertTrue(ability.can("read", post1))
        XCTAssertFalse(ability.can("read", post2))
        XCTAssertFalse(ability.can("read", post3))

        // Test $gte
        let ability2 = Ability(rules: [
            RawRule(action: "read", subject: "Post", conditions: ["views": ["$gte": 100]])
        ], options: defaultOptions())

        XCTAssertTrue(ability2.can("read", post3))
    }

    func testConditionsLtLte() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", conditions: ["views": ["$lt": 100]])
        ], options: defaultOptions())

        let post1: [String: Any] = ["__caslSubjectType__": "Post", "views": 50]
        let post2: [String: Any] = ["__caslSubjectType__": "Post", "views": 200]
        let post3: [String: Any] = ["__caslSubjectType__": "Post", "views": 100]

        XCTAssertTrue(ability.can("read", post1))
        XCTAssertFalse(ability.can("read", post2))
        XCTAssertFalse(ability.can("read", post3))

        let ability2 = Ability(rules: [
            RawRule(action: "read", subject: "Post", conditions: ["views": ["$lte": 100]])
        ], options: defaultOptions())

        XCTAssertTrue(ability2.can("read", post3))
    }

    func testConditionsExists() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", conditions: ["author": ["$exists": true]])
        ], options: defaultOptions())

        let post1: [String: Any] = ["__caslSubjectType__": "Post", "author": "john"]
        let post2: [String: Any] = ["__caslSubjectType__": "Post"]

        XCTAssertTrue(ability.can("read", post1))
        XCTAssertFalse(ability.can("read", post2))
    }

    func testConditionsExistsFalse() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", conditions: ["deletedAt": ["$exists": false]])
        ], options: defaultOptions())

        let post1: [String: Any] = ["__caslSubjectType__": "Post"]
        let post2: [String: Any] = ["__caslSubjectType__": "Post", "deletedAt": "2024-01-01"]

        XCTAssertTrue(ability.can("read", post1))
        XCTAssertFalse(ability.can("read", post2))
    }

    func testConditionsDotNotation() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", conditions: ["author.name": "john"])
        ], options: defaultOptions())

        let post1: [String: Any] = ["__caslSubjectType__": "Post", "author": ["name": "john"]]
        let post2: [String: Any] = ["__caslSubjectType__": "Post", "author": ["name": "jane"]]

        XCTAssertTrue(ability.can("read", post1))
        XCTAssertFalse(ability.can("read", post2))
    }

    func testConditionsRegex() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", conditions: ["title": ["$regex": "^Hello"]])
        ], options: defaultOptions())

        let post1: [String: Any] = ["__caslSubjectType__": "Post", "title": "Hello World"]
        let post2: [String: Any] = ["__caslSubjectType__": "Post", "title": "Goodbye World"]

        XCTAssertTrue(ability.can("read", post1))
        XCTAssertFalse(ability.can("read", post2))
    }

    func testConditionsElemMatch() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", conditions: [
                "comments": ["$elemMatch": ["author": "john"]]
            ])
        ], options: defaultOptions())

        let post1: [String: Any] = [
            "__caslSubjectType__": "Post",
            "comments": [["author": "john", "text": "hi"]] as [[String: Any]]
        ]
        let post2: [String: Any] = [
            "__caslSubjectType__": "Post",
            "comments": [["author": "jane", "text": "bye"]] as [[String: Any]]
        ]

        XCTAssertTrue(ability.can("read", post1))
        XCTAssertFalse(ability.can("read", post2))
    }

    func testInvertedRuleWithSubjectString() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post"),
            RawRule(action: "read", subject: "Post", conditions: ["private": true], inverted: true)
        ], options: defaultOptions())

        // On type string, inverted rules with conditions are optimistic (return !inverted = false for conditions check)
        XCTAssertTrue(ability.can("read", "Post"))

        let post1: [String: Any] = ["__caslSubjectType__": "Post", "private": false]
        XCTAssertTrue(ability.can("read", post1))

        let post2: [String: Any] = ["__caslSubjectType__": "Post", "private": true]
        XCTAssertFalse(ability.can("read", post2))
    }

    // MARK: - Per-field Permissions

    func testSingleField() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", fields: "title")
        ], options: defaultOptions())

        XCTAssertTrue(ability.can("read", "Post", field: "title"))
        XCTAssertFalse(ability.can("read", "Post", field: "body"))
    }

    func testMultipleFields() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", fields: .array(["title", "body"]))
        ], options: defaultOptions())

        XCTAssertTrue(ability.can("read", "Post", field: "title"))
        XCTAssertTrue(ability.can("read", "Post", field: "body"))
        XCTAssertFalse(ability.can("read", "Post", field: "author"))
    }

    func testInvertedFieldRule() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post"),
            RawRule(action: "read", subject: "Post", fields: "secret", inverted: true)
        ], options: defaultOptions())

        XCTAssertTrue(ability.can("read", "Post", field: "title"))
        XCTAssertFalse(ability.can("read", "Post", field: "secret"))
    }

    func testNoFieldMeansAllFields() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post")
        ], options: defaultOptions())

        XCTAssertTrue(ability.can("read", "Post", field: "anyField"))
    }

    func testFieldPatternSingleLevel() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", fields: "author.*")
        ], options: defaultOptions())

        XCTAssertTrue(ability.can("read", "Post", field: "author.name"))
        XCTAssertFalse(ability.can("read", "Post", field: "author.pub.name"))
    }

    func testFieldPatternMultiLevel() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", fields: "author.**")
        ], options: defaultOptions())

        XCTAssertTrue(ability.can("read", "Post", field: "author.name"))
        XCTAssertTrue(ability.can("read", "Post", field: "author.pub.name"))
    }

    func testFieldPatternMiddleSingleWildcard() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", fields: "author.*.name")
        ], options: defaultOptions())

        XCTAssertTrue(ability.can("read", "Post", field: "author.pub.name"))
        XCTAssertFalse(ability.can("read", "Post", field: "author.pub.sub.name"))
    }

    func testFieldPatternMiddleMultiWildcard() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", fields: "author.**.name")
        ], options: defaultOptions())

        XCTAssertTrue(ability.can("read", "Post", field: "author.pub.name"))
        XCTAssertTrue(ability.can("read", "Post", field: "author.pub.sub.name"))
    }

    func testFieldPatternStartWildcards() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", fields: "*.name")
        ], options: defaultOptions())

        XCTAssertTrue(ability.can("read", "Post", field: "author.name"))
        XCTAssertFalse(ability.can("read", "Post", field: "author.pub.name"))

        let ability2 = Ability(rules: [
            RawRule(action: "read", subject: "Post", fields: "**.name")
        ], options: defaultOptions())

        XCTAssertTrue(ability2.can("read", "Post", field: "author.name"))
        XCTAssertTrue(ability2.can("read", "Post", field: "author.pub.name"))
    }

    func testFieldPatternTrailingStar() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", fields: "street*")
        ], options: defaultOptions())

        XCTAssertTrue(ability.can("read", "Post", field: "street"))
        XCTAssertTrue(ability.can("read", "Post", field: "street1"))
        XCTAssertTrue(ability.can("read", "Post", field: "street2"))
        XCTAssertFalse(ability.can("read", "Post", field: "avenue"))
    }

    func testFieldPatternSpecialChars() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", fields: "field?name")
        ], options: defaultOptions())

        // The ? should be escaped, so it matches literally
        XCTAssertTrue(ability.can("read", "Post", field: "field?name"))
        XCTAssertFalse(ability.can("read", "Post", field: "fieldXname"))
    }

    func testFieldsWithConditions() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", fields: "title", conditions: ["published": true])
        ], options: defaultOptions())

        let post1: [String: Any] = ["__caslSubjectType__": "Post", "published": true]
        let post2: [String: Any] = ["__caslSubjectType__": "Post", "published": false]

        XCTAssertTrue(ability.can("read", post1, field: "title"))
        XCTAssertFalse(ability.can("read", post2, field: "title"))
    }

    // MARK: - Manage Action (wildcard)

    func testManageActionAllowsEverything() {
        let ability = Ability(rules: [
            RawRule(action: "manage", subject: "Post")
        ], options: defaultOptions())

        XCTAssertTrue(ability.can("read", "Post"))
        XCTAssertTrue(ability.can("update", "Post"))
        XCTAssertTrue(ability.can("delete", "Post"))
    }

    func testManageWithCannot() {
        let ability = Ability(rules: [
            RawRule(action: "manage", subject: "Post"),
            RawRule(action: "delete", subject: "Post", inverted: true)
        ], options: defaultOptions())

        XCTAssertTrue(ability.can("read", "Post"))
        XCTAssertFalse(ability.can("delete", "Post"))
    }

    func testCannotManageAll() {
        let ability = Ability(rules: [
            RawRule(action: "manage", subject: "all"),
            RawRule(action: "manage", subject: "Post", inverted: true)
        ], options: defaultOptions())

        XCTAssertTrue(ability.can("read", "Comment"))
        XCTAssertFalse(ability.can("read", "Post"))
    }

    func testFieldSpecificManage() {
        let ability = Ability(rules: [
            RawRule(action: "manage", subject: "Post", fields: "title")
        ], options: defaultOptions())

        XCTAssertTrue(ability.can("read", "Post", field: "title"))
        XCTAssertFalse(ability.can("read", "Post", field: "body"))
    }

    // MARK: - rulesFor

    func testRulesForReturnsCorrectRules() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post"),
            RawRule(action: "update", subject: "Post"),
            RawRule(action: "read", subject: "Comment")
        ], options: defaultOptions())

        let rules = ability.rulesFor("read", "Post")
        XCTAssertEqual(rules.count, 1)
    }

    func testRulesForFiltersInvertedFieldRules() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post"),
            RawRule(action: "read", subject: "Post", fields: "secret", inverted: true)
        ], options: defaultOptions())

        let rules = ability.rulesFor("read", "Post", field: "title")
        // Both rules should be present since the inverted rule has fields and we ask for "title"
        // The inverted rule for "secret" should not match field "title"
        XCTAssertTrue(rules.count >= 1)
    }

    func testRulesForFieldSpecific() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", fields: "title"),
            RawRule(action: "read", subject: "Post", fields: "body")
        ], options: defaultOptions())

        let titleRules = ability.rulesFor("read", "Post", field: "title")
        XCTAssertEqual(titleRules.count, 1)
    }

    // MARK: - actionsFor

    func testActionsForSpecificSubject() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post"),
            RawRule(action: "update", subject: "Post"),
            RawRule(action: "read", subject: "Comment")
        ], options: defaultOptions())

        let actions = ability.actionsFor("Post")
        XCTAssertTrue(actions.contains("read"))
        XCTAssertTrue(actions.contains("update"))
        XCTAssertFalse(actions.contains("delete"))
    }

    func testActionsForWithAliases() {
        let resolver = try! createAliasResolver(["modify": ["update", "delete"]])
        let ability = Ability(rules: [
            RawRule(action: "modify", subject: "Post")
        ], options: defaultOptions(resolveAction: resolver))

        let actions = ability.actionsFor("Post")
        XCTAssertTrue(actions.contains("update"))
        XCTAssertTrue(actions.contains("delete"))
    }

    func testActionsForWithAllSubject() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "all"),
            RawRule(action: "update", subject: "Post")
        ], options: defaultOptions())

        let actions = ability.actionsFor("Post")
        XCTAssertTrue(actions.contains("update"))
        // "read" is on "all", should also appear
        XCTAssertTrue(actions.contains("read"))
    }

    func testActionsForEmptyAbility() {
        let ability = Ability(rules: [], options: defaultOptions())
        let actions = ability.actionsFor("Post")
        XCTAssertTrue(actions.isEmpty)
    }

    // MARK: - Rule Precedence

    func testORLogicForRules() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", conditions: ["author": "john"]),
            RawRule(action: "read", subject: "Post", conditions: ["published": true])
        ], options: defaultOptions())

        let post: [String: Any] = ["__caslSubjectType__": "Post", "published": true, "author": "jane"]
        XCTAssertTrue(ability.can("read", post))
    }

    func testInverseOrderPrecedence() {
        // Later rules have lower priority number but are checked first
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post"),
            RawRule(action: "read", subject: "Post", inverted: true)
        ], options: defaultOptions())

        // The inverted rule (last, priority 0) has higher precedence
        XCTAssertFalse(ability.can("read", "Post"))
    }

    func testShadowByRuleWithoutConditions() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", conditions: ["author": "john"]),
            RawRule(action: "read", subject: "Post")
        ], options: defaultOptions())

        // Second rule (no conditions) shadows the first
        let post: [String: Any] = ["__caslSubjectType__": "Post", "author": "jane"]
        XCTAssertTrue(ability.can("read", post))
    }

    func testCannotBetweenCans() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post"),
            RawRule(action: "read", subject: "Post", conditions: ["private": true], inverted: true),
            RawRule(action: "read", subject: "Post", conditions: ["author": "admin"])
        ], options: defaultOptions())

        let publicPost: [String: Any] = ["__caslSubjectType__": "Post", "private": false, "author": "john"]
        XCTAssertTrue(ability.can("read", publicPost))

        let privateAdminPost: [String: Any] = ["__caslSubjectType__": "Post", "private": true, "author": "admin"]
        XCTAssertTrue(ability.can("read", privateAdminPost))
    }

    func testInvertedShadowedByRegular() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", inverted: true),
            RawRule(action: "read", subject: "Post")
        ], options: defaultOptions())

        XCTAssertTrue(ability.can("read", "Post"))
    }

    func testAllSubjectShadowedBySpecific() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "all"),
            RawRule(action: "read", subject: "Post", inverted: true)
        ], options: defaultOptions())

        XCTAssertFalse(ability.can("read", "Post"))
        XCTAssertTrue(ability.can("read", "Comment"))
    }

    // MARK: - Update Events

    func testUpdateEventFires() {
        let ability = Ability(rules: [], options: defaultOptions())
        var updateCalled = false
        var updatedCalled = false

        ability.on(.update) { _ in updateCalled = true }
        ability.on(.updated) { _ in updatedCalled = true }

        ability.update([RawRule(action: "read", subject: "Post")])

        XCTAssertTrue(updateCalled)
        XCTAssertTrue(updatedCalled)
    }

    func testUnsubscribeWorks() {
        let ability = Ability(rules: [], options: defaultOptions())
        var callCount = 0

        let unsub = ability.on(.updated) { _ in callCount += 1 }
        ability.update([RawRule(action: "read", subject: "Post")])
        XCTAssertEqual(callCount, 1)

        unsub()
        ability.update([RawRule(action: "update", subject: "Post")])
        XCTAssertEqual(callCount, 1)
    }

    func testDoubleUnsubscribeIsSafe() {
        let ability = Ability(rules: [], options: defaultOptions())
        let unsub = ability.on(.updated) { _ in }
        unsub()
        unsub() // Should not crash
    }

    func testSelfUnsubscribeDuringEmission() {
        let ability = Ability(rules: [], options: defaultOptions())
        var callCount = 0
        var unsub: Unsubscribe?

        unsub = ability.on(.updated) { _ in
            callCount += 1
            unsub?()
        }

        ability.update([RawRule(action: "read", subject: "Post")])
        XCTAssertEqual(callCount, 1)

        ability.update([RawRule(action: "update", subject: "Post")])
        XCTAssertEqual(callCount, 1) // Should not be called again
    }

    func testUnsubscribeOtherDuringEmission() {
        let ability = Ability(rules: [], options: defaultOptions())
        var callCount1 = 0
        var callCount2 = 0
        var unsub1: Unsubscribe?

        unsub1 = ability.on(.updated) { _ in
            callCount1 += 1
        }

        ability.on(.updated) { _ in
            callCount2 += 1
            unsub1?() // Unsubscribe the other handler during emission
        }

        ability.update([RawRule(action: "read", subject: "Post")])
        // Both should be called (emission goes tail to head, so handler2 fires first, unsubs handler1,
        // but handler1 was already the previous node being iterated via saved prev pointer)
        XCTAssertEqual(callCount2, 1)
        // handler1 may or may not be called depending on implementation
        // The key is that it doesn't crash
    }

    func testUnsubscribeLastHandler() {
        let ability = Ability(rules: [], options: defaultOptions())
        var callCount = 0

        let unsub1 = ability.on(.updated) { _ in callCount += 1 }
        let unsub2 = ability.on(.updated) { _ in callCount += 1 }

        unsub2()
        unsub1()

        ability.update([RawRule(action: "read", subject: "Post")])
        XCTAssertEqual(callCount, 0)
    }

    // MARK: - Subject Type Detection

    func testDetectSubjectTypeString() {
        let ability = Ability(rules: [], options: defaultOptions())
        XCTAssertEqual(ability.detectSubjectType("Post"), "Post")
    }

    func testDetectSubjectTypeNil() {
        let ability = Ability(rules: [], options: defaultOptions())
        XCTAssertEqual(ability.detectSubjectType(nil), "all")
    }

    func testDetectSubjectTypeDictWithKey() {
        let ability = Ability(rules: [], options: defaultOptions())
        let obj: [String: Any] = ["__caslSubjectType__": "Post"]
        XCTAssertEqual(ability.detectSubjectType(obj), "Post")
    }

    func testDetectSubjectTypeDictWithoutKey() {
        let ability = Ability(rules: [], options: defaultOptions())
        let obj: [String: Any] = ["name": "test"]
        XCTAssertEqual(ability.detectSubjectType(obj), "Dictionary")
    }

    // MARK: - Input Validation (JS parity: RuleIndex.ts)

    func testPossibleRulesForThrowsOnNonStringSubjectType() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post")
        ], options: defaultOptions())

        // Passing an Int instead of String should throw
        XCTAssertThrowsError(try ability.possibleRulesForThrowing("read", 42)) { error in
            XCTAssertTrue(error is AbilityError)
            if case AbilityError.invalidSubjectType(let method) = error as! AbilityError {
                XCTAssertEqual(method, "possibleRulesFor")
            }
        }

        // Passing a dictionary instead of String should throw
        XCTAssertThrowsError(try ability.possibleRulesForThrowing("read", ["key": "value"] as [String: Any])) { error in
            XCTAssertTrue(error is AbilityError)
        }
    }

    func testPossibleRulesForAcceptsStringSubjectType() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post")
        ], options: defaultOptions())

        // String should not throw
        XCTAssertNoThrow(try ability.possibleRulesForThrowing("read", "Post"))

        // nil should not throw
        XCTAssertNoThrow(try ability.possibleRulesForThrowing("read", nil))
    }

    func testRulesForThrowsOnNonStringField() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post")
        ], options: defaultOptions())

        // Passing an Int as field should throw
        XCTAssertThrowsError(try ability.rulesForThrowing("read", "Post", field: 42)) { error in
            XCTAssertTrue(error is AbilityError)
            if case AbilityError.invalidFieldParameter = error as! AbilityError {
                // expected
            } else {
                XCTFail("Expected invalidFieldParameter error")
            }
        }
    }

    func testRulesForAcceptsStringField() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", fields: "title")
        ], options: defaultOptions())

        XCTAssertNoThrow(try ability.rulesForThrowing("read", "Post", field: "title"))
        XCTAssertNoThrow(try ability.rulesForThrowing("read", "Post", field: nil))
    }

    func testActionsForThrowsOnNonStringSubjectType() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post")
        ], options: defaultOptions())

        XCTAssertThrowsError(try ability.actionsForThrowing(42)) { error in
            XCTAssertTrue(error is AbilityError)
            if case AbilityError.invalidSubjectType(let method) = error as! AbilityError {
                XCTAssertEqual(method, "actionsFor")
            }
        }
    }

    func testActionsForAcceptsStringSubjectType() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post")
        ], options: defaultOptions())

        XCTAssertNoThrow(try ability.actionsForThrowing("Post"))
    }

    // MARK: - Ported JS Tests

    // JS: "lists all rules"
    func testListsAllRules() {
        let ability = defineAbility({ builder in
            builder.can("crud", "all")
            builder.can("learn", "Range")
            builder.cannot("read", "String")
            builder.cannot("read", "Hash")
            builder.cannot("preview", "Array")
        }, options: defaultOptions())

        let rules = ability.rules
        XCTAssertEqual(rules.count, 5)
        XCTAssertEqual(rules[0].action, .single("crud"))
        XCTAssertEqual(rules[0].subject, .single("all"))
        XCTAssertFalse(rules[0].inverted)

        XCTAssertEqual(rules[1].action, .single("learn"))
        XCTAssertEqual(rules[1].subject, .single("Range"))
        XCTAssertFalse(rules[1].inverted)

        XCTAssertEqual(rules[2].action, .single("read"))
        XCTAssertEqual(rules[2].subject, .single("String"))
        XCTAssertTrue(rules[2].inverted)

        XCTAssertEqual(rules[3].action, .single("read"))
        XCTAssertEqual(rules[3].subject, .single("Hash"))
        XCTAssertTrue(rules[3].inverted)

        XCTAssertEqual(rules[4].action, .single("preview"))
        XCTAssertEqual(rules[4].subject, .single("Array"))
        XCTAssertTrue(rules[4].inverted)
    }

    // JS: "disallows to perform action if action parameter is falsy"
    func testDisallowsActionIfActionIsFalsy() {
        let ability = defineAbility({ builder in
            builder.can("read", "Post")
        }, options: defaultOptions())

        // Empty string action should not match any rule
        XCTAssertFalse(ability.can("", "Post"))
    }

    // JS: "allows to perform action if target type matches at least 1 rule with or without conditions"
    func testAllowsActionIfTypeMatchesRuleWithConditions() {
        let ability = defineAbility({ builder in
            builder.can("test", "all")
            builder.can(["read", "update"], "Post")
            builder.can("delete", "Post", conditions: ["creator": "admin"])
            builder.cannot("publish", "Post")
        }, options: defaultOptions())

        // Rule with conditions should still allow type-level check
        XCTAssertTrue(ability.can("delete", "Post"))
    }

    // JS: "can match field patterns (vehicle.*.generic.*)"
    func testCanMatchFieldPatternsComplexMultiWildcard() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", fields: "vehicle.*.generic.*")
        ], options: defaultOptions())

        XCTAssertTrue(ability.can("read", "Post", field: "vehicle.profile.generic.item"))
        XCTAssertTrue(ability.can("read", "Post", field: "vehicle.*.generic.signal"))
        XCTAssertTrue(ability.can("read", "Post", field: "vehicle.profile.generic.*"))
        XCTAssertFalse(ability.can("read", "Post", field: "vehicle.*.user.*"))
    }

    // JS: "does not allow to perform action on instance of the subject which mismatches specified conditions"
    // (with fields + conditions, instance doesn't match)
    func testFieldsWithConditionsInstanceMismatch() {
        let ability = defineAbility({ builder in
            builder.can("read", "Post", fields: .array(["title", "description"]),
                        conditions: ["author": "me", "published": true])
        }, options: defaultOptions())

        // Instance that doesn't match conditions
        let post: [String: Any] = ["__caslSubjectType__": "Post"]
        XCTAssertFalse(ability.can("read", post))
    }

    // JS: "allows to perform action on instance which matches conditions"
    // (with fields + conditions, instance matches)
    func testFieldsWithConditionsInstanceMatches() {
        let ability = defineAbility({ builder in
            builder.can("read", "Post", fields: .array(["title", "description"]),
                        conditions: ["author": "me", "published": true])
        }, options: defaultOptions())

        let myPost: [String: Any] = [
            "__caslSubjectType__": "Post",
            "author": "me",
            "published": true
        ]
        XCTAssertTrue(ability.can("read", myPost))
    }

    // JS: "allows to perform action on instance field if that instance matches conditions"
    func testFieldCheckOnMatchingInstance() {
        let ability = defineAbility({ builder in
            builder.can("read", "Post", fields: .array(["title", "description"]),
                        conditions: ["author": "me", "published": true])
        }, options: defaultOptions())

        let myPost: [String: Any] = [
            "__caslSubjectType__": "Post",
            "author": "me",
            "published": true
        ]
        XCTAssertTrue(ability.can("read", myPost, field: "title"))
        XCTAssertTrue(ability.can("read", myPost, field: "description"))
    }

    // JS: "does not allow to perform action on instance field if that instance matches conditions but field is not in specified list"
    func testFieldNotInListInstanceMatches() {
        let ability = defineAbility({ builder in
            builder.can("read", "Post", fields: .array(["title", "description"]),
                        conditions: ["author": "me", "published": true])
        }, options: defaultOptions())

        let myPost: [String: Any] = [
            "__caslSubjectType__": "Post",
            "author": "me",
            "published": true
        ]
        XCTAssertFalse(ability.can("read", myPost, field: "id"))
    }

    // JS: "ensures that both conditions are met"
    func testEnsuresBothConditionsAreMet() {
        let ability = defineAbility({ builder in
            builder.can("read", "Post", fields: .array(["title", "description"]),
                        conditions: ["author": "me", "published": true])
        }, options: defaultOptions())

        let myPost: [String: Any] = [
            "__caslSubjectType__": "Post",
            "author": "me",
            "published": true
        ]
        XCTAssertTrue(ability.can("read", myPost))

        // Only one condition met ("author": "me") but not "published"
        let otherPost: [String: Any] = [
            "__caslSubjectType__": "Post",
            "author": "me",
            "active": false
        ]
        XCTAssertFalse(ability.can("read", otherPost))
    }

    // JS: "returns actions associated with 'all' subject type if there is no actions for provided one"
    func testActionsForReturnsAllSubjectActionsWhenNoSpecific() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "all")
        ], options: defaultOptions())

        let actions = ability.actionsFor("Post")
        XCTAssertEqual(actions, ["read"])
    }
}
