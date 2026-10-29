package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.Locale;

import seedu.address.commons.util.LaplaceText;
import seedu.address.logic.commands.Command;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses the {@code loans} command family through its concrete individual parsers. */
public class LoansCommandParser implements Parser<Command> {

    /**
     * Recognizes a case-insensitive subcommand and forwards all remaining arguments for validation.
     *
     * @throws ParseException if the subcommand is missing or unknown.
     */
    @Override
    public Command parse(String userInput) throws ParseException {
        requireNonNull(userInput);
        String[] parts = LaplaceText.stripWhitespace(userInput).split("(?U)\\s+", 2);
        String subcommand = parts[0].toLowerCase(Locale.ROOT);
        String arguments = parts.length == 2 ? parts[1] : "";
        return switch (subcommand) {
            case "reset" -> new LoanResetCommandParser().parse(arguments);
            case "list" -> new LoanListCommandParser().parse(arguments);
            case "add" -> new LoanAddCommandParser().parse(arguments);
            case "return" -> new LoanReturnCommandParser().parse(arguments);
            default -> throw new ParseException("Expected loans subcommand: add, list, reset, return.");
        };
    }
}
