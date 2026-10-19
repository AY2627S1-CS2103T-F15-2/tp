package seedu.address.logic.parser;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Set;

import seedu.address.logic.commands.DataCommand;
import seedu.address.logic.commands.DataCommand.Action;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses the data command family using the existing strict prefix grammar. */
public class DataCommandParser implements Parser<DataCommand> {
    public static final String MESSAGE_USAGE =
            "Use data import|export|restore file/PATH, or data backup|confirm|cancel.";
    private static final Prefix FILE = new Prefix("file/");

    @Override
    public DataCommand parse(String args) throws ParseException {
        String[] parts = args.strip().split("\\s+", 2);
        String arguments = parts.length == 2 ? parts[1] : "";
        return switch (parts[0]) {
            case "import" -> withPath(Action.IMPORT, arguments);
            case "export" -> withPath(Action.EXPORT, arguments);
            case "restore" -> withPath(Action.RESTORE, arguments);
            case "backup" -> withoutPath(Action.BACKUP, arguments);
            case "confirm" -> withoutPath(Action.CONFIRM, arguments);
            case "cancel" -> withoutPath(Action.CANCEL, arguments);
            default -> throw new ParseException(MESSAGE_USAGE);
        };
    }

    private DataCommand withPath(Action action, String arguments) throws ParseException {
        String value;
        if (arguments.startsWith("file/\"")) {
            if (!arguments.matches("file/\"[^\"]+\"\\s*")) {
                throw new ParseException("Use one complete quoted file path. " + MESSAGE_USAGE);
            }
            value = arguments.substring(6, arguments.lastIndexOf('"'));
            if (value.isBlank()) {
                throw new ParseException("File path must not be blank.");
            }
        } else {
            value = LaplaceArgumentParser.parse(arguments, Set.of(FILE), Set.of()).getValue(FILE).orElseThrow();
        }
        try {
            return new DataCommand(action, Path.of(value));
        } catch (InvalidPathException exception) {
            throw new ParseException("Invalid file path. " + MESSAGE_USAGE, exception);
        }
    }

    private DataCommand withoutPath(Action action, String arguments) throws ParseException {
        LaplaceArgumentParser.parse(arguments, Set.of(), Set.of());
        return new DataCommand(action, null);
    }
}
