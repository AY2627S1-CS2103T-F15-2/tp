package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.text.Normalizer;
import java.util.function.Predicate;

import seedu.address.commons.util.LaplaceText;

/**
 * Represents a Person's name in the address book.
 * Guarantees: immutable; legacy construction and member factories apply their respective validation rules.
 */
public class Name {

    public static final String MESSAGE_CONSTRAINTS =
            "Names should only contain alphanumeric characters and spaces, and should not be blank";

    /*
     * The first character of the name must not be a whitespace,
     * otherwise " " (a blank string) becomes a valid input.
     */
    public static final String VALIDATION_REGEX = "[\\p{Alnum}][\\p{Alnum} ]*";

    public static final String MEMBER_MESSAGE_CONSTRAINTS =
            "Invalid member name. Use 1-100 characters: letters with optional combining marks, spaces, "
                    + "apostrophes, hyphens, or periods. Include at least one letter.";

    public final String fullName;

    /**
     * Constructs a {@code Name}.
     *
     * @param name A valid name.
     */
    public Name(String name) {
        this(name, Name::isValidName, MESSAGE_CONSTRAINTS);
    }

    private Name(String value, Predicate<String> validator, String message) {
        requireNonNull(value);
        checkArgument(validator.test(value), message);
        fullName = value;
    }

    /**
     * Validates a member name, accepting combining marks only after a letter or its preceding marks.
     * Legacy person names retain their original rules.
     */
    public static boolean isValidMemberName(String value) {
        return LaplaceText.isPrintable(value, 1, 100) && value.matches("(?:\\p{L}\\p{M}*|[ .'\u2019-])+")
                && value.codePoints().anyMatch(Character::isLetter);
    }

    /**
     * Normalizes whitespace and canonical Unicode spelling for new member input before validation.
     * Existing stored member spelling and legacy AB3 parsing are unchanged.
     */
    public static Name forMember(String value) {
        String normalized = Normalizer.normalize(LaplaceText.normalize(value), Normalizer.Form.NFC);
        return new Name(normalized, Name::isValidMemberName, MEMBER_MESSAGE_CONSTRAINTS);
    }

    /** Accepts either the former stored form or the MVP form, preserving the exact saved value. */
    public static Name fromStoredMember(String value) {
        return new Name(value, candidate -> isValidName(candidate) || isValidMemberName(candidate),
                MESSAGE_CONSTRAINTS);
    }

    /**
     * Returns true if a given string is a valid name.
     */
    public static boolean isValidName(String test) {
        return test.matches(VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return fullName;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Name otherName)) {
            return false;
        }

        return fullName.equals(otherName.fullName);
    }

    @Override
    public int hashCode() {
        return fullName.hashCode();
    }

}
