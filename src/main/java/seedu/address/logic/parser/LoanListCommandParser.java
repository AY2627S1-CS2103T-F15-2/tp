package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_EQUIPMENT_UUID;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NUS_ID;

import java.util.Set;

import seedu.address.logic.commands.LoanListCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.query.LoanQuery;

/**
 * Parses optional identity substrings; an argument-free list retains the panel's filters.
 */
public class LoanListCommandParser implements Parser<LoanListCommand> {
    @Override
    public LoanListCommand parse(String args) throws ParseException {
        try {
            ArgumentMultimap values = LaplaceArgumentParser.parse(args, Set.of(),
                    Set.of(PREFIX_NUS_ID, PREFIX_EQUIPMENT_UUID));
            if (values.getValue(PREFIX_NUS_ID).isEmpty() && values.getValue(PREFIX_EQUIPMENT_UUID).isEmpty()) {
                return new LoanListCommand();
            }
            return new LoanListCommand(new LoanQuery(values.getValue(PREFIX_NUS_ID).orElse(""),
                    values.getValue(PREFIX_EQUIPMENT_UUID).orElse("")));
        } catch (ParseException e) {
            throw new ParseException(e.getMessage() + "\n" + LoanListCommand.MESSAGE_USAGE, e);
        }
    }
}
