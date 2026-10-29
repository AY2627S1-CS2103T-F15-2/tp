package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.UUID;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.equipment.Condition;
import seedu.address.model.loan.Loan;
import seedu.address.model.member.NusId;

/** Jackson-facing contract for a {@link Loan}. */
class JsonAdaptedLoan {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Loan's %s field is missing!";
    private static final Pattern STANDARD_IDENTIFIER_PATTERN = Pattern.compile(
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
    private static final Pattern DATE_PATTERN = Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}");

    private final String equipmentUuid;
    private final String memberNusId;
    private final String assignedDate;
    private final String expectedReturnDate;
    private final String returnedDate;
    private final String issueCondition;
    private final String returnCondition;

    /** Constructs an adapter from raw JSON fields. */
    @JsonCreator
    public JsonAdaptedLoan(@JsonProperty("equipmentUuid") String equipmentUuid,
            @JsonProperty("memberNusId") String memberNusId,
            @JsonProperty("assignedDate") String assignedDate,
            @JsonProperty("expectedReturnDate") String expectedReturnDate,
            @JsonProperty("returnedDate") String returnedDate,
            @JsonProperty("issueCondition") String issueCondition,
            @JsonProperty("returnCondition") String returnCondition) {
        this.equipmentUuid = equipmentUuid;
        this.memberNusId = memberNusId;
        this.assignedDate = assignedDate;
        this.expectedReturnDate = expectedReturnDate;
        this.returnedDate = returnedDate;
        this.issueCondition = issueCondition;
        this.returnCondition = returnCondition;
    }

    /** Preserves the five-field adapter constructor used by existing callers and tests. */
    public JsonAdaptedLoan(String equipmentUuid, String memberNusId, String assignedDate,
            String expectedReturnDate, String returnedDate) {
        this(equipmentUuid, memberNusId, assignedDate, expectedReturnDate, returnedDate, null, null);
    }

    /** Constructs an adapter from a domain loan. */
    public JsonAdaptedLoan(Loan source) {
        requireNonNull(source);
        equipmentUuid = source.getEquipmentUuid().toString();
        memberNusId = source.getMemberNusId().value;
        assignedDate = source.getAssignedDate().toString();
        expectedReturnDate = source.getExpectedReturnDate().toString();
        returnedDate = source.getReturnedDate().map(Object::toString).orElse(null);
        issueCondition = source.getIssueCondition().map(Enum::name).orElse(null);
        returnCondition = source.getReturnCondition().map(Enum::name).orElse(null);
    }

    /**
     * Rejects unrecognized loan fields instead of silently losing lifecycle information on save.
     * Older records may omit optional fields, but extensions require a reader that understands them.
     */
    @JsonAnySetter
    private void rejectUnknownField(String fieldName, Object value) {
        throw new IllegalArgumentException("Unknown loan field '" + fieldName + "'. Supported fields: "
                + "equipmentUuid, memberNusId, assignedDate, expectedReturnDate, returnedDate, "
                + "issueCondition, returnCondition.");
    }

    /**
     * Returns stored identities for diagnostics even when value conversion fails.
     */
    String describeIdentity() {
        return "equipment " + equipmentUuid + ", member " + memberNusId;
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
                    modelExpectedDate, modelReturnedDate, parseCondition(issueCondition),
                    parseCondition(returnCondition));
        } catch (IllegalArgumentException exception) {
            throw new IllegalValueException(exception.getMessage(), exception);
        }
    }

    private static void requireField(String value, String field) throws IllegalValueException {
        if (value == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, field));
        }
    }

    /**
     * Parses a stored Gregorian date without accepting proleptic or extended ISO years.
     */
    private static LocalDate parseDate(String value, String field) throws IllegalValueException {
        String message = "Loan " + field + " '" + value
                + "' must be a valid yyyy-MM-dd date with a year from 0001 through 9999.";
        if (!DATE_PATTERN.matcher(value).matches()) {
            throw new IllegalValueException(message);
        }
        try {
            LocalDate date = LocalDate.parse(value);
            if (date.getYear() == 0) {
                throw new IllegalValueException(message);
            }
            return date;
        } catch (DateTimeParseException exception) {
            throw new IllegalValueException(message, exception);
        }
    }

    private static Condition parseCondition(String value) throws IllegalValueException {
        if (value == null) {
            return null;
        }
        try {
            return Condition.fromText(value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalValueException("Loan condition is invalid: " + value, exception);
        }
    }
}
