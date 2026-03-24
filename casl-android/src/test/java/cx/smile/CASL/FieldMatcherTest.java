package cx.smile.CASL;

import org.junit.Test;

import java.util.*;
import java.util.function.Predicate;

import static org.junit.Assert.*;

public class FieldMatcherTest {

    // ---- Exact match ----

    @Test
    public void testExactMatch() {
        Predicate<String> matcher = FieldMatcher.match(Arrays.asList("title", "description"));
        assertTrue(matcher.test("title"));
        assertTrue(matcher.test("description"));
        assertFalse(matcher.test("author"));
    }

    @Test
    public void testExactMatchSingle() {
        Predicate<String> matcher = FieldMatcher.match(Collections.singletonList("title"));
        assertTrue(matcher.test("title"));
        assertFalse(matcher.test("description"));
    }

    // ---- Single star (author.*) ----

    @Test
    public void testSingleStarAtEnd() {
        Predicate<String> matcher = FieldMatcher.match(Collections.singletonList("author.*"));
        assertTrue(matcher.test("author"));
        assertTrue(matcher.test("author.*"));
        assertTrue(matcher.test("author.name"));
        assertTrue(matcher.test("author.age"));
        assertFalse(matcher.test("author.publication.name"));
    }

    // ---- Double star (author.**) ----

    @Test
    public void testDoubleStarAtEnd() {
        Predicate<String> matcher = FieldMatcher.match(Collections.singletonList("author.**"));
        assertTrue(matcher.test("author"));
        assertTrue(matcher.test("author.**"));
        assertTrue(matcher.test("author.name"));
        assertTrue(matcher.test("author.age"));
        assertTrue(matcher.test("author.publication.name"));
    }

    // ---- Star in middle (author.*.name) ----

    @Test
    public void testStarInMiddle() {
        Predicate<String> matcher = FieldMatcher.match(Collections.singletonList("author.*.name"));
        assertFalse(matcher.test("author"));
        assertTrue(matcher.test("author.*.name"));
        assertTrue(matcher.test("author.publication.name"));
        assertFalse(matcher.test("author.publication.startDate"));
        assertFalse(matcher.test("author.publication.country.name"));
    }

    // ---- Double star in middle (author.**.name) ----

    @Test
    public void testDoubleStarInMiddle() {
        Predicate<String> matcher = FieldMatcher.match(Collections.singletonList("author.**.name"));
        assertFalse(matcher.test("author"));
        assertTrue(matcher.test("author.**.name"));
        assertTrue(matcher.test("author.publication.name"));
        assertFalse(matcher.test("author.publication.startDate"));
        assertTrue(matcher.test("author.publication.country.name"));
    }

    // ---- Star at start (*.name) ----

    @Test
    public void testStarAtStart() {
        Predicate<String> matcher = FieldMatcher.match(Collections.singletonList("*.name"));
        assertTrue(matcher.test("author.name"));
        assertTrue(matcher.test("*.name"));
        assertFalse(matcher.test("author.publication.name"));
    }

    // ---- Double star at start (**.name) ----

    @Test
    public void testDoubleStarAtStart() {
        Predicate<String> matcher = FieldMatcher.match(Collections.singletonList("**.name"));
        assertTrue(matcher.test("author.name"));
        assertTrue(matcher.test("**.name"));
        assertTrue(matcher.test("author.publication.name"));
    }

    // ---- Trailing star (street*) ----

    @Test
    public void testTrailingStar() {
        Predicate<String> matcher = FieldMatcher.match(Collections.singletonList("author.address.street*"));
        assertTrue(matcher.test("author.address.street"));
        assertTrue(matcher.test("author.address.street1"));
        assertTrue(matcher.test("author.address.street2"));
        assertFalse(matcher.test("author.address"));
    }

    // ---- Special regex characters ----

    @Test
    public void testSpecialRegexChars() {
        Predicate<String> matcher = FieldMatcher.match(Collections.singletonList("author?.address+.street*"));
        assertTrue(matcher.test("author?.address+.street"));
        assertTrue(matcher.test("author?.address+.street1"));
        assertTrue(matcher.test("author?.address+.street2"));
        assertFalse(matcher.test("author?.address+"));
    }

    // ---- Complex patterns ----

    @Test
    public void testComplexMiddleStar() {
        Predicate<String> matcher = FieldMatcher.match(Collections.singletonList("vehicle.*.generic.*"));
        assertTrue(matcher.test("vehicle.profile.generic.item"));
        assertTrue(matcher.test("vehicle.*.generic.signal"));
        assertTrue(matcher.test("vehicle.profile.generic.*"));
        assertFalse(matcher.test("vehicle.*.user.*"));
    }

    // ---- Multiple patterns ----

    @Test
    public void testMultiplePatterns() {
        Predicate<String> matcher = FieldMatcher.match(Arrays.asList("title", "author.*"));
        assertTrue(matcher.test("title"));
        assertTrue(matcher.test("author.name"));
        assertFalse(matcher.test("description"));
    }

    // ---- No wildcards uses contains ----

    @Test
    public void testNoWildcardsUsesContains() {
        Predicate<String> matcher = FieldMatcher.match(Arrays.asList("title", "description", "author"));
        assertTrue(matcher.test("title"));
        assertTrue(matcher.test("description"));
        assertTrue(matcher.test("author"));
        assertFalse(matcher.test("id"));
    }
}
