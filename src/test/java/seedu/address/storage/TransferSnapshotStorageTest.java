package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;

/** Interchange validation is deliberately stricter than backward-compatible startup loading. */
public class TransferSnapshotStorageTest {
    @TempDir
    public Path directory;

    private final TransferSnapshotStorage snapshots = new TransferSnapshotStorage();

    @Test
    public void roundTrip_completeAndEmptySnapshots_preservesAllCollectionsAndHistory() throws Exception {
        Path legacy = Path.of("src/test/data/JsonSerializableAddressBookTest/fullAddressBook.json");
        ReadOnlyAddressBook original = snapshots.read(legacy);
        assertEquals(2, original.getLoanList().size());
        Path destination = directory.resolve("nested folder/snapshot with spaces.json");
        snapshots.writeNew(destination, original);
        assertEquals(original, snapshots.read(destination));
        assertTrue(Files.readString(destination).contains("\"schemaVersion\" : 1"));
        Path empty = directory.resolve("empty.json");
        snapshots.writeNew(empty, new AddressBook());
        assertEquals(new AddressBook(), snapshots.read(empty));
        Path persons = directory.resolve("legacy.json");
        Files.writeString(persons, "{\"persons\":[]}");
        assertEquals(new AddressBook(), snapshots.read(persons));
    }

    @Test
    public void read_invalidStructureVersionAndFields_rejectsWithoutWriting() throws Exception {
        Path source = directory.resolve("invalid.json");
        for (String text : List.of("", "null", "[]", "1", "{", "{} {}", "{} null",
                "{\"persons\":[],\"schemaVersion\":2}", "{\"persons\":[],\"schemaVersion\":null}",
                "{\"persons\":[],\"schemaVersion\":\"1\"}", "{\"persons\":[],\"schemaVersion\":1.0}",
                "{\"persons\":[],\"persons\":[]}", "{\"persons\":[],\"futureField\":true}",
                "{\"persons\":[],\"members\":null}", "{\"persons\":[],\"equipment\":{}}",
                "{\"persons\":[],\"members\":[{\"nusId\":\"A0123456X\",\"archived\":true}]}",
                "{\"persons\":[],\"equipment\":[{\"uuid\":\"invalid\",\"availability\":\"AVAILABLE\"}]}")) {
            Files.writeString(source, text);
            assertThrows(IOException.class, () -> snapshots.read(source), text);
            assertEquals(text, Files.readString(source));
        }
        assertThrows(IOException.class, () -> snapshots.read(directory.resolve("missing.json")));
        assertThrows(IOException.class, () -> snapshots.read(directory));
    }

    @Test
    public void read_invalidEntitiesDuplicatesAndReferences_rejectsWholeSnapshot() {
        Path fixtures = Path.of("src/test/data/JsonSerializableAddressBookTest");
        for (String name : List.of("invalidMembers", "invalidEquipment", "invalidLoans", "duplicateMembers",
                "duplicateEquipment", "duplicateOpenLoans", "duplicatePersonAddressBook", "missingMembersOpen",
                "missingMembersClosed", "missingEquipmentOpen", "missingEquipmentClosed",
                "invalidPersonAddressBook")) {
            assertThrows(Exception.class, () -> snapshots.read(fixtures.resolve(name + ".json")), name);
        }
    }

    @Test
    public void read_nullNestedTag_reportsValidationErrorRatherThanCrashing() throws Exception {
        Path source = directory.resolve("null-tag.json");
        String full = Files.readString(Path.of("src/test/data/JsonSerializableAddressBookTest/fullAddressBook.json"));
        Files.writeString(source, full.replace("\"friends\"", "null"));
        assertThrows(IOException.class, () -> snapshots.read(source));
    }

    @Test
    public void manuallyEditedLiveFile_validContactReloadsAndInvalidContactLeavesOriginalBytes() throws Exception {
        Path live = directory.resolve("addressbook.json");
        String source = Files.readString(Path.of("src/test/data/JsonSerializableAddressBookTest/fullAddressBook.json"));
        String edited = source.replace("Alex Tan", "Alex Tan Edited");
        Files.writeString(live, edited);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(live);
        ReadOnlyAddressBook loaded = storage.readAddressBook().orElseThrow();
        assertEquals("Alex Tan Edited", loaded.getMemberList().getFirst().getName().fullName);
        assertEquals(1, loaded.getPersonList().size());
        assertEquals(1, loaded.getEquipmentList().size());
        assertEquals(2, loaded.getLoanList().size());
        String invalid = edited.replace("91234567", "invalid phone");
        Files.writeString(live, invalid);
        assertThrows(DataLoadingException.class, storage::readAddressBook);
        assertEquals(invalid, Files.readString(live));
    }

    @Test
    public void writeNew_existingDestinationOrInvalidParent_preservesBytesAndCleansCandidate() throws Exception {
        Path existing = directory.resolve("existing.json");
        Files.writeString(existing, "preserve these bytes");
        assertThrows(IOException.class, () -> snapshots.writeNew(existing, new AddressBook()));
        assertEquals("preserve these bytes", Files.readString(existing));
        assertThrows(IOException.class, () -> snapshots.writeNew(existing.resolve("child.json"), new AddressBook()));
        try (var entries = Files.list(directory)) {
            assertEquals(List.of(existing), entries.toList());
        }
    }
}
