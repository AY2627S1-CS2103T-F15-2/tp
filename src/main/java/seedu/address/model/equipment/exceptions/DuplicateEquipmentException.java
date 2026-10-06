package seedu.address.model.equipment.exceptions;

/** Signals that an operation would create two equipment items with the same UUID. */
public class DuplicateEquipmentException extends RuntimeException {
    public DuplicateEquipmentException() {
        super("Operation would result in duplicate equipment");
    }
}
