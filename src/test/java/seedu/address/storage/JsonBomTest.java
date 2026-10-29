package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.AddressBook;

/**
 * Verifies editor BOM compatibility without stripping embedded data or malformed syntax.
 */
public class JsonBomTest {
    @TempDir
    public Path directory;

    @Test
    public void readAndSave_leadingBom_matchesUnmarkedControlAndPreservesRecords() throws Exception {
        Path file = directory.resolve("bom.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        AddressBook expected = RootPersistenceTestData.fullAddressBook();
        storage.saveAddressBook(expected);
        String original = Files.readString(file);
        assertEquals(expected, new AddressBook(storage.readAddressBook().orElseThrow()));
        Files.writeString(file, "\ufeff" + original);

        var loaded = storage.readAddressBook().orElseThrow();
        assertEquals(expected, new AddressBook(loaded));
        assertEquals("\ufeff" + original, Files.readString(file));
        storage.saveAddressBook(loaded);
        assertFalse(Files.readString(file).startsWith("\ufeff"));
        assertEquals(expected, new AddressBook(storage.readAddressBook().orElseThrow()));
    }

    @Test
    public void read_misplacedOrRepeatedBom_keepsRejectingMalformedSyntax() throws Exception {
        Path file = directory.resolve("invalid.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        for (String json : List.of("\ufeff\ufeff{\"persons\":[]}", "{\ufeff\"persons\":[]}",
                " \ufeff{\"persons\":[]}", "\ufeff{\"persons\":[}")) {
            Files.writeString(file, json);
            assertThrows(DataLoadingException.class, storage::readAddressBook);
            assertEquals(json, Files.readString(file));
        }
    }

    @Test
    public void read_bomWithinText_preservesTheCharacter() throws Exception {
        Path file = directory.resolve("text.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        storage.saveAddressBook(RootPersistenceTestData.fullAddressBook());
        String original = Files.readString(file).replace("Case included", "Case\ufeff included");
        Files.writeString(file, "\ufeff" + original);
        var loaded = storage.readAddressBook().orElseThrow();
        assertEquals("Case\ufeff included", loaded.getEquipmentList().get(0).getNotes());
        storage.saveAddressBook(loaded);
        assertEquals(loaded.getEquipmentList(), storage.readAddressBook().orElseThrow().getEquipmentList());
    }
}
