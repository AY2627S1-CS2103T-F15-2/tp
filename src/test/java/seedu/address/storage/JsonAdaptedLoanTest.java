package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.loan.Loan;
import seedu.address.model.member.NusId;

public class JsonAdaptedLoanTest {

    private static final String EQUIPMENT_UUID = "550e8400-e29b-41d4-a716-446655440000";
    private static final String MEMBER_ID = "A0123456X";
    private static final String ASSIGNED = "2026-10-06";
    private static final String EXPECTED = "2026-10-13";
    private static final String[] FIELD_NAMES = {
        "equipmentUuid", "memberNusId", "assignedDate", "expectedReturnDate", "returnedDate"
    };
    private static final Loan OPEN = new Loan(UUID.fromString(EQUIPMENT_UUID), new NusId(MEMBER_ID),
            LocalDate.parse(ASSIGNED), LocalDate.parse(EXPECTED), null);

    @Test
    public void toModelType_validRawFields_restoresOpenAndClosedLoans() throws Exception {
        assertEquals(OPEN, adapter((String) null).toModelType());
        for (String date : List.of(ASSIGNED, "2026-10-07", EXPECTED, "2026-10-20")) {
            assertEquals(OPEN.withReturnedDate(LocalDate.parse(date)), adapter(date).toModelType());
        }
        assertEquals(OPEN, new JsonAdaptedLoan(EQUIPMENT_UUID, "a0123456x", ASSIGNED, EXPECTED, null).toModelType());
        Loan sameDay = new Loan(OPEN.getEquipmentUuid(), OPEN.getMemberNusId(),
                OPEN.getAssignedDate(), OPEN.getAssignedDate(), OPEN.getAssignedDate());
        assertEquals(sameDay, new JsonAdaptedLoan(EQUIPMENT_UUID, MEMBER_ID, ASSIGNED, ASSIGNED,
                ASSIGNED).toModelType());
    }

    @Test
    public void constructor_nullSource_rejects() {
        assertThrows(NullPointerException.class, () -> new JsonAdaptedLoan((Loan) null));
    }

    @Test
    public void toModelType_missingRequiredFields_reportsField() {
        for (int index = 0; index < 4; index++) {
            String[] fields = validFields();
            fields[index] = null;
            JsonAdaptedLoan invalid = adapter(fields);
            assertThrows(IllegalValueException.class,
                    String.format(JsonAdaptedLoan.MISSING_FIELD_MESSAGE_FORMAT, FIELD_NAMES[index]),
                    invalid::toModelType);
        }
    }

    @Test
    public void toModelType_malformedIdentifiers_rejects() {
        for (String uuid : List.of("", "not-a-uuid", "1-1-1-1-1", " " + EQUIPMENT_UUID,
                "550e8400-e29b-41d4-a716-44665544000g")) {
            String[] fields = validFields();
            fields[0] = uuid;
            assertThrows(IllegalValueException.class, adapter(fields)::toModelType);
        }
        for (String member : List.of("", "invalid", " A0123456X", "A0123456X ")) {
            String[] fields = validFields();
            fields[1] = member;
            assertThrows(IllegalValueException.class, adapter(fields)::toModelType);
        }
    }

    @Test
    public void toModelType_malformedDatesInEveryDateField_rejects() {
        for (int index = 2; index < FIELD_NAMES.length; index++) {
            for (String date : List.of("", "2026-02-29", "2026-04-31", "06/10/2026",
                    "2026-1-06", "2026-10-06T00:00:00", " 2026-10-06")) {
                String[] fields = validFields();
                fields[index] = date;
                assertThrows(IllegalValueException.class, adapter(fields)::toModelType);
            }
        }
    }

    @Test
    public void toModelType_invalidDateRelationships_usesLoanValidationMessages() {
        JsonAdaptedLoan invalidExpected = new JsonAdaptedLoan(EQUIPMENT_UUID, MEMBER_ID,
                ASSIGNED, "2026-10-05", null);
        assertThrows(IllegalValueException.class, Loan.MESSAGE_EXPECTED_DATE_CONSTRAINTS,
                invalidExpected::toModelType);
        assertThrows(IllegalValueException.class, Loan.MESSAGE_RETURNED_DATE_CONSTRAINTS,
                adapter("2026-10-05")::toModelType);
    }

    @Test
    public void toModelType_leapDay_acceptsValidCalendarDate() throws Exception {
        Loan leapDay = new Loan(OPEN.getEquipmentUuid(), OPEN.getMemberNusId(),
                LocalDate.of(2028, 2, 29), LocalDate.of(2028, 3, 1), null);
        assertEquals(leapDay, new JsonAdaptedLoan(EQUIPMENT_UUID, MEMBER_ID,
                "2028-02-29", "2028-03-01", null).toModelType());
    }

    @Test
    public void jsonRoundTrip_openAndClosedLoans_preservesExactlyFiveFields() throws Exception {
        for (Loan loan : List.of(OPEN, OPEN.withReturnedDate(LocalDate.parse(EXPECTED)))) {
            String json = JsonUtil.toJsonString(new JsonAdaptedLoan(loan));
            JsonNode fields = new ObjectMapper().readTree(json);
            assertEquals(5, fields.size());
            assertEquals(EQUIPMENT_UUID, fields.get("equipmentUuid").asText());
            assertEquals(MEMBER_ID, fields.get("memberNusId").asText());
            assertEquals(ASSIGNED, fields.get("assignedDate").asText());
            assertEquals(EXPECTED, fields.get("expectedReturnDate").asText());
            if (loan.isOpen()) {
                assertTrue(fields.get("returnedDate").isNull());
            } else {
                assertEquals(EXPECTED, fields.get("returnedDate").asText());
            }
            assertFalse(fields.has("availability"));
            assertEquals(loan, JsonUtil.fromJsonString(json, JsonAdaptedLoan.class).toModelType());
        }
    }

    @Test
    public void json_missingOrNullReturnedDate_restoresOpenLoan() throws Exception {
        String json = "{\"equipmentUuid\":\"" + EQUIPMENT_UUID + "\",\"memberNusId\":\"" + MEMBER_ID
                + "\",\"assignedDate\":\"" + ASSIGNED + "\",\"expectedReturnDate\":\"" + EXPECTED + "\"";
        assertEquals(OPEN, JsonUtil.fromJsonString(json + "}", JsonAdaptedLoan.class).toModelType());
        assertEquals(OPEN, JsonUtil.fromJsonString(json + ",\"returnedDate\":null}",
                JsonAdaptedLoan.class).toModelType());
    }

    private static String[] validFields() {
        return new String[] {EQUIPMENT_UUID, MEMBER_ID, ASSIGNED, EXPECTED, null};
    }

    private static JsonAdaptedLoan adapter(String returnedDate) {
        return new JsonAdaptedLoan(EQUIPMENT_UUID, MEMBER_ID, ASSIGNED, EXPECTED, returnedDate);
    }

    private static JsonAdaptedLoan adapter(String[] fields) {
        return new JsonAdaptedLoan(fields[0], fields[1], fields[2], fields[3], fields[4]);
    }
}
