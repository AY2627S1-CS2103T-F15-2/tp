package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.Model;

/**
 * Opens the Loans tab without modifying member, equipment or loan records.
 */
public class LoanListCommand extends Command {
    public static final String COMMAND_WORD = "loans list";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Opens the Loans tab. Takes no arguments.";
    public static final String MESSAGE_SUCCESS = "Showing current loans.";

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        return new CommandResult(MESSAGE_SUCCESS, DisplayRequest.loans());
    }
}
