package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.Model;
import seedu.address.model.query.LoanQuery;

/** Opens the Loans tab without modifying member, equipment or loan records. */
public class LoanListCommand extends Command {
    public static final String COMMAND_WORD = "loans list";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Opens Loans. Optional filters: [nus/TEXT] [uuid/TEXT]. No arguments retains filters."
            + " Use loans reset to clear both filters.";
    public static final String RESET_USAGE = "loans reset: Clears both Loans filters. Takes no arguments.";
    public static final String MESSAGE_SUCCESS = "Showing current loans.";

    private final LoanQuery query;

    /**
     * Opens the existing panel while retaining its filters.
     */
    public LoanListCommand() {
        query = null;
    }

    /**
     * Replaces both identity filters with explicit, immutable criteria.
     */
    public LoanListCommand(LoanQuery query) {
        this.query = requireNonNull(query);
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        return new CommandResult(MESSAGE_SUCCESS,
                query == null ? DisplayRequest.loans() : DisplayRequest.loans(query));
    }
}
