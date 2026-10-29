package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;

/**
 * Converts retained member records between JSON fields and immutable model values.
 */
class JsonAdaptedMember {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Member's %s field is missing!";

    private final String nusId;
    private final String name;
    private final String phone;
    private final String email;
    private boolean deleted;

    /**
     * Constructs an adapter from raw JSON fields, deferring validation until model conversion.
     */
    @JsonCreator
    public JsonAdaptedMember(@JsonProperty("nusId") String nusId,
            @JsonProperty("name") String name, @JsonProperty("phone") String phone,
            @JsonProperty("email") String email) {
        this.nusId = nusId;
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    /**
     * Constructs an adapter preserving a domain member's stored details and deletion state.
     */
    public JsonAdaptedMember(Member source) {
        nusId = source.getNusId().value;
        name = source.getName().fullName;
        phone = source.getPhone().value;
        email = source.getEmail().value;
        deleted = source.isDeleted();
    }

    /**
     * Validates an explicitly supplied flag; absent legacy fields retain the default false value.
     */
    @JsonSetter("deleted")
    void setDeleted(JsonNode value) {
        if (value == null || !value.isBoolean()) {
            throw new IllegalArgumentException("Member deleted must be a boolean.");
        }
        deleted = value.booleanValue();
    }

    /**
     * Converts this adapter to its domain type after validating each stored field in declaration order.
     */
    public Member toModelType() throws IllegalValueException {
        return new Member(parseStoredNusId(), parseStoredName(), parseStoredPhone(), parseStoredEmail(), deleted);
    }

    /**
     * Parses the stored identity without allowing invalid or missing NUS IDs.
     */
    private NusId parseStoredNusId() throws IllegalValueException {
        requireField(nusId, NusId.class);
        if (!NusId.isValidNusId(nusId)) {
            throw new IllegalValueException(NusId.MESSAGE_CONSTRAINTS);
        }
        return new NusId(nusId);
    }

    /**
     * Parses a supported legacy or current member name without changing its stored spelling.
     */
    private Name parseStoredName() throws IllegalValueException {
        requireField(name, Name.class);
        if (!Name.isValidName(name) && !Name.isValidMemberName(name)) {
            throw new IllegalValueException(Name.MESSAGE_CONSTRAINTS);
        }
        return Name.fromStoredMember(name);
    }

    /**
     * Parses a supported legacy or current phone without normalizing the saved value.
     */
    private Phone parseStoredPhone() throws IllegalValueException {
        requireField(phone, Phone.class);
        if (!Phone.isValidPhone(phone) && !Phone.isValidMemberPhone(phone)) {
            throw new IllegalValueException(Phone.MESSAGE_CONSTRAINTS);
        }
        return Phone.fromStoredMember(phone);
    }

    /**
     * Parses a supported legacy or current email while retaining the exact stored value.
     */
    private Email parseStoredEmail() throws IllegalValueException {
        requireField(email, Email.class);
        if (!Email.isValidEmail(email) && !Email.isValidMemberEmail(email)) {
            throw new IllegalValueException(Email.MESSAGE_CONSTRAINTS);
        }
        return Email.fromStoredMember(email);
    }

    /**
     * Rejects an absent stored field using the adapter's established field-specific diagnostic.
     */
    private static void requireField(String value, Class<?> fieldType) throws IllegalValueException {
        if (value == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, fieldType.getSimpleName()));
        }
    }
}
