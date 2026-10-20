package seedu.address.model.member;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;

/**
 * Represents a CCA member in Laplace.
 * Guarantees: immutable; fields are present, non-null, and individually valid.
 */
public class Member {

    private final NusId nusId;
    private final Name name;
    private final Phone phone;
    private final Email email;
    private final boolean deleted;

    /**
     * Constructs a {@code Member} with the given details.
     */
    public Member(NusId nusId, Name name, Phone phone, Email email) {
        this(nusId, name, phone, email, false);
    }

    /**
     * Constructs a retained member, including their persisted deletion state.
     */
    public Member(NusId nusId, Name name, Phone phone, Email email, boolean isDeleted) {
        requireAllNonNull(nusId, name, phone, email);
        this.nusId = nusId;
        this.name = name;
        this.phone = phone;
        this.email = email;
        deleted = isDeleted;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public NusId getNusId() {
        return nusId;
    }

    public Name getName() {
        return name;
    }

    public Phone getPhone() {
        return phone;
    }

    public Email getEmail() {
        return email;
    }

    /**
     * Returns true if both members have the same NUS ID.
     */
    public boolean isSameMember(Member otherMember) {
        if (otherMember == this) {
            return true;
        }
        return otherMember != null && otherMember.getNusId().equals(getNusId());
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Member otherMember)) {
            return false;
        }
        return nusId.equals(otherMember.nusId)
                && name.equals(otherMember.name)
                && phone.equals(otherMember.phone)
                && email.equals(otherMember.email)
                && deleted == otherMember.deleted;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nusId, name, phone, email, deleted);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("nusId", nusId)
                .add("name", name)
                .add("phone", phone)
                .add("email", email)
                .add("deleted", deleted)
                .toString();
    }
}
