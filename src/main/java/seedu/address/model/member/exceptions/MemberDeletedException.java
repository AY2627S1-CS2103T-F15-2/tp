package seedu.address.model.member.exceptions;

/**
 * Signals an operation that requires an active member but targets a retained deleted record.
 */
public class MemberDeletedException extends IllegalStateException {
    /**
     * Creates the failure with recovery guidance.
     */
    public MemberDeletedException() {
        super("Member is deleted. Restore the member first.");
    }
}
