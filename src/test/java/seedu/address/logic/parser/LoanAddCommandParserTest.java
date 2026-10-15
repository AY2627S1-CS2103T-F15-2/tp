package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.LoanAddCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.member.NusId;

public class LoanAddCommandParserTest {
    private static final UUID UUID_VALUE = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private static final NusId NUS_ID = new NusId("A0123456X");
    private static final LocalDate ASSIGNED = LocalDate.of(2026, 10, 15);
    private static final LocalDate EXPECTED = LocalDate.of(2026, 10, 20);
    private static final List<String> FIELDS = List.of("uuid/" + UUID_VALUE, "nus/a0123456x",
            "assigned/2026-10-15", "expected/2026-10-20");
    private final LoanAddCommandParser parser = new LoanAddCommandParser();

    @Test
    public void parse_allTwentyFourPermutationsSpacesAndTabs_normalizesIdentities() throws Exception {
        LoanAddCommand expected = new LoanAddCommand(UUID_VALUE, NUS_ID, ASSIGNED, EXPECTED);
        for (List<String> permutation : permutations(FIELDS)) {
            for (String separator : List.of(" ", "\t")) {
                assertEquals(expected, parser.parse(" \t" + String.join(separator, permutation) + "\t "));
            }
        }
        assertEquals(expected, parser.parse(String.join(" ", FIELDS)
                .replace(UUID_VALUE.toString(), UUID_VALUE.toString().toUpperCase(Locale.ROOT))));
    }

    @Test
    public void parse_eachMissingBlankOrRepeatedRequiredField_rejectsWithUsage() {
        for (int index = 0; index < FIELDS.size(); index++) {
            List<String> fields = new ArrayList<>(FIELDS);
            String field = fields.remove(index);
            assertInvalid(String.join(" ", fields));
            fields.add(field.substring(0, field.indexOf('/') + 1) + " \t");
            assertInvalid(String.join(" ", fields));
            assertInvalid(String.join(" ", FIELDS) + " " + field);
        }
    }

    @Test
    public void parse_unknownMarkersPreambleAndEmptyInput_rejectsWithUsage() {
        String valid = String.join(" ", FIELDS);
        for (String invalid : List.of("", " \t", "preamble " + valid, valid + " unknown/value",
                valid + " notes/text", valid + " returned/2026-10-16", valid + " UUID/" + UUID_VALUE,
                valid + " name/Camera", valid + " reserved_marker/value")) {
            assertInvalid(invalid);
        }
    }

    @Test
    public void parse_malformedIdentityOrCalendarDate_rejectsWithFieldExplanationAndUsage() {
        String valid = String.join(" ", FIELDS);
        ParseException uuid = assertInvalid(valid.replace(UUID_VALUE.toString(), "invalid-uuid"));
        assertTrue(uuid.getMessage().contains("UUID"));
        ParseException nus = assertInvalid(valid.replace("a0123456x", "1234567"));
        assertTrue(nus.getMessage().contains("NUS"));
        for (String date : List.of("2026-02-29", "2026-13-01", "2026-10-32", "2026-1-01", "2026/10/15", "today")) {
            for (String original : List.of(ASSIGNED.toString(), EXPECTED.toString())) {
                ParseException error = assertInvalid(valid.replace(original, date));
                assertTrue(error.getMessage().contains("Date"));
            }
        }
    }

    @Test
    public void parse_validDatesDefersOrderingAndTodayRestrictionsUntilExecution() throws Exception {
        assertEquals(new LoanAddCommand(UUID_VALUE, NUS_ID, EXPECTED, ASSIGNED), parser.parse(
                "uuid/" + UUID_VALUE + " nus/" + NUS_ID + " assigned/" + EXPECTED + " expected/" + ASSIGNED));
        LocalDate future = LocalDate.of(2999, 12, 31);
        assertEquals(new LoanAddCommand(UUID_VALUE, NUS_ID, future, future), parser.parse(
                "uuid/" + UUID_VALUE + " nus/" + NUS_ID + " assigned/" + future + " expected/" + future));
    }

    @Test
    public void parse_nullInput_rejectsProgrammerMisuse() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    private ParseException assertInvalid(String input) {
        ParseException error = assertThrows(ParseException.class, () -> parser.parse(input));
        assertTrue(error.getMessage().contains(LoanAddCommand.MESSAGE_USAGE));
        return error;
    }

    private static List<List<String>> permutations(List<String> fields) {
        if (fields.isEmpty()) {
            return List.of(List.of());
        }
        List<List<String>> result = new ArrayList<>();
        for (String first : fields) {
            List<String> remaining = new ArrayList<>(fields);
            remaining.remove(first);
            for (List<String> tail : permutations(remaining)) {
                List<String> order = new ArrayList<>();
                order.add(first);
                order.addAll(tail);
                result.add(order);
            }
        }
        return result;
    }
}
