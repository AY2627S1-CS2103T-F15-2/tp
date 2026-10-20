package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.LoanCommandTestData.CLOCK;
import static seedu.address.logic.commands.LoanCommandTestData.FIRST_MEMBER;
import static seedu.address.logic.commands.LoanCommandTestData.FIRST_UUID;
import static seedu.address.logic.commands.LoanCommandTestData.SECOND_UUID;
import static seedu.address.logic.commands.LoanCommandTestData.TODAY;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.LoanAddCommandParser;
import seedu.address.logic.parser.LoanReturnCommandParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.equipment.Availability;
import seedu.address.model.equipment.Condition;

/** Covers batch commits and rollback boundaries across loans and equipment. */
public class LoanBatchCommandTest {
    @Test
    public void batchAssignment_twoUnits_capturesConditionsAndCommitsTogether() throws Exception {
        ModelManager model = LoanCommandTestData.model();
        LoanAddCommand command = new LoanAddCommand(List.of(FIRST_UUID, SECOND_UUID), FIRST_MEMBER.getNusId(),
                TODAY, TODAY.plusDays(7), CLOCK);

        CommandResult result = command.execute(model);

        assertEquals(DisplayRequest.loans(), result.getDisplayRequest());
        assertEquals(Condition.GOOD, model.findOpenLoan(FIRST_UUID).orElseThrow().getIssueCondition().orElseThrow());
        assertEquals(Condition.FAIR, model.findOpenLoan(SECOND_UUID).orElseThrow().getIssueCondition().orElseThrow());
        assertEquals(Availability.ASSIGNED, model.getAvailability(FIRST_UUID));
        assertEquals(Availability.ASSIGNED, model.getAvailability(SECOND_UUID));
    }

