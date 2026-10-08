package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.HOON;
import static seedu.address.testutil.TypicalPersons.IDA;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;

public class JsonAddressBookStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonAddressBookStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readAddressBook(null));
    }

    private java.util.Optional<ReadOnlyAddressBook> readAddressBook(String filePath) throws Exception {
        return new JsonAddressBookStorage(Paths.get(filePath)).readAddressBook(addToTestDataPathIfNotNull(filePath));
    }

    private Path addToTestDataPathIfNotNull(String prefsFileInTestDataFolder) {
        return prefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(prefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void read_missingFile_emptyResult() throws Exception {
        assertFalse(readAddressBook("NonExistentFile.json").isPresent());
    }

    @Test
    public void read_notJsonFormat_exceptionThrown() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("notJsonFormatAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidPersonAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidAndValidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidAndValidPersonAddressBook.json"));
    }

    @Test
    public void readAndSaveAddressBook_allInOrder_success() throws Exception {
        Path filePath = testFolder.resolve("TempAddressBook.json");
        AddressBook original = getTypicalAddressBook();
        JsonAddressBookStorage jsonAddressBookStorage = new JsonAddressBookStorage(filePath);

        // Save in new file and read back
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        ReadOnlyAddressBook readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Modify data, overwrite existing file, and read back
        original.addPerson(HOON);
        original.removePerson(ALICE);
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Save and read without specifying file path
        original.addPerson(IDA);
        jsonAddressBookStorage.saveAddressBook(original); // file path not specified
        readBack = jsonAddressBookStorage.readAddressBook().get(); // file path not specified
        assertEquals(original, new AddressBook(readBack));

    }

    @Test
    public void saveAddressBook_nullAddressBook_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(null, "SomeFile.json"));
    }

    /**
     * Saves {@code addressBook} at the specified {@code filePath}.
     */
    private void saveAddressBook(ReadOnlyAddressBook addressBook, String filePath) {
        try {
            new JsonAddressBookStorage(Paths.get(filePath))
                    .saveAddressBook(addressBook, addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }

    @Test
    public void saveAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(new AddressBook(), null));
    }
    @Test
    public void readAndSaveAddressBook_allCollections_realRoundTripAndOverwrite() throws Exception {
        AddressBook original = RootPersistenceTestData.fullAddressBook();
        Path file = testFolder.resolve("full.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        storage.saveAddressBook(original);
        AddressBook restored = new AddressBook(storage.readAddressBook().orElseThrow());
        assertEquals(original, restored);
        assertEquals(original.getMemberList(), restored.getMemberList());
        assertEquals(original.getEquipmentList(), restored.getEquipmentList());
        assertEquals(original.getLoanList(), restored.getLoanList());
        assertTrue(restored.getLoanList().get(0).isOpen());
        assertFalse(restored.getLoanList().get(1).isOpen());

        JsonNode json = new ObjectMapper().readTree(Files.readString(file));
        assertEquals(4, json.size());
        assertEquals(1, json.get("persons").size());
        assertEquals(1, json.get("members").size());
        assertEquals(1, json.get("equipment").size());
        assertEquals(2, json.get("loans").size());
        assertFalse(Files.readString(file).contains("availability"));

        restored.closeLoan(restored.getEquipmentList().get(0).getUuid(), LocalDate.of(2026, 10, 8));
        storage.saveAddressBook(restored, file);
        assertEquals(restored, new AddressBook(storage.readAddressBook(file).orElseThrow()));
        restored.removeEquipment(restored.getEquipmentList().get(0));
        restored.removeMember(restored.getMemberList().get(0));
        storage.saveAddressBook(restored);
        AddressBook afterDeletion = new AddressBook(storage.readAddressBook().orElseThrow());
        assertEquals(restored, afterDeletion);
        assertTrue(afterDeletion.getLoanList().isEmpty());
        assertEquals(original.getPersonList(), afterDeletion.getPersonList());
    }

    @Test
    public void readAddressBook_invalidRootFixtures_reportsLoadingErrorWithoutRewritingFile() throws Exception {
        Path fixtures = Paths.get("src", "test", "data", "JsonSerializableAddressBookTest");
        for (String name : List.of("duplicateMembers.json", "duplicateEquipment.json", "duplicateOpenLoans.json",
                "missingMembersOpen.json", "missingMembersClosed.json", "missingEquipmentOpen.json",
                "missingEquipmentClosed.json", "invalidMembers.json", "invalidEquipment.json", "invalidLoans.json")) {
            Path file = fixtures.resolve(name);
            String before = Files.readString(file);
            JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
            assertThrows(DataLoadingException.class, storage::readAddressBook);
            assertEquals(before, Files.readString(file));
        }
        assertThrows(DataLoadingException.class, () -> readAddressBook("missingLoanMember.json"));
    }

    @Test
    public void readAddressBook_legacyFile_preservesPersonsAndEmptyNewCollections() throws Exception {
        Path file = Paths.get("src", "test", "data", "JsonSerializableAddressBookTest",
                "typicalPersonsAddressBook.json");
        ReadOnlyAddressBook loaded = new JsonAddressBookStorage(file).readAddressBook().orElseThrow();
        assertEquals(getTypicalAddressBook(), new AddressBook(loaded));
        assertTrue(loaded.getMemberList().isEmpty());
        assertTrue(loaded.getEquipmentList().isEmpty());
        assertTrue(loaded.getLoanList().isEmpty());
    }

    @Test
    public void readAddressBook_nullEntriesOrWrongArrayTypes_reportsLoadingError() throws Exception {
        Path file = testFolder.resolve("invalid.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        for (String field : List.of("persons", "members", "equipment", "loans")) {
            for (boolean nullEntry : List.of(true, false)) {
                ObjectNode json = (ObjectNode) new ObjectMapper().readTree("{\"persons\":[]}");
                if (nullEntry) {
                    json.putArray(field).addNull();
                } else {
                    json.put(field, "unsupported");
                }
                Files.writeString(file, json.toString());
                assertThrows(DataLoadingException.class, storage::readAddressBook);
            }
        }
        for (String json : List.of("{}", "{\"persons\":null}")) {
            Files.writeString(file, json);
            assertThrows(DataLoadingException.class, storage::readAddressBook);
        }
    }

}
