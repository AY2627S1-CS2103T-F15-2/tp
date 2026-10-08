package seedu.address.storage;

import java.time.LocalDate;
import java.util.UUID;

import seedu.address.model.AddressBook;
import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.loan.Loan;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.testutil.TypicalPersons;

/** Real domain data shared by the root persistence tests. */
final class RootPersistenceTestData {
    private RootPersistenceTestData() {}

    static AddressBook fullAddressBook() {
        AddressBook book = new AddressBook();
        book.addPerson(TypicalPersons.ALICE);
        NusId memberId = new NusId("A0123456X");
        UUID equipmentId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        book.addMember(new Member(memberId, new Name("Alex Tan"), new Phone("91234567"),
                new Email("alex@example.com")));
        book.addEquipment(new Equipment(equipmentId, "Camera", "Photography", Condition.GOOD, "Case included"));
        book.addLoan(new Loan(equipmentId, memberId, LocalDate.of(2026, 10, 6),
                LocalDate.of(2026, 10, 13), null));
        book.addLoan(new Loan(equipmentId, memberId, LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 8), LocalDate.of(2026, 9, 7)));
        return book;
    }
}
