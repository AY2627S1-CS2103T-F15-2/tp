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
import static seedu.address.logic.commands.LoanCommandTestData.SECOND_MEMBER;
import static seedu.address.logic.commands.LoanCommandTestData.SECOND_UUID;
import static seedu.address.logic.commands.LoanCommandTestData.TODAY;
import static seedu.address.logic.commands.LoanCommandTestData.UNKNOWN_MEMBER;
import static seedu.address.logic.commands.LoanCommandTestData.UNKNOWN_UUID;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
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
import seedu.address.model.loan.exceptions.DuplicateOpenLoanException;
import seedu.address.model.member.exceptions.MemberNotFoundException;

public class LoanAddCommandTest {
    @Test
    public void execute_backdatedAssignment_checksActualReturnsForTheExactUnit() throws Exception {
        ModelManager model = LoanCommandTestData.model();
        LocalDate returned = TODAY.minusDays(5);
        model.addLoan(new Loan(FIRST_UUID, FIRST_MEMBER.getNusId(), TODAY.minusDays(10),
                TODAY.plusDays(20), returned));
        model.addLoan(new Loan(FIRST_UUID, SECOND_MEMBER.getNusId(), TODAY.minusDays(20),
                TODAY.minusDays(15), TODAY.minusDays(15)));
        AddressBook before = new AddressBook(model.getAddressBook());
        for (int daysAgo : List.of(12, 7, 6)) {
            CommandException failure = assertThrows(CommandException.class, () ->
                    command(TODAY.minusDays(daysAgo), TODAY).execute(model));
            assertEquals(String.format(LoanAddCommand.MESSAGE_HISTORICAL_OVERLAP, FIRST_UUID, returned),
                    failure.getMessage());
            assertEquals(before, model.getAddressBook());
        }
        new LoanAddCommand(SECOND_UUID, FIRST_MEMBER.getNusId(), TODAY.minusDays(7), TODAY, CLOCK).execute(model);
        command(returned, TODAY).execute(model);
        assertEquals(returned, model.findOpenLoan(FIRST_UUID).orElseThrow().getAssignedDate());
    }

    @Test
    public void execute_availableEquipment_recordsOpenLoanAndExactIdentitiesWithDetailsRequest() throws Exception {
        ModelManager model = LoanCommandTestData.model();
        AddressBook before = new AddressBook(model.getAddressBook());
        CommandResult result = command(TODAY, TODAY.plusDays(5)).execute(model);
        Loan loan = new Loan(FIRST_UUID, FIRST_MEMBER.getNusId(), TODAY, TODAY.plusDays(5),
                null, Condition.GOOD, null);
        assertEquals(loan, model.findOpenLoan(FIRST_UUID).orElseThrow());
        assertEquals(Availability.ASSIGNED, model.getAvailability(FIRST_UUID));
        assertEquals(List.of(loan), model.findOpenLoansForMember(FIRST_MEMBER.getNusId()));
        assertTrue(model.findOpenLoansForMember(SECOND_MEMBER.getNusId()).isEmpty());
        assertEquals(before.getLoanList().get(0), model.getLoanList().get(0));
        assertEquals(before.getPersonList(), model.getAddressBook().getPersonList());
        assertEquals(before.getMemberList(), model.getMemberList());
        assertEquals(before.getEquipmentList(), model.getEquipmentList());
        assertEquals(DisplayRequest.equipmentDetails(FIRST_UUID), result.getDisplayRequest());
        assertEquals(String.format(LoanAddCommand.MESSAGE_SUCCESS, "Camera", FIRST_UUID, "Alex Tan",
                FIRST_MEMBER.getNusId(), TODAY, TODAY.plusDays(5)), result.getFeedbackToUser());
        assertFalse(result.isShowHelp());
        assertFalse(result.isExit());
    }

    @Test
    public void execute_eachCondition_onlyGoodAndFairCanBeAssigned() throws Exception {
        for (Condition condition : Condition.values()) {
            ModelManager model = LoanCommandTestData.model(condition);
            if (condition == Condition.GOOD || condition == Condition.FAIR) {
                command(TODAY, TODAY).execute(model);
                assertEquals(Availability.ASSIGNED, model.getAvailability(FIRST_UUID));
            } else {
                AddressBook before = new AddressBook(model.getAddressBook());
                CommandException error = assertThrows(CommandException.class, () ->
                        command(TODAY, TODAY).execute(model));
                assertEquals(String.format(LoanAddCommand.MESSAGE_UNAVAILABLE, FIRST_UUID, condition),
                        error.getMessage());
                assertEquals(before, model.getAddressBook());
            }
        }
    }

