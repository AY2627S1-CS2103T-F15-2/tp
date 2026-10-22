package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.DisplayRequest;
import seedu.address.logic.commands.RedoCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.AddressBookHistory;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.equipment.Availability;
import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.Equipment;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.TypicalMembers;
import seedu.address.testutil.TypicalPersons;

class UndoRedoIntegrationTest {
    private static final UUID UNIT = new UUID(0, 1);
    private static final String MEMBER = TypicalMembers.ALEX.getNusId().value;
    private static final String ADD_MEMBER = "member add nus/A9999999X name/Robin Tan"
            + " phone/91234567 email/robin@example.com";

    @TempDir
    public Path directory;

    private ModelManager model;
    private FailingStorage storage;
    private LogicManager logic;

    @BeforeEach
    void setUp() throws Exception {
        AddressBook data = new AddressBook();
        data.addPerson(TypicalPersons.ALICE);
        data.addMember(TypicalMembers.ALEX);
        data.addEquipment(new Equipment(UNIT, "Camera", "Photo", Condition.GOOD, "Original"));
        model = new ModelManager(data, new UserPrefs());
        storage = new FailingStorage(directory);
        storage.saveAddressBook(data);
        logic = new LogicManager(model, storage);
    }

    @Test
    void batchCreation_undoAndRedo_restoresExactUuidsAndSavedState() throws Exception {
        ReadOnlyAddressBook root = model.getAddressBook();
        var members = root.getMemberList();
        var equipment = root.getEquipmentList();
        var loans = root.getLoanList();
        AddressBook before = snapshot();
        logic.execute("equipment add name/Camera category/Photo condition/GOOD count/3");
        AddressBook added = snapshot();
        assertEquals(4, added.getEquipmentList().size());
        assertEquals("Undid: equipment add.", logic.execute("undo").getFeedbackToUser());
        assertState(before);
        assertUnavailable("undo", UndoCommand.MESSAGE_UNAVAILABLE);
        assertEquals("Redid: equipment add.", logic.execute("redo").getFeedbackToUser());
        assertState(added);
        assertUnavailable("redo", RedoCommand.MESSAGE_UNAVAILABLE);
        assertSame(root, model.getAddressBook());
        assertSame(members, root.getMemberList());
        assertSame(equipment, root.getEquipmentList());
        assertSame(loans, root.getLoanList());
        assertEquals(before.getPersonList(), root.getPersonList());
    }

    @Test
    void lifecycleChanges_roundTripMemberAndEquipmentRecords() throws Exception {
        roundTrip(ADD_MEMBER);
        roundTrip("member edit nus/" + MEMBER + " name/Updated Member phone/98765432 email/updated@example.com");
        roundTrip("member delete nus/" + MEMBER);
        roundTrip("member restore nus/" + MEMBER);
        roundTrip("equipment edit uuid/" + UNIT + " name/Tripod category/Accessories"
                + " condition/FAIR notes/Updated notes");
        roundTrip("equipment delete uuid/" + UNIT);
        roundTrip("equipment restore uuid/" + UNIT);
    }

    @Test
    void loanBatchChanges_restoreReferencesDatesConditionsAndDerivedAvailability() throws Exception {
        logic.execute("equipment add name/Camera category/Photo condition/FAIR count/2");
        List<UUID> targets = model.getEquipmentList().stream().map(Equipment::getUuid).toList();
        String identifiers = String.join(",", targets.stream().map(UUID::toString).toList());
        LocalDate assigned = LocalDate.now().minusDays(2);
        roundTrip("loans add uuid/" + identifiers + " nus/" + MEMBER
                + " assigned/" + assigned + " expected/" + assigned.plusDays(3));
        AddressBook open = snapshot();
        logic.execute("loans return uuid/" + identifiers + " returned/" + LocalDate.now() + " condition/DAMAGED");
        AddressBook returned = snapshot();
        assertTrue(model.getLoanList().stream().noneMatch(loan -> loan.isOpen()));
        targets.forEach(id -> assertEquals(Availability.UNAVAILABLE, model.getAvailability(id)));
        logic.execute("undo");
        assertState(open);
        targets.forEach(id -> assertEquals(Availability.ASSIGNED, model.getAvailability(id)));
        assertEquals(3, model.findOpenLoansForMember(TypicalMembers.ALEX.getNusId()).size());
        logic.execute("redo");
        assertState(returned);
        assertEquals(Condition.DAMAGED, model.findEquipment(UNIT).orElseThrow().getCondition());
        assertTrue(model.getLoanList().stream().allMatch(loan ->
                loan.getReturnCondition().orElseThrow() == Condition.DAMAGED));
    }

