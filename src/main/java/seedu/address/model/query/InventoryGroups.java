package seedu.address.model.query;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.equipment.Availability;
import seedu.address.model.equipment.Equipment;

/**
 * Inventory planning by normalized category and equipment name, retaining every eligible physical unit.
 */
public final class InventoryGroups {
    private InventoryGroups() {
    }

    /**
     * Category and type are separate keys even when either contains a display separator.
     */
    public record Key(String category, String name) {
        /**
         * Normalizes a presentation key without changing any stored unit.
         */
        public Key {
            category = DirectoryQuery.normalize(category);
            name = DirectoryQuery.normalize(name);
        }

        @Override
        public String toString() {
            return "\"" + category.replace("\"", "\"\"") + "\" / \"" + name.replace("\"", "\"\"") + "\"";
        }
    }

    /**
     * A derived group retains every physical unit and separately identifies query matches.
     */
    public record Group(Key key, List<Equipment> units, List<Equipment> matching) {
        /**
         * Copies the lists so a view cannot mutate group membership.
         */
        public Group {
            units = List.copyOf(units);
            matching = List.copyOf(matching);
        }

        public String name() {
            return units.getFirst().getName();
        }

        public String category() {
            return units.getFirst().getCategory();
        }
    }

    /**
     * Returns the normalized category/name key for one physical unit.
     */
    public static Key keyOf(Equipment unit) {
        return new Key(unit.getCategory(), unit.getName());
    }

    /**
     * Groups matches in their query order while retaining all units for scoped counts and navigation.
     */
    public static List<Group> group(ReadOnlyAddressBook data, List<Equipment> matching) {
        Map<Key, List<Equipment>> all = new LinkedHashMap<>();
        data.getEquipmentList().forEach(unit ->
                all.computeIfAbsent(keyOf(unit), unused -> new ArrayList<>()).add(unit));
        Map<Key, List<Equipment>> matches = new LinkedHashMap<>();
        matching.forEach(unit -> matches.computeIfAbsent(keyOf(unit), unused -> new ArrayList<>()).add(unit));
        return matches.entrySet().stream().map(entry ->
                new Group(entry.getKey(), all.get(entry.getKey()), entry.getValue())).toList();
    }

    /**
     * Counts are derived from current loans/condition and are never persisted.
     */
    public static List<String> summarize(ReadOnlyAddressBook data, Function<UUID, Availability> lookup,
            Predicate<Equipment> eligible) {
        Map<Key, EnumMap<Availability, Integer>> counts = new TreeMap<>(
                Comparator.comparing(Key::category).thenComparing(Key::name));
        data.getEquipmentList().stream().filter(item -> !item.isDeleted()).filter(eligible).forEach(item -> {
            Key key = new Key(DirectoryQuery.normalize(item.getCategory()),
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
