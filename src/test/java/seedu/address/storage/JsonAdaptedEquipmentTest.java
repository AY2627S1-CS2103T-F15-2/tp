package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.Equipment;

public class JsonAdaptedEquipmentTest {

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonAdaptedEquipmentTest");
    private static final String VALID_UUID = "550e8400-e29b-41d4-a716-446655440000";
    private static final String VALID_NAME = "Camera";
    private static final String VALID_CATEGORY = "Photography";
    private static final String VALID_CONDITION = "GOOD";
    private static final String VALID_NOTES = "With strap";
    private static final String MISSING_FIELD_MESSAGE_FORMAT = "Equipment's %s field is missing!";

    private static final Equipment CAMERA = new Equipment(
            UUID.fromString(VALID_UUID), VALID_NAME, VALID_CATEGORY, Condition.GOOD, VALID_NOTES);

    @Test
    public void toModelType_validEquipmentDetails_returnsEquipment() throws Exception {
        JsonAdaptedEquipment adaptedEquipment = new JsonAdaptedEquipment(CAMERA);
        assertEquals(CAMERA, adaptedEquipment.toModelType());
    }

    @Test
    public void toModelType_validRawFields_returnsEquipment() throws Exception {
        JsonAdaptedEquipment adaptedEquipment = new JsonAdaptedEquipment(
                VALID_UUID, VALID_NAME, VALID_CATEGORY, VALID_CONDITION, VALID_NOTES);
        assertEquals(CAMERA, adaptedEquipment.toModelType());
    }

    @Test
    public void toModelType_everyCondition_preservesCondition() throws Exception {
        assertEquals(Condition.GOOD, equipmentWithCondition("GOOD").toModelType().getCondition());
        assertEquals(Condition.FAIR, equipmentWithCondition("FAIR").toModelType().getCondition());
        assertEquals(Condition.DAMAGED, equipmentWithCondition("DAMAGED").toModelType().getCondition());
        assertEquals(Condition.UNDER_REPAIR, equipmentWithCondition("UNDER_REPAIR").toModelType().getCondition());
    }

    @Test
    public void toModelType_nullNotes_becomesEmptyString() throws Exception {
        JsonAdaptedEquipment adaptedEquipment = new JsonAdaptedEquipment(
                VALID_UUID, VALID_NAME, VALID_CATEGORY, VALID_CONDITION, null);
        Equipment expected = new Equipment(
                UUID.fromString(VALID_UUID), VALID_NAME, VALID_CATEGORY, Condition.GOOD, "");

        assertEquals(expected, adaptedEquipment.toModelType());
    }

    @Test
    public void toModelType_blankNotes_preservesNotes() throws Exception {
        JsonAdaptedEquipment adaptedEquipment = new JsonAdaptedEquipment(
                VALID_UUID, VALID_NAME, VALID_CATEGORY, "FAIR", "   ");
        Equipment expected = new Equipment(
                UUID.fromString(VALID_UUID), VALID_NAME, VALID_CATEGORY, Condition.FAIR, "   ");

        assertEquals(expected, adaptedEquipment.toModelType());
    }

    @Test
    public void toModelType_nullUuid_throwsIllegalValueException() {
        JsonAdaptedEquipment adaptedEquipment = new JsonAdaptedEquipment(
                null, VALID_NAME, VALID_CATEGORY, VALID_CONDITION, VALID_NOTES);
        assertThrows(IllegalValueException.class,
                String.format(MISSING_FIELD_MESSAGE_FORMAT, "uuid"), adaptedEquipment::toModelType);
    }

    @Test
    public void toModelType_malformedUuid_throwsIllegalValueException() {
        JsonAdaptedEquipment adaptedEquipment = new JsonAdaptedEquipment(
                "not-a-uuid", VALID_NAME, VALID_CATEGORY, VALID_CONDITION, VALID_NOTES);
        assertThrows(IllegalValueException.class, "Equipment uuid is invalid.", adaptedEquipment::toModelType);
    }

    @Test
    public void toModelType_nullName_throwsIllegalValueException() {
        JsonAdaptedEquipment adaptedEquipment = new JsonAdaptedEquipment(
                VALID_UUID, null, VALID_CATEGORY, VALID_CONDITION, VALID_NOTES);
        assertThrows(IllegalValueException.class,
                String.format(MISSING_FIELD_MESSAGE_FORMAT, "name"), adaptedEquipment::toModelType);
    }

    @Test
    public void toModelType_blankName_throwsIllegalValueException() {
        JsonAdaptedEquipment adaptedEquipment = new JsonAdaptedEquipment(
                VALID_UUID, "  ", VALID_CATEGORY, VALID_CONDITION, VALID_NOTES);
        assertThrows(IllegalValueException.class, "Equipment names should not be blank",
                adaptedEquipment::toModelType);
    }

    @Test
    public void toModelType_nullCategory_throwsIllegalValueException() {
        JsonAdaptedEquipment adaptedEquipment = new JsonAdaptedEquipment(
                VALID_UUID, VALID_NAME, null, VALID_CONDITION, VALID_NOTES);
        assertThrows(IllegalValueException.class,
                String.format(MISSING_FIELD_MESSAGE_FORMAT, "category"), adaptedEquipment::toModelType);
    }

