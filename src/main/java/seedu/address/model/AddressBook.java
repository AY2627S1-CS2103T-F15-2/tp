package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.equipment.UniqueEquipmentList;
import seedu.address.model.equipment.exceptions.EquipmentNotFoundException;
import seedu.address.model.loan.Loan;
import seedu.address.model.loan.UniqueLoanList;
import seedu.address.model.loan.exceptions.OpenLoanNotFoundException;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.model.member.UniqueMemberList;
import seedu.address.model.member.exceptions.MemberNotFoundException;
import seedu.address.model.person.Person;
import seedu.address.model.person.UniquePersonList;

/**
 * Wraps all data at the address-book level.
 * Collections enforce identity uniqueness; this root enforces references between collections.
 * New collection containers are initialized on demand so person-only paths remain independent of the scaffold.
 */
public class AddressBook implements ReadOnlyAddressBook {

    private final UniquePersonList persons = new UniquePersonList();

    // Null storage represents a known empty collection, without invoking unfinished scaffold methods.
    private UniqueMemberList members;
    private UniqueEquipmentList equipment;
    private UniqueLoanList loans;

    // Properties forward changes from the current storage without copying domain objects or live lists.
    private final ListProperty<Member> memberView = new SimpleListProperty<>(FXCollections.emptyObservableList());
    private final ListProperty<Equipment> equipmentView = new SimpleListProperty<>(FXCollections.emptyObservableList());
    private final ListProperty<Loan> loanView = new SimpleListProperty<>(FXCollections.emptyObservableList());
    private final ObservableList<Member> unmodifiableMembers = FXCollections.unmodifiableObservableList(memberView);
    private final ObservableList<Equipment> unmodifiableEquipment =
            FXCollections.unmodifiableObservableList(equipmentView);
    private final ObservableList<Loan> unmodifiableLoans = FXCollections.unmodifiableObservableList(loanView);

    // Factories also supply fresh containers for validating replacement state without changing live lists.
    private final Supplier<UniqueMemberList> memberFactory;
    private final Supplier<UniqueEquipmentList> equipmentFactory;
    private final Supplier<UniqueLoanList> loanFactory;

    /**
     * Creates an empty address book using the production collections.
     */
    public AddressBook() {
        this(UniqueMemberList::new, UniqueEquipmentList::new, UniqueLoanList::new);
    }

    /**
     * Supplies fresh, empty collection containers, including controlled dependencies in root tests.
     * Each factory must return a new independent container on every invocation.
     */
    AddressBook(Supplier<UniqueMemberList> memberFactory, Supplier<UniqueEquipmentList> equipmentFactory,
            Supplier<UniqueLoanList> loanFactory) {
        this.memberFactory = requireNonNull(memberFactory);
        this.equipmentFactory = requireNonNull(equipmentFactory);
        this.loanFactory = requireNonNull(loanFactory);
    }

    /**
     * Creates an independent collection container for every list in {@code toBeCopied}.
     */
    public AddressBook(ReadOnlyAddressBook toBeCopied) {
        this();
        resetData(toBeCopied);
    }

    // list overwrite operations

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

        // Snapshot first, including self-reset: caller-owned lists cannot become our storage.
        List<Person> replacementPersons = List.copyOf(newData.getPersonList());
        List<Member> replacementMembers = List.copyOf(newData.getMemberList());
        List<Equipment> replacementEquipment = List.copyOf(newData.getEquipmentList());
        List<Loan> replacementLoans = List.copyOf(newData.getLoanList());

        UniquePersonList validatedPersons = new UniquePersonList();
        validatedPersons.setPersons(replacementPersons);
        UniqueMemberList validatedMembers = null;
        UniqueEquipmentList validatedEquipment = null;
        UniqueLoanList validatedLoans = null;
        if (!replacementMembers.isEmpty()) {
            validatedMembers = requireNonNull(memberFactory.get());
            validatedMembers.setMembers(replacementMembers);
        }
        if (!replacementEquipment.isEmpty()) {
            validatedEquipment = requireNonNull(equipmentFactory.get());
            validatedEquipment.setEquipment(replacementEquipment);
        }
        if (!replacementLoans.isEmpty()) {
            validatedLoans = requireNonNull(loanFactory.get());
            validatedLoans.setLoans(replacementLoans);
        }
        for (Loan loan : replacementLoans) {
            requireLoanReferences(loan, replacementMembers, replacementEquipment);
        }

