package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.LoanCommandTestData.CLOCK;
import static seedu.address.logic.commands.LoanCommandTestData.FIRST_MEMBER;
import static seedu.address.logic.commands.LoanCommandTestData.FIRST_UUID;
import static seedu.address.logic.commands.LoanCommandTestData.SECOND_MEMBER;
import static seedu.address.logic.commands.LoanCommandTestData.SECOND_UUID;
import static seedu.address.logic.commands.LoanCommandTestData.TODAY;
import static seedu.address.logic.commands.LoanCommandTestData.UNKNOWN_UUID;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.LoanAddCommandParser;
import seedu.address.logic.parser.LoanReturnCommandParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.equipment.Availability;
import seedu.address.model.equipment.Condition;
import seedu.address.model.loan.Loan;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Direct command/parser/model/storage integration; Duy owns activation through LogicManager. */
public class LoanCommandIntegrationTest {
    @TempDir
    public Path testFolder;

    @Test
    public void routedAssignment_historicalOverlap_rejectsSingleAndBatchWithoutSavingOrClearingRedo() throws Exception {
        ModelManager model = LoanCommandTestData.model();
        LocalDate returned = TODAY.minusDays(5);
        model.addLoan(new Loan(FIRST_UUID, FIRST_MEMBER.getNusId(), TODAY.minusDays(10), TODAY, returned));
        JsonAddressBookStorage disk = new JsonAddressBookStorage(testFolder.resolve("overlap.json"));
        LogicManager logic = new LogicManager(model, new StorageManager(disk,
                new JsonUserPrefsStorage(testFolder.resolve("prefs.json"))));
        logic.execute("equipment edit uuid/" + FIRST_UUID + " notes/Changed");
        logic.execute("undo");
        AddressBook before = new AddressBook(model.getAddressBook());
        String bytes = Files.readString(disk.getAddressBookFilePath());
        var history = model.getAddressBookHistory();
        for (String identifiers : List.of(FIRST_UUID.toString(), SECOND_UUID + "," + FIRST_UUID)) {
            String command = "loans add uuid/" + identifiers + " nus/" + SECOND_MEMBER.getNusId()
                    + " assigned/" + TODAY.minusDays(7) + " expected/" + TODAY;
            CommandException failure = assertThrows(CommandException.class, () -> logic.execute(command));
            assertEquals(String.format(LoanAddCommand.MESSAGE_HISTORICAL_OVERLAP, FIRST_UUID, returned),
                    failure.getMessage());
            assertEquals(before, model.getAddressBook());
            assertSame(history, model.getAddressBookHistory());
            assertTrue(model.canRedoAddressBook());
            assertEquals(bytes, Files.readString(disk.getAddressBookFilePath()));
            assertEquals(before, disk.readAddressBook().orElseThrow());
        }
        logic.execute("loans add uuid/" + FIRST_UUID + " nus/" + SECOND_MEMBER.getNusId()
                + " assigned/" + returned + " expected/" + TODAY);
        assertEquals(SECOND_MEMBER.getNusId(), model.findOpenLoan(FIRST_UUID).orElseThrow().getMemberNusId());
        assertFalse(model.canRedoAddressBook());
        assertEquals(model.getAddressBook(), disk.readAddressBook().orElseThrow());
    }

