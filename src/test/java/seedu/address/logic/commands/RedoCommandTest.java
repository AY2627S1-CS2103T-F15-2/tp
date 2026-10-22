package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.testutil.TypicalMembers;

class RedoCommandTest {
    @Test
    void execute_availableHistory_restoresRecordsAndKeepsNavigation() throws Exception {
        ModelManager model = new ModelManager();
        AddressBook before = new AddressBook(model.getAddressBook());
        model.addMember(TypicalMembers.ALEX);
        model.recordAddressBook(before, "member add");
        model.undoAddressBook();
        RedoCommand command = new RedoCommand();
        CommandResult result = command.execute(model);
        assertEquals("Redid: member add.", result.getFeedbackToUser());
        assertEquals(DisplayRequest.unchanged(), result.getDisplayRequest());
        assertEquals(1, model.getMemberList().size());
        assertTrue(command.isTransactional());
        assertFalse(command.isReadOnly());
        assertFalse(command.recordsHistory());
    }

    @Test
    void execute_unavailableHistory_reportsErrorWithoutChanges() {
        ModelManager model = new ModelManager();
        RedoCommand command = new RedoCommand();
        CommandException error = assertThrows(CommandException.class, () -> command.execute(model));
        assertEquals(RedoCommand.MESSAGE_UNAVAILABLE, error.getMessage());
        assertTrue(model.getMemberList().isEmpty());
        assertThrows(NullPointerException.class, () -> command.execute(null));
    }
}
