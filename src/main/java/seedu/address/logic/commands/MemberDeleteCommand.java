package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.model.member.exceptions.MemberDeletedException;
import seedu.address.model.member.exceptions.MemberNotFoundException;

/**
 * Soft-deletes a stored member by normalized NUS ID, delegating loan integrity to the model.
 */
public class MemberDeleteCommand extends Command {

    public static final String COMMAND_WORD = "member delete";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Soft-deletes a member without open loans. Parameters: nus/NUS_ID\n"
            + "Example: member delete nus/A0123456X";
    public static final String MESSAGE_SUCCESS = "Deleted member %1$s: %2$s.";
    public static final String MESSAGE_NOT_FOUND = "No member with NUS ID %1$s exists.";
    public static final String MESSAGE_ALREADY_DELETED =
            "Member %1$s is already deleted. Use member restore nus/%1$s to restore them.";
    public static final String MESSAGE_OPEN_LOAN =
            "Cannot delete %1$s because the member holds %2$d item(s). Return the items first.";

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
            int heldCount = model.findOpenLoansForMember(targetNusId).size();
            try {
                model.deleteMember(member);
            } catch (MemberDeletedException e) {
                throw new CommandException(String.format(MESSAGE_ALREADY_DELETED, targetNusId), e);
            } catch (IllegalStateException e) {
                throw new CommandException(String.format(MESSAGE_OPEN_LOAN, targetNusId, heldCount), e);
            }
            return new CommandResult(String.format(MESSAGE_SUCCESS, targetNusId, member.getName()),
                    DisplayRequest.members());
        } catch (MemberNotFoundException e) {
            throw new CommandException(String.format(MESSAGE_NOT_FOUND, targetNusId), e);
        }
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
