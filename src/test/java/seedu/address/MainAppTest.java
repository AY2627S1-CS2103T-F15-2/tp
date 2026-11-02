package seedu.address;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.commons.core.AppPaths;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.Storage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.FxTestUtil;
import seedu.address.testutil.ViewTestData;

/**
 * Verifies startup paths, preference recovery, and fail-closed startup with real storage and JavaFX guidance.
 */
public class MainAppTest {
    @TempDir
    public Path directory;

    @Test
    public void init_nullGuiSettings_recoversAndSavesDefaults() throws Exception {
        assertStartupPreferences("{\"guiSettings\":null}", new UserPrefs());
    }

    @Test
    public void init_missingGuiSettings_preservesDefaultPreferences() throws Exception {
        assertStartupPreferences("{}", new UserPrefs());
    }

    @Test
    public void init_validGuiSettings_preservesGeometryAndDarkMode() throws Exception {
        UserPrefs expected = new UserPrefs();
        expected.setGuiSettings(new GuiSettings(1200, 800, 50, 70).withDarkMode(true));
        assertStartupPreferences(JsonUtil.toJsonString(expected), expected);
    }

    /**
     * Verifies the real startup constructs a model and rewrites usable preferences.
     */
    private void assertStartupPreferences(String preferencesJson, UserPrefs expected) throws Exception {
        Path prefsFile = directory.resolve("preferences.json");
        Files.writeString(prefsFile, preferencesJson);
        JsonUserPrefsStorage prefsStorage = new JsonUserPrefsStorage(prefsFile);
        Storage isolatedStorage = new StorageManager(
                new JsonAddressBookStorage(directory.resolve("data.json")), prefsStorage);
        MainApp app = new MainApp() {
            @Override
            protected UserPrefs initPrefs(JsonUserPrefsStorage storage) {
                return super.initPrefs(prefsStorage);
            }

            @Override
            void initModel(Storage storage, ReadOnlyUserPrefs userPrefs) {
                super.initModel(isolatedStorage, userPrefs);
            }
        };

        app.init();

        assertEquals(expected, app.model.getUserPrefs());
        assertEquals(expected, prefsStorage.readUserPrefs().orElseThrow());
        assertNotNull(app.logic);
        assertNotNull(app.ui);
    }

    @Test
    public void init_defaultStorage_usesRuntimeHomeForPreferencesAndData() throws Exception {
        MainApp app = new MainApp() {
            @Override
            protected UserPrefs initPrefs(JsonUserPrefsStorage storage) {
                // Inspect the real startup path without writing preferences in the development folder.
                assertEquals(AppPaths.getPreferencesFilePath(), storage.getUserPrefsFilePath());
                return new UserPrefs();
            }

            @Override
            void initModel(Storage storage, ReadOnlyUserPrefs userPrefs) {
                // Verify default paths without loading the development checkout's real dataset.
                assertEquals(AppPaths.getDataFilePath(), storage.getAddressBookFilePath());
            }
        };
        app.init();
        assertEquals(AppPaths.getPreferencesFilePath(), app.storage.getUserPrefsFilePath());
        assertEquals(AppPaths.getDataFilePath(), app.storage.getAddressBookFilePath());
    }

    @Test
    public void initModel_malformedContacts_omitsSourceAndContactsFromLogsButKeepsRecovery() throws Exception {
        String malformed = """
                {"persons":[],"members":[{"nusId":"A8400001X","name":"Privacy Example",
                "phone":"91118888","email":"privacy-audit@example.com"}],"equipment":[INVALID],"loans":[]}
                """;
        assertPrivateStartupDiagnostics(malformed, "JsonUtil", Level.WARNING);
    }

