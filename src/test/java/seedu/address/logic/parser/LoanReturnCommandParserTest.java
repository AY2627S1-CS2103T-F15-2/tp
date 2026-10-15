package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.LoanReturnCommand;
import seedu.address.logic.parser.exceptions.ParseException;

public class LoanReturnCommandParserTest {
    private static final UUID UUID_VALUE = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private static final LocalDate RETURNED = LocalDate.of(2026, 10, 15);
    private static final String UUID_FIELD = "uuid/" + UUID_VALUE;
    private static final String DATE_FIELD = "returned/" + RETURNED;
    private final LoanReturnCommandParser parser = new LoanReturnCommandParser();

    @Test
    public void parse_bothOrdersSpacesAndTabs_returnsConcreteCommand() throws Exception {
        for (List<String> fields : List.of(List.of(UUID_FIELD, DATE_FIELD), List.of(DATE_FIELD, UUID_FIELD))) {
            for (String separator : List.of(" ", "\t")) {
                assertEquals(new LoanReturnCommand(UUID_VALUE, RETURNED), parser.parse(
                        " \t" + String.join(separator, fields) + " \t"));
            }
        }
    }

    @Test
    public void parse_missingBlankRepeatedUnknownPreamble_rejectsWithUsage() {
        String valid = UUID_FIELD + " " + DATE_FIELD;
        for (String invalid : List.of("", " \t", UUID_FIELD, DATE_FIELD, "uuid/ \t" + DATE_FIELD,
                UUID_FIELD + " returned/ \t", valid + " " + UUID_FIELD, valid + " " + DATE_FIELD,
                "preamble " + valid, valid + " unknown/value", valid + " nus/A0123456X",
                valid + " assigned/2026-10-15", valid + " Returned/2026-10-15", valid + " notes/text")) {
            assertInvalid(invalid);
        }
    }

    @Test
    public void parse_malformedUuidAndCalendarDates_rejectsWithFieldExplanation() {
        assertTrue(assertInvalid("uuid/not-a-uuid " + DATE_FIELD).getMessage().contains("UUID"));
        for (String date : List.of("2026-02-29", "2026-13-01", "2026-10-32", "2026-1-01", "2026/10/15", "today")) {
            assertTrue(assertInvalid(UUID_FIELD + " returned/" + date).getMessage().contains("Date"));
        }
    }

    @Test
    public void parse_calendarDatesDefersTodayAndAssignmentChecksUntilExecution() throws Exception {
        LocalDate future = LocalDate.of(2999, 12, 31);
        assertEquals(new LoanReturnCommand(UUID_VALUE, future), parser.parse(UUID_FIELD + " returned/" + future));
        LocalDate leapDay = LocalDate.of(2024, 2, 29);
        assertEquals(new LoanReturnCommand(UUID_VALUE, leapDay), parser.parse(UUID_FIELD + " returned/" + leapDay));
    }

    @Test
    public void parse_nullInput_rejectsProgrammerMisuse() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    private ParseException assertInvalid(String input) {
        ParseException error = assertThrows(ParseException.class, () -> parser.parse(input));
        assertTrue(error.getMessage().contains(LoanReturnCommand.MESSAGE_USAGE));
        return error;
    }
}
