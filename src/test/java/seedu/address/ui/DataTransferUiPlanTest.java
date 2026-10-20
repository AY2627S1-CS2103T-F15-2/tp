package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.logic.LogicManager;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.storage.TransferSnapshotStorage;
import seedu.address.testutil.FxTestUtil;

/** Recorded, fail-fast UI plan against the real shell, command pipeline and disposable JSON files. */
public class DataTransferUiPlanTest {
    private static final Pattern CASE = Pattern.compile(
            "(?s)## (TC\\d+:[^\\n]+).*?### Input\\s+```text\\n(.*?)```.*?### Expected output\\s+```text\\n(.*?)```");

    @TempDir
    public Path directory;

    @Test
    public void recordedPlan_realShell_matchesEveryStepAndPreservesRejectedState() throws Exception {
        assertEquals(25, Runtime.version().feature());
        if (Boolean.getBoolean("laplace.packagedTransfer")) {
            assertTrue(MainWindow.class.getProtectionDomain().getCodeSource().getLocation().getPath()
                    .endsWith("Laplace.jar"));
            assertEquals("jar", DataTransferPanel.class.getResource("/view/DataTransferPanel.fxml").getProtocol());
        }
        List<String> session = new ArrayList<>();
        Matcher cases = casesIn(Files.readString(Path.of("test/ui-test-plan.md")));
        int count = 0;
        try {
            while (cases.find()) {
                String name = cases.group(1);
                String inputs = cases.group(2);
                String expected = cases.group(3);
                Path run = Files.createDirectory(directory.resolve("case" + ++count));
                FxTestUtil.runAndWait(() -> runCase(name, inputs, expected, run, session));
            }
            assertEquals(3, count);
        } finally {
            Files.createDirectories(Path.of("_temp"));
            Files.write(Path.of("_temp/ui-test-session.txt"), session);
        }
    }

    @Test
    public void recordedPlan_windowsAndUnixLineEndings_produceIdenticalCases() throws Exception {
        String plan = Files.readString(Path.of("test/ui-test-plan.md")).replace("\r\n", "\n");
        Matcher unix = casesIn(plan);
        Matcher windows = casesIn(plan.replace("\n", "\r\n"));
        int count = 0;
        while (unix.find()) {
            assertTrue(windows.find());
            assertEquals(unix.group(1), windows.group(1));
            assertEquals(unix.group(2), windows.group(2));
            assertEquals(unix.group(3), windows.group(3));
            count++;
        }
        assertFalse(windows.find());
        assertEquals(3, count);
    }

    private Matcher casesIn(String plan) {
        return CASE.matcher(plan.replace("\r\n", "\n").replace('\r', '\n'));
    }

    private void runCase(String name, String inputs, String expected, Path run, List<String> session) {
        MainWindow window = null;
        try {
            TransferSnapshotStorage snapshots = new TransferSnapshotStorage();
            ReadOnlyAddressBook original = snapshots.read(
                    Path.of("src/test/data/JsonSerializableAddressBookTest/fullAddressBook.json"));
            ModelManager model = new ModelManager(original, new UserPrefs());
            RecordingStorage storage = new RecordingStorage(run);
            storage.saveAddressBook(original);
            snapshots.writeNew(run.resolve("empty.json"), new AddressBook());
            Files.writeString(run.resolve("unsupported.json"), "{\"schemaVersion\":2,\"persons\":[]}");
            window = new MainWindow(new Stage(), new LogicManager(model, storage), run.resolve("live.json"));
            window.show();
            window.fillInnerParts();
            session.add(name);
            StringBuilder actual = new StringBuilder();
            for (String input : inputs.strip().split("\\n")) {
                AddressBook before = new AddressBook(model.getAddressBook());
                int saves = storage.saves;
                byte[] bytes = Files.readAllBytes(run.resolve("live.json"));
                TextField field = (TextField) node(window, "commandTextField");
                field.setText(input.replace("{{DIR}}", run.toString()));
                field.fireEvent(new ActionEvent());
                String status = ((Label) node(window, "feedbackStatus")).getText();
                String feedback = ((TextArea) node(window, "resultDisplay")).getText();
                String normalized = normalize(status + ": " + feedback, run);
                session.add("INPUT: " + input);
                session.add("OUTPUT: " + normalized);
                actual.append(normalized).append('\n');
                // Fail at this step, before running any subsequent command or case.
                assertTrue(expected.startsWith(actual.toString()), "Expected:\n" + expected + "Actual:\n" + actual);
                if (input.startsWith("data ")) {
                    int expectedSaves = input.equals("data confirm") && status.equals("Success") ? saves + 1 : saves;
                    assertEquals(expectedSaves, storage.saves);
                }
                if (status.equals("Error")) {
                    assertEquals(before, model.getAddressBook());
                    assertArrayEquals(bytes, Files.readAllBytes(run.resolve("live.json")));
                    assertEquals(input.replace("{{DIR}}", run.toString()), field.getText());
                    if (input.contains("unsupported.json")) {
                        assertNull(node(window, "confirmTransfer"), "Invalidated preview must remove Confirm");
                        assertFalse(node(window, "singlePanelPlaceholder").isVisible());
                    }
                }
                if (input.startsWith("data import") && status.equals("Success")) {
                    assertEquals(before, model.getAddressBook());
                    assertArrayEquals(bytes, Files.readAllBytes(run.resolve("live.json")));
                    render(window, "preview-normal", 1100, 700);
                    render(window, "preview-small", 760, 500);
                }
            }
            assertEquals(expected, actual.toString());
            assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
            if (name.startsWith("TC2")) {
                assertEquals(new AddressBook(), model.getAddressBook());
                assertTrue(((ListView<?>) node(window, "rows")).getSelectionModel().getSelectedItems().isEmpty());
                try (var backups = Files.list(run.resolve("backups"))) {
                    List<Path> paths = backups.toList();
                    assertEquals(2, paths.size());
                    for (Path backup : paths) {
                        assertEquals(original, snapshots.read(backup));
                    }
                }
            }
            window.dispose();
            window.getPrimaryStage().hide();
            ModelManager reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
            window = new MainWindow(new Stage(), new LogicManager(reloaded, storage), run.resolve("live.json"));
            window.show();
            window.fillInnerParts();
            assertEquals(model.getAddressBook(), reloaded.getAddressBook());
            ListView<?> reloadedRows = (ListView<?>) node(window, "rows");
            assertEquals(reloaded.getAddressBook().getMemberList().size(),
                    reloadedRows.getItems().size());
            session.add("PASS: rejected state preserved; fresh model/logic/window reload and selection verified.");
        } catch (IOException | IllegalValueException | DataLoadingException exception) {
            throw new AssertionError(exception);
        } finally {
            if (window != null) {
                window.dispose();
                window.getPrimaryStage().hide();
            }
        }
    }

