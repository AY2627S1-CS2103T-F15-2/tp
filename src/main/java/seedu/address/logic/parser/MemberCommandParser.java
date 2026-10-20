package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.Command;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses the {@code member} command family through its concrete individual parsers. */
public class MemberCommandParser implements Parser<Command> {

    /**
     * Recognises a case-sensitive subcommand and forwards all remaining arguments for validation.
     *
     * @throws ParseException if the subcommand is missing or unknown.
     */
    @Override
    public Command parse(String userInput) throws ParseException {
        requireNonNull(userInput);
        String[] parts = userInput.stripLeading().split("\\s+", 2);
        String subcommand = parts[0];
        String arguments = parts.length == 2 ? parts[1] : "";
        return switch (subcommand) {
            case "list" -> new MemberListCommandParser().parse(arguments);
            case "add" -> new MemberAddCommandParser().parse(arguments);
            case "view" -> new MemberViewCommandParser().parse(arguments);
            case "delete" -> new MemberDeleteCommandParser().parse(arguments);
            case "restore" -> new MemberRestoreCommandParser().parse(arguments);
            default -> throw new ParseException("Expected member subcommand: add, list, view, delete, restore.");
        };
    }
}
