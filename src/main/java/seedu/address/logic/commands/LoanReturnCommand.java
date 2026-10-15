package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.equipment.exceptions.EquipmentNotFoundException;
import seedu.address.model.loan.Loan;
import seedu.address.model.loan.exceptions.OpenLoanNotFoundException;
import seedu.address.model.member.Member;
import seedu.address.model.member.exceptions.MemberNotFoundException;

/** Records a validated equipment return, retaining the immutable closed loan in history. */
public class LoanReturnCommand extends Command {
    public static final String COMMAND_WORD = "loans return";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Records an equipment return. Parameters: uuid/UUID returned/DATE\nDates must use yyyy-MM-dd.";
    public static final String MESSAGE_SUCCESS = "Returned %1$s (%2$s) from %3$s (%4$s) on %5$s.";
    public static final String MESSAGE_EQUIPMENT_NOT_FOUND = "No equipment with UUID %1$s exists.";
    public static final String MESSAGE_NO_OPEN_LOAN = "Equipment %1$s has no open loan to return.";
    public static final String MESSAGE_MEMBER_NOT_FOUND =
            "Cannot return equipment %1$s: its open loan refers to a missing member.";
    public static final String MESSAGE_FUTURE_RETURN = "Returned date cannot be after today (%1$s).";

    private final UUID equipmentUuid;
    private final LocalDate returnedDate;
    private final Clock clock;

    /** Creates a command using the computer's local date at execution time. */
    public LoanReturnCommand(UUID equipmentUuid, LocalDate returnedDate) {
        this(equipmentUuid, returnedDate, Clock.systemDefaultZone());
    }

    /** Supplies a clock for deterministic command tests. */
    LoanReturnCommand(UUID equipmentUuid, LocalDate returnedDate, Clock clock) {
        requireAllNonNull(equipmentUuid, returnedDate, clock);
        this.equipmentUuid = equipmentUuid;
        this.returnedDate = returnedDate;
        this.clock = clock;
    }

    @Override
    public boolean isTransactional() {
        return true;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        try {
            Equipment equipment = model.findEquipment(equipmentUuid).orElseThrow(EquipmentNotFoundException::new);
            Loan loan = model.findOpenLoan(equipmentUuid).orElseThrow(OpenLoanNotFoundException::new);
            Member member = model.findMember(loan.getMemberNusId()).orElseThrow(MemberNotFoundException::new);
            LocalDate today = LocalDate.now(clock);
            if (returnedDate.isBefore(loan.getAssignedDate())) {
                throw new CommandException(Loan.MESSAGE_RETURNED_DATE_CONSTRAINTS);
            }
            if (returnedDate.isAfter(today)) {
                throw new CommandException(String.format(MESSAGE_FUTURE_RETURN, today));
            }
            model.closeLoan(equipmentUuid, returnedDate);
            return new CommandResult(String.format(MESSAGE_SUCCESS, equipment.getName(), equipmentUuid,
                    member.getName(), member.getNusId(), returnedDate), DisplayRequest.equipmentDetails(equipmentUuid));
        } catch (EquipmentNotFoundException e) {
            throw new CommandException(String.format(MESSAGE_EQUIPMENT_NOT_FOUND, equipmentUuid), e);
        } catch (OpenLoanNotFoundException e) {
            throw new CommandException(String.format(MESSAGE_NO_OPEN_LOAN, equipmentUuid), e);
        } catch (MemberNotFoundException e) {
            throw new CommandException(String.format(MESSAGE_MEMBER_NOT_FOUND, equipmentUuid), e);
        } catch (IllegalArgumentException e) {
            throw new CommandException("Could not return equipment " + equipmentUuid + ": " + e.getMessage(), e);
        }
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof LoanReturnCommand command
                && equipmentUuid.equals(command.equipmentUuid) && returnedDate.equals(command.returnedDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(equipmentUuid, returnedDate);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("equipmentUuid", equipmentUuid)
                .add("returnedDate", returnedDate).toString();
    }
}