    @Test
    public void failedReplacementPreview_clearsOldControlsWithoutChangingData() throws Exception {
        Path source = directory.resolve("incoming.json");
        TransferSnapshotStorage snapshots = new TransferSnapshotStorage();
        snapshots.writeNew(source, new AddressBook());
        Path invalid = directory.resolve("invalid.json");
        Files.writeString(invalid, "{");
        FxTestUtil.runAndWait(() -> {
            ModelManager model = new ModelManager();
            RecordingStorage storage = new RecordingStorage(directory);
            MainWindow window = new MainWindow(new Stage(), new LogicManager(model, storage),
                    directory.resolve("live.json"));
            try {
                storage.saveAddressBook(model.getAddressBook());
                String saved = Files.readString(storage.getAddressBookFilePath());
                window.show();
                window.fillInnerParts();
                for (String verb : List.of("import", "restore")) {
                    for (Path rejected : List.of(directory.resolve("missing.json"), invalid)) {
                        submit(window, "data import file/" + source);
                        assertTrue(node(window, "confirmTransfer").isVisible());
                        String command = "data " + verb + " file/" + rejected;
                        submit(window, command);
                        assertEquals("Error", ((Label) node(window, "feedbackStatus")).getText());
                        assertEquals(command, ((TextField) node(window, "commandTextField")).getText());
                        assertEquals(new AddressBook(), model.getAddressBook());
                        assertEquals(saved, Files.readString(storage.getAddressBookFilePath()));
                        assertEquals(1, storage.saves);
                        assertNull(node(window, "confirmTransfer"), "Expected no obsolete Confirm control");
                        assertFalse(node(window, "singlePanelPlaceholder").isVisible());
                        submit(window, "data confirm");
                        assertEquals("Error", ((Label) node(window, "feedbackStatus")).getText());
                    }
                }
            } catch (IOException exception) {
                throw new AssertionError(exception);
            } finally {
                window.dispose();
                window.getPrimaryStage().hide();
            }
        });
    }

