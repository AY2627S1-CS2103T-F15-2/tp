package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.FxTestUtil;
import seedu.address.testutil.ViewTestData;

/**
 * Checks candidate freshness across real transactions while queries and rejected commands preserve cycles.
 */
class CompletionRefreshIntegrationTest {
    @TempDir
    public Path directory;

    @Test
    void recordCandidates_commitUndoRedoLifecycleAndReload_remainFreshWithoutTabSaves() {
        ModelManager model = ViewTestData.model();
        JsonAddressBookStorage disk = new JsonAddressBookStorage(directory.resolve("records.json"));
        LogicManager logic = new LogicManager(model, new StorageManager(disk,
                new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
        FxTestUtil.runAndWait(() -> {
            TextField field = new TextField();
            new CompletionController(field, model::getAddressBook);
            new Scene(new VBox(field));
            assertNames(field, List.of("Camera", "Speaker"));
            execute(logic, "equipment add name/Tripod category/Photo condition/GOOD");
            assertNames(field, List.of("Camera", "Speaker", "Tripod"));
            execute(logic, "undo");
            assertNames(field, List.of("Camera", "Speaker"));
            execute(logic, "redo");
            assertNames(field, List.of("Camera", "Speaker", "Tripod"));
            String tripodUuid = model.getEquipmentList().stream().filter(unit -> unit.getName().equals("Tripod"))
                    .findFirst().orElseThrow().getUuid().toString();
            execute(logic, "equipment delete uuid/" + tripodUuid);
            assertNames(field, List.of("Camera", "Speaker", "Tripod"));
            execute(logic, "equipment restore uuid/" + tripodUuid);
            execute(logic, "loans add uuid/" + ViewTestData.CAMERA.getUuid() + " nus/" + ViewTestData.ALEX.getNusId()
                    + " assigned/2026-01-01 expected/2026-01-02");
            execute(logic, "loans return uuid/" + ViewTestData.CAMERA.getUuid() + " returned/2026-01-02");
            model.setAddressBook(assertDoesNotThrow(() -> disk.readAddressBook().orElseThrow()));
            assertNames(field, List.of("Camera", "Speaker", "Tripod"));
            String saved = assertDoesNotThrow(() -> Files.readString(disk.getAddressBookFilePath()));
            field.setText("equipment add name/");
            field.positionCaret(field.getLength());
            tab(field);
            assertThrows(CommandException.class, () -> logic.execute("equipment delete"
                    + " uuid/ffffffff-ffff-ffff-ffff-ffffffffffff"));
            tab(field);
            assertEquals("equipment add name/Speaker", field.getText());
            execute(logic, "equipment search q/camera");
            tab(field);
            assertEquals("equipment add name/Tripod", field.getText());
            assertEquals(saved, assertDoesNotThrow(() -> Files.readString(disk.getAddressBookFilePath())));
            assertEquals(model.getAddressBook(), assertDoesNotThrow(() -> disk.readAddressBook().orElseThrow()));
        });
    }

    private static void execute(LogicManager logic, String command) {
        assertDoesNotThrow(() -> logic.execute(command), command);
    }

    private static void assertNames(TextField field, List<String> expected) {
        field.setText("equipment add name/");
        field.positionCaret(field.getLength());
        for (String name : expected) {
            tab(field);
            assertEquals("equipment add name/" + name, field.getText());
        }
        tab(field);
        assertEquals("equipment add name/" + expected.getFirst(), field.getText());
    }

    private static void tab(TextField field) {
        field.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.TAB, false, false, false, false));
    }
}
