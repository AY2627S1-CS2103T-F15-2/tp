package seedu.address.model.query;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

import seedu.address.commons.util.LaplaceSortOrder;
import seedu.address.commons.util.LaplaceText;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.equipment.Availability;
import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.member.Member;

/** Immutable, transient directory criteria. Queries sort snapshots and never change stored records. */
public record DirectoryQuery(String text, String category, Condition condition, Availability availability,
        Holding holding, Sort sort, boolean descending, Status status) {
    /** Whether a member must hold any current equipment. */
    public enum Holding { ANY, YES, NO }

    /** Lifecycle states searchable without erasing retained records. */
    public enum Status { ALL, ACTIVE, DELETED }

    /** Directory sort fields; parsers restrict fields to the applicable directory. */
    public enum Sort { DEFAULT, NAME, ID, HELD, CATEGORY, CONDITION, AVAILABILITY }

    /** Null condition/availability mean no restriction; all other criteria are required. */
    public DirectoryQuery {
        text = normalize(text);
        category = normalize(category);
        requireNonNull(holding);
        requireNonNull(sort);
        requireNonNull(status);
    }

    /** Preserves the all-records criteria used by existing directory reset callers. */
    public DirectoryQuery(String text, String category, Condition condition, Availability availability,
            Holding holding, Sort sort, boolean descending) {
        this(text, category, condition, availability, holding, sort, descending, Status.ALL);
    }

    public static DirectoryQuery all() {
        return new DirectoryQuery("", "", null, null, Holding.ANY, Sort.DEFAULT, false);
    }

    /** Returns normalized substring matches across member identity and contact fields. */
    public List<Member> members(ReadOnlyAddressBook data) {
        return members(data, member -> true);
    }

    /** Composes additional eligibility with the requested member lifecycle status. */
    public List<Member> members(ReadOnlyAddressBook data, Predicate<Member> eligible) {
        requireNonNull(eligible);
        Comparator<Member> comparator = switch (sort) {
            case ID -> Comparator.comparing(member -> member.getNusId().value);
            case HELD -> Comparator.comparingLong(member -> holdings(data, member));
            default -> LaplaceSortOrder.MEMBERS;
        };
        comparator = comparator.thenComparing(LaplaceSortOrder.MEMBERS);
        if (descending) {
            comparator = comparator.reversed();
        }
        return data.getMemberList().stream().filter(eligible)
                .filter(member -> matchesStatus(member.isDeleted()))
                .filter(member -> matches(member.getName().fullName, member.getNusId().value,
                        member.getPhone().value, member.getEmail().value))
                .filter(member -> holding == Holding.ANY || (holdings(data, member) > 0) == (holding == Holding.YES))
                .sorted(comparator).toList();
    }

    private boolean matchesStatus(boolean deleted) {
        return switch (status) {
            case ALL -> true;
            case ACTIVE -> !deleted;
            case DELETED -> deleted;
        };
    }

    public List<Equipment> equipment(ReadOnlyAddressBook data, Function<UUID, Availability> lookup) {
        return equipment(data, lookup, item -> true);
    }

    /** Eligibility is applied before joins/availability, so retained deleted items need not be looked up. */
    public List<Equipment> equipment(ReadOnlyAddressBook data, Function<UUID, Availability> lookup,
            Predicate<Equipment> eligible) {
        requireNonNull(lookup);
        requireNonNull(eligible);
        Comparator<Equipment> comparator = switch (sort) {
            case NAME -> Comparator.comparing(item -> normalize(item.getName()));
            case ID -> Comparator.comparing(item -> item.getUuid().toString());
            case CONDITION -> Comparator.comparing(item -> item.getCondition().name());
            case AVAILABILITY -> Comparator.comparing(item -> lookup.apply(item.getUuid()).name());
            default -> LaplaceSortOrder.EQUIPMENT;
        };
        comparator = comparator.thenComparing(LaplaceSortOrder.EQUIPMENT);
        if (descending) {
            comparator = comparator.reversed();
        }
        return data.getEquipmentList().stream().filter(eligible)
                .filter(item -> matchesStatus(item.isDeleted()))
                .filter(item -> matches(item.getUuid().toString(), item.getName(), item.getCategory(), item.getNotes()))
                .filter(item -> normalize(item.getCategory()).contains(category))
                .filter(item -> condition == null || item.getCondition() == condition)
                .filter(item -> availability == null || lookup.apply(item.getUuid()) == availability)
                .sorted(comparator).toList();
    }

    /** Counts only current loans for this exact member identity. */
    public static long holdings(ReadOnlyAddressBook data, Member member) {
        return data.getLoanList().stream()
                .filter(loan -> loan.isOpen() && loan.getMemberNusId().equals(member.getNusId())).count();
    }

    private boolean matches(String... fields) {
        for (String field : fields) {
            if (normalize(field).contains(text)) {
                return true;
            }
        }
        return false;
    }

    public static String normalize(String value) {
        return LaplaceText.normalize(value).toLowerCase(Locale.ROOT);
    }

    /** Human-readable criteria shown beside the actual results. */
    public String description() {
        List<String> filters = new ArrayList<>();
        if (!text.isEmpty()) {
            filters.add("Search: " + text);
        }
        if (!category.isEmpty()) {
            filters.add("Category: " + category);
        }
        if (condition != null) {
            filters.add("Condition: " + condition);
        }
        if (availability != null) {
            filters.add("Availability: " + availability);
        }
        if (holding != Holding.ANY) {
            filters.add("Holding equipment: " + holding);
        }
        if (status != Status.ALL) {
            filters.add("Status: " + status);
        }
        return filters.isEmpty() ? "All records" : String.join("; ", filters);
    }

    /** Describes sorting without implying that the underlying collection was reordered. */
    public String sortDescription(boolean members) {
        if (sort == Sort.DEFAULT && !descending) {
            return members ? "Name (A-Z), then NUS ID" : "Category, name, then UUID";
        }
        return sort.name().toLowerCase(Locale.ROOT) + (descending ? " descending" : " ascending")
                + "; deterministic identity ties";
    }
}
