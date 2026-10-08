package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.testutil.TypicalPersons;

public class JsonSerializableAddressBookTest {

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonSerializableAddressBookTest");
    private static final Path TYPICAL_PERSONS_FILE = TEST_DATA_FOLDER.resolve("typicalPersonsAddressBook.json");
    private static final Path INVALID_PERSON_FILE = TEST_DATA_FOLDER.resolve("invalidPersonAddressBook.json");
    private static final Path DUPLICATE_PERSON_FILE = TEST_DATA_FOLDER.resolve("duplicatePersonAddressBook.json");

    @Test
    public void toModelType_typicalPersonsFile_success() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(TYPICAL_PERSONS_FILE,
                JsonSerializableAddressBook.class).get();
        AddressBook addressBookFromFile = dataFromFile.toModelType();
        AddressBook typicalPersonsAddressBook = TypicalPersons.getTypicalAddressBook();
        assertEquals(addressBookFromFile, typicalPersonsAddressBook);
    }

    @Test
    public void toModelType_invalidPersonFile_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(INVALID_PERSON_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, dataFromFile::toModelType);
    }

    @Test
    public void toModelType_duplicatePersons_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(DUPLICATE_PERSON_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_PERSON,
                dataFromFile::toModelType);
    }

    @Test
    public void toModelType_fullFixture_restoresAllCollections() throws Exception {
        assertEquals(RootPersistenceTestData.fullAddressBook(), readFixture("fullAddressBook.json").toModelType());
    }

    @Test
    public void toModelType_invalidFixtures_rejectsEntireLoad() throws Exception {
        for (String file : List.of("duplicateMembers.json", "duplicateEquipment.json", "duplicateOpenLoans.json",
                "missingMembersOpen.json", "missingMembersClosed.json", "missingEquipmentOpen.json",
                "missingEquipmentClosed.json", "invalidMembers.json", "invalidEquipment.json", "invalidLoans.json")) {
            JsonSerializableAddressBook data = readFixture(file);
            assertThrows(IllegalValueException.class, data::toModelType);
        }
    }

    @Test
    public void toModelType_missingOrNullNewArrays_preservesLegacyPersons() throws Exception {
        ObjectNode legacy = (ObjectNode) new ObjectMapper().readTree(
                JsonUtil.toJsonString(new JsonSerializableAddressBook(TypicalPersons.getTypicalAddressBook())));
        for (String field : List.of("members", "equipment", "loans")) {
            legacy.remove(field);
        }
        assertEquals(TypicalPersons.getTypicalAddressBook(), convert(legacy).toModelType());
        for (String field : List.of("members", "equipment", "loans")) {
            legacy.putNull(field);
        }
        assertEquals(TypicalPersons.getTypicalAddressBook(), convert(legacy).toModelType());
    }

    @Test
    public void toModelType_nullEntriesInEachCollection_rejects() throws Exception {
        for (String field : List.of("persons", "members", "equipment", "loans")) {
            ObjectNode json = fullJson();
            json.withArray(field).addNull();
            JsonSerializableAddressBook data = convert(json);
            assertThrows(IllegalValueException.class, data::toModelType);
        }
    }

    @Test
    public void toModelType_missingOrNullPersons_rejects() throws Exception {
        ObjectNode json = fullJson();
        json.remove("persons");
        JsonSerializableAddressBook missing = convert(json);
        assertThrows(IllegalValueException.class, missing::toModelType);
        json.putNull("persons");
        JsonSerializableAddressBook nullPersons = convert(json);
        assertThrows(IllegalValueException.class, nullPersons::toModelType);
    }

    @Test
    public void constructor_snapshotDoesNotChangeWhenSourceChanges() throws Exception {
        AddressBook source = RootPersistenceTestData.fullAddressBook();
        AddressBook expected = new AddressBook(source);
        JsonSerializableAddressBook snapshot = new JsonSerializableAddressBook(source);
        source.closeLoan(source.getEquipmentList().get(0).getUuid(), LocalDate.of(2026, 10, 8));
        source.resetData(new AddressBook());
        assertEquals(expected, snapshot.toModelType());
    }

    @Test
    public void constructor_copiesCallerArrays() throws Exception {
        List<JsonAdaptedPerson> persons = new ArrayList<>();
        List<JsonAdaptedMember> members = new ArrayList<>();
        List<JsonAdaptedEquipment> equipment = new ArrayList<>();
        List<JsonAdaptedLoan> loans = new ArrayList<>();
        AddressBook expected = RootPersistenceTestData.fullAddressBook();
        expected.getPersonList().forEach(person -> persons.add(new JsonAdaptedPerson(person)));
        expected.getMemberList().forEach(member -> members.add(new JsonAdaptedMember(member)));
        expected.getEquipmentList().forEach(item -> equipment.add(new JsonAdaptedEquipment(item)));
        expected.getLoanList().forEach(loan -> loans.add(new JsonAdaptedLoan(loan)));
        JsonSerializableAddressBook snapshot = new JsonSerializableAddressBook(persons, members, equipment, loans);
        persons.clear();
        members.clear();
        equipment.clear();
        loans.clear();
        assertEquals(expected, snapshot.toModelType());
    }

    @Test
    public void toModelType_optionalAdapterFields_missingValuesRestoreDefaults() throws Exception {
        ObjectNode json = fullJson();
        ObjectNode item = (ObjectNode) json.withArray("equipment").get(0);
        ObjectNode loan = (ObjectNode) json.withArray("loans").get(0);
        item.remove("notes");
        loan.remove("returnedDate");
        AddressBook loaded = convert(json).toModelType();
        assertEquals("", loaded.getEquipmentList().get(0).getNotes());
        assertTrue(loaded.getLoanList().get(0).isOpen());
    }

    private static ObjectNode fullJson() throws Exception {
        return (ObjectNode) new ObjectMapper().readTree(JsonUtil.toJsonString(
                new JsonSerializableAddressBook(RootPersistenceTestData.fullAddressBook())));
    }

    private static JsonSerializableAddressBook convert(JsonNode json) throws Exception {
        return JsonUtil.fromJsonString(json.toString(), JsonSerializableAddressBook.class);
    }

    private static JsonSerializableAddressBook readFixture(String name) throws Exception {
        return JsonUtil.readJsonFile(TEST_DATA_FOLDER.resolve(name), JsonSerializableAddressBook.class).orElseThrow();
    }

}
