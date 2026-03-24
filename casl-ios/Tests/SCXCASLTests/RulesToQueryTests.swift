import XCTest
@testable import SCXCASL

final class RulesToQueryTests: XCTestCase {

    private func defaultOptions() -> AbilityOptions {
        return AbilityOptions(
            conditionsMatcher: ConditionsMatcher.match,
            fieldMatcher: FieldMatcher.match
        )
    }

    private struct QueryRule: Equatable {
        let conditions: [String: Any]?
        let inverted: Bool

        static func == (lhs: QueryRule, rhs: QueryRule) -> Bool {
            if lhs.inverted != rhs.inverted { return false }
            switch (lhs.conditions, rhs.conditions) {
            case (nil, nil): return true
            case (nil, _), (_, nil): return false
            case (let a?, let b?):
                guard let ad = try? JSONSerialization.data(withJSONObject: a, options: .sortedKeys),
                      let bd = try? JSONSerialization.data(withJSONObject: b, options: .sortedKeys) else { return false }
                return ad == bd
            }
        }
    }

    private func convert(_ rule: Rule) -> QueryRule {
        return QueryRule(conditions: rule.conditions, inverted: rule.inverted)
    }

    func testRegularRuleWithoutConditionsReturnsEmpty() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post")
        ], options: defaultOptions())

        let query: AbilityQuery<QueryRule>? = rulesToQuery(ability, action: "read", subjectType: "Post", convert: convert)
        XCTAssertNotNil(query)
        XCTAssertNil(query?.or)
        XCTAssertNil(query?.and)
    }

    func testEmptyAbilityReturnsNil() {
        let ability = Ability(rules: [], options: defaultOptions())

        let query: AbilityQuery<QueryRule>? = rulesToQuery(ability, action: "read", subjectType: "Post", convert: convert)
        XCTAssertNil(query)
    }

    func testOnlyInvertedReturnsNil() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", conditions: ["private": true], inverted: true)
        ], options: defaultOptions())

        let query: AbilityQuery<QueryRule>? = rulesToQuery(ability, action: "read", subjectType: "Post", convert: convert)
        XCTAssertNil(query)
    }

    func testInvertedWithoutConditionsReturnsNil() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", inverted: true)
        ], options: defaultOptions())

        let query: AbilityQuery<QueryRule>? = rulesToQuery(ability, action: "read", subjectType: "Post", convert: convert)
        XCTAssertNil(query)
    }

    func testORForRegularRules() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", conditions: ["author": "john"]),
            RawRule(action: "read", subject: "Post", conditions: ["published": true])
        ], options: defaultOptions())

        let query: AbilityQuery<QueryRule>? = rulesToQuery(ability, action: "read", subjectType: "Post", convert: convert)
        XCTAssertNotNil(query)
        XCTAssertNotNil(query?.or)
        XCTAssertEqual(query?.or?.count, 2)
        XCTAssertNil(query?.and)
    }

    func testANDForInvertedRules() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post"),
            RawRule(action: "read", subject: "Post", conditions: ["private": true], inverted: true),
            RawRule(action: "read", subject: "Post", conditions: ["draft": true], inverted: true)
        ], options: defaultOptions())

        let query: AbilityQuery<QueryRule>? = rulesToQuery(ability, action: "read", subjectType: "Post", convert: convert)
        XCTAssertNotNil(query)
        XCTAssertNotNil(query?.and)
        XCTAssertEqual(query?.and?.count, 2)
    }

    func testMixedORAndAND() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", conditions: ["author": "john"]),
            RawRule(action: "read", subject: "Post", conditions: ["private": true], inverted: true)
        ], options: defaultOptions())

        let query: AbilityQuery<QueryRule>? = rulesToQuery(ability, action: "read", subjectType: "Post", convert: convert)
        XCTAssertNotNil(query)
        XCTAssertNotNil(query?.or)
        XCTAssertNotNil(query?.and)
    }

    func testInvertedBeforeRegularWithoutConditionsReturnsEmpty() {
        // Inverted rule defined BEFORE regular rule without conditions
        // In rule checking order, regular (last defined) is checked first → returns empty
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", conditions: ["private": true], inverted: true),
            RawRule(action: "read", subject: "Post")
        ], options: defaultOptions())

        let query: AbilityQuery<QueryRule>? = rulesToQuery(ability, action: "read", subjectType: "Post", convert: convert)
        XCTAssertNotNil(query)
        // Regular rule without conditions encountered first → empty result
        XCTAssertNil(query?.or)
        XCTAssertNil(query?.and)
    }

    func testIgnoresInvertedWithFields() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post"),
            RawRule(action: "read", subject: "Post", fields: "secret", inverted: true)
        ], options: defaultOptions())

        let query: AbilityQuery<QueryRule>? = rulesToQuery(ability, action: "read", subjectType: "Post", convert: convert)
        XCTAssertNotNil(query)
        // The inverted rule with fields should be ignored
        XCTAssertNil(query?.and)
    }

    // MARK: - Ported JS Tests

    // JS: "returns empty $or part if at least one regular rule does not have conditions"
    func testReturnsEmptyIfAtLeastOneRegularRuleHasNoConditions() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", conditions: ["author": 123]),
            RawRule(action: "read", subject: "Post")
        ], options: defaultOptions())

        let query: AbilityQuery<QueryRule>? = rulesToQuery(ability, action: "read", subjectType: "Post", convert: convert)
        XCTAssertNotNil(query)
        XCTAssertNil(query?.or)
        XCTAssertNil(query?.and)
    }

    // JS: "returns empty $or part if rule with conditions defined last"
    func testReturnsEmptyIfRuleWithConditionsDefinedLast() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post"),
            RawRule(action: "read", subject: "Post", conditions: ["author": 123])
        ], options: defaultOptions())

        let query: AbilityQuery<QueryRule>? = rulesToQuery(ability, action: "read", subjectType: "Post", convert: convert)
        XCTAssertNotNil(query)
        XCTAssertNil(query?.or)
        XCTAssertNil(query?.and)
    }

    // JS: "returns null if at least one inverted rule does not have conditions even if direct rule exists"
    func testReturnsNilIfInvertedWithoutConditionsEvenIfDirectRuleExists() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", conditions: ["public": true]),
            RawRule(action: "read", subject: "Post", conditions: ["author": 321], inverted: true),
            RawRule(action: "read", subject: "Post", inverted: true)
        ], options: defaultOptions())

        let query: AbilityQuery<QueryRule>? = rulesToQuery(ability, action: "read", subjectType: "Post", convert: convert)
        XCTAssertNil(query)
    }

    // JS: "should ignore inverted rules with fields and without conditions"
    func testIgnoresInvertedRulesWithFieldsAndWithoutConditions() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", conditions: ["author": 123]),
            RawRule(action: "read", subject: "Post", fields: "description", inverted: true)
        ], options: defaultOptions())

        let query: AbilityQuery<QueryRule>? = rulesToQuery(ability, action: "read", subjectType: "Post", convert: convert)
        XCTAssertNotNil(query)
        XCTAssertNotNil(query?.or)
        XCTAssertEqual(query?.or?.count, 1)
        // The inverted rule with fields should be ignored
        XCTAssertNil(query?.and)
    }

    // MARK: - rulesToAST

    func testRulesToASTReturnsNilWhenNotAllowed() {
        let ability = Ability(rules: [], options: defaultOptions())
        let ast = rulesToAST(ability, action: "read", subjectType: "Post")
        XCTAssertNil(ast)
    }

    func testRulesToASTReturnsEmptyAndWhenAllowedWithoutConditions() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post")
        ], options: defaultOptions())

        let ast = rulesToAST(ability, action: "read", subjectType: "Post")
        XCTAssertNotNil(ast)
        XCTAssertEqual(ast, .compound(op: "and", children: []))
    }

    func testRulesToASTWithSingleCondition() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", conditions: ["author": "john"])
        ], options: defaultOptions())

        let ast = rulesToAST(ability, action: "read", subjectType: "Post")
        XCTAssertNotNil(ast)
        // Single rule -> OR with one child -> the condition node
        if case .compound(let op, let children) = ast! {
            XCTAssertEqual(op, "or")
            XCTAssertEqual(children.count, 1)
            if case .condition(let field, let condOp, let value) = children[0] {
                XCTAssertEqual(field, "author")
                XCTAssertEqual(condOp, "$eq")
                XCTAssertEqual(value, .string("john"))
            } else {
                XCTFail("Expected condition node")
            }
        } else {
            XCTFail("Expected compound OR node")
        }
    }

    func testRulesToASTWithMultipleConditions() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", conditions: ["author": "john"]),
            RawRule(action: "read", subject: "Post", conditions: ["published": true])
        ], options: defaultOptions())

        let ast = rulesToAST(ability, action: "read", subjectType: "Post")
        XCTAssertNotNil(ast)
        if case .compound(let op, let children) = ast! {
            XCTAssertEqual(op, "or")
            XCTAssertEqual(children.count, 2)
        } else {
            XCTFail("Expected compound OR node")
        }
    }

    func testRulesToASTWithInvertedRule() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post"),
            RawRule(action: "read", subject: "Post", conditions: ["private": true], inverted: true)
        ], options: defaultOptions())

        let ast = rulesToAST(ability, action: "read", subjectType: "Post")
        XCTAssertNotNil(ast)
        // Should have AND with the inverted rule
        if case .compound(let op, let children) = ast! {
            XCTAssertEqual(op, "and")
            XCTAssertEqual(children.count, 1)
            // The inverted rule becomes compound("not", ...)
            if case .compound(let innerOp, _) = children[0] {
                XCTAssertEqual(innerOp, "not")
            } else {
                XCTFail("Expected compound NOT node")
            }
        } else {
            XCTFail("Expected compound AND node")
        }
    }

    func testRulesToASTWithOperatorCondition() {
        let ability = Ability(rules: [
            RawRule(action: "read", subject: "Post", conditions: ["views": ["$gt": 100]])
        ], options: defaultOptions())

        let ast = rulesToAST(ability, action: "read", subjectType: "Post")
        XCTAssertNotNil(ast)
        if case .compound(let op, let children) = ast! {
            XCTAssertEqual(op, "or")
            XCTAssertEqual(children.count, 1)
            if case .condition(let field, let condOp, let value) = children[0] {
                XCTAssertEqual(field, "views")
                XCTAssertEqual(condOp, "$gt")
                XCTAssertEqual(value, .int(100))
            }
        }
    }

    func testRuleToASTHelperReturnsNilForNoConditions() {
        let rule = Rule(rawRule: RawRule(action: "read", subject: "Post"), options: RuleOptions())
        let ast = ruleToAST(rule)
        XCTAssertNil(ast)
    }

    func testRuleToASTHelperWrapsInvertedInNot() {
        let rule = Rule(rawRule: RawRule(action: "read", subject: "Post", conditions: ["x": 1], inverted: true), options: RuleOptions())
        let ast = ruleToAST(rule)
        XCTAssertNotNil(ast)
        if case .compound(let op, _) = ast! {
            XCTAssertEqual(op, "not")
        } else {
            XCTFail("Expected compound NOT node")
        }
    }
}
