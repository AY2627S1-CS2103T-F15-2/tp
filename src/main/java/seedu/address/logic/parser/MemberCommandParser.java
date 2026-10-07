package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.NotImplementedCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses the {@code member} command family without implementing business operations. */
public class MemberCommandParser implements Parser<Command> {

    /**
     * Recognises a case-sensitive subcommand. Trailing arguments await business-command parsing.
     *
     * @throws ParseException if the subcommand is missing or unknown.
     */
    @Override
    public Command parse(String userInput) throws ParseException {
        requireNonNull(userInput);
        String subcommand = userInput.trim().split("\\s+", 2)[0];
        return switch (subcommand) {
            case "add", "list", "view", "delete" -> new NotImplementedCommand("member " + subcommand);
            default -> throw new ParseException("Expected member subcommand: add, list, view, delete.");
        };
    }
}
