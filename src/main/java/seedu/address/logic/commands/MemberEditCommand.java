package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.model.member.exceptions.MemberDeletedException;
import seedu.address.model.member.exceptions.MemberNotFoundException;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;

/**
 * Edits supplied member details while preserving the NUS ID and every loan reference.
 */
public class MemberEditCommand extends Command {
    public static final String COMMAND_WORD = "member edit";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Edits member details. Parameters: nus/NUS_ID [name/NAME] [phone/PHONE] [email/EMAIL]\n"
            + "Example: member edit nus/A0123456X phone/+65 9876-5432";
    public static final String MESSAGE_NO_FIELDS = "At least one field to edit must be provided.";
    public static final String MESSAGE_NO_CHANGE = "The supplied details do not change this member.";
    public static final String MESSAGE_NOT_FOUND = "No member with NUS ID %s exists.";
    public static final String MESSAGE_DELETED = "Cannot edit deleted member %1$s. "
            + "Use member restore nus/%1$s first.";
    public static final String MESSAGE_SUCCESS = "Edited member %1$s: %2$s.";

    private final NusId targetNusId;
    private final Optional<Name> name;
    private final Optional<Phone> phone;
    private final Optional<Email> email;

    /**
     * Creates an edit with at least one supplied field; empty optionals preserve stored values.
     */
    public MemberEditCommand(NusId targetNusId, Optional<Name> name, Optional<Phone> phone, Optional<Email> email) {
        this.targetNusId = requireNonNull(targetNusId);
        this.name = requireNonNull(name);
        this.phone = requireNonNull(phone);
        this.email = requireNonNull(email);
        checkArgument(name.isPresent() || phone.isPresent() || email.isPresent(), MESSAGE_NO_FIELDS);
    }

    @Override
    public boolean isTransactional() {
        return true;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        try {
            Member stored = model.findMember(targetNusId).orElseThrow(MemberNotFoundException::new);
            if (stored.isDeleted()) {
                throw new MemberDeletedException();
            }
            Member edited = new Member(stored.getNusId(), name.orElse(stored.getName()),
                    phone.orElse(stored.getPhone()), email.orElse(stored.getEmail()), stored.isDeleted());
            if (stored.equals(edited)) {
                throw new CommandException(MESSAGE_NO_CHANGE);
            }
            model.setMember(stored, edited);
            return new CommandResult(String.format(MESSAGE_SUCCESS, targetNusId, edited.getName()),
                    DisplayRequest.memberDetails(targetNusId));
        } catch (MemberDeletedException exception) {
            throw new CommandException(String.format(MESSAGE_DELETED, targetNusId), exception);
        } catch (MemberNotFoundException exception) {
            throw new CommandException(String.format(MESSAGE_NOT_FOUND, targetNusId), exception);
        }
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof MemberEditCommand otherCommand
                && targetNusId.equals(otherCommand.targetNusId) && name.equals(otherCommand.name)
                && phone.equals(otherCommand.phone) && email.equals(otherCommand.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetNusId, name, phone, email);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("targetNusId", targetNusId).add("name", name)
                .add("phone", phone).add("email", email).toString();
    }
}
