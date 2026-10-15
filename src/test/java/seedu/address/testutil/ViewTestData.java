package seedu.address.testutil;

import java.time.LocalDate;
import java.util.UUID;

import seedu.address.model.ModelManager;
import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.loan.Loan;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;

/** Fixtures scoped to the view command, parser and detail panel tests. */
public final class ViewTestData {
    public static final Member ALEX = new Member(new NusId("A0123456X"), new Name("Alex Tan"),
            new Phone("91234567"), new Email("alex@example.com"));
    public static final Member OTHER_ALEX = new Member(new NusId("A0234567R"), new Name("Alex Tan"),
            new Phone("98765432"), new Email("other@example.com"));
    public static final Equipment CAMERA = new Equipment(new UUID(0, 1), "Camera", "Photography", Condition.GOOD,
            "Keep the lens cap on.\nCase included.");
    public static final Equipment OTHER_CAMERA = new Equipment(new UUID(0, 2), "Camera", "Photography", Condition.FAIR,
            "Spare battery included.");
    public static final Equipment SPEAKER = new Equipment(new UUID(0, 3), "Speaker", "Audio", Condition.DAMAGED, "");
    public static final LocalDate ASSIGNED = LocalDate.of(2026, 1, 1);
    public static final LocalDate EXPECTED = ASSIGNED.plusDays(7);

    private ViewTestData() {}

    /** Returns real data with duplicate display names, distinct identities and a legacy person. */
    public static ModelManager model() {
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder().build());
        model.addMember(ALEX);
        model.addMember(OTHER_ALEX);
        model.addEquipment(OTHER_CAMERA);
        model.addEquipment(CAMERA);
        model.addEquipment(SPEAKER);
        return model;
    }

    /** Makes a loan with fixed dates; a closed loan is returned on its expected date. */
    public static Loan loan(Equipment equipment, Member member, boolean open) {
        return new Loan(equipment.getUuid(), member.getNusId(), ASSIGNED, EXPECTED, open ? null : EXPECTED);
    }
}
