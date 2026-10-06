package seedu.address.model.member;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;

public class MemberTest {

    private static final NusId NUS_ID = new NusId("A0123456X");
    private static final Name NAME = new Name("Alex Tan");
    private static final Phone PHONE = new Phone("91234567");
    private static final Email EMAIL = new Email("alex@example.com");

    @Test
    public void constructor_nullField_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Member(null, NAME, PHONE, EMAIL));
        assertThrows(NullPointerException.class, () -> new Member(NUS_ID, null, PHONE, EMAIL));
        assertThrows(NullPointerException.class, () -> new Member(NUS_ID, NAME, null, EMAIL));
        assertThrows(NullPointerException.class, () -> new Member(NUS_ID, NAME, PHONE, null));
    }

    @Test
    public void getters_returnConstructorValues() {
        Member member = new Member(NUS_ID, NAME, PHONE, EMAIL);

        assertEquals(NUS_ID, member.getNusId());
        assertEquals(NAME, member.getName());
        assertEquals(PHONE, member.getPhone());
        assertEquals(EMAIL, member.getEmail());
    }

    @Test
    public void isSameMember() {
        Member member = new Member(NUS_ID, NAME, PHONE, EMAIL);

        assertTrue(member.isSameMember(member));
        assertTrue(member.isSameMember(new Member(new NusId("a0123456x"),
                new Name("Different Name"), new Phone("87654321"), new Email("different@example.com"))));
        assertFalse(member.isSameMember(null));
        assertFalse(member.isSameMember(new Member(new NusId("A7654321X"), NAME, PHONE, EMAIL)));
    }

    @Test
    public void equals() {
        Member member = new Member(NUS_ID, NAME, PHONE, EMAIL);
        Member copy = new Member(new NusId("a0123456x"), new Name("Alex Tan"),
                new Phone("91234567"), new Email("alex@example.com"));

        assertTrue(member.equals(member));
        assertTrue(member.equals(copy));
        assertFalse(member.equals(null));
        assertFalse(member.equals("member"));
        assertFalse(member.equals(new Member(new NusId("A7654321X"), NAME, PHONE, EMAIL)));
        assertFalse(member.equals(new Member(NUS_ID, new Name("Alice Tan"), PHONE, EMAIL)));
        assertFalse(member.equals(new Member(NUS_ID, NAME, new Phone("87654321"), EMAIL)));
        assertFalse(member.equals(new Member(NUS_ID, NAME, PHONE, new Email("other@example.com"))));
        assertEquals(member.hashCode(), copy.hashCode());
    }

    @Test
    public void toStringMethod() {
        Member member = new Member(NUS_ID, NAME, PHONE, EMAIL);
        String expected = Member.class.getCanonicalName()
                + "{nusId=A0123456X, name=Alex Tan, phone=91234567, email=alex@example.com}";

        assertEquals(expected, member.toString());
    }
}
