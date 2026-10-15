package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.MemberDeleteCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.member.NusId;

/**
 * Verifies the single-identity member deletion grammar and value validation.
 */
public class MemberDeleteCommandParserTest {

    private final MemberDeleteCommandParser parser = new MemberDeleteCommandParser();

    @Test
    public void parse_lowercaseAndWhitespace_normalizesIdentity() throws Exception {
        MemberDeleteCommand expected = new MemberDeleteCommand(new NusId("A0123456X"));
        assertEquals(expected, parser.parse("nus/A0123456X"));
        assertEquals(expected, parser.parse(" \tnus/ a0123456x \t "));
        assertEquals(new MemberDeleteCommand(new NusId("A0123456")), parser.parse("nus/a0123456"));
    }

    @Test
    public void parse_missingBlankRepeatedUnknownAndPreamble_rejectsWithUsageAndCause() {
        for (String args : List.of("", " \t ", "nus/", "nus/ \t", "A0123456X", "1",
                "unexpected nus/A0123456X", "nus/A0123456X nus/A0234567R",
                "nus/A0123456X\tnus/A0123456X", "nus/A0123456X name/Alex",
                "nus/A0123456X unknown-marker/value", "NUS/A0123456X", "n/Alex")) {
            ParseException error = assertThrows(ParseException.class, () -> parser.parse(args), args);
            assertTrue(error.getMessage().contains(MemberDeleteCommand.MESSAGE_USAGE), args);
            assertInstanceOf(ParseException.class, error.getCause());
        }
    }

    @Test
    public void parse_malformedIdentity_providesFieldConstraints() {
        for (String value : List.of("123", "A0123", "A0123456XX", "A0123456X extra", "A0123456X/extra")) {
            ParseException error = assertThrows(ParseException.class, () -> parser.parse("nus/" + value));
            assertTrue(error.getMessage().startsWith(NusId.MESSAGE_CONSTRAINTS));
        }
    }

    @Test
    public void parse_nullArguments_rejectsMisuse() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
