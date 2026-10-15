package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DisplayRequest;
import seedu.address.logic.commands.LoanListCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;

/** Checks no-argument Loans navigation through both the individual and complete command parsers. */
public class LoanListCommandParserTest {
    @Test
    public void parse_whitespaceOnly_returnsLoansNavigation() throws Exception {
        for (String args : List.of("", " ", "\t\n")) {
            LoanListCommand command = new LoanListCommandParser().parse(args);
            assertEquals(new CommandResult("Showing current loans.", DisplayRequest.loans()),
                    command.execute(new ModelManager()));
        }
        assertInstanceOf(LoanListCommand.class, new LoansCommandParser().parse("list"));
        assertInstanceOf(LoanListCommand.class, new AddressBookParser().parseCommand(" \tloans\tlist \t"));
    }

    @Test
    public void parse_arguments_rejectsWithOriginalCauseAndUsage() {
        for (String args : List.of("extra", "uuid/value", "nus/A0123456X", "unknown/a unknown/b")) {
            ParseException failure = assertThrows(ParseException.class, () ->
                    new LoanListCommandParser().parse(args));
            assertTrue(failure.getMessage().contains(LoanListCommand.MESSAGE_USAGE));
            assertInstanceOf(ParseException.class, failure.getCause());
        }
    }

    @Test
    public void parse_nullArgumentsOrModel_rejectsNull() {
        assertThrows(NullPointerException.class, () -> new LoanListCommandParser().parse(null));
        assertThrows(NullPointerException.class, () -> new LoanListCommand().execute(null));
    }
}
