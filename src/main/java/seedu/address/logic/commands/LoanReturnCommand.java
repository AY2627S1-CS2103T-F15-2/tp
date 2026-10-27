package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.equipment.exceptions.EquipmentNotFoundException;
import seedu.address.model.loan.Loan;
import seedu.address.model.loan.exceptions.OpenLoanNotFoundException;
import seedu.address.model.member.exceptions.MemberNotFoundException;

/** Completes one or more loans with a shared return date and observed condition. */
public class LoanReturnCommand extends Command {
    public static final String COMMAND_WORD = "loans return";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Parameters: uuid/UUID[,UUID...] returned/DATE [condition/CONDITION]\n"
            + "Dates must use yyyy-MM-dd.";
    public static final String MESSAGE_SUCCESS = "Returned %1$s (%2$s) from %3$s (%4$s) on %5$s.";
    public static final String MESSAGE_BATCH_SUCCESS = "Returned %1$d units (%2$s) on %3$s.";
    public static final String MESSAGE_EQUIPMENT_NOT_FOUND = "No equipment with UUID %1$s exists.";
    public static final String MESSAGE_NO_OPEN_LOAN = "Equipment %1$s has no open loan to return.";
    public static final String MESSAGE_MEMBER_NOT_FOUND =
            "Cannot return equipment %1$s: its open loan refers to a missing member.";
    public static final String MESSAGE_RETURN_BEFORE_ASSIGNMENT =
            "Cannot return %1$s (%2$s) on %3$s: it was assigned on %4$s.";
    public static final String MESSAGE_FUTURE_RETURN = "Returned date cannot be after today (%1$s).";

    private final List<UUID> equipmentUuids;
    private final LocalDate returnedDate;
    private final Condition returnCondition;
    private final Clock clock;

    /** Creates an individual return using the existing syntax. */
    public LoanReturnCommand(UUID equipmentUuid, LocalDate returnedDate) {
        this(List.of(requireNonNull(equipmentUuid)), returnedDate, null);
    }

    /** Creates a return batch with an optional shared observed condition. */
    public LoanReturnCommand(List<UUID> equipmentUuids, LocalDate returnedDate, Condition returnCondition) {
        this(equipmentUuids, returnedDate, returnCondition, Clock.systemDefaultZone());
    }

    /** Supplies a clock for deterministic single-item tests. */
    LoanReturnCommand(UUID equipmentUuid, LocalDate returnedDate, Clock clock) {
        this(List.of(requireNonNull(equipmentUuid)), returnedDate, null, clock);
    }

    /** Supplies a clock for deterministic batch tests. */
    LoanReturnCommand(List<UUID> equipmentUuids, LocalDate returnedDate, Condition returnCondition, Clock clock) {
        requireAllNonNull(equipmentUuids, returnedDate, clock);
        this.equipmentUuids = List.copyOf(equipmentUuids);
        if (this.equipmentUuids.isEmpty() || new HashSet<>(this.equipmentUuids).size() != this.equipmentUuids.size()) {
            throw new IllegalArgumentException("Return requires distinct equipment UUIDs");
        }
        this.returnedDate = returnedDate;
        this.returnCondition = returnCondition;
        this.clock = clock;
    }

    @Override
    public boolean isTransactional() {
        return true;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        LocalDate today = LocalDate.now(clock);
        if (returnedDate.isAfter(today)) {
            throw new CommandException(String.format(MESSAGE_FUTURE_RETURN, today));
        }
        List<CommandException> errors = new ArrayList<>();
        for (UUID uuid : equipmentUuids) {
            try {
                validateReturn(model, uuid);
            } catch (CommandException exception) {
                errors.add(exception);
            }
        }
        LoanBatchValidation.rejectInvalidUnits(errors, equipmentUuids.size());
        UUID first = equipmentUuids.getFirst();
        Equipment firstItem = model.findEquipment(first).orElseThrow();
        Loan firstLoan = model.findOpenLoan(first).orElseThrow();
        var firstMember = model.findMember(firstLoan.getMemberNusId()).orElseThrow();
        String firstName = firstItem.getName();
        String firstMemberName = firstMember.getName().toString();
        String firstMemberId = firstMember.getNusId().toString();
        try {
            if (equipmentUuids.size() == 1 && returnCondition == null) {
                model.closeLoan(equipmentUuids.get(0), returnedDate);
            } else {
                model.returnLoans(equipmentUuids, returnedDate, returnCondition);
            }
        } catch (OpenLoanNotFoundException exception) {
            throw new CommandException(String.format(MESSAGE_NO_OPEN_LOAN, equipmentUuids.get(0)), exception);
        } catch (EquipmentNotFoundException | MemberNotFoundException exception) {
            throw new CommandException("Could not return equipment: " + exception.getMessage(), exception);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw new CommandException("Could not return equipment: " + exception.getMessage(), exception);
        }
        String identifiers = equipmentUuids.size() == 1 ? first.toString() : equipmentUuids.toString();
        DisplayRequest display = equipmentUuids.size() == 1
                ? DisplayRequest.equipmentDetails(first) : DisplayRequest.loans();
        String feedback = equipmentUuids.size() == 1
                ? String.format(MESSAGE_SUCCESS, firstName, identifiers, firstMemberName, firstMemberId, returnedDate)
                : String.format(MESSAGE_BATCH_SUCCESS, equipmentUuids.size(), identifiers, returnedDate);
        return new CommandResult(feedback, display);
    }

    /**
     * Validates a unit and its references without closing loans or changing conditions.
     */
    private void validateReturn(Model model, UUID uuid) throws CommandException {
        Equipment item;
        Loan open;
        try {
            item = model.findEquipment(uuid).orElseThrow(EquipmentNotFoundException::new);
        } catch (EquipmentNotFoundException exception) {
            throw new CommandException(String.format(MESSAGE_EQUIPMENT_NOT_FOUND, uuid), exception);
        }
        try {
            open = model.findOpenLoan(uuid).orElseThrow(OpenLoanNotFoundException::new);
        } catch (OpenLoanNotFoundException exception) {
            throw new CommandException(String.format(MESSAGE_NO_OPEN_LOAN, uuid), exception);
        }
        model.findMember(open.getMemberNusId()).orElseThrow(() ->
                new CommandException(String.format(MESSAGE_MEMBER_NOT_FOUND, uuid),
                        new MemberNotFoundException()));
        if (returnedDate.isBefore(open.getAssignedDate())) {
            throw new CommandException(String.format(MESSAGE_RETURN_BEFORE_ASSIGNMENT,
                    item.getName(), uuid, returnedDate, open.getAssignedDate()));
        }
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof LoanReturnCommand command
                && equipmentUuids.equals(command.equipmentUuids) && returnedDate.equals(command.returnedDate)
                && returnCondition == command.returnCondition;
    }

    @Override
    public int hashCode() {
        return Objects.hash(equipmentUuids, returnedDate, returnCondition);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("equipmentUuids", equipmentUuids)
                .add("returnedDate", returnedDate).add("returnCondition", returnCondition).toString();
    }
}
