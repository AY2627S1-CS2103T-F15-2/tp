package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.UUID;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.equipment.exceptions.EquipmentNotFoundException;

/**
 * Soft-deletes a stored unit by UUID, retaining details and loan history.
 */
public class EquipmentDeleteCommand extends Command {

    public static final String COMMAND_WORD = "equipment delete";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Soft-deletes equipment without an open loan; retains details and history. Parameters: uuid/UUID\n"
            + "Example: equipment delete uuid/550e8400-e29b-41d4-a716-446655440000";
    public static final String MESSAGE_SUCCESS = "Deleted equipment %1$s: %2$s.";
    public static final String MESSAGE_NOT_FOUND = "No equipment with UUID %1$s exists.";
    public static final String MESSAGE_DELETED = "Equipment %s is already deleted.";
    public static final String MESSAGE_OPEN_LOAN =
            "Cannot delete %1$s because it is assigned to %2$s. Return the item first.";

    private final UUID targetUuid;

    /**
     * Creates a deletion command for a non-null equipment identity.
     */
    public EquipmentDeleteCommand(UUID uuid) {
        targetUuid = requireNonNull(uuid);
    }

    @Override
    public boolean isTransactional() {
        return true;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        try {
            Equipment equipment = model.findEquipment(targetUuid).orElseThrow(EquipmentNotFoundException::new);
            if (equipment.isDeleted()) {
                throw new CommandException(String.format(MESSAGE_DELETED, targetUuid));
            }
            String blockedMessage = model.findOpenLoan(targetUuid)
                    .map(loan -> String.format(MESSAGE_OPEN_LOAN, targetUuid, loan.getMemberNusId()))
                    .orElse("Could not delete equipment " + targetUuid + ".");
            try {
                model.deleteEquipment(equipment);
            } catch (IllegalStateException e) {
                throw new CommandException(blockedMessage, e);
            }
            return new CommandResult(String.format(MESSAGE_SUCCESS, targetUuid, equipment.getName()),
                    DisplayRequest.equipment());
        } catch (EquipmentNotFoundException e) {
            throw new CommandException(String.format(MESSAGE_NOT_FOUND, targetUuid), e);
        }
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
