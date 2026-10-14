package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.EquipmentListCommand;
import seedu.address.logic.commands.MemberListCommand;
import seedu.address.logic.parser.exceptions.ParseException;

public class LaplaceListCommandParserTest {

    @Test
    public void parse_whitespaceOnly_returnsConcreteCommands() throws Exception {
        for (String args : new String[] {"", " ", "\t \n"}) {
            assertInstanceOf(MemberListCommand.class, new MemberListCommandParser().parse(args));
            assertInstanceOf(EquipmentListCommand.class, new EquipmentListCommandParser().parse(args));
        }
    }

    @Test
    public void parse_arguments_rejectsWithCauseAndUsage() {
        List<Parser<? extends Command>> parsers = List.of(new MemberListCommandParser(),
                new EquipmentListCommandParser());
        for (Parser<? extends Command> parser : parsers) {
            String usage = parser instanceof MemberListCommandParser
                    ? MemberListCommand.MESSAGE_USAGE : EquipmentListCommand.MESSAGE_USAGE;
            for (String args : new String[] {"extra", "2", "nus/A0000001X", "notes/", "unknown/a unknown/b"}) {
                ParseException failure = org.junit.jupiter.api.Assertions.assertThrows(ParseException.class, () ->
                        parser.parse(args));
                assertTrue(failure.getMessage().contains(usage));
                assertTrue(failure.getCause() instanceof ParseException);
            }
        }
    }

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new MemberListCommandParser().parse(null));
        assertThrows(NullPointerException.class, () -> new EquipmentListCommandParser().parse(null));
    }
}
