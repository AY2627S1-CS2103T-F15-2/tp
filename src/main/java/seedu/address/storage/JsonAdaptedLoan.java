package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.loan.Loan;

/** Jackson-facing contract for a {@link Loan}. */
class JsonAdaptedLoan {

    private final String equipmentUuid;
    private final String memberNusId;
    private final String assignedDate;
    private final String expectedReturnDate;
    private final String returnedDate;

    /** Constructs an adapter from raw JSON fields. */
    @JsonCreator
    public JsonAdaptedLoan(@JsonProperty("equipmentUuid") String equipmentUuid,
            @JsonProperty("memberNusId") String memberNusId,
            @JsonProperty("assignedDate") String assignedDate,
            @JsonProperty("expectedReturnDate") String expectedReturnDate,
            @JsonProperty("returnedDate") String returnedDate) {
        this.equipmentUuid = equipmentUuid;
        this.memberNusId = memberNusId;
        this.assignedDate = assignedDate;
        this.expectedReturnDate = expectedReturnDate;
        this.returnedDate = returnedDate;
    }

    /** Constructs an adapter from a domain loan. */
    public JsonAdaptedLoan(Loan source) {
        equipmentUuid = source.getEquipmentUuid().toString();
        memberNusId = source.getMemberNusId().value;
        assignedDate = source.getAssignedDate().toString();
        expectedReturnDate = source.getExpectedReturnDate().toString();
        returnedDate = source.getReturnedDate().map(Object::toString).orElse(null);
    }

    /** Converts this adapter to its domain type. */
    public Loan toModelType() throws IllegalValueException {
        throw new UnsupportedOperationException("Loan JSON conversion is not implemented");
    }
}
