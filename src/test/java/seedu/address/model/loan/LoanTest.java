package seedu.address.model.loan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import seedu.address.model.member.NusId;

public class LoanTest {

    private static final UUID UUID_ONE = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private static final UUID UUID_TWO = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
    private static final NusId NUS_ID = new NusId("A0123456X");
    private static final LocalDate ASSIGNED_DATE = LocalDate.of(2026, 10, 1);
    private static final LocalDate EXPECTED_DATE = LocalDate.of(2026, 10, 15);
    private static final LocalDate RETURNED_DATE = LocalDate.of(2026, 10, 12);

    @Test
    public void constructor_nullRequiredField_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                new Loan(null, NUS_ID, ASSIGNED_DATE, EXPECTED_DATE, null));
        assertThrows(NullPointerException.class, () ->
                new Loan(UUID_ONE, null, ASSIGNED_DATE, EXPECTED_DATE, null));
        assertThrows(NullPointerException.class, () ->
                new Loan(UUID_ONE, NUS_ID, null, EXPECTED_DATE, null));
        assertThrows(NullPointerException.class, () ->
                new Loan(UUID_ONE, NUS_ID, ASSIGNED_DATE, null, null));
    }

    @Test
    public void openLoan_gettersReturnConstructorValues() {
        Loan loan = new Loan(UUID_ONE, NUS_ID, ASSIGNED_DATE, EXPECTED_DATE, null);

        assertEquals(UUID_ONE, loan.getEquipmentUuid());
        assertEquals(NUS_ID, loan.getMemberNusId());
        assertEquals(ASSIGNED_DATE, loan.getAssignedDate());
        assertEquals(EXPECTED_DATE, loan.getExpectedReturnDate());
        assertEquals(Optional.empty(), loan.getReturnedDate());
        assertTrue(loan.isOpen());
    }

    @Test
    public void closedLoan_returnedDatePresentAndIsNotOpen() {
        Loan loan = new Loan(UUID_ONE, NUS_ID, ASSIGNED_DATE, EXPECTED_DATE, RETURNED_DATE);

        assertEquals(Optional.of(RETURNED_DATE), loan.getReturnedDate());
        assertFalse(loan.isOpen());
    }

    @Test
    public void constructor_expectedDateBeforeAssignedDate_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Loan(UUID_ONE, NUS_ID, ASSIGNED_DATE, ASSIGNED_DATE.minusDays(1), null));
    }

    @Test
    public void constructor_returnedDateBeforeAssignedDate_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Loan(UUID_ONE, NUS_ID, ASSIGNED_DATE, EXPECTED_DATE, ASSIGNED_DATE.minusDays(1)));
    }

    @Test
    public void withReturnedDate_returnsClosedCopyAndLeavesOriginalOpen() {
        Loan loan = new Loan(UUID_ONE, NUS_ID, ASSIGNED_DATE, EXPECTED_DATE, null);

        Loan closedLoan = loan.withReturnedDate(RETURNED_DATE);

        assertTrue(loan.isOpen());
        assertFalse(closedLoan.isOpen());
        assertEquals(Optional.of(RETURNED_DATE), closedLoan.getReturnedDate());
        assertEquals(UUID_ONE, closedLoan.getEquipmentUuid());
        assertEquals(NUS_ID, closedLoan.getMemberNusId());
        assertEquals(ASSIGNED_DATE, closedLoan.getAssignedDate());
        assertEquals(EXPECTED_DATE, closedLoan.getExpectedReturnDate());
    }

    @Test
    public void constructor_sameDayExpectedAndSameDayEarlyLateReturns_acceptsBoundaries() {
        Loan sameDay = new Loan(UUID_ONE, NUS_ID, ASSIGNED_DATE, ASSIGNED_DATE, ASSIGNED_DATE);
        assertEquals(Optional.of(ASSIGNED_DATE), sameDay.getReturnedDate());
        for (LocalDate date : List.of(ASSIGNED_DATE, EXPECTED_DATE.minusDays(1),
                EXPECTED_DATE, EXPECTED_DATE.plusDays(1))) {
            Loan loan = new Loan(UUID_ONE, NUS_ID, ASSIGNED_DATE, EXPECTED_DATE, date);
            assertEquals(Optional.of(date), loan.getReturnedDate());
        }
    }

    @Test
    public void withReturnedDate_nullOrBeforeAssignment_rejectsAndLeavesOriginalOpen() {
        Loan loan = new Loan(UUID_ONE, NUS_ID, ASSIGNED_DATE, EXPECTED_DATE, null);
        assertThrows(NullPointerException.class, () -> loan.withReturnedDate(null));
        assertThrows(IllegalArgumentException.class, () -> loan.withReturnedDate(ASSIGNED_DATE.minusDays(1)));
        assertTrue(loan.isOpen());
    }

    @Test
    public void equals() {
        Loan loan = new Loan(UUID_ONE, NUS_ID, ASSIGNED_DATE, EXPECTED_DATE, null);
        Loan copy = new Loan(UUID_ONE, new NusId("a0123456x"), ASSIGNED_DATE, EXPECTED_DATE, null);

        assertTrue(loan.equals(loan));
        assertTrue(loan.equals(copy));
        assertFalse(loan.equals(null));
        assertFalse(loan.equals("loan"));
        assertFalse(loan.equals(new Loan(UUID_TWO, NUS_ID, ASSIGNED_DATE, EXPECTED_DATE, null)));
        assertFalse(loan.equals(new Loan(UUID_ONE, new NusId("A7654321X"),
                ASSIGNED_DATE, EXPECTED_DATE, null)));
        assertFalse(loan.equals(new Loan(UUID_ONE, NUS_ID,
                ASSIGNED_DATE.minusDays(1), EXPECTED_DATE, null)));
        assertFalse(loan.equals(new Loan(UUID_ONE, NUS_ID,
                ASSIGNED_DATE, EXPECTED_DATE.plusDays(1), null)));
        assertFalse(loan.equals(new Loan(UUID_ONE, NUS_ID,
                ASSIGNED_DATE, EXPECTED_DATE, RETURNED_DATE)));
        assertEquals(loan.hashCode(), copy.hashCode());
    }

    @Test
    public void toStringMethod() {
        Loan loan = new Loan(UUID_ONE, NUS_ID, ASSIGNED_DATE, EXPECTED_DATE, RETURNED_DATE);
        String expected = Loan.class.getCanonicalName() + "{equipmentUuid=" + UUID_ONE
                + ", memberNusId=A0123456X, assignedDate=2026-10-01, expectedReturnDate=2026-10-15"
                + ", returnedDate=2026-10-12}";

        assertEquals(expected, loan.toString());
    }
}
