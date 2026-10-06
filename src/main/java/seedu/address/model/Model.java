package seedu.address.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.equipment.Availability;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.loan.Loan;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.model.person.Person;

/**
 * The API of the Model component.
 */
public interface Model {
    /** {@code Predicate} that always evaluates to true */
    Predicate<Person> PREDICATE_SHOW_ALL_PERSONS = unused -> true;

    /**
     * Returns the user prefs.
     */
    ReadOnlyUserPrefs getUserPrefs();

    /**
     * Returns the user prefs' GUI settings.
     */
    GuiSettings getGuiSettings();

    /**
     * Sets the user prefs' GUI settings.
     */
    void setGuiSettings(GuiSettings guiSettings);

    /**
     * Replaces address book data with the data in {@code addressBook}.
     */
    void setAddressBook(ReadOnlyAddressBook addressBook);

    /** Returns the AddressBook */
    ReadOnlyAddressBook getAddressBook();

    /**
     * Returns true if a person with the same identity as {@code person} exists in the address book.
     */
    boolean hasPerson(Person person);

    /**
     * Deletes the given person.
     * The person must exist in the address book.
     */
    void deletePerson(Person target);

    /**
     * Adds the given person.
     * {@code person} must not already exist in the address book.
     */
    void addPerson(Person person);

    /**
     * Replaces the given person {@code target} with {@code editedPerson}.
     * {@code target} must exist in the address book.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the address book.
     */
    void setPerson(Person target, Person editedPerson);

    /** Returns an unmodifiable view of the filtered person list */
    ObservableList<Person> getFilteredPersonList();

    /**
     * Updates the filter of the filtered person list to filter by the given {@code predicate}.
     * @throws NullPointerException if {@code predicate} is null.
     */
    void updateFilteredPersonList(Predicate<Person> predicate);

    default boolean hasMember(NusId nusId) {
        throw new UnsupportedOperationException("Member model operations are not implemented");
    }

    default Optional<Member> findMember(NusId nusId) {
        throw new UnsupportedOperationException("Member model operations are not implemented");
    }

    default void addMember(Member member) {
        throw new UnsupportedOperationException("Member model operations are not implemented");
    }

    default void deleteMember(Member member) {
        throw new UnsupportedOperationException("Member model operations are not implemented");
    }

    default ObservableList<Member> getMemberList() {
        throw new UnsupportedOperationException("Member model operations are not implemented");
    }

    default boolean hasEquipment(UUID uuid) {
        throw new UnsupportedOperationException("Equipment model operations are not implemented");
    }

    default Optional<Equipment> findEquipment(UUID uuid) {
        throw new UnsupportedOperationException("Equipment model operations are not implemented");
    }

    default void addEquipment(Equipment equipment) {
        throw new UnsupportedOperationException("Equipment model operations are not implemented");
    }

    default void deleteEquipment(Equipment equipment) {
        throw new UnsupportedOperationException("Equipment model operations are not implemented");
    }

    default ObservableList<Equipment> getEquipmentList() {
        throw new UnsupportedOperationException("Equipment model operations are not implemented");
    }

    default void addLoan(Loan loan) {
        throw new UnsupportedOperationException("Loan model operations are not implemented");
    }

    default Optional<Loan> findOpenLoan(UUID equipmentUuid) {
        throw new UnsupportedOperationException("Loan model operations are not implemented");
    }

    default List<Loan> findOpenLoansForMember(NusId nusId) {
        throw new UnsupportedOperationException("Loan model operations are not implemented");
    }

    default void closeLoan(UUID equipmentUuid, LocalDate returnedDate) {
        throw new UnsupportedOperationException("Loan model operations are not implemented");
    }

    default ObservableList<Loan> getLoanList() {
        throw new UnsupportedOperationException("Loan model operations are not implemented");
    }

    default Availability getAvailability(UUID uuid) {
        throw new UnsupportedOperationException("Availability calculation is not implemented");
    }
}
