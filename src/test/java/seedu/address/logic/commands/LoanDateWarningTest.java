package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.LoanCommandTestData.CLOCK;
import static seedu.address.logic.commands.LoanCommandTestData.FIRST_MEMBER;
import static seedu.address.logic.commands.LoanCommandTestData.FIRST_UUID;
import static seedu.address.logic.commands.LoanCommandTestData.SECOND_UUID;
import static seedu.address.logic.commands.LoanCommandTestData.TODAY;
import static seedu.address.logic.commands.LoanCommandTestData.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.equipment.Condition;
import seedu.address.model.loan.Loan;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Checks date advisories without relaxing validation or changing loading and transaction behavior. */
public class LoanDateWarningTest {
    @TempDir
    public Path directory;

    @Test
    public void assignment_tenYearBoundaries_warnsOnlyBeyondTheAnniversary() throws Exception {
        LocalDate past = TODAY.minusYears(10);
        LocalDate future = TODAY.plusYears(10);
        for (int offset : List.of(-1, 0, 1)) {
            ModelManager model = model();
            var result = new LoanAddCommand(FIRST_UUID, FIRST_MEMBER.getNusId(), past.plusDays(offset),
                    TODAY, CLOCK).execute(model);
            assertEquals(offset < 0, result.getFeedbackToUser().contains("Warning: assigned/"));
            assertEquals(past.plusDays(offset), model.findOpenLoan(FIRST_UUID).orElseThrow().getAssignedDate());
            result = new LoanAddCommand(FIRST_UUID, FIRST_MEMBER.getNusId(), TODAY,
                    future.plusDays(offset), CLOCK).execute(model());
            assertEquals(offset > 0, result.getFeedbackToUser().contains("Warning: expected/"));
        }
    }

    @Test
    public void return_tenYearBoundaries_warnsAndRetainsTheActualReturn() throws Exception {
        LocalDate past = TODAY.minusYears(10);
        for (int offset : List.of(-1, 0, 1)) {
            ModelManager model = withOldLoan(past.minusDays(1));
            var result = new LoanReturnCommand(FIRST_UUID, past.plusDays(offset), CLOCK).execute(model);
            assertEquals(offset < 0, result.getFeedbackToUser().contains("Warning: returned/"));
            assertEquals(past.plusDays(offset), model.getLoanList().getLast().getReturnedDate().orElseThrow());
        }
    }

    @Test
    public void batch_warnsOnceForTheSharedDateAndReturnsNormalNavigation() throws Exception {
        ModelManager model = model();
        var result = new LoanAddCommand(List.of(FIRST_UUID, SECOND_UUID), FIRST_MEMBER.getNusId(), TODAY,
                LocalDate.of(9999, 12, 31), CLOCK).execute(model);
        assertEquals(1, result.getFeedbackToUser().lines().filter(line -> line.startsWith("Warning:")).count());
        assertTrue(result.getFeedbackToUser().contains(FIRST_UUID.toString()));
        assertTrue(result.getFeedbackToUser().contains(SECOND_UUID.toString()));
        assertEquals(DisplayRequest.loans(), result.getDisplayRequest());
        assertEquals(2, model.findOpenLoansForMember(FIRST_MEMBER.getNusId()).size());
    }

    @Test
    public void warnings_useTheExecutionClockAndCalendarYears() throws Exception {
        LoanCommandTestData.MutableClock clock = new LoanCommandTestData.MutableClock();
        LocalDate past = TODAY.minusYears(10);
        LoanAddCommand assignment = new LoanAddCommand(FIRST_UUID, FIRST_MEMBER.getNusId(), past, TODAY, clock);
        LoanReturnCommand returned = new LoanReturnCommand(FIRST_UUID, past, clock);
        clock.advanceDay();
        assertTrue(assignment.execute(model()).getFeedbackToUser().contains("Warning: assigned/"));
        assertTrue(returned.execute(withOldLoan(past)).getFeedbackToUser().contains("Warning: returned/"));
        Clock leapDay = Clock.fixed(Instant.parse("2024-02-29T12:00:00Z"), ZoneOffset.UTC);
        for (int day : List.of(27, 28)) {
            var result = new LoanAddCommand(FIRST_UUID, FIRST_MEMBER.getNusId(), LocalDate.of(2014, 2, day),
                    LocalDate.of(2024, 2, 29), leapDay).execute(model());
            assertEquals(day == 27, result.getFeedbackToUser().contains("Warning: assigned/"));
        }
    }

    @Test
    public void extremeDates_realCommandsSaveReloadAndRetainHistory() throws Exception {
        ModelManager model = model();
        StorageManager storage = storage();
        LogicManager logic = new LogicManager(model, storage);
        var result = logic.execute("loans add uuid/" + FIRST_UUID + " nus/" + FIRST_MEMBER.getNusId()
                + " assigned/0001-01-01 expected/9999-12-31");
        assertTrue(result.getFeedbackToUser().contains("Warning: assigned/"));
        assertTrue(result.getFeedbackToUser().contains("Warning: expected/"));
        assertEquals(LocalDate.of(9999, 12, 31), storage.readAddressBook().orElseThrow().getLoanList()
                .getLast().getExpectedReturnDate());
        result = logic.execute("loans return uuid/" + FIRST_UUID + " returned/0001-01-01");
        assertTrue(result.getFeedbackToUser().contains("Warning: returned/"));
        assertEquals(new AddressBook(model.getAddressBook()),
                new AddressBook(storage.readAddressBook().orElseThrow()));
        assertEquals(LocalDate.of(1, 1, 1), model.getLoanList().getLast().getReturnedDate().orElseThrow());
        assertTrue(model.canUndoAddressBook());
    }

    @Test
    public void failedSave_preservesRecordsBytesAndHistoryWithoutSuccessWarning() throws Exception {
        ModelManager model = model();
        StorageManager storage = storage();
        storage.saveAddressBook(model.getAddressBook());
        byte[] saved = Files.readAllBytes(storage.getAddressBookFilePath());
        AddressBook before = new AddressBook(model.getAddressBook());
        StorageManager failing = new StorageManager(new JsonAddressBookStorage(storage.getAddressBookFilePath()),
                new JsonUserPrefsStorage(directory.resolve("prefs.json"))) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook data) throws IOException {
                throw new IOException("Injected save failure");
            }
        };
        CommandException failure = assertThrows(CommandException.class, () -> new LogicManager(model, failing)
                .execute("loans add uuid/" + FIRST_UUID + " nus/" + FIRST_MEMBER.getNusId()
                        + " assigned/0001-01-01 expected/9999-12-31"));
        assertFalse(failure.getMessage().contains("Warning:"));
        assertEquals(before, model.getAddressBook());
        assertArrayEquals(saved, Files.readAllBytes(storage.getAddressBookFilePath()));
        assertFalse(model.canUndoAddressBook());
    }

    private StorageManager storage() {
        return new StorageManager(new JsonAddressBookStorage(directory.resolve("data.json")),
                new JsonUserPrefsStorage(directory.resolve("prefs.json")));
    }

    private ModelManager withOldLoan(LocalDate assigned) {
        ModelManager model = model();
        model.addLoan(new Loan(FIRST_UUID, FIRST_MEMBER.getNusId(), assigned, assigned,
                null, Condition.GOOD, null));
        return model;
    }
}