    @Test
    public void batchAssignment_unavailableSecondUnit_keepsWholeModel() {
        ModelManager model = LoanCommandTestData.model();
        model.addLoan(LoanCommandTestData.openLoan());
        AddressBook before = new AddressBook(model.getAddressBook());
        LoanAddCommand command = new LoanAddCommand(List.of(SECOND_UUID, FIRST_UUID), FIRST_MEMBER.getNusId(),
                TODAY, TODAY.plusDays(7), CLOCK);

        assertThrows(CommandException.class, () -> command.execute(model));
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void batchAssignment_emptyOrDuplicateIdentifiers_rejectsBeforeChangingModel() {
        ModelManager model = LoanCommandTestData.model();
        AddressBook before = new AddressBook(model.getAddressBook());

        assertThrows(IllegalArgumentException.class, () -> new LoanAddCommand(List.of(),
                FIRST_MEMBER.getNusId(), TODAY, TODAY.plusDays(7), CLOCK));
        assertThrows(IllegalArgumentException.class, () -> new LoanAddCommand(List.of(FIRST_UUID, FIRST_UUID),
                FIRST_MEMBER.getNusId(), TODAY, TODAY.plusDays(7), CLOCK));
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void batchReturn_updatesBothConditionsAndPreservesLoanHistory() throws Exception {
        ModelManager model = LoanCommandTestData.model();
        new LoanAddCommand(List.of(FIRST_UUID, SECOND_UUID), FIRST_MEMBER.getNusId(),
                TODAY, TODAY.plusDays(7), CLOCK).execute(model);

        new LoanReturnCommand(List.of(FIRST_UUID, SECOND_UUID), TODAY, Condition.DAMAGED, CLOCK).execute(model);

        assertTrue(model.findOpenLoan(FIRST_UUID).isEmpty());
        assertTrue(model.findOpenLoan(SECOND_UUID).isEmpty());
        assertEquals(3, model.getLoanList().size());
        assertEquals(Condition.DAMAGED, model.getLoanList().get(1).getReturnCondition().orElseThrow());
        assertEquals(Condition.DAMAGED, model.getLoanList().get(2).getReturnCondition().orElseThrow());
        assertEquals(Condition.DAMAGED, model.findEquipment(FIRST_UUID).orElseThrow().getCondition());
        assertEquals(Condition.DAMAGED, model.findEquipment(SECOND_UUID).orElseThrow().getCondition());
        assertEquals(Availability.UNAVAILABLE, model.getAvailability(FIRST_UUID));
    }

    @Test
    public void batchReturn_missingSecondLoan_keepsFirstOpenAndConditionUnchanged() {
        ModelManager model = LoanCommandTestData.model();
        model.addLoan(LoanCommandTestData.openLoan());
        AddressBook before = new AddressBook(model.getAddressBook());

        assertThrows(CommandException.class, () -> new LoanReturnCommand(List.of(FIRST_UUID, SECOND_UUID),
                TODAY, Condition.DAMAGED, CLOCK).execute(model));

        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void batchReturn_emptyOrDuplicateIdentifiers_rejectsBeforeChangingModel() {
        ModelManager model = LoanCommandTestData.model();
        model.addLoan(LoanCommandTestData.openLoan());
        AddressBook before = new AddressBook(model.getAddressBook());

        assertThrows(IllegalArgumentException.class, () -> new LoanReturnCommand(List.of(), TODAY,
                Condition.GOOD, CLOCK));
        assertThrows(IllegalArgumentException.class, () -> new LoanReturnCommand(List.of(FIRST_UUID, FIRST_UUID),
                TODAY, Condition.GOOD, CLOCK));
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void batchReturn_equality_includesIdentifiersDateAndCondition() {
        LoanReturnCommand command = new LoanReturnCommand(List.of(FIRST_UUID, SECOND_UUID),
                TODAY, Condition.GOOD, CLOCK);
        LoanReturnCommand equal = new LoanReturnCommand(List.of(FIRST_UUID, SECOND_UUID),
                TODAY, Condition.GOOD, CLOCK);

        assertEquals(command, equal);
        assertEquals(command.hashCode(), equal.hashCode());
        assertNotEquals(command, new LoanReturnCommand(List.of(SECOND_UUID, FIRST_UUID),
                TODAY, Condition.GOOD, CLOCK));
        assertNotEquals(command, new LoanReturnCommand(List.of(FIRST_UUID, SECOND_UUID),
                TODAY.minusDays(1), Condition.GOOD, CLOCK));
        assertNotEquals(command, new LoanReturnCommand(List.of(FIRST_UUID, SECOND_UUID),
                TODAY, Condition.FAIR, CLOCK));
    }

    @Test
    public void parsers_acceptListsAndRejectDuplicatesOrEmptyEntries() throws Exception {
        String ids = FIRST_UUID + "," + SECOND_UUID;
        LoanAddCommand add = new LoanAddCommandParser().parse("uuid/" + ids + " nus/" + FIRST_MEMBER.getNusId()
                + " assigned/" + TODAY + " expected/" + TODAY.plusDays(7));
        LoanReturnCommand returned = new LoanReturnCommandParser().parse("uuid/" + ids
                + " returned/" + TODAY + " condition/DAMAGED");
        ModelManager model = LoanCommandTestData.model();
        assertEquals(DisplayRequest.loans(), add.execute(model).getDisplayRequest());
        assertEquals(DisplayRequest.loans(), returned.execute(model).getDisplayRequest());
        assertEquals(Condition.DAMAGED, model.getLoanList().get(1).getReturnCondition().orElseThrow());
        assertThrows(ParseException.class, () -> new LoanAddCommandParser().parse("uuid/" + FIRST_UUID + ","
                + FIRST_UUID + " nus/" + FIRST_MEMBER.getNusId() + " assigned/" + TODAY + " expected/" + TODAY));
        assertThrows(ParseException.class, () -> new LoanReturnCommandParser().parse("uuid/" + FIRST_UUID
                + ", returned/" + TODAY));
    }
}
