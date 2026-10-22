package seedu.address.logic.parser;

import java.util.Set;

import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses the no-argument {@code undo} command.
 */
public class UndoCommandParser implements Parser<UndoCommand> {
    @Override
    public UndoCommand parse(String args) throws ParseException {
        try {
            LaplaceArgumentParser.parse(args, Set.of(), Set.of());
        } catch (ParseException exception) {
            throw new ParseException(exception.getMessage() + "\n" + UndoCommand.MESSAGE_USAGE, exception);
        }
        return new UndoCommand();
    }
}
