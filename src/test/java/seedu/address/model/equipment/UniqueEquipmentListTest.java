package seedu.address.model.equipment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import seedu.address.model.equipment.exceptions.DuplicateEquipmentException;
import seedu.address.model.equipment.exceptions.EquipmentNotFoundException;

public class UniqueEquipmentListTest {

    private static final UUID UUID_ONE = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private static final Equipment EQUIPMENT =
            new Equipment(UUID_ONE, "Camera", "Photography", Condition.GOOD, "");

    @Test
    public void scaffoldOperations_throwUnsupportedOperationException() {
        UniqueEquipmentList equipment = new UniqueEquipmentList();

        assertThrows(UnsupportedOperationException.class, () -> equipment.contains(UUID_ONE));
        assertThrows(UnsupportedOperationException.class, () -> equipment.findByUuid(UUID_ONE));
        assertThrows(UnsupportedOperationException.class, () -> equipment.add(EQUIPMENT));
        assertThrows(UnsupportedOperationException.class, () -> equipment.remove(EQUIPMENT));
        assertThrows(UnsupportedOperationException.class, () -> equipment.setEquipment(List.of(EQUIPMENT)));
        assertThrows(UnsupportedOperationException.class, equipment::asUnmodifiableObservableList);
    }

    @Test
    public void scaffoldExceptions_haveExpectedMessages() {
        assertEquals("Operation would result in duplicate equipment",
                new DuplicateEquipmentException().getMessage());
        assertNull(new EquipmentNotFoundException().getMessage());
    }
}
