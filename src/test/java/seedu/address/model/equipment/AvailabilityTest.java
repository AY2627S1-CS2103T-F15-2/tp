package seedu.address.model.equipment;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class AvailabilityTest {

    @Test
    public void values_containsEveryAvailability() {
        Availability[] expected = {
            Availability.AVAILABLE,
            Availability.ASSIGNED,
            Availability.UNAVAILABLE
        };

        assertArrayEquals(expected, Availability.values());
        assertEquals(Availability.ASSIGNED, Availability.valueOf("ASSIGNED"));
    }
}
