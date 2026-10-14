package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import seedu.address.model.loan.Loan;
import seedu.address.model.member.NusId;

/** Verifies the panel's record ordering without starting the JavaFX toolkit. */
public class LoanPanelOrderTest {
    private static final NusId MEMBER_ID = new NusId("A0123456X");
    private static final UUID LOWER_UUID = UUID.fromString("10000000-0000-0000-0000-000000000000");
    private static final UUID HIGHER_UUID = UUID.fromString("90000000-0000-0000-0000-000000000000");
    private static final LocalDate ASSIGNED = LocalDate.of(2026, 10, 1);

    @Test
    public void openOrder_expectedDateThenCanonicalUuid() {
        Loan firstDue = new Loan(HIGHER_UUID, MEMBER_ID, ASSIGNED, ASSIGNED.plusDays(1), null);
        Loan lowerId = new Loan(LOWER_UUID, MEMBER_ID, ASSIGNED, ASSIGNED.plusDays(2), null);
        Loan higherId = new Loan(HIGHER_UUID, MEMBER_ID, ASSIGNED, ASSIGNED.plusDays(2), null);
        assertEquals(List.of(firstDue, lowerId, higherId), List.of(higherId, lowerId, firstDue).stream()
                .sorted(LoanPanel.OPEN_ORDER).toList());
    }

    @Test
    public void historyOrder_returnedDateDescendingThenCanonicalUuidThenAssignedDate() {
        LocalDate returned = ASSIGNED.plusDays(10);
        Loan oldestReturn = new Loan(LOWER_UUID, MEMBER_ID, ASSIGNED, returned, returned.minusDays(1));
        Loan earlierAssignment = new Loan(LOWER_UUID, MEMBER_ID, ASSIGNED, returned, returned);
        Loan laterAssignment = new Loan(LOWER_UUID, MEMBER_ID, ASSIGNED.plusDays(1), returned, returned);
        Loan higherId = new Loan(HIGHER_UUID, MEMBER_ID, ASSIGNED, returned, returned);
        assertEquals(List.of(earlierAssignment, laterAssignment, higherId, oldestReturn),
                List.of(oldestReturn, higherId, laterAssignment, earlierAssignment).stream()
                        .sorted(LoanPanel.HISTORY_ORDER).toList());
    }

    @Test
    public void historyOrder_sameDatesAndEquipment_memberIdentityMakesOrderStable() {
        Loan alex = new Loan(LOWER_UUID, MEMBER_ID, ASSIGNED, ASSIGNED, ASSIGNED);
        Loan jamie = new Loan(LOWER_UUID, new NusId("A0234567R"), ASSIGNED, ASSIGNED, ASSIGNED);
        assertEquals(List.of(alex, jamie), List.of(jamie, alex).stream().sorted(LoanPanel.HISTORY_ORDER).toList());
    }
}
