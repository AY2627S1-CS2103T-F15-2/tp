package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.Remark;
import seedu.address.testutil.PersonBuilder;

public class RemarkCommandTest {

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new RemarkCommand(null, new Remark("note")));
        assertThrows(NullPointerException.class, () -> new RemarkCommand(INDEX_FIRST_PERSON, null));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                new RemarkCommand(INDEX_FIRST_PERSON, new Remark("note")).execute(null));
    }

    @Test
    public void execute_addRemark_success() {
        assertRemarkSuccess("Likes baseball", RemarkCommand.MESSAGE_ADD_REMARK_SUCCESS);
    }

    @Test
    public void execute_replaceRemark_success() {
        setExistingRemark();
        assertRemarkSuccess("Likes swimming", RemarkCommand.MESSAGE_ADD_REMARK_SUCCESS);
    }

    @Test
    public void execute_clearRemark_success() {
        setExistingRemark();
        assertRemarkSuccess("", RemarkCommand.MESSAGE_DELETE_REMARK_SUCCESS);
    }

    @Test
    public void execute_clearEmptyRemark_success() {
        assertRemarkSuccess("", RemarkCommand.MESSAGE_DELETE_REMARK_SUCCESS);
    }

    @Test
    public void execute_filteredList_updatesDisplayedPerson() {
        // The first displayed person is the second person in the underlying address book.
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        assertRemarkSuccess("Filtered person", RemarkCommand.MESSAGE_ADD_REMARK_SUCCESS);
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        Index invalid = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(new RemarkCommand(invalid, new Remark("note")), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandFailure(new RemarkCommand(INDEX_SECOND_PERSON, new Remark("note")), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        RemarkCommand command = new RemarkCommand(INDEX_FIRST_PERSON, new Remark("note"));
        RemarkCommand copy = new RemarkCommand(INDEX_FIRST_PERSON, new Remark("note"));
        assertTrue(command.equals(command));
        assertEquals(command, copy);
        assertEquals(command.hashCode(), copy.hashCode());
        assertFalse(command.equals(null));
        assertFalse(command.equals("note"));
        assertFalse(command.equals(new RemarkCommand(INDEX_SECOND_PERSON, new Remark("note"))));
        assertFalse(command.equals(new RemarkCommand(INDEX_FIRST_PERSON, new Remark("other"))));
    }

    private void setExistingRemark() {
        Person original = model.getFilteredPersonList().get(0);
        model.setPerson(original, new PersonBuilder(original).withRemark("Existing remark").build());
    }

    private void assertRemarkSuccess(String text, String message) {
        Person original = model.getFilteredPersonList().get(0);
        Person updated = new PersonBuilder(original).withRemark(text).build();
        Model expected = new ModelManager(model.getAddressBook(), new UserPrefs());
        expected.setPerson(original, updated);
        assertCommandSuccess(new RemarkCommand(INDEX_FIRST_PERSON, new Remark(text)), model,
                String.format(message, Messages.format(updated)), expected);
    }
}
