package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.RemarkCommand;
import seedu.address.model.person.Remark;

public class RemarkCommandParserTest {

    private final RemarkCommandParser parser = new RemarkCommandParser();

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_remark_returnsCommand() {
        assertParseSuccess(parser, "1 r/Likes baseball and swimming!",
                new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Likes baseball and swimming!")));
    }

    @Test
    public void parse_surroundingWhitespace_trimsRemark() {
        assertParseSuccess(parser, "  1   r/  Likes baseball  ",
                new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Likes baseball")));
    }

    @Test
    public void parse_emptyRemark_clearsRemark() {
        RemarkCommand clear = new RemarkCommand(INDEX_FIRST_PERSON, new Remark(""));
        assertParseSuccess(parser, "1 r/", clear);
        assertParseSuccess(parser, "1 r/   ", clear);
        assertParseSuccess(parser, "1", clear);
    }

    @Test
    public void parse_repeatedPrefix_usesLastRemark() {
        assertParseSuccess(parser, "1 r/first r/last",
                new RemarkCommand(INDEX_FIRST_PERSON, new Remark("last")));
    }

    @Test
    public void parse_invalidIndex_throwsParseException() {
        String message = String.format(MESSAGE_INVALID_COMMAND_FORMAT, RemarkCommand.MESSAGE_USAGE);
        for (String input : new String[] {"", "r/note", "0 r/note", "-1 r/note", "abc r/note",
            "1.5 r/note", "2147483648 r/note", "1 2 r/note", "1 note"}) {
            assertParseFailure(parser, input, message);
        }
    }
}
