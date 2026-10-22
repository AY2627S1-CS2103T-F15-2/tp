package seedu.address.model.loan;

import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.equipment.Condition;
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
    private final Condition issueCondition;
    private final Condition returnCondition;

    /**
     * Constructs a {@code Loan} with the given details.
     *
     * @param returnedDate The actual return date, or {@code null} for an open loan.
     */
    public Loan(UUID equipmentUuid, NusId memberNusId, LocalDate assignedDate,
            LocalDate expectedReturnDate, LocalDate returnedDate) {
        this(equipmentUuid, memberNusId, assignedDate, expectedReturnDate, returnedDate, null, null);
    }

    /** Creates a loan with recorded conditions; null denotes an unknown historical condition. */
    public Loan(UUID equipmentUuid, NusId memberNusId, LocalDate assignedDate,
            LocalDate expectedReturnDate, LocalDate returnedDate, Condition issueCondition, Condition returnCondition) {
        requireAllNonNull(equipmentUuid, memberNusId, assignedDate, expectedReturnDate);
        checkArgument(!expectedReturnDate.isBefore(assignedDate), MESSAGE_EXPECTED_DATE_CONSTRAINTS);
        checkArgument(returnedDate == null || !returnedDate.isBefore(assignedDate),
                MESSAGE_RETURNED_DATE_CONSTRAINTS);
        checkArgument(returnedDate != null || returnCondition == null,
                "An open loan cannot have a return condition");
        this.equipmentUuid = equipmentUuid;
        this.memberNusId = memberNusId;
        this.assignedDate = assignedDate;
        this.expectedReturnDate = expectedReturnDate;
        this.returnedDate = returnedDate;
        this.issueCondition = issueCondition;
        this.returnCondition = returnCondition;
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

    public Optional<Condition> getIssueCondition() {
        return Optional.ofNullable(issueCondition);
    }

    public Optional<Condition> getReturnCondition() {
        return Optional.ofNullable(returnCondition);
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
        return withReturnedDate(returnedDate, null);
    }

    /** Returns a closed copy with the actual return date and observed condition. */
    public Loan withReturnedDate(LocalDate returnedDate, Condition returnCondition) {
        requireAllNonNull(returnedDate);
        return new Loan(equipmentUuid, memberNusId, assignedDate, expectedReturnDate, returnedDate,
                issueCondition, returnCondition);
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
                && Objects.equals(returnedDate, otherLoan.returnedDate)
                && issueCondition == otherLoan.issueCondition && returnCondition == otherLoan.returnCondition;
    }

    @Override
    public int hashCode() {
        return Objects.hash(equipmentUuid, memberNusId, assignedDate, expectedReturnDate,
                returnedDate, issueCondition, returnCondition);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("equipmentUuid", equipmentUuid)
                .add("memberNusId", memberNusId)
                .add("assignedDate", assignedDate)
                .add("expectedReturnDate", expectedReturnDate)
                .add("returnedDate", returnedDate)
                .add("issueCondition", issueCondition)
                .add("returnCondition", returnCondition)
                .toString();
    }
}
