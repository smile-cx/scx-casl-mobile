package cx.smile.CASL;

import org.junit.After;
import org.junit.Test;

import java.util.*;
import java.util.function.Function;

import static org.junit.Assert.*;

public class ForbiddenErrorTest {

    @After
    public void resetDefaultMessage() {
        ForbiddenError.setDefaultMessage(
                (Function<ForbiddenError, String>) e -> "Cannot execute \"" + e.getAction() + "\" on \"" + e.getSubjectType() + "\"");
    }

    @Test
    public void testThrowUnlessCanThrowsOnDisallowedAction() {
        Ability ability = AbilityBuilder.defineAbility(b -> b.can("read", "Post"));
        ForbiddenError error = ForbiddenError.from(ability);

        try {
            error.throwUnlessCan("archive", "Post");
            fail("Expected ForbiddenError");
        } catch (ForbiddenError e) {
            // expected
        }
    }

    @Test
    public void testThrowUnlessCanDoesNotThrowOnAllowedAction() {
        Ability ability = AbilityBuilder.defineAbility(b -> b.can("read", "Post"));
        ForbiddenError error = ForbiddenError.from(ability);

        try {
            error.throwUnlessCan("read", "Post");
        } catch (ForbiddenError e) {
            fail("Should not throw for allowed action");
        }
    }

    @Test
    public void testErrorContextInfo() {
        Ability ability = AbilityBuilder.defineAbility(b -> b.can("read", "Post"));
        ForbiddenError error = ForbiddenError.from(ability);

        try {
            error.throwUnlessCan("archive", "Post");
            fail("Expected ForbiddenError");
        } catch (ForbiddenError e) {
            assertEquals("archive", e.getAction());
            assertEquals("Post", e.getSubject());
            assertEquals("Post", e.getSubjectType());
        }
    }

    @Test
    public void testReasonFromRule() {
        String noCardMessage = "No credit card provided";
        Ability ability = AbilityBuilder.defineAbility(b -> b.can("read", "Post"));

        List<RawRule> newRules = new ArrayList<>();
        newRules.add(RawRule.builder()
                .action("update")
                .subject("Post")
                .inverted(true)
                .reason(noCardMessage)
                .build());
        ability.update(newRules);

        ForbiddenError error = ForbiddenError.from(ability);

        try {
            error.throwUnlessCan("update", "Post");
            fail("Expected ForbiddenError");
        } catch (ForbiddenError e) {
            assertEquals(noCardMessage, e.getMessage());
        }
    }

    @Test
    public void testCustomMessage() {
        String message = "My custom error message";
        Ability ability = AbilityBuilder.defineAbility(b -> b.can("read", "Post"));
        ForbiddenError error = ForbiddenError.from(ability);

        try {
            error.setMessage(message).throwUnlessCan("update", "Post");
            fail("Expected ForbiddenError");
        } catch (ForbiddenError e) {
            assertEquals(message, e.getMessage());
        }
    }

    @Test
    public void testSetDefaultMessageWithFunction() {
        ForbiddenError.setDefaultMessage(err -> "errror -> " + err.getAction() + "-" + err.getSubjectType());

        Ability ability = AbilityBuilder.defineAbility(b -> b.can("read", "Post"));
        ForbiddenError error = ForbiddenError.from(ability);

        try {
            error.throwUnlessCan("update", "Post");
            fail("Expected ForbiddenError");
        } catch (ForbiddenError e) {
            assertEquals("errror -> update-Post", e.getMessage());
        }
    }

    @Test
    public void testSetDefaultMessageWithString() {
        ForbiddenError.setDefaultMessage("Access denied");

        Ability ability = AbilityBuilder.defineAbility(b -> b.can("read", "Post"));
        ForbiddenError error = ForbiddenError.from(ability);

        try {
            error.throwUnlessCan("update", "Post");
            fail("Expected ForbiddenError");
        } catch (ForbiddenError e) {
            assertEquals("Access denied", e.getMessage());
        }
    }

    @Test
    public void testFromFactoryStoresAbilityReference() {
        Ability ability = AbilityBuilder.defineAbility(b -> b.can("read", "Post"));
        ForbiddenError error = ForbiddenError.from(ability);
        assertSame(ability, error.getAbility());
    }

    @Test
    public void testToStringMatchesMessage() {
        Ability ability = AbilityBuilder.defineAbility(b -> b.can("read", "Post"));
        ForbiddenError error = ForbiddenError.from(ability);
        try {
            error.throwUnlessCan("delete", "Post");
            fail("Expected ForbiddenError");
        } catch (ForbiddenError e) {
            assertEquals("Cannot execute \"delete\" on \"Post\"", e.getMessage());
        }
    }

    @Test
    public void testDefaultErrorMessage() {
        Ability ability = AbilityBuilder.defineAbility(b -> b.can("read", "Post"));
        ForbiddenError error = ForbiddenError.from(ability);

        try {
            error.throwUnlessCan("update", "Post");
            fail("Expected ForbiddenError");
        } catch (ForbiddenError e) {
            assertEquals("Cannot execute \"update\" on \"Post\"", e.getMessage());
        }
    }
}
