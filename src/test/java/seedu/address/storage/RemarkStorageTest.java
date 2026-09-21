package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.Remark;
import seedu.address.testutil.PersonBuilder;

public class RemarkStorageTest {

    @TempDir
    public Path testFolder;

    @Test
    public void remark_addEditAndRemove_survivesStorageRoundTrips() throws Exception {
        JsonAddressBookStorage storage = new JsonAddressBookStorage(testFolder.resolve("addressbook.json"));
        AddressBookParser parser = new AddressBookParser();
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        parser.parseCommand("remark 1 r/Likes baseball — \"weekends\"").execute(model);
        Person expected = new PersonBuilder(ALICE).withRemark("Likes baseball — \"weekends\"").build();
        assertEquals(expected, model.getFilteredPersonList().get(0));

        storage.saveAddressBook(model.getAddressBook());
        Model reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(model.getAddressBook(), reloaded.getAddressBook());
        parser.parseCommand("edit 1 p/91234567").execute(reloaded);
        assertEquals(expected.getRemark(), reloaded.getFilteredPersonList().get(0).getRemark());

        parser.parseCommand("remark 1 r/").execute(reloaded);
        storage.saveAddressBook(reloaded.getAddressBook());
        assertEquals(reloaded.getAddressBook(), storage.readAddressBook().orElseThrow());
        assertEquals(new Remark(""), storage.readAddressBook().orElseThrow().getPersonList().get(0).getRemark());
    }

    @Test
    public void read_legacyFileWithoutRemarks_preservesContacts() throws Exception {
        Path legacy = Path.of("src", "test", "data", "JsonSerializableAddressBookTest",
                "typicalPersonsAddressBook.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(legacy);
        assertEquals(getTypicalAddressBook(), storage.readAddressBook().orElseThrow());
        assertTrue(storage.readAddressBook().orElseThrow().getPersonList().stream()
                .allMatch(person -> person.getRemark().value.isEmpty()));
    }

    @Test
    public void read_explicitNullRemark_usesEmptyRemark() throws Exception {
        Path file = testFolder.resolve("null-remark.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        storage.saveAddressBook(getTypicalAddressBook());
        String json = Files.readString(file);
        assertTrue(json.contains("\"remark\" : \"\""));
        Files.writeString(file, json.replace("\"remark\" : \"\"", "\"remark\" : null"));
        assertEquals(getTypicalAddressBook(), storage.readAddressBook().orElseThrow());
    }
}
