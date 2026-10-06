package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javafx.collections.ObservableList;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.loan.Loan;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.model.person.Person;
import seedu.address.model.person.UniquePersonList;

/**
 * Wraps all data at the address-book level.
 * Duplicates are not allowed (by .isSamePerson comparison).
 */
public class AddressBook implements ReadOnlyAddressBook {

    private final UniquePersonList persons = new UniquePersonList();

    public AddressBook() {}

    /**
     * Creates an AddressBook using the Persons in the {@code toBeCopied}
     */
    public AddressBook(ReadOnlyAddressBook toBeCopied) {
        this();
        resetData(toBeCopied);
    }

    //// list overwrite operations

    /**
     * Replaces the contents of the person list with {@code persons}.
     * {@code persons} must not contain duplicate persons.
     */
    public void setPersons(List<Person> persons) {
        this.persons.setPersons(persons);
    }

    /**
     * Resets the existing data of this {@code AddressBook} with {@code newData}.
     */
    public void resetData(ReadOnlyAddressBook newData) {
        requireNonNull(newData);

        setPersons(newData.getPersonList());
    }

    //// person-level operations

    /**
     * Returns true if a person with the same identity as {@code person} exists in the address book.
     */
    public boolean hasPerson(Person person) {
        requireNonNull(person);
        return persons.contains(person);
    }

    /**
     * Adds a person to the address book.
     * The person must not already exist in the address book.
     */
    public void addPerson(Person p) {
        persons.add(p);
    }

    /**
     * Replaces the given person {@code target} in the list with {@code editedPerson}.
     * {@code target} must exist in the address book.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the address book.
     */
    public void setPerson(Person target, Person editedPerson) {
        requireNonNull(editedPerson);

        persons.setPerson(target, editedPerson);
    }

    /**
     * Removes {@code key} from this {@code AddressBook}.
     * {@code key} must exist in the address book.
     */
    public void removePerson(Person key) {
        persons.remove(key);
    }

    //// Laplace scaffold operations

    public boolean hasMember(NusId nusId) {
        throw new UnsupportedOperationException("Member storage is not implemented");
    }

    public Optional<Member> findMember(NusId nusId) {
        throw new UnsupportedOperationException("Member storage is not implemented");
    }

    public void addMember(Member member) {
        throw new UnsupportedOperationException("Member storage is not implemented");
    }

    public void removeMember(Member member) {
        throw new UnsupportedOperationException("Member storage is not implemented");
    }

    public boolean hasEquipment(UUID uuid) {
        throw new UnsupportedOperationException("Equipment storage is not implemented");
    }

    public Optional<Equipment> findEquipment(UUID uuid) {
        throw new UnsupportedOperationException("Equipment storage is not implemented");
    }

    public void addEquipment(Equipment equipment) {
        throw new UnsupportedOperationException("Equipment storage is not implemented");
    }

    public void removeEquipment(Equipment equipment) {
        throw new UnsupportedOperationException("Equipment storage is not implemented");
    }

    public void addLoan(Loan loan) {
        throw new UnsupportedOperationException("Loan storage is not implemented");
    }

    public Optional<Loan> findOpenLoan(UUID equipmentUuid) {
        throw new UnsupportedOperationException("Loan storage is not implemented");
    }

    public List<Loan> findOpenLoansForMember(NusId nusId) {
        throw new UnsupportedOperationException("Loan storage is not implemented");
    }

    public void closeLoan(UUID equipmentUuid, LocalDate returnedDate) {
        throw new UnsupportedOperationException("Loan storage is not implemented");
    }

    @Override
    public ObservableList<Member> getMemberList() {
        throw new UnsupportedOperationException("Member storage is not implemented");
    }

    @Override
    public ObservableList<Equipment> getEquipmentList() {
        throw new UnsupportedOperationException("Equipment storage is not implemented");
    }

    @Override
    public ObservableList<Loan> getLoanList() {
        throw new UnsupportedOperationException("Loan storage is not implemented");
    }

    //// util methods

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("persons", persons)
                .toString();
    }

    @Override
    public ObservableList<Person> getPersonList() {
        return persons.asUnmodifiableObservableList();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddressBook otherAddressBook)) {
            return false;
        }

        return persons.equals(otherAddressBook.persons);
    }

    @Override
    public int hashCode() {
        return persons.hashCode();
    }
}
