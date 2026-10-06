package seedu.address.model.loan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import seedu.address.model.loan.exceptions.DuplicateOpenLoanException;
import seedu.address.model.loan.exceptions.OpenLoanNotFoundException;
import seedu.address.model.member.NusId;

public class UniqueLoanListTest {

    private static final UUID EQUIPMENT_UUID =
            UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private static final NusId MEMBER_NUS_ID = new NusId("A0123456X");
    private static final LocalDate ASSIGNED_DATE = LocalDate.of(2026, 10, 6);
    private static final Loan LOAN = new Loan(EQUIPMENT_UUID, MEMBER_NUS_ID, ASSIGNED_DATE,
            ASSIGNED_DATE.plusDays(7), null);

    @Test
    public void scaffoldOperations_throwUnsupportedOperationException() {
        UniqueLoanList loans = new UniqueLoanList();

        assertThrows(UnsupportedOperationException.class, () -> loans.findOpenLoan(EQUIPMENT_UUID));
        assertThrows(UnsupportedOperationException.class, () -> loans.findOpenLoansForMember(MEMBER_NUS_ID));
        assertThrows(UnsupportedOperationException.class, () -> loans.add(LOAN));
        assertThrows(UnsupportedOperationException.class, () ->
                loans.closeLoan(EQUIPMENT_UUID, ASSIGNED_DATE.plusDays(1)));
        assertThrows(UnsupportedOperationException.class, () -> loans.setLoans(List.of(LOAN)));
        assertThrows(UnsupportedOperationException.class, loans::asUnmodifiableObservableList);
    }

    @Test
    public void scaffoldExceptions_haveExpectedMessages() {
        assertEquals("Equipment already has an open loan",
                new DuplicateOpenLoanException().getMessage());
        assertNull(new OpenLoanNotFoundException().getMessage());
    }
}
