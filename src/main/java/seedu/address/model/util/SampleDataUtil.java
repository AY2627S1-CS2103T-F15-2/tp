package seedu.address.model.util;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.loan.Loan;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.Tag;

/**
 * Contains utility methods for populating {@code AddressBook} with sample data.
 */
public class SampleDataUtil {
    public static Person[] getSamplePersons() {
        return new Person[] {
            new Person(new Name("Alex Yeoh"), new Phone("87438807"), new Email("alexyeoh@example.com"),
                new Address("Blk 30 Geylang Street 29, #06-40"),
                getTagSet("friends")),
            new Person(new Name("Bernice Yu"), new Phone("99272758"), new Email("berniceyu@example.com"),
                new Address("Blk 30 Lorong 3 Serangoon Gardens, #07-18"),
                getTagSet("colleagues", "friends")),
            new Person(new Name("Charlotte Oliveiro"), new Phone("93210283"), new Email("charlotte@example.com"),
                new Address("Blk 11 Ang Mo Kio Street 74, #11-04"),
                getTagSet("neighbours")),
            new Person(new Name("David Li"), new Phone("91031282"), new Email("lidavid@example.com"),
                new Address("Blk 436 Serangoon Gardens Street 26, #16-43"),
                getTagSet("family")),
            new Person(new Name("Irfan Ibrahim"), new Phone("92492021"), new Email("irfan@example.com"),
                new Address("Blk 47 Tampines Street 20, #17-35"),
                getTagSet("classmates")),
            new Person(new Name("Roy Balakrishnan"), new Phone("92624417"), new Email("royb@example.com"),
                new Address("Blk 45 Aljunied Street 85, #11-31"),
                getTagSet("colleagues"))
        };
    }

    /**
     * Returns a fresh sample book for startup when no saved data exists.
     */
    public static ReadOnlyAddressBook getSampleAddressBook() {
        return getSampleAddressBook(Clock.systemDefaultZone());
    }

    /**
     * Builds sample relationships relative to the supplied clock, without touching stored data.
     */
    static ReadOnlyAddressBook getSampleAddressBook(Clock clock) {
        requireNonNull(clock);
        LocalDate today = LocalDate.now(clock);
        AddressBook sampleAb = new AddressBook();
        for (Person samplePerson : getSamplePersons()) {
            sampleAb.addPerson(samplePerson);
        }

        NusId alexId = new NusId("A0123456X");
        sampleAb.addMember(new Member(alexId, new Name("Alex Tan"), new Phone("91234567"),
                new Email("alex.tan@example.com")));
        sampleAb.addMember(new Member(new NusId("A0234567R"), new Name("Jamie Lim"), new Phone("98765432"),
                new Email("jamie.lim@example.com")));
        sampleAb.addMember(new Member(new NusId("A0345678M"), new Name("Priya Nair"), new Phone("90123456"),
                new Email("priya.nair@example.com")));

        UUID availableCameraId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        UUID assignedCameraId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        sampleAb.addEquipment(new Equipment(availableCameraId, "Camera", "Photography", Condition.GOOD,
                "Case included"));
        sampleAb.addEquipment(new Equipment(assignedCameraId, "Camera", "Photography", Condition.FAIR,
                "Strap included"));
        sampleAb.addEquipment(new Equipment(UUID.fromString("550e8400-e29b-41d4-a716-446655440002"),
                "Tripod", "Photography", Condition.DAMAGED, "Loose leg; do not issue"));
        // This intentional legacy example has no recorded issue observation.
        sampleAb.addLoan(new Loan(assignedCameraId, alexId, today.minusDays(2), today.plusDays(5), null));
        addMoreSampleData(sampleAb, today, alexId);
        return sampleAb;
    }

    /** Adds overdue, returned and multiple-holding examples alongside the basic camera workflow. */
    private static void addMoreSampleData(AddressBook book, LocalDate today, NusId alexId) {
        book.addMember(new Member(new NusId("A0456789B"), Name.forMember("Siti Nur"),
                Phone.forMember("+65 8123-4567"), Email.forMember("siti.nur@example.com")));
        book.addMember(new Member(new NusId("A0567890C"), Name.forMember("Daniel Wong"),
                Phone.forMember("92345678"), Email.forMember("daniel.wong@example.com")));

        UUID microphoneId = UUID.fromString("550e8400-e29b-41d4-a716-446655440003");
        UUID speakerId = UUID.fromString("550e8400-e29b-41d4-a716-446655440004");
        UUID projectorId = UUID.fromString("550e8400-e29b-41d4-a716-446655440005");
        UUID laptopId = UUID.fromString("550e8400-e29b-41d4-a716-446655440006");
        book.addEquipment(new Equipment(microphoneId, "Wireless Microphone", "Audio", Condition.GOOD,
                "Includes receiver and two rechargeable batteries"));
        book.addEquipment(new Equipment(speakerId, "Portable Speaker", "Audio", Condition.FAIR,
                "Minor scratches; charging cable included"));
        book.addEquipment(new Equipment(projectorId, "Projector", "Presentation", Condition.UNDER_REPAIR,
                "Lamp replacement pending; do not issue"));
        book.addEquipment(new Equipment(laptopId, "Event Laptop", "Computing", Condition.GOOD,
                "Power adapter and carry case included"));

        book.addLoan(new Loan(microphoneId, new NusId("A0234567R"), today.minusDays(7), today.minusDays(1),
                null, Condition.GOOD, null));
        book.addLoan(new Loan(laptopId, alexId, today.minusDays(1), today.plusDays(3), null, Condition.GOOD, null));
        book.addLoan(new Loan(speakerId, new NusId("A0345678M"), today.minusDays(5), today.minusDays(2),
                today.minusDays(3), Condition.FAIR, Condition.FAIR));
        book.addLoan(new Loan(UUID.fromString("550e8400-e29b-41d4-a716-446655440002"),
                new NusId("A0456789B"), today.minusDays(10), today.minusDays(5), today.minusDays(4),
                Condition.GOOD, Condition.DAMAGED));
    }

    /**
     * Returns a tag set containing the list of strings given.
     */
    public static Set<Tag> getTagSet(String... strings) {
        return Arrays.stream(strings)
                .map(Tag::new)
                .collect(Collectors.toSet());
    }

}
