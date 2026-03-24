package cx.smile.CASL;

import org.junit.Test;

import java.util.*;
import java.util.function.Predicate;

import static org.junit.Assert.*;

public class ConditionsMatcherTest {

    private static Map<String, Object> mapOf(Object... keyValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put((String) keyValues[i], keyValues[i + 1]);
        }
        return map;
    }

    // ---- $eq ----

    @Test
    public void testEqString() {
        Predicate<Map<String, Object>> matcher = ConditionsMatcher.match(mapOf("name", "John"));
        assertTrue(matcher.test(mapOf("name", "John")));
        assertFalse(matcher.test(mapOf("name", "Jane")));
    }

    @Test
    public void testEqNumber() {
        Predicate<Map<String, Object>> matcher = ConditionsMatcher.match(mapOf("age", 25));
        assertTrue(matcher.test(mapOf("age", 25)));
        assertFalse(matcher.test(mapOf("age", 30)));
    }

    @Test
    public void testEqDouble() {
        Predicate<Map<String, Object>> matcher = ConditionsMatcher.match(mapOf("score", 9.5));
        assertTrue(matcher.test(mapOf("score", 9.5)));
        assertFalse(matcher.test(mapOf("score", 8.0)));
    }

    @Test
    public void testEqBoolean() {
        Predicate<Map<String, Object>> matcher = ConditionsMatcher.match(mapOf("active", true));
        assertTrue(matcher.test(mapOf("active", true)));
        assertFalse(matcher.test(mapOf("active", false)));
    }

    @Test
    public void testEqOnList() {
        // If field is a list, check if any element equals
        Predicate<Map<String, Object>> matcher = ConditionsMatcher.match(mapOf("tags", "java"));
        assertTrue(matcher.test(mapOf("tags", Arrays.asList("java", "python"))));
        assertFalse(matcher.test(mapOf("tags", Arrays.asList("python", "ruby"))));
    }

    @Test
    public void testExplicitEqOperator() {
        Predicate<Map<String, Object>> matcher = ConditionsMatcher.match(mapOf("name", mapOf("$eq", "John")));
        assertTrue(matcher.test(mapOf("name", "John")));
        assertFalse(matcher.test(mapOf("name", "Jane")));
    }

    // ---- $ne ----

    @Test
    public void testNe() {
        Predicate<Map<String, Object>> matcher = ConditionsMatcher.match(mapOf("name", mapOf("$ne", "John")));
        assertFalse(matcher.test(mapOf("name", "John")));
        assertTrue(matcher.test(mapOf("name", "Jane")));
    }

    // ---- $gt ----

    @Test
    public void testGt() {
        Predicate<Map<String, Object>> matcher = ConditionsMatcher.match(mapOf("age", mapOf("$gt", 18)));
        assertTrue(matcher.test(mapOf("age", 19)));
        assertFalse(matcher.test(mapOf("age", 18)));
        assertFalse(matcher.test(mapOf("age", 17)));
    }

    // ---- $gte ----

    @Test
    public void testGte() {
        Predicate<Map<String, Object>> matcher = ConditionsMatcher.match(mapOf("age", mapOf("$gte", 18)));
        assertTrue(matcher.test(mapOf("age", 19)));
        assertTrue(matcher.test(mapOf("age", 18)));
        assertFalse(matcher.test(mapOf("age", 17)));
    }

    // ---- $lt ----

    @Test
    public void testLt() {
        Predicate<Map<String, Object>> matcher = ConditionsMatcher.match(mapOf("age", mapOf("$lt", 18)));
        assertTrue(matcher.test(mapOf("age", 17)));
        assertFalse(matcher.test(mapOf("age", 18)));
        assertFalse(matcher.test(mapOf("age", 19)));
    }

    // ---- $lte ----

    @Test
    public void testLte() {
        Predicate<Map<String, Object>> matcher = ConditionsMatcher.match(mapOf("age", mapOf("$lte", 18)));
        assertTrue(matcher.test(mapOf("age", 17)));
        assertTrue(matcher.test(mapOf("age", 18)));
        assertFalse(matcher.test(mapOf("age", 19)));
    }

    // ---- $in ----

    @Test
    public void testIn() {
        Predicate<Map<String, Object>> matcher = ConditionsMatcher.match(
                mapOf("status", mapOf("$in", Arrays.asList("active", "pending"))));
        assertTrue(matcher.test(mapOf("status", "active")));
        assertTrue(matcher.test(mapOf("status", "pending")));
        assertFalse(matcher.test(mapOf("status", "closed")));
    }

    @Test
    public void testInWithListField() {
        Predicate<Map<String, Object>> matcher = ConditionsMatcher.match(
                mapOf("tags", mapOf("$in", Arrays.asList("java", "python"))));
        assertTrue(matcher.test(mapOf("tags", Arrays.asList("java", "ruby"))));
        assertFalse(matcher.test(mapOf("tags", Arrays.asList("ruby", "go"))));
    }

    // ---- $nin ----

    @Test
    public void testNin() {
        Predicate<Map<String, Object>> matcher = ConditionsMatcher.match(
                mapOf("status", mapOf("$nin", Arrays.asList("active", "pending"))));
        assertFalse(matcher.test(mapOf("status", "active")));
        assertTrue(matcher.test(mapOf("status", "closed")));
    }

    // ---- $all ----

    @Test
    public void testAll() {
        Predicate<Map<String, Object>> matcher = ConditionsMatcher.match(
                mapOf("tags", mapOf("$all", Arrays.asList("java", "python"))));
        assertTrue(matcher.test(mapOf("tags", Arrays.asList("java", "python", "ruby"))));
        assertFalse(matcher.test(mapOf("tags", Arrays.asList("java", "ruby"))));
        assertFalse(matcher.test(mapOf("tags", "java")));
    }

    // ---- $size ----

    @Test
    public void testSize() {
        Predicate<Map<String, Object>> matcher = ConditionsMatcher.match(
                mapOf("tags", mapOf("$size", 3)));
        assertTrue(matcher.test(mapOf("tags", Arrays.asList("a", "b", "c"))));
        assertFalse(matcher.test(mapOf("tags", Arrays.asList("a", "b"))));
    }

    // ---- $regex ----

    @Test
    public void testRegex() {
        Predicate<Map<String, Object>> matcher = ConditionsMatcher.match(
                mapOf("title", mapOf("$regex", "^Hello")));
        assertTrue(matcher.test(mapOf("title", "Hello World")));
        assertFalse(matcher.test(mapOf("title", "Goodbye World")));
    }

    @Test
    public void testRegexWithOptions() {
        Map<String, Object> regexCond = new LinkedHashMap<>();
        regexCond.put("$regex", "hello");
        regexCond.put("$options", "i");
        Predicate<Map<String, Object>> matcher = ConditionsMatcher.match(mapOf("title", regexCond));
        assertTrue(matcher.test(mapOf("title", "Hello World")));
        assertTrue(matcher.test(mapOf("title", "HELLO World")));
    }

    // ---- $exists ----

    @Test
    public void testExistsTrue() {
        Predicate<Map<String, Object>> matcher = ConditionsMatcher.match(
                mapOf("email", mapOf("$exists", true)));
        assertTrue(matcher.test(mapOf("email", "test@test.com")));
        assertFalse(matcher.test(mapOf("name", "John")));
    }

    @Test
    public void testExistsFalse() {
        Predicate<Map<String, Object>> matcher = ConditionsMatcher.match(
                mapOf("email", mapOf("$exists", false)));
        assertFalse(matcher.test(mapOf("email", "test@test.com")));
        assertTrue(matcher.test(mapOf("name", "John")));
    }

    // ---- $elemMatch ----

    @Test
    public void testElemMatch() {
        Predicate<Map<String, Object>> matcher = ConditionsMatcher.match(
                mapOf("authors", mapOf("$elemMatch", mapOf("name", "John"))));

        assertTrue(matcher.test(mapOf("authors", Arrays.asList(mapOf("name", "John")))));
        assertTrue(matcher.test(mapOf("authors", Arrays.asList(mapOf("name", "Jane"), mapOf("name", "John")))));
        assertFalse(matcher.test(mapOf("authors", Arrays.asList(mapOf("name", "Jane")))));
        assertFalse(matcher.test(mapOf("authors", "not a list")));
    }

    @Test
    public void testElemMatchWithScalarArrayGt() {
        // $elemMatch with operator conditions on scalar arrays
        Predicate<Map<String, Object>> matcher = ConditionsMatcher.match(
                mapOf("scores", mapOf("$elemMatch", mapOf("$gt", 5))));

        assertTrue(matcher.test(mapOf("scores", Arrays.asList(1, 2, 10))));
        assertFalse(matcher.test(mapOf("scores", Arrays.asList(1, 2, 3))));
    }

    @Test
    public void testElemMatchWithScalarArrayLt() {
        Predicate<Map<String, Object>> matcher = ConditionsMatcher.match(
                mapOf("values", mapOf("$elemMatch", mapOf("$lt", 3))));

        assertTrue(matcher.test(mapOf("values", Arrays.asList(1, 5, 10))));
        assertFalse(matcher.test(mapOf("values", Arrays.asList(5, 10, 20))));
    }

    @Test
    public void testElemMatchWithScalarArrayMultipleOperators() {
        // $elemMatch with multiple operators: element must satisfy ALL
        Map<String, Object> elemCond = new LinkedHashMap<>();
        elemCond.put("$gt", 3);
        elemCond.put("$lt", 8);
        Predicate<Map<String, Object>> matcher = ConditionsMatcher.match(
                mapOf("values", mapOf("$elemMatch", elemCond)));

        assertTrue(matcher.test(mapOf("values", Arrays.asList(1, 5, 10))));  // 5 matches
        assertFalse(matcher.test(mapOf("values", Arrays.asList(1, 2, 10)))); // no element in (3,8)
    }

    @Test
    public void testElemMatchWithScalarArrayEq() {
        Predicate<Map<String, Object>> matcher = ConditionsMatcher.match(
                mapOf("tags", mapOf("$elemMatch", mapOf("$eq", "swift"))));
        assertTrue(matcher.test(mapOf("tags", Arrays.asList("swift", "ios"))));
        assertFalse(matcher.test(mapOf("tags", Arrays.asList("java", "android"))));
    }

    @Test
    public void testElemMatchWithScalarArrayNe() {
        // $ne in $elemMatch: at least one element != "draft"
        Predicate<Map<String, Object>> matcher = ConditionsMatcher.match(
                mapOf("statuses", mapOf("$elemMatch", mapOf("$ne", "draft"))));
        assertTrue(matcher.test(mapOf("statuses", Arrays.asList("draft", "published"))));
        assertFalse(matcher.test(mapOf("statuses", Arrays.asList("draft", "draft"))));
    }

    @Test
    public void testElemMatchWithEmptyScalarArray() {
        Predicate<Map<String, Object>> matcher = ConditionsMatcher.match(
                mapOf("scores", mapOf("$elemMatch", mapOf("$gt", 5))));
        assertFalse(matcher.test(mapOf("scores", new ArrayList<>())));
    }

    @Test
    public void testElemMatchWithScalarArrayNotAList() {
        Predicate<Map<String, Object>> matcher = ConditionsMatcher.match(
                mapOf("scores", mapOf("$elemMatch", mapOf("$gt", 5))));

        assertFalse(matcher.test(mapOf("scores", "not a list")));
        assertFalse(matcher.test(mapOf("scores", 10)));
    }

    @Test
    public void testElemMatchWithMixedArray() {
        // Array with both Maps and scalars - only Maps match sub-conditions without operators
        Predicate<Map<String, Object>> matcher = ConditionsMatcher.match(
                mapOf("items", mapOf("$elemMatch", mapOf("name", "John"))));

        assertTrue(matcher.test(mapOf("items", Arrays.asList(mapOf("name", "John"), 42))));
        assertFalse(matcher.test(mapOf("items", Arrays.asList(42, "hello"))));
    }

    // ---- Custom operators ----

    @Test
    public void testCustomOperator() {
        try {
            ConditionsMatcher.registerOperator("$startsWith",
                    (fieldValue, opValue) -> fieldValue != null && fieldValue.toString().startsWith(opValue.toString()));

            Predicate<Map<String, Object>> matcher = ConditionsMatcher.match(
                    mapOf("name", mapOf("$startsWith", "Jo")));

            assertTrue(matcher.test(mapOf("name", "John")));
            assertFalse(matcher.test(mapOf("name", "Jane")));
        } finally {
            ConditionsMatcher.clearCustomOperators();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterOperatorWithoutDollarPrefix() {
        ConditionsMatcher.registerOperator("noDollar", (a, b) -> true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterOperatorNullHandler() {
        ConditionsMatcher.registerOperator("$test", null);
    }

    // ---- Dot notation ----

    @Test
    public void testDotNotation() {
        Predicate<Map<String, Object>> matcher = ConditionsMatcher.match(
                mapOf("address.city", "NYC"));

        assertTrue(matcher.test(mapOf("address", mapOf("city", "NYC"))));
        assertFalse(matcher.test(mapOf("address", mapOf("city", "LA"))));
    }

    @Test
    public void testDotNotationArrayIndex() {
        Predicate<Map<String, Object>> matcher = ConditionsMatcher.match(
                mapOf("items.0", "first"));

        assertTrue(matcher.test(mapOf("items", Arrays.asList("first", "second"))));
        assertFalse(matcher.test(mapOf("items", Arrays.asList("second", "first"))));
    }

    @Test
    public void testDotNotationArrayIndexExists() {
        Predicate<Map<String, Object>> matcher = ConditionsMatcher.match(
                mapOf("authors.0", mapOf("$exists", false)));

        assertTrue(matcher.test(mapOf("authors", new ArrayList<>())));
        assertFalse(matcher.test(mapOf("authors", Arrays.asList("me", "someoneelse"))));
    }

    // ---- Multiple conditions (AND) ----

    @Test
    public void testMultipleConditions() {
        Predicate<Map<String, Object>> matcher = ConditionsMatcher.match(
                mapOf("name", "John", "age", mapOf("$gt", 18)));

        assertTrue(matcher.test(mapOf("name", "John", "age", 25)));
        assertFalse(matcher.test(mapOf("name", "John", "age", 15)));
        assertFalse(matcher.test(mapOf("name", "Jane", "age", 25)));
    }

    // ---- Cross-type numeric comparison ----

    @Test
    public void testCrossTypeNumericComparison() {
        Predicate<Map<String, Object>> matcher = ConditionsMatcher.match(
                mapOf("count", 5));
        assertTrue(matcher.test(mapOf("count", 5)));
        assertTrue(matcher.test(mapOf("count", 5L)));
        assertTrue(matcher.test(mapOf("count", 5.0)));
    }

    // ---- $and ----

    @Test
    public void testAnd() {
        Map<String, Object> conditions = new LinkedHashMap<>();
        conditions.put("$and", Arrays.asList(
                mapOf("name", "John"),
                mapOf("age", mapOf("$gt", 18))
        ));
        Predicate<Map<String, Object>> matcher = ConditionsMatcher.match(conditions);
        assertTrue(matcher.test(mapOf("name", "John", "age", 25)));
        assertFalse(matcher.test(mapOf("name", "John", "age", 15)));
        assertFalse(matcher.test(mapOf("name", "Jane", "age", 25)));
    }

    @Test
    public void testAndAllMustMatch() {
        Map<String, Object> conditions = new LinkedHashMap<>();
        conditions.put("$and", Arrays.asList(
                mapOf("status", "active"),
                mapOf("role", "admin"),
                mapOf("age", mapOf("$gte", 21))
        ));
        Predicate<Map<String, Object>> matcher = ConditionsMatcher.match(conditions);
        assertTrue(matcher.test(mapOf("status", "active", "role", "admin", "age", 25)));
        assertFalse(matcher.test(mapOf("status", "active", "role", "user", "age", 25)));
    }

    // ---- $or ----

    @Test
    public void testOr() {
        Map<String, Object> conditions = new LinkedHashMap<>();
        conditions.put("$or", Arrays.asList(
                mapOf("status", "active"),
                mapOf("status", "pending")
        ));
        Predicate<Map<String, Object>> matcher = ConditionsMatcher.match(conditions);
        assertTrue(matcher.test(mapOf("status", "active")));
        assertTrue(matcher.test(mapOf("status", "pending")));
        assertFalse(matcher.test(mapOf("status", "closed")));
    }

    @Test
    public void testOrAtLeastOneMustMatch() {
        Map<String, Object> conditions = new LinkedHashMap<>();
        conditions.put("$or", Arrays.asList(
                mapOf("role", "admin"),
                mapOf("age", mapOf("$gte", 21))
        ));
        Predicate<Map<String, Object>> matcher = ConditionsMatcher.match(conditions);
        assertTrue(matcher.test(mapOf("role", "admin", "age", 18)));
        assertTrue(matcher.test(mapOf("role", "user", "age", 25)));
        assertFalse(matcher.test(mapOf("role", "user", "age", 18)));
    }

    // ---- $not ----

    @Test
    public void testNot() {
        Map<String, Object> conditions = new LinkedHashMap<>();
        conditions.put("$not", mapOf("status", "active"));
        Predicate<Map<String, Object>> matcher = ConditionsMatcher.match(conditions);
        assertFalse(matcher.test(mapOf("status", "active")));
        assertTrue(matcher.test(mapOf("status", "closed")));
    }

    @Test
    public void testNotWithOperators() {
        Map<String, Object> conditions = new LinkedHashMap<>();
        conditions.put("$not", mapOf("age", mapOf("$gt", 18)));
        Predicate<Map<String, Object>> matcher = ConditionsMatcher.match(conditions);
        assertTrue(matcher.test(mapOf("age", 15)));
        assertTrue(matcher.test(mapOf("age", 18)));
        assertFalse(matcher.test(mapOf("age", 25)));
    }

    // ---- Combined logical operators ----

    @Test
    public void testAndWithOr() {
        Map<String, Object> conditions = new LinkedHashMap<>();
        conditions.put("$and", Arrays.asList(
                mapOf("name", "John"),
                new LinkedHashMap<String, Object>() {{
                    put("$or", Arrays.asList(
                            mapOf("age", 25),
                            mapOf("age", 30)
                    ));
                }}
        ));
        Predicate<Map<String, Object>> matcher = ConditionsMatcher.match(conditions);
        assertTrue(matcher.test(mapOf("name", "John", "age", 25)));
        assertTrue(matcher.test(mapOf("name", "John", "age", 30)));
        assertFalse(matcher.test(mapOf("name", "John", "age", 20)));
        assertFalse(matcher.test(mapOf("name", "Jane", "age", 25)));
    }

    // ---- __caslSubjectType__ filtering ----

    @Test
    public void testCaslSubjectTypeIsIgnoredInConditions() {
        // If __caslSubjectType__ appears in conditions, it should be skipped
        Predicate<Map<String, Object>> matcher = ConditionsMatcher.match(
                mapOf("__caslSubjectType__", "Post", "status", "active"));
        assertTrue(matcher.test(mapOf("status", "active")));
        assertFalse(matcher.test(mapOf("status", "closed")));
    }

    @Test
    public void testCaslSubjectTypeInObjectDoesNotInterfere() {
        Predicate<Map<String, Object>> matcher = ConditionsMatcher.match(
                mapOf("status", "active"));
        assertTrue(matcher.test(mapOf("__caslSubjectType__", "Post", "status", "active")));
    }

    // ---- $regex multiline and dotall flags ----

    @Test
    public void testRegexMultilineFlag() {
        Map<String, Object> regexCond = new LinkedHashMap<>();
        regexCond.put("$regex", "^world");
        regexCond.put("$options", "m");
        Predicate<Map<String, Object>> matcher = ConditionsMatcher.match(mapOf("text", regexCond));
        // With MULTILINE, ^ matches at start of each line
        assertTrue(matcher.test(mapOf("text", "hello\nworld")));
        assertFalse(matcher.test(mapOf("text", "hello world")));
    }

    @Test
    public void testRegexDotallFlag() {
        Map<String, Object> regexCond = new LinkedHashMap<>();
        regexCond.put("$regex", "hello.world");
        regexCond.put("$options", "s");
        Predicate<Map<String, Object>> matcher = ConditionsMatcher.match(mapOf("text", regexCond));
        // With DOTALL, . matches newline
        assertTrue(matcher.test(mapOf("text", "hello\nworld")));
        // Without DOTALL, dot doesn't match newline, but we have DOTALL here
        assertTrue(matcher.test(mapOf("text", "hello world")));
    }

    @Test
    public void testRegexCombinedFlags() {
        Map<String, Object> regexCond = new LinkedHashMap<>();
        regexCond.put("$regex", "^hello.world");
        regexCond.put("$options", "ims");
        Predicate<Map<String, Object>> matcher = ConditionsMatcher.match(mapOf("text", regexCond));
        assertTrue(matcher.test(mapOf("text", "HELLO\nworld")));
    }
}
