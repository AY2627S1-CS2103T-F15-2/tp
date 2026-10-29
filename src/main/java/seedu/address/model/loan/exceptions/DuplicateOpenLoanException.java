package seedu.address.model.loan.exceptions;

/**
 * Signals that equipment already has an open loan.
 */
public class DuplicateOpenLoanException extends RuntimeException {
    /**
     * Creates a failure for an equipment unit that already has an open loan.
     */
    public DuplicateOpenLoanException() {
        super("Equipment already has an open loan");
    }
}
