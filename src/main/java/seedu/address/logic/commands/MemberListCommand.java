package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.Model;

/** Requests the complete member directory without modifying domain data or collection order. */
public class MemberListCommand extends Command {

    public static final String COMMAND_WORD = "member list";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Lists active and deleted members together. Takes no arguments.";
    public static final String MESSAGE_SUCCESS = "Listed %d active members.";

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        long deleted = model.getMemberList().stream().filter(member -> member.isDeleted()).count();
        long active = model.getMemberList().size() - deleted;
        String feedback = String.format(MESSAGE_SUCCESS, active);
        if (deleted > 0) {
            feedback += " " + deleted + " deleted member(s) remain visible.";
        }
        return new CommandResult(feedback,
                DisplayRequest.members());
    }
}
