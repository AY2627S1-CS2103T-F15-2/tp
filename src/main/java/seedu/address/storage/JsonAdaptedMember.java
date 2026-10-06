package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.member.Member;

/** Jackson-facing contract for a {@link Member}. */
class JsonAdaptedMember {

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
        throw new UnsupportedOperationException("Member JSON conversion is not implemented");
    }
}
