package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.UUID;

import org.junit.jupiter.api.Test;

/**
 * Verifies that restoration commands compare by physical equipment identity.
 */
public class EquipmentRestoreCommandTest {
    @Test
    public void equalsAndHashCode_targetUuid_distinguishesRestoreTargets() {
        UUID id = new UUID(0, 1);
        EquipmentRestoreCommand command = new EquipmentRestoreCommand(id);
        EquipmentRestoreCommand equivalent = new EquipmentRestoreCommand(id);
        assertEquals(command, command);
        assertEquals(command, equivalent);
        assertEquals(equivalent, command);
        assertEquals(command.hashCode(), equivalent.hashCode());
        assertNotEquals(command, new EquipmentRestoreCommand(new UUID(0, 2)));
        assertNotEquals(command, null);
        assertNotEquals(command, new EquipmentDeleteCommand(id));
    }
}
