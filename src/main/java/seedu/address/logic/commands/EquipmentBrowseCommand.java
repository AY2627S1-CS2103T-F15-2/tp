package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Locale;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.query.InventoryGroups;

/**
 * Opens a named inventory group or resolves a compact unit reference without changing records.
 * References are resolved against all retained units, independently of directory filters and sorting.
 */
public final class EquipmentBrowseCommand extends Command {
    private final InventoryGroups.Key group;
    private final String uuidReference;

    private EquipmentBrowseCommand(InventoryGroups.Key group, String uuidReference) {
        this.group = group;
        this.uuidReference = uuidReference;
    }

    /**
     * Creates navigation to the exact normalized category/name pair.
     */
    public static EquipmentBrowseCommand forGroup(String category, String name) {
        InventoryGroups.Key key = new InventoryGroups.Key(requireNonNull(category), requireNonNull(name));
        return new EquipmentBrowseCommand(key, null);
    }

    /**
     * Creates navigation using the displayed UUID prefix and an optional four-character suffix.
     *
     * @throws IllegalArgumentException if the reference is not eight hex characters with an optional suffix.
     */
    public static EquipmentBrowseCommand forShortUuid(String reference) {
        if (!requireNonNull(reference).matches("(?i)[0-9a-f]{8}(?:[-…][0-9a-f]{4})?")) {
            throw new IllegalArgumentException("Use eight UUID hex characters, optionally followed by -"
                    + " and the last four, for short/.");
        }
        return new EquipmentBrowseCommand(null, reference.toLowerCase(Locale.ROOT).replace('…', '-'));
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Equipment> units = model.getAddressBook().getEquipmentList();
        if (group != null) {
            if (units.stream().noneMatch(unit -> InventoryGroups.keyOf(unit).equals(group))) {
                throw new CommandException("No equipment group " + group + " exists.");
            }
            return new CommandResult("Viewing equipment group: " + group + ".", DisplayRequest.equipmentGroup(group));
        }
        List<Equipment> matches = units.stream()
                .filter(this::matchesReference).toList();
        if (matches.isEmpty()) {
            throw new CommandException("No equipment with compact UUID " + uuidReference + " exists.");
        }
        if (matches.size() > 1) {
            throw new CommandException("Compact UUID " + uuidReference
                    + " matches multiple retained units. Use equipment view uuid/FULL_UUID.");
        }
        return new EquipmentViewCommand(matches.getFirst().getUuid()).execute(model);
    }

    /**
     * Matches both visible ends when supplied, retaining ambiguity checks across the complete dataset.
     */
    private boolean matchesReference(Equipment unit) {
        String uuid = unit.getUuid().toString();
        return uuid.startsWith(uuidReference.substring(0, 8))
                && (uuidReference.length() == 8 || uuid.endsWith(uuidReference.substring(9)));
    }
}
