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

/** Feature-specific search fixtures with duplicate descriptions and distinct stable identities. */
public final class SearchData {
    private SearchData() {
    }

    /** Builds deliberately unsorted real records with three availability states. */
    public static ModelManager sample() {
        ModelManager model = new ModelManager();
        model.addMember(member(2));
        model.addMember(member(1));
        model.addEquipment(item(3, Condition.DAMAGED));
        model.addEquipment(item(2, Condition.GOOD));
        model.addEquipment(item(1, Condition.GOOD));
        LocalDate date = LocalDate.of(2026, 1, 1);
        model.addLoan(new Loan(new UUID(0, 1), member(1).getNusId(), date, date.plusDays(1), null));
        return model;
    }

    /** Returns a distinct NUS identity sharing the same name/phone as other search fixtures. */
    public static Member member(int id) {
        return new Member(new NusId("A000000" + id + "X"), new Name("Alex Tan"), new Phone("91234567"),
                new Email("alex" + id + "@example.com"));
    }

    public static Equipment item(int id, Condition condition) {
        return new Equipment(new UUID(0, id), "Camera", "Photography", condition, "Travel kit");
    }
}
