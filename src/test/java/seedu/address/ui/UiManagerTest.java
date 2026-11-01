package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.FxTestUtil;

/** Verifies recovery diagnostics and the shared alert's owner and content. */
class UiManagerTest {
    @TempDir
    public Path directory;

    @Test
    void showDataLoadingError_ioCause_showsReadableReasonAndRecoveryGuidance() {
        FxTestUtil.runAndWait(() -> {
            Path file = directory.resolve("data.json");
            ShownDialog dialog = observeDialog(() -> UiManager.showDataLoadingError(file,
                    new DataLoadingException(new IOException("Cannot read\n\tdata file"))));
            String text = dialog.pane().getContentText();
            assertTrue(text.contains("Problem: Cannot read data file"));
            assertFalse(text.contains("java.io.IOException"));
            assertTrue(text.contains(file.toAbsolutePath().toString()));
            assertTrue(text.contains("back up"));
            TextArea details = (TextArea) dialog.pane().getContent();
            assertEquals(text, details.getText());
            assertFalse(details.isEditable());
            assertTrue(details.isWrapText());
        });
    }

    @Test
    void showDataLoadingError_missingCauseOrMessage_showsFallbackReason() {
        FxTestUtil.runAndWait(() -> {
            for (DataLoadingException failure : List.of(new DataLoadingException(null),
                    new DataLoadingException(new IOException()))) {
                ShownDialog dialog = observeDialog(() -> UiManager.showDataLoadingError(
                        directory.resolve("data.json"), failure));
                assertTrue(dialog.pane().getContentText().contains("Problem: The saved data is invalid."));
            }
        });
    }

    @Test
    void showDataLoadingError_longDiagnostic_retainsFullSelectableText() {
        FxTestUtil.runAndWait(() -> {
            String diagnostic = "members[987]: " + "Invalid field details. ".repeat(250) + "Check the email.";
            ShownDialog dialog = observeDialog(() -> UiManager.showDataLoadingError(
                    directory.resolve("data.json"), new DataLoadingException(new IOException(diagnostic))));
            TextArea details = (TextArea) dialog.pane().getContent();
            String text = details.getText();
            assertTrue(text.contains("Problem: " + diagnostic));
            assertTrue(text.contains("The original data file has been preserved."));
            assertFalse(details.isEditable());
            assertTrue(details.isWrapText());
            assertTrue(dialog.resizable());
            details.selectAll();
            assertEquals(text, details.getSelectedText());
        });
    }

    @Test
    void showAlertDialogAndWait_preservesOwnerAndContent() {
        FxTestUtil.runAndWait(() -> {
            Path dataFile = directory.resolve("data.json");
            StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataFile),
                    new JsonUserPrefsStorage(directory.resolve("prefs.json")));
            UiManager manager = new UiManager(new LogicManager(new ModelManager(), storage), dataFile);
            Stage owner = new Stage();
            try {
                manager.start(owner);
                ShownDialog dialog = observeDialog(() -> manager.showAlertDialogAndWait(
                        AlertType.ERROR, "Save error", "Could not save", "The data file is read-only."));
                assertSame(owner, dialog.owner());
                assertEquals("Could not save", dialog.pane().getHeaderText());
                assertEquals("The data file is read-only.", dialog.pane().getContentText());
                assertEquals(UiManager.ALERT_DIALOG_PANE_FIELD_ID, dialog.pane().getId());
                assertTrue(dialog.pane().getStylesheets().contains("view/DarkTheme.css"));
            } finally {
                owner.hide();
            }
        });
    }

    /** Captures and closes a real modal alert from its nested event loop on the FX thread. */
    private ShownDialog observeDialog(Runnable showDialog) {
        AtomicReference<ShownDialog> shown = new AtomicReference<>();
        Platform.runLater(() -> {
            for (Window window : List.copyOf(Window.getWindows())) {
                if (window.getScene().getRoot() instanceof DialogPane pane) {
                    Stage stage = (Stage) window;
                    shown.set(new ShownDialog(pane, stage.getOwner(), stage.isResizable()));
                    window.hide();
                }
            }
        });
        showDialog.run();
        assertNotNull(shown.get());
        return shown.get();
    }

    private record ShownDialog(DialogPane pane, Window owner, boolean resizable) {}
}
