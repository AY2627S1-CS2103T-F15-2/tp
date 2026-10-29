package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.testutil.TypicalPersons;

/**
 * Protects supported absent/null collection forms without inventing deleted records.
 */
public class LegacyCollectionPolicyTest {
    @TempDir
    public Path directory;

    @Test
    public void readAndSave_legacyCollectionForms_preservesPersonsAndCanonicalizesArrays() throws Exception {
        AddressBook expected = TypicalPersons.getTypicalAddressBook();
        Path file = directory.resolve("legacy.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        for (String form : List.of("missing", "null", "empty")) {
            ObjectNode json = snapshot(expected);
            for (String field : List.of("members", "equipment", "loans")) {
                if (form.equals("missing")) {
                    json.remove(field);
                } else if (form.equals("null")) {
                    json.putNull(field);
                }
            }
            Files.writeString(file, json.toString());
            AddressBook loaded = new AddressBook(storage.readAddressBook().orElseThrow());
            assertEquals(expected, loaded);
            storage.saveAddressBook(loaded);
            JsonNode saved = new ObjectMapper().readTree(Files.readString(file));
            for (String field : List.of("members", "equipment", "loans")) {
                assertEquals("[]", saved.get(field).toString());
            }
            assertEquals(expected, new AddressBook(storage.readAddressBook().orElseThrow()));
        }
    }

    @Test
    public void readAndSave_omittedDeletionFlagsAndNullLoans_preservesActiveIdentities() throws Exception {
        ObjectNode json = snapshot(RootPersistenceTestData.fullAddressBook());
        json.putNull("loans");
        ObjectNode member = (ObjectNode) json.get("members").get(0);
        member.remove("deleted");
        ObjectNode equipment = (ObjectNode) json.get("equipment").get(0);
        equipment.remove("deleted");
        Path file = directory.resolve("active.json");
        Files.writeString(file, json.toString());
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        AddressBook loaded = new AddressBook(storage.readAddressBook().orElseThrow());
        assertFalse(loaded.getMemberList().get(0).isDeleted());
        assertFalse(loaded.getEquipmentList().get(0).isDeleted());
        assertEquals(0, loaded.getLoanList().size());
        storage.saveAddressBook(loaded);
        assertEquals(loaded, new AddressBook(storage.readAddressBook().orElseThrow()));
    }

    /**
     * Returns mutable JSON for compatibility fixtures without changing the source model.
     */
    private ObjectNode snapshot(AddressBook book) throws Exception {
        return (ObjectNode) new ObjectMapper().readTree(JsonUtil.toJsonString(new JsonSerializableAddressBook(book)));
    }
}
