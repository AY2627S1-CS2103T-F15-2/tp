package seedu.address.model.equipment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.Test;

public class EquipmentTest {

    private static final UUID UUID_ONE = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private static final UUID UUID_TWO = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");

    @Test
    public void constructor_nullField_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                new Equipment(null, "Camera", "Photography", Condition.GOOD, "With strap"));
        assertThrows(NullPointerException.class, () ->
                new Equipment(UUID_ONE, null, "Photography", Condition.GOOD, "With strap"));
        assertThrows(NullPointerException.class, () ->
                new Equipment(UUID_ONE, "Camera", null, Condition.GOOD, "With strap"));
        assertThrows(NullPointerException.class, () ->
                new Equipment(UUID_ONE, "Camera", "Photography", null, "With strap"));
        assertThrows(NullPointerException.class, () ->
                new Equipment(UUID_ONE, "Camera", "Photography", Condition.GOOD, null));
    }

    @Test
    public void getters_returnConstructorValues() {
        Equipment equipment = new Equipment(UUID_ONE, "Camera", "Photography", Condition.GOOD, "With strap");

        assertEquals(UUID_ONE, equipment.getUuid());
        assertEquals("Camera", equipment.getName());
        assertEquals("Photography", equipment.getCategory());
        assertEquals(Condition.GOOD, equipment.getCondition());
        assertEquals("With strap", equipment.getNotes());
    }

    @Test
    public void isSameEquipment() {
        Equipment equipment = new Equipment(UUID_ONE, "Camera", "Photography", Condition.GOOD, "With strap");

        assertTrue(equipment.isSameEquipment(equipment));
        assertTrue(equipment.isSameEquipment(
                new Equipment(UUID_ONE, "Tripod", "Support", Condition.FAIR, "Different item details")));
        assertFalse(equipment.isSameEquipment(null));
        assertFalse(equipment.isSameEquipment(
                new Equipment(UUID_TWO, "Camera", "Photography", Condition.GOOD, "With strap")));
    }

    @Test
    public void equals() {
        Equipment equipment = new Equipment(UUID_ONE, "Camera", "Photography", Condition.GOOD, "With strap");
        Equipment copy = new Equipment(UUID_ONE, "Camera", "Photography", Condition.GOOD, "With strap");

        assertTrue(equipment.equals(equipment));
        assertTrue(equipment.equals(copy));
        assertFalse(equipment.equals(null));
        assertFalse(equipment.equals("equipment"));
        assertFalse(equipment.equals(
                new Equipment(UUID_TWO, "Camera", "Photography", Condition.GOOD, "With strap")));
        assertFalse(equipment.equals(
                new Equipment(UUID_ONE, "Tripod", "Photography", Condition.GOOD, "With strap")));
        assertFalse(equipment.equals(
                new Equipment(UUID_ONE, "Camera", "Audio", Condition.GOOD, "With strap")));
        assertFalse(equipment.equals(
                new Equipment(UUID_ONE, "Camera", "Photography", Condition.DAMAGED, "With strap")));
        assertFalse(equipment.equals(
                new Equipment(UUID_ONE, "Camera", "Photography", Condition.UNDER_REPAIR, "No strap")));
        assertEquals(equipment.hashCode(), copy.hashCode());
    }

    @Test
    public void toStringMethod() {
        Equipment equipment = new Equipment(UUID_ONE, "Camera", "Photography", Condition.GOOD, "With strap");
        String expected = Equipment.class.getCanonicalName()
                + "{uuid=" + UUID_ONE + ", name=Camera, category=Photography, condition=GOOD, notes=With strap}";

        assertEquals(expected, equipment.toString());
    }
}
