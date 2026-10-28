package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.UUID;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.equipment.Availability;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.equipment.exceptions.EquipmentNotFoundException;
import seedu.address.model.loan.Loan;
import seedu.address.model.loan.exceptions.OpenLoanNotFoundException;
import seedu.address.model.member.exceptions.MemberNotFoundException;

/** Resolves one physical item and its current holder without modifying application data. */
public class EquipmentViewCommand extends Command {
    public static final String COMMAND_WORD = "equipment view";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Shows equipment details. Parameters: uuid/UUID\n"
            + "Example: equipment view uuid/550e8400-e29b-41d4-a716-446655440000";
    public static final String MESSAGE_NOT_FOUND = "No equipment with UUID %s exists.";

    private final UUID targetUuid;

    /** Creates a view command for a non-null equipment identity. */
    public EquipmentViewCommand(UUID uuid) {
        targetUuid = requireNonNull(uuid);
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        try {
            Equipment equipment = model.findEquipment(targetUuid).orElseThrow(() ->
                    new CommandException(String.format(MESSAGE_NOT_FOUND, targetUuid)));
            Availability availability = model.getAvailability(targetUuid);
            Optional<Loan> loan = model.findOpenLoan(targetUuid);
            if (loan.isPresent()) {
                Loan current = loan.orElseThrow();
                if (!current.isOpen() || !current.getEquipmentUuid().equals(targetUuid)
                        || availability != Availability.ASSIGNED) {
                    throw new CommandException("Inconsistent loan or availability for equipment " + targetUuid + ".");
                }
                model.findMember(current.getMemberNusId()).orElseThrow(() -> new CommandException(
                        "Missing holder reference " + current.getMemberNusId() + " for equipment " + targetUuid + "."));
            } else if (availability == Availability.ASSIGNED) {
                throw new CommandException("Missing open loan for assigned equipment " + targetUuid + ".");
            }
            return new CommandResult("Viewing equipment: " + equipment.getName() + " (" + targetUuid
                    + "). Availability: " + availability + ".", DisplayRequest.equipmentDetails(targetUuid));
        } catch (EquipmentNotFoundException | MemberNotFoundException | OpenLoanNotFoundException e) {
            throw new CommandException("Could not resolve equipment " + targetUuid
                    + " and its current loan references.", e);
        }
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof EquipmentViewCommand otherCommand
                && targetUuid.equals(otherCommand.targetUuid);
    }

    @Override
    public int hashCode() {
        return targetUuid.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("targetUuid", targetUuid).toString();
    }
}