    @Test
    public void toModelType_blankCategory_throwsIllegalValueException() {
        JsonAdaptedEquipment adaptedEquipment = new JsonAdaptedEquipment(
                VALID_UUID, VALID_NAME, " ", VALID_CONDITION, VALID_NOTES);
        assertThrows(IllegalValueException.class, "Equipment categories should not be blank",
                adaptedEquipment::toModelType);
    }

    @Test
    public void toModelType_nullCondition_throwsIllegalValueException() {
        JsonAdaptedEquipment adaptedEquipment = new JsonAdaptedEquipment(
                VALID_UUID, VALID_NAME, VALID_CATEGORY, null, VALID_NOTES);
        assertThrows(IllegalValueException.class,
                String.format(MISSING_FIELD_MESSAGE_FORMAT, "condition"), adaptedEquipment::toModelType);
    }

    @Test
    public void toModelType_invalidCondition_throwsIllegalValueException() {
        JsonAdaptedEquipment adaptedEquipment = new JsonAdaptedEquipment(
                VALID_UUID, VALID_NAME, VALID_CATEGORY, "good", VALID_NOTES);
        assertThrows(IllegalValueException.class, "Equipment condition is invalid.",
                adaptedEquipment::toModelType);
    }

    @Test
    public void toModelType_surroundingWhitespace_isNotTrimmed() throws Exception {
        JsonAdaptedEquipment adaptedEquipment = new JsonAdaptedEquipment(
                VALID_UUID, " Camera ", " Photography ", VALID_CONDITION, " note ");
        Equipment expected = new Equipment(UUID.fromString(VALID_UUID), " Camera ", " Photography ",
                Condition.GOOD, " note ");

        assertEquals(expected, adaptedEquipment.toModelType());
    }

    @Test
    public void toModelType_validEquipmentFile_returnsEquipment() throws Exception {
        JsonAdaptedEquipment adaptedEquipment = read("validEquipment.json");
        assertEquals(CAMERA, adaptedEquipment.toModelType());
    }

    @Test
    public void toModelType_missingNotesFile_becomesEmptyString() throws Exception {
        JsonAdaptedEquipment adaptedEquipment = read("missingNotesEquipment.json");
        Equipment expected = new Equipment(UUID.fromString(VALID_UUID), VALID_NAME, VALID_CATEGORY,
                Condition.FAIR, "");

        assertEquals(expected, adaptedEquipment.toModelType());
    }

    @Test
    public void toModelType_nullNotesFile_becomesEmptyString() throws Exception {
        JsonAdaptedEquipment adaptedEquipment = read("nullNotesEquipment.json");
        Equipment expected = new Equipment(UUID.fromString(VALID_UUID), VALID_NAME, VALID_CATEGORY,
                Condition.DAMAGED, "");

        assertEquals(expected, adaptedEquipment.toModelType());
    }

    @Test
    public void toModelType_fileWithAvailability_ignoresAvailability() throws Exception {
        JsonAdaptedEquipment adaptedEquipment = read("equipmentWithAvailability.json");
        Equipment expected = new Equipment(UUID.fromString(VALID_UUID), VALID_NAME, VALID_CATEGORY,
                Condition.UNDER_REPAIR, "Loose leg");

        assertEquals(expected, adaptedEquipment.toModelType());
    }

    @Test
    public void toModelType_malformedUuidFile_throwsIllegalValueException() throws Exception {
        JsonAdaptedEquipment adaptedEquipment = read("malformedUuidEquipment.json");
        assertThrows(IllegalValueException.class, "Equipment uuid is invalid.", adaptedEquipment::toModelType);
    }

    @Test
    public void toModelType_invalidConditionFile_throwsIllegalValueException() throws Exception {
        JsonAdaptedEquipment adaptedEquipment = read("invalidConditionEquipment.json");
        assertThrows(IllegalValueException.class, "Equipment condition is invalid.",
                adaptedEquipment::toModelType);
    }

    @Test
    public void toModelType_missingNameFile_throwsIllegalValueException() throws Exception {
        JsonAdaptedEquipment adaptedEquipment = read("missingNameEquipment.json");
        assertThrows(IllegalValueException.class,
                String.format(MISSING_FIELD_MESSAGE_FORMAT, "name"), adaptedEquipment::toModelType);
    }

    @Test
    public void toJson_doesNotPersistAvailability() throws Exception {
        String json = JsonUtil.toJsonString(new JsonAdaptedEquipment(CAMERA));

        assertFalse(json.contains("availability"));
        assertFalse(json.contains("AVAILABLE"));
        assertFalse(json.contains("ASSIGNED"));
        assertFalse(json.contains("UNAVAILABLE"));
    }

    @Test
    public void entityConstructor_nullEquipment_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new JsonAdaptedEquipment((Equipment) null));
    }

    private static JsonAdaptedEquipment equipmentWithCondition(String condition) {
        return new JsonAdaptedEquipment(VALID_UUID, VALID_NAME, VALID_CATEGORY, condition, "");
    }

    private static JsonAdaptedEquipment read(String fileName) throws Exception {
        return JsonUtil.readJsonFile(TEST_DATA_FOLDER.resolve(fileName), JsonAdaptedEquipment.class).get();
    }
}
