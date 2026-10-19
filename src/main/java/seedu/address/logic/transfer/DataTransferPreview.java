package seedu.address.logic.transfer;

import java.nio.file.Path;

import seedu.address.model.ReadOnlyAddressBook;

/** Read-only presentation of a validated replacement; it exposes no mutable candidate data. */
public record DataTransferPreview(Path source, int persons, int members, int equipment, int loans) {
    /** Creates counts from the isolated snapshot that confirmation will apply. */
    public static DataTransferPreview from(Path source, ReadOnlyAddressBook data) {
        return new DataTransferPreview(source, data.getPersonList().size(), data.getMemberList().size(),
                data.getEquipmentList().size(), data.getLoanList().size());
    }

    /** Returns the replacement warning used by both the command and graphical preview. */
    public String summary() {
        return "Replace all data from " + source + "\nPersons: " + persons + "; members: " + members
                + "; equipment: " + equipment + "; loans (including history): " + loans
                + ".\nAll current records will be replaced. A backup is required before applying."
                + "\nUse data confirm to apply or data cancel to discard.";
    }
}
