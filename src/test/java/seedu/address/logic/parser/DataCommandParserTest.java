package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.DataCommand;
import seedu.address.logic.commands.DataCommand.Action;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;

public class DataCommandParserTest {
    private final DataCommandParser parser = new DataCommandParser();

    @Test
    public void parse_allActions_acceptsWhitespaceAndPathsWithSpaces() throws Exception {
        for (String verb : List.of("import", "export", "restore")) {
            DataCommand command = parser.parse("\t" + verb + "\tfile/\"a folder/snapshot.json\"  ");
            assertEquals(Action.valueOf(verb.toUpperCase(java.util.Locale.ROOT)), command.getAction());
            assertEquals(Path.of("a folder/snapshot.json"), command.getPath());
            assertFalse(command.requiresPersistence());
            assertInstanceOf(DataCommand.class, new AddressBookParser().parseCommand("data " + verb + " file/a"));
        }
        for (String verb : List.of("backup", "confirm", "cancel")) {
            DataCommand command = parser.parse(verb + "\t");
            assertEquals(Action.valueOf(verb.toUpperCase(java.util.Locale.ROOT)), command.getAction());
            assertNull(command.getPath());
        }
        assertThrows(CommandException.class, () -> parser.parse("backup").execute(new ModelManager()));
        assertThrows(NullPointerException.class, () -> new DataCommand(null, null));
    }

    @Test
    public void parse_invalidRequests_rejectsMissingRepeatedUnknownAndMalformedArguments() {
        for (String input : List.of("", "  ", "unknown", "Import file/a", "import", "import file/ ",
                "import preamble file/a", "import file/a file/b", "import wrong/a", "import file/a unknown/b",
                "export file/a\0b", "backup file/a", "confirm extra", "cancel unknown/a")) {
            assertThrows(ParseException.class, () -> parser.parse(input), input);
        }
        for (String input : List.of("import file/\"unterminated", "import file/\"\"", "import file/\" \"",
                "import file/\"a\" file/b", "import file/\"a\" unknown/b", "export file/\"a\0b\"")) {
            assertThrows(ParseException.class, () -> parser.parse(input), input);
        }
        assertThrows(ParseException.class, () -> new AddressBookParser().parseCommand("Data backup"));
    }
}