        // Resolve every replacement view before committing, so unsupported dependencies cannot cause a partial reset.
        ObservableList<Member> replacementMemberView = validatedMembers == null
                ? FXCollections.emptyObservableList() : validatedMembers.asUnmodifiableObservableList();
        ObservableList<Equipment> replacementEquipmentView = validatedEquipment == null
                ? FXCollections.emptyObservableList() : validatedEquipment.asUnmodifiableObservableList();
        ObservableList<Loan> replacementLoanView = validatedLoans == null
                ? FXCollections.emptyObservableList() : validatedLoans.asUnmodifiableObservableList();

        members = validatedMembers;
        equipment = validatedEquipment;
        loans = validatedLoans;
        persons.setPersons(validatedPersons);
        memberView.set(replacementMemberView);
        equipmentView.set(replacementEquipmentView);
        loanView.set(replacementLoanView);
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

    //// member, equipment, and loan operations

    /**
     * Returns whether the normalized member identity exists.
     */
    public boolean hasMember(NusId nusId) {
        requireNonNull(nusId);
        return members != null && members.contains(nusId);
    }

    /**
     * Finds a member by identity, regardless of other member details.
     */
    public Optional<Member> findMember(NusId nusId) {
        requireNonNull(nusId);
        return members == null ? Optional.empty() : members.findByNusId(nusId);
    }

    /**
     * Adds a member, delegating identity uniqueness to the collection.
     */
    public void addMember(Member member) {
        requireNonNull(member);
        getMemberStorage().add(member);
    }

    /**
     * Removes a member by identity and its closed history, as required by the scaffold contract.
     * An open loan blocks deletion and leaves every collection unchanged.
     *
     * @throws IllegalStateException if this member has an open loan.
     */
    public void removeMember(Member member) {
        requireNonNull(member);
        NusId nusId = member.getNusId();
        if (!hasMember(nusId)) {
            throw new MemberNotFoundException();
        }
        if (!findOpenLoansForMember(nusId).isEmpty()) {
            throw new IllegalStateException("Cannot delete a member with an open loan");
        }
        List<Loan> retainedLoans = getLoanList().stream()
                .filter(loan -> !loan.getMemberNusId().equals(nusId)).toList();
        if (loans != null) {
            UniqueLoanList validatedLoans = requireNonNull(loanFactory.get());
            validatedLoans.setLoans(retainedLoans);
        }
        members.remove(member);
        if (loans != null) {
            loans.setLoans(retainedLoans);
        }
    }

    /**
     * Returns whether the equipment identity exists.
     */
    public boolean hasEquipment(UUID uuid) {
        requireNonNull(uuid);
        return equipment != null && equipment.contains(uuid);
    }

    /**
     * Finds equipment by UUID, regardless of other equipment details.
     */
    public Optional<Equipment> findEquipment(UUID uuid) {
        requireNonNull(uuid);
        return equipment == null ? Optional.empty() : equipment.findByUuid(uuid);
    }

    /**
     * Adds equipment, delegating identity uniqueness to the collection.
     */
    public void addEquipment(Equipment item) {
        requireNonNull(item);
        getEquipmentStorage().add(item);
    }

    /**
     * Removes equipment by identity and its closed history, as required by the scaffold contract.
     * An open loan blocks deletion and leaves every collection unchanged.
     *
     * @throws IllegalStateException if this equipment has an open loan.
     */
    public void removeEquipment(Equipment item) {
        requireNonNull(item);
        UUID uuid = item.getUuid();
        if (!hasEquipment(uuid)) {
            throw new EquipmentNotFoundException();
        }
        if (findOpenLoan(uuid).isPresent()) {
            throw new IllegalStateException("Cannot delete equipment with an open loan");
        }
        List<Loan> retainedLoans = getLoanList().stream()
                .filter(loan -> !loan.getEquipmentUuid().equals(uuid)).toList();
        if (loans != null) {
            UniqueLoanList validatedLoans = requireNonNull(loanFactory.get());
            validatedLoans.setLoans(retainedLoans);
        }
        equipment.remove(item);
        if (loans != null) {
            loans.setLoans(retainedLoans);
        }
    }

