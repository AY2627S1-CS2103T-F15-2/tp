package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.RedoCommand;
import seedu.address.logic.parser.exceptions.ParseException;

class RedoCommandParserTest {
    @Test
    void parse_whitespaceOnly_acceptsBareCommandThroughRootParser() throws Exception {
        RedoCommandParser parser = new RedoCommandParser();
        for (String whitespace : List.of("", " ", "\t\n  ")) {
            assertInstanceOf(RedoCommand.class, parser.parse(whitespace));
            assertInstanceOf(RedoCommand.class, new AddressBookParser().parseCommand(" redo" + whitespace));
        }
    }

    @Test
    void parse_extraArgumentsAndWrongCase_rejectsWithoutExecuting() {
        for (String argument : List.of("1", "redo", "uuid/value", "unknown/value", "count/2 count/3")) {
            ParseException error = assertThrows(ParseException.class, () ->
                    new RedoCommandParser().parse(argument));
            assertTrue(error.getMessage().contains(RedoCommand.MESSAGE_USAGE));
            assertThrows(ParseException.class, () -> new AddressBookParser().parseCommand("redo " + argument));
        }
        assertThrows(ParseException.class, () -> new AddressBookParser().parseCommand("REDO"));
        assertThrows(NullPointerException.class, () -> new RedoCommandParser().parse(null));
    }
}
