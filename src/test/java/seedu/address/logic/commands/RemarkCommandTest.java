package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
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

/**
 * Contains integration tests (interaction with the Model) and unit tests for {@code RemarkCommand}.
 */
public class RemarkCommandTest {

    private static final String REMARK_STUB = "Likes to swim";

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_addRemarkUnfilteredList_success() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(firstPerson).withRemark(REMARK_STUB).build();

        RemarkCommand remarkCommand = new RemarkCommand(INDEX_FIRST_PERSON, new Remark(REMARK_STUB));

        String expectedMessage = String.format(RemarkCommand.MESSAGE_ADD_REMARK_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(firstPerson, editedPerson);

        assertCommandSuccess(remarkCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_deleteRemarkUnfilteredList_success() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person personWithRemark = new PersonBuilder(firstPerson).withRemark(REMARK_STUB).build();
        model.setPerson(firstPerson, personWithRemark);

        RemarkCommand remarkCommand = new RemarkCommand(INDEX_FIRST_PERSON, new Remark(""));

        String expectedMessage = String.format(RemarkCommand.MESSAGE_DELETE_REMARK_SUCCESS,
                Messages.format(firstPerson));

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(personWithRemark, firstPerson);

        assertCommandSuccess(remarkCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndexUnfilteredList_failure() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        RemarkCommand remarkCommand = new RemarkCommand(outOfBoundIndex, new Remark(REMARK_STUB));

        assertCommandFailure(remarkCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        RemarkCommand remarkFirstCommand = new RemarkCommand(INDEX_FIRST_PERSON, new Remark(REMARK_STUB));
        RemarkCommand remarkSecondCommand = new RemarkCommand(INDEX_SECOND_PERSON, new Remark(REMARK_STUB));

        // same object -> returns true
        assertTrue(remarkFirstCommand.equals(remarkFirstCommand));

        // same values -> returns true
        RemarkCommand remarkFirstCommandCopy = new RemarkCommand(INDEX_FIRST_PERSON, new Remark(REMARK_STUB));
        assertTrue(remarkFirstCommand.equals(remarkFirstCommandCopy));

        // different types -> returns false
        assertFalse(remarkFirstCommand.equals(1));

        // null -> returns false
        assertFalse(remarkFirstCommand.equals(null));

        // different person -> returns false
        assertFalse(remarkFirstCommand.equals(remarkSecondCommand));
    }

    @Test
    public void toStringMethod() {
        Index targetIndex = Index.fromOneBased(1);
        Remark remark = new Remark(REMARK_STUB);
        RemarkCommand remarkCommand = new RemarkCommand(targetIndex, remark);
        String expected = RemarkCommand.class.getCanonicalName() + "{index=" + targetIndex + ", remark="
                + remark + "}";
        assertEquals(expected, remarkCommand.toString());
    }
}
