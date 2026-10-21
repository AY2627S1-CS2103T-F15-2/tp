package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import seedu.address.model.equipment.Condition;

/**
 * Verifies edit construction and value equality independently of command routing.
 */
public class EquipmentEditCommandTest {
    private static final UUID ID = new UUID(0, 1);

    @Test
    public void constructor_noFields_rejectsEditWithoutParserValidation() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
                new EquipmentEditCommand(ID, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()));
        assertEquals(EquipmentEditCommand.MESSAGE_NO_FIELDS, error.getMessage());
    }

    @Test
    public void equalsAndHashCode_normalizedFields_matchEquivalentEdits() {
        EquipmentEditCommand command = edit(ID, "Camera", "Photography", Condition.GOOD, "With strap");
        EquipmentEditCommand equivalent = edit(ID, " Camera ", " Photography ", Condition.GOOD, "With  strap ");
        assertEquals(command, command);
        assertEquals(command, equivalent);
        assertEquals(equivalent, command);
        assertEquals(command.hashCode(), equivalent.hashCode());
        assertNotEquals(command, null);
        assertNotEquals(command, new EquipmentRestoreCommand(ID));
    }

    @Test
    public void equals_differentTargetOrField_distinguishesEdits() {
        EquipmentEditCommand command = edit(ID, "Camera", "Photography", Condition.GOOD, "With strap");
        assertNotEquals(command, edit(new UUID(0, 2), "Camera", "Photography", Condition.GOOD, "With strap"));
        assertNotEquals(command, edit(ID, "Tripod", "Photography", Condition.GOOD, "With strap"));
        assertNotEquals(command, edit(ID, "Camera", "Video", Condition.GOOD, "With strap"));
        assertNotEquals(command, edit(ID, "Camera", "Photography", Condition.DAMAGED, "With strap"));
        assertNotEquals(command, edit(ID, "Camera", "Photography", Condition.GOOD, ""));
    }

    @Test
    public void equals_absentNotesAndEmptyNotes_distinguishesRetainingAndClearing() {
        EquipmentEditCommand retain = new EquipmentEditCommand(ID, Optional.of("Camera"), Optional.empty(),
                Optional.empty(), Optional.empty());
        EquipmentEditCommand clear = new EquipmentEditCommand(ID, Optional.of("Camera"), Optional.empty(),
                Optional.empty(), Optional.of(""));
        assertNotEquals(retain, clear);
    }

    private EquipmentEditCommand edit(UUID id, String name, String category, Condition condition, String notes) {
        return new EquipmentEditCommand(id, Optional.of(name), Optional.of(category), Optional.of(condition),
                Optional.of(notes));
    }
}
