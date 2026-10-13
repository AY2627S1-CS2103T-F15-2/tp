package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.member.Member;
import seedu.address.model.member.exceptions.DuplicateMemberException;

/**
 * Adds a member identified by a normalized NUS ID.
 */
public class MemberAddCommand extends Command {

    public static final String COMMAND_WORD = "member add";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Adds a member. Parameters: nus/NUS_ID name/NAME phone/PHONE email/EMAIL\n"
            + "Example: member add nus/A0123456X name/Alex Tan phone/91234567 email/alex@example.com";
    public static final String MESSAGE_SUCCESS = "New member added: %1$s (%2$s)";
    public static final String MESSAGE_DUPLICATE_MEMBER = "A member with NUS ID %1$s already exists.";

    private final Member toAdd;

    /**
     * Creates a command retaining the immutable member to add.
     */
    public MemberAddCommand(Member member) {
        toAdd = requireNonNull(member);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        try {
            model.addMember(toAdd);
        } catch (DuplicateMemberException e) {
            throw new CommandException(String.format(MESSAGE_DUPLICATE_MEMBER, toAdd.getNusId()), e);
        }
        return new CommandResult(String.format(MESSAGE_SUCCESS, toAdd.getName(), toAdd.getNusId()),
                DisplayRequest.memberDetails(toAdd.getNusId()));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof MemberAddCommand otherCommand && toAdd.equals(otherCommand.toAdd);
    }

    @Override
    public int hashCode() {
        return toAdd.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("toAdd", toAdd).toString();
    }
}
