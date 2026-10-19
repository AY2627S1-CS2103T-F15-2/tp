package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.transfer.DataTransferService;
import seedu.address.model.Model;

/** A parsed transfer request, with filesystem work delegated to the logic-owned transfer service. */
public class DataCommand extends Command {
    /** Supported data-transfer operations. */
    public enum Action { IMPORT, EXPORT, BACKUP, RESTORE, CONFIRM, CANCEL }

    private final Action action;
    private final Path path;

    /** Creates a request; only import, export and restore carry a file path. */
    public DataCommand(Action action, Path path) {
        this.action = requireNonNull(action);
        this.path = path;
    }

    public Action getAction() {
        return action;
    }

    public Path getPath() {
        return path;
    }

    @Override
    public boolean requiresPersistence() {
        return false;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        throw new CommandException("Data commands require the data-transfer service.");
    }

    @Override
    public CommandResult execute(Model model, DataTransferService transfers) throws CommandException {
        return transfers.execute(this, model);
    }
}
