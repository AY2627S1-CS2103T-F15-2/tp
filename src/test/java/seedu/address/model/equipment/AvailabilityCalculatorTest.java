package seedu.address.model.equipment;

import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class AvailabilityCalculatorTest {

    @Test
    public void calculate_scaffold_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () ->
                AvailabilityCalculator.calculate(Condition.GOOD, false));
    }
}
