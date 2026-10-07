package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.getTypicalPersons;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.equipment.UniqueEquipmentList;
import seedu.address.model.equipment.exceptions.DuplicateEquipmentException;
import seedu.address.model.equipment.exceptions.EquipmentNotFoundException;
import seedu.address.model.loan.Loan;
import seedu.address.model.loan.UniqueLoanList;
import seedu.address.model.loan.exceptions.DuplicateOpenLoanException;
import seedu.address.model.loan.exceptions.OpenLoanNotFoundException;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.model.member.UniqueMemberList;
import seedu.address.model.member.exceptions.DuplicateMemberException;
import seedu.address.model.member.exceptions.MemberNotFoundException;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.exceptions.DuplicatePersonException;
import seedu.address.testutil.PersonBuilder;

/**
 * Verifies root behavior with controlled collections and legacy behavior with production defaults.
 * The real collection integration test runs only when all three scaffold dependencies are available.
 */
public class AddressBookTest {

    private static final NusId MEMBER_ID = new NusId("A0123456X");
    private static final Member MEMBER = new Member(MEMBER_ID, new Name("Alex Tan"),
            new Phone("91234567"), new Email("alex@example.com"));
    private static final Member OTHER_MEMBER = new Member(new NusId("A0765432X"), new Name("Blair Tan"),
            new Phone("98765432"), new Email("blair@example.com"));
    private static final UUID EQUIPMENT_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private static final Equipment EQUIPMENT = new Equipment(EQUIPMENT_ID, "Camera", "Photo", Condition.GOOD, "");
    private static final Equipment OTHER_EQUIPMENT = new Equipment(
            UUID.fromString("550e8400-e29b-41d4-a716-446655440001"), "Tripod", "Photo", Condition.FAIR, "");
    private static final LocalDate ASSIGNED_DATE = LocalDate.of(2026, 10, 6);
    private static final LocalDate RETURN_DATE = ASSIGNED_DATE.plusDays(7);
    private static final Loan OPEN_LOAN = new Loan(EQUIPMENT_ID, MEMBER_ID, ASSIGNED_DATE, RETURN_DATE, null);
    private static final Loan CLOSED_LOAN = OPEN_LOAN.withReturnedDate(RETURN_DATE);

    // These tests exercise root behavior with controlled collection contracts, not real collection internals.
    private final AddressBook addressBook = controlledAddressBook();

    @Test
    public void constructor() {
        assertEquals(List.of(), addressBook.getPersonList());
    }

