package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.UUID;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.equipment.exceptions.EquipmentNotFoundException;

/**
 * Deletes a stored equipment item by UUID, delegating loan integrity to the model.
 */
public class EquipmentDeleteCommand extends Command {

    public static final String COMMAND_WORD = "equipment delete";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes equipment without an open loan. Parameters: uuid/UUID\n"
            + "Example: equipment delete uuid/550e8400-e29b-41d4-a716-446655440000";
    public static final String MESSAGE_SUCCESS = "Deleted equipment: %1$s";
    public static final String MESSAGE_NOT_FOUND = "No equipment with UUID %1$s exists.";
    public static final String MESSAGE_OPEN_LOAN =
            "Cannot delete equipment %1$s with an open loan. Record its return first.";

    private final UUID targetUuid;

    /**
     * Creates a deletion command for a non-null equipment identity.
     */
    public EquipmentDeleteCommand(UUID uuid) {
        targetUuid = requireNonNull(uuid);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        try {
            Equipment equipment = model.findEquipment(targetUuid).orElseThrow(EquipmentNotFoundException::new);
            model.deleteEquipment(equipment);
        } catch (EquipmentNotFoundException e) {
            throw new CommandException(String.format(MESSAGE_NOT_FOUND, targetUuid), e);
        } catch (IllegalStateException e) {
            throw new CommandException(String.format(MESSAGE_OPEN_LOAN, targetUuid), e);
        }
        return new CommandResult(String.format(MESSAGE_SUCCESS, targetUuid), DisplayRequest.equipment());
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof EquipmentDeleteCommand otherCommand
                && targetUuid.equals(otherCommand.targetUuid);
    }

    @Override
    public int hashCode() {
        return targetUuid.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("targetUuid", targetUuid).toString();
    }
}
