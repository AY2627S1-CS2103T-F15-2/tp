package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.LoanCommandTestData.CLOCK;
import static seedu.address.logic.commands.LoanCommandTestData.FIRST_MEMBER;
import static seedu.address.logic.commands.LoanCommandTestData.FIRST_UUID;
import static seedu.address.logic.commands.LoanCommandTestData.SECOND_MEMBER;
import static seedu.address.logic.commands.LoanCommandTestData.SECOND_UUID;
import static seedu.address.logic.commands.LoanCommandTestData.TODAY;
import static seedu.address.logic.commands.LoanCommandTestData.UNKNOWN_UUID;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.loan.Loan;

/** Checks ordered batch diagnostics and validation before any candidate mutation. */
public class LoanBatchValidationTest {
    @Test
    public void assignment_mixedInvalidUnits_reportsEveryBlockingReasonAndKeepsValidUnit() {
        ModelManager model = LoanCommandTestData.model(Condition.DAMAGED);
        model.addLoan(new Loan(SECOND_UUID, SECOND_MEMBER.getNusId(), TODAY, TODAY, null));
        UUID deleted = UUID.randomUUID();
        Equipment deletedItem = new Equipment(deleted, "Deleted", "Photo", Condition.GOOD, "");
        model.addEquipment(deletedItem);
        model.deleteEquipment(deletedItem);
        UUID valid = UUID.randomUUID();
        model.addEquipment(new Equipment(valid, "Valid", "Photo", Condition.GOOD, ""));
        AddressBook before = new AddressBook(model.getAddressBook());

        CommandException error = assertThrows(CommandException.class, () -> new LoanAddCommand(
                List.of(UNKNOWN_UUID, FIRST_UUID, valid, SECOND_UUID, deleted), FIRST_MEMBER.getNusId(),
                TODAY, TODAY, CLOCK).execute(model));

        assertEquals("Rejected batch: 4 of 5 units are invalid. No records changed. Open Details for all errors.\n"
                + String.format(LoanAddCommand.MESSAGE_EQUIPMENT_NOT_FOUND, UNKNOWN_UUID) + "\n"
                + String.format(LoanAddCommand.MESSAGE_UNAVAILABLE, FIRST_UUID, Condition.DAMAGED) + "\n"
                + String.format(LoanAddCommand.MESSAGE_ASSIGNED, SECOND_UUID, SECOND_MEMBER.getNusId()) + "\n"
                + String.format(LoanAddCommand.MESSAGE_DELETED, deleted), error.getMessage());
        assertEquals(3, error.getSuppressed().length);
        assertTrue(error.getCause().getCause() instanceof RuntimeException);
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void assignment_historicalOverlapAndDamagedUnit_reportsBothAndAllowsReturnDayHandoff() throws Exception {
        ModelManager model = LoanCommandTestData.model();
        model.addLoan(new Loan(FIRST_UUID, FIRST_MEMBER.getNusId(), TODAY.minusDays(4), TODAY.minusDays(2),
                TODAY.minusDays(2)));
        Equipment second = model.findEquipment(SECOND_UUID).orElseThrow();
        model.setEquipment(second, new Equipment(SECOND_UUID, second.getName(), second.getCategory(),
                Condition.DAMAGED, second.getNotes()));
        AddressBook before = new AddressBook(model.getAddressBook());

        CommandException error = assertThrows(CommandException.class, () -> new LoanAddCommand(
                List.of(FIRST_UUID, SECOND_UUID), FIRST_MEMBER.getNusId(), TODAY.minusDays(3), TODAY, CLOCK)
                .execute(model));

        assertEquals("Rejected batch: 2 of 2 units are invalid. No records changed. Open Details for all errors.\n"
                + String.format(LoanAddCommand.MESSAGE_HISTORICAL_OVERLAP, FIRST_UUID, TODAY.minusDays(2)) + "\n"
                + String.format(LoanAddCommand.MESSAGE_UNAVAILABLE, SECOND_UUID, Condition.DAMAGED),
                error.getMessage());
        assertEquals(before, model.getAddressBook());
        new LoanAddCommand(FIRST_UUID, FIRST_MEMBER.getNusId(), TODAY.minusDays(2), TODAY, CLOCK).execute(model);
        assertEquals(TODAY.minusDays(2), model.findOpenLoan(FIRST_UUID).orElseThrow().getAssignedDate());
    }

    @Test
    public void return_mixedInvalidUnits_reportsDatesMissingLoanAndUnknownUnitWithoutChangingConditions() {
        ModelManager model = LoanCommandTestData.model();
        model.addLoan(new Loan(FIRST_UUID, FIRST_MEMBER.getNusId(), TODAY, TODAY, null));
        UUID valid = UUID.randomUUID();
        model.addEquipment(new Equipment(valid, "Valid", "Photo", Condition.GOOD, ""));
        model.addLoan(new Loan(valid, FIRST_MEMBER.getNusId(), TODAY.minusDays(3), TODAY, null));
        AddressBook before = new AddressBook(model.getAddressBook());

        CommandException error = assertThrows(CommandException.class, () -> new LoanReturnCommand(
                List.of(FIRST_UUID, valid, SECOND_UUID, UNKNOWN_UUID), TODAY.minusDays(1),
                Condition.DAMAGED, CLOCK).execute(model));

        assertEquals("Rejected batch: 3 of 4 units are invalid. No records changed. Open Details for all errors.\n"
                + String.format(LoanReturnCommand.MESSAGE_RETURN_BEFORE_ASSIGNMENT,
                        "Camera", FIRST_UUID, TODAY.minusDays(1), TODAY) + "\n"
                + String.format(LoanReturnCommand.MESSAGE_NO_OPEN_LOAN, SECOND_UUID) + "\n"
                + String.format(LoanReturnCommand.MESSAGE_EQUIPMENT_NOT_FOUND, UNKNOWN_UUID), error.getMessage());
        assertEquals(2, error.getSuppressed().length);
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void return_longBatch_preservesEveryDiagnosticInInputOrder() {
        ModelManager model = LoanCommandTestData.model();
        List<UUID> missing = IntStream.range(100, 130).mapToObj(value -> new UUID(0, value)).toList();
        CommandException error = assertThrows(CommandException.class, () ->
                new LoanReturnCommand(missing, TODAY, null, CLOCK).execute(model));
        assertTrue(error.getMessage().startsWith("Rejected batch: 30 of 30 units are invalid."));
        String[] lines = error.getMessage().split("\n");
        assertEquals(31, lines.length);
        for (int i = 0; i < missing.size(); i++) {
            assertEquals(String.format(LoanReturnCommand.MESSAGE_EQUIPMENT_NOT_FOUND, missing.get(i)), lines[i + 1]);
        }
    }

    @Test
    public void sharedMemberOrDateError_preventsPerUnitDiagnostics() {
        ModelManager model = LoanCommandTestData.model();
        model.deleteMember(FIRST_MEMBER);
        CommandException member = assertThrows(CommandException.class, () -> new LoanAddCommand(
                List.of(UNKNOWN_UUID, SECOND_UUID), FIRST_MEMBER.getNusId(), TODAY, TODAY, CLOCK).execute(model));
        assertEquals(String.format(LoanAddCommand.MESSAGE_MEMBER_DELETED, FIRST_MEMBER.getNusId()),
                member.getMessage());
        CommandException assigned = assertThrows(CommandException.class, () -> new LoanAddCommand(
                List.of(UNKNOWN_UUID, SECOND_UUID), SECOND_MEMBER.getNusId(), TODAY.plusDays(1), TODAY.plusDays(1),
                CLOCK).execute(model));
        assertEquals(String.format(LoanAddCommand.MESSAGE_FUTURE_ASSIGNMENT, TODAY), assigned.getMessage());
        CommandException returned = assertThrows(CommandException.class, () -> new LoanReturnCommand(
                List.of(UNKNOWN_UUID, SECOND_UUID), TODAY.plusDays(1), null, CLOCK).execute(model));
        assertEquals(String.format(LoanReturnCommand.MESSAGE_FUTURE_RETURN, TODAY), returned.getMessage());
    }
}
