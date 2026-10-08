package seedu.address.model.loan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import seedu.address.model.loan.exceptions.DuplicateOpenLoanException;
import seedu.address.model.loan.exceptions.OpenLoanNotFoundException;
import seedu.address.model.member.NusId;

public class UniqueLoanListTest {

    private static final UUID EQUIPMENT_UUID =
            UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private static final UUID OTHER_UUID =
            UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
    private static final NusId MEMBER_NUS_ID = new NusId("A0123456X");
    private static final NusId OTHER_MEMBER = new NusId("A0765432X");
    private static final LocalDate ASSIGNED_DATE = LocalDate.of(2026, 10, 6);
    private static final Loan OPEN = new Loan(EQUIPMENT_UUID, MEMBER_NUS_ID, ASSIGNED_DATE,
            ASSIGNED_DATE.plusDays(7), null);
    private static final Loan CLOSED = OPEN.withReturnedDate(ASSIGNED_DATE.plusDays(1));
    private static final Loan OTHER_OPEN = new Loan(OTHER_UUID, MEMBER_NUS_ID, ASSIGNED_DATE,
            ASSIGNED_DATE.plusDays(7), null);

    private final UniqueLoanList loans = new UniqueLoanList();

    @Test
    public void operations_nullArguments_rejectWithoutMutation() {
        loans.add(OPEN);
        assertThrows(NullPointerException.class, () -> loans.findOpenLoan(null));
        assertThrows(NullPointerException.class, () -> loans.findOpenLoansForMember(null));
        assertThrows(NullPointerException.class, () -> loans.add(null));
        assertThrows(NullPointerException.class, () -> loans.closeLoan(null, ASSIGNED_DATE));
        assertThrows(NullPointerException.class, () -> loans.closeLoan(EQUIPMENT_UUID, null));
        assertThrows(NullPointerException.class, () -> loans.setLoans(null));
        assertThrows(NullPointerException.class, () -> loans.setLoans(Arrays.asList(CLOSED, null)));
        assertEquals(List.of(OPEN), loans.asUnmodifiableObservableList());
    }

    @Test
    public void findOpenLoan_emptyOrClosedOnly_returnsEmpty() {
        assertEquals(Optional.empty(), loans.findOpenLoan(EQUIPMENT_UUID));
        loans.add(CLOSED);
        assertEquals(Optional.empty(), loans.findOpenLoan(EQUIPMENT_UUID));
        loans.add(OPEN);
        assertEquals(Optional.of(OPEN), loans.findOpenLoan(EQUIPMENT_UUID));
        assertEquals(Optional.empty(), loans.findOpenLoan(OTHER_UUID));
    }

    @Test
    public void add_duplicateOpenWithDifferentDetails_rejectsWithoutMutation() {
        loans.add(OPEN);
        Loan duplicate = new Loan(EQUIPMENT_UUID, OTHER_MEMBER, ASSIGNED_DATE.plusDays(1),
                ASSIGNED_DATE.plusDays(8), null);
        assertThrows(DuplicateOpenLoanException.class, () -> loans.add(OPEN));
        assertThrows(DuplicateOpenLoanException.class, () -> loans.add(duplicate));
        assertEquals(List.of(OPEN), loans.asUnmodifiableObservableList());
    }

    @Test
    public void add_closedHistoryAndDifferentEquipment_acceptsWithoutRootDependencies() {
        loans.add(CLOSED);
        loans.add(CLOSED);
        loans.add(OPEN);
        loans.add(OTHER_OPEN);
        loans.add(CLOSED);
        assertEquals(List.of(CLOSED, CLOSED, OPEN, OTHER_OPEN, CLOSED), loans.asUnmodifiableObservableList());
    }

    @Test
    public void findOpenLoansForMember_normalizedIdentity_returnsProtectedSnapshot() {
        loans.setLoans(List.of(CLOSED, OPEN, OTHER_OPEN));
        List<Loan> snapshot = loans.findOpenLoansForMember(new NusId("a0123456x"));
        assertEquals(List.of(OPEN, OTHER_OPEN), snapshot);
        assertTrue(loans.findOpenLoansForMember(OTHER_MEMBER).isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(CLOSED));
        loans.closeLoan(EQUIPMENT_UUID, ASSIGNED_DATE);
        assertEquals(List.of(OPEN, OTHER_OPEN), snapshot);
        assertEquals(List.of(OTHER_OPEN), loans.findOpenLoansForMember(MEMBER_NUS_ID));
    }

