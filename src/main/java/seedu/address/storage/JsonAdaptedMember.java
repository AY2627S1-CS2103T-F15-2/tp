package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;

/** Jackson-facing contract for a {@link Member}. */
class JsonAdaptedMember {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Member's %s field is missing!";

    private final String nusId;
    private final String name;
    private final String phone;
    private final String email;

    /** Constructs an adapter from raw JSON fields. */
    @JsonCreator
    public JsonAdaptedMember(@JsonProperty("nusId") String nusId,
            @JsonProperty("name") String name, @JsonProperty("phone") String phone,
            @JsonProperty("email") String email) {
        this.nusId = nusId;
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    /** Constructs an adapter from a domain member. */
    public JsonAdaptedMember(Member source) {
        nusId = source.getNusId().value;
        name = source.getName().fullName;
        phone = source.getPhone().value;
        email = source.getEmail().value;
    }

    /** Converts this adapter to its domain type. */
    public Member toModelType() throws IllegalValueException {
        if (nusId == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT,
                    NusId.class.getSimpleName()));
        }
        if (!NusId.isValidNusId(nusId)) {
            throw new IllegalValueException(NusId.MESSAGE_CONSTRAINTS);
        }
        NusId modelNusId = new NusId(nusId);

        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT,
                    Name.class.getSimpleName()));
        }
        if (!Name.isValidName(name)) {
            throw new IllegalValueException(Name.MESSAGE_CONSTRAINTS);
        }
        Name modelName = new Name(name);

        if (phone == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT,
                    Phone.class.getSimpleName()));
        }
        if (!Phone.isValidPhone(phone)) {
            throw new IllegalValueException(Phone.MESSAGE_CONSTRAINTS);
        }
        Phone modelPhone = new Phone(phone);

        if (email == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT,
                    Email.class.getSimpleName()));
        }
        if (!Email.isValidEmail(email)) {
            throw new IllegalValueException(Email.MESSAGE_CONSTRAINTS);
        }
        Email modelEmail = new Email(email);

        return new Member(modelNusId, modelName, modelPhone, modelEmail);
    }
}
