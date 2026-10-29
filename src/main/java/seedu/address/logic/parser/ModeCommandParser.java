package seedu.address.logic.parser;

import java.util.Locale;

import seedu.address.logic.commands.ModeCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses exactly one supported appearance name.
 */
public class ModeCommandParser implements Parser<ModeCommand> {
    @Override
    public ModeCommand parse(String arguments) throws ParseException {
        return switch (arguments.trim().toLowerCase(Locale.ROOT)) {
            case "dark" -> new ModeCommand(true);
            case "light" -> new ModeCommand(false);
            default -> throw new ParseException(ModeCommand.MESSAGE_USAGE);
        };
    }
}
