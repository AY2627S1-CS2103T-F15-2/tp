package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.MemberViewCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.member.NusId;
public class MemberViewCommandParserTest {
    private final MemberViewCommandParser parser = new MemberViewCommandParser();

    @Test
    public void parse_validIdentityAndWhitespace_returnsExactCommand() throws Exception {
        for (String prefix : List.of("", " ", "\t", " \t ")) {
            for (String value : List.of("A0123456X", "a0123456x")) {
                assertEquals(new MemberViewCommand(new NusId("A0123456X")),
                        parser.parse(prefix + "nus/ " + value + " \t"));
            }
        }
    }

    @Test
    public void parse_invalidArguments_reportsUsageAndCause() {
        for (String input : List.of("",
                " ",
                "\t",
                "nus/",
                "nus/  ",
                "nus/invalid",
                "NUS/A0123456X",
                "A0123456X",
                "junk nus/A0123456X",
                "nus/A0123456X nus/A0123456X",
                "nus/A0123456X\tnus/",
                "nus/A0123456X notes/test",
                "nus/A0123456X extra-x/test",
                "nus/A0123456X a1/test",
                "nus/A0123456X tail",
                "nus/A0123456X name/Camera",
                "nus/1234567",
                "nus/A123",
                "nus/A0123456XX")) {
            ParseException error = assertThrows(ParseException.class, () -> parser.parse(input), input);
            assertTrue(error.getMessage().contains(MemberViewCommand.MESSAGE_USAGE), input);
            assertNotNull(error.getCause());
        }
    }

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