    @Test
    void queriesErrorsAndAppearance_keepRedoAndDoNotCreateHistoryEntries() throws Exception {
        logic.execute(ADD_MEMBER);
        AddressBook added = snapshot();
        logic.execute("undo");
        AddressBookHistory undone = model.getAddressBookHistory();
        for (String query : List.of("member list", "equipment list", "loans list",
                "member search q/Alex", "equipment search availability/available",
                "member view nus/" + MEMBER, "equipment view uuid/" + UNIT, "help", "mode dark")) {
            logic.execute(query);
            assertSame(undone, model.getAddressBookHistory(), query);
        }
        for (String invalid : List.of("undo 2", "redo count/1", "member add", "not-a-command")) {
            assertThrows(ParseException.class, () -> logic.execute(invalid));
            assertSame(undone, model.getAddressBookHistory());
        }
        for (String invalid : List.of("member edit nus/" + MEMBER + " name/"
                + TypicalMembers.ALEX.getName().fullName, "equipment edit uuid/" + UNIT + " condition/GOOD",
                "member delete nus/A8888888X")) {
            assertThrows(CommandException.class, () -> logic.execute(invalid));
            assertSame(undone, model.getAddressBookHistory());
        }
        logic.execute("redo");
        assertState(added);
        assertTrue(model.getGuiSettings().isDarkMode());
        logic.execute("undo");
        logic.execute("equipment edit uuid/" + UNIT + " notes/Another branch");
        assertUnavailable("redo", RedoCommand.MESSAGE_UNAVAILABLE);
    }

    @Test
    void saveFailures_preserveRecordsHistoryAndFileAndAllowRetry() throws Exception {
        logic.execute(ADD_MEMBER);
        AddressBook added = snapshot();
        for (IOException failure : List.of(new IOException("write failed"), new AccessDeniedException("denied"))) {
            AddressBookHistory originalHistory = model.getAddressBookHistory();
            byte[] originalBytes = Files.readAllBytes(storage.getAddressBookFilePath());
            storage.failure = failure;
            CommandException undoFailure = assertThrows(CommandException.class, () -> logic.execute("undo"));
            assertSame(failure, undoFailure.getCause());
            assertSame(originalHistory, model.getAddressBookHistory());
            assertEquals(added, model.getAddressBook());
            assertArrayEquals(originalBytes, Files.readAllBytes(storage.getAddressBookFilePath()));
            storage.failure = null;
            logic.execute("undo");
            AddressBook undone = snapshot();
            AddressBookHistory redoHistory = model.getAddressBookHistory();
            byte[] undoneBytes = Files.readAllBytes(storage.getAddressBookFilePath());
            storage.failure = failure;
            CommandException redoFailure = assertThrows(CommandException.class, () -> logic.execute("redo"));
            assertSame(failure, redoFailure.getCause());
            assertSame(redoHistory, model.getAddressBookHistory());
            assertEquals(undone, model.getAddressBook());
            assertArrayEquals(undoneBytes, Files.readAllBytes(storage.getAddressBookFilePath()));
            assertThrows(CommandException.class, () ->
                    logic.execute("equipment edit uuid/" + UNIT + " notes/Unsaved branch"));
            assertSame(redoHistory, model.getAddressBookHistory());
            assertEquals(undone, model.getAddressBook());
            storage.failure = null;
            logic.execute("redo");
            assertState(added);
        }
    }

