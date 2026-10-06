package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.equipment.Equipment;

/** Jackson-facing contract for an {@link Equipment}. */
class JsonAdaptedEquipment {

    private final String uuid;
    private final String name;
    private final String category;
    private final String condition;
    private final String notes;

    /** Constructs an adapter from raw JSON fields. */
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

    /** Constructs an adapter from domain equipment. */
    public JsonAdaptedEquipment(Equipment source) {
        uuid = source.getUuid().toString();
        name = source.getName();
        category = source.getCategory();
        condition = source.getCondition().name();
        notes = source.getNotes();
    }

    /** Converts this adapter to its domain type. */
    public Equipment toModelType() throws IllegalValueException {
        throw new UnsupportedOperationException("Equipment JSON conversion is not implemented");
    }
}
