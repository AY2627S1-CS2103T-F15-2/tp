package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class CommandResultTest {
    @Test
    public void constructors_preserveLegacyFlagsAndDisplay() {
        CommandResult legacy = new CommandResult("feedback");
        assertEquals(DisplayRequest.legacyPersons(), legacy.getDisplayRequest());
        assertFalse(legacy.isShowHelp());
        assertFalse(legacy.isExit());
        for (boolean help : new boolean[] {false, true}) {
            for (boolean exit : new boolean[] {false, true}) {
                CommandResult result = new CommandResult("feedback", help, exit);
                assertEquals("feedback", result.getFeedbackToUser());
                assertEquals(help, result.isShowHelp());
                assertEquals(exit, result.isExit());
                assertEquals(DisplayRequest.legacyPersons(), result.getDisplayRequest());
            }
        }
    }

    @Test
    public void explicitDisplay_preservesFeedbackAndValueSemantics() {
        CommandResult result = new CommandResult("feedback", DisplayRequest.members());
        CommandResult equal = new CommandResult("feedback", DisplayRequest.members());
        assertEquals("feedback", result.getFeedbackToUser());
        assertEquals(DisplayRequest.members(), result.getDisplayRequest());
        assertFalse(result.isShowHelp());
        assertFalse(result.isExit());
        assertEquals(result, equal);
        assertEquals(result.hashCode(), equal.hashCode());
        assertNotEquals(result, new CommandResult("feedback", DisplayRequest.equipment()));
        assertNotEquals(result, new CommandResult("feedback"));
        assertEquals(new CommandResult("feedback"),
                new CommandResult("feedback", DisplayRequest.legacyPersons()));
    }

    @Test
    public void constructors_nullRequiredFields_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new CommandResult(null));
        assertThrows(NullPointerException.class, () -> new CommandResult(null, true, true));
        assertThrows(NullPointerException.class, () -> new CommandResult(null, DisplayRequest.members()));
        assertThrows(NullPointerException.class, () -> new CommandResult("feedback", null));
    }

    @Test
    public void equals() {
        CommandResult commandResult = new CommandResult("feedback");

        // same values -> returns true
        assertTrue(commandResult.equals(new CommandResult("feedback")));
        assertTrue(commandResult.equals(new CommandResult("feedback", false, false)));

        // same object -> returns true
        assertTrue(commandResult.equals(commandResult));

        // null -> returns false
        assertFalse(commandResult.equals(null));

        // different types -> returns false
        assertFalse(commandResult.equals(0.5f));

        // different feedbackToUser value -> returns false
        assertFalse(commandResult.equals(new CommandResult("different")));

        // different showHelp value -> returns false
        assertFalse(commandResult.equals(new CommandResult("feedback", true, false)));

        // different exit value -> returns false
        assertFalse(commandResult.equals(new CommandResult("feedback", false, true)));
    }

    @Test
    public void hashcode() {
        CommandResult commandResult = new CommandResult("feedback");

        // same values -> returns same hashcode
        assertEquals(commandResult.hashCode(), new CommandResult("feedback").hashCode());

        // different feedbackToUser value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("different").hashCode());

        // different showHelp value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("feedback", true, false).hashCode());

        // different exit value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("feedback", false, true).hashCode());
    }

    @Test
    public void toStringMethod() {
        CommandResult commandResult = new CommandResult("feedback");
        String expected = CommandResult.class.getCanonicalName() + "{feedbackToUser="
                + commandResult.getFeedbackToUser() + ", showHelp=" + commandResult.isShowHelp()
                + ", exit=" + commandResult.isExit()
                + ", displayRequest=" + commandResult.getDisplayRequest() + "}";
        assertEquals(expected, commandResult.toString());
    }
}
