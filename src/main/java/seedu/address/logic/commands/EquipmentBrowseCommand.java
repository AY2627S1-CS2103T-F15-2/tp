package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.query.InventoryGroups;

/**
 * Opens a named inventory group without choosing a physical unit or changing records.
 */
public final class EquipmentBrowseCommand extends Command {
    private final InventoryGroups.Key group;

    private EquipmentBrowseCommand(InventoryGroups.Key group) {
        this.group = requireNonNull(group);
    }

    /**
     * Creates navigation to the exact normalized category/name pair.
     */
    public static EquipmentBrowseCommand forGroup(String category, String name) {
        return new EquipmentBrowseCommand(new InventoryGroups.Key(requireNonNull(category), requireNonNull(name)));
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (model.getAddressBook().getEquipmentList().stream()
                .noneMatch(unit -> InventoryGroups.keyOf(unit).equals(group))) {
            throw new CommandException("No equipment group " + group + " exists.");
        }
        return new CommandResult("Viewing equipment group: " + group + ".", DisplayRequest.equipmentGroup(group));
    }
}
