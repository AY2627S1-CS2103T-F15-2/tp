package seedu.address.logic.commands;

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
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.testutil.PersonBuilder;

/**
 * Small deletion-specific fixtures with equal descriptions and distinct identities.
 */
final class DeletionTestData {

    static final Member FIRST_MEMBER = member("A0123456X");
    static final Member SECOND_MEMBER = member("A0234567R");
    static final Equipment FIRST_EQUIPMENT = equipment("550e8400-e29b-41d4-a716-446655440000");
    static final Equipment SECOND_EQUIPMENT = equipment("550e8400-e29b-41d4-a716-446655440001");
    static final Person LEGACY_PERSON = new PersonBuilder().withName("Legacy Person").build();
    static final LocalDate ASSIGNED = LocalDate.of(2026, 1, 1);
    static final LocalDate RETURNED = LocalDate.of(2026, 1, 3);

    private DeletionTestData() {}

    /**
     * Builds a real model containing both member/item identities and legacy person data.
     */
    static ModelManager model() {
        ModelManager model = new ModelManager();
        model.addPerson(LEGACY_PERSON);
        model.addMember(FIRST_MEMBER);
        model.addMember(SECOND_MEMBER);
        model.addEquipment(FIRST_EQUIPMENT);
        model.addEquipment(SECOND_EQUIPMENT);
        return model;
    }

    /**
     * Creates an open loan or a closed historical loan for existing fixture identities.
     */
    static Loan loan(Equipment equipment, Member member, boolean isOpen) {
        return new Loan(equipment.getUuid(), member.getNusId(), ASSIGNED, ASSIGNED.plusDays(7),
                isOpen ? null : RETURNED);
    }

    private static Member member(String nusId) {
        return new Member(new NusId(nusId), new Name("Alex Tan"), new Phone("91234567"),
                new Email("alex@example.com"));
    }

    private static Equipment equipment(String uuid) {
        return new Equipment(UUID.fromString(uuid), "Camera", "Photography", Condition.GOOD, "Same description");
    }
}
