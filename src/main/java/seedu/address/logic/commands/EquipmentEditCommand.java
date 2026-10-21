package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.equipment.exceptions.EquipmentDeletedException;
import seedu.address.model.equipment.exceptions.EquipmentNotFoundException;

/**
 * Edits one physical unit while preserving its UUID and all loan references.
 */
public class EquipmentEditCommand extends Command {
    public static final String COMMAND_WORD = "equipment edit";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Edits active equipment. Parameters: uuid/UUID [name/NAME] [category/CATEGORY]"
            + " [condition/CONDITION] [notes/NOTES]\nUse notes/ with no value to clear notes.";
    public static final String MESSAGE_NO_FIELDS = "At least one field to edit must be provided.";
    public static final String MESSAGE_NO_CHANGE = "The supplied details do not change this equipment.";
    public static final String MESSAGE_DELETED = "Cannot edit deleted equipment %1$s. "
            + "Use equipment restore uuid/%1$s first.";
    public static final String MESSAGE_SUCCESS = "Edited equipment %1$s: %2$s.";

    private final UUID targetUuid;
    private final Optional<String> name;
    private final Optional<String> category;
    private final Optional<Condition> condition;
    private final Optional<String> notes;

    /**
     * Creates a validated edit; absent fields retain their stored values and blank notes clear notes.
     */
    public EquipmentEditCommand(UUID targetUuid, Optional<String> name, Optional<String> category,
            Optional<Condition> condition, Optional<String> notes) {
        this.targetUuid = requireNonNull(targetUuid);
        this.name = requireNonNull(name).map(Equipment::normalizeName);
        this.category = requireNonNull(category).map(Equipment::normalizeCategory);
        this.condition = requireNonNull(condition);
        this.notes = requireNonNull(notes).map(Equipment::normalizeNotes);
        checkArgument(name.isPresent() || category.isPresent() || condition.isPresent() || notes.isPresent(),
                MESSAGE_NO_FIELDS);
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
            if (stored.isDeleted()) {
                throw new EquipmentDeletedException();
            }
            Equipment edited = new Equipment(stored.getUuid(), name.orElse(stored.getName()),
                    category.orElse(stored.getCategory()), condition.orElse(stored.getCondition()),
                    notes.orElse(stored.getNotes()));
            if (stored.equals(edited)) {
                throw new CommandException(MESSAGE_NO_CHANGE);
            }
            model.setEquipment(stored, edited);
            return new CommandResult(String.format(MESSAGE_SUCCESS, targetUuid, edited.getName()),
                    DisplayRequest.equipmentDetails(targetUuid));
        } catch (EquipmentDeletedException exception) {
            throw new CommandException(String.format(MESSAGE_DELETED, targetUuid), exception);
        } catch (EquipmentNotFoundException exception) {
            throw new CommandException(String.format(EquipmentDeleteCommand.MESSAGE_NOT_FOUND, targetUuid), exception);
        }
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof EquipmentEditCommand command
                && targetUuid.equals(command.targetUuid) && name.equals(command.name)
                && category.equals(command.category) && condition.equals(command.condition)
                && notes.equals(command.notes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetUuid, name, category, condition, notes);
    }
}
