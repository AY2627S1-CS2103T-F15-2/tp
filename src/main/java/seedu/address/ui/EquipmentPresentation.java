package seedu.address.ui;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;

import seedu.address.model.equipment.Availability;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.equipment.exceptions.EquipmentNotFoundException;

/**
 * Formats derived equipment states consistently without changing command or storage values.
 */
final class EquipmentPresentation {
    private EquipmentPresentation() {
    }

    /**
     * Returns a readable state label, including compound conditions such as Under repair.
     */
    static String label(Enum<?> state) {
        String words = state.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        return Character.toUpperCase(words.charAt(0)) + words.substring(1);
    }

    /**
     * Returns the UUID prefix and suffix shown in compact tables, retaining both ends for visual checks.
     */
    static String shortUuid(UUID uuid) {
        String value = uuid.toString();
        return value.substring(0, 8) + "…" + value.substring(value.length() - 4);
    }

    /**
     * Counts active availability and retained deletions within exactly the supplied unit scope.
     */
    static String counts(List<Equipment> units, Function<UUID, Availability> lookup) {
        EnumMap<Availability, Integer> counts = new EnumMap<>(Availability.class);
        int deleted = 0;
        try {
            for (Equipment unit : units) {
                if (unit.isDeleted()) {
                    deleted++;
                } else {
                    counts.merge(lookup.apply(unit.getUuid()), 1, Integer::sum);
                }
            }
        } catch (EquipmentNotFoundException exception) {
            return "Availability could not be determined.";
        }
        return counts.getOrDefault(Availability.AVAILABLE, 0) + " available · "
                + counts.getOrDefault(Availability.ASSIGNED, 0) + " assigned · "
                + counts.getOrDefault(Availability.UNAVAILABLE, 0) + " unavailable"
                + (deleted == 0 ? "" : " · " + deleted + " deleted");
    }
}
