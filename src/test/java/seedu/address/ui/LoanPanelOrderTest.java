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

}
