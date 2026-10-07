package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.loan.Loan;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;

public class JsonLaplaceAdapterScaffoldTest {

    private static final UUID EQUIPMENT_UUID =
            UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private static final NusId MEMBER_NUS_ID = new NusId("A0123456X");
    private static final Member MEMBER = new Member(MEMBER_NUS_ID, new Name("Alex Tan"),
            new Phone("91234567"), new Email("alex@example.com"));
    private static final Equipment EQUIPMENT =
            new Equipment(EQUIPMENT_UUID, "Camera", "Photography", Condition.GOOD, "");
    private static final LocalDate ASSIGNED_DATE = LocalDate.of(2026, 10, 6);
    private static final LocalDate EXPECTED_RETURN_DATE = LocalDate.of(2026, 10, 13);

    @Test
    public void memberConstructors_createAdaptersAndConversionFailsExplicitly() {
        JsonAdaptedMember rawAdapter =
                new JsonAdaptedMember("A0123456X", "Alex Tan", "91234567", "alex@example.com");
        JsonAdaptedMember entityAdapter = new JsonAdaptedMember(MEMBER);

        assertThrows(UnsupportedOperationException.class, rawAdapter::toModelType);
        assertThrows(UnsupportedOperationException.class, entityAdapter::toModelType);
    }

    @Test
    public void equipmentConstructors_createAdaptersAndConvertToModelType() throws Exception {
        JsonAdaptedEquipment rawAdapter = new JsonAdaptedEquipment(
                EQUIPMENT_UUID.toString(), "Camera", "Photography", "GOOD", "");
        JsonAdaptedEquipment entityAdapter = new JsonAdaptedEquipment(EQUIPMENT);

        assertEquals(EQUIPMENT, rawAdapter.toModelType());
        assertEquals(EQUIPMENT, entityAdapter.toModelType());
    }

    @Test
    public void loanConstructors_createAdaptersAndConversionFailsExplicitly() {
        JsonAdaptedLoan rawAdapter = new JsonAdaptedLoan(EQUIPMENT_UUID.toString(),
                MEMBER_NUS_ID.toString(), ASSIGNED_DATE.toString(), EXPECTED_RETURN_DATE.toString(), null);
        Loan openLoan = new Loan(EQUIPMENT_UUID, MEMBER_NUS_ID, ASSIGNED_DATE, EXPECTED_RETURN_DATE, null);
        Loan closedLoan = openLoan.withReturnedDate(EXPECTED_RETURN_DATE);
        JsonAdaptedLoan openEntityAdapter = new JsonAdaptedLoan(openLoan);
        JsonAdaptedLoan closedEntityAdapter = new JsonAdaptedLoan(closedLoan);

        assertThrows(UnsupportedOperationException.class, rawAdapter::toModelType);
        assertThrows(UnsupportedOperationException.class, openEntityAdapter::toModelType);
        assertThrows(UnsupportedOperationException.class, closedEntityAdapter::toModelType);
    }
}