    @Test
    void tenStepLimit_boundsBothDirectionsAndFailedChangesDoNotEvictHistory() throws Exception {
        List<AddressBook> states = new ArrayList<>();
        states.add(snapshot());
        for (int index = 1; index <= 12; index++) {
            logic.execute("equipment edit uuid/" + UNIT + " notes/Revision " + index);
            states.add(snapshot());
        }
        AddressBookHistory fullHistory = model.getAddressBookHistory();
        storage.failure = new IOException("failed at history limit");
        assertThrows(CommandException.class, () ->
                logic.execute("equipment edit uuid/" + UNIT + " notes/Unsaved revision"));
        assertSame(fullHistory, model.getAddressBookHistory());
        storage.failure = null;
        for (int index = 11; index >= 2; index--) {
            logic.execute("undo");
            assertState(states.get(index));
        }
        assertUnavailable("undo", UndoCommand.MESSAGE_UNAVAILABLE);
        for (int index = 3; index <= 12; index++) {
            logic.execute("redo");
            assertState(states.get(index));
        }
        assertUnavailable("redo", RedoCommand.MESSAGE_UNAVAILABLE);
    }

    @Test
    void restart_keepsRestoredDataAndStartsWithEmptyHistory() throws Exception {
        logic.execute(ADD_MEMBER);
        logic.execute("equipment add name/Tripod category/Photo condition/GOOD");
        logic.execute("undo");
        AddressBook restored = snapshot();
        ModelManager restarted = new ModelManager(storage.readAddressBook().orElseThrow(), model.getUserPrefs());
        LogicManager restartedLogic = new LogicManager(restarted, storage);
        assertEquals(restored, restarted.getAddressBook());
        assertFalse(restarted.canUndoAddressBook());
        assertFalse(restarted.canRedoAddressBook());
        assertThrows(CommandException.class, () -> restartedLogic.execute("undo"));
        assertThrows(CommandException.class, () -> restartedLogic.execute("redo"));
        assertFalse(Files.readString(storage.getAddressBookFilePath()).contains("history"));
    }

    @Test
    void transactionCandidate_isolatesHistoryAndPreservesLivePreferencesOnPublication() throws Exception {
        logic.execute(ADD_MEMBER);
        var candidate = model.createTransactionSnapshot();
        assertSame(model.getAddressBookHistory(), candidate.getAddressBookHistory());
        candidate.undoAddressBook();
        assertTrue(model.canUndoAddressBook());
        assertFalse(model.canRedoAddressBook());
        assertTrue(candidate.canRedoAddressBook());
        model.setGuiSettings(model.getGuiSettings().withDarkMode(true));
        model.publishTransaction(candidate);
        assertTrue(model.getGuiSettings().isDarkMode());
        assertSame(candidate.getAddressBookHistory(), model.getAddressBookHistory());
        model.setAddressBook(new AddressBook());
        assertFalse(model.canUndoAddressBook());
        assertFalse(model.canRedoAddressBook());
    }

    private void roundTrip(String command) throws Exception {
        AddressBook before = snapshot();
        logic.execute(command);
        AddressBook after = snapshot();
        assertEquals(DisplayRequest.unchanged(), logic.execute("undo").getDisplayRequest());
        assertState(before);
        assertEquals(DisplayRequest.unchanged(), logic.execute("redo").getDisplayRequest());
        assertState(after);
    }

    private AddressBook snapshot() {
        return new AddressBook(model.getAddressBook());
    }

    private void assertState(AddressBook expected) throws Exception {
        assertEquals(expected, model.getAddressBook());
        assertEquals(expected, storage.readAddressBook().orElseThrow());
    }

    private void assertUnavailable(String command, String message) throws Exception {
        AddressBook before = snapshot();
        AddressBookHistory history = model.getAddressBookHistory();
        byte[] bytes = Files.readAllBytes(storage.getAddressBookFilePath());
        int saves = storage.saves;
        CommandException error = assertThrows(CommandException.class, () -> logic.execute(command));
        assertEquals(message, error.getMessage());
        assertEquals(before, model.getAddressBook());
        assertSame(history, model.getAddressBookHistory());
        assertArrayEquals(bytes, Files.readAllBytes(storage.getAddressBookFilePath()));
        assertEquals(saves, storage.saves);
    }

    private static class FailingStorage extends StorageManager {
        private IOException failure;
        private int saves;

        FailingStorage(Path directory) {
            super(new JsonAddressBookStorage(directory.resolve("addressbook.json")),
                    new JsonUserPrefsStorage(directory.resolve("preferences.json")));
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook data) throws IOException {
            saves++;
            if (failure != null) {
                throw failure;
            }
            super.saveAddressBook(data);
        }
    }
}
