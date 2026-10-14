package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.Model;

/** Requests the complete member directory without modifying domain data or collection order. */
public class MemberListCommand extends Command {

    public static final String COMMAND_WORD = "member list";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Lists all active members. Takes no arguments.";
    public static final String MESSAGE_SUCCESS = "Listed %d active members.";

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        return new CommandResult(String.format(MESSAGE_SUCCESS, model.getMemberList().size()),
                DisplayRequest.members());
    }
}
