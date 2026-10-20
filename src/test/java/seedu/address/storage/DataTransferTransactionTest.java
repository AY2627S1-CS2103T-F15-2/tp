package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.CommittedReplacementResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;

/** Verifies real routed transfers against the atomic writer, including partial writes and failed moves. */
public class DataTransferTransactionTest {
    @TempDir
    public Path directory;

    @Test
    public void confirm_partialWriteAndReplacementFailure_preservesAllDataAndBytesThenRecovers() throws Exception {
        for (boolean partialWrite : List.of(true, false)) {
            Path run = Files.createDirectory(directory.resolve(Boolean.toString(partialWrite)));
            ReadOnlyAddressBook original = new TransferSnapshotStorage().read(
                    Path.of("src/test/data/JsonSerializableAddressBookTest/fullAddressBook.json"));
            ModelManager model = new ModelManager(original, new UserPrefs());
            FailingWriter writer = new FailingWriter(partialWrite);
            JsonAddressBookStorage json = new JsonAddressBookStorage(run.resolve("live.json"), writer);
            StorageManager storage = new StorageManager(json, new JsonUserPrefsStorage(run.resolve("prefs.json")));
            storage.saveAddressBook(original);
            byte[] before = Files.readAllBytes(json.getAddressBookFilePath());
            Path incoming = run.resolve("empty.json");
            new TransferSnapshotStorage().writeNew(incoming, new AddressBook());
            LogicManager logic = new LogicManager(model, storage);
            assertFalse(logic.execute("data import file/" + incoming) instanceof CommittedReplacementResult);
            writer.fail = true;
            CommandException failure = assertThrows(CommandException.class, () -> logic.execute("data confirm"));
            assertSame(writer.failure, failure.getCause());
            assertEquals(original, model.getAddressBook());
            assertTrue(java.util.Arrays.equals(before, Files.readAllBytes(json.getAddressBookFilePath())));
            try (var backups = Files.list(run.resolve("backups"))) {
                assertEquals(original, new TransferSnapshotStorage().read(backups.findFirst().orElseThrow()));
            }
            try (var children = Files.list(run)) {
                assertTrue(children.noneMatch(path -> path.getFileName().toString().startsWith(".laplace-")));
            }
            writer.fail = false;
            CommittedReplacementResult result = assertInstanceOf(CommittedReplacementResult.class,
                    logic.execute("data confirm"));
            assertEquals(original, result.getBefore());
            assertEquals(new AddressBook(), result.getAfter());
            assertEquals(new AddressBook(), model.getAddressBook());
            assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
            assertThrows(CommandException.class, () -> logic.execute("data confirm"));
        }
    }

    @Test
    public void execute_onlySuccessfulConfirmationReturnsOnePersistedHistoryHandoff() throws Exception {
        TransferSnapshotStorage snapshots = new TransferSnapshotStorage();
        ReadOnlyAddressBook original = snapshots.read(
                Path.of("src/test/data/JsonSerializableAddressBookTest/fullAddressBook.json"));
        ModelManager model = new ModelManager(original, new UserPrefs());
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(directory.resolve("live.json")),
                new JsonUserPrefsStorage(directory.resolve("prefs.json")));
        storage.saveAddressBook(original);
        Path incoming = directory.resolve("incoming.json");
        snapshots.writeNew(incoming, new AddressBook());
        LogicManager logic = new LogicManager(model, storage);
        for (String input : List.of("data import file/" + incoming, "data backup",
                "data export file/" + directory.resolve("export.json"), "data cancel",
                "data restore file/" + incoming, "member list")) {
            assertFalse(logic.execute(input) instanceof CommittedReplacementResult, input);
            assertEquals(original, model.getAddressBook());
            assertEquals(original, storage.readAddressBook().orElseThrow());
        }
        assertThrows(CommandException.class, () -> logic.execute("data export file/" + incoming));
        assertThrows(ParseException.class, () -> logic.execute("data confirm file/extra.json"));
        CommittedReplacementResult committed = assertInstanceOf(CommittedReplacementResult.class,
                logic.execute("data confirm"));
        assertEquals(original, committed.getBefore());
        assertEquals(new AddressBook(), committed.getAfter());
        assertEquals(committed.getAfter(), model.getAddressBook());
        assertEquals(committed.getAfter(), storage.readAddressBook().orElseThrow());
        assertThrows(CommandException.class, () -> logic.execute("data confirm"));
    }

    @Test
    public void execute_queriesAndRejectionsPreservePreview_successfulMutationsInvalidateIt() throws Exception {
        Path live = directory.resolve("live.json");
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(live),
                new JsonUserPrefsStorage(directory.resolve("prefs.json")));
        ModelManager model = new ModelManager();
        LogicManager logic = new LogicManager(model, storage);
        Path incoming = directory.resolve("incoming.json");
        new TransferSnapshotStorage().writeNew(incoming, new AddressBook());
        logic.execute("data import file/" + incoming);
        assertThrows(ParseException.class, () -> logic.execute("data export wrong/value"));
        assertThrows(CommandException.class, () -> logic.execute("member delete nus/A0123456X"));
        logic.execute("member list");
        logic.execute("data confirm");
        logic.execute("data restore file/" + incoming);
        logic.execute("member add nus/A0123456X name/Alex phone/91234567 email/alex@example.com");
        assertThrows(CommandException.class, () -> logic.execute("data confirm"));
        assertEquals(1, model.getAddressBook().getMemberList().size());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    private static class FailingWriter extends AtomicFileWriter {
        private final boolean partialWrite;
        private final IOException failure;
        private boolean fail;

        FailingWriter(boolean partialWrite) {
            this.partialWrite = partialWrite;
            failure = partialWrite ? new IOException("Partial write") : new AccessDeniedException("Atomic move");
        }

        @Override
        void writeTemporaryFile(Path temporary, byte[] bytes) throws IOException {
            if (fail && partialWrite) {
                Files.write(temporary, new byte[] {bytes[0]});
                throw failure;
            }
            super.writeTemporaryFile(temporary, bytes);
        }

        @Override
        void replaceFile(Path temporary, Path target) throws IOException {
            if (fail) {
                throw failure;
            }
            super.replaceFile(temporary, target);
        }
    }
}