    @Test
    public void execute_existingOpenLoan_rejectsEveryConditionAndPreservesAllCollections() {
        for (Condition condition : Condition.values()) {
            ModelManager model = LoanCommandTestData.model(condition);
            model.addLoan(LoanCommandTestData.openLoan());
            AddressBook before = new AddressBook(model.getAddressBook());
            CommandException error = assertThrows(CommandException.class, () -> command(TODAY, TODAY).execute(model));
            assertEquals("Equipment " + FIRST_UUID + " is unavailable because it is assigned to A0123456X.",
                    error.getMessage());
            assertEquals(before, model.getAddressBook());
        }
    }

    @Test
    public void execute_modelDetectsDuplicate_reportsCurrentHolderAndPreservesCause() {
        ModelManager model = new ModelManager(LoanCommandTestData.model().getAddressBook(), new UserPrefs()) {
            @Override
            public Availability getAvailability(UUID uuid) {
                return Availability.AVAILABLE;
            }
        };
        model.addLoan(LoanCommandTestData.openLoan());
        AddressBook before = new AddressBook(model.getAddressBook());
        CommandException error = assertThrows(CommandException.class, () -> new LoanAddCommand(
                FIRST_UUID, SECOND_MEMBER.getNusId(), TODAY, TODAY, CLOCK).execute(model));
        assertEquals("Equipment " + FIRST_UUID + " is unavailable because it is assigned to A0123456X.",
                error.getMessage());
        assertInstanceOf(DuplicateOpenLoanException.class, error.getCause());
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void execute_unknownEquipmentOrMember_reportsIdentityAndPreservesState() {
        ModelManager model = LoanCommandTestData.model();
        AddressBook before = new AddressBook(model.getAddressBook());
        CommandException equipmentError = assertThrows(CommandException.class, () ->
                new LoanAddCommand(UNKNOWN_UUID, FIRST_MEMBER.getNusId(), TODAY, TODAY, CLOCK).execute(model));
        assertInstanceOf(EquipmentNotFoundException.class, equipmentError.getCause());
        assertEquals(String.format(LoanAddCommand.MESSAGE_EQUIPMENT_NOT_FOUND, UNKNOWN_UUID),
                equipmentError.getMessage());
        CommandException memberError = assertThrows(CommandException.class, () ->
                new LoanAddCommand(FIRST_UUID, UNKNOWN_MEMBER, TODAY, TODAY, CLOCK).execute(model));
        assertInstanceOf(MemberNotFoundException.class, memberError.getCause());
        assertEquals(String.format(LoanAddCommand.MESSAGE_MEMBER_NOT_FOUND, UNKNOWN_MEMBER), memberError.getMessage());
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void execute_assignmentTodayExpectedSameDayOrFuture_acceptsBoundaryDates() throws Exception {
        for (LocalDate expected : List.of(TODAY, TODAY.plusDays(20))) {
            ModelManager model = LoanCommandTestData.model();
            command(TODAY, expected).execute(model);
            assertEquals(expected, model.findOpenLoan(FIRST_UUID).orElseThrow().getExpectedReturnDate());
        }
    }

    @Test
    public void execute_futureAssignmentOrExpectedBeforeAssignment_rejectsWithoutMutation() {
        ModelManager model = LoanCommandTestData.model();
        AddressBook before = new AddressBook(model.getAddressBook());
        CommandException future = assertThrows(CommandException.class, () ->
                command(TODAY.plusDays(1), TODAY.plusDays(2)).execute(model));
        assertEquals(String.format(LoanAddCommand.MESSAGE_FUTURE_ASSIGNMENT, TODAY), future.getMessage());
        CommandException order = assertThrows(CommandException.class, () ->
                command(TODAY, TODAY.minusDays(1)).execute(model));
        assertEquals(Loan.MESSAGE_EXPECTED_DATE_CONSTRAINTS, order.getMessage());
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void execute_clockZoneControlsToday_atAnInstantAcrossLocalMidnight() throws Exception {
        ModelManager localModel = LoanCommandTestData.model();
        command(TODAY, TODAY).execute(localModel);
        ModelManager utcModel = LoanCommandTestData.model();
        AddressBook before = new AddressBook(utcModel.getAddressBook());
        assertThrows(CommandException.class, () -> new LoanAddCommand(FIRST_UUID, FIRST_MEMBER.getNusId(),
                TODAY, TODAY, CLOCK.withZone(ZoneOffset.UTC)).execute(utcModel));
        assertEquals(before, utcModel.getAddressBook());
    }

    @Test
    public void execute_readsClockAtExecution_notConstruction() throws Exception {
        LoanCommandTestData.MutableClock clock = new LoanCommandTestData.MutableClock();
        LoanAddCommand command = new LoanAddCommand(FIRST_UUID, FIRST_MEMBER.getNusId(),
                TODAY.plusDays(1), TODAY.plusDays(2), clock);
        clock.advanceDay();
        ModelManager model = LoanCommandTestData.model();
        command.execute(model);
        assertEquals(TODAY.plusDays(1), model.findOpenLoan(FIRST_UUID).orElseThrow().getAssignedDate());
    }

    @Test
    public void execute_rechecksAvailabilityAfterConstruction_rejectsAChangedModel() {
        ModelManager model = LoanCommandTestData.model();
        LoanAddCommand command = command(TODAY, TODAY);
        model.addLoan(LoanCommandTestData.openLoan());
        AddressBook before = new AddressBook(model.getAddressBook());
        assertThrows(CommandException.class, () -> command.execute(model));
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void execute_usesModelAvailability_evenWhenConditionAloneLooksAvailable() {
        ModelManager model = new ModelManager(LoanCommandTestData.model().getAddressBook(), new UserPrefs()) {
            @Override
            public Availability getAvailability(UUID uuid) {
                return Availability.UNAVAILABLE;
            }
        };
        AddressBook before = new AddressBook(model.getAddressBook());
        assertThrows(CommandException.class, () -> command(TODAY, TODAY).execute(model));
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void execute_expectedModelFailures_preservesCauseAndAllCollections() {
        for (RuntimeException failure : List.of(new EquipmentNotFoundException(), new MemberNotFoundException(),
                new DuplicateOpenLoanException(), new IllegalArgumentException("Invalid loan dates"))) {
            ModelManager model = new ModelManager(LoanCommandTestData.model().getAddressBook(), new UserPrefs()) {
                @Override
                public void addLoan(Loan loan) {
                    throw failure;
                }
            };
            AddressBook before = new AddressBook(model.getAddressBook());
            CommandException error = assertThrows(CommandException.class, () -> command(TODAY, TODAY).execute(model));
            assertSame(failure, error.getCause());
            assertEquals(before, model.getAddressBook());
        }
    }

    @Test
    public void constructorAndExecute_nullArguments_rejectsProgrammerMisuse() {
        assertThrows(NullPointerException.class, () -> new LoanAddCommand(null, FIRST_MEMBER.getNusId(), TODAY, TODAY));
        assertThrows(NullPointerException.class, () -> new LoanAddCommand(FIRST_UUID, null, TODAY, TODAY));
        assertThrows(NullPointerException.class, () ->
                new LoanAddCommand(FIRST_UUID, FIRST_MEMBER.getNusId(), null, TODAY));
        assertThrows(NullPointerException.class, () ->
                new LoanAddCommand(FIRST_UUID, FIRST_MEMBER.getNusId(), TODAY, null));
        assertThrows(NullPointerException.class, () ->
                new LoanAddCommand(FIRST_UUID, FIRST_MEMBER.getNusId(), TODAY, TODAY, null));
        assertThrows(NullPointerException.class, () -> command(TODAY, TODAY).execute(null));
    }

    @Test
    public void equalsHashCodeAndToString_compareCommandInputsWithoutClockIdentity() {
        LoanAddCommand command = command(TODAY, TODAY);
        LoanAddCommand same = new LoanAddCommand(FIRST_UUID, FIRST_MEMBER.getNusId(), TODAY, TODAY);
        assertEquals(command, command);
        assertEquals(command, same);
        assertEquals(command.hashCode(), same.hashCode());
        assertNotEquals(command, null);
        assertNotEquals(command, "loans add");
        assertNotEquals(command, new LoanAddCommand(SECOND_UUID, FIRST_MEMBER.getNusId(), TODAY, TODAY));
        assertNotEquals(command, new LoanAddCommand(FIRST_UUID, SECOND_MEMBER.getNusId(), TODAY, TODAY));
        assertNotEquals(command, command(TODAY.minusDays(1), TODAY));
        assertNotEquals(command, command(TODAY, TODAY.plusDays(1)));
        assertTrue(command.toString().contains(FIRST_UUID.toString()));
        assertTrue(command.toString().contains(FIRST_MEMBER.getNusId().value));
        assertTrue(command.toString().contains(TODAY.toString()));
    }

    private static LoanAddCommand command(LocalDate assigned, LocalDate expected) {
        return new LoanAddCommand(FIRST_UUID, FIRST_MEMBER.getNusId(), assigned, expected, CLOCK);
    }
}
