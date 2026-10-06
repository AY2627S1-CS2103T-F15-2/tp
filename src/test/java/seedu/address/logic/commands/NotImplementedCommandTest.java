package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.model.ModelManager;

public class NotImplementedCommandTest {

    @Test
    public void constructor_nullCommandName_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new NotImplementedCommand(null));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        NotImplementedCommand command = new NotImplementedCommand("member list");

        assertThrows(NullPointerException.class, () -> command.execute(null));
    }

    @Test
    public void execute_validModel_returnsMessageWithoutMutation() {
        ModelManager model = new ModelManager();
        ModelManager originalModel = new ModelManager();
        NotImplementedCommand command = new NotImplementedCommand("member list");

        CommandResult result = command.execute(model);

        assertEquals("The member list command is not implemented yet.", result.getFeedbackToUser());
        assertEquals(originalModel, model);
    }
}
