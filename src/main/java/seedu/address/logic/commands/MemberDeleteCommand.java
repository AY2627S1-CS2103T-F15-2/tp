package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.model.member.exceptions.MemberNotFoundException;

/**
 * Deletes a stored member by normalized NUS ID, delegating loan integrity to the model.
 */
public class MemberDeleteCommand extends Command {

    public static final String COMMAND_WORD = "member delete";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes a member without open loans. Parameters: nus/NUS_ID\n"
            + "Example: member delete nus/A0123456X";
    public static final String MESSAGE_SUCCESS = "Deleted member: %1$s";
    public static final String MESSAGE_NOT_FOUND = "No member with NUS ID %1$s exists.";
    public static final String MESSAGE_OPEN_LOAN =
            "Cannot delete member %1$s with open loans. Return their outstanding equipment first.";

    private final NusId targetNusId;

    /**
     * Creates a deletion command for a non-null member identity.
     */
    public MemberDeleteCommand(NusId nusId) {
        targetNusId = requireNonNull(nusId);
    }

    @Override
    public boolean isTransactional() {
        return true;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        try {
            Member member = model.findMember(targetNusId).orElseThrow(MemberNotFoundException::new);
            model.deleteMember(member);
        } catch (MemberNotFoundException e) {
            throw new CommandException(String.format(MESSAGE_NOT_FOUND, targetNusId), e);
        } catch (IllegalStateException e) {
            throw new CommandException(String.format(MESSAGE_OPEN_LOAN, targetNusId), e);
        }
        return new CommandResult(String.format(MESSAGE_SUCCESS, targetNusId), DisplayRequest.members());
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof MemberDeleteCommand otherCommand
                && targetNusId.equals(otherCommand.targetNusId);
    }

    @Override
    public int hashCode() {
        return targetNusId.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("targetNusId", targetNusId).toString();
    }
}
