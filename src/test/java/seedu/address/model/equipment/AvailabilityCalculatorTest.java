package seedu.address.model.equipment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class AvailabilityCalculatorTest {

    @Test
    public void calculate_nullCondition_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> AvailabilityCalculator.calculate(null, false));
        assertThrows(NullPointerException.class, () -> AvailabilityCalculator.calculate(null, true));
    }

    @Test
    public void calculate_openLoan_returnsAssignedForEveryCondition() {
        for (Condition condition : Condition.values()) {
            assertEquals(Availability.ASSIGNED, AvailabilityCalculator.calculate(condition, true));
        }
    }

    @Test
    public void calculate_noOpenLoan_returnsAvailabilityFromCondition() {
        assertEquals(Availability.AVAILABLE, AvailabilityCalculator.calculate(Condition.GOOD, false));
        assertEquals(Availability.AVAILABLE, AvailabilityCalculator.calculate(Condition.FAIR, false));
        assertEquals(Availability.UNAVAILABLE, AvailabilityCalculator.calculate(Condition.DAMAGED, false));
        assertEquals(Availability.UNAVAILABLE, AvailabilityCalculator.calculate(Condition.UNDER_REPAIR, false));
    }
}
