package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;

/**
 * Checks real JSON round trips and ensures rejected edits never reach storage.
 */
public class MemberEditPersistenceTest {
    @TempDir
    public Path directory;

    @Test
    public void edit_legacyStoredMemberAndRejectedRequests_preservesHistoryAndSavedBytes() throws Exception {
        ReadOnlyAddressBook original = new JsonAddressBookStorage(
                Path.of("src/test/data/JsonSerializableAddressBookTest/fullAddressBook.json"))
                .readAddressBook().orElseThrow();
        ModelManager model = new ModelManager(original, new UserPrefs());
        CountingStorage storage = new CountingStorage(directory);
        storage.saveAddressBook(original);
        LogicManager logic = new LogicManager(model, storage);
        logic.execute("member edit nus/a0123456x name/Anne-Marie O'Connor phone/+65 9876-5432"
                + " email/ANNE@EXAMPLE.COM");
        assertEquals(2, storage.saves);
        assertEquals(original.getLoanList(), model.getLoanList());
        assertEquals(original.getEquipmentList(), model.getEquipmentList());
        assertEquals(original.getPersonList(), model.getAddressBook().getPersonList());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        assertRejectedEditsDoNotSave(logic, storage);
        assertEquals(model.getAddressBook(), new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs())
                .getAddressBook());
    }

    private void assertRejectedEditsDoNotSave(LogicManager logic, CountingStorage storage) throws Exception {
        byte[] saved = Files.readAllBytes(storage.getAddressBookFilePath());
        for (String fields : List.of("nus/A0123456X name/Anne-Marie O'Connor", "nus/A9999999X name/Unknown",
                "nus/A0123456X phone/+65 9876-5432", "nus/A0123456X email/ANNE@EXAMPLE.COM")) {
            assertThrows(CommandException.class, () -> logic.execute("member edit " + fields));
            assertArrayEquals(saved, Files.readAllBytes(storage.getAddressBookFilePath()));
        }
        for (String fields : List.of("nus/A0123456X", "nus/A0123456X phone/invalid")) {
            assertThrows(ParseException.class, () -> logic.execute("member edit " + fields));
            assertArrayEquals(saved, Files.readAllBytes(storage.getAddressBookFilePath()));
        }
        assertEquals(2, storage.saves);
    }

    /**
     * Counts successful writes through the real JSON storage implementation.
     */
    private static class CountingStorage extends StorageManager {
        private int saves;

        CountingStorage(Path directory) {
            super(new JsonAddressBookStorage(directory.resolve("members.json")),
                    new JsonUserPrefsStorage(directory.resolve("preferences.json")));
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook data) throws IOException {
            super.saveAddressBook(data);
            saves++;
        }
    }
}
