package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Strict argument parsing for Laplace commands. Legacy AB3 tokenization is unchanged.
 * Start-of-input or whitespace-delimited parameter markers are reserved, including in notes.
 * There is no quoting or escaping. URLs and slashes without a preceding marker boundary remain text.
 */
public final class LaplaceArgumentParser {

    private static final Pattern MARKER = Pattern.compile("(?<!\\S)[A-Za-z][A-Za-z0-9_-]*/");

    private LaplaceArgumentParser() {
    }

    /**
     * Parses trimmed values into an existing {@code ArgumentMultimap}. Optional values may be blank.
     * Diagnostics report preamble first, then marker errors in input order, then required-field
     * errors in lexicographic prefix order. Empty prefix sets accept only whitespace arguments.
     *
     * @throws NullPointerException for null arguments, sets, prefixes or prefix tokens.
     * @throws IllegalArgumentException for overlapping sets or tokens outside the marker grammar.
     * @throws ParseException for preamble, unknown/duplicate markers or missing/blank required values.
     */
    public static ArgumentMultimap parse(String args, Set<Prefix> required,
            Set<Prefix> optional) throws ParseException {
        requireNonNull(args);
        requireNonNull(required);
        requireNonNull(optional);
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
                result.put(current, args.substring(valueStart, matcher.start()).trim());
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
            result.put(current, args.substring(valueStart).trim());
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

    private static void validatePrefix(Prefix prefix) {
        requireNonNull(prefix);
        String token = requireNonNull(prefix.getPrefix());
        if (!MARKER.matcher(token).matches()) {
            throw new IllegalArgumentException("Invalid Laplace prefix token: " + token);
        }
    }

    private static void rejectPreamble(String preamble) throws ParseException {
        if (!preamble.isBlank()) {
            throw new ParseException("Unexpected text before parameters.");
        }
    }
}
