package seedu.address.model.member;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.member.exceptions.DuplicateMemberException;
import seedu.address.model.member.exceptions.MemberNotFoundException;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;

public class UniqueMemberListTest {

    private static final NusId NUS_ID = new NusId("A0123456X");
    private static final Member MEMBER = new Member(NUS_ID, new Name("Alex Tan"),
            new Phone("91234567"), new Email("alex@example.com"));

    @Test
    public void scaffoldOperations_throwUnsupportedOperationException() {
        UniqueMemberList members = new UniqueMemberList();

        assertThrows(UnsupportedOperationException.class, () -> members.contains(NUS_ID));
        assertThrows(UnsupportedOperationException.class, () -> members.findByNusId(NUS_ID));
        assertThrows(UnsupportedOperationException.class, () -> members.add(MEMBER));
        assertThrows(UnsupportedOperationException.class, () -> members.remove(MEMBER));
        assertThrows(UnsupportedOperationException.class, () -> members.setMembers(List.of(MEMBER)));
        assertThrows(UnsupportedOperationException.class, members::asUnmodifiableObservableList);
    }

    @Test
    public void scaffoldExceptions_haveExpectedMessages() {
        assertEquals("Operation would result in duplicate members",
                new DuplicateMemberException().getMessage());
        assertNull(new MemberNotFoundException().getMessage());
    }
}
