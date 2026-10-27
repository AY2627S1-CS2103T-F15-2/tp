package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.JsonUtil;

/**
 * Verifies failed conversions identify recoverable records without rewriting input.
 */
public class DataLoadingContextTest {
    @TempDir
    public Path directory;

    @Test
    public void read_malformedDate_identifiesLoanAndRejectedValue() throws Exception {
        ObjectNode json = snapshot();
        ObjectNode loan = (ObjectNode) json.get("loans").get(1);
        loan.put("returnedDate", "2026-99-01");
        assertContext(json, "loans[1]", loan.get("equipmentUuid").asText(),
                loan.get("memberNusId").asText(), "returnedDate", "2026-99-01");
    }

    @Test
    public void read_missingReference_identifiesLoanAndMissingMember() throws Exception {
        ObjectNode json = snapshot();
        ObjectNode loan = (ObjectNode) json.get("loans").get(0);
        loan.put("memberNusId", "A0000009X");
        assertContext(json, "loans[0]", loan.get("equipmentUuid").asText(), "A0000009X",
                JsonSerializableAddressBook.MESSAGE_MISSING_MEMBER);
    }

    @Test
    public void read_duplicateIdentities_identifiesSecondRecord() throws Exception {
        for (String collection : new String[] {"members", "equipment"}) {
            ObjectNode json = snapshot();
            ArrayNode records = (ArrayNode) json.get(collection);
            ObjectNode duplicate = (ObjectNode) records.get(0).deepCopy();
            records.add(duplicate);
            String field = collection.equals("members") ? "nusId" : "uuid";
            assertContext(json, collection + "[1]", duplicate.get(field).asText(), "duplicate");
        }
    }

    @Test
    public void read_invalidConditionOrNullEntry_identifiesRecordBeforeConversion() throws Exception {
        ObjectNode json = snapshot();
        ObjectNode equipment = (ObjectNode) json.get("equipment").get(0);
        equipment.put("condition", "BROKEN");
        assertContext(json, "equipment[0]", equipment.get("uuid").asText(), "BROKEN", "condition");
        json = snapshot();
        ArrayNode loans = (ArrayNode) json.get("loans");
        loans.addNull();
        assertContext(json, "loans[2]", "identity unavailable", "null");
    }

    /**
     * Returns a valid all-collection fixture whose reference identities are known before conversion.
     */
    private ObjectNode snapshot() throws Exception {
        return (ObjectNode) new ObjectMapper().readTree(JsonUtil.toJsonString(
                new JsonSerializableAddressBook(RootPersistenceTestData.fullAddressBook())));
    }

    /**
     * Verifies context, retained causes, and original file bytes at the public storage boundary.
     */
    private void assertContext(ObjectNode json, String... fragments) throws Exception {
        Path file = directory.resolve("invalid.json");
        String original = json.toString();
        Files.writeString(file, original);
        DataLoadingException failure = assertThrows(DataLoadingException.class,
                new JsonAddressBookStorage(file)::readAddressBook);
        assertNotNull(failure.getCause().getCause());
        for (String fragment : fragments) {
            assertTrue(failure.getMessage().contains(fragment), failure.getMessage());
        }
        assertEquals(original, Files.readString(file));
    }
}
