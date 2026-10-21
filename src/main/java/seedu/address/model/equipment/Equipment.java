package seedu.address.model.equipment;

import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;
import java.util.UUID;

import seedu.address.commons.util.LaplaceText;
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
    private final boolean deleted;

    /**
     * Constructs an {@code Equipment} item with the given details.
     */
    public Equipment(UUID uuid, String name, String category, Condition condition, String notes) {
        this(uuid, name, category, condition, notes, false);
    }

    /**
     * Constructs a retained physical unit with an explicit lifecycle state.
     */
    public Equipment(UUID uuid, String name, String category, Condition condition, String notes, boolean deleted) {
        requireAllNonNull(uuid, name, category, condition, notes);
        this.uuid = uuid;
        this.name = name;
        this.category = category;
        this.condition = condition;
        this.notes = notes;
        this.deleted = deleted;
    }

    /** Validates and normalizes a newly entered equipment name; stored legacy descriptions remain readable. */
    public static String normalizeName(String value) {
        return normalizeText(value, 1, 100, "Invalid equipment name. Enter 1-100 printable characters.");
    }

    /** Validates the MVP category length and printable-text contract. */
    public static String normalizeCategory(String value) {
        return normalizeText(value, 1, 50, "Invalid category. Enter 1-50 printable characters.");
    }

    /** Converts whitespace/line breaks to spaces; omitted or blank notes remain empty. */
    public static String normalizeNotes(String value) {
        return normalizeText(value, 0, 300, "Invalid notes. Use at most 300 printable characters.");
    }

    private static String normalizeText(String value, int minimum, int maximum, String message) {
        String normalized = LaplaceText.normalize(value);
        checkArgument(LaplaceText.isPrintable(normalized, minimum, maximum), message);
        return normalized;
    }

    public boolean isDeleted() {
        return deleted;
    }

    /**
     * Returns a copy with the requested lifecycle state and the same identity and details.
     */
    public Equipment withDeleted(boolean deleted) {
        return new Equipment(uuid, name, category, condition, notes, deleted);
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
                && notes.equals(otherEquipment.notes)
                && deleted == otherEquipment.deleted;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, name, category, condition, notes, deleted);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("uuid", uuid)
                .add("name", name)
                .add("category", category)
                .add("condition", condition)
                .add("notes", notes)
                .add("deleted", deleted)
                .toString();
    }
}
