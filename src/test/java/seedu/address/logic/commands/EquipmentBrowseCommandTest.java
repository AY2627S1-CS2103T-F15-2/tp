package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.ViewTestData.CAMERA;
import static seedu.address.testutil.ViewTestData.OTHER_CAMERA;

import java.util.List;
import java.util.UUID;

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
            assertEquals("Viewing equipment group: \"Photography\" / \"Camera\".", result.getFeedbackToUser());
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
        assertEquals("No equipment group \"Photography\" / \"Camera\" exists.", error.getMessage());
        CommandException typed = assertThrows(CommandException.class, () ->
                new EquipmentViewCommandParser().parse("category/PHOTOGRAPHY name/cam").execute(model));
        assertEquals("No equipment group \"PHOTOGRAPHY\" / \"cam\" exists.", typed.getMessage());
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void execute_differentlyCasedUnits_reportsSpellingOfLowestUuidRegardlessOfInsertionOrder()
            throws Exception {
        ModelManager model = new ModelManager();
        model.addEquipment(new Equipment(new UUID(0, 7), "camera", "photography", Condition.GOOD, ""));
        model.addEquipment(CAMERA);
        Command command = new EquipmentViewCommandParser().parse("category/PHOTOGRAPHY name/camera");
        assertEquals("Viewing equipment group: \"Photography\" / \"Camera\".",
                command.execute(model).getFeedbackToUser());
        model.addEquipment(new Equipment(new UUID(0, 0), "CAMERA", "PHOTOGRAPHY", Condition.GOOD, ""));
        assertEquals("Viewing equipment group: \"PHOTOGRAPHY\" / \"CAMERA\".",
                command.execute(model).getFeedbackToUser());
        model.addEquipment(new Equipment(new UUID(0, 8), "\"Quoted\" Camera", "Photography", Condition.GOOD, ""));
        CommandResult quoted = EquipmentBrowseCommand.forGroup("photography", "\"quoted\" camera").execute(model);
        assertEquals("Viewing equipment group: \"Photography\" / \"\"\"Quoted\"\" Camera\".",
                quoted.getFeedbackToUser());
    }

    @Test
    public void factories_nullInputs_rejects() {
        assertThrows(NullPointerException.class, () -> EquipmentBrowseCommand.forGroup(null, "Camera"));
        assertThrows(NullPointerException.class, () -> EquipmentBrowseCommand.forGroup("Photography", null));
        Command command = EquipmentBrowseCommand.forGroup("Photography", "Camera");
        assertThrows(NullPointerException.class, () -> command.execute(null));
    }
}
