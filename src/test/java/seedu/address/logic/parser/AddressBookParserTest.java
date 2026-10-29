package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.testutil.ViewTestData;

public class AddressBookParserTest {

    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parseCommand_aliasesAndCase_reusesCanonicalCommands() throws Exception {
        String uuid = "00000000-0000-0000-0000-000000000001";
        List<String> commands = List.of(
                "member list", "member view nus/A0123456X", "member delete nus/A0123456X",
                "member restore nus/A0123456X", "member edit nus/A0123456X name/Mixed Case Name",
                "member add nus/A0123456X name/Mixed Case Name phone/91234567 email/Mixed@example.com",
                "member search q/Mixed Case Name",
                "equipment list", "equipment view uuid/" + uuid, "equipment delete uuid/" + uuid,
                "equipment restore uuid/" + uuid, "equipment edit uuid/" + uuid + " notes/Keep CASE",
                "equipment add name/Mixed Camera category/Photo condition/GOOD notes/Keep CASE",
                "equipment search q/Keep CASE", "loans list",
                "loans add uuid/" + uuid + " nus/A0123456X assigned/2026-01-01 expected/2026-01-02",
                "loans return uuid/" + uuid + " returned/2026-01-02 condition/FAIR");
        for (String canonical : commands) {
            String[] parts = canonical.split(" ", 3);
            String arguments = parts.length == 3 ? " " + parts[2] : "";
            String alias = switch (parts[0]) {
                case "member" -> "members";
                case "loans" -> "loan";
                default -> parts[0];
            };
            for (String family : List.of(parts[0], alias)) {
                String input = family.toUpperCase(Locale.ROOT) + " "
                        + parts[1].toUpperCase(Locale.ROOT) + arguments;
                Command expected = parser.parseCommand(canonical);
                Command actual = parser.parseCommand(input);
                assertEquals(expected.getClass(), actual.getClass(), input);
                assertEquals(expected.isTransactional(), actual.isTransactional(), input);
                assertEquals(expected.isReadOnly(), actual.isReadOnly(), input);
                if (parts[1].equals("list") || parts[1].equals("search")) {
                    assertEquals(expected.execute(ViewTestData.model()), actual.execute(ViewTestData.model()), input);
                } else {
                    assertEquals(expected, actual, input);
                }
            }
        }
        for (String alias : List.of("loan list", "Loan list", "LOANS LIST")) {
            assertEquals(parser.parseCommand("loans list").execute(ViewTestData.model()),
                    parser.parseCommand(alias).execute(ViewTestData.model()));
        }
        assertTrue(parser.parseCommand("HELP") instanceof HelpCommand);
        assertTrue(parser.parseCommand("EXIT") instanceof ExitCommand);
        assertEquals(parser.parseCommand("mode dark").execute(ViewTestData.model()),
                parser.parseCommand("MODE DARK").execute(ViewTestData.model()));
        assertEquals(parser.parseCommand("undo").getClass(), parser.parseCommand("UNDO").getClass());
        assertEquals(parser.parseCommand("redo").getClass(), parser.parseCommand("ReDo").getClass());
    }

    @Test
    public void parseCommand_aliasFailures_preservesCanonicalDiagnostics() {
        for (String[] pair : List.of(
                new String[] {"loans add uuid/not-a-uuid", "LOAN ADD uuid/not-a-uuid"},
                new String[] {"member view nus/invalid", "MEMBERS VIEW nus/invalid"},
                new String[] {"member edit nus/A0123456X nus/A0234567R", "Members Edit nus/A0123456X nus/A0234567R"},
                new String[] {"equipment add NAME/Camera", "EQUIPMENT ADD NAME/Camera"},
                new String[] {"loans returns", "LOAN RETURNS"},
                new String[] {"member lists", "MEMBERS LISTS"})) {
            ParseException expected = org.junit.jupiter.api.Assertions.assertThrows(
                    ParseException.class, () -> parser.parseCommand(pair[0]));
            assertThrows(ParseException.class, expected.getMessage(), () -> parser.parseCommand(pair[1]));
        }
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("equipments list"));
    }

    @Test
    public void parseCommand_retiredPersonCommands_throwsUnknownCommand() {
        for (String input : new String[] {
            "add", "add n/Legacy Person p/98765432 e/legacy@example.com a/NUS",
            "edit", "edit 1 n/Updated Person", "delete", "delete 1",
            "clear", "clear 3", "find", "find Legacy", "list", "list members", " \tlist\tmembers \t"
        }) {
            assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand(input));
        }
    }

    @Test
    public void parseCommand_exit() throws Exception {
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD) instanceof ExitCommand);
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD + " 3") instanceof ExitCommand);
    }

    @Test
    public void parseCommand_help() throws Exception {
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD) instanceof HelpCommand);
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD + " 3") instanceof HelpCommand);
    }

    @Test
    public void parseCommand_unrecognisedInput_throwsParseException() {
        assertThrows(ParseException.class, String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE), ()
            -> parser.parseCommand(""));
    }

    @Test
    public void parseCommand_unknownCommand_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("unknownCommand"));
    }
}
