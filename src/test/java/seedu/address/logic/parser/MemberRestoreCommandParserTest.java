package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.MemberRestoreCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.member.NusId;

/**
 * Checks exact identity parsing, normalization and strict restoration arguments.
 */
public class MemberRestoreCommandParserTest {
    private final MemberRestoreCommandParser parser = new MemberRestoreCommandParser();

    @Test
    public void parse_normalizedIdentityAndRoute_returnsRestoreCommand() throws Exception {
        MemberRestoreCommand expected = new MemberRestoreCommand(new NusId("A0123456X"));
        assertEquals(expected, parser.parse(" \tnus/ a0123456x "));
        assertEquals(expected, new AddressBookParser().parseCommand("member restore nus/a0123456x"));
    }

    @Test
    public void parse_invalidArguments_rejectsWithUsage() {
        for (String args : List.of("", "nus/", "nus/invalid", "extra nus/A0123456X",
                "nus/A0123456X nus/A0123456X", "nus/A0123456X name/Alex", "nus/A0123456X unknown/value")) {
            ParseException error = assertThrows(ParseException.class, () -> parser.parse(args), args);
            assertTrue(error.getMessage().contains(MemberRestoreCommand.MESSAGE_USAGE));
        }
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
