package cx.smile.CASL;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class SubjectHelperTest {

    @Test
    public void testSubjectSetsType() {
        Map<String, Object> object = new HashMap<>();
        Map<String, Object> result = CaslUtils.subject("Article", object);
        assertEquals("Article", CaslUtils.detectSubjectType(result));
    }

    @Test(expected = IllegalStateException.class)
    public void testRedefineThrows() {
        Map<String, Object> object = CaslUtils.subject("Article", new HashMap<>());
        CaslUtils.subject("User", object);
    }

    @Test
    public void testSameTypeOk() {
        Map<String, Object> object = CaslUtils.subject("Article", new HashMap<>());
        Map<String, Object> result = CaslUtils.subject("Article", object);
        assertEquals("Article", CaslUtils.detectSubjectType(result));
    }

    @Test
    public void testIgnoresNullSubject() {
        // Should not throw
        Map<String, Object> result = CaslUtils.subject("Test", null);
        assertNull(result);
    }

    @Test
    public void testDetectSubjectTypeReturnsType() {
        Map<String, Object> object = CaslUtils.subject("Article", new HashMap<>());
        assertEquals("Article", CaslUtils.detectSubjectType(object));
    }

    @Test
    public void testDetectSubjectTypeReturnsNullForPlainMap() {
        Map<String, Object> object = new HashMap<>();
        assertNull(CaslUtils.detectSubjectType(object));
    }

    @Test
    public void testSubjectMutatesOriginalMap() {
        Map<String, Object> object = new HashMap<>();
        object.put("name", "test");
        Map<String, Object> result = CaslUtils.subject("Article", object);
        // Should return the same reference (mutated in place)
        assertSame(object, result);
        assertEquals("Article", object.get("__caslSubjectType__"));
    }
}
