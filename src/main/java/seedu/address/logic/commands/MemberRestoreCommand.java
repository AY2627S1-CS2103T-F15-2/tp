package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.model.member.exceptions.MemberNotFoundException;

/**
 * Restores a retained member by normalized NUS ID, without any recovery deadline.
 */
public class MemberRestoreCommand extends Command {
    public static final String COMMAND_WORD = "member restore";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Restores a deleted member. Parameters: nus/NUS_ID\n"
            + "Example: member restore nus/A0123456X";
    public static final String MESSAGE_SUCCESS = "Restored member %1$s: %2$s.";
    public static final String MESSAGE_ACTIVE = "Member %s is not deleted.";

    private final NusId targetNusId;

    /**
     * Creates a restoration command for an exact, non-null identity.
     */
    public MemberRestoreCommand(NusId nusId) {
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
            assert member.getNusId().equals(targetNusId) : "Member lookup must preserve the requested identity.";
            model.restoreMember(member);
            return new CommandResult(String.format(MESSAGE_SUCCESS, targetNusId, member.getName()),
                    DisplayRequest.memberDetails(targetNusId));
        } catch (MemberNotFoundException exception) {
            throw new CommandException(String.format(MemberDeleteCommand.MESSAGE_NOT_FOUND, targetNusId), exception);
        } catch (IllegalStateException exception) {
            throw new CommandException(String.format(MESSAGE_ACTIVE, targetNusId), exception);
        }
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof MemberRestoreCommand otherCommand
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
