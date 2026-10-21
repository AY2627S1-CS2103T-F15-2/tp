package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.UUID;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.equipment.exceptions.EquipmentNotFoundException;

/**
 * Restores a retained physical unit with the same UUID, details and loan history.
 */
public class EquipmentRestoreCommand extends Command {
    public static final String COMMAND_WORD = "equipment restore";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Restores deleted equipment. Parameters: uuid/UUID";
    public static final String MESSAGE_ACTIVE = "Equipment %s is already active.";
    public static final String MESSAGE_SUCCESS = "Restored equipment %1$s: %2$s.";

    private final UUID targetUuid;

    /**
     * Creates a restoration for one exact equipment identity.
     */
    public EquipmentRestoreCommand(UUID targetUuid) {
        this.targetUuid = requireNonNull(targetUuid);
    }

    @Override
    public boolean isTransactional() {
        return true;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        try {
            Equipment stored = model.findEquipment(targetUuid).orElseThrow(EquipmentNotFoundException::new);
            if (!stored.isDeleted()) {
                throw new CommandException(String.format(MESSAGE_ACTIVE, targetUuid));
            }
            model.restoreEquipment(stored);
            return new CommandResult(String.format(MESSAGE_SUCCESS, targetUuid, stored.getName()),
                    DisplayRequest.equipmentDetails(targetUuid));
        } catch (EquipmentNotFoundException exception) {
            throw new CommandException(String.format(EquipmentDeleteCommand.MESSAGE_NOT_FOUND, targetUuid), exception);
        }
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof EquipmentRestoreCommand command
                && targetUuid.equals(command.targetUuid);
    }

    @Override
    public int hashCode() {
        return targetUuid.hashCode();
    }
}
