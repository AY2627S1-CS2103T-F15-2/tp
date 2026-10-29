package seedu.address.logic.parser;

import java.util.Set;

import seedu.address.logic.commands.LoanListCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.query.LoanQuery;

/**
 * Parses the explicit, read-only Loans filter reset command.
 */
public class LoanResetCommandParser implements Parser<LoanListCommand> {
    @Override
    public LoanListCommand parse(String args) throws ParseException {
        try {
            LaplaceArgumentParser.parse(args, Set.of(), Set.of());
        } catch (ParseException exception) {
            throw new ParseException(exception.getMessage() + "\n" + LoanListCommand.RESET_USAGE, exception);
        }
        return new LoanListCommand(LoanQuery.all());
    }
}
