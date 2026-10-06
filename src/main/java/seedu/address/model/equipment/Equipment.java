package seedu.address.model.equipment;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;
import java.util.UUID;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Represents a physical equipment item in Laplace.
 * Guarantees: immutable; fields are present and non-null.
 */
public class Equipment {

    private final UUID uuid;
    private final String name;
    private final String category;
    private final Condition condition;
    private final String notes;

    /**
     * Constructs an {@code Equipment} item with the given details.
     */
    public Equipment(UUID uuid, String name, String category, Condition condition, String notes) {
        requireAllNonNull(uuid, name, category, condition, notes);
        this.uuid = uuid;
        this.name = name;
        this.category = category;
        this.condition = condition;
        this.notes = notes;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public Condition getCondition() {
        return condition;
    }

    public String getNotes() {
        return notes;
    }

    /**
     * Returns true if both equipment objects represent the same physical item.
     */
    public boolean isSameEquipment(Equipment otherEquipment) {
        if (otherEquipment == this) {
            return true;
        }
        return otherEquipment != null && otherEquipment.getUuid().equals(getUuid());
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Equipment otherEquipment)) {
            return false;
        }
        return uuid.equals(otherEquipment.uuid)
                && name.equals(otherEquipment.name)
                && category.equals(otherEquipment.category)
                && condition == otherEquipment.condition
                && notes.equals(otherEquipment.notes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, name, category, condition, notes);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("uuid", uuid)
                .add("name", name)
                .add("category", category)
                .add("condition", condition)
                .add("notes", notes)
                .toString();
    }
}
