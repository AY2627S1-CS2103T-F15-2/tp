package seedu.address.logic.commands;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.loan.Loan;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.testutil.TypicalPersons;

/** Fixtures owned by the loan command tests; equal names never substitute for identity. */
final class LoanCommandTestData {
    static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-14T18:00:00Z"), ZoneId.of("Asia/Singapore"));
    static final LocalDate TODAY = LocalDate.of(2026, 10, 15);
    static final UUID FIRST_UUID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    static final UUID SECOND_UUID = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
    static final UUID UNKNOWN_UUID = UUID.fromString("550e8400-e29b-41d4-a716-446655440099");
    static final NusId UNKNOWN_MEMBER = new NusId("A0999999Z");
    static final Member FIRST_MEMBER = member("A0123456X");
    static final Member SECOND_MEMBER = member("A0234567R");

    private LoanCommandTestData() {}

    static ModelManager model() {
        return model(Condition.GOOD);
    }

    static ModelManager model(Condition condition) {
        AddressBook book = new AddressBook();
        book.addPerson(TypicalPersons.ALICE);
        book.addMember(FIRST_MEMBER);
        book.addMember(SECOND_MEMBER);
        book.addEquipment(new Equipment(FIRST_UUID, "Camera", "Photography", condition, "Case included"));
        book.addEquipment(new Equipment(SECOND_UUID, "Camera", "Photography", Condition.FAIR, "Strap included"));
        book.addLoan(new Loan(SECOND_UUID, SECOND_MEMBER.getNusId(), TODAY.minusDays(20),
                TODAY.minusDays(15), TODAY.minusDays(14)));
        return new ModelManager(book, new UserPrefs());
    }

    static Loan openLoan() {
        return new Loan(FIRST_UUID, FIRST_MEMBER.getNusId(), TODAY.minusDays(3), TODAY.minusDays(1), null);
    }

    private static Member member(String id) {
        return new Member(new NusId(id), new Name("Alex Tan"), new Phone("91234567"), new Email("alex@example.com"));
    }

    /** Advances only the injected test clock; the process-wide clock is never changed. */
    static final class MutableClock extends Clock {
        private Clock current = CLOCK;

        void advanceDay() {
            current = Clock.offset(current, Duration.ofDays(1));
        }

        @Override
        public ZoneId getZone() {
            return current.getZone();
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return current.withZone(zone);
        }

        @Override
        public Instant instant() {
            return current.instant();
        }
    }
}
