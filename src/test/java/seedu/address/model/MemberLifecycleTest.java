package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalMembers.ALEX;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.loan.Loan;
import seedu.address.model.member.Member;
import seedu.address.model.member.UniqueMemberList;
import seedu.address.model.member.exceptions.DuplicateMemberException;
import seedu.address.model.member.exceptions.MemberDeletedException;
import seedu.address.model.member.exceptions.MemberNotFoundException;

/**
 * Verifies retained identity, history and root-model eligibility across deletion and restoration.
 */
public class MemberLifecycleTest {
    private static final UUID UUID_CAMERA = new UUID(0, 1);
    private static final LocalDate DATE = LocalDate.of(2000, 1, 1);
    private static final Equipment CAMERA = new Equipment(UUID_CAMERA, "Camera", "Photography", Condition.GOOD, "");
    private static final Loan CLOSED = new Loan(UUID_CAMERA, ALEX.getNusId(), DATE, DATE.plusDays(1), DATE);
    private static final Loan OPEN = new Loan(UUID_CAMERA, ALEX.getNusId(), DATE, DATE.plusDays(1), null);

    @Test
    public void deleteAndRestore_retainedIdentityAndHistory_remainsRecoverableWithoutClock() {
        ModelManager model = model();
        ObservableList<Member> view = model.getMemberList();
        model.deleteMember(ALEX);
        Member deleted = model.findMember(ALEX.getNusId()).orElseThrow();
        assertTrue(deleted.isDeleted());
        assertTrue(deleted.isSameMember(ALEX));
        assertNotEquals(ALEX, deleted);
        assertEquals(List.of(CLOSED), model.getLoanList());
        assertEquals(List.of(deleted), view);
        assertThrows(DuplicateMemberException.class, () -> model.addMember(ALEX));
        assertThrows(MemberDeletedException.class, () -> model.addLoan(OPEN));
        assertThrows(MemberDeletedException.class, () -> model.deleteMember(ALEX));
        ModelManager reloaded = new ModelManager(model.getAddressBook(), new UserPrefs());
        reloaded.restoreMember(ALEX);
        assertEquals(ALEX, reloaded.findMember(ALEX.getNusId()).orElseThrow());
        assertEquals(List.of(CLOSED), reloaded.getLoanList());
        reloaded.addLoan(OPEN);
        assertEquals(List.of(CLOSED, OPEN), reloaded.getLoanList());
    }

    @Test
    public void lifecycle_invalidRequests_preservesRootAndRecords() {
        ModelManager model = model();
        AddressBook before = new AddressBook(model.getAddressBook());
        assertThrows(IllegalStateException.class, () -> model.restoreMember(ALEX));
        assertThrows(NullPointerException.class, () -> model.restoreMember(null));
        assertThrows(MemberNotFoundException.class, () -> new ModelManager().restoreMember(ALEX));
        assertEquals(before, model.getAddressBook());
        model.addLoan(OPEN);
        AddressBook holding = new AddressBook(model.getAddressBook());
        assertThrows(IllegalStateException.class, () -> model.deleteMember(ALEX));
        assertEquals(holding, model.getAddressBook());
        assertFalse(model.findMember(ALEX.getNusId()).orElseThrow().isDeleted());
    }

    @Test
    public void resetData_deletedMemberWithOpenLoan_rejectsBeforePublication() {
        AddressBook invalid = new AddressBook() {
            @Override
            public ObservableList<Loan> getLoanList() {
                return FXCollections.observableArrayList(OPEN);
            }
        };
        invalid.addMember(new Member(ALEX.getNusId(), ALEX.getName(), ALEX.getPhone(), ALEX.getEmail(), true));
        invalid.addEquipment(CAMERA);
        ModelManager model = model();
        AddressBook before = new AddressBook(model.getAddressBook());
        assertThrows(MemberDeletedException.class, () -> model.setAddressBook(invalid));
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void collection_missingAndNullIdentity_rejectsStateReplacement() {
        UniqueMemberList members = new UniqueMemberList();
        assertThrows(NullPointerException.class, () -> members.setDeleted(null, true));
        assertThrows(MemberNotFoundException.class, () -> members.setDeleted(ALEX.getNusId(), true));
    }

    private ModelManager model() {
        ModelManager model = new ModelManager();
        model.addMember(ALEX);
        model.addEquipment(CAMERA);
        model.addLoan(CLOSED);
        return model;
    }
}
