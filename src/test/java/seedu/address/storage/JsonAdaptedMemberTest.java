package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.storage.JsonAdaptedMember.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalMembers.ALEX;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.member.NusId;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;

public class JsonAdaptedMemberTest {

    private static final String VALID_NUS_ID = ALEX.getNusId().toString();
    private static final String VALID_NAME = ALEX.getName().toString();
    private static final String VALID_PHONE = ALEX.getPhone().toString();
    private static final String VALID_EMAIL = ALEX.getEmail().toString();

    @Test
    public void toModelType_validMember_returnsMember() throws Exception {
        assertEquals(ALEX, new JsonAdaptedMember(ALEX).toModelType());
    }

    @Test
    public void toModelType_lowercaseNusId_normalizesNusId() throws Exception {
        JsonAdaptedMember adaptedMember =
                new JsonAdaptedMember("a0123456x", VALID_NAME, VALID_PHONE, VALID_EMAIL);

        assertEquals(ALEX, adaptedMember.toModelType());
    }

    @Test
    public void toModelType_invalidNusId_throwsIllegalValueException() {
        JsonAdaptedMember adaptedMember =
                new JsonAdaptedMember("not-an-id", VALID_NAME, VALID_PHONE, VALID_EMAIL);

        assertThrows(IllegalValueException.class, NusId.MESSAGE_CONSTRAINTS, adaptedMember::toModelType);
    }

    @Test
    public void toModelType_nullNusId_throwsIllegalValueException() {
        JsonAdaptedMember adaptedMember =
                new JsonAdaptedMember(null, VALID_NAME, VALID_PHONE, VALID_EMAIL);

        assertMissingField(NusId.class, adaptedMember);
    }

    @Test
    public void toModelType_invalidName_throwsIllegalValueException() {
        JsonAdaptedMember adaptedMember =
                new JsonAdaptedMember(VALID_NUS_ID, "R@chel", VALID_PHONE, VALID_EMAIL);

        assertThrows(IllegalValueException.class, Name.MESSAGE_CONSTRAINTS, adaptedMember::toModelType);
    }

    @Test
    public void toModelType_nullName_throwsIllegalValueException() {
        JsonAdaptedMember adaptedMember =
                new JsonAdaptedMember(VALID_NUS_ID, null, VALID_PHONE, VALID_EMAIL);

        assertMissingField(Name.class, adaptedMember);
    }

    @Test
    public void toModelType_invalidPhone_throwsIllegalValueException() {
        JsonAdaptedMember adaptedMember =
                new JsonAdaptedMember(VALID_NUS_ID, VALID_NAME, "+651234", VALID_EMAIL);

        assertThrows(IllegalValueException.class, Phone.MESSAGE_CONSTRAINTS, adaptedMember::toModelType);
    }

    @Test
    public void toModelType_nullPhone_throwsIllegalValueException() {
        JsonAdaptedMember adaptedMember =
                new JsonAdaptedMember(VALID_NUS_ID, VALID_NAME, null, VALID_EMAIL);

        assertMissingField(Phone.class, adaptedMember);
    }

    @Test
    public void toModelType_invalidEmail_throwsIllegalValueException() {
        JsonAdaptedMember adaptedMember =
                new JsonAdaptedMember(VALID_NUS_ID, VALID_NAME, VALID_PHONE, "example.com");

        assertThrows(IllegalValueException.class, Email.MESSAGE_CONSTRAINTS, adaptedMember::toModelType);
    }

    @Test
    public void toModelType_nullEmail_throwsIllegalValueException() {
        JsonAdaptedMember adaptedMember =
                new JsonAdaptedMember(VALID_NUS_ID, VALID_NAME, VALID_PHONE, null);

        assertMissingField(Email.class, adaptedMember);
    }

    private void assertMissingField(Class<?> fieldClass, JsonAdaptedMember adaptedMember) {
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, fieldClass.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, adaptedMember::toModelType);
    }
}
