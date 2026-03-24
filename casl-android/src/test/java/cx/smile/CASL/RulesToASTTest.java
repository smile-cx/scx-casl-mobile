package cx.smile.CASL;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class RulesToASTTest {

    private static Map<String, Object> mapOf(Object... keyValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put((String) keyValues[i], keyValues[i + 1]);
        }
        return map;
    }

    @Test
    public void testReturnsNullWhenNotAllowed() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            // no rules for read Post
        });

        Object result = RulesToAST.rulesToAST(ability, "read", "Post");
        assertNull(result);
    }

    @Test
    public void testReturnsASTForSingleCondition() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("read", "Post", mapOf("author", "me"));
        });

        Object result = RulesToAST.rulesToAST(ability, "read", "Post");
        assertNotNull(result);
        assertTrue(result instanceof ConditionNode);
        ConditionNode node = (ConditionNode) result;
        assertEquals("author", node.getField());
        assertEquals("$eq", node.getOperator());
        assertEquals("me", node.getValue());
    }

    @Test
    public void testReturnsOrASTForMultipleConditions() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("read", "Post", mapOf("author", "me"));
            b.can("read", "Post", mapOf("published", true));
        });

        Object result = RulesToAST.rulesToAST(ability, "read", "Post");
        assertNotNull(result);
        assertTrue(result instanceof CompoundNode);
        CompoundNode node = (CompoundNode) result;
        assertEquals("or", node.getOperator());
        assertEquals(2, node.getChildren().size());
    }

    @Test
    public void testReturnsASTWithInvertedRulesAsAnd() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("read", "Post", mapOf("author", "me"));
            b.cannot("read", "Post", mapOf("hidden", true));
        });

        Object result = RulesToAST.rulesToAST(ability, "read", "Post");
        assertNotNull(result);
        assertTrue(result instanceof CompoundNode);
        CompoundNode node = (CompoundNode) result;
        assertEquals("and", node.getOperator());
        // Should have the inverted "not" node and the or node
        assertTrue(node.getChildren().size() >= 2);
    }

    @Test
    public void testReturnsEmptyAndForRuleWithoutConditions() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("read", "Post");
        });

        Object result = RulesToAST.rulesToAST(ability, "read", "Post");
        assertNotNull(result);
        // Rule without conditions means "allow all" - returns empty compound
        assertTrue(result instanceof CompoundNode);
        CompoundNode node = (CompoundNode) result;
        assertEquals("and", node.getOperator());
        assertTrue(node.getChildren().isEmpty());
    }

    @Test
    public void testReturnsASTWithOperatorConditions() {
        Ability ability = AbilityBuilder.defineAbility(b -> {
            b.can("read", "Post", mapOf("views", mapOf("$gt", 10)));
        });

        Object result = RulesToAST.rulesToAST(ability, "read", "Post");
        assertNotNull(result);
        assertTrue(result instanceof ConditionNode);
        ConditionNode node = (ConditionNode) result;
        assertEquals("views", node.getField());
        assertEquals("$gt", node.getOperator());
        assertEquals(10, node.getValue());
    }

    @Test
    public void testInvertedRuleWrappedInNot() {
        Object ast = RulesToAST.ruleToAST(
                new Rule(RawRule.builder().action("read").subject("Post")
                                .conditions(mapOf("hidden", true)).inverted(true).build(),
                        RuleOptions.builder()
                                .conditionsMatcher(ConditionsMatcher::match)
                                .fieldMatcher(FieldMatcher::match)
                                .resolveAction(a -> a)
                                .build(),
                        0));

        assertTrue(ast instanceof CompoundNode);
        CompoundNode node = (CompoundNode) ast;
        assertEquals("not", node.getOperator());
        assertEquals(1, node.getChildren().size());
    }
}
