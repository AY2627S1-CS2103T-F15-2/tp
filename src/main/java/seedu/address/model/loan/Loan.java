package seedu.address.model.loan;

import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.member.NusId;

/**
 * Represents an equipment loan in Laplace.
 * A loan is open while its returned date is absent.
 */
public class Loan {

    public static final String MESSAGE_EXPECTED_DATE_CONSTRAINTS =
            "Expected return date cannot be before assigned date";
    public static final String MESSAGE_RETURNED_DATE_CONSTRAINTS =
            "Returned date cannot be before assigned date";

    private final UUID equipmentUuid;
    private final NusId memberNusId;
    private final LocalDate assignedDate;
    private final LocalDate expectedReturnDate;
    private final LocalDate returnedDate;

    /**
     * Constructs a {@code Loan} with the given details.
     *
     * @param returnedDate The actual return date, or {@code null} for an open loan.
     */
    public Loan(UUID equipmentUuid, NusId memberNusId, LocalDate assignedDate,
            LocalDate expectedReturnDate, LocalDate returnedDate) {
        requireAllNonNull(equipmentUuid, memberNusId, assignedDate, expectedReturnDate);
        checkArgument(!expectedReturnDate.isBefore(assignedDate), MESSAGE_EXPECTED_DATE_CONSTRAINTS);
        checkArgument(returnedDate == null || !returnedDate.isBefore(assignedDate),
                MESSAGE_RETURNED_DATE_CONSTRAINTS);
        this.equipmentUuid = equipmentUuid;
        this.memberNusId = memberNusId;
        this.assignedDate = assignedDate;
        this.expectedReturnDate = expectedReturnDate;
        this.returnedDate = returnedDate;
    }

    public UUID getEquipmentUuid() {
        return equipmentUuid;
    }

    public NusId getMemberNusId() {
        return memberNusId;
    }

    public LocalDate getAssignedDate() {
        return assignedDate;
    }

    public LocalDate getExpectedReturnDate() {
        return expectedReturnDate;
    }

    public Optional<LocalDate> getReturnedDate() {
        return Optional.ofNullable(returnedDate);
    }

    /**
     * Returns true if this loan has not been returned.
     */
    public boolean isOpen() {
        return returnedDate == null;
    }

    /**
     * Returns a closed copy of this loan with the given actual return date.
     */
    public Loan withReturnedDate(LocalDate returnedDate) {
        requireAllNonNull(returnedDate);
        return new Loan(equipmentUuid, memberNusId, assignedDate, expectedReturnDate, returnedDate);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Loan otherLoan)) {
            return false;
        }
        return equipmentUuid.equals(otherLoan.equipmentUuid)
                && memberNusId.equals(otherLoan.memberNusId)
                && assignedDate.equals(otherLoan.assignedDate)
                && expectedReturnDate.equals(otherLoan.expectedReturnDate)
                && Objects.equals(returnedDate, otherLoan.returnedDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(equipmentUuid, memberNusId, assignedDate, expectedReturnDate, returnedDate);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("equipmentUuid", equipmentUuid)
                .add("memberNusId", memberNusId)
                .add("assignedDate", assignedDate)
                .add("expectedReturnDate", expectedReturnDate)
                .add("returnedDate", returnedDate)
                .toString();
    }
}
