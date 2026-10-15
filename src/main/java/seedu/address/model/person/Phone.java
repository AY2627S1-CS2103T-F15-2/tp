package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.function.Predicate;

import seedu.address.commons.util.LaplaceText;

/**
 * Represents a Person's phone number in the address book.
 * Guarantees: immutable; legacy construction and member factories apply their respective validation rules.
 */
public class Phone {

    public static final String MESSAGE_CONSTRAINTS =
            "Phone numbers should only contain digits, and should be at least 3 digits long";
    public static final String VALIDATION_REGEX = "\\d{3,}";
    public static final String MEMBER_MESSAGE_CONSTRAINTS =
            "Invalid phone number. Enter 8-15 digits, optionally beginning with +.";

    public final String value;

    /**
     * Constructs a {@code Phone}.
     *
     * @param phone A valid phone number.
     */
    public Phone(String phone) {
        this(phone, Phone::isValidPhone, MESSAGE_CONSTRAINTS);
    }

    private Phone(String value, Predicate<String> validator, String message) {
        requireNonNull(value);
        checkArgument(validator.test(value), message);
        this.value = value;
    }

    /** Validates the stored MVP phone form after spaces and hyphens have been removed. */
    public static boolean isValidMemberPhone(String value) {
        return value.matches("\\+?[0-9]{8,15}");
    }

    /** Normalizes and validates new Laplace member input without changing legacy AB3 parsing. */
    public static Phone forMember(String value) {
        String normalized = LaplaceText.normalize(value).replace(" ", "").replace("-", "");
        return new Phone(normalized, Phone::isValidMemberPhone, MEMBER_MESSAGE_CONSTRAINTS);
    }

    /** Accepts either the former stored form or the MVP form, preserving the exact saved value. */
    public static Phone fromStoredMember(String value) {
        return new Phone(value, candidate -> isValidPhone(candidate) || isValidMemberPhone(candidate),
                MESSAGE_CONSTRAINTS);
    }

    /**
     * Returns true if a given string is a valid phone number.
     */
    public static boolean isValidPhone(String test) {
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

        // instanceof handles nulls
        if (!(other instanceof Phone otherPhone)) {
            return false;
        }

        return value.equals(otherPhone.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
