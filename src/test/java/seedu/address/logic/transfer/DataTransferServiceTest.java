package seedu.address.logic.transfer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DataCommand;
import seedu.address.logic.commands.DataCommand.Action;
import seedu.address.logic.commands.DisplayRequest;
import seedu.address.logic.commands.TransferPreviewResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.storage.TransferSnapshotStorage;

/** Exercises session state and persistence ordering with real snapshots and controlled I/O failures. */
public class DataTransferServiceTest {
    @TempDir
    public Path directory;

    private ModelManager model;
    private RecordingStorage storage;
    private TransferSnapshotStorage snapshots;
    private DataTransferService transfers;
    private ReadOnlyAddressBook original;

    @BeforeEach
    public void setUp() throws Exception {
        snapshots = new TransferSnapshotStorage();
        original = snapshots.read(Path.of("src/test/data/JsonSerializableAddressBookTest/fullAddressBook.json"));
        model = new ModelManager(original, new UserPrefs());
        storage = new RecordingStorage(directory);
        storage.saveAddressBook(original);
        transfers = new DataTransferService(storage, snapshots, directory);
        snapshots.writeNew(directory.resolve("incoming.json"), new AddressBook());
    }

    @Test
    public void previewConfirm_capturesExactSnapshotBacksUpThenPersistsAndPublishes() throws Exception {
        ReadOnlyAddressBook observableRoot = model.getAddressBook();
        byte[] beforeBytes = Files.readAllBytes(storage.getAddressBookFilePath());
        TransferPreviewResult result = assertInstanceOf(TransferPreviewResult.class,
                execute(Action.IMPORT, "incoming.json"));
        DataTransferPreview preview = result.getPreview();
        assertEquals(directory.resolve("incoming.json"), preview.source());
        assertEquals(0, preview.persons());
        assertEquals(0, preview.members());
        assertEquals(0, preview.equipment());
        assertEquals(0, preview.loans());
        assertEquals(preview.summary(), result.getFeedbackToUser());
        assertEquals(original, model.getAddressBook());
        assertTrue(java.util.Arrays.equals(beforeBytes, Files.readAllBytes(storage.getAddressBookFilePath())));
        // Confirmation must not reread an edited or removed source file.
        Files.delete(directory.resolve("incoming.json"));
        CommandResult confirmed = execute(Action.CONFIRM, null);
        assertSame(observableRoot, model.getAddressBook());
        assertEquals(new AddressBook(), model.getAddressBook());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        assertEquals(DisplayRequest.members(), confirmed.getDisplayRequest());
        assertEquals(2, storage.saves);
        Path backup = backups().getFirst();
        assertEquals(original, snapshots.read(backup));
        assertTrue(confirmed.getFeedbackToUser().contains(backup.toString()));
        assertThrows(CommandException.class, () -> execute(Action.CONFIRM, null));
    }

    @Test
    public void exportAndBackup_doNotSaveMutateOrInvalidatePreviewAndNeverOverwrite() throws Exception {
        execute(Action.RESTORE, "incoming.json");
        CommandResult exported = execute(Action.EXPORT, "a folder/export.json");
        assertTrue(exported.getFeedbackToUser().contains("a folder"));
        assertEquals(original, snapshots.read(directory.resolve("a folder/export.json")));
        assertThrows(CommandException.class, () -> execute(Action.EXPORT, "a folder/export.json"));
        assertThrows(CommandException.class, () -> execute(Action.EXPORT, "live.json"));
        execute(Action.BACKUP, null);
        execute(Action.BACKUP, null);
        assertEquals(2, backups().size());
        for (Path path : backups()) {
            assertEquals(original, snapshots.read(path));
        }
        assertEquals(1, storage.saves);
        assertEquals(original, model.getAddressBook());
        execute(Action.CONFIRM, null);
        assertEquals(new AddressBook(), model.getAddressBook());
    }

    @Test
    public void previewReplacementCancellationAndInvalidation_requireFreshConfirmation() throws Exception {
        execute(Action.IMPORT, "incoming.json");
        snapshots.writeNew(directory.resolve("original.json"), original);
        execute(Action.RESTORE, "original.json");
        execute(Action.CONFIRM, null);
        assertEquals(original, model.getAddressBook());
        execute(Action.IMPORT, "incoming.json");
        execute(Action.CANCEL, null);
        assertThrows(CommandException.class, () -> execute(Action.CONFIRM, null));
        execute(Action.CANCEL, null);
        execute(Action.IMPORT, "incoming.json");
        transfers.invalidate();
        assertThrows(CommandException.class, () -> execute(Action.CONFIRM, null));
        execute(Action.IMPORT, "incoming.json");
        model.setAddressBook(new AddressBook());
        assertTrue(assertThrows(CommandException.class, () -> execute(Action.CONFIRM, null))
                .getMessage().contains("Data changed"));
        assertThrows(CommandException.class, () -> execute(Action.CONFIRM, null));
        assertEquals(1, backups().size());
    }

    @Test
    public void failedPreview_discardsOlderRequestAndPreservesLiveData() throws Exception {
        execute(Action.IMPORT, "incoming.json");
        CommandException failure = assertThrows(CommandException.class, () -> execute(Action.IMPORT, "missing.json"));
        assertInstanceOf(IOException.class, failure.getCause());
        assertThrows(CommandException.class, () -> execute(Action.CONFIRM, null));
        Files.writeString(directory.resolve("invalid.json"), "{}");
        assertThrows(CommandException.class, () -> execute(Action.RESTORE, "invalid.json"));
        assertEquals(original, model.getAddressBook());
        assertEquals(original, storage.readAddressBook().orElseThrow());
        assertEquals(1, storage.saves);
    }

    @Test
    public void backupFailure_abortsBeforeSaveAndLeavesPreviewRetryable() throws Exception {
        execute(Action.IMPORT, "incoming.json");
        Path blocked = directory.resolve("backups");
        Files.writeString(blocked, "a file prevents the backup directory");
        assertThrows(CommandException.class, () -> execute(Action.CONFIRM, null));
        assertEquals(original, model.getAddressBook());
        assertEquals(original, storage.readAddressBook().orElseThrow());
        assertEquals(1, storage.saves);
        Files.delete(blocked);
        execute(Action.CONFIRM, null);
        assertEquals(new AddressBook(), model.getAddressBook());
    }

    @Test
    public void saveFailure_preservesOriginalCauseLiveStateAndBytesThenAllowsRetry() throws Exception {
        execute(Action.IMPORT, "incoming.json");
        storage.failure = new IOException("controlled replacement failure");
        String saved = Files.readString(storage.getAddressBookFilePath());
        CommandException failure = assertThrows(CommandException.class, () -> execute(Action.CONFIRM, null));
        assertSame(storage.failure, failure.getCause());
        assertEquals(original, model.getAddressBook());
        assertEquals(saved, Files.readString(storage.getAddressBookFilePath()));
        assertEquals(original, snapshots.read(backups().getFirst()));
        storage.failure = null;
        execute(Action.CONFIRM, null);
        assertEquals(new AddressBook(), model.getAddressBook());
        assertFalse(backups().isEmpty());
    }

    private CommandResult execute(Action action, String path) throws CommandException {
        return new DataCommand(action, path == null ? null : Path.of(path)).execute(model, transfers);
    }

    private List<Path> backups() throws IOException {
        try (var paths = Files.list(directory.resolve("backups"))) {
            return paths.sorted().toList();
        }
    }

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
