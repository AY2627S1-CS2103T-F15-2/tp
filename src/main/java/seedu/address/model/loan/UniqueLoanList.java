package seedu.address.model.loan;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javafx.collections.ObservableList;
import seedu.address.model.member.NusId;

/**
 * Contract for the loan collection, which permits at most one open loan per equipment UUID.
 */
public class UniqueLoanList {

    public Optional<Loan> findOpenLoan(UUID equipmentUuid) {
        throw new UnsupportedOperationException("UniqueLoanList is not implemented");
    }

    public List<Loan> findOpenLoansForMember(NusId nusId) {
        throw new UnsupportedOperationException("UniqueLoanList is not implemented");
    }

    public void add(Loan loan) {
        throw new UnsupportedOperationException("UniqueLoanList is not implemented");
    }

    public void closeLoan(UUID equipmentUuid, LocalDate returnedDate) {
        throw new UnsupportedOperationException("UniqueLoanList is not implemented");
    }

    public void setLoans(List<Loan> loans) {
        throw new UnsupportedOperationException("UniqueLoanList is not implemented");
    }

    public ObservableList<Loan> asUnmodifiableObservableList() {
        throw new UnsupportedOperationException("UniqueLoanList is not implemented");
    }
}
