package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.AddressBook;
import seedu.address.model.equipment.Condition;

/**
 * Verifies unknown loan fields cannot silently change the meaning of retained history.
 */
public class JsonLoanSchemaTest {
    @TempDir
    public Path testFolder;

    @Test
    public void readAddressBook_misspelledLifecycleFields_rejectsWithoutRewriting() throws Exception {
        for (List<String> names : List.of(List.of("returnedDate", "returnDate"),
                List.of("returnCondition", "returnCondtion"), List.of("issueCondition", "issueCondtion"))) {
            ObjectNode json = RootPersistenceTestData.jsonSnapshot();
            ArrayNode loans = (ArrayNode) json.get("loans");
            loans.remove(0);
            ObjectNode loan = (ObjectNode) loans.get(0);
            loan.set(names.get(1), loan.remove(names.get(0)));

            assertUnknownFieldRejected(json, names.get(1));
        }
    }

    @Test
    public void readAddressBook_futureLoanFields_rejectsObjectsAndNullValues() throws Exception {
        for (String value : List.of("{\"observed\":true}", "null")) {
            ObjectNode json = RootPersistenceTestData.jsonSnapshot();
            ObjectNode loan = (ObjectNode) json.get("loans").get(0);
            loan.set("futureLifecycle", new ObjectMapper().readTree(value));

            assertUnknownFieldRejected(json, "futureLifecycle");
        }
    }

    @Test
    public void readAndSaveAddressBook_missingOrNullOptionalFields_preservesLegacyLoans() throws Exception {
        AddressBook expected = RootPersistenceTestData.fullAddressBook();
        Path file = testFolder.resolve("legacy.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        for (boolean shouldOmitFields : List.of(false, true)) {
            ObjectNode json = RootPersistenceTestData.jsonSnapshot();
            for (JsonNode node : json.get("loans")) {
                ObjectNode loan = (ObjectNode) node;
                for (String field : List.of("issueCondition", "returnCondition")) {
                    if (shouldOmitFields) {
                        loan.remove(field);
                    } else {
                        loan.putNull(field);
                    }
                }
                if (shouldOmitFields && loan.get("returnedDate").isNull()) {
                    loan.remove("returnedDate");
                }
            }
            Files.writeString(file, json.toString());
            AddressBook loaded = new AddressBook(storage.readAddressBook().orElseThrow());
            assertEquals(expected, loaded);
            storage.saveAddressBook(loaded);
            assertEquals(expected, new AddressBook(storage.readAddressBook().orElseThrow()));
        }
    }

    @Test
    public void readAndSaveAddressBook_correctLifecycleFields_preservesRecordedConditions() throws Exception {
        ObjectNode json = RootPersistenceTestData.jsonSnapshot();
        ObjectNode loan = (ObjectNode) json.get("loans").get(1);
        loan.put("issueCondition", "GOOD");
        loan.put("returnCondition", "FAIR");
        Path file = testFolder.resolve("conditions.json");
        Files.writeString(file, json.toString());
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);

        AddressBook loaded = new AddressBook(storage.readAddressBook().orElseThrow());
        var completed = loaded.getLoanList().get(1);
        assertFalse(completed.isOpen());
        assertEquals(Condition.GOOD, completed.getIssueCondition().orElseThrow());
        assertEquals(Condition.FAIR, completed.getReturnCondition().orElseThrow());
        assertEquals(loan.get("returnedDate").asText(), completed.getReturnedDate().orElseThrow().toString());
        storage.saveAddressBook(loaded);
        assertEquals(loaded, new AddressBook(storage.readAddressBook().orElseThrow()));
    }

    @Test
    public void readAddressBook_unknownFieldsOutsideLoans_preservesExistingCompatibility() throws Exception {
        ObjectNode json = RootPersistenceTestData.jsonSnapshot();
        json.putObject("futureMetadata").put("version", 2);
        ObjectNode member = (ObjectNode) json.get("members").get(0);
        member.put("futureMemberField", "value");
        ObjectNode equipment = (ObjectNode) json.get("equipment").get(0);
        equipment.put("futureEquipmentField", "value");
        Path file = testFolder.resolve("metadata.json");
        Files.writeString(file, json.toString());

        assertEquals(RootPersistenceTestData.fullAddressBook(),
                new AddressBook(new JsonAddressBookStorage(file).readAddressBook().orElseThrow()));
    }

    /**
     * Verifies a checked failure identifies the field and preserves the rejected file for recovery.
     */
    private void assertUnknownFieldRejected(ObjectNode json, String field) throws Exception {
        Path file = testFolder.resolve("unknown.json");
        String original = json.toString();
        Files.writeString(file, original);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);

        DataLoadingException failure = assertThrows(DataLoadingException.class, storage::readAddressBook);
        assertNotNull(failure.getCause());
        assertTrue(failure.getCause().getMessage().contains("Unknown loan field '" + field + "'"));
        assertTrue(failure.getCause().getMessage().contains("returnedDate"));
        assertTrue(failure.getCause().getMessage().contains("returnCondition"));
        assertEquals(original, Files.readString(file));
    }
}
