package seedu.address.model.member.exceptions;

/** Signals that an operation would create two members with the same NUS ID. */
public class DuplicateMemberException extends RuntimeException {
    public DuplicateMemberException() {
        super("Operation would result in duplicate members");
    }
}
