package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.util.LaplaceText;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Strict argument parsing for Laplace commands. Legacy AB3 tokenization is unchanged.
 * Start-of-input or whitespace-delimited parameter markers are reserved, including in notes.
 * There is no quoting or escaping. URLs and slashes without a preceding marker boundary remain text.
 */
public final class LaplaceArgumentParser {

    private static final Pattern MARKER = Pattern.compile("(?U)(?<!\\S)[A-Za-z][A-Za-z0-9_-]*/");

    private LaplaceArgumentParser() {
    }

    /**
     * Parses trimmed values into an existing {@code ArgumentMultimap}. Optional values may be blank.
     * Diagnostics follow supplied fields in input order, then absent required fields
     * in lexicographic prefix order. Empty prefix sets accept only whitespace arguments.
     *
     * @throws NullPointerException for null arguments, sets, prefixes or prefix tokens.
     * @throws IllegalArgumentException for overlapping sets or tokens outside the marker grammar.
     * @throws ParseException for preamble, unknown/duplicate markers or missing/blank required values.
     */
    public static ArgumentMultimap parse(String args, Set<Prefix> required,
            Set<Prefix> optional) throws ParseException {
        return parse(args, required, optional, Map.of());
    }

    /**
     * Scans supplied fields left to right, validating each value before the following marker.
     * Blank required values occupy their field's position. Missing fields have no input position and
     * are reported only after supplied fields, in lexicographic prefix order. Legacy AB3 is unaffected.
     */
    public static ArgumentMultimap parse(String args, Set<Prefix> required, Set<Prefix> optional,
            Map<Prefix, ValueValidator> validators) throws ParseException {
        requireNonNull(args);
        requireNonNull(required);
        requireNonNull(optional);
        requireNonNull(validators);
        Set<Prefix> allowed = new HashSet<>();
        Set<String> requiredTokens = new TreeSet<>();
        for (Prefix prefix : required) {
            validatePrefix(prefix);
            allowed.add(prefix);
            requiredTokens.add(prefix.getPrefix());
        }
        for (Prefix prefix : optional) {
            validatePrefix(prefix);
            if (!allowed.add(prefix)) {
                throw new IllegalArgumentException("Required and optional prefixes must not overlap.");
            }
        }

        ArgumentMultimap result = new ArgumentMultimap();
        Set<Prefix> seen = new HashSet<>();
        Matcher matcher = MARKER.matcher(args);
        Prefix current = null;
        int valueStart = 0;
        while (matcher.find()) {
            if (current == null) {
                rejectPreamble(args.substring(0, matcher.start()));
            } else {
                addValue(result, current, LaplaceText.stripWhitespace(args.substring(valueStart, matcher.start())),
                        required, validators);
            }
            current = new Prefix(matcher.group());
            if (!allowed.contains(current)) {
                throw new ParseException("Unknown parameter: " + current);
            }
            if (!seen.add(current)) {
                throw new ParseException("Repeated parameter: " + current);
            }
            valueStart = matcher.end();
        }
        if (current == null) {
            rejectPreamble(args);
        } else {
            addValue(result, current, LaplaceText.stripWhitespace(args.substring(valueStart)), required, validators);
        }
        for (String token : requiredTokens) {
            Prefix prefix = new Prefix(token);
            if (result.getValue(prefix).isEmpty()) {
                throw new ParseException("Missing required parameter: " + token);
            }
            if (result.getValue(prefix).orElseThrow().isBlank()) {
                throw new ParseException("Required parameter must not be blank: " + token);
            }
        }
        return result;
    }

    /** A field's pure validation step; method references may discard their parsed return value. */
    @FunctionalInterface
    public interface ValueValidator {
        void validate(String value) throws ParseException;
    }

    private static void addValue(ArgumentMultimap result, Prefix prefix, String value, Set<Prefix> required,
            Map<Prefix, ValueValidator> validators) throws ParseException {
        if (required.contains(prefix) && value.isBlank()) {
            throw new ParseException("Required parameter must not be blank: " + prefix);
        }
        ValueValidator validator = validators.get(prefix);
        if (validator != null) {
            validator.validate(value);
        }
        result.put(prefix, value);
    }

    private static void validatePrefix(Prefix prefix) {
        requireNonNull(prefix);
        String token = requireNonNull(prefix.getPrefix());
        if (!MARKER.matcher(token).matches()) {
            throw new IllegalArgumentException("Invalid Laplace prefix token: " + token);
        }
    }

    private static void rejectPreamble(String preamble) throws ParseException {
        if (!LaplaceText.stripWhitespace(preamble).isEmpty()) {
            throw new ParseException("Unexpected text before parameters.");
        }
    }
}
