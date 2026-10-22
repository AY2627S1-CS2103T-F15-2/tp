package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/**
 * Restores one undone session state on a transaction candidate.
 */
public class RedoCommand extends Command {
    public static final String COMMAND_WORD = "redo";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Redoes the last undone record change."
            + " Takes no arguments.";
    public static final String MESSAGE_UNAVAILABLE = "Nothing to redo.";
    public static final String MESSAGE_SUCCESS = "Redid: %s.";

    @Override
    public boolean isTransactional() {
        return true;
    }

    @Override
    public boolean recordsHistory() {
        return false;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (!model.canRedoAddressBook()) {
            throw new CommandException(MESSAGE_UNAVAILABLE);
        }
        String description = model.redoAddressBook();
        return new CommandResult(String.format(MESSAGE_SUCCESS, description), DisplayRequest.unchanged());
    }
}
