package seedu.address.model.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import seedu.address.model.loan.Loan;
import seedu.address.model.member.NusId;

/** Checks the common identity predicate across open and retained completed loans. */
public class LoanQueryTest {
    @Test
    public void matches_identitySubstrings_ignoresCaseAndOuterWhitespaceInBothLifecycleStates() {
        Loan open = new Loan(UUID.fromString("abcdef00-0000-0000-0000-000000000001"), new NusId("A0123456X"),
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2), null);
        LoanQuery query = new LoanQuery(" a012 ", " ABCdef ");
        assertEquals("a012", query.memberText());
        assertEquals("ABCdef", query.equipmentText());
        assertTrue(query.matches(open));
        assertTrue(query.matches(open.withReturnedDate(LocalDate.of(2026, 1, 2))));
        assertTrue(LoanQuery.all().matches(open));
        assertFalse(new LoanQuery("other", "abcdef").matches(open));
        assertFalse(new LoanQuery("a012", "missing").matches(open));
        assertFalse(new LoanQuery("a0 12", "").matches(open));
        assertThrows(NullPointerException.class, () -> new LoanQuery(null, ""));
        assertThrows(NullPointerException.class, () -> new LoanQuery("", null));
        assertThrows(NullPointerException.class, () -> query.matches(null));
    }
}
