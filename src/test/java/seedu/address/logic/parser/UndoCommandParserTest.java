package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.parser.exceptions.ParseException;

class UndoCommandParserTest {
    @Test
    void parse_whitespaceOnly_acceptsBareCommandThroughRootParser() throws Exception {
        UndoCommandParser parser = new UndoCommandParser();
        for (String whitespace : List.of("", " ", "\t\n  ")) {
            assertInstanceOf(UndoCommand.class, parser.parse(whitespace));
            assertInstanceOf(UndoCommand.class, new AddressBookParser().parseCommand(" undo" + whitespace));
        }
    }

    @Test
    void parse_extraArguments_rejectsWithoutExecuting() throws Exception {
        for (String argument : List.of("1", "undo", "uuid/value", "unknown/value", "count/2 count/3")) {
            ParseException error = assertThrows(ParseException.class, () ->
                    new UndoCommandParser().parse(argument));
            assertTrue(error.getMessage().contains(UndoCommand.MESSAGE_USAGE));
            assertThrows(ParseException.class, () -> new AddressBookParser().parseCommand("undo " + argument));
        }
        assertInstanceOf(UndoCommand.class, new AddressBookParser().parseCommand("UNDO"));
        assertThrows(NullPointerException.class, () -> new UndoCommandParser().parse(null));
    }
}
