package seedu.address.model.equipment.exceptions;

/**
 * Indicates that an operation requires active equipment but found a deleted unit.
 */
public class EquipmentDeletedException extends IllegalStateException {
    /**
     * Creates a failure for an operation on a retained deleted unit.
     */
    public EquipmentDeletedException() {
        super("Equipment is deleted.");
    }
}
