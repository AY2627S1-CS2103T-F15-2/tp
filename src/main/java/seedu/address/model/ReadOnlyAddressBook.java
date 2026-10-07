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

    /**
     * Returns an unmodifiable observable view with unique member NUS IDs.
     */
    ObservableList<Member> getMemberList();

    /**
     * Returns an unmodifiable observable view with unique equipment UUIDs.
     */
    ObservableList<Equipment> getEquipmentList();

    /**
     * Returns an unmodifiable observable view of open loans and closed history.
     */
    ObservableList<Loan> getLoanList();

}
