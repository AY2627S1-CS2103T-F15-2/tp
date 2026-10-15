package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.LoanCommandTestData.CLOCK;
import static seedu.address.logic.commands.LoanCommandTestData.FIRST_MEMBER;
import static seedu.address.logic.commands.LoanCommandTestData.FIRST_UUID;
import static seedu.address.logic.commands.LoanCommandTestData.SECOND_UUID;
import static seedu.address.logic.commands.LoanCommandTestData.TODAY;
import static seedu.address.logic.commands.LoanCommandTestData.UNKNOWN_UUID;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.equipment.Availability;
import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.exceptions.EquipmentNotFoundException;
import seedu.address.model.loan.Loan;
import seedu.address.model.loan.exceptions.OpenLoanNotFoundException;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.model.member.exceptions.MemberNotFoundException;

public class LoanReturnCommandTest {
    @Test
    public void execute_validReturn_replacesImmutableLoanAndClearsHoldingsWithDetailsRequest() throws Exception {
        ModelManager model = modelWithLoan(Condition.GOOD);
        AddressBook before = new AddressBook(model.getAddressBook());
        Loan original = model.findOpenLoan(FIRST_UUID).orElseThrow();
        CommandResult result = command(TODAY).execute(model);
        assertTrue(original.isOpen());
        assertTrue(model.findOpenLoan(FIRST_UUID).isEmpty());
        assertTrue(model.findOpenLoansForMember(FIRST_MEMBER.getNusId()).isEmpty());
        assertEquals(Availability.AVAILABLE, model.getAvailability(FIRST_UUID));
        assertEquals(original.withReturnedDate(TODAY), model.getLoanList().get(1));
        assertEquals(before.getLoanList().get(0), model.getLoanList().get(0));
        assertEquals(before.getPersonList(), model.getAddressBook().getPersonList());
        assertEquals(before.getMemberList(), model.getMemberList());
        assertEquals(before.getEquipmentList(), model.getEquipmentList());
        assertEquals(DisplayRequest.equipmentDetails(FIRST_UUID), result.getDisplayRequest());
        assertEquals(String.format(LoanReturnCommand.MESSAGE_SUCCESS, "Camera", FIRST_UUID, "Alex Tan",
                FIRST_MEMBER.getNusId(), TODAY), result.getFeedbackToUser());
        assertFalse(result.isShowHelp());
        assertFalse(result.isExit());
    }

    @Test
    public void execute_sameDayEarlyOnTimeAndLateReturns_acceptsDateBoundaries() throws Exception {
        Loan original = LoanCommandTestData.openLoan();
        for (LocalDate date : List.of(original.getAssignedDate(), original.getAssignedDate().plusDays(1),
                original.getExpectedReturnDate(), TODAY)) {
            ModelManager model = modelWithLoan(Condition.GOOD);
            command(date).execute(model);
            assertEquals(Optional.of(date), model.getLoanList().get(1).getReturnedDate());
        }
        ModelManager sameDay = LoanCommandTestData.model();
        sameDay.addLoan(new Loan(FIRST_UUID, FIRST_MEMBER.getNusId(), TODAY, TODAY, null));
        command(TODAY).execute(sameDay);
        assertEquals(Optional.of(TODAY), sameDay.getLoanList().get(1).getReturnedDate());
    }

    @Test
    public void execute_returnEachCondition_derivesAvailabilityAfterClosure() throws Exception {
        for (Condition condition : Condition.values()) {
            ModelManager model = modelWithLoan(condition);
            assertEquals(Availability.ASSIGNED, model.getAvailability(FIRST_UUID));
            command(TODAY).execute(model);
            assertEquals(condition == Condition.GOOD || condition == Condition.FAIR
                    ? Availability.AVAILABLE : Availability.UNAVAILABLE, model.getAvailability(FIRST_UUID));
        }
    }

