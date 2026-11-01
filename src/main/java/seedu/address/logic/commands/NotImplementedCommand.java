package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.Model;

/**
 * Reports a recognized Laplace command whose business behavior has not been implemented.
 */
public class NotImplementedCommand extends Command {

    public static final String MESSAGE_FORMAT = "The %s command is not implemented yet.";

    private final String commandName;

    public NotImplementedCommand(String commandName) {
        this.commandName = requireNonNull(commandName);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        return new CommandResult(String.format(MESSAGE_FORMAT, commandName));
    }
}
