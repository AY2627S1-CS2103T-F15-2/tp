package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.Equipment;

/**
 * Jackson-friendly version of {@link Equipment}.
 * Availability is derived and is not stored.
 */
class JsonAdaptedEquipment {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Equipment's %s field is missing!";
    private static final String INVALID_IDENTIFIER_MESSAGE = "Equipment uuid is invalid.";
    private static final String INVALID_NAME_MESSAGE = "Equipment names should not be blank";
    private static final String INVALID_CATEGORY_MESSAGE = "Equipment categories should not be blank";
    private static final String INVALID_CONDITION_MESSAGE = "Equipment condition is invalid.";

    private final String uuid;
    private final String name;
    private final String category;
    private final String condition;
    private final String notes;

    /**
     * Constructs a {@code JsonAdaptedEquipment} with the given equipment details.
     */
    @JsonCreator
    public JsonAdaptedEquipment(@JsonProperty("uuid") String uuid,
            @JsonProperty("name") String name, @JsonProperty("category") String category,
            @JsonProperty("condition") String condition, @JsonProperty("notes") String notes) {
        this.uuid = uuid;
        this.name = name;
        this.category = category;
        this.condition = condition;
        this.notes = notes;
    }

    /**
     * Converts a given {@code Equipment} into this class for Jackson use.
     */
    public JsonAdaptedEquipment(Equipment source) {
        requireNonNull(source);
        uuid = source.getUuid().toString();
        name = source.getName();
        category = source.getCategory();
        condition = source.getCondition().name();
        notes = source.getNotes();
    }

    /**
     * Converts this Jackson-friendly adapted equipment object into the model's {@code Equipment} object.
     * A missing or null {@code notes} value becomes an empty string. Text is not trimmed.
     *
     * @throws IllegalValueException if any persisted field violates an equipment constraint.
     */
    public Equipment toModelType() throws IllegalValueException {
        if (uuid == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "uuid"));
        }
        final UUID modelUuid = parseUuid(uuid);

        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "name"));
        }
        if (name.isBlank()) {
            throw new IllegalValueException(INVALID_NAME_MESSAGE);
        }

        if (category == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "category"));
        }
        if (category.isBlank()) {
            throw new IllegalValueException(INVALID_CATEGORY_MESSAGE);
        }

        if (condition == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "condition"));
        }
        final Condition modelCondition = parseCondition(condition);
        final String modelNotes = notes == null ? "" : notes;
        return new Equipment(modelUuid, name, category, modelCondition, modelNotes);
    }

    private static UUID parseUuid(String rawUuid) throws IllegalValueException {
        try {
            return UUID.fromString(rawUuid);
        } catch (IllegalArgumentException exception) {
            throw new IllegalValueException(INVALID_IDENTIFIER_MESSAGE, exception);
        }
    }

    private static Condition parseCondition(String rawCondition) throws IllegalValueException {
        try {
            return Condition.valueOf(rawCondition);
        } catch (IllegalArgumentException exception) {
            throw new IllegalValueException(INVALID_CONDITION_MESSAGE, exception);
        }
    }
}
