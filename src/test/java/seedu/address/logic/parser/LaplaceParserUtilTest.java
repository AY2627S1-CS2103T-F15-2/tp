package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.equipment.Condition;
import seedu.address.model.member.NusId;

public class LaplaceParserUtilTest {

    @Test
    public void parseNusId_validValues_normalizesAndTrims() throws Exception {
        assertEquals(new NusId("A0123456X"), ParserUtil.parseNusId(" \t a0123456x \n"));
        assertEquals(new NusId("A0123456"), ParserUtil.parseNusId("A0123456"));
    }

    @Test
    public void parseNusId_invalidValues_throwsParseException() {
        for (String value : new String[] {"", " \t", "0123456", "A123456", "A12345678", "A0123456XX",
            "A012 3456X", "A0123456!"}) {
            assertThrows(ParseException.class, NusId.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseNusId(value));
        }
    }

    @Test
    public void parseUuid_validValues_usesJavaSyntaxAndTrims() throws Exception {
        UUID expected = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        assertEquals(expected, ParserUtil.parseUuid(" \t550E8400-E29B-41D4-A716-446655440000\n"));
        assertEquals(UUID.fromString("1-1-1-1-1"), ParserUtil.parseUuid("1-1-1-1-1"));
    }

    @Test
    public void parseUuid_invalidValues_throwsFieldSpecificParseException() {
        for (String value : new String[] {"", " ", "not-a-uuid", "550e8400-e29b-41d4-a716-44665544000z",
            "550e8400e29b41d4a716446655440000", "550e8400-e29b-41d4-a716-446655440000-extra"}) {
            assertThrows(ParseException.class, "Equipment UUID must use Java UUID syntax.", () ->
                    ParserUtil.parseUuid(value));
        }
    }

    @Test
    public void parseDate_validValues_acceptsCalendarDatesAndTrims() throws Exception {
        assertEquals(LocalDate.of(2024, 2, 29), ParserUtil.parseDate(" \t2024-02-29\n"));
        assertEquals(LocalDate.of(2026, 10, 6), ParserUtil.parseDate("2026-10-06"));
        assertEquals(LocalDate.of(2099, 12, 31), ParserUtil.parseDate("2099-12-31"));
    }

    @Test
    public void parseDate_invalidValues_throwsFieldSpecificParseException() {
        for (String value : new String[] {"", " ", "2025-02-29", "2026-04-31", "2026-13-01",
            "2026-10-00", "2026-1-06", "06-10-2026", "2026/10/06", "2026-10-06T00:00:00", "today"}) {
            assertThrows(ParseException.class, "Date must be a valid calendar date in uuuu-MM-dd format.", () ->
                    ParserUtil.parseDate(value));
        }
    }

    @Test
    public void parseCondition_validValues_normalizesAndTrims() throws Exception {
        for (Condition condition : Condition.values()) {
            assertEquals(condition,
                    ParserUtil.parseCondition(" \t" + condition.name().toLowerCase(Locale.ROOT) + "\n"));
        }
        assertEquals(Condition.UNDER_REPAIR, ParserUtil.parseCondition("under repair"));
        assertEquals(Condition.UNDER_REPAIR, ParserUtil.parseCondition("under-repair"));
    }

    @Test
    public void parseCondition_turkishLocale_usesLocaleIndependentCase() throws Exception {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertEquals(Condition.FAIR, ParserUtil.parseCondition("fair"));
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test
    public void parseCondition_invalidValues_throwsFieldSpecificParseException() {
        for (String value : new String[] {"", " ", "broken", "GOOD FAIR", "under__repair"}) {
            assertThrows(ParseException.class, "Condition must be GOOD, FAIR, DAMAGED or UNDER_REPAIR.", () ->
                    ParserUtil.parseCondition(value));
        }
    }

    @Test
    public void parseValues_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseNusId(null));
        assertThrows(NullPointerException.class, () -> ParserUtil.parseUuid(null));
        assertThrows(NullPointerException.class, () -> ParserUtil.parseDate(null));
        assertThrows(NullPointerException.class, () -> ParserUtil.parseCondition(null));
    }

    @Test
    public void parseValues_conversionFailures_preserveCause() {
        ParseException exception = org.junit.jupiter.api.Assertions.assertThrows(ParseException.class, () ->
                ParserUtil.parseDate("2026-02-30"));
        assertTrue(exception.getCause() instanceof java.time.format.DateTimeParseException);
    }
}
