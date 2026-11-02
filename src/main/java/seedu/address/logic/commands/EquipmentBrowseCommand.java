package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.query.InventoryGroups;

/**
 * Opens a named inventory group without choosing a physical unit or changing records.
 */
public final class EquipmentBrowseCommand extends Command {
    private final String category;
    private final String name;
    private final InventoryGroups.Key group;

    private EquipmentBrowseCommand(String category, String name) {
        this.category = requireNonNull(category);
        this.name = requireNonNull(name);
        this.group = new InventoryGroups.Key(category, name);
    }

    /**
     * Creates navigation to the exact normalized category/name pair.
     */
    public static EquipmentBrowseCommand forGroup(String category, String name) {
        return new EquipmentBrowseCommand(category, name);
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    /**
     * Opens the group and reports it in the stored spelling that the group header and list card show.
     *
     * @throws CommandException if no unit belongs to the group; the message repeats the supplied spelling.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Equipment> units = InventoryGroups.unitsOf(model.getAddressBook(), group);
        if (units.isEmpty()) {
            throw new CommandException("No equipment group " + InventoryGroups.label(category, name) + " exists.");
        }
        Equipment first = units.getFirst();
        return new CommandResult("Viewing equipment group: "
                + InventoryGroups.label(first.getCategory(), first.getName()) + ".",
                DisplayRequest.equipmentGroup(group));
    }
}
