package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

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
     * Opens the group and reports it in the stored spelling of its first unit, matching the group header.
     *
     * @throws CommandException if no unit belongs to the group; the message repeats the supplied spelling.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Equipment first = model.getAddressBook().getEquipmentList().stream()
                .filter(unit -> InventoryGroups.keyOf(unit).equals(group)).findFirst()
                .orElseThrow(() -> new CommandException("No equipment group "
                        + InventoryGroups.label(category, name) + " exists."));
        return new CommandResult("Viewing equipment group: "
                + InventoryGroups.label(first.getCategory(), first.getName()) + ".",
                DisplayRequest.equipmentGroup(group));
    }
}
