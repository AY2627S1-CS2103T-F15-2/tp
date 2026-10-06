package seedu.address.logic.parser;

import seedu.address.logic.commands.Command;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parser contract for the {@code loans} command family. */
public class LoansCommandParser implements Parser<Command> {

    @Override
    public Command parse(String userInput) throws ParseException {
        throw new UnsupportedOperationException("Loan command parsing is not implemented");
    }
}