    @Test
    public void closeLoan_validDates_replacesInPlaceAndAllowsAnotherOpenLoan() {
        for (LocalDate date : List.of(ASSIGNED_DATE, ASSIGNED_DATE.plusDays(1), ASSIGNED_DATE.plusDays(10))) {
            loans.setLoans(List.of(CLOSED, OPEN, OTHER_OPEN));
            loans.closeLoan(EQUIPMENT_UUID, date);
            assertEquals(List.of(CLOSED, OPEN.withReturnedDate(date), OTHER_OPEN),
                    loans.asUnmodifiableObservableList());
            assertTrue(OPEN.isOpen());
            assertEquals(Optional.empty(), loans.findOpenLoan(EQUIPMENT_UUID));
            loans.add(OPEN);
            assertEquals(Optional.of(OPEN), loans.findOpenLoan(EQUIPMENT_UUID));
        }
    }

    @Test
    public void closeLoan_missingOrInvalidDate_leavesHistoryUnchanged() {
        assertThrows(OpenLoanNotFoundException.class, () -> loans.closeLoan(EQUIPMENT_UUID, ASSIGNED_DATE));
        loans.setLoans(List.of(CLOSED, OPEN));
        assertThrows(OpenLoanNotFoundException.class, () -> loans.closeLoan(OTHER_UUID, ASSIGNED_DATE));
        assertThrows(IllegalArgumentException.class, () -> loans.closeLoan(EQUIPMENT_UUID, ASSIGNED_DATE.minusDays(1)));
        assertEquals(List.of(CLOSED, OPEN), loans.asUnmodifiableObservableList());
        loans.closeLoan(EQUIPMENT_UUID, ASSIGNED_DATE);
        List<Loan> before = List.copyOf(loans.asUnmodifiableObservableList());
        assertThrows(OpenLoanNotFoundException.class, () -> loans.closeLoan(EQUIPMENT_UUID, ASSIGNED_DATE));
        assertEquals(before, loans.asUnmodifiableObservableList());
    }

    @Test
    public void setLoans_duplicateOpen_isAtomicAndDoesNotNotify() {
        loans.add(OTHER_OPEN);
        int[] notifications = {0};
        loans.asUnmodifiableObservableList().addListener((ListChangeListener<Loan>) change -> notifications[0]++);
        Loan duplicate = new Loan(EQUIPMENT_UUID, OTHER_MEMBER, ASSIGNED_DATE, ASSIGNED_DATE, null);
        assertThrows(DuplicateOpenLoanException.class, () -> loans.setLoans(List.of(CLOSED, OPEN, duplicate)));
        assertEquals(List.of(OTHER_OPEN), loans.asUnmodifiableObservableList());
        assertEquals(0, notifications[0]);
    }

    @Test
    public void setLoans_copiesInputSupportsSelfReplacementAndClearing() {
        List<Loan> input = new ArrayList<>(List.of(CLOSED, CLOSED, OPEN));
        loans.setLoans(input);
        input.clear();
        assertEquals(List.of(CLOSED, CLOSED, OPEN), loans.asUnmodifiableObservableList());
        loans.setLoans(loans.asUnmodifiableObservableList());
        assertEquals(List.of(CLOSED, CLOSED, OPEN), loans.asUnmodifiableObservableList());
        loans.setLoans(List.of());
        assertTrue(loans.asUnmodifiableObservableList().isEmpty());
    }

    @Test
    public void observableView_protectedAndLiveAcrossAllWrites() {
        ObservableList<Loan> view = loans.asUnmodifiableObservableList();
        int[] notifications = {0};
        view.addListener((ListChangeListener<Loan>) change -> notifications[0]++);
        loans.add(OPEN);
        assertEquals(List.of(OPEN), view);
        assertTrue(notifications[0] > 0);
        notifications[0] = 0;
        loans.closeLoan(EQUIPMENT_UUID, ASSIGNED_DATE);
        assertEquals(List.of(OPEN.withReturnedDate(ASSIGNED_DATE)), view);
        assertTrue(notifications[0] > 0);
        notifications[0] = 0;
        loans.setLoans(List.of(OTHER_OPEN));
        assertEquals(List.of(OTHER_OPEN), view);
        assertTrue(notifications[0] > 0);
        assertThrows(UnsupportedOperationException.class, () -> view.add(CLOSED));
        assertThrows(UnsupportedOperationException.class, () -> view.set(0, CLOSED));
        assertThrows(UnsupportedOperationException.class, () -> view.remove(0));
        assertThrows(UnsupportedOperationException.class, view::clear);
        assertFalse(view.isEmpty());
    }
}
