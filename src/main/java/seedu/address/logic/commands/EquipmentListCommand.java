package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.Model;

/** Requests the complete equipment directory without modifying domain data or collection order. */
public class EquipmentListCommand extends Command {

    public static final String COMMAND_WORD = "equipment list";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Lists all equipment items. Takes no arguments.";
    public static final String MESSAGE_SUCCESS = "Listed %d equipment items.";

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        return new CommandResult(String.format(MESSAGE_SUCCESS, model.getEquipmentList().size()),
                DisplayRequest.equipment());
    }
}
