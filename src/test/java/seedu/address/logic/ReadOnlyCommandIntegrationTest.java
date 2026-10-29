package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.ViewTestData.ALEX;
import static seedu.address.testutil.ViewTestData.CAMERA;
import static seedu.address.testutil.ViewTestData.OTHER_ALEX;
import static seedu.address.testutil.ViewTestData.OTHER_CAMERA;
import static seedu.address.testutil.ViewTestData.SPEAKER;
import static seedu.address.testutil.ViewTestData.loan;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DisplayRequest;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.LoanListCommand;
import seedu.address.model.AddressBook;
import seedu.address.model.AddressBookHistory;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.query.DirectoryQuery;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.ViewTestData;

/**
 * Verifies that routed queries and utility commands do not depend on record persistence.
 */
public class ReadOnlyCommandIntegrationTest {
    private static final Map<String, CommandResult> EXPECTED_RESULTS = Map.of(
            "member list", new CommandResult("Listed 2 active members.", DisplayRequest.members(DirectoryQuery.all())),
            "loans list", new CommandResult(LoanListCommand.MESSAGE_SUCCESS, DisplayRequest.loans()),
            "member view nus/" + ALEX.getNusId(), new CommandResult("Viewing member: Alex Tan ("
                    + ALEX.getNusId() + "). Currently holds 1 item(s).", DisplayRequest.memberDetails(ALEX.getNusId())),
            "equipment view uuid/" + CAMERA.getUuid(), new CommandResult("Viewing equipment: Camera ("
                    + CAMERA.getUuid() + "). Availability: ASSIGNED.",
                    DisplayRequest.equipmentDetails(CAMERA.getUuid())),
            "help", new CommandResult(HelpCommand.SHOWING_HELP_MESSAGE, true, false),
            "exit", new CommandResult(ExitCommand.MESSAGE_EXIT_ACKNOWLEDGEMENT, false, true));

    @TempDir
    public Path directory;

    private ModelManager model;
    private RecordingStorage storage;
    private LogicManager logic;

    @BeforeEach
    public void setUp() {
        model = ViewTestData.model();
        model.addLoan(loan(CAMERA, ALEX, true));
        model.addLoan(loan(OTHER_CAMERA, OTHER_ALEX, false));
        model.deleteEquipment(SPEAKER);
        model.updateFilteredPersonList(person -> false);
        storage = new RecordingStorage(directory);
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_readOnlyCommands_doNotCreateDataFile() throws Exception {
        assertReadOnlyResults();
        assertFalse(Files.exists(storage.getAddressBookFilePath()));
    }

    @Test
    public void execute_readOnlyCommands_preserveHumanEditedBytesAndTimestamp() throws Exception {
        storage.saveAddressBook(model.getAddressBook());
        Path file = storage.getAddressBookFilePath();
        Files.writeString(file, "\n  " + Files.readString(file) + "\n\n");
        Files.setLastModifiedTime(file, FileTime.from(Instant.parse("2000-01-01T00:00:00Z")));
        byte[] before = Files.readAllBytes(file);
        FileTime modified = Files.getLastModifiedTime(file);
        storage.saveAttempts = 0;

        assertReadOnlyResults();

        assertArrayEquals(before, Files.readAllBytes(file));
        assertEquals(modified, Files.getLastModifiedTime(file));
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_storageUnavailable_returnsReadOnlyResultsAndPreservesRedo() throws Exception {
        logic.execute("equipment edit uuid/" + CAMERA.getUuid() + " notes/Revised notes");
        logic.execute("undo");
        byte[] before = Files.readAllBytes(storage.getAddressBookFilePath());
        AddressBookHistory history = model.getAddressBookHistory();
        storage.saveAttempts = 0;

        for (IOException failure : List.of(new IOException("Controlled save failure"),
                new AccessDeniedException("Controlled access denial"))) {
            storage.failure = failure;
            assertReadOnlyResults();
            assertSame(history, model.getAddressBookHistory());
            assertTrue(model.canRedoAddressBook());
            assertArrayEquals(before, Files.readAllBytes(storage.getAddressBookFilePath()));
        }

        storage.failure = null;
        logic.execute("redo");
        assertEquals(1, storage.saveAttempts);
        assertEquals("Revised notes", model.findEquipment(CAMERA.getUuid()).orElseThrow().getNotes());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    /**
     * Checks navigation and utility results without changing records, history, filtering or preferences.
     */
    private void assertReadOnlyResults() throws Exception {
        ReadOnlyAddressBook root = model.getAddressBook();
        AddressBook before = new AddressBook(root);
        AddressBookHistory history = model.getAddressBookHistory();
        var settings = model.getGuiSettings();
        for (Map.Entry<String, CommandResult> entry : EXPECTED_RESULTS.entrySet()) {
            assertEquals(entry.getValue(), logic.execute(entry.getKey()), entry.getKey());
            assertEquals(0, storage.saveAttempts, entry.getKey());
            assertSame(root, model.getAddressBook());
            assertEquals(before, model.getAddressBook(), entry.getKey());
            assertSame(history, model.getAddressBookHistory());
            assertTrue(model.getFilteredPersonList().isEmpty());
            assertEquals(settings, model.getGuiSettings());
        }
    }

    /**
     * Counts save attempts and supplies recoverable failures while retaining real JSON storage.
     */
    private static class RecordingStorage extends StorageManager {
        private int saveAttempts;
        private IOException failure;

        RecordingStorage(Path directory) {
            super(new JsonAddressBookStorage(directory.resolve("records.json")),
                    new JsonUserPrefsStorage(directory.resolve("prefs.json")));
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook data) throws IOException {
            saveAttempts++;
            if (failure != null) {
                throw failure;
            }
            super.saveAddressBook(data);
        }
    }
}
