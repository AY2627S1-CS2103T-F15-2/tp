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
import seedu.address.testutil.ViewTestData;

/**
 * Exercises deletion metadata and real persistence failures without changing the live dataset.
 */
public class MemberLifecycleStorageTest {
    private static final NusId ID = new NusId("A0123456X");

    @TempDir
    public Path directory;

    @Test
    public void commands_unicodeMemberAddAndEdit_roundTripsWithStableLoanReferences() throws Exception {
        ModelManager model = ViewTestData.model();
        model.addLoan(ViewTestData.loan(ViewTestData.CAMERA, ViewTestData.ALEX, true));
        model.addLoan(ViewTestData.loan(ViewTestData.OTHER_CAMERA, ViewTestData.ALEX, false));
        var loans = List.copyOf(model.getLoanList());
        Path file = directory.resolve("unicode-members.json");
        JsonAddressBookStorage disk = new JsonAddressBookStorage(file);
        StorageManager storage = new StorageManager(disk, new JsonUserPrefsStorage(directory.resolve("prefs.json")));
        LogicManager logic = new LogicManager(model, storage);
        String[] names = {"அருண்", "Anne O\u2019Connor", "Jose\u0301 Tan"};
        for (int index = 0; index < names.length; index++) {
            String name = names[index];
            NusId addedId = new NusId("A800000" + index + "X");
            logic.execute("member add nus/" + addedId + " name/" + name
                    + " phone/91234567 email/member@example.com");
            assertEquals(Name.forMember(name), model.findMember(addedId).orElseThrow().getName());
            logic.execute("member edit nus/" + ID + " name/" + name);
            var edited = model.findMember(ID).orElseThrow();
            assertEquals(ID, edited.getNusId());
            assertEquals(Name.forMember(name), edited.getName());
            assertEquals(ViewTestData.ALEX.getPhone(), edited.getPhone());
            assertEquals(ViewTestData.ALEX.getEmail(), edited.getEmail());
            assertEquals(loans, model.getLoanList());
            assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        }
        byte[] saved = Files.readAllBytes(file);
        CommandException unchanged = assertThrows(CommandException.class, () ->
                logic.execute("member edit nus/" + ID + " name/Jos\u00e9 Tan"));
        assertEquals(MemberEditCommand.MESSAGE_NO_CHANGE, unchanged.getMessage());
        assertArrayEquals(saved, Files.readAllBytes(file));
    }

    @Test
    public void read_decomposedStoredMemberName_preservesExactSpellingAndLoanReferences() throws Exception {
        ModelManager model = fullModel();
        Path file = directory.resolve("decomposed-name.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        storage.saveAddressBook(model.getAddressBook());
        String originalName = model.findMember(ID).orElseThrow().getName().fullName;
        String storedName = "Jose\u0301  Tan";
        Files.writeString(file, Files.readString(file).replace(originalName, storedName));
        var reloaded = storage.readAddressBook().orElseThrow();
        assertEquals(storedName, reloaded.getMemberList().stream()
                .filter(member -> member.getNusId().equals(ID)).findFirst().orElseThrow().getName().fullName);
        assertEquals(model.getLoanList(), reloaded.getLoanList());
        storage.saveAddressBook(reloaded);
        assertEquals(reloaded, storage.readAddressBook().orElseThrow());
        ModelManager restored = new ModelManager(reloaded, new UserPrefs());
        LogicManager logic = new LogicManager(restored,
                new StorageManager(storage, new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
        logic.execute("member edit nus/" + ID + " phone/+6591234567");
        assertEquals(storedName, restored.findMember(ID).orElseThrow().getName().fullName);
        assertEquals(reloaded.getLoanList(), restored.getLoanList());
        assertEquals(restored.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

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
        for (boolean isInitiallyDeleted : List.of(false, true)) {
            for (boolean shouldFailDuringWrite : List.of(false, true)) {
                verifyFailedMutation(isInitiallyDeleted, shouldFailDuringWrite);
            }
        }
    }

    private void verifyFailedMutation(boolean isInitiallyDeleted, boolean shouldFailDuringWrite) throws Exception {
        Path run = Files.createDirectory(directory.resolve(isInitiallyDeleted + "-" + shouldFailDuringWrite));
        ModelManager model = fullModel();
        if (isInitiallyDeleted) {
            model.deleteMember(model.findMember(ID).orElseThrow());
        }
        FailingWriter writer = new FailingWriter(shouldFailDuringWrite);
        JsonAddressBookStorage disk = new JsonAddressBookStorage(run.resolve("live.json"), writer);
        StorageManager storage = new StorageManager(disk, new JsonUserPrefsStorage(run.resolve("prefs.json")));
        storage.saveAddressBook(model.getAddressBook());
        byte[] saved = Files.readAllBytes(disk.getAddressBookFilePath());
        AddressBook before = new AddressBook(model.getAddressBook());
        LogicManager logic = new LogicManager(model, storage);
        String command = (isInitiallyDeleted ? "member restore nus/" : "member delete nus/") + ID;
        writer.shouldFail = true;
        CommandException error = assertThrows(CommandException.class, () -> logic.execute(command));
        assertSame(writer.failure, error.getCause());
        assertEquals(before, model.getAddressBook());
        assertArrayEquals(saved, Files.readAllBytes(disk.getAddressBookFilePath()));
        writer.shouldFail = false;
        logic.execute(command);
        ModelManager reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(!isInitiallyDeleted, reloaded.findMember(ID).orElseThrow().isDeleted());
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
        private final boolean shouldFailDuringWrite;
        private final IOException failure = new IOException("Controlled lifecycle save failure");
        private boolean shouldFail;

        FailingWriter(boolean shouldFailDuringWrite) {
            this.shouldFailDuringWrite = shouldFailDuringWrite;
        }

        @Override
        void writeTemporaryFile(Path temporary, byte[] bytes) throws IOException {
            if (shouldFail && shouldFailDuringWrite) {
                Files.write(temporary, new byte[] {bytes[0]});
                throw failure;
            }
            super.writeTemporaryFile(temporary, bytes);
        }

        @Override
        void replaceFile(Path temporary, Path target) throws IOException {
            if (shouldFail) {
                throw failure;
            }
            super.replaceFile(temporary, target);
        }
    }
}