    @Test
    public void initModel_invalidStoredIdentities_omitsContactsFromLogsButRetainsConversionCause() throws Exception {
        String invalidMember = """
                {"persons":[],"members":[{"nusId":"privacy-audit@example.com","name":"Privacy Example",
                "phone":"91118888","email":"privacy-audit@example.com"}],"equipment":[],"loans":[]}
                """;
        String invalidEquipment = invalidMember.replace("\"nusId\":\"privacy-audit@example.com\"",
                "\"nusId\":\"A8400001X\"").replace("\"equipment\":[]", """
                "equipment":[{"uuid":"privacy-audit@example.com","name":"Camera","category":"Testing",
                "condition":"GOOD","notes":""}]
                """.strip());
        for (String invalid : List.of(invalidMember, invalidEquipment)) {
            assertPrivateStartupDiagnostics(invalid, "JsonAddressBookStorage", Level.INFO);
            DataLoadingException failure = assertThrows(DataLoadingException.class,
                    storage(directory.resolve("invalid.json"))::readAddressBook);
            assertInstanceOf(IllegalValueException.class, failure.getCause());
            assertTrue(failure.getCause().getMessage().contains("privacy-audit@example.com"),
                    "The detailed checked cause must retain the invalid identity for recovery");
        }
    }

    /** Verifies startup diagnostics and a real log file without changing the rejected dataset. */
    private void assertPrivateStartupDiagnostics(String invalid, String failedLogger, Level failureLevel)
            throws Exception {
        Path file = directory.resolve("invalid.json");
        Files.writeString(file, invalid);
        byte[] before = Files.readAllBytes(file);
        MainApp app = new MainApp();
        Storage storage = storage(file);
        List<LogRecord> records = new ArrayList<>();
        Handler capture = new Handler() {
            @Override
            public void publish(LogRecord record) {
                records.add(record);
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };
        Logger logger = Logger.getLogger("ab3");
        Path logFile = directory.resolve("startup.log");
        FileHandler fileCapture = new FileHandler(logFile.toString());
        fileCapture.setFormatter(new SimpleFormatter());
        logger.addHandler(capture);
        logger.addHandler(fileCapture);
        try {
            app.initModel(storage, new UserPrefs());
            assertNull(app.model);
            assertNull(app.logic);
            assertNull(app.ui);
            FxTestUtil.runAndWait(() -> assertFailedStartupDialog(app, file));
            app.stop();
        } finally {
            logger.removeHandler(capture);
            logger.removeHandler(fileCapture);
            fileCapture.close();
        }
        assertTrue(records.stream().anyMatch(record -> record.getLoggerName().endsWith("." + failedLogger)
                && record.getLevel().equals(failureLevel)));
        assertTrue(records.stream().anyMatch(record -> record.getLoggerName().endsWith(".MainApp")
                && record.getLevel().equals(Level.SEVERE)));
        SimpleFormatter formatter = new SimpleFormatter();
        String logged = records.stream().map(formatter::format).collect(java.util.stream.Collectors.joining());
        String persisted = Files.readString(logFile);
        for (String sensitive : List.of("Privacy Example", "91118888", "privacy-audit@example.com", "[Source:")) {
            assertFalse(logged.contains(sensitive), "Persistent diagnostics must omit contact data and JSON excerpts");
            assertFalse(persisted.contains(sensitive), "The actual log file must omit contact data and JSON excerpts");
        }
        assertTrue(logged.contains(file.toString()));
        assertTrue(persisted.contains(file.toString()));
        assertArrayEquals(before, Files.readAllBytes(file));
    }

    @Test
    public void initModel_invalidFiles_preservesBytesAndShowsRecovery() throws Exception {
        Path file = directory.resolve("data.json");
        Storage storage = storage(file);
        var data = ViewTestData.model();
        data.addLoan(ViewTestData.loan(ViewTestData.CAMERA, ViewTestData.ALEX, false));
        storage.saveAddressBook(data.getAddressBook());
        String valid = Files.readString(file);
        for (String invalid : List.of("null", "{invalid data}", valid + " {}",
                valid.replace("alex@example.com", "alice@@example.com"))) {
            Files.writeString(file, invalid);
            byte[] before = Files.readAllBytes(file);
            MainApp app = new MainApp();
            app.initModel(storage, new UserPrefs());
            assertNull(app.model);
            assertNull(app.logic);
            assertNull(app.ui);
            FxTestUtil.runAndWait(() -> {
                String message = assertFailedStartupDialog(app, file);
                assertTrue(message.contains("Problem:"));
                assertFalse(message.contains("IllegalValueException:"));
                assertFalse(message.contains("[Source:"));
                if (invalid.contains("alice@@example.com")) {
                    assertTrue(message.contains("members[0]"), message);
                    assertTrue(message.contains(data.getMemberList().getFirst().getNusId().value), message);
                    assertTrue(message.contains("Emails should be"), message);
                }
            });
            app.stop();
            assertArrayEquals(before, Files.readAllBytes(file));
        }
        Files.writeString(file, valid);
        MainApp repaired = new MainApp();
        repaired.initModel(storage, new UserPrefs());
        assertEquals(data.getAddressBook(), repaired.model.getAddressBook());
        assertEquals(data.getLoanList(), repaired.model.getAddressBook().getLoanList());
        FxTestUtil.runAndWait(() -> {
            Stage stage = new Stage();
            try {
                assertDoesNotThrow(() -> repaired.start(stage));
                assertTrue(stage.isShowing());
            } finally {
                stage.hide();
            }
        });
        repaired.stop();
        assertEquals(new UserPrefs(), storage.readUserPrefs().orElseThrow());
    }

    @Test
    public void initModel_missingFile_initializesSampleWithoutCreatingDataFile() {
        Path file = directory.resolve("missing.json");
        MainApp app = new MainApp();
        app.initModel(storage(file), new UserPrefs());
        assertNotNull(app.logic);
        assertNotNull(app.ui);
        assertFalse(app.model.getAddressBook().getMemberList().isEmpty());
        assertTrue(app.model.getAddressBook().getPersonList().isEmpty());
        assertFalse(Files.exists(file));
    }

    @Test
    public void initModel_existingLegacyContacts_preservesThemAfterMutationAndReload() throws Exception {
        Path file = directory.resolve("existing.json");
        Storage storage = storage(file);
        var data = ViewTestData.model();
        data.addPerson(SampleDataUtil.getSamplePersons()[0]);
        var expected = List.copyOf(data.getAddressBook().getPersonList());
        storage.saveAddressBook(data.getAddressBook());
        MainApp app = new MainApp();
        app.initModel(storage, new UserPrefs());
        assertEquals(expected, app.model.getAddressBook().getPersonList());
        app.logic.execute("member add nus/A8400999X name/New Member phone/91112222 email/new@example.com");
        assertEquals(expected, storage.readAddressBook().orElseThrow().getPersonList());
    }

    private Storage storage(Path file) {
        return new StorageManager(new JsonAddressBookStorage(file),
                new JsonUserPrefsStorage(directory.resolve("prefs.json")));
    }

    /** Closes the real modal dialog from its nested event loop and inspects the displayed guidance. */
    private String assertFailedStartupDialog(MainApp app, Path file) {
        Stage primary = new Stage();
        AtomicReference<String> content = new AtomicReference<>();
        AtomicReference<TextArea> details = new AtomicReference<>();
        Platform.runLater(() -> {
            for (Window window : List.copyOf(Window.getWindows())) {
                if (window.getScene().getRoot() instanceof DialogPane pane) {
                    content.set(pane.getContentText());
                    if (pane.getContent() instanceof TextArea area) {
                        details.set(area);
                    }
                    window.hide();
                }
            }
        });
        DataLoadingException failure = assertThrows(DataLoadingException.class, () -> app.start(primary));
        assertNotNull(failure.getCause());
        assertNotNull(content.get());
        assertTrue(content.get().contains(file.toAbsolutePath().toString()));
        assertTrue(content.get().contains("preserved"));
        assertTrue(content.get().contains("back up"));
        assertTrue(content.get().contains("Repair"));
        assertFalse(primary.isShowing());
        assertNotNull(details.get());
        assertEquals(content.get(), details.get().getText());
        assertFalse(details.get().isEditable());
        assertTrue(details.get().isWrapText());
        return content.get();
    }
}
