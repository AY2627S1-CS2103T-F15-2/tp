package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;
import java.util.function.Predicate;

import seedu.address.commons.util.LaplaceText;

/**
 * Represents a Person's email in the address book.
 * Guarantees: immutable; legacy construction and member factories apply their respective validation rules.
 */
public class Email {

    public static final String MEMBER_MESSAGE_CONSTRAINTS =
            "Invalid email address. Enter an address such as name@example.com.";

    private static final String SPECIAL_CHARACTERS = "+_.-";
    public static final String MESSAGE_CONSTRAINTS = "Emails should be of the format local-part@domain "
            + "and adhere to the following constraints:\n"
            + "1. The local-part should only contain alphanumeric characters and these special characters, excluding "
            + "the parentheses, (" + SPECIAL_CHARACTERS + "). The local-part may not start or end with any special "
            + "characters.\n"
            + "2. The local-part is followed by an '@' and then a domain name. The domain name is made up of domain "
            + "labels separated by periods.\n"
            + "The domain name must:\n"
            + "    - end with a domain label at least 2 characters long\n"
            + "    - have each domain label start and end with alphanumeric characters\n"
            + "    - have each domain label consist of alphanumeric characters, separated only by hyphens, if any.";
    // alphanumeric and special characters
    private static final String ALPHANUMERIC_NO_UNDERSCORE = "[^\\W_]+"; // alphanumeric characters except underscore
    private static final String LOCAL_PART_REGEX = "^" + ALPHANUMERIC_NO_UNDERSCORE + "([" + SPECIAL_CHARACTERS + "]"
            + ALPHANUMERIC_NO_UNDERSCORE + ")*";
    private static final String DOMAIN_PART_REGEX = ALPHANUMERIC_NO_UNDERSCORE
            + "(-" + ALPHANUMERIC_NO_UNDERSCORE + ")*";
    private static final String DOMAIN_LAST_PART_REGEX = "(" + DOMAIN_PART_REGEX + "){2,}$"; // At least two chars
    private static final String DOMAIN_REGEX = "(" + DOMAIN_PART_REGEX + "\\.)*" + DOMAIN_LAST_PART_REGEX;
    public static final String VALIDATION_REGEX = LOCAL_PART_REGEX + "@" + DOMAIN_REGEX;

    private static final String MEMBER_DOMAIN_LABEL = "[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?";

    public final String value;

    /**
     * Constructs an {@code Email}.
     *
     * @param email A valid email address.
     */
    public Email(String email) {
        this(email, Email::isValidEmail, MESSAGE_CONSTRAINTS);
    }

    private Email(String value, Predicate<String> validator, String message) {
        requireNonNull(value);
        checkArgument(validator.test(value), message);
        this.value = value;
    }

    /** Validates the MVP ASCII syntax, including a domain dot and the complete length limit. */
    public static boolean isValidMemberEmail(String value) {
        return value.length() >= 3 && value.length() <= 254
                && value.matches("[A-Za-z0-9._%+\\-]+@" + MEMBER_DOMAIN_LABEL
                        + "(?:\\." + MEMBER_DOMAIN_LABEL + ")+");
    }

    /**
     * Normalizes and validates new Laplace member input without changing legacy AB3 parsing.
     */
    public static Email forMember(String value) {
        String normalized = LaplaceText.normalize(value);
        checkArgument(isValidMemberEmail(normalized), MEMBER_MESSAGE_CONSTRAINTS);
        return new Email(normalized.toLowerCase(Locale.ROOT), Email::isValidMemberEmail, MEMBER_MESSAGE_CONSTRAINTS);
    }

    /** Accepts either the former stored form or the MVP form, preserving the exact saved value. */
    public static Email fromStoredMember(String value) {
        return new Email(value, candidate -> isValidEmail(candidate) || isValidMemberEmail(candidate),
                MESSAGE_CONSTRAINTS);
    }

    /**
     * Returns true if a given string is a valid email.
     */
    public static boolean isValidEmail(String test) {
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
        if (!(other instanceof Email otherEmail)) {
            return false;
        }

        return value.equals(otherEmail.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
