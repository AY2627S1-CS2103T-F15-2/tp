package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

import seedu.address.commons.util.LaplaceSortOrder;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.equipment.exceptions.EquipmentNotFoundException;
import seedu.address.model.loan.Loan;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.model.member.exceptions.MemberNotFoundException;

/** Resolves a member and their current holdings without modifying application data. */
public class MemberViewCommand extends Command {
    public static final String COMMAND_WORD = "member view";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Shows member details and current holdings. Parameters: nus/NUS_ID\n"
            + "Example: member view nus/A0123456X";
    public static final String MESSAGE_NOT_FOUND = "No member with NUS ID %s exists.";

    private final NusId targetNusId;

    /** Creates a view command for a non-null member identity. */
    public MemberViewCommand(NusId nusId) {
        targetNusId = requireNonNull(nusId);
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        try {
            Member member = model.findMember(targetNusId).orElseThrow(() ->
                    new CommandException(String.format(MESSAGE_NOT_FOUND, targetNusId)));
            List<Equipment> holdings = new ArrayList<>();
            for (Loan loan : model.findOpenLoansForMember(targetNusId)) {
                if (!loan.isOpen() || !loan.getMemberNusId().equals(targetNusId)) {
                    throw new CommandException("Inconsistent current holdings for member " + targetNusId + ".");
                }
                holdings.add(model.findEquipment(loan.getEquipmentUuid()).orElseThrow(() -> new CommandException(
                        "Missing equipment reference " + loan.getEquipmentUuid()
                                + " for member " + targetNusId + ".")));
            }
            holdings.sort(LaplaceSortOrder.EQUIPMENT);
            String name = member.getName() + (member.isDeleted() ? " (deleted)" : "");
            return new CommandResult("Viewing member: " + name + " (" + targetNusId
                    + "). Currently holds " + holdings.size() + " item(s).", DisplayRequest.memberDetails(targetNusId));
        } catch (MemberNotFoundException | EquipmentNotFoundException e) {
            throw new CommandException("Could not resolve member " + targetNusId + " and their current holdings.", e);
        }
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof MemberViewCommand otherCommand
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
