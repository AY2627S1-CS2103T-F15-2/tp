package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.equipment.Availability;
import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.equipment.exceptions.DuplicateEquipmentException;
import seedu.address.model.equipment.exceptions.EquipmentNotFoundException;
import seedu.address.model.loan.Loan;
import seedu.address.model.loan.exceptions.DuplicateOpenLoanException;
import seedu.address.model.loan.exceptions.OpenLoanNotFoundException;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.model.member.exceptions.DuplicateMemberException;
import seedu.address.model.member.exceptions.MemberNotFoundException;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.testutil.AddressBookBuilder;

public class ModelManagerTest {

    private static final NusId MEMBER_ID = new NusId("A0123456X");
    private static final UUID EQUIPMENT_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private static final Member MEMBER = new Member(MEMBER_ID, ALICE.getName(), ALICE.getPhone(), ALICE.getEmail());
    private static final Equipment EQUIPMENT = new Equipment(EQUIPMENT_ID, "Camera", "Photography", Condition.GOOD, "");
    private static final LocalDate ASSIGNED_DATE = LocalDate.of(2026, 10, 6);
    private static final LocalDate RETURNED_DATE = ASSIGNED_DATE.plusDays(2);
    private static final Loan LOAN = new Loan(EQUIPMENT_ID, MEMBER_ID, ASSIGNED_DATE, ASSIGNED_DATE.plusDays(7), null);

    private ModelManager modelManager = new ModelManager();

    @Test
    public void constructor() {
        assertEquals(new UserPrefs(), modelManager.getUserPrefs());
        assertEquals(new GuiSettings(), modelManager.getGuiSettings());
        assertEquals(new AddressBook(), new AddressBook(modelManager.getAddressBook()));
    }

    @Test
    public void constructor_validUserPrefs_copiesUserPrefs() {
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        modelManager = new ModelManager(new AddressBook(), userPrefs);
        assertEquals(userPrefs, modelManager.getUserPrefs());

        // Modifying userPrefs should not modify modelManager's userPrefs
        UserPrefs oldUserPrefs = new UserPrefs(userPrefs);
        userPrefs.setGuiSettings(new GuiSettings(5, 6, 7, 8));
        assertEquals(oldUserPrefs, modelManager.getUserPrefs());
    }