    @Test
    public void parserCommands_saveAndReload_openReturnedAndReassignedLoansPreserveAllCollections() throws Exception {
        ModelManager model = LoanCommandTestData.model();
        AddressBook initial = new AddressBook(model.getAddressBook());
        LoanAddCommandParser addParser = new LoanAddCommandParser();
        LoanReturnCommandParser returnParser = new LoanReturnCommandParser();
        LocalDate today = LocalDate.now();
        LocalDate assigned = today.minusDays(3);
        LocalDate expected = today.plusDays(5);
        LocalDate returned = today.minusDays(1);
        CommandResult added = addParser.parse("uuid/" + FIRST_UUID + " nus/" + SECOND_MEMBER.getNusId()
                + " assigned/" + assigned + " expected/" + expected).execute(model);
        addParser.parse("expected/" + expected + " assigned/" + assigned + " nus/" + FIRST_MEMBER.getNusId()
                + " uuid/" + SECOND_UUID).execute(model);
        assertEquals(DisplayRequest.equipmentDetails(FIRST_UUID), added.getDisplayRequest());
        assertEquals(SECOND_MEMBER.getNusId(), model.findOpenLoan(FIRST_UUID).orElseThrow().getMemberNusId());
        assertEquals(1, model.findOpenLoansForMember(FIRST_MEMBER.getNusId()).size());
        assertEquals(1, model.findOpenLoansForMember(SECOND_MEMBER.getNusId()).size());

        JsonAddressBookStorage storage = new JsonAddressBookStorage(testFolder.resolve("loans.json"));
        storage.saveAddressBook(model.getAddressBook());
        ModelManager openReload = reload(storage);
        assertEquals(model.getAddressBook(), openReload.getAddressBook());
        assertEquals(Availability.ASSIGNED, openReload.getAvailability(FIRST_UUID));
        assertEquals(assigned, openReload.findOpenLoan(FIRST_UUID).orElseThrow().getAssignedDate());
        assertEquals(expected, openReload.findOpenLoan(FIRST_UUID).orElseThrow().getExpectedReturnDate());
        Loan originalOpen = openReload.findOpenLoan(FIRST_UUID).orElseThrow();
        CommandResult closed = returnParser.parse("returned/" + returned + " uuid/" + FIRST_UUID).execute(openReload);
        assertEquals(DisplayRequest.equipmentDetails(FIRST_UUID), closed.getDisplayRequest());
        assertTrue(originalOpen.isOpen());
        assertEquals(Availability.AVAILABLE, openReload.getAvailability(FIRST_UUID));
        assertEquals(Availability.ASSIGNED, openReload.getAvailability(SECOND_UUID));
        assertTrue(openReload.findOpenLoansForMember(SECOND_MEMBER.getNusId()).isEmpty());
        storage.saveAddressBook(openReload.getAddressBook());
        ModelManager closedReload = reload(storage);
        assertEquals(openReload.getAddressBook(), closedReload.getAddressBook());
        assertEquals(originalOpen.withReturnedDate(returned), closedReload.getLoanList().get(1));
        assertEquals(Optional.of(returned), closedReload.getLoanList().get(1).getReturnedDate());
        assertTrue(closedReload.findOpenLoan(FIRST_UUID).isEmpty());

        addParser.parse("uuid/" + FIRST_UUID + " nus/" + FIRST_MEMBER.getNusId()
                + " assigned/" + returned + " expected/" + expected).execute(closedReload);
        storage.saveAddressBook(closedReload.getAddressBook());
        ModelManager finalReload = reload(storage);
        assertEquals(closedReload.getAddressBook(), finalReload.getAddressBook());
        assertEquals(4, finalReload.getLoanList().size());
        assertEquals(FIRST_MEMBER.getNusId(), finalReload.findOpenLoan(FIRST_UUID).orElseThrow().getMemberNusId());
        assertEquals(initial.getLoanList().get(0), finalReload.getLoanList().get(0));
        assertEquals(initial.getPersonList(), finalReload.getAddressBook().getPersonList());
        assertEquals(initial.getMemberList(), finalReload.getMemberList());
        assertEquals(initial.getEquipmentList(), finalReload.getEquipmentList());
        String json = Files.readString(storage.getAddressBookFilePath());
        assertFalse(json.contains("availability"));
        assertFalse(json.contains("holder"));
    }

