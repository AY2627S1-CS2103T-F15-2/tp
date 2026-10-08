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
 * Member, equipment and loan operations reject null arguments and propagate root-model failures.
 * Collection and cross-collection validation belong to the address book.
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

    /**
     * Returns whether a member with the normalized NUS ID exists.
     */
    boolean hasMember(NusId nusId);

    /**
     * Returns the member with this NUS ID, or an empty Optional if unknown.
     */
    Optional<Member> findMember(NusId nusId);

    /**
     * Adds a member. A duplicate identity throws DuplicateMemberException.
     */
    void addMember(Member member);

    /**
     * Deletes a member by identity and removes their closed loan history.
     * @throws seedu.address.model.member.exceptions.MemberNotFoundException if the identity is unknown.
     * @throws IllegalStateException if the member has an open loan.
     */
    void deleteMember(Member member);

    /**
     * Returns an unmodifiable live view of all members.
     */
    ObservableList<Member> getMemberList();

    /**
     * Returns whether equipment with this UUID exists.
     */
    boolean hasEquipment(UUID uuid);

    /**
     * Returns the equipment with this UUID, or an empty Optional if unknown.
     */
    Optional<Equipment> findEquipment(UUID uuid);

    /**
     * Adds equipment. A duplicate UUID throws DuplicateEquipmentException.
     */
    void addEquipment(Equipment equipment);

    /**
     * Deletes equipment by UUID and removes its closed loan history.
     * @throws seedu.address.model.equipment.exceptions.EquipmentNotFoundException if the UUID is unknown.
     * @throws IllegalStateException if the equipment has an open loan.
     */
    void deleteEquipment(Equipment equipment);

    /**
     * Returns an unmodifiable live view of all equipment.
     */
    ObservableList<Equipment> getEquipmentList();

    /**
     * Adds a loan referencing an existing member and equipment item.
     * Unknown identities throw MemberNotFoundException or EquipmentNotFoundException.
     * A second open loan for the same equipment throws DuplicateOpenLoanException.
     */
    void addLoan(Loan loan);

    /**
     * Returns the open loan for this equipment UUID, or an empty Optional if none exists.
     */
    Optional<Loan> findOpenLoan(UUID equipmentUuid);

    /**
     * Returns an unmodifiable snapshot of the member's open loans, empty for an unknown member.
     */
    List<Loan> findOpenLoansForMember(NusId nusId);

    /**
     * Replaces the equipment's open loan with a closed copy using the supplied return date.
     * Same-day, early and late returns are valid; Loan validates the date against its assigned date.
     * @throws seedu.address.model.loan.exceptions.OpenLoanNotFoundException if no open loan exists.
     * @throws IllegalArgumentException if the returned date precedes the assigned date.
     */
    void closeLoan(UUID equipmentUuid, LocalDate returnedDate);

    /**
     * Returns an unmodifiable live view of all loans, including closed history.
     */
    ObservableList<Loan> getLoanList();

    /**
     * Derives availability from equipment condition and whether it has an open loan.
     * @throws seedu.address.model.equipment.exceptions.EquipmentNotFoundException if the UUID is unknown.
     */
    Availability getAvailability(UUID uuid);
}
