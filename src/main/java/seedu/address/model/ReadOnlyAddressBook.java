package seedu.address.model;

import javafx.collections.ObservableList;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.loan.Loan;
import seedu.address.model.member.Member;
import seedu.address.model.person.Person;

/**
 * Unmodifiable view of an address book
 */
public interface ReadOnlyAddressBook {

    /**
     * Returns an unmodifiable view of the persons list.
     * This list will not contain any duplicate persons.
     */
    ObservableList<Person> getPersonList();

    /** Returns an unmodifiable view of the Laplace member list. */
    default ObservableList<Member> getMemberList() {
        throw new UnsupportedOperationException("Member storage is not implemented");
    }

    /** Returns an unmodifiable view of the equipment list. */
    default ObservableList<Equipment> getEquipmentList() {
        throw new UnsupportedOperationException("Equipment storage is not implemented");
    }

    /** Returns an unmodifiable view of the loan list. */
    default ObservableList<Loan> getLoanList() {
        throw new UnsupportedOperationException("Loan storage is not implemented");
    }

}
