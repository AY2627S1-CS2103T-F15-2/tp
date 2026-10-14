package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.parser.EquipmentAddCommandParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.equipment.Condition;
import seedu.address.storage.JsonAddressBookStorage;

/** Direct leaf integration; family routing and LogicManager saving await Duy's activation. */
public class EquipmentAddCommandIntegrationTest {

    @TempDir
    public Path testFolder;

    @Test
    public void addSaveReload_allConditions_preservesIdentityNotesAndDerivedAvailability() throws Exception {
        ModelManager model = new ModelManager();
        EquipmentAddCommandParser parser = new EquipmentAddCommandParser();
        for (Condition condition : Condition.values()) {
            parser.parse("name/Camera (kit) category/Photo condition/" + condition
                    + " notes/Includes battery & charger").execute(model);
        }
        JsonAddressBookStorage storage = new JsonAddressBookStorage(testFolder.resolve("equipment.json"));
        storage.saveAddressBook(model.getAddressBook());
        ModelManager reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(model.getAddressBook(), reloaded.getAddressBook());
        assertEquals(4, reloaded.getEquipmentList().size());
        for (var equipment : model.getEquipmentList()) {
            assertEquals(model.getAvailability(equipment.getUuid()), reloaded.getAvailability(equipment.getUuid()));
            assertEquals("Includes battery & charger",
                    reloaded.findEquipment(equipment.getUuid()).orElseThrow().getNotes());
        }
        String saved = Files.readString(storage.getAddressBookFilePath());
        assertFalse(saved.contains("availability"));
        assertFalse(saved.contains("holder"));
        AddressBook before = new AddressBook(reloaded.getAddressBook());
        assertThrows(ParseException.class, () -> parser.parse(
                "name/Camera category/Photo condition/GOOD notes/one notes/two").execute(reloaded));
        assertEquals(before, reloaded.getAddressBook());
        assertEquals(saved, Files.readString(storage.getAddressBookFilePath()));
    }
}