    /**
     * Adds a loan only after resolving both references; the collection owns open-loan uniqueness.
     */
    public void addLoan(Loan loan) {
        requireNonNull(loan);
        requireLoanReferences(loan, getMemberList(), getEquipmentList());
        getLoanStorage().add(loan);
    }

    /**
     * Finds the open loan for an equipment identity.
     */
    public Optional<Loan> findOpenLoan(UUID equipmentUuid) {
        requireNonNull(equipmentUuid);
        return loans == null ? Optional.empty() : loans.findOpenLoan(equipmentUuid);
    }

    /**
     * Returns an unmodifiable snapshot of a member's open loans.
     */
    public List<Loan> findOpenLoansForMember(NusId nusId) {
        requireNonNull(nusId);
        return loans == null ? List.of() : List.copyOf(loans.findOpenLoansForMember(nusId));
    }

    /**
     * Closes the open loan by delegating immutable replacement to the collection.
     */
    public void closeLoan(UUID equipmentUuid, LocalDate returnedDate) {
        requireNonNull(equipmentUuid);
        requireNonNull(returnedDate);
        if (loans == null) {
            throw new OpenLoanNotFoundException();
        }
        loans.closeLoan(equipmentUuid, returnedDate);
    }

    /**
     * Checks references by identity for both open and closed loans.
     */
    private static void requireLoanReferences(Loan loan, List<Member> members, List<Equipment> equipment) {
        if (members.stream().noneMatch(member -> member.getNusId().equals(loan.getMemberNusId()))) {
            throw new MemberNotFoundException();
        }
        if (equipment.stream().noneMatch(item -> item.getUuid().equals(loan.getEquipmentUuid()))) {
            throw new EquipmentNotFoundException();
        }
    }

    /**
     * Initializes member storage only when needed. An unsupported view fails before publishing the container.
     */
    private UniqueMemberList getMemberStorage() {
        if (members == null) {
            UniqueMemberList candidate = requireNonNull(memberFactory.get());
            ObservableList<Member> view = candidate.asUnmodifiableObservableList();
            members = candidate;
            memberView.set(view);
        }
        return members;
    }

    /**
     * Initializes equipment storage while retaining the observable view already exposed to callers.
     */
    private UniqueEquipmentList getEquipmentStorage() {
        if (equipment == null) {
            UniqueEquipmentList candidate = requireNonNull(equipmentFactory.get());
            ObservableList<Equipment> view = candidate.asUnmodifiableObservableList();
            equipment = candidate;
            equipmentView.set(view);
        }
        return equipment;
    }

    /**
     * Initializes loan storage while retaining the observable view already exposed to callers.
     */
    private UniqueLoanList getLoanStorage() {
        if (loans == null) {
            UniqueLoanList candidate = requireNonNull(loanFactory.get());
            ObservableList<Loan> view = candidate.asUnmodifiableObservableList();
            loans = candidate;
            loanView.set(view);
        }
        return loans;
    }

    @Override
    public ObservableList<Member> getMemberList() {
        return unmodifiableMembers;
    }

    @Override
    public ObservableList<Equipment> getEquipmentList() {
        return unmodifiableEquipment;
    }

    @Override
    public ObservableList<Loan> getLoanList() {
        return unmodifiableLoans;
    }

    //// util methods

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("persons", getPersonList())
                .add("members", getMemberList())
                .add("equipment", getEquipmentList())
                .add("loans", getLoanList())
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

        return getPersonList().equals(otherAddressBook.getPersonList())
                && getMemberList().equals(otherAddressBook.getMemberList())
                && getEquipmentList().equals(otherAddressBook.getEquipmentList())
                && getLoanList().equals(otherAddressBook.getLoanList());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPersonList(), getMemberList(), getEquipmentList(), getLoanList());
    }
}
