package seedu.address.logic.commands;

import java.util.List;
import java.util.stream.Collectors;

import seedu.address.logic.commands.exceptions.CommandException;

/**
 * Formats all rejected units before either loan command can mutate its candidate.
 */
final class LoanBatchValidation {
    private LoanBatchValidation() {
    }

    /**
     * Throws the original single error or one ordered batch diagnostic retaining every cause.
     */
    static void rejectInvalidUnits(List<CommandException> errors, int unitCount) throws CommandException {
        if (errors.isEmpty()) {
            return;
        }
        if (errors.size() == 1) {
            throw errors.getFirst();
        }
        String details = errors.stream().map(Throwable::getMessage).collect(Collectors.joining("\n"));
        CommandException failure = new CommandException("Rejected batch: " + errors.size() + " of " + unitCount
                + " units are invalid. No records changed. Open Details for all errors.\n" + details,
                errors.getFirst());
        errors.stream().skip(1).forEach(failure::addSuppressed);
        throw failure;
    }
}
