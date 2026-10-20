package seedu.address.logic.commands;

import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;

/** Successful persisted replacement and its detached snapshots, ready for the common history boundary. */
public class CommittedReplacementResult extends CommandResult {
    private final AddressBook before;
    private final AddressBook after;

    /** Creates a commit signal only after the replacement has been saved and published successfully. */
    public CommittedReplacementResult(String feedback, ReadOnlyAddressBook before, ReadOnlyAddressBook after) {
        super(feedback, DisplayRequest.members());
        this.before = new AddressBook(before);
        this.after = new AddressBook(after);
    }

    /** Returns a defensive copy of the complete pre-replacement state. */
    public ReadOnlyAddressBook getBefore() {
        return new AddressBook(before);
    }

    /** Returns a defensive copy of the complete persisted replacement state. */
    public ReadOnlyAddressBook getAfter() {
        return new AddressBook(after);
    }
}
