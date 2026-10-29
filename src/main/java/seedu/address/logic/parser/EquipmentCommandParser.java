package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.LaplaceText;
import seedu.address.logic.commands.Command;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses the {@code equipment} command family through its concrete individual parsers. */
public class EquipmentCommandParser implements Parser<Command> {

    /**
     * Recognises a case-sensitive subcommand and forwards all remaining arguments for validation.
     *
     * @throws ParseException if the subcommand is missing or unknown.
     */
    @Override
    public Command parse(String userInput) throws ParseException {
        requireNonNull(userInput);
        String[] parts = LaplaceText.stripWhitespace(userInput).split("(?U)\\s+", 2);
        String subcommand = parts[0];
        String arguments = parts.length == 2 ? parts[1] : "";
        return switch (subcommand) {
            case "search" -> new DirectorySearchCommandParser(false).parse(arguments);
            case "list" -> new EquipmentListCommandParser().parse(arguments);
            case "add" -> new EquipmentAddCommandParser().parse(arguments);
            case "view" -> new EquipmentViewCommandParser().parse(arguments);
            case "edit" -> new EquipmentEditCommandParser().parse(arguments);
            case "restore" -> new EquipmentRestoreCommandParser().parse(arguments);
            case "delete" -> new EquipmentDeleteCommandParser().parse(arguments);
            default -> throw new ParseException(
                    "Expected equipment subcommand: add, list, search, view, edit, delete, restore.");
        };
    }
}
