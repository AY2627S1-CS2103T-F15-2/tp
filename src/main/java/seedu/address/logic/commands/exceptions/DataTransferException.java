package seedu.address.logic.commands.exceptions;

/** A transfer failure with explicit presentation state; callers need not interpret its message. */
public class DataTransferException extends CommandException {
    private final boolean isPreviewInvalidated;

    /** Preserves the underlying failure and states whether an older preview has been discarded. */
    public DataTransferException(String message, Throwable cause, boolean isPreviewInvalidated) {
        super(message, cause);
        this.isPreviewInvalidated = isPreviewInvalidated;
    }

    public boolean isPreviewInvalidated() {
        return isPreviewInvalidated;
    }
}
