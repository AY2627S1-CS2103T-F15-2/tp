package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.equipment.Condition;

/**
 * Verifies one condition vocabulary across commands and every persisted condition field.
 */
public class StoredConditionCaseTest {
    @TempDir
    public Path directory;

    @Test
    public void readAndSave_mixedCaseAndWhitespace_writesCanonicalConditions() throws Exception {
        Path file = directory.resolve("conditions.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        for (Condition condition : Condition.values()) {
            ObjectNode json = snapshot();
            ObjectNode equipment = (ObjectNode) json.get("equipment").get(0);
            ObjectNode loan = (ObjectNode) json.get("loans").get(1);
            String input = " \t" + condition.name().toLowerCase(Locale.ROOT) + "\n";
            equipment.put("condition", input);
            loan.put("issueCondition", input);
            loan.put("returnCondition", input);
            Files.writeString(file, json.toString());
            var loaded = storage.readAddressBook().orElseThrow();
            assertEquals(condition, loaded.getEquipmentList().get(0).getCondition());
            assertEquals(condition, loaded.getLoanList().get(1).getIssueCondition().orElseThrow());
            assertEquals(condition, loaded.getLoanList().get(1).getReturnCondition().orElseThrow());
            storage.saveAddressBook(loaded);
            var saved = new ObjectMapper().readTree(Files.readString(file));
            assertEquals(condition.name(), saved.get("equipment").get(0).get("condition").asText());
            assertEquals(condition.name(), saved.get("loans").get(1).get("issueCondition").asText());
            assertEquals(condition.name(), saved.get("loans").get(1).get("returnCondition").asText());
            assertEquals(new AddressBook(loaded), new AddressBook(storage.readAddressBook().orElseThrow()));
        }
    }

    @Test
    public void read_unknownAndBlankConditions_rejectsEveryFieldWithoutRewriting() throws Exception {
        Path file = directory.resolve("invalid.json");
        for (List<String> target : List.of(List.of("equipment", "condition"),
                List.of("loans", "issueCondition"), List.of("loans", "returnCondition"))) {
            for (String value : List.of("BROKEN", "", " \t")) {
                ObjectNode json = snapshot();
                ObjectNode record = (ObjectNode) json.get(target.get(0)).get(target.get(0).equals("loans") ? 1 : 0);
                record.put(target.get(1), value);
                String original = json.toString();
                Files.writeString(file, original);
                assertThrows(DataLoadingException.class, new JsonAddressBookStorage(file)::readAddressBook);
                assertEquals(original, Files.readString(file));
            }
        }
    }

    private ObjectNode snapshot() throws Exception {
        return (ObjectNode) new ObjectMapper().readTree(JsonUtil.toJsonString(
                new JsonSerializableAddressBook(RootPersistenceTestData.fullAddressBook())));
    }
}
