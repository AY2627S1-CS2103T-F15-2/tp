package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/**
 * Restores one preceding session state on a transaction candidate.
 */
public class UndoCommand extends Command {
    public static final String COMMAND_WORD = "undo";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Undoes the last successful record change."
            + " Takes no arguments.";
    public static final String MESSAGE_UNAVAILABLE = "Nothing to undo.";
    public static final String MESSAGE_SUCCESS = "Undid: %s.";

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
        if (!model.canUndoAddressBook()) {
            throw new CommandException(MESSAGE_UNAVAILABLE);
        }
        String description = model.undoAddressBook();
        return new CommandResult(String.format(MESSAGE_SUCCESS, description), DisplayRequest.unchanged());
    }
}
