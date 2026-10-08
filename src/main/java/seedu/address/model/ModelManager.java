package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.equipment.Availability;
import seedu.address.model.equipment.AvailabilityCalculator;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.equipment.exceptions.EquipmentNotFoundException;
import seedu.address.model.loan.Loan;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.model.person.Person;

/**
 * Represents the in-memory model of the address book data.
 */
public class ModelManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final AddressBook addressBook;
    private final UserPrefs userPrefs;
    private final FilteredList<Person> filteredPersons;

    /**
     * Initializes a ModelManager with the given addressBook and userPrefs.
     */
    public ModelManager(ReadOnlyAddressBook addressBook, ReadOnlyUserPrefs userPrefs) {
        this(userPrefs, new AddressBook(requireNonNull(addressBook)));
        logger.fine("Initializing with address book: " + addressBook + " and user prefs " + userPrefs);
    }

    /**
     * Uses the supplied root directly, allowing model tests to control the root-model boundary.
     * The public constructor continues to copy caller-owned address book data.
     */
    ModelManager(ReadOnlyUserPrefs userPrefs, AddressBook addressBook) {
        requireAllNonNull(addressBook, userPrefs);
        this.addressBook = addressBook;
        this.userPrefs = new UserPrefs(userPrefs);
        filteredPersons = new FilteredList<>(this.addressBook.getPersonList());
    }

    public ModelManager() {
        this(new AddressBook(), new UserPrefs());
    }

    //=========== UserPrefs ==================================================================================

    @Override
    public ReadOnlyUserPrefs getUserPrefs() {
        return userPrefs;
    }

    @Override
    public GuiSettings getGuiSettings() {
        return userPrefs.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        requireNonNull(guiSettings);
        userPrefs.setGuiSettings(guiSettings);
    }

    //=========== AddressBook ================================================================================

    @Override
    public void setAddressBook(ReadOnlyAddressBook addressBook) {
        this.addressBook.resetData(addressBook);
    }

    @Override
    public ReadOnlyAddressBook getAddressBook() {
        return addressBook;
    }

    @Override
    public boolean hasPerson(Person person) {
        requireNonNull(person);
        return addressBook.hasPerson(person);
    }

    @Override
    public void deletePerson(Person target) {
        addressBook.removePerson(target);
    }

    @Override
    public void addPerson(Person person) {
        addressBook.addPerson(person);
        updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
    }

    @Override
    public void setPerson(Person target, Person editedPerson) {
        requireAllNonNull(target, editedPerson);

        addressBook.setPerson(target, editedPerson);
    }

    //=========== Members, Equipment and Loans ===============================================================

    @Override
    public boolean hasMember(NusId nusId) {
        return addressBook.hasMember(nusId);
    }

    @Override
    public Optional<Member> findMember(NusId nusId) {
        return addressBook.findMember(nusId);
    }

    @Override
    public void addMember(Member member) {
        addressBook.addMember(member);
    }

    @Override
    public void deleteMember(Member member) {
        addressBook.removeMember(member);
    }

    @Override
    public ObservableList<Member> getMemberList() {
        return addressBook.getMemberList();
    }

    @Override
    public boolean hasEquipment(UUID uuid) {
        return addressBook.hasEquipment(uuid);
    }

    @Override
    public Optional<Equipment> findEquipment(UUID uuid) {
        return addressBook.findEquipment(uuid);
    }

    @Override
    public void addEquipment(Equipment equipment) {
        addressBook.addEquipment(equipment);
    }

    @Override
    public void deleteEquipment(Equipment equipment) {
        addressBook.removeEquipment(equipment);
    }

    @Override
    public ObservableList<Equipment> getEquipmentList() {
        return addressBook.getEquipmentList();
    }

    @Override
    public void addLoan(Loan loan) {
        addressBook.addLoan(loan);
    }

    @Override
    public Optional<Loan> findOpenLoan(UUID equipmentUuid) {
        return addressBook.findOpenLoan(equipmentUuid);
    }

    @Override
    public List<Loan> findOpenLoansForMember(NusId nusId) {
        return addressBook.findOpenLoansForMember(nusId);
    }

    @Override
    public void closeLoan(UUID equipmentUuid, LocalDate returnedDate) {
        addressBook.closeLoan(equipmentUuid, returnedDate);
    }

    @Override
    public ObservableList<Loan> getLoanList() {
        return addressBook.getLoanList();
    }

    @Override
    public Availability getAvailability(UUID uuid) {
        requireNonNull(uuid);
        Equipment equipment = addressBook.findEquipment(uuid).orElseThrow(EquipmentNotFoundException::new);
        boolean hasOpenLoan = addressBook.findOpenLoan(uuid).isPresent();
        return AvailabilityCalculator.calculate(equipment.getCondition(), hasOpenLoan);
    }

    //=========== Filtered Person List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code Person} backed by the internal list of
     * {@code addressBook}
     */
    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return filteredPersons;
    }

    @Override
    public void updateFilteredPersonList(Predicate<Person> predicate) {
        requireNonNull(predicate);
        filteredPersons.setPredicate(predicate);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ModelManager otherModelManager)) {
            return false;
        }

        return addressBook.equals(otherModelManager.addressBook)
                && userPrefs.equals(otherModelManager.userPrefs)
                && filteredPersons.equals(otherModelManager.filteredPersons);
    }

}
