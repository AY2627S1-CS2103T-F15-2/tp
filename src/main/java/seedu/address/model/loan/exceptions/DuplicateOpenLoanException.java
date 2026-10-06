package seedu.address.model.loan.exceptions;

/** Signals that equipment already has an open loan. */
public class DuplicateOpenLoanException extends RuntimeException {
    public DuplicateOpenLoanException() {
        super("Equipment already has an open loan");
    }
}
