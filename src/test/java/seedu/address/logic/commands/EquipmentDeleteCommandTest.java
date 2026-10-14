package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.DeletionTestData.FIRST_EQUIPMENT;
import static seedu.address.logic.commands.DeletionTestData.FIRST_MEMBER;
import static seedu.address.logic.commands.DeletionTestData.LEGACY_PERSON;
import static seedu.address.logic.commands.DeletionTestData.SECOND_EQUIPMENT;
import static seedu.address.logic.commands.DeletionTestData.SECOND_MEMBER;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.equipment.exceptions.EquipmentNotFoundException;
import seedu.address.model.loan.Loan;

/**
 * Verifies UUID deletion, preserving identical physical items and unrelated loan history.
 */
public class EquipmentDeleteCommandTest {

    @Test
    public void execute_exactUuid_preservesIdenticalItemAndOtherCollections() throws Exception {
        ModelManager model = DeletionTestData.model();
        CommandResult result = new EquipmentDeleteCommand(FIRST_EQUIPMENT.getUuid()).execute(model);
        assertEquals(List.of(SECOND_EQUIPMENT), model.getEquipmentList());
        assertEquals(List.of(FIRST_MEMBER, SECOND_MEMBER), model.getMemberList());
        assertEquals(List.of(LEGACY_PERSON), model.getFilteredPersonList());
        assertEquals(DisplayRequest.equipment(), result.getDisplayRequest());
        assertEquals("Deleted equipment: " + FIRST_EQUIPMENT.getUuid(), result.getFeedbackToUser());
        assertFalse(result.isShowHelp());
        assertFalse(result.isExit());
    }

    @Test
    public void execute_resolvesStoredItem_passesActualEntityToModel() throws Exception {
        ModelManager model = new ModelManager() {
            @Override
            public void deleteEquipment(Equipment equipment) {
                assertSame(FIRST_EQUIPMENT, equipment);
                super.deleteEquipment(equipment);
            }
        };
        model.addEquipment(FIRST_EQUIPMENT);
        new EquipmentDeleteCommand(FIRST_EQUIPMENT.getUuid()).execute(model);
        assertTrue(model.getEquipmentList().isEmpty());
    }

    @Test
    public void execute_unknownUuid_preservesAllDataAndReportsIdentity() {
        ModelManager model = DeletionTestData.model();
        model.addLoan(DeletionTestData.loan(FIRST_EQUIPMENT, FIRST_MEMBER, false));
        AddressBook before = new AddressBook(model.getAddressBook());
        UUID unknown = new UUID(0, 99);
        CommandException error = assertThrows(
                CommandException.class, () -> new EquipmentDeleteCommand(unknown).execute(model));
        assertInstanceOf(EquipmentNotFoundException.class, error.getCause());
        assertEquals(String.format(EquipmentDeleteCommand.MESSAGE_NOT_FOUND, unknown), error.getMessage());
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void execute_openLoan_preservesEquipmentAndClosedHistory() {
        ModelManager model = DeletionTestData.model();
        model.addLoan(DeletionTestData.loan(FIRST_EQUIPMENT, FIRST_MEMBER, false));
        model.addLoan(DeletionTestData.loan(FIRST_EQUIPMENT, SECOND_MEMBER, true));
        AddressBook before = new AddressBook(model.getAddressBook());
        CommandException error = assertThrows(
                CommandException.class, () -> new EquipmentDeleteCommand(FIRST_EQUIPMENT.getUuid()).execute(model));
        assertInstanceOf(IllegalStateException.class, error.getCause());
        assertEquals(String.format(EquipmentDeleteCommand.MESSAGE_OPEN_LOAN, FIRST_EQUIPMENT.getUuid()),
                error.getMessage());
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void execute_closedHistory_removesOnlyDeletedItemsLoans() throws Exception {
        ModelManager model = DeletionTestData.model();
        model.addLoan(DeletionTestData.loan(FIRST_EQUIPMENT, FIRST_MEMBER, false));
        model.addLoan(DeletionTestData.loan(FIRST_EQUIPMENT, SECOND_MEMBER, false));
        Loan retainedHistory = DeletionTestData.loan(SECOND_EQUIPMENT, FIRST_MEMBER, false);
        Loan retainedOpen = DeletionTestData.loan(SECOND_EQUIPMENT, SECOND_MEMBER, true);
        model.addLoan(retainedHistory);
        model.addLoan(retainedOpen);
        new EquipmentDeleteCommand(FIRST_EQUIPMENT.getUuid()).execute(model);
        assertEquals(List.of(SECOND_EQUIPMENT), model.getEquipmentList());
        assertEquals(List.of(retainedHistory, retainedOpen), model.getLoanList());
        assertEquals(List.of(FIRST_MEMBER, SECOND_MEMBER), model.getMemberList());
        assertEquals(List.of(LEGACY_PERSON), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_expectedModelFailure_preservesOriginalCause() {
        EquipmentNotFoundException failure = new EquipmentNotFoundException();
        ModelManager model = new ModelManager() {
            @Override
            public void deleteEquipment(Equipment equipment) {
                throw failure;
            }
        };
        model.addEquipment(FIRST_EQUIPMENT);
        AddressBook before = new AddressBook(model.getAddressBook());
        CommandException error = assertThrows(
                CommandException.class, () -> new EquipmentDeleteCommand(FIRST_EQUIPMENT.getUuid()).execute(model));
        assertSame(failure, error.getCause());
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void execute_openLoanModelFailure_preservesOriginalCause() {
        IllegalStateException failure = new IllegalStateException("Equipment still has an open loan");
        ModelManager model = new ModelManager() {
            @Override
            public void deleteEquipment(Equipment equipment) {
                throw failure;
            }
        };
        model.addMember(FIRST_MEMBER);
        model.addEquipment(FIRST_EQUIPMENT);
        model.addLoan(DeletionTestData.loan(FIRST_EQUIPMENT, FIRST_MEMBER, true));
        AddressBook before = new AddressBook(model.getAddressBook());
        CommandException error = assertThrows(
                CommandException.class, () -> new EquipmentDeleteCommand(FIRST_EQUIPMENT.getUuid()).execute(model));
        assertSame(failure, error.getCause());
        assertEquals(String.format(EquipmentDeleteCommand.MESSAGE_OPEN_LOAN, FIRST_EQUIPMENT.getUuid()),
                error.getMessage());
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void constructorAndExecute_nullArguments_rejectsMisuse() {
        assertThrows(NullPointerException.class, () -> new EquipmentDeleteCommand(null));
        assertThrows(
                NullPointerException.class, () -> new EquipmentDeleteCommand(FIRST_EQUIPMENT.getUuid()).execute(null));
    }

    @Test
    public void equalsHashCodeAndToString_uuid_valueBehavior() {
        EquipmentDeleteCommand command = new EquipmentDeleteCommand(FIRST_EQUIPMENT.getUuid());
        EquipmentDeleteCommand equal = new EquipmentDeleteCommand(
                UUID.fromString(FIRST_EQUIPMENT.getUuid().toString()));
        assertEquals(command, command);
        assertEquals(command, equal);
        assertEquals(command.hashCode(), equal.hashCode());
        assertNotEquals(command, new EquipmentDeleteCommand(SECOND_EQUIPMENT.getUuid()));
        assertNotEquals(command, null);
        assertNotEquals(command, "equipment delete");
        assertTrue(command.toString().contains(FIRST_EQUIPMENT.getUuid().toString()));
    }
}
