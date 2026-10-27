package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;

/**
 * Verifies duplicate properties cannot silently replace persisted collections or record fields.
 */
public class JsonDuplicatePropertyTest {
    @TempDir
    public Path testFolder;

    @Test
    public void readAddressBook_duplicateRootCollections_rejectsWithoutRewriting() throws Exception {
        String original = validJson().toString();
        for (String field : List.of("persons", "members", "equipment", "loans")) {
            String duplicate = original.substring(0, original.length() - 1) + ",\"" + field + "\":[]}";
            assertDuplicateRejected(duplicate, field);
        }
    }

    @Test
    public void readAddressBook_escapedDuplicateName_rejectsWithoutRewriting() throws Exception {
        assertDuplicateRejected("{\"persons\":[],\"loans\":[],\"lo\\u0061ns\":[]}", "loans");
    }

    @Test
    public void readAddressBook_duplicateIdentityAndLifecycleFields_rejectsWithoutRewriting() throws Exception {
        for (List<String> target : List.of(List.of("members", "nusId"), List.of("equipment", "uuid"),
                List.of("equipment", "condition"), List.of("equipment", "deleted"),
                List.of("loans", "equipmentUuid"), List.of("loans", "memberNusId"),
                List.of("loans", "returnedDate"), List.of("loans", "issueCondition"),
                List.of("loans", "returnCondition"))) {
            ObjectNode json = validJson();
            JsonNode record = json.get(target.get(0)).get(0);
            String field = target.get(1);
            String original = record.toString();
            String duplicate = original.substring(0, original.length() - 1)
                    + ",\"" + field + "\":" + record.get(field) + "}";
            assertDuplicateRejected(json.toString().replace(original, duplicate), field);
        }
    }

    @Test
    public void readAndSaveAddressBook_repeatedFieldsInSeparateRecords_preservesAllRecords() throws Exception {
        Path file = testFolder.resolve("valid.json");
        AddressBook original = RootPersistenceTestData.fullAddressBook();
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        storage.saveAddressBook(original);

        AddressBook loaded = new AddressBook(storage.readAddressBook().orElseThrow());
        assertEquals(original, loaded);
        storage.saveAddressBook(loaded);
        assertEquals(original, new AddressBook(storage.readAddressBook().orElseThrow()));
    }

    /**
     * Returns valid persisted data containing both open and completed loan records.
     */
    private ObjectNode validJson() throws Exception {
        return (ObjectNode) new ObjectMapper().readTree(JsonUtil.toJsonString(
                new JsonSerializableAddressBook(RootPersistenceTestData.fullAddressBook())));
    }

    /**
     * Verifies parsing identifies the duplicate before conversion and leaves the source bytes intact.
     */
    private void assertDuplicateRejected(String json, String field) throws Exception {
        Path file = testFolder.resolve("duplicate.json");
        Files.writeString(file, json);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);

        DataLoadingException failure = assertThrows(DataLoadingException.class, storage::readAddressBook);
        Throwable cause = failure.getCause();
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        assertInstanceOf(JsonParseException.class, cause);
        assertTrue(cause.getMessage().contains("Duplicate field '" + field + "'"));
        assertEquals(json, Files.readString(file));
    }
}
