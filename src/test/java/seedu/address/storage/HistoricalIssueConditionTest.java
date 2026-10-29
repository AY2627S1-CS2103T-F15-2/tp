package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.node.ObjectNode;

import seedu.address.model.AddressBook;
import seedu.address.model.equipment.Condition;

/**
 * Protects historical observations independently of present assignment eligibility.
 */
public class HistoricalIssueConditionTest {
    @TempDir
    public Path directory;

    @Test
    public void readAndSave_knownIssueConditions_preservesObservationAndCurrentCondition() throws Exception {
        Path file = directory.resolve("history.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        for (Condition condition : Condition.values()) {
            ObjectNode json = RootPersistenceTestData.jsonSnapshot();
            ObjectNode loan = (ObjectNode) json.get("loans").get(0);
            loan.put("issueCondition", condition.name());
            Files.writeString(file, json.toString());

            AddressBook loaded = new AddressBook(storage.readAddressBook().orElseThrow());
            assertEquals(condition, loaded.getLoanList().get(0).getIssueCondition().orElseThrow());
            assertEquals(Condition.GOOD, loaded.getEquipmentList().get(0).getCondition());
            storage.saveAddressBook(loaded);
            assertEquals(loaded, new AddressBook(storage.readAddressBook().orElseThrow()));
        }
    }

    @Test
    public void readAndSave_missingAndNullIssueCondition_preservesUnknownObservation() throws Exception {
        Path file = directory.resolve("legacy.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        for (String value : new String[] {"", ",\"issueCondition\":null"}) {
            ObjectNode json = RootPersistenceTestData.jsonSnapshot();
            ObjectNode loan = (ObjectNode) json.get("loans").get(0);
            loan.remove("issueCondition");
            String record = loan.toString();
            Files.writeString(file, json.toString().replace(record,
                    record.substring(0, record.length() - 1) + value + "}"));
            var loaded = storage.readAddressBook().orElseThrow();
            assertTrue(loaded.getLoanList().get(0).getIssueCondition().isEmpty());
            storage.saveAddressBook(loaded);
            assertEquals(loaded.getLoanList(), storage.readAddressBook().orElseThrow().getLoanList());
        }
    }
}
