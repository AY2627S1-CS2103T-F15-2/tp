package seedu.address.model.equipment.exceptions;

/**
 * Signals that an operation would create two equipment items with the same UUID.
 */
public class DuplicateEquipmentException extends RuntimeException {
    /**
     * Creates a failure for an equipment UUID already reserved by another unit.
     */
    public DuplicateEquipmentException() {
        super("Operation would result in duplicate equipment");
    }
}
