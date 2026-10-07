package seedu.address.model.loan;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.loan.exceptions.DuplicateOpenLoanException;
import seedu.address.model.loan.exceptions.OpenLoanNotFoundException;
import seedu.address.model.member.NusId;

/**
 * Stores loan history with at most one open loan per equipment UUID.
 * Member and equipment existence are validated by the root model, not this collection.
 */
public class UniqueLoanList {

    private final ObservableList<Loan> internalList = FXCollections.observableArrayList();
    private final ObservableList<Loan> internalUnmodifiableList =
            FXCollections.unmodifiableObservableList(internalList);

    /** Returns the open loan for the given equipment, if any. */
    public Optional<Loan> findOpenLoan(UUID equipmentUuid) {
        requireNonNull(equipmentUuid);
        return internalList.stream()
                .filter(loan -> loan.isOpen() && loan.getEquipmentUuid().equals(equipmentUuid)).findFirst();
    }

    /** Returns an unmodifiable snapshot of the member's open loans. */
    public List<Loan> findOpenLoansForMember(NusId nusId) {
        requireNonNull(nusId);
        return internalList.stream()
                .filter(loan -> loan.isOpen() && loan.getMemberNusId().equals(nusId)).toList();
    }

    /** Adds a loan, rejecting a second open loan for the same equipment. */
    public void add(Loan loan) {
        requireNonNull(loan);
        if (loan.isOpen() && findOpenLoan(loan.getEquipmentUuid()).isPresent()) {
            throw new DuplicateOpenLoanException();
        }
        internalList.add(loan);
    }

    /** Replaces the open loan with a validated closed copy, retaining its position in history. */
    public void closeLoan(UUID equipmentUuid, LocalDate returnedDate) {
        requireNonNull(equipmentUuid);
        requireNonNull(returnedDate);
        Loan open = findOpenLoan(equipmentUuid).orElseThrow(OpenLoanNotFoundException::new);
        Loan closed = open.withReturnedDate(returnedDate);
        internalList.set(internalList.indexOf(open), closed);
    }

    /** Validates and copies the entire replacement before changing the current collection. */
    public void setLoans(List<Loan> loans) {
        List<Loan> replacement = List.copyOf(requireNonNull(loans));
        Set<UUID> openEquipment = new HashSet<>();
        for (Loan loan : replacement) {
            if (loan.isOpen() && !openEquipment.add(loan.getEquipmentUuid())) {
                throw new DuplicateOpenLoanException();
            }
        }
        internalList.setAll(replacement);
    }

    /** Returns an unmodifiable live view, including changes made by bulk replacement. */
    public ObservableList<Loan> asUnmodifiableObservableList() {
        return internalUnmodifiableList;
    }
}
