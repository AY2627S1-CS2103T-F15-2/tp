package seedu.address.model.query;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.equipment.Availability;
import seedu.address.model.equipment.Equipment;

/** Inventory planning by normalized category and equipment name, retaining every eligible physical unit. */
public final class InventoryGroups {
    /** Category and type are separate keys even when either contains a display separator. */
    private record Group(String category, String name) {
        @Override
        public String toString() {
            return "\"" + category.replace("\"", "\"\"") + "\" / \"" + name.replace("\"", "\"\"") + "\"";
        }
    }

    private InventoryGroups() {
    }

    /** Counts are derived from current loans/condition and are never persisted. */
    public static List<String> summarize(ReadOnlyAddressBook data, Function<UUID, Availability> lookup,
            Predicate<Equipment> eligible) {
        Map<Group, EnumMap<Availability, Integer>> counts = new TreeMap<>(
                Comparator.comparing(Group::category).thenComparing(Group::name));
        data.getEquipmentList().stream().filter(item -> !item.isDeleted()).filter(eligible).forEach(item -> {
            Group key = new Group(DirectoryQuery.normalize(item.getCategory()),
                    DirectoryQuery.normalize(item.getName()));
            EnumMap<Availability, Integer> values = counts.computeIfAbsent(key,
                    unused -> new EnumMap<>(Availability.class));
            values.merge(lookup.apply(item.getUuid()), 1, Integer::sum);
        });
        return counts.entrySet().stream().map(entry -> entry.getKey() + ": "
                + entry.getValue().getOrDefault(Availability.AVAILABLE, 0) + " available, "
                + entry.getValue().getOrDefault(Availability.ASSIGNED, 0) + " assigned, "
                + entry.getValue().getOrDefault(Availability.UNAVAILABLE, 0) + " unavailable").toList();
    }
}
