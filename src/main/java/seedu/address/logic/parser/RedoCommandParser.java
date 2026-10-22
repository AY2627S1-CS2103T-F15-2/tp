package seedu.address.logic.parser;

import java.util.Set;

import seedu.address.logic.commands.RedoCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses the no-argument {@code redo} command.
 */
public class RedoCommandParser implements Parser<RedoCommand> {
    @Override
    public RedoCommand parse(String args) throws ParseException {
        try {
            LaplaceArgumentParser.parse(args, Set.of(), Set.of());
        } catch (ParseException exception) {
            throw new ParseException(exception.getMessage() + "\n" + RedoCommand.MESSAGE_USAGE, exception);
        }
        return new RedoCommand();
    }
}
