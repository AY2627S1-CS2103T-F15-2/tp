package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.beans.InvalidationListener;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.equipment.Availability;
import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.loan.Loan;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.testutil.PersonBuilder;

/** Real command/model/JSON transactions, including partial candidate writes and atomic replacement failure. */
public class LaplaceTransactionTest {
    private static final LocalDate DATE = LocalDate.of(2026, 1, 1);
    private static final NusId FIRST = new NusId("A0000001X");
    private static final NusId SECOND = new NusId("A0000002X");

    @TempDir
    public Path directory;

    @Test
    public void execute_eachLaplaceMutationAndFilesystemFailure_preservesMemoryDiskAndLaterCommands() throws Exception {
        List<String> commands = List.of(
                "member add nus/A0000003X name/Third Member phone/91234567 email/third@example.com",
                "member edit nus/" + FIRST + " name/Updated Name phone/98765432 email/updated@example.com",
                "member delete nus/" + SECOND,
                "equipment add name/Camera category/Test condition/GOOD notes/Case included",
                "equipment delete uuid/" + uuid(3),
                "loans add uuid/" + uuid(1) + " nus/" + FIRST + " assigned/" + DATE + " expected/" + DATE,
                "loans return uuid/" + uuid(2) + " returned/" + DATE);
        for (boolean partialWrite : List.of(true, false)) {
            for (int index = 0; index < commands.size(); index++) {
                Path folder = Files.createDirectory(directory.resolve(partialWrite + "-" + index));
                FailingWriter writer = new FailingWriter(partialWrite);
                JsonAddressBookStorage disk = new JsonAddressBookStorage(folder.resolve("data.json"), writer);
                CountingStorage storage = new CountingStorage(disk, folder);
                ModelManager model = sample();
                LogicManager logic = new LogicManager(model, storage);
                storage.saveAddressBook(model.getAddressBook());
                ReadOnlyAddressBook root = model.getAddressBook();
                var memberView = root.getMemberList();
                var equipmentView = root.getEquipmentList();
                var loanView = root.getLoanList();
                var personView = root.getPersonList();
                AddressBook before = new AddressBook(root);
                byte[] saved = Files.readAllBytes(disk.getAddressBookFilePath());
                int[] notifications = {0};
                InvalidationListener observer = unused -> {
                    notifications[0]++;
                    // Every observer must see the same committed snapshot as storage, with intact references.
                    assertEquals(0, root.getLoanList().stream()
                            .filter(loan -> root.getMemberList().stream()
                                    .noneMatch(member -> member.getNusId().equals(loan.getMemberNusId())))
                            .count());
                };
                memberView.addListener(observer);
                equipmentView.addListener(observer);
                loanView.addListener(observer);
                personView.addListener(observer);
                int attempts = storage.attempts;
                writer.fail = true;
                String command = commands.get(index);
                CommandException error = assertThrows(CommandException.class, () -> logic.execute(command), command);
                assertSame(writer.failure, error.getCause());
                assertEquals(attempts + 1, storage.attempts);
                assertEquals(1, storage.saved);
                assertEquals(0, notifications[0]);
                assertEquals(before, root, command);
                assertArrayEquals(saved, Files.readAllBytes(disk.getAddressBookFilePath()), command);
                assertEquals(Availability.AVAILABLE, model.getAvailability(uuid(1)));
                assertEquals(Availability.ASSIGNED, model.getAvailability(uuid(2)));
                assertEquals(before, storage.readAddressBook().orElseThrow());
                assertSame(root, model.getAddressBook());
                assertSame(memberView, root.getMemberList());
                assertSame(equipmentView, root.getEquipmentList());
                assertSame(loanView, root.getLoanList());
                assertSame(personView, root.getPersonList());
                try (var files = Files.list(folder)) {
                    assertFalse(files.anyMatch(path -> path.getFileName().toString().endsWith(".tmp")));
                }
                writer.fail = false;
                logic.execute("member list");
                assertEquals(before, root, "A later success must not persist an earlier failed operation");
                assertArrayEquals(saved, Files.readAllBytes(disk.getAddressBookFilePath()));
                assertEquals(0, notifications[0]);
                assertEquals(2, storage.saved);
                logic.execute(command);
                assertEquals(3, storage.saved);
                assertEquals(attempts + 3, storage.attempts);
                assertTrue(notifications[0] > 0);
                assertEquals(root, new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs())
                        .getAddressBook());
                assertSame(root, model.getAddressBook());
                assertSame(memberView, root.getMemberList());
                assertSame(equipmentView, root.getEquipmentList());
                assertSame(loanView, root.getLoanList());
            }
        }
    }

    @Test
    public void save_firstWriteFails_leavesNoDataFileOrCommittedRecords() throws Exception {
        FailingWriter writer = new FailingWriter(true);
        writer.fail = true;
        JsonAddressBookStorage disk = new JsonAddressBookStorage(directory.resolve("new/data.json"), writer);
        ModelManager model = new ModelManager();
        LogicManager logic = new LogicManager(model, new CountingStorage(disk, directory));
        assertThrows(CommandException.class, () -> logic.execute(
                "member add nus/A0000001X name/Alex phone/91234567 email/alex@example.com"));
        assertTrue(model.getMemberList().isEmpty());
        assertFalse(Files.exists(disk.getAddressBookFilePath()));
        assertTrue(disk.readAddressBook().isEmpty());
    }

    @Test
    public void save_unsupportedAtomicReplacement_preservesOriginalAndException() throws Exception {
        Path file = directory.resolve("existing.json");
        Files.writeString(file, "original bytes");
        IOException failure = new AtomicMoveNotSupportedException(file.toString(), file.toString(), "unsupported");
        AtomicFileWriter writer = new AtomicFileWriter() {
            @Override
            void replaceFile(Path temporary, Path target) throws IOException {
                throw failure;
            }
        };
        assertSame(failure, assertThrows(IOException.class, () -> writer.write(file, new byte[] {1, 2, 3})));
        assertEquals("original bytes", Files.readString(file));
        try (var files = Files.list(directory)) {
            assertEquals(List.of(file), files.toList());
        }
    }

    private static UUID uuid(int value) {
        return new UUID(0, value);
    }

    private static ModelManager sample() {
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder().build());
        for (NusId id : List.of(FIRST, SECOND)) {
            model.addMember(new Member(id, new Name("Same Name"), new Phone("91234567"),
                    new Email("same@example.com")));
        }
        for (int index = 1; index <= 3; index++) {
            model.addEquipment(new Equipment(uuid(index), "Camera", "Test", Condition.GOOD, "Original notes"));
        }
        model.addLoan(new Loan(uuid(2), FIRST, DATE, DATE.plusDays(1), null));
        model.addLoan(new Loan(uuid(3), SECOND, DATE, DATE.plusDays(1), DATE));
        return model;
    }

    /** Fails inside the real candidate write or immediately before the atomic replacement. */
    private static class FailingWriter extends AtomicFileWriter {
        private final boolean partialWrite;
        private final IOException failure = new IOException("Controlled filesystem failure");
        private boolean fail;

        FailingWriter(boolean partialWrite) {
            this.partialWrite = partialWrite;
        }

        @Override
        void writeTemporaryFile(Path temporary, byte[] content) throws IOException {
            if (fail && partialWrite) {
                Files.writeString(temporary, "{partial candidate");
                throw failure;
            }
            super.writeTemporaryFile(temporary, content);
        }

        @Override
        void replaceFile(Path temporary, Path target) throws IOException {
            if (fail) {
                throw failure;
            }
            super.replaceFile(temporary, target);
        }
    }

    private static class CountingStorage extends StorageManager {
        private int attempts;
        private int saved;

        CountingStorage(JsonAddressBookStorage disk, Path folder) {
            super(disk, new JsonUserPrefsStorage(folder.resolve("prefs.json")));
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook data) throws IOException {
            attempts++;
            super.saveAddressBook(data);
            saved++;
        }
    }
}
