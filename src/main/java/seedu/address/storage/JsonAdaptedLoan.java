package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.UUID;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.loan.Loan;
import seedu.address.model.member.NusId;

/** Jackson-facing contract for a {@link Loan}. */
class JsonAdaptedLoan {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Loan's %s field is missing!";
    private static final Pattern STANDARD_IDENTIFIER_PATTERN = Pattern.compile(
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

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
        requireNonNull(source);
        equipmentUuid = source.getEquipmentUuid().toString();
        memberNusId = source.getMemberNusId().value;
        assignedDate = source.getAssignedDate().toString();
        expectedReturnDate = source.getExpectedReturnDate().toString();
        returnedDate = source.getReturnedDate().map(Object::toString).orElse(null);
    }

    /** Converts this adapter to its domain type. */
    public Loan toModelType() throws IllegalValueException {
        requireField(equipmentUuid, "equipmentUuid");
        requireField(memberNusId, "memberNusId");
        requireField(assignedDate, "assignedDate");
        requireField(expectedReturnDate, "expectedReturnDate");
        if (!STANDARD_IDENTIFIER_PATTERN.matcher(equipmentUuid).matches()) {
            throw new IllegalValueException("Loan equipmentUuid is invalid.");
        }
        if (!NusId.isValidNusId(memberNusId)) {
            throw new IllegalValueException(NusId.MESSAGE_CONSTRAINTS);
        }
        LocalDate modelAssignedDate = parseDate(assignedDate, "assignedDate");
        LocalDate modelExpectedDate = parseDate(expectedReturnDate, "expectedReturnDate");
        LocalDate modelReturnedDate = returnedDate == null ? null : parseDate(returnedDate, "returnedDate");
        try {
            return new Loan(UUID.fromString(equipmentUuid), new NusId(memberNusId), modelAssignedDate,
                    modelExpectedDate, modelReturnedDate);
        } catch (IllegalArgumentException exception) {
            throw new IllegalValueException(exception.getMessage(), exception);
        }
    }

    private static void requireField(String value, String field) throws IllegalValueException {
        if (value == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, field));
        }
    }

    private static LocalDate parseDate(String value, String field) throws IllegalValueException {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            throw new IllegalValueException("Loan " + field + " must be a valid ISO calendar date.", exception);
        }
    }
}
