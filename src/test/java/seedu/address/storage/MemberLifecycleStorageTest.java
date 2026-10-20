package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.MemberEditCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.member.NusId;
import seedu.address.model.person.Name;

/**
 * Exercises deletion metadata and real persistence failures without changing the live dataset.
 */
public class MemberLifecycleStorageTest {
    private static final NusId ID = new NusId("A0123456X");

    @TempDir
    public Path directory;

    @Test
    public void commands_deleteEditRestoreEdit_rejectsDeletedEditsWithoutSaving() throws Exception {
        ModelManager model = fullModel();
        Path file = directory.resolve("edit-lifecycle.json");
        JsonAddressBookStorage disk = new JsonAddressBookStorage(file);
        StorageManager storage = new StorageManager(disk, new JsonUserPrefsStorage(directory.resolve("prefs.json")));
        LogicManager logic = new LogicManager(model, storage);
        String originalName = model.findMember(ID).orElseThrow().getName().fullName;
        logic.execute("member delete nus/" + ID);
        byte[] deletedBytes = Files.readAllBytes(file);
        AddressBook deletedState = new AddressBook(model.getAddressBook());

        for (String name : List.of("Updated Name", originalName)) {
            CommandException error = assertThrows(CommandException.class, () ->
                    logic.execute("member edit nus/" + ID + " name/" + name));
            assertEquals(String.format(MemberEditCommand.MESSAGE_DELETED, ID), error.getMessage());
            assertTrue(model.findMember(ID).orElseThrow().isDeleted());
            assertEquals(deletedState, model.getAddressBook());
            assertArrayEquals(deletedBytes, Files.readAllBytes(file));
        }

        logic.execute("member restore nus/" + ID);
        logic.execute("member edit nus/" + ID + " name/Updated Name");
        assertFalse(model.findMember(ID).orElseThrow().isDeleted());
        assertEquals(Name.forMember("Updated Name"), model.findMember(ID).orElseThrow().getName());
        assertEquals(deletedState.getLoanList(), model.getLoanList());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void read_legacyAndBooleanFlags_defaultsToActiveAndRejectsInvalidExplicitValues() throws Exception {
        Path file = directory.resolve("flags.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        Files.writeString(file, snapshot(""));
        assertFalse(storage.readAddressBook().orElseThrow().getMemberList().getFirst().isDeleted());
        for (String flag : List.of("true", "false")) {
            Files.writeString(file, snapshot(",\"deleted\":" + flag));
            assertEquals(Boolean.parseBoolean(flag),
                    storage.readAddressBook().orElseThrow().getMemberList().getFirst().isDeleted());
        }
        for (String invalid : List.of("null", "\"true\"", "0", "[]", "{}")) {
            String text = snapshot(",\"deleted\":" + invalid);
            Files.writeString(file, text);
            assertThrows(DataLoadingException.class, storage::readAddressBook, invalid);
            assertEquals(text, Files.readString(file));
        }
    }

    @Test
    public void read_deletedMemberWithClosedHistory_roundTripsButOpenLoanIsRejected() throws Exception {
        ModelManager model = fullModel();
        model.deleteMember(model.findMember(ID).orElseThrow());
        Path file = directory.resolve("history.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        storage.saveAddressBook(model.getAddressBook());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        assertEquals(2, storage.readAddressBook().orElseThrow().getLoanList().size());
        String json = Files.readString(file);
        assertTrue(json.contains("\"deleted\" : true"));
        String invalid = json.replaceFirst("\"returnedDate\" : \"2026-10-13\"", "\"returnedDate\" : null");
        Files.writeString(file, invalid);
        assertThrows(DataLoadingException.class, storage::readAddressBook);
        assertEquals(invalid, Files.readString(file));
    }

    @Test
    public void commands_partialWriteAndFailedMove_preservesStateAndAllowsRetry() throws Exception {
        for (boolean initiallyDeleted : List.of(false, true)) {
            for (boolean partialWrite : List.of(false, true)) {
                verifyFailedMutation(initiallyDeleted, partialWrite);
            }
        }
    }

    private void verifyFailedMutation(boolean initiallyDeleted, boolean partialWrite) throws Exception {
        Path run = Files.createDirectory(directory.resolve(initiallyDeleted + "-" + partialWrite));
        ModelManager model = fullModel();
        if (initiallyDeleted) {
            model.deleteMember(model.findMember(ID).orElseThrow());
        }
        FailingWriter writer = new FailingWriter(partialWrite);
        JsonAddressBookStorage disk = new JsonAddressBookStorage(run.resolve("live.json"), writer);
        StorageManager storage = new StorageManager(disk, new JsonUserPrefsStorage(run.resolve("prefs.json")));
        storage.saveAddressBook(model.getAddressBook());
        byte[] saved = Files.readAllBytes(disk.getAddressBookFilePath());
        AddressBook before = new AddressBook(model.getAddressBook());
        LogicManager logic = new LogicManager(model, storage);
        String command = (initiallyDeleted ? "member restore nus/" : "member delete nus/") + ID;
        writer.fail = true;
        CommandException error = assertThrows(CommandException.class, () -> logic.execute(command));
        assertSame(writer.failure, error.getCause());
        assertEquals(before, model.getAddressBook());
        assertArrayEquals(saved, Files.readAllBytes(disk.getAddressBookFilePath()));
        writer.fail = false;
        logic.execute(command);
        ModelManager reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(!initiallyDeleted, reloaded.findMember(ID).orElseThrow().isDeleted());
        assertEquals(before.getLoanList(), reloaded.getLoanList());
        assertEquals(model.getAddressBook(), reloaded.getAddressBook());
        assertThrows(CommandException.class, () -> logic.execute(command));
    }

    private ModelManager fullModel() throws Exception {
        ReadOnlyAddressBook source = new JsonAddressBookStorage(
                Path.of("src/test/data/JsonSerializableAddressBookTest/fullAddressBook.json"))
                .readAddressBook().orElseThrow();
        ModelManager model = new ModelManager(source, new UserPrefs());
        model.closeLoan(source.getEquipmentList().getFirst().getUuid(), LocalDate.of(2026, 10, 13));
        return model;
    }

    private String snapshot(String deletionField) {
        return "{\"persons\":[],\"members\":[{\"nusId\":\"A0123456X\",\"name\":\"Alex Tan\","
                + "\"phone\":\"91234567\",\"email\":\"alex@example.com\"" + deletionField
                + "}],\"equipment\":[],\"loans\":[]}";
    }

    /**
     * Injects a real partial candidate write or a failed atomic installation.
     */
    private static class FailingWriter extends AtomicFileWriter {
        private final boolean partialWrite;
        private final IOException failure = new IOException("Controlled lifecycle save failure");
        private boolean fail;

        FailingWriter(boolean partialWrite) {
            this.partialWrite = partialWrite;
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
