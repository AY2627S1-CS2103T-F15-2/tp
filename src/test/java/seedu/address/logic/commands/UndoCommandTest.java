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

class UndoCommandTest {
    @Test
    void execute_availableHistory_restoresRecordsAndKeepsNavigation() throws Exception {
        ModelManager model = new ModelManager();
        AddressBook before = new AddressBook(model.getAddressBook());
        model.addMember(TypicalMembers.ALEX);
        model.recordAddressBook(before, "member add");
        UndoCommand command = new UndoCommand();
        CommandResult result = command.execute(model);
        assertEquals("Undid: member add.", result.getFeedbackToUser());
        assertEquals(DisplayRequest.unchanged(), result.getDisplayRequest());
        assertEquals(0, model.getMemberList().size());
        assertTrue(command.isTransactional());
        assertFalse(command.isReadOnly());
        assertFalse(command.recordsHistory());
    }

    @Test
    void execute_unavailableHistory_reportsErrorWithoutChanges() {
        ModelManager model = new ModelManager();
        UndoCommand command = new UndoCommand();
        CommandException error = assertThrows(CommandException.class, () -> command.execute(model));
        assertEquals(UndoCommand.MESSAGE_UNAVAILABLE, error.getMessage());
        assertTrue(model.getMemberList().isEmpty());
        assertThrows(NullPointerException.class, () -> command.execute(null));
    }
}