    @Test
    public void resetData_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> addressBook.resetData(null));
    }

    @Test
    public void resetData_withValidReadOnlyAddressBook_replacesData() {
        AddressBook newData = controlledAddressBook();
        newData.setPersons(getTypicalPersons());
        addressBook.resetData(newData);
        assertEquals(newData, addressBook);
    }

    @Test
    public void resetData_withDuplicatePersons_throwsDuplicatePersonException() {
        // Two persons with the same identity fields
        Person editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND)
                .build();
        List<Person> newPersons = List.of(ALICE, editedAlice);
        AddressBookStub newData = new AddressBookStub(newPersons);

        assertThrows(DuplicatePersonException.class, () -> addressBook.resetData(newData));
    }

    @Test
    public void hasPerson_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> addressBook.hasPerson(null));
    }

    @Test
    public void hasPerson_personNotInAddressBook_returnsFalse() {
        assertFalse(addressBook.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personInAddressBook_returnsTrue() {
        addressBook.addPerson(ALICE);
        assertTrue(addressBook.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personWithSameIdentityFieldsInAddressBook_returnsTrue() {
        addressBook.addPerson(ALICE);
        Person editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND)
                .build();
        assertTrue(addressBook.hasPerson(editedAlice));
    }

    @Test
    public void getPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> addressBook.getPersonList().remove(0));
    }

    @Test
    public void laplaceOperations_nullArguments_throwNullPointerException() {
        assertThrows(NullPointerException.class, () -> addressBook.hasMember(null));
        assertThrows(NullPointerException.class, () -> addressBook.findMember(null));
        assertThrows(NullPointerException.class, () -> addressBook.addMember(null));
        assertThrows(NullPointerException.class, () -> addressBook.removeMember(null));
        assertThrows(NullPointerException.class, () -> addressBook.hasEquipment(null));
        assertThrows(NullPointerException.class, () -> addressBook.findEquipment(null));
        assertThrows(NullPointerException.class, () -> addressBook.addEquipment(null));
        assertThrows(NullPointerException.class, () -> addressBook.removeEquipment(null));
        assertThrows(NullPointerException.class, () -> addressBook.addLoan(null));
        assertThrows(NullPointerException.class, () -> addressBook.findOpenLoan(null));
        assertThrows(NullPointerException.class, () -> addressBook.findOpenLoansForMember(null));
        assertThrows(NullPointerException.class, () -> addressBook.closeLoan(null, RETURN_DATE));
        assertThrows(NullPointerException.class, () -> addressBook.closeLoan(EQUIPMENT_ID, null));
    }

    @Test
    public void toStringMethod() {
        String expected = AddressBook.class.getCanonicalName() + "{persons=" + addressBook.getPersonList()
                + ", members=" + addressBook.getMemberList() + ", equipment=" + addressBook.getEquipmentList()
                + ", loans=" + addressBook.getLoanList() + "}";
        assertEquals(expected, addressBook.toString());
    }

    @Test
    public void equalsAndHashCode() {
        AddressBook copy = controlledCopy(addressBook);

        assertTrue(addressBook.equals(addressBook));
        assertEquals(addressBook, copy);
        assertEquals(addressBook.hashCode(), copy.hashCode());
        assertFalse(addressBook.equals(null));
        assertFalse(addressBook.equals("address book"));
    }

    @Test
    public void membersAndEquipment_identityOperations_delegateAndPreserveStateOnFailure() {
        assertFalse(addressBook.hasMember(MEMBER_ID));
        assertEquals(Optional.empty(), addressBook.findMember(MEMBER_ID));
        assertFalse(addressBook.hasEquipment(EQUIPMENT_ID));
        assertEquals(Optional.empty(), addressBook.findEquipment(EQUIPMENT_ID));
        addressBook.addMember(MEMBER);
        addressBook.addEquipment(EQUIPMENT);
        assertTrue(addressBook.hasMember(MEMBER_ID));
        assertSame(MEMBER, addressBook.findMember(MEMBER_ID).orElseThrow());
        assertTrue(addressBook.hasEquipment(EQUIPMENT_ID));
        assertSame(EQUIPMENT, addressBook.findEquipment(EQUIPMENT_ID).orElseThrow());

        Member sameMember = new Member(MEMBER_ID, OTHER_MEMBER.getName(), MEMBER.getPhone(), MEMBER.getEmail());
        Equipment sameEquipment = new Equipment(EQUIPMENT_ID, "Edited", "Other", Condition.DAMAGED, "changed");
        AddressBook before = controlledCopy(addressBook);
        assertThrows(DuplicateMemberException.class, () -> addressBook.addMember(sameMember));
        assertThrows(DuplicateEquipmentException.class, () -> addressBook.addEquipment(sameEquipment));
        assertThrows(MemberNotFoundException.class, () -> addressBook.removeMember(OTHER_MEMBER));
        assertThrows(EquipmentNotFoundException.class, () -> addressBook.removeEquipment(OTHER_EQUIPMENT));
        assertEquals(before, addressBook);
        addressBook.removeMember(sameMember);
        addressBook.removeEquipment(sameEquipment);
        assertTrue(addressBook.getMemberList().isEmpty());
        assertTrue(addressBook.getEquipmentList().isEmpty());
    }

    @Test
    public void loans_validLifecycle_delegatesAndRetainsClosedHistory() {
        populateReferences(addressBook);
        addressBook.addLoan(OPEN_LOAN);
        addressBook.addLoan(new Loan(OTHER_EQUIPMENT.getUuid(), MEMBER_ID, ASSIGNED_DATE, RETURN_DATE, null));
        assertEquals(Optional.of(OPEN_LOAN), addressBook.findOpenLoan(EQUIPMENT_ID));
        List<Loan> snapshot = addressBook.findOpenLoansForMember(MEMBER_ID);
        assertEquals(2, snapshot.size());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(CLOSED_LOAN));
        assertTrue(addressBook.findOpenLoansForMember(OTHER_MEMBER.getNusId()).isEmpty());
        addressBook.closeLoan(EQUIPMENT_ID, RETURN_DATE);
        assertEquals(Optional.empty(), addressBook.findOpenLoan(EQUIPMENT_ID));
        assertEquals(1, addressBook.findOpenLoansForMember(MEMBER_ID).size());
        assertEquals(2, snapshot.size());
        assertEquals(CLOSED_LOAN, addressBook.getLoanList().get(0));
        addressBook.addLoan(OPEN_LOAN);
        assertEquals(3, addressBook.getLoanList().size());
    }

    @Test
    public void addLoan_missingReferences_rejectsOpenAndClosedLoansWithoutMutation() {
        AddressBook before = controlledCopy(addressBook);
        assertThrows(MemberNotFoundException.class, () -> addressBook.addLoan(OPEN_LOAN));
        assertThrows(MemberNotFoundException.class, () -> addressBook.addLoan(CLOSED_LOAN));
        assertEquals(before, addressBook);
        addressBook.addMember(MEMBER);
        before = controlledCopy(addressBook);
        assertThrows(EquipmentNotFoundException.class, () -> addressBook.addLoan(OPEN_LOAN));
        assertThrows(EquipmentNotFoundException.class, () -> addressBook.addLoan(CLOSED_LOAN));
        assertEquals(before, addressBook);
        AddressBook equipmentOnly = controlledAddressBook();
        equipmentOnly.addEquipment(EQUIPMENT);
        assertThrows(MemberNotFoundException.class, () -> equipmentOnly.addLoan(OPEN_LOAN));
        assertTrue(equipmentOnly.getLoanList().isEmpty());
    }

    @Test
    public void loans_collectionRejections_leaveRootUnchanged() {
        populateReferences(addressBook);
        addressBook.addLoan(OPEN_LOAN);
        AddressBook before = controlledCopy(addressBook);
        assertThrows(DuplicateOpenLoanException.class, () -> addressBook.addLoan(OPEN_LOAN));
        assertThrows(OpenLoanNotFoundException.class, () ->
                addressBook.closeLoan(OTHER_EQUIPMENT.getUuid(), RETURN_DATE));
        assertThrows(IllegalArgumentException.class, () ->
                addressBook.closeLoan(EQUIPMENT_ID, ASSIGNED_DATE.minusDays(1)));
        assertEquals(before, addressBook);
        addressBook.closeLoan(EQUIPMENT_ID, RETURN_DATE);
        before = controlledCopy(addressBook);
        assertThrows(OpenLoanNotFoundException.class, () -> addressBook.closeLoan(EQUIPMENT_ID, RETURN_DATE));
        assertEquals(before, addressBook);
    }

    @Test
    public void deletion_openReferences_blocksBothEntitiesWithoutMutation() {
        populateReferences(addressBook);
        addressBook.addLoan(OPEN_LOAN);
        addressBook.addLoan(CLOSED_LOAN);
        AddressBook before = controlledCopy(addressBook);
        assertThrows(IllegalStateException.class, () -> addressBook.removeMember(MEMBER));
        assertThrows(IllegalStateException.class, () -> addressBook.removeEquipment(EQUIPMENT));
        assertEquals(before, addressBook);
        addressBook.removeMember(OTHER_MEMBER);
        addressBook.removeEquipment(OTHER_EQUIPMENT);
        assertEquals(List.of(OPEN_LOAN, CLOSED_LOAN), addressBook.getLoanList());
    }

    @Test
    public void deletion_closedReferences_removesOnlyRelatedHistoryAsContractRequires() {
        populateReferences(addressBook);
        Loan unrelated = new Loan(OTHER_EQUIPMENT.getUuid(), OTHER_MEMBER.getNusId(),
                ASSIGNED_DATE, RETURN_DATE, RETURN_DATE);
        addressBook.addLoan(CLOSED_LOAN);
        addressBook.addLoan(unrelated);
        AddressBook equipmentDeletion = controlledCopy(addressBook);
        addressBook.removeMember(MEMBER);
        equipmentDeletion.removeEquipment(EQUIPMENT);
        assertEquals(List.of(unrelated), addressBook.getLoanList());
        assertEquals(List.of(unrelated), equipmentDeletion.getLoanList());
        assertTrue(addressBook.hasEquipment(EQUIPMENT_ID));
        assertTrue(equipmentDeletion.hasMember(MEMBER_ID));
    }

    @Test
    public void deletion_closedHistory_observersNeverSeeDanglingReferences() {
        populateReferences(addressBook);
        addressBook.addLoan(CLOSED_LOAN);
        addressBook.addLoan(new Loan(OTHER_EQUIPMENT.getUuid(), OTHER_MEMBER.getNusId(),
                ASSIGNED_DATE, RETURN_DATE, RETURN_DATE));
        AddressBook equipmentDeletion = controlledCopy(addressBook);
        List<Boolean> memberObservations = new ArrayList<>();
        List<Boolean> equipmentObservations = new ArrayList<>();
        observeRootChanges(addressBook, () -> memberObservations.add(hasValidVisibleReferences(addressBook)));
        observeRootChanges(equipmentDeletion, () ->
                equipmentObservations.add(hasValidVisibleReferences(equipmentDeletion)));

        addressBook.removeMember(MEMBER);
        equipmentDeletion.removeEquipment(EQUIPMENT);

        assertFalse(memberObservations.isEmpty());
        assertFalse(equipmentObservations.isEmpty());
        assertTrue(memberObservations.stream().allMatch(Boolean::booleanValue));
        assertTrue(equipmentObservations.stream().allMatch(Boolean::booleanValue));
        assertEquals(1, addressBook.getLoanList().size());
        assertEquals(1, equipmentDeletion.getLoanList().size());
    }

    @Test
    public void resetData_observersSeeCompleteReplacement_beforeEveryCollectionNotification() {
        populateReferences(addressBook);
        addressBook.addPerson(ALICE);
        addressBook.addLoan(OPEN_LOAN);
        AddressBookStub replacement = new AddressBookStub(List.of());
        replacement.members.add(OTHER_MEMBER);
        replacement.equipment.add(OTHER_EQUIPMENT);
        replacement.loans.add(new Loan(OTHER_EQUIPMENT.getUuid(), OTHER_MEMBER.getNusId(),
                ASSIGNED_DATE, RETURN_DATE, null));
        List<Boolean> observations = new ArrayList<>();
        List<Boolean> invalidationObservations = new ArrayList<>();
        observeRootChanges(addressBook, () -> observations.add(hasSameVisibleCollections(addressBook, replacement)
                && hasValidVisibleReferences(addressBook)
                && addressBook.findMember(MEMBER_ID).isEmpty()
                && addressBook.findEquipment(EQUIPMENT_ID).isEmpty()
                && addressBook.findOpenLoan(OTHER_EQUIPMENT.getUuid()).isPresent()));
        observeRootInvalidations(addressBook, () ->
                invalidationObservations.add(hasSameVisibleCollections(addressBook, replacement)));

        addressBook.resetData(replacement);

        assertEquals(4, observations.size());
        assertTrue(observations.stream().allMatch(Boolean::booleanValue));
        assertEquals(4, invalidationObservations.size());
        assertTrue(invalidationObservations.stream().allMatch(Boolean::booleanValue));
    }

    @Test
    public void resetData_equipmentAndLoanChanges_replacesDataWhenOtherCollectionsMatch() {
        populateReferences(addressBook);
        addressBook.addLoan(OPEN_LOAN);
        AddressBookStub replacement = new AddressBookStub(List.of());
        replacement.members.setAll(addressBook.getMemberList());
        replacement.equipment.setAll(addressBook.getEquipmentList());
        replacement.loans.setAll(addressBook.getLoanList());
        Equipment editedEquipment = new Equipment(EQUIPMENT_ID, "Edited camera", "Photo", Condition.FAIR, "Updated");
        replacement.equipment.set(0, editedEquipment);
        List<Boolean> observations = new ArrayList<>();
        observeRootChanges(addressBook, () -> observations.add(hasSameVisibleCollections(addressBook, replacement)));

        addressBook.resetData(replacement);

        assertSame(editedEquipment, addressBook.findEquipment(EQUIPMENT_ID).orElseThrow());
        assertFalse(observations.isEmpty());
        assertTrue(observations.stream().allMatch(Boolean::booleanValue));
        observations.clear();
        replacement.loans.set(0, CLOSED_LOAN);

        addressBook.resetData(replacement);

        assertEquals(List.of(CLOSED_LOAN), addressBook.getLoanList());
        assertEquals(Optional.empty(), addressBook.findOpenLoan(EQUIPMENT_ID));
        assertFalse(observations.isEmpty());
        assertTrue(observations.stream().allMatch(Boolean::booleanValue));
    }

    @Test
    public void resetData_unchangedData_doesNotNotifyObservers() {
        populateReferences(addressBook);
        addressBook.addPerson(ALICE);
        addressBook.addLoan(OPEN_LOAN);
        AddressBook copy = controlledCopy(addressBook);
        int[] notifications = {0};
        observeRootChanges(addressBook, () -> notifications[0]++);
        observeRootInvalidations(addressBook, () -> notifications[0]++);

        addressBook.resetData(addressBook);
        addressBook.resetData(copy);

        assertEquals(0, notifications[0]);
        assertEquals(copy, addressBook);
    }

    @Test
    public void getters_unmodifiableObservableViews_remainLiveAcrossWritesAndReset() {
        ObservableList<Person> persons = addressBook.getPersonList();
        ObservableList<Member> members = addressBook.getMemberList();
        ObservableList<Equipment> equipment = addressBook.getEquipmentList();
        ObservableList<Loan> loans = addressBook.getLoanList();
        int[] changes = {0};
        members.addListener((ListChangeListener<Member>) change -> changes[0]++);
        populateReferences(addressBook);
        addressBook.addPerson(ALICE);
        addressBook.addLoan(OPEN_LOAN);
        assertEquals(List.of(ALICE), persons);
        assertEquals(2, members.size());
        assertEquals(2, equipment.size());
        assertEquals(List.of(OPEN_LOAN), loans);
        assertTrue(changes[0] > 0);
        assertThrows(UnsupportedOperationException.class, () -> persons.add(ALICE));
        assertThrows(UnsupportedOperationException.class, () -> members.clear());
        assertThrows(UnsupportedOperationException.class, () -> equipment.set(0, OTHER_EQUIPMENT));
        assertThrows(UnsupportedOperationException.class, () -> loans.remove(0));
        addressBook.resetData(controlledAddressBook());
        assertTrue(persons.isEmpty());
        assertTrue(members.isEmpty());
        assertTrue(equipment.isEmpty());
        assertTrue(loans.isEmpty());
        AddressBook replacement = controlledAddressBook();
        populateReferences(replacement);
        replacement.addLoan(OPEN_LOAN);
        addressBook.resetData(replacement);
        assertEquals(2, members.size());
        assertEquals(2, equipment.size());
        assertEquals(List.of(OPEN_LOAN), loans);
        addressBook.closeLoan(EQUIPMENT_ID, RETURN_DATE);
        assertEquals(List.of(CLOSED_LOAN), loans);
        assertEquals(List.of(OPEN_LOAN), replacement.getLoanList());
    }

    @Test
    public void resetData_copiesContainers_reusesImmutableObjectsAndSupportsSelfReset() {
        AddressBookStub source = new AddressBookStub(List.of(ALICE));
        source.members.add(MEMBER);
        source.equipment.add(EQUIPMENT);
        source.loans.add(OPEN_LOAN);
        addressBook.resetData(source);
        assertSame(ALICE, addressBook.getPersonList().get(0));
        assertSame(MEMBER, addressBook.getMemberList().get(0));
        assertSame(EQUIPMENT, addressBook.getEquipmentList().get(0));
        assertSame(OPEN_LOAN, addressBook.getLoanList().get(0));
        AddressBook copy = controlledCopy(addressBook);
        source.persons.clear();
        source.members.clear();
        source.equipment.clear();
        source.loans.clear();
        assertEquals(copy, addressBook);
        addressBook.resetData(addressBook);
        assertEquals(copy, addressBook);
        addressBook.closeLoan(EQUIPMENT_ID, RETURN_DATE);
        assertEquals(List.of(OPEN_LOAN), copy.getLoanList());
    }

    @Test
    public void resetData_invalidReplacements_leaveAllExistingCollectionsUnchanged() {
        populateReferences(addressBook);
        addressBook.addPerson(ALICE);
        addressBook.addLoan(OPEN_LOAN);
        AddressBook before = controlledCopy(addressBook);
        int[] notifications = {0};
        observeRootChanges(addressBook, () -> notifications[0]++);
        AddressBookStub source = validReplacement();
        source.persons.add(ALICE);
        assertThrows(DuplicatePersonException.class, () -> addressBook.resetData(source));
        assertEquals(before, addressBook);
        source.persons.clear();
        source.members.add(MEMBER);
        assertThrows(DuplicateMemberException.class, () -> addressBook.resetData(source));
        assertEquals(before, addressBook);
        source.members.remove(1);
        source.equipment.add(EQUIPMENT);
        assertThrows(DuplicateEquipmentException.class, () -> addressBook.resetData(source));
        assertEquals(before, addressBook);
        source.equipment.remove(1);
        source.loans.add(OPEN_LOAN);
        assertThrows(DuplicateOpenLoanException.class, () -> addressBook.resetData(source));
        assertEquals(before, addressBook);
        source.loans.remove(1);
        source.members.clear();
        assertThrows(MemberNotFoundException.class, () -> addressBook.resetData(source));
        assertEquals(before, addressBook);
        source.members.add(MEMBER);
        source.equipment.clear();
        assertThrows(EquipmentNotFoundException.class, () -> addressBook.resetData(source));
        assertEquals(before, addressBook);
        source.loans.setAll(CLOSED_LOAN);
        assertThrows(EquipmentNotFoundException.class, () -> addressBook.resetData(source));
        assertEquals(before, addressBook);
        source.equipment.add(EQUIPMENT);
        source.members.clear();
        assertThrows(MemberNotFoundException.class, () -> addressBook.resetData(source));
        assertEquals(before, addressBook);
        source.members.add(MEMBER);
        source.loans.add(null);
        assertThrows(NullPointerException.class, () -> addressBook.resetData(source));
        assertEquals(before, addressBook);
        assertEquals(0, notifications[0]);
    }

    @Test
    public void equalsAndHashCode_allCollections_useValuesRatherThanContainerIdentity() {
        populateReferences(addressBook);
        addressBook.addPerson(ALICE);
        addressBook.addLoan(OPEN_LOAN);
        AddressBook copy = controlledCopy(addressBook);
        assertEquals(addressBook, copy);
        assertEquals(addressBook.hashCode(), copy.hashCode());
        copy.closeLoan(EQUIPMENT_ID, RETURN_DATE);
        assertFalse(addressBook.equals(copy));
        copy = controlledCopy(addressBook);
        copy.addMember(new Member(new NusId("A0111111X"), MEMBER.getName(), MEMBER.getPhone(), MEMBER.getEmail()));
        assertFalse(addressBook.equals(copy));
        copy = controlledCopy(addressBook);
        copy.removeEquipment(OTHER_EQUIPMENT);
        assertFalse(addressBook.equals(copy));
        copy = controlledCopy(addressBook);
        copy.removePerson(ALICE);
        assertFalse(addressBook.equals(copy));
        assertTrue(addressBook.toString().contains("members=" + addressBook.getMemberList()));
        assertTrue(addressBook.toString().contains("equipment=" + addressBook.getEquipmentList()));
        assertTrue(addressBook.toString().contains("loans=" + addressBook.getLoanList()));
    }

    @Test
    public void collectionOperations_controlledSpies_verifyDelegation() {
        ControlledMembers members = new ControlledMembers();
        ControlledEquipment equipment = new ControlledEquipment();
        ControlledLoans loans = new ControlledLoans();
        AddressBook root = new AddressBook(() -> members, () -> equipment, () -> loans);
        // This root is used only for direct operations; reset requires factories that return fresh containers.
        root.addMember(MEMBER);
        root.addEquipment(EQUIPMENT);
        root.addLoan(OPEN_LOAN);
        assertSame(MEMBER, members.lastAdded);
        assertSame(EQUIPMENT, equipment.lastAdded);
        assertSame(OPEN_LOAN, loans.lastAdded);
        root.closeLoan(EQUIPMENT_ID, RETURN_DATE);
        assertEquals(EQUIPMENT_ID, loans.lastClosedUuid);
        assertEquals(RETURN_DATE, loans.lastReturnedDate);
    }

    @Test
    public void personOnlyLifecycle_unavailableFactories_preservesLegacyBehaviorWithoutInitializingCollections() {
        AddressBook root = unavailableCollectionsAddressBook();
        ObservableList<Member> members = root.getMemberList();
        ObservableList<Equipment> equipment = root.getEquipmentList();
        ObservableList<Loan> loans = root.getLoanList();
        assertTrue(members.isEmpty());
        assertTrue(equipment.isEmpty());
        assertTrue(loans.isEmpty());
        assertFalse(root.hasMember(MEMBER_ID));
        assertEquals(Optional.empty(), root.findMember(MEMBER_ID));
        assertFalse(root.hasEquipment(EQUIPMENT_ID));
        assertEquals(Optional.empty(), root.findEquipment(EQUIPMENT_ID));
        assertEquals(Optional.empty(), root.findOpenLoan(EQUIPMENT_ID));
        assertEquals(List.of(), root.findOpenLoansForMember(MEMBER_ID));
        assertThrows(MemberNotFoundException.class, () -> root.removeMember(MEMBER));
        assertThrows(EquipmentNotFoundException.class, () -> root.removeEquipment(EQUIPMENT));
        assertThrows(OpenLoanNotFoundException.class, () -> root.closeLoan(EQUIPMENT_ID, RETURN_DATE));
        assertThrows(MemberNotFoundException.class, () -> root.addLoan(OPEN_LOAN));
        assertThrows(UnsupportedOperationException.class, () -> members.add(MEMBER));
        assertThrows(UnsupportedOperationException.class, () -> equipment.add(EQUIPMENT));
        assertThrows(UnsupportedOperationException.class, () -> loans.add(OPEN_LOAN));
        root.addPerson(ALICE);
        root.setPerson(ALICE, ALICE);
        AddressBook copy = new AddressBook(root);
        assertEquals(root, copy);
        assertEquals(root.hashCode(), copy.hashCode());
        assertTrue(root.toString().contains("persons=" + List.of(ALICE)));
        assertTrue(root.toString().contains("members=[]"));
        assertTrue(root.toString().contains("equipment=[]"));
        assertTrue(root.toString().contains("loans=[]"));
        root.resetData(root);
        assertEquals(copy, root);
        root.removePerson(ALICE);
        assertEquals(List.of(ALICE), copy.getPersonList());
        root.resetData(new AddressBook());
        assertEquals(new AddressBook(), root);
    }

    @Test
    public void writes_unavailableFactories_failExplicitlyWithoutChangingExistingPersons() {
        AddressBook root = unavailableCollectionsAddressBook();
        root.addPerson(ALICE);
        AddressBook before = new AddressBook(root);
        assertThrows(UnsupportedOperationException.class, () -> root.addMember(MEMBER));
        assertThrows(UnsupportedOperationException.class, () -> root.addEquipment(EQUIPMENT));
        assertEquals(before, root);
        AddressBookStub replacement = new AddressBookStub(List.of());
        replacement.members.add(MEMBER);
        assertThrows(UnsupportedOperationException.class, () -> root.resetData(replacement));
        assertEquals(before, root);
        replacement.members.clear();
        replacement.equipment.add(EQUIPMENT);
        assertThrows(UnsupportedOperationException.class, () -> root.resetData(replacement));
        assertEquals(before, root);
        replacement.equipment.clear();
        replacement.loans.add(OPEN_LOAN);
        assertThrows(UnsupportedOperationException.class, () -> root.resetData(replacement));
        assertEquals(before, root);
    }

    @Test
    public void resetData_unavailableReplacementView_doesNotCommitAnyCollection() {
        AddressBook root = new AddressBook(UnavailableMemberView::new,
                ControlledEquipment::new, ControlledLoans::new);
        root.addPerson(ALICE);
        AddressBook before = new AddressBook(root);
        AddressBookStub replacement = validReplacement();
        replacement.persons.clear();
        assertThrows(UnsupportedOperationException.class, () -> root.resetData(replacement));
        assertEquals(before, root);
        assertThrows(UnsupportedOperationException.class, () -> root.addMember(MEMBER));
        assertEquals(before, root);
    }

    @Test
    public void realCollections_rootIntegration_runsOnlyWhenImplementationsAreAvailable() {
        assumeTrue(!Boolean.getBoolean("rootModel.controlledCollections"),
                "Controlled dependency run is not real collection integration");
        AddressBook real = new AddressBook();
        try {
            new UniqueMemberList().asUnmodifiableObservableList();
            new UniqueEquipmentList().asUnmodifiableObservableList();
            new UniqueLoanList().asUnmodifiableObservableList();
        } catch (UnsupportedOperationException exception) {
            boolean knownPlaceholder = List.of("UniqueMemberList is not implemented",
                    "UniqueEquipmentList is not implemented", "UniqueLoanList is not implemented")
                    .contains(exception.getMessage());
            if (!knownPlaceholder) {
                throw exception;
            }
            assumeTrue(false, "Real collection integration unavailable: " + exception.getMessage());
        }
        populateReferences(real);
        real.addLoan(OPEN_LOAN);
        AddressBook before = new AddressBook(real);
        assertThrows(IllegalStateException.class, () -> real.removeMember(MEMBER));
        assertThrows(IllegalStateException.class, () -> real.removeEquipment(EQUIPMENT));
        assertThrows(DuplicateOpenLoanException.class, () -> real.addLoan(OPEN_LOAN));
        assertEquals(before, real);
        AddressBookStub invalid = validReplacement();
        invalid.members.clear();
        assertThrows(MemberNotFoundException.class, () -> real.resetData(invalid));
        assertEquals(before, real);
        assertThrows(MemberNotFoundException.class, () -> real.addLoan(new Loan(EQUIPMENT_ID,
                new NusId("A0111111X"), ASSIGNED_DATE, RETURN_DATE, null)));
        assertEquals(before, real);
        assertThrows(UnsupportedOperationException.class, () -> real.getMemberList().clear());
        real.resetData(real);
        assertEquals(before, real);
        real.closeLoan(EQUIPMENT_ID, RETURN_DATE);
        assertEquals(Optional.empty(), real.findOpenLoan(EQUIPMENT_ID));
        assertEquals(List.of(CLOSED_LOAN), real.getLoanList());
        assertEquals(List.of(OPEN_LOAN), before.getLoanList());
        real.removeEquipment(EQUIPMENT);
        assertTrue(real.getLoanList().isEmpty());
        real.removeMember(MEMBER);
        assertFalse(real.hasMember(MEMBER_ID));
    }

    /**
     * Provides dependencies that fail immediately if a person-only path attempts to initialize them.
     */
    private static AddressBook unavailableCollectionsAddressBook() {
        return new AddressBook(() -> {
            throw new UnsupportedOperationException("Member factory must not be used for person-only data");
        }, () -> {
            throw new UnsupportedOperationException("Equipment factory must not be used for person-only data");
        }, () -> {
            throw new UnsupportedOperationException("Loan factory must not be used for person-only data");
        });
    }

    /**
     * Observes public collection events, where synchronous UI listeners read the root's other collections.
     */
    private static void observeRootChanges(AddressBook root, Runnable observer) {
        root.getPersonList().addListener((ListChangeListener<Person>) change -> observer.run());
        root.getMemberList().addListener((ListChangeListener<Member>) change -> observer.run());
        root.getEquipmentList().addListener((ListChangeListener<Equipment>) change -> observer.run());
        root.getLoanList().addListener((ListChangeListener<Loan>) change -> observer.run());
    }

    /**
     * Observes invalidations as well as list changes, because both can trigger UI refreshes.
     */
    private static void observeRootInvalidations(AddressBook root, Runnable observer) {
        root.getPersonList().addListener((InvalidationListener) observable -> observer.run());
        root.getMemberList().addListener((InvalidationListener) observable -> observer.run());
        root.getEquipmentList().addListener((InvalidationListener) observable -> observer.run());
        root.getLoanList().addListener((InvalidationListener) observable -> observer.run());
    }

    private static boolean hasSameVisibleCollections(AddressBook root, ReadOnlyAddressBook expected) {
        return root.getPersonList().equals(expected.getPersonList())
                && root.getMemberList().equals(expected.getMemberList())
                && root.getEquipmentList().equals(expected.getEquipmentList())
                && root.getLoanList().equals(expected.getLoanList());
    }

    private static boolean hasValidVisibleReferences(AddressBook root) {
        return root.getLoanList().stream().allMatch(loan ->
                root.getMemberList().stream().anyMatch(member -> member.getNusId().equals(loan.getMemberNusId()))
                && root.getEquipmentList().stream().anyMatch(item -> item.getUuid().equals(loan.getEquipmentUuid())));
    }

    private static AddressBook controlledAddressBook() {
        return new AddressBook(ControlledMembers::new, ControlledEquipment::new, ControlledLoans::new);
    }

    private static AddressBook controlledCopy(ReadOnlyAddressBook source) {
        AddressBook copy = controlledAddressBook();
        copy.resetData(source);
        return copy;
    }

    private static void populateReferences(AddressBook root) {
        root.addMember(MEMBER);
        root.addMember(OTHER_MEMBER);
        root.addEquipment(EQUIPMENT);
        root.addEquipment(OTHER_EQUIPMENT);
    }

    private static AddressBookStub validReplacement() {
        AddressBookStub source = new AddressBookStub(List.of(ALICE));
        source.members.add(MEMBER);
        source.equipment.add(EQUIPMENT);
        source.loans.add(OPEN_LOAN);
        return source;
    }

    /**
     * A read-only test double with independently mutable lists for deliberately invalid replacement data.
     */
    private static class AddressBookStub implements ReadOnlyAddressBook {
        private final ObservableList<Person> persons = FXCollections.observableArrayList();
        private final ObservableList<Member> members = FXCollections.observableArrayList();
        private final ObservableList<Equipment> equipment = FXCollections.observableArrayList();
        private final ObservableList<Loan> loans = FXCollections.observableArrayList();

        AddressBookStub(Collection<Person> persons) {
            this.persons.setAll(persons);
        }

        @Override
        public ObservableList<Person> getPersonList() {
            return persons;
        }

        @Override
        public ObservableList<Member> getMemberList() {
            return members;
        }

        @Override
        public ObservableList<Equipment> getEquipmentList() {
            return equipment;
        }

        @Override
        public ObservableList<Loan> getLoanList() {
            return loans;
        }
    }

    /**
     * Controlled member dependency. It models only the agreed contract, not the production implementation.
     */
    private static class ControlledMembers extends UniqueMemberList {
        private final ObservableList<Member> values = FXCollections.observableArrayList();
        private Member lastAdded;

        @Override
        public boolean contains(NusId nusId) {
            return findByNusId(nusId).isPresent();
        }

        @Override
        public Optional<Member> findByNusId(NusId nusId) {
            return values.stream().filter(member -> member.getNusId().equals(nusId)).findFirst();
        }

        @Override
        public void add(Member member) {
            if (contains(member.getNusId())) {
                throw new DuplicateMemberException();
            }
            lastAdded = member;
            values.add(member);
        }

        @Override
        public void remove(Member member) {
            Member stored = findByNusId(member.getNusId()).orElseThrow(MemberNotFoundException::new);
            values.remove(stored);
        }

        @Override
        public void setMembers(List<Member> members) {
            ControlledMembers replacement = new ControlledMembers();
            members.forEach(replacement::add);
            values.setAll(replacement.values);
        }

        @Override
        public ObservableList<Member> asUnmodifiableObservableList() {
            return FXCollections.unmodifiableObservableList(values);
        }
    }

    /**
     * Controlled dependency whose bulk validation works but whose observable view is unavailable.
     */
    private static class UnavailableMemberView extends ControlledMembers {
        @Override
        public ObservableList<Member> asUnmodifiableObservableList() {
            throw new UnsupportedOperationException("Member view is unavailable");
        }
    }

    /**
     * Controlled equipment dependency with identity-based operations and atomic bulk replacement.
     */
    private static class ControlledEquipment extends UniqueEquipmentList {
        private final ObservableList<Equipment> values = FXCollections.observableArrayList();
        private Equipment lastAdded;

        @Override
        public boolean contains(UUID uuid) {
            return findByUuid(uuid).isPresent();
        }

        @Override
        public Optional<Equipment> findByUuid(UUID uuid) {
            return values.stream().filter(item -> item.getUuid().equals(uuid)).findFirst();
        }

        @Override
        public void add(Equipment item) {
            if (contains(item.getUuid())) {
                throw new DuplicateEquipmentException();
            }
            lastAdded = item;
            values.add(item);
        }

        @Override
        public void remove(Equipment item) {
            Equipment stored = findByUuid(item.getUuid()).orElseThrow(EquipmentNotFoundException::new);
            values.remove(stored);
        }

        @Override
        public void setEquipment(List<Equipment> equipment) {
            ControlledEquipment replacement = new ControlledEquipment();
            equipment.forEach(replacement::add);
            values.setAll(replacement.values);
        }

        @Override
        public ObservableList<Equipment> asUnmodifiableObservableList() {
            return FXCollections.unmodifiableObservableList(values);
        }
    }

    /**
     * Controlled loan dependency with open-loan uniqueness and immutable closing.
     */
    private static class ControlledLoans extends UniqueLoanList {
        private final ObservableList<Loan> values = FXCollections.observableArrayList();
        private Loan lastAdded;
        private UUID lastClosedUuid;
        private LocalDate lastReturnedDate;

        @Override
        public Optional<Loan> findOpenLoan(UUID uuid) {
            return values.stream().filter(loan -> loan.isOpen() && loan.getEquipmentUuid().equals(uuid)).findFirst();
        }

        @Override
        public List<Loan> findOpenLoansForMember(NusId nusId) {
            return values.stream().filter(loan -> loan.isOpen() && loan.getMemberNusId().equals(nusId)).toList();
        }

        @Override
        public void add(Loan loan) {
            if (loan.isOpen() && findOpenLoan(loan.getEquipmentUuid()).isPresent()) {
                throw new DuplicateOpenLoanException();
            }
            lastAdded = loan;
            values.add(loan);
        }

        @Override
        public void closeLoan(UUID uuid, LocalDate returnedDate) {
            Loan open = findOpenLoan(uuid).orElseThrow(OpenLoanNotFoundException::new);
            if (returnedDate.isBefore(open.getAssignedDate())) {
                throw new IllegalArgumentException("Return date precedes assigned date");
            }
            lastClosedUuid = uuid;
            lastReturnedDate = returnedDate;
            values.set(values.indexOf(open), open.withReturnedDate(returnedDate));
        }

        @Override
        public void setLoans(List<Loan> loans) {
            ControlledLoans replacement = new ControlledLoans();
            loans.forEach(replacement::add);
            values.setAll(replacement.values);
        }

        @Override
        public ObservableList<Loan> asUnmodifiableObservableList() {
            return FXCollections.unmodifiableObservableList(values);
        }
    }
}
