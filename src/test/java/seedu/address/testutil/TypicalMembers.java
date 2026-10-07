package seedu.address.testutil;

import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;

/** A utility class containing sample members for tests. */
public class TypicalMembers {

    public static final Member ALEX = new Member(new NusId("A0123456X"), new Name("Alex Tan"),
            new Phone("91234567"), new Email("alex@example.com"));
    public static final Member BEN = new Member(new NusId("E7654321"), new Name("Ben Lim"),
            new Phone("87654321"), new Email("ben@example.com"));

    private TypicalMembers() {}
}