    @Test
    public void failedConfirmation_keepsExactPreviewRetryableForBackupAndSaveFailures() throws Exception {
        Path source = directory.resolve("incoming.json");
        TransferSnapshotStorage snapshots = new TransferSnapshotStorage();
        snapshots.writeNew(source, new AddressBook());
        ReadOnlyAddressBook original = snapshots.read(
                Path.of("src/test/data/JsonSerializableAddressBookTest/fullAddressBook.json"));
        FxTestUtil.runAndWait(() -> {
            for (boolean blockBackup : List.of(true, false)) {
                Path run = directory.resolve(Boolean.toString(blockBackup));
                ModelManager model = new ModelManager(original, new UserPrefs());
                RecordingStorage storage = new RecordingStorage(run);
                MainWindow window = new MainWindow(new Stage(), new LogicManager(model, storage),
                        run.resolve("live.json"));
                try {
                    storage.saveAddressBook(original);
                    String saved = Files.readString(storage.getAddressBookFilePath());
                    window.show();
                    window.fillInnerParts();
                    submit(window, "data import file/" + source);
                    Button confirm = (Button) node(window, "confirmTransfer");
                    // An unrelated export failure must also leave the pending replacement usable.
                    submit(window, "data export file/" + storage.getAddressBookFilePath());
                    assertSame(confirm, node(window, "confirmTransfer"));
                    if (blockBackup) {
                        Files.writeString(run.resolve("backups"), "blocked backup directory");
                    } else {
                        storage.failure = new IOException("controlled save failure");
                    }
                    confirm.fire();
                    assertEquals("Error", ((Label) node(window, "feedbackStatus")).getText());
                    assertSame(confirm, node(window, "confirmTransfer"));
                    assertTrue(node(window, "singlePanelPlaceholder").isVisible());
                    assertEquals(original, model.getAddressBook());
                    assertEquals(saved, Files.readString(storage.getAddressBookFilePath()));
                    assertEquals(1, storage.saves);
                    if (blockBackup) {
                        Files.delete(run.resolve("backups"));
                    }
                    storage.failure = null;
                    confirm.fire();
                    assertEquals("Success", ((Label) node(window, "feedbackStatus")).getText());
                    assertEquals(new AddressBook(), model.getAddressBook());
                    assertNull(node(window, "confirmTransfer"));
                    assertEquals(2, storage.saves);
                } catch (IOException exception) {
                    throw new AssertionError(exception);
                } finally {
                    window.dispose();
                    window.getPrimaryStage().hide();
                }
            }
        });
    }

    @Test
    public void previewButtons_dispatchCancelAndConfirmAndRejectStaleConfirmation() throws Exception {
        Path source = directory.resolve("incoming.json");
        new TransferSnapshotStorage().writeNew(source, new AddressBook());
        FxTestUtil.runAndWait(() -> {
            ModelManager model = new ModelManager();
            StorageManager storage = new StorageManager(new JsonAddressBookStorage(directory.resolve("live.json")),
                    new JsonUserPrefsStorage(directory.resolve("prefs.json")));
            MainWindow window = new MainWindow(new Stage(), new LogicManager(model, storage),
                    directory.resolve("live.json"));
            try {
                window.show();
                window.fillInnerParts();
                submit(window, "data import file/" + source);
                Button cancel = (Button) node(window, "cancelTransfer");
                cancel.fire();
                assertTrue(((TextArea) node(window, "resultDisplay")).getText().contains("cancelled"));
                submit(window, "data restore file/" + source);
                Button confirm = (Button) node(window, "confirmTransfer");
                confirm.fire();
                assertTrue(((TextArea) node(window, "resultDisplay")).getText().contains("Snapshot applied"));
                submit(window, "data import file/" + source);
                Button stale = (Button) node(window, "confirmTransfer");
                submit(window, "member add nus/A0123456X name/Alex phone/91234567 email/alex@example.com");
                stale.fire();
                assertEquals("Error", ((Label) node(window, "feedbackStatus")).getText());
                assertEquals(1, model.getAddressBook().getMemberList().size());
                assertFalse(node(window, "singlePanelPlaceholder").isVisible());
            } finally {
                window.dispose();
                window.getPrimaryStage().hide();
            }
        });
    }

    private void submit(MainWindow window, String command) {
        TextField field = (TextField) node(window, "commandTextField");
        field.setText(command);
        field.fireEvent(new ActionEvent());
    }

    private Node node(MainWindow window, String id) {
        Region root = (Region) window.getPrimaryStage().getScene().getRoot();
        root.applyCss();
        root.layout();
        return root.lookup("#" + id);
    }

    private String normalize(String feedback, Path run) {
        return feedback.replace('\\', '/').replace(run.toString().replace('\\', '/'), "{{DIR}}")
                .replaceAll("\\{\\{DIR}}/backups/laplace-[0-9a-f-]+\\.json", "{{BACKUP}}");
    }

    private void render(MainWindow window, String name, int width, int height) throws IOException {
        Region root = (Region) window.getPrimaryStage().getScene().getRoot();
        root.resize(width, height);
        root.applyCss();
        root.layout();
        assertTrue(node(window, "previewSummary").getBoundsInParent().getWidth() <= width);
        WritableImage image = root.snapshot(null, new WritableImage(width, height));
        int[] pixels = new int[width * height];
        image.getPixelReader().getPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), pixels, 0, width);
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        output.setRGB(0, 0, width, height, pixels, 0, width);
        Path destination = Path.of("build/reports/laplace/transfer-" + name + ".png");
        Files.createDirectories(destination.getParent());
        assertTrue(ImageIO.write(output, "png", destination.toFile()));
    }

    /** Counts only completed writes through the real JSON storage implementation. */
    private static class RecordingStorage extends StorageManager {
        private int saves;
        private IOException failure;

        RecordingStorage(Path directory) {
            super(new JsonAddressBookStorage(directory.resolve("live.json")),
                    new JsonUserPrefsStorage(directory.resolve("prefs.json")));
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook data) throws IOException {
            if (failure != null) {
                throw failure;
            }
            super.saveAddressBook(data);
            saves++;
        }
    }
}
