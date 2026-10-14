package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.equipment.Availability;
import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.equipment.exceptions.DuplicateEquipmentException;

public class EquipmentAddCommandTest {

    private static final UUID ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    @Test
    public void execute_generatesIdentityOnlyAtExecution_andReturnsDetailsAndAvailability() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        EquipmentAddCommand command = new EquipmentAddCommand(" Camera ", " Photo ", Condition.GOOD, " notes ", () -> {
            calls.incrementAndGet();
            return ID;
        });
        assertEquals(0, calls.get());
        ModelManager model = new ModelManager();
        CommandResult result = command.execute(model);
        assertEquals(1, calls.get());
        assertEquals(new Equipment(ID, "Camera", "Photo", Condition.GOOD, "notes"),
                model.findEquipment(ID).orElseThrow());
        assertEquals(DisplayRequest.equipmentDetails(ID), result.getDisplayRequest());
        assertTrue(result.getFeedbackToUser().contains(ID.toString()));
        assertTrue(result.getFeedbackToUser().contains("Camera"));
        assertTrue(result.getFeedbackToUser().contains("AVAILABLE"));
        AddressBook before = new AddressBook(model.getAddressBook());
        assertThrows(CommandException.class, () -> command.execute(model));
        assertEquals(2, calls.get());
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void execute_identicalDescriptions_keepSeparateIdentities() throws Exception {
        ModelManager model = new ModelManager();
        EquipmentAddCommand command = new EquipmentAddCommand("Camera", "Photo", Condition.GOOD, "");
        command.execute(model);
        command.execute(model);
        assertEquals(2, model.getEquipmentList().size());
        assertNotEquals(model.getEquipmentList().get(0).getUuid(), model.getEquipmentList().get(1).getUuid());
    }

    @Test
    public void execute_eachCondition_usesRealModelAvailability() throws Exception {
        for (Condition condition : Condition.values()) {
            ModelManager model = new ModelManager();
            new EquipmentAddCommand("Camera", "Photo", condition, "", () -> ID).execute(model);
            assertEquals(condition == Condition.GOOD || condition == Condition.FAIR
                    ? Availability.AVAILABLE : Availability.UNAVAILABLE, model.getAvailability(ID));
        }
    }

    @Test
    public void execute_domainDuplicate_preservesCauseAndState() {
        DuplicateEquipmentException cause = new DuplicateEquipmentException();
        ModelManager model = new ModelManager() {
            @Override
            public void addEquipment(Equipment equipment) {
                throw cause;
            }
        };
        AddressBook before = new AddressBook(model.getAddressBook());
        CommandException exception = assertThrows(CommandException.class, () ->
                new EquipmentAddCommand("Camera", "Photo", Condition.GOOD, "", () -> ID).execute(model));
        assertSame(cause, exception.getCause());
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void constructor_invalidArguments_rejectsMisuse() {
        assertThrows(NullPointerException.class, () -> new EquipmentAddCommand(null, "Photo", Condition.GOOD, ""));
        assertThrows(NullPointerException.class, () -> new EquipmentAddCommand("Camera", null, Condition.GOOD, ""));
        assertThrows(NullPointerException.class, () -> new EquipmentAddCommand("Camera", "Photo", null, ""));
        assertThrows(NullPointerException.class, () ->
                new EquipmentAddCommand("Camera", "Photo", Condition.GOOD, null));
        assertThrows(NullPointerException.class, () ->
                new EquipmentAddCommand("Camera", "Photo", Condition.GOOD, "", null));
        assertThrows(IllegalArgumentException.class, () ->
                new EquipmentAddCommand(" ", "Photo", Condition.GOOD, ""));
        assertThrows(IllegalArgumentException.class, () ->
                new EquipmentAddCommand("Camera", "\t", Condition.GOOD, ""));
        assertThrows(NullPointerException.class, () ->
                new EquipmentAddCommand("Camera", "Photo", Condition.GOOD, "").execute(null));
    }

    @Test
    public void equality_comparesDescriptionsWithoutGeneratingIdentity() {
        EquipmentAddCommand command = new EquipmentAddCommand("Camera", "Photo", Condition.GOOD, "");
        EquipmentAddCommand same = new EquipmentAddCommand("Camera", "Photo", Condition.GOOD, "", () -> ID);
        assertEquals(command, command);
        assertEquals(command, same);
        assertEquals(command.hashCode(), same.hashCode());
        assertNotEquals(command, null);
        assertNotEquals(command, "Camera");
        assertNotEquals(command, new EquipmentAddCommand("Other", "Photo", Condition.GOOD, ""));
        assertNotEquals(command, new EquipmentAddCommand("Camera", "Other", Condition.GOOD, ""));
        assertNotEquals(command, new EquipmentAddCommand("Camera", "Photo", Condition.FAIR, ""));
        assertNotEquals(command, new EquipmentAddCommand("Camera", "Photo", Condition.GOOD, "Other"));
        assertTrue(command.toString().contains("Camera"));
    }
}