    @Test
    public void execute_returnBeforeAssignmentOrAfterToday_rejectsWithoutMutation() {
        ModelManager model = modelWithLoan(Condition.GOOD);
        AddressBook before = new AddressBook(model.getAddressBook());
        CommandException early = assertThrows(CommandException.class, () -> command(TODAY.minusDays(4)).execute(model));
        assertEquals(Loan.MESSAGE_RETURNED_DATE_CONSTRAINTS, early.getMessage());
        CommandException future = assertThrows(CommandException.class, () -> command(TODAY.plusDays(1)).execute(model));
        assertEquals(String.format(LoanReturnCommand.MESSAGE_FUTURE_RETURN, TODAY), future.getMessage());
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void execute_unknownEquipment_reportsIdentityAndPreservesState() {
        ModelManager model = modelWithLoan(Condition.GOOD);
        AddressBook before = new AddressBook(model.getAddressBook());
        CommandException error = assertThrows(CommandException.class, () ->
                new LoanReturnCommand(UNKNOWN_UUID, TODAY, CLOCK).execute(model));
        assertInstanceOf(EquipmentNotFoundException.class, error.getCause());
        assertEquals(String.format(LoanReturnCommand.MESSAGE_EQUIPMENT_NOT_FOUND, UNKNOWN_UUID), error.getMessage());
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void execute_noOpenLoanOrRepeatedReturn_rejectsAndRetainsHistory() throws Exception {
        ModelManager noLoan = LoanCommandTestData.model();
        AddressBook noLoanBefore = new AddressBook(noLoan.getAddressBook());
        CommandException absent = assertThrows(CommandException.class, () -> command(TODAY).execute(noLoan));
        assertInstanceOf(OpenLoanNotFoundException.class, absent.getCause());
        assertEquals(String.format(LoanReturnCommand.MESSAGE_NO_OPEN_LOAN, FIRST_UUID), absent.getMessage());
        assertEquals(noLoanBefore, noLoan.getAddressBook());

        ModelManager returned = modelWithLoan(Condition.GOOD);
        command(TODAY).execute(returned);
        AddressBook before = new AddressBook(returned.getAddressBook());
        assertThrows(CommandException.class, () -> command(TODAY).execute(returned));
        assertEquals(before, returned.getAddressBook());
    }

    @Test
    public void execute_missingMemberReference_boundaryDoubleRejectsWithoutClosingLoan() {
        ModelManager model = new ModelManager(modelWithLoan(Condition.GOOD).getAddressBook(), new UserPrefs()) {
            @Override
            public Optional<Member> findMember(NusId nusId) {
                return Optional.empty();
            }
        };
        AddressBook before = new AddressBook(model.getAddressBook());
        CommandException error = assertThrows(CommandException.class, () -> command(TODAY).execute(model));
        assertInstanceOf(MemberNotFoundException.class, error.getCause());
        assertEquals(String.format(LoanReturnCommand.MESSAGE_MEMBER_NOT_FOUND, FIRST_UUID), error.getMessage());
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void execute_expectedModelFailures_preservesCauseAndAllCollections() {
        for (RuntimeException failure : List.of(new EquipmentNotFoundException(), new OpenLoanNotFoundException(),
                new MemberNotFoundException(), new IllegalArgumentException("Invalid return date"))) {
            ModelManager model = new ModelManager(modelWithLoan(Condition.GOOD).getAddressBook(), new UserPrefs()) {
                @Override
                public void closeLoan(UUID uuid, LocalDate returnedDate) {
                    throw failure;
                }
            };
            AddressBook before = new AddressBook(model.getAddressBook());
            CommandException error = assertThrows(CommandException.class, () -> command(TODAY).execute(model));
            assertSame(failure, error.getCause());
            assertEquals(before, model.getAddressBook());
        }
    }

    @Test
    public void execute_clockZoneControlsToday_atAnInstantAcrossLocalMidnight() throws Exception {
        command(TODAY).execute(modelWithLoan(Condition.GOOD));
        ModelManager utc = modelWithLoan(Condition.GOOD);
        AddressBook before = new AddressBook(utc.getAddressBook());
        assertThrows(CommandException.class, () ->
                new LoanReturnCommand(FIRST_UUID, TODAY, CLOCK.withZone(ZoneOffset.UTC)).execute(utc));
        assertEquals(before, utc.getAddressBook());
    }

    @Test
    public void execute_readsClockAndOpenLoanAtExecution() throws Exception {
        LoanCommandTestData.MutableClock clock = new LoanCommandTestData.MutableClock();
        LoanReturnCommand command = new LoanReturnCommand(FIRST_UUID, TODAY.plusDays(1), clock);
        ModelManager model = LoanCommandTestData.model();
        model.addLoan(LoanCommandTestData.openLoan());
        clock.advanceDay();
        command.execute(model);
        assertEquals(Optional.of(TODAY.plusDays(1)), model.getLoanList().get(1).getReturnedDate());
    }

    @Test
    public void constructorAndExecute_nullArguments_rejectsProgrammerMisuse() {
        assertThrows(NullPointerException.class, () -> new LoanReturnCommand(null, TODAY));
        assertThrows(NullPointerException.class, () -> new LoanReturnCommand(FIRST_UUID, null));
        assertThrows(NullPointerException.class, () -> new LoanReturnCommand(FIRST_UUID, TODAY, null));
        assertThrows(NullPointerException.class, () -> command(TODAY).execute(null));
    }

    @Test
    public void equalsHashCodeAndToString_compareCommandInputsWithoutClockIdentity() {
        LoanReturnCommand command = command(TODAY);
        LoanReturnCommand equal = new LoanReturnCommand(FIRST_UUID, TODAY);
        assertEquals(command, command);
        assertEquals(command, equal);
        assertEquals(command.hashCode(), equal.hashCode());
        assertNotEquals(command, null);
        assertNotEquals(command, "loans return");
        assertNotEquals(command, new LoanReturnCommand(SECOND_UUID, TODAY));
        assertNotEquals(command, command(TODAY.minusDays(1)));
        assertTrue(command.toString().contains(FIRST_UUID.toString()));
        assertTrue(command.toString().contains(TODAY.toString()));
    }

    private static ModelManager modelWithLoan(Condition condition) {
        ModelManager model = LoanCommandTestData.model(condition);
        model.addLoan(LoanCommandTestData.openLoan());
        return model;
    }

    private static LoanReturnCommand command(LocalDate returned) {
        return new LoanReturnCommand(FIRST_UUID, returned, CLOCK);
    }
}
