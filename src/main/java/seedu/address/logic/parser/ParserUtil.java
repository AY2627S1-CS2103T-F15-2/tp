package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.member.NusId;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.Tag;

/**
 * Contains utility methods used for parsing strings in the various *Parser classes.
 */
public class ParserUtil {

    public static final String MESSAGE_INVALID_INDEX = "Index must be a positive integer.";

    /**
     * Parses {@code oneBasedIndex} into an {@code Index} and returns it. Leading and trailing whitespaces will be
     * trimmed.
     * @throws ParseException if the specified index is invalid (not a non-zero unsigned integer).
     */
    public static Index parseIndex(String oneBasedIndex) throws ParseException {
        String trimmedIndex = oneBasedIndex.trim();
        if (!StringUtil.isNonZeroUnsignedInteger(trimmedIndex)) {
            throw new ParseException(MESSAGE_INVALID_INDEX);
        }
        return Index.fromOneBased(Integer.parseInt(trimmedIndex));
    }

    /**
     * Parses a {@code String name} into a {@code Name}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code name} is invalid.
     */
    public static Name parseName(String name) throws ParseException {
        requireNonNull(name);
        String trimmedName = name.trim();
        if (!Name.isValidName(trimmedName)) {
            throw new ParseException(Name.MESSAGE_CONSTRAINTS);
        }
        return new Name(trimmedName);
    }

    /**
     * Parses a {@code String phone} into a {@code Phone}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code phone} is invalid.
     */
    public static Phone parsePhone(String phone) throws ParseException {
        requireNonNull(phone);
        String trimmedPhone = phone.trim();
        if (!Phone.isValidPhone(trimmedPhone)) {
            throw new ParseException(Phone.MESSAGE_CONSTRAINTS);
        }
        return new Phone(trimmedPhone);
    }

    /**
     * Parses a {@code String address} into an {@code Address}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code address} is invalid.
     */
    public static Address parseAddress(String address) throws ParseException {
        requireNonNull(address);
        String trimmedAddress = address.trim();
        if (!Address.isValidAddress(trimmedAddress)) {
            throw new ParseException(Address.MESSAGE_CONSTRAINTS);
        }
        return new Address(trimmedAddress);
    }

    /**
     * Parses a {@code String email} into an {@code Email}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code email} is invalid.
     */
    public static Email parseEmail(String email) throws ParseException {
        requireNonNull(email);
        String trimmedEmail = email.trim();
        if (!Email.isValidEmail(trimmedEmail)) {
            throw new ParseException(Email.MESSAGE_CONSTRAINTS);
        }
        return new Email(trimmedEmail);
    }

    /**
     * Parses a {@code String tag} into a {@code Tag}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code tag} is invalid.
     */
    public static Tag parseTag(String tag) throws ParseException {
        requireNonNull(tag);
        String trimmedTag = tag.trim();
        if (!Tag.isValidTagName(trimmedTag)) {
            throw new ParseException(Tag.MESSAGE_CONSTRAINTS);
        }
        return new Tag(trimmedTag);
    }

    /**
     * Parses {@code Collection<String> tags} into a {@code Set<Tag>}.
     */
    public static Set<Tag> parseTags(Collection<String> tags) throws ParseException {
        requireNonNull(tags);
        final Set<Tag> tagSet = new HashSet<>();
        for (String tagName : tags) {
            tagSet.add(parseTag(tagName));
        }
        return tagSet;
    }

    /**
     * Parses a NUS ID, trimming surrounding whitespace and normalizing case in {@code NusId}.
     *
     * @throws NullPointerException if {@code value} is null, following the AB3 value-parser contract.
     * @throws ParseException if {@code value} is not a valid NUS ID.
     */
    public static NusId parseNusId(String value) throws ParseException {
        requireNonNull(value);
        String trimmedValue = value.strip();
        if (!NusId.isValidNusId(trimmedValue)) {
            throw new ParseException(NusId.MESSAGE_CONSTRAINTS);
        }
        return new NusId(trimmedValue);
    }

    /** Parses new member input using MVP rules while leaving legacy parseName unchanged. */
    public static Name parseMemberName(String value) throws ParseException {
        try {
            return Name.forMember(value);
        } catch (IllegalArgumentException e) {
            throw new ParseException(Name.MEMBER_MESSAGE_CONSTRAINTS, e);
        }
    }

    /** Normalizes international phones using the member-specific value factory. */
    public static Phone parseMemberPhone(String value) throws ParseException {
        try {
            return Phone.forMember(value);
        } catch (IllegalArgumentException e) {
            throw new ParseException(Phone.MEMBER_MESSAGE_CONSTRAINTS, e);
        }
    }

    /** Normalizes and validates ASCII email input under the MVP member rules. */
    public static Email parseMemberEmail(String value) throws ParseException {
        try {
            return Email.forMember(value);
        } catch (IllegalArgumentException e) {
            throw new ParseException(Email.MEMBER_MESSAGE_CONSTRAINTS, e);
        }
    }

    /** Parses printable equipment text using the shared MVP domain normalization. */
    public static String parseEquipmentName(String value) throws ParseException {
        try {
            return Equipment.normalizeName(value);
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage(), e);
        }
    }

    /** Parses a category using the shared MVP domain normalization. */
    public static String parseCategory(String value) throws ParseException {
        try {
            return Equipment.normalizeCategory(value);
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage(), e);
        }
    }

    /** Parses optional notes, accepting an explicitly empty value. */
    public static String parseNotes(String value) throws ParseException {
        try {
            return Equipment.normalizeNotes(value);
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage(), e);
        }
    }

    /**
     * Parses a canonical 8-4-4-4-12 UUID, ignoring hexadecimal case and surrounding whitespace.
     *
     * @throws NullPointerException if {@code value} is null.
     * @throws ParseException if {@code value} is not a canonical UUID.
     */
    public static UUID parseUuid(String value) throws ParseException {
        requireNonNull(value);
        if (!value.strip().matches("(?i)[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")) {
            throw new ParseException("Invalid equipment UUID. Use the UUID shown by Laplace.");
        }
        return UUID.fromString(value.strip());
    }

    /**
     * Parses a calendar date in {@code yyyy-MM-dd} format after trimming surrounding whitespace.
     * Date ordering and restrictions relative to today belong to the domain and business commands.
     *
     * @throws NullPointerException if {@code value} is null.
     * @throws ParseException if {@code value} is not a valid calendar date in the required format.
     */
    public static LocalDate parseDate(String value) throws ParseException {
        requireNonNull(value);
        String trimmedValue = value.strip();
        if (!trimmedValue.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new ParseException("Date must be a valid calendar date in yyyy-MM-dd format.");
        }
        try {
            LocalDate date = LocalDate.parse(trimmedValue);
            if (date.getYear() == 0) {
                throw new ParseException("Date must use a Gregorian year from 0001 through 9999.");
            }
            return date;
        } catch (DateTimeParseException e) {
            throw new ParseException("Date must be a valid calendar date in yyyy-MM-dd format.", e);
        }
    }

    /**
     * Parses a case-insensitive condition after trimming whitespace; only the four declared spellings are accepted.
     *
     * @throws NullPointerException if {@code value} is null.
     * @throws ParseException if {@code value} does not identify a declared condition.
     */
    public static Condition parseCondition(String value) throws ParseException {
        requireNonNull(value);
        try {
            return Condition.fromText(value);
        } catch (IllegalArgumentException e) {
            throw new ParseException("Condition must be GOOD, FAIR, DAMAGED or UNDER_REPAIR.", e);
        }
    }
}
