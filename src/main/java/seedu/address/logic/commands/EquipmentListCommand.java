package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.Model;
import seedu.address.model.query.DirectoryQuery;

/** Requests the complete equipment directory without modifying domain data or collection order. */
public class EquipmentListCommand extends Command {

    public static final String COMMAND_WORD = "equipment list";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Lists all equipment items, including deleted units. Takes no arguments.";
    public static final String MESSAGE_SUCCESS = "Listed %d equipment items.";
    public static final String MESSAGE_SUCCESS_SINGULAR = "Listed %d equipment item.";

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        int count = model.getEquipmentList().size();
        String message = count == 1 ? MESSAGE_SUCCESS_SINGULAR : MESSAGE_SUCCESS;
        return new CommandResult(String.format(message, count),
                DisplayRequest.equipment(DirectoryQuery.all()));
    }
}
