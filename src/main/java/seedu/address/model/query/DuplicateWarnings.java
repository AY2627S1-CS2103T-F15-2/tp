package seedu.address.model.query;

import java.util.List;

import seedu.address.commons.util.LaplaceText;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.member.Member;

/** Advisory similarity only. Identity uniqueness remains enforced by the existing domain collections. */
public final class DuplicateWarnings {
    private DuplicateWarnings() {
    }

    /** Reports other member identities sharing a normalized contact or name field. */
    public static String member(ReadOnlyAddressBook data, Member candidate) {
        List<String> matches = data.getMemberList().stream()
                .filter(member -> !member.isDeleted())
                .filter(member -> !member.getNusId().equals(candidate.getNusId()))
                .filter(member -> same(member.getName().fullName, candidate.getName().fullName)
                        || same(member.getPhone().value, candidate.getPhone().value)
                        || same(member.getEmail().value, candidate.getEmail().value))
                .map(member -> member.getNusId().value).sorted().toList();
        return warning(matches);
    }

    /** Reports same-type physical units without rejecting legitimate multiple units. */
    public static String equipment(ReadOnlyAddressBook data, Equipment candidate) {
        InventoryGroups.Key key = InventoryGroups.keyOf(candidate);
        List<String> matches = data.getEquipmentList().stream()
                .filter(item -> !item.getUuid().equals(candidate.getUuid()))
                .filter(item -> InventoryGroups.keyOf(item).equals(key))
                .map(item -> item.getUuid().toString()).sorted().toList();
        return warning(matches);
    }

    private static boolean same(String first, String second) {
        return LaplaceText.caseKey(LaplaceText.normalize(first))
                .equals(LaplaceText.caseKey(LaplaceText.normalize(second)));
    }

    private static String warning(List<String> matches) {
        return matches.isEmpty() ? "" : "\nPossible duplicate(s): " + String.join(", ", matches)
                + ". Distinct identities are retained; review these records.";
    }
}