    @Test
    public void setGuiSettings_nullGuiSettings_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.setGuiSettings(null));
    }

    @Test
    public void setGuiSettings_validGuiSettings_setsGuiSettings() {
        GuiSettings guiSettings = new GuiSettings(1, 2, 3, 4);
        modelManager.setGuiSettings(guiSettings);
        assertEquals(guiSettings, modelManager.getGuiSettings());
    }

    @Test
    public void hasPerson_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.hasPerson(null));
    }

    @Test
    public void hasPerson_personNotInAddressBook_returnsFalse() {
        assertFalse(modelManager.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personInAddressBook_returnsTrue() {
        modelManager.addPerson(ALICE);
        assertTrue(modelManager.hasPerson(ALICE));
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> modelManager.getFilteredPersonList().remove(0));
    }

    @Test
    public void memberOperations_controlledRoot_delegatesArgumentsAndResults() {
        RecordingAddressBook root = new RecordingAddressBook();
        Model model = new ModelManager(new UserPrefs(), root);
        assertTrue(model.hasMember(MEMBER_ID));
        root.assertCall("hasMember", MEMBER_ID);
        root.memberExists = false;
        assertFalse(model.hasMember(MEMBER_ID));
        root.assertCall("hasMember", MEMBER_ID);
        assertSame(root.memberResult, model.findMember(MEMBER_ID));
        root.assertCall("findMember", MEMBER_ID);
        root.memberResult = Optional.empty();
        assertSame(root.memberResult, model.findMember(MEMBER_ID));
        root.assertCall("findMember", MEMBER_ID);
        model.addMember(MEMBER);
        root.assertCall("addMember", MEMBER);
        model.deleteMember(MEMBER);
        root.assertCall("removeMember", MEMBER);
        assertSame(root.memberList, model.getMemberList());
        root.assertCall("getMemberList");
    }

    @Test
    public void equipmentOperations_controlledRoot_delegatesArgumentsAndResults() {
        RecordingAddressBook root = new RecordingAddressBook();
        Model model = new ModelManager(new UserPrefs(), root);
        assertTrue(model.hasEquipment(EQUIPMENT_ID));
        root.assertCall("hasEquipment", EQUIPMENT_ID);
        root.equipmentExists = false;
        assertFalse(model.hasEquipment(EQUIPMENT_ID));
        root.assertCall("hasEquipment", EQUIPMENT_ID);
        assertSame(root.equipmentResult, model.findEquipment(EQUIPMENT_ID));
        root.assertCall("findEquipment", EQUIPMENT_ID);
        root.equipmentResult = Optional.empty();
        assertSame(root.equipmentResult, model.findEquipment(EQUIPMENT_ID));
        root.assertCall("findEquipment", EQUIPMENT_ID);
        model.addEquipment(EQUIPMENT);
        root.assertCall("addEquipment", EQUIPMENT);
        model.deleteEquipment(EQUIPMENT);
        root.assertCall("removeEquipment", EQUIPMENT);
        assertSame(root.equipmentList, model.getEquipmentList());
        root.assertCall("getEquipmentList");
    }

    @Test
    public void loanOperations_controlledRoot_delegatesArgumentsAndResults() {
        RecordingAddressBook root = new RecordingAddressBook();
        Model model = new ModelManager(new UserPrefs(), root);
        model.addLoan(LOAN);
        root.assertCall("addLoan", LOAN);
        assertSame(root.loanResult, model.findOpenLoan(EQUIPMENT_ID));
        root.assertCall("findOpenLoan", EQUIPMENT_ID);
        root.loanResult = Optional.empty();
        assertSame(root.loanResult, model.findOpenLoan(EQUIPMENT_ID));
        root.assertCall("findOpenLoan", EQUIPMENT_ID);
        assertSame(root.memberLoans, model.findOpenLoansForMember(MEMBER_ID));
        root.assertCall("findOpenLoansForMember", MEMBER_ID);
        root.memberLoans = List.of();
        assertSame(root.memberLoans, model.findOpenLoansForMember(MEMBER_ID));
        root.assertCall("findOpenLoansForMember", MEMBER_ID);
        model.closeLoan(EQUIPMENT_ID, RETURNED_DATE);
        root.assertCall("closeLoan", EQUIPMENT_ID, RETURNED_DATE);
        assertSame(root.loanList, model.getLoanList());
        root.assertCall("getLoanList");
    }

    @Test
    public void laplaceOperations_rootFailure_propagatesOriginalException() {
        RecordingAddressBook root = new RecordingAddressBook();
        Model model = new ModelManager(new UserPrefs(), root);
        List<Consumer<Model>> operations = List.of(
                target -> target.hasMember(MEMBER_ID), target -> target.findMember(MEMBER_ID),
                target -> target.addMember(MEMBER), target -> target.deleteMember(MEMBER), Model::getMemberList,
                target -> target.hasEquipment(EQUIPMENT_ID), target -> target.findEquipment(EQUIPMENT_ID),
                target -> target.addEquipment(EQUIPMENT), target -> target.deleteEquipment(EQUIPMENT),
                Model::getEquipmentList,
                target -> target.addLoan(LOAN), target -> target.findOpenLoan(EQUIPMENT_ID),
                target -> target.findOpenLoansForMember(MEMBER_ID),
                target -> target.closeLoan(EQUIPMENT_ID, RETURNED_DATE),
                Model::getLoanList, target -> target.getAvailability(EQUIPMENT_ID));
        List<RuntimeException> failures = List.of(new UnsupportedOperationException("Unfinished root"),
                new DuplicateMemberException(), new MemberNotFoundException(), new DuplicateEquipmentException(),
                new EquipmentNotFoundException(), new DuplicateOpenLoanException(), new OpenLoanNotFoundException(),
                new IllegalStateException("Open loan blocks deletion"), new IllegalArgumentException("Invalid date"));
        for (RuntimeException failure : failures) {
            root.failure = failure;
            for (Consumer<Model> operation : operations) {
                assertSame(failure, org.junit.jupiter.api.Assertions.assertThrows(
                        failure.getClass(), () -> operation.accept(model)));
            }
        }
    }

    @Test
    public void getAvailability_allConditions_usesConditionAndOpenLoanStatus() {
        RecordingAddressBook root = new RecordingAddressBook();
        Model model = new ModelManager(new UserPrefs(), root);
        for (Condition condition : Condition.values()) {
            root.equipmentResult = Optional.of(new Equipment(EQUIPMENT_ID, "Camera", "Photography", condition, ""));
            root.loanResult = Optional.empty();
            Availability expected = condition == Condition.GOOD || condition == Condition.FAIR
                    ? Availability.AVAILABLE : Availability.UNAVAILABLE;
            assertEquals(expected, model.getAvailability(EQUIPMENT_ID));
            root.assertAvailabilityCalls();
            root.loanResult = Optional.of(LOAN);
            assertEquals(Availability.ASSIGNED, model.getAvailability(EQUIPMENT_ID));
            root.assertAvailabilityCalls();
        }
    }

    @Test
    public void getAvailability_unknownEquipment_throwsEquipmentNotFoundException() {
        RecordingAddressBook root = new RecordingAddressBook();
        root.equipmentResult = Optional.empty();
        Model model = new ModelManager(new UserPrefs(), root);
        assertThrows(EquipmentNotFoundException.class, () -> model.getAvailability(EQUIPMENT_ID));
        root.assertCall("findEquipment", EQUIPMENT_ID);
    }

    @Test
    public void getAvailability_loanLookupFails_propagatesOriginalException() {
        RuntimeException failure = new UnsupportedOperationException("Unfinished loan lookup");
        AddressBook root = new RecordingAddressBook() {
            @Override
            public Optional<Loan> findOpenLoan(UUID uuid) {
                assertSame(EQUIPMENT_ID, uuid);
                throw failure;
            }
        };
        Model model = new ModelManager(new UserPrefs(), root);
        assertSame(failure, org.junit.jupiter.api.Assertions.assertThrows(
                UnsupportedOperationException.class, () -> model.getAvailability(EQUIPMENT_ID)));
    }

    @Test
    public void laplaceOperations_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.hasMember(null));
        assertThrows(NullPointerException.class, () -> modelManager.findMember(null));
        assertThrows(NullPointerException.class, () -> modelManager.addMember(null));
        assertThrows(NullPointerException.class, () -> modelManager.deleteMember(null));
        assertThrows(NullPointerException.class, () -> modelManager.hasEquipment(null));
        assertThrows(NullPointerException.class, () -> modelManager.findEquipment(null));
        assertThrows(NullPointerException.class, () -> modelManager.addEquipment(null));
        assertThrows(NullPointerException.class, () -> modelManager.deleteEquipment(null));
        assertThrows(NullPointerException.class, () -> modelManager.addLoan(null));
        assertThrows(NullPointerException.class, () -> modelManager.findOpenLoan(null));
        assertThrows(NullPointerException.class, () -> modelManager.findOpenLoansForMember(null));
        assertThrows(NullPointerException.class, () -> modelManager.closeLoan(null, RETURNED_DATE));
        assertThrows(NullPointerException.class, () -> modelManager.closeLoan(EQUIPMENT_ID, null));
        assertThrows(NullPointerException.class, () -> modelManager.getAvailability(null));
    }

    @Test
    public void loanLifecycle_realRoot_updatesCollectionsLookupsAndAvailability() {
        ObservableList<Member> members = modelManager.getMemberList();
        ObservableList<Equipment> equipment = modelManager.getEquipmentList();
        ObservableList<Loan> loans = modelManager.getLoanList();
        assertFalse(modelManager.hasMember(MEMBER_ID));
        assertFalse(modelManager.hasEquipment(EQUIPMENT_ID));
        assertTrue(modelManager.findMember(MEMBER_ID).isEmpty());
        assertTrue(modelManager.findEquipment(EQUIPMENT_ID).isEmpty());
        assertTrue(modelManager.findOpenLoan(EQUIPMENT_ID).isEmpty());
        assertTrue(modelManager.findOpenLoansForMember(MEMBER_ID).isEmpty());
        assertThrows(EquipmentNotFoundException.class, () -> modelManager.getAvailability(EQUIPMENT_ID));

        modelManager.addMember(MEMBER);
        modelManager.addEquipment(EQUIPMENT);
        assertEquals(List.of(MEMBER), members);
        assertEquals(List.of(EQUIPMENT), equipment);
        assertTrue(modelManager.hasMember(MEMBER_ID));
        assertTrue(modelManager.hasEquipment(EQUIPMENT_ID));
        assertEquals(Optional.of(MEMBER), modelManager.findMember(MEMBER_ID));
        assertEquals(Optional.of(EQUIPMENT), modelManager.findEquipment(EQUIPMENT_ID));
        assertEquals(Availability.AVAILABLE, modelManager.getAvailability(EQUIPMENT_ID));
        modelManager.addLoan(LOAN);
        assertEquals(List.of(LOAN), loans);
        assertEquals(Optional.of(LOAN), modelManager.findOpenLoan(EQUIPMENT_ID));
        List<Loan> snapshot = modelManager.findOpenLoansForMember(MEMBER_ID);
        assertEquals(List.of(LOAN), snapshot);
        assertEquals(Availability.ASSIGNED, modelManager.getAvailability(EQUIPMENT_ID));
        assertThrows(UnsupportedOperationException.class, () -> members.add(MEMBER));
        assertThrows(UnsupportedOperationException.class, () -> equipment.add(EQUIPMENT));
        assertThrows(UnsupportedOperationException.class, () -> loans.add(LOAN));
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(LOAN));

        modelManager.closeLoan(EQUIPMENT_ID, RETURNED_DATE);
        assertEquals(List.of(LOAN.withReturnedDate(RETURNED_DATE)), loans);
        assertTrue(LOAN.isOpen());
        assertTrue(modelManager.findOpenLoan(EQUIPMENT_ID).isEmpty());
        assertTrue(modelManager.findOpenLoansForMember(MEMBER_ID).isEmpty());
        assertEquals(List.of(LOAN), snapshot);
        assertEquals(Availability.AVAILABLE, modelManager.getAvailability(EQUIPMENT_ID));
        modelManager.deleteMember(MEMBER);
        assertTrue(members.isEmpty());
        assertTrue(loans.isEmpty());
        modelManager.deleteEquipment(EQUIPMENT);
        assertTrue(equipment.isEmpty());
    }

    @Test
    public void closeLoan_realRoot_unavailableConditionRestoredAfterReturn() {
        for (Condition condition : List.of(Condition.DAMAGED, Condition.UNDER_REPAIR)) {
            Model model = new ModelManager();
            model.addMember(MEMBER);
            model.addEquipment(new Equipment(EQUIPMENT_ID, "Camera", "Photography", condition, ""));
            assertEquals(Availability.UNAVAILABLE, model.getAvailability(EQUIPMENT_ID));
            model.addLoan(LOAN);
            assertEquals(Availability.ASSIGNED, model.getAvailability(EQUIPMENT_ID));
            model.closeLoan(EQUIPMENT_ID, ASSIGNED_DATE);
            assertEquals(Availability.UNAVAILABLE, model.getAvailability(EQUIPMENT_ID));
        }
    }

    @Test
    public void laplaceWrites_realRoot_rejectsInvalidWritesWithoutChangingState() {
        assertThrows(MemberNotFoundException.class, () -> modelManager.addLoan(LOAN));
        assertThrows(MemberNotFoundException.class, () -> modelManager.deleteMember(MEMBER));
        assertThrows(EquipmentNotFoundException.class, () -> modelManager.deleteEquipment(EQUIPMENT));
        assertThrows(OpenLoanNotFoundException.class, () -> modelManager.closeLoan(EQUIPMENT_ID, RETURNED_DATE));
        assertEquals(new AddressBook(), modelManager.getAddressBook());
        modelManager.addMember(MEMBER);
        AddressBook memberOnly = new AddressBook(modelManager.getAddressBook());
        assertThrows(EquipmentNotFoundException.class, () -> modelManager.addLoan(LOAN));
        assertEquals(memberOnly, modelManager.getAddressBook());
        modelManager.addEquipment(EQUIPMENT);
        modelManager.addLoan(LOAN);
        AddressBook before = new AddressBook(modelManager.getAddressBook());
        assertThrows(DuplicateMemberException.class, () -> modelManager.addMember(MEMBER));
        assertThrows(DuplicateEquipmentException.class, () -> modelManager.addEquipment(EQUIPMENT));
        assertThrows(DuplicateOpenLoanException.class, () -> modelManager.addLoan(LOAN));
        assertThrows(IllegalStateException.class, () -> modelManager.deleteMember(MEMBER));
        assertThrows(IllegalStateException.class, () -> modelManager.deleteEquipment(EQUIPMENT));
        assertThrows(IllegalArgumentException.class, () ->
                modelManager.closeLoan(EQUIPMENT_ID, ASSIGNED_DATE.minusDays(1)));
        assertEquals(before, modelManager.getAddressBook());
        assertEquals(Availability.ASSIGNED, modelManager.getAvailability(EQUIPMENT_ID));
    }

    @Test
    public void deleteEquipment_realRoot_removesClosedHistoryByIdentity() {
        modelManager.addMember(MEMBER);
        modelManager.addEquipment(EQUIPMENT);
        modelManager.addLoan(LOAN.withReturnedDate(RETURNED_DATE));
        modelManager.deleteEquipment(new Equipment(EQUIPMENT_ID, "Renamed", "Other", Condition.FAIR, "Updated"));
        assertFalse(modelManager.hasEquipment(EQUIPMENT_ID));
        assertTrue(modelManager.getLoanList().isEmpty());
        assertEquals(List.of(MEMBER), modelManager.getMemberList());
    }

    @Test
    public void constructor_realRoot_copiesEveryCollection() {
        AddressBook root = new AddressBook();
        root.addPerson(ALICE);
        root.addMember(MEMBER);
        root.addEquipment(EQUIPMENT);
        root.addLoan(LOAN);
        modelManager = new ModelManager(root, new UserPrefs());
        modelManager.closeLoan(EQUIPMENT_ID, RETURNED_DATE);
        modelManager.deleteMember(MEMBER);
        modelManager.deleteEquipment(EQUIPMENT);
        modelManager.deletePerson(ALICE);
        assertEquals(List.of(ALICE), root.getPersonList());
        assertEquals(List.of(MEMBER), root.getMemberList());
        assertEquals(List.of(EQUIPMENT), root.getEquipmentList());
        assertEquals(List.of(LOAN), root.getLoanList());
    }

    @Test
    public void setAddressBook_realRoot_updatesExistingViewsAndPreservesPersonFilter() {
        ObservableList<Member> members = modelManager.getMemberList();
        ObservableList<Equipment> equipment = modelManager.getEquipmentList();
        ObservableList<Loan> loans = modelManager.getLoanList();
        modelManager.updateFilteredPersonList(person -> person.equals(ALICE));
        AddressBook replacement = new AddressBook();
        replacement.addPerson(ALICE);
        replacement.addPerson(BENSON);
        replacement.addMember(MEMBER);
        replacement.addEquipment(EQUIPMENT);
        replacement.addLoan(LOAN);
        modelManager.setAddressBook(replacement);
        assertEquals(List.of(ALICE), modelManager.getFilteredPersonList());
        assertEquals(List.of(MEMBER), members);
        assertEquals(List.of(EQUIPMENT), equipment);
        assertEquals(List.of(LOAN), loans);
        modelManager.setAddressBook(new AddressBook());
        assertTrue(modelManager.getFilteredPersonList().isEmpty());
        assertTrue(members.isEmpty());
        assertTrue(equipment.isEmpty());
        assertTrue(loans.isEmpty());
    }

    @Test
    public void equals() {
        AddressBook addressBook = new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON).build();
        AddressBook differentAddressBook = new AddressBook();
        UserPrefs userPrefs = new UserPrefs();

        // same values -> returns true
        modelManager = new ModelManager(addressBook, userPrefs);
        ModelManager modelManagerCopy = new ModelManager(addressBook, userPrefs);
        assertTrue(modelManager.equals(modelManagerCopy));

        // same object -> returns true
        assertTrue(modelManager.equals(modelManager));

        // null -> returns false
        assertFalse(modelManager.equals(null));

        // different types -> returns false
        assertFalse(modelManager.equals(5));

        // different addressBook -> returns false
        assertFalse(modelManager.equals(new ModelManager(differentAddressBook, userPrefs)));

        // different filteredList -> returns false
        String[] keywords = ALICE.getName().fullName.split("\\s+");
        modelManager.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of(keywords)));
        assertFalse(modelManager.equals(new ModelManager(addressBook, userPrefs)));

        // resets modelManager to initial state for upcoming tests
        modelManager.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        // different userPrefs -> returns false
        UserPrefs differentUserPrefs = new UserPrefs();
        differentUserPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        assertFalse(modelManager.equals(new ModelManager(addressBook, differentUserPrefs)));
    }

    /**
     * Controlled root boundary: records exact calls and supplies results or failures without implementing storage.
     */
    private static class RecordingAddressBook extends AddressBook {
        private final List<String> calls = new ArrayList<>();
        private final List<List<Object>> arguments = new ArrayList<>();
        private final ObservableList<Member> memberList = FXCollections.observableArrayList(MEMBER);
        private final ObservableList<Equipment> equipmentList = FXCollections.observableArrayList(EQUIPMENT);
        private final ObservableList<Loan> loanList = FXCollections.observableArrayList(LOAN);
        private boolean memberExists = true;
        private boolean equipmentExists = true;
        private Optional<Member> memberResult = Optional.of(MEMBER);
        private Optional<Equipment> equipmentResult = Optional.of(EQUIPMENT);
        private Optional<Loan> loanResult = Optional.of(LOAN);
        private List<Loan> memberLoans = List.of(LOAN);
        private RuntimeException failure;

        private void record(String method, Object... args) {
            calls.add(method);
            arguments.add(Arrays.asList(args));
            if (failure != null) {
                throw failure;
            }
        }

        private void assertCall(String method, Object... args) {
            assertEquals(List.of(method), calls);
            assertEquals(args.length, arguments.get(0).size());
            for (int i = 0; i < args.length; i++) {
                assertSame(args[i], arguments.get(0).get(i));
            }
            calls.clear();
            arguments.clear();
        }

        private void assertAvailabilityCalls() {
            assertEquals(List.of("findEquipment", "findOpenLoan"), calls);
            assertEquals(List.of(List.of(EQUIPMENT_ID), List.of(EQUIPMENT_ID)), arguments);
            calls.clear();
            arguments.clear();
        }

        @Override
        public boolean hasMember(NusId nusId) {
            record("hasMember", nusId);
            return memberExists;
        }

        @Override
        public Optional<Member> findMember(NusId nusId) {
            record("findMember", nusId);
            return memberResult;
        }

        @Override
        public void addMember(Member member) {
            record("addMember", member);
        }

        @Override
        public void removeMember(Member member) {
            record("removeMember", member);
        }

        @Override
        public ObservableList<Member> getMemberList() {
            record("getMemberList");
            return memberList;
        }

        @Override
        public boolean hasEquipment(UUID uuid) {
            record("hasEquipment", uuid);
            return equipmentExists;
        }

        @Override
        public Optional<Equipment> findEquipment(UUID uuid) {
            record("findEquipment", uuid);
            return equipmentResult;
        }

        @Override
        public void addEquipment(Equipment equipment) {
            record("addEquipment", equipment);
        }

        @Override
        public void removeEquipment(Equipment equipment) {
            record("removeEquipment", equipment);
        }

        @Override
        public ObservableList<Equipment> getEquipmentList() {
            record("getEquipmentList");
            return equipmentList;
        }

        @Override
        public void addLoan(Loan loan) {
            record("addLoan", loan);
        }

        @Override
        public Optional<Loan> findOpenLoan(UUID equipmentUuid) {
            record("findOpenLoan", equipmentUuid);
            return loanResult;
        }

        @Override
        public List<Loan> findOpenLoansForMember(NusId nusId) {
            record("findOpenLoansForMember", nusId);
            return memberLoans;
        }

        @Override
        public void closeLoan(UUID equipmentUuid, LocalDate returnedDate) {
            record("closeLoan", equipmentUuid, returnedDate);
        }

        @Override
        public ObservableList<Loan> getLoanList() {
            record("getLoanList");
            return loanList;
        }
    }
}
