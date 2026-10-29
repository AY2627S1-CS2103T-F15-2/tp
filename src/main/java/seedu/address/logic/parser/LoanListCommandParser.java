package seedu.address.logic.parser;

import java.util.Set;

import seedu.address.logic.commands.LoanListCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses the no-argument {@code loans list} navigation command.
 */
public class LoanListCommandParser implements Parser<LoanListCommand> {
    @Override
    public LoanListCommand parse(String args) throws ParseException {
        try {
            LaplaceArgumentParser.parse(args, Set.of(), Set.of());
        } catch (ParseException e) {
            throw new ParseException(e.getMessage() + "\n" + LoanListCommand.MESSAGE_USAGE, e);
        }
        return new LoanListCommand();
    }
}
