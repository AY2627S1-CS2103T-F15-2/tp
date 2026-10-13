package seedu.address.commons.util;

import java.util.Comparator;
import java.util.Locale;

import seedu.address.model.equipment.Equipment;
import seedu.address.model.member.Member;

/** Shared directory ordering; use on copies or streams, never mutate root collections. */
public final class LaplaceSortOrder {

    public static final Comparator<Member> MEMBERS = Comparator
            .comparing((Member member) -> normalize(member.getName().fullName))
            .thenComparing(member -> normalize(member.getNusId().value));

    public static final Comparator<Equipment> EQUIPMENT = Comparator
            .comparing((Equipment equipment) -> normalize(equipment.getCategory()))
            .thenComparing(equipment -> normalize(equipment.getName()))
            .thenComparing(equipment -> equipment.getUuid().toString());

    private LaplaceSortOrder() {
    }

    private static String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
