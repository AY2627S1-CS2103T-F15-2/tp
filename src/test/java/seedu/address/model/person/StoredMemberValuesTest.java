package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** Checks that stored member values accept both supported formats without normalizing saved data. */
public class StoredMemberValuesTest {
    @Test
    public void fromStoredMember_legacyValues_preservesExactStoredForm() {
        assertEquals("Member  123", Name.fromStoredMember("Member  123").fullName);
        assertEquals("123", Phone.fromStoredMember("123").value);
        assertEquals("UPPER@EXAMPLE.COM", Email.fromStoredMember("UPPER@EXAMPLE.COM").value);
    }

    @Test
    public void fromStoredMember_mvpValues_preservesPunctuationInternationalPhoneAndEmailCase() {
        assertEquals("Anne-Marie  O'Connor", Name.fromStoredMember("Anne-Marie  O'Connor").fullName);
        assertEquals("+6591234567", Phone.fromStoredMember("+6591234567").value);
        assertEquals("%ANNE@EXAMPLE.COM", Email.fromStoredMember("%ANNE@EXAMPLE.COM").value);
    }

    @Test
    public void fromStoredMember_invalidUnderBothFormats_rejectsInsteadOfRepairing() {
        assertThrows(IllegalArgumentException.class, () -> Name.fromStoredMember("Alex@Tan"));
        assertThrows(IllegalArgumentException.class, () -> Phone.fromStoredMember("+65 9123-4567"));
        assertThrows(IllegalArgumentException.class, () -> Email.fromStoredMember("anne@example..com"));
    }
}
