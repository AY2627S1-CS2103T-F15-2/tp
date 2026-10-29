package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.ViewTestData.CAMERA;
import static seedu.address.testutil.ViewTestData.OTHER_CAMERA;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.EquipmentViewCommandParser;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.query.InventoryGroups;
import seedu.address.testutil.ViewTestData;

public class EquipmentBrowseCommandTest {
    @Test
    public void execute_sameNamedUnits_opensGroupWithoutChoosingOrChangingUnit() throws Exception {
        ModelManager model = ViewTestData.model();
        AddressBook before = new AddressBook(model.getAddressBook());
        EquipmentViewCommandParser parser = new EquipmentViewCommandParser();
        for (String input : List.of("category/ PHOTOGRAPHY name/camera", "name/Camera category/Photography")) {
            Command command = parser.parse(input);
            assertTrue(command.isReadOnly());
            assertFalse(command.isTransactional());
            CommandResult result = command.execute(model);
            assertEquals(DisplayRequest.equipmentGroup(InventoryGroups.keyOf(CAMERA)), result.getDisplayRequest());
            assertTrue(result.getDisplayRequest().getEquipmentUuid().isEmpty());
            assertEquals("Viewing equipment group: \"photography\" / \"camera\".", result.getFeedbackToUser());
            assertEquals(before, model.getAddressBook());
        }
        model.deleteEquipment(CAMERA);
        model.deleteEquipment(OTHER_CAMERA);
        before = new AddressBook(model.getAddressBook());
        assertEquals(DisplayRequest.equipmentGroup(InventoryGroups.keyOf(CAMERA)),
                parser.parse("category/Photography name/Camera").execute(model).getDisplayRequest());
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void execute_missingOrRenamedGroup_rejectsWithoutMutation() {
        ModelManager model = new ModelManager();
        Command command = EquipmentBrowseCommand.forGroup("Photography", "Camera");
        assertThrows(CommandException.class, () -> command.execute(model));
        model.addEquipment(CAMERA);
        model.setEquipment(CAMERA, new Equipment(CAMERA.getUuid(), "Lens", "Photography", Condition.GOOD, ""));
        AddressBook before = new AddressBook(model.getAddressBook());
        CommandException error = assertThrows(CommandException.class, () -> command.execute(model));
        assertEquals("No equipment group \"photography\" / \"camera\" exists.", error.getMessage());
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void factories_nullInputs_rejects() {
        assertThrows(NullPointerException.class, () -> EquipmentBrowseCommand.forGroup(null, "Camera"));
        assertThrows(NullPointerException.class, () -> EquipmentBrowseCommand.forGroup("Photography", null));
        Command command = EquipmentBrowseCommand.forGroup("Photography", "Camera");
        assertThrows(NullPointerException.class, () -> command.execute(null));
    }
}
