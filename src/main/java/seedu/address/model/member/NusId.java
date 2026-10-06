package seedu.address.model.member;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;

/**
 * Represents a member's NUS ID.
 * Guarantees: immutable; stored in uppercase; valid as declared in
 * {@link #isValidNusId(String)}.
 */
public class NusId {

    public static final String MESSAGE_CONSTRAINTS =
            "NUS IDs should contain one letter, seven digits, and an optional final letter";
    public static final String VALIDATION_REGEX = "[A-Za-z]\\d{7}[A-Za-z]?";

    public final String value;

    /**
     * Constructs a {@code NusId}.
     *
     * @param nusId A valid NUS ID.
     */
    public NusId(String nusId) {
        requireNonNull(nusId);
        checkArgument(isValidNusId(nusId), MESSAGE_CONSTRAINTS);
        value = nusId.toUpperCase(Locale.ROOT);
    }

    /**
     * Returns true if a given string is a valid NUS ID.
     */
    public static boolean isValidNusId(String test) {
        return test.matches(VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof NusId otherNusId)) {
            return false;
        }
        return value.equals(otherNusId.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