    @Test
    public void fixedClockCommands_roundTripRetainsDatesAndDeletionRules() throws Exception {
        ModelManager model = LoanCommandTestData.model();
        new LoanAddCommand(FIRST_UUID, FIRST_MEMBER.getNusId(), TODAY, TODAY.plusDays(7), CLOCK).execute(model);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(testFolder.resolve("fixed-clock.json"));
        storage.saveAddressBook(model.getAddressBook());
        ModelManager restored = reload(storage);
        AddressBook assignedSnapshot = new AddressBook(restored.getAddressBook());
        assertThrows(IllegalStateException.class, () -> restored.deleteMember(FIRST_MEMBER));
        assertThrows(IllegalStateException.class, () -> restored.deleteEquipment(restored.findEquipment(FIRST_UUID)
                .orElseThrow()));
        assertEquals(assignedSnapshot, restored.getAddressBook());
        new LoanReturnCommand(FIRST_UUID, TODAY, CLOCK).execute(restored);
        storage.saveAddressBook(restored.getAddressBook());
        ModelManager returned = reload(storage);
        Loan closed = returned.getLoanList().get(1);
        assertEquals(new Loan(FIRST_UUID, FIRST_MEMBER.getNusId(), TODAY, TODAY.plusDays(7),
                TODAY, Condition.GOOD, null), closed);
        assertEquals(Availability.AVAILABLE, returned.getAvailability(FIRST_UUID));
        returned.deleteMember(FIRST_MEMBER);
        assertTrue(returned.findMember(FIRST_MEMBER.getNusId()).orElseThrow().isDeleted());
        assertEquals(2, returned.getMemberList().size());
        assertEquals(2, returned.getLoanList().size());
        assertEquals(closed, returned.getLoanList().get(1));
        storage.saveAddressBook(returned.getAddressBook());
        assertEquals(returned.getAddressBook(), reload(storage).getAddressBook());
    }

    @Test
    public void rejectedParsersAndCommands_preserveCompleteModelAndPreviouslySavedJson() throws Exception {
        ModelManager model = LoanCommandTestData.model();
        new LoanAddCommand(FIRST_UUID, FIRST_MEMBER.getNusId(), TODAY.minusDays(2), TODAY, CLOCK).execute(model);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(testFolder.resolve("rejected.json"));
        storage.saveAddressBook(model.getAddressBook());
        AddressBook before = new AddressBook(model.getAddressBook());
        String saved = Files.readString(storage.getAddressBookFilePath());
        for (Command command : List.of(new LoanAddCommand(FIRST_UUID, SECOND_MEMBER.getNusId(), TODAY, TODAY, CLOCK),
                new LoanReturnCommand(FIRST_UUID, TODAY.plusDays(1), CLOCK),
                new LoanReturnCommand(FIRST_UUID, TODAY.minusDays(3), CLOCK),
                new LoanAddCommand(UNKNOWN_UUID, FIRST_MEMBER.getNusId(), TODAY, TODAY, CLOCK),
                new LoanReturnCommand(UNKNOWN_UUID, TODAY, CLOCK))) {
            assertThrows(CommandException.class, () -> command.execute(model));
            assertEquals(before, model.getAddressBook());
            assertEquals(saved, Files.readString(storage.getAddressBookFilePath()));
        }
        assertThrows(ParseException.class, () -> new LoanAddCommandParser().parse("uuid/" + FIRST_UUID
                + " nus/" + FIRST_MEMBER.getNusId() + " assigned/" + TODAY + " expected/" + TODAY
                + " uuid/" + FIRST_UUID).execute(model));
        assertThrows(ParseException.class, () -> new LoanReturnCommandParser().parse(
                "uuid/" + FIRST_UUID + " returned/" + TODAY + " unexpected/value").execute(model));
        assertEquals(before, model.getAddressBook());
        assertEquals(saved, Files.readString(storage.getAddressBookFilePath()));
        assertEquals(before, reload(storage).getAddressBook());
    }

    private static ModelManager reload(JsonAddressBookStorage storage) throws Exception {
        return new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
    }
}
