package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.EquipmentViewCommandParser;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.query.InventoryGroups;

public class EquipmentBrowseCommandTest {
    private static final Equipment CAMERA = new Equipment(
            UUID.fromString("abcdef12-0000-0000-0000-000000000001"), "Camera", "Photo", Condition.GOOD, "");

    @Test
    public void execute_groupAndShortForms_resolveStableIdentitiesWithoutMutation() throws Exception {
        ModelManager model = new ModelManager();
        model.addEquipment(CAMERA);
        AddressBook before = new AddressBook(model.getAddressBook());
        EquipmentViewCommandParser parser = new EquipmentViewCommandParser();
        for (String input : new String[] {"category/ PHOTO name/camera", "name/Camera category/Photo"}) {
            Command command = parser.parse(input);
            assertTrue(command.isReadOnly());
            assertFalse(command.isTransactional());
            assertEquals(DisplayRequest.equipmentGroup(InventoryGroups.keyOf(CAMERA)),
                    command.execute(model).getDisplayRequest());
        }
        Command command = parser.parse("short/ABCDEF12");
        assertTrue(command.isReadOnly());
        assertEquals(new EquipmentViewCommand(CAMERA.getUuid()).execute(model), command.execute(model));
        assertEquals(before, model.getAddressBook());
        model.deleteEquipment(CAMERA);
        assertEquals(DisplayRequest.equipmentDetails(CAMERA.getUuid()), command.execute(model).getDisplayRequest());
        assertEquals(DisplayRequest.equipmentGroup(InventoryGroups.keyOf(CAMERA)),
                parser.parse("category/Photo name/Camera").execute(model).getDisplayRequest());
    }

    @Test
    public void execute_collisionIncludingDeletedUnit_rejectsInsteadOfChoosingFirst() {
        ModelManager model = new ModelManager();
        model.addEquipment(CAMERA);
        Equipment collision = new Equipment(UUID.fromString("abcdef12-1111-0000-0000-000000000001"),
                "Speaker", "Audio", Condition.GOOD, "");
        model.addEquipment(collision);
        model.deleteEquipment(collision);
        AddressBook before = new AddressBook(model.getAddressBook());
        CommandException error = assertThrows(
                CommandException.class, () -> EquipmentBrowseCommand.forShortUuid("abcdef12").execute(model));
        assertTrue(error.getMessage().contains("multiple retained units"));
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void execute_missingOrRenamedTargets_rejectsWithoutMutation() throws Exception {
        ModelManager model = new ModelManager();
        model.addEquipment(CAMERA);
        Command group = EquipmentBrowseCommand.forGroup("Photo", "Camera");
        model.setEquipment(CAMERA, new Equipment(CAMERA.getUuid(), "Lens", "Photo", Condition.GOOD, ""));
        AddressBook before = new AddressBook(model.getAddressBook());
        assertThrows(CommandException.class, () -> group.execute(model));
        assertThrows(CommandException.class, () -> EquipmentBrowseCommand.forShortUuid("00000000").execute(model));
        assertEquals(DisplayRequest.equipmentDetails(CAMERA.getUuid()),
                EquipmentBrowseCommand.forShortUuid("abcdef12").execute(model).getDisplayRequest());
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void execute_displayedSuffix_disambiguatesSharedPrefixes() throws Exception {
        ModelManager model = new ModelManager();
        model.addEquipment(CAMERA);
        Equipment other = new Equipment(UUID.fromString("abcdef12-0000-0000-0000-000000000002"),
                "Other", "Audio", Condition.GOOD, "");
        model.addEquipment(other);
        for (String reference : new String[] {"ABCDEF12-0001", "abcdef12…0001"}) {
            assertEquals(DisplayRequest.equipmentDetails(CAMERA.getUuid()),
                    EquipmentBrowseCommand.forShortUuid(reference).execute(model).getDisplayRequest());
        }
        Equipment collision = new Equipment(UUID.fromString("abcdef12-1111-0000-0000-000000000001"),
                "Third", "Audio", Condition.GOOD, "");
        model.addEquipment(collision);
        assertThrows(CommandException.class, () -> EquipmentBrowseCommand.forShortUuid("abcdef12-0001").execute(model));
    }

    @Test
    public void factories_invalidInputs_rejects() {
        assertThrows(NullPointerException.class, () -> EquipmentBrowseCommand.forGroup(null, "Camera"));
        assertThrows(NullPointerException.class, () -> EquipmentBrowseCommand.forShortUuid(null));
        assertThrows(IllegalArgumentException.class, () -> EquipmentBrowseCommand.forShortUuid("bad"));
        assertThrows(
                NullPointerException.class, () -> EquipmentBrowseCommand.forShortUuid("abcdef12").execute(null));
    }
}
