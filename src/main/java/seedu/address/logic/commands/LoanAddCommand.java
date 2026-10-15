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
import seedu.address.model.equipment.Availability;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.equipment.exceptions.EquipmentNotFoundException;
import seedu.address.model.loan.Loan;
import seedu.address.model.loan.exceptions.DuplicateOpenLoanException;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.model.member.exceptions.MemberNotFoundException;

/** Assigns available equipment to a member using execution-time identity, availability and date checks. */
public class LoanAddCommand extends Command {
    public static final String COMMAND_WORD = "loans add";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Assigns available equipment. Parameters: uuid/UUID nus/NUS_ID assigned/DATE expected/DATE\n"
            + "Dates must use uuuu-MM-dd.";
    public static final String MESSAGE_SUCCESS = "Assigned %1$s (%2$s) to %3$s (%4$s) on %5$s; expected return: %6$s.";
    public static final String MESSAGE_EQUIPMENT_NOT_FOUND = "No equipment with UUID %1$s exists.";
    public static final String MESSAGE_MEMBER_NOT_FOUND = "No member with NUS ID %1$s exists.";
    public static final String MESSAGE_ASSIGNED =
            "Equipment %1$s is unavailable because it is assigned to %2$s.";
    public static final String MESSAGE_UNAVAILABLE = "Equipment %1$s is unavailable (condition: %2$s).";
    public static final String MESSAGE_FUTURE_ASSIGNMENT = "Assigned date cannot be after today (%1$s).";

    private final UUID equipmentUuid;
    private final NusId memberNusId;
    private final LocalDate assignedDate;
    private final LocalDate expectedReturnDate;
    private final Clock clock;

    /** Creates a command using the computer's local date at execution time. */
    public LoanAddCommand(UUID equipmentUuid, NusId memberNusId, LocalDate assignedDate, LocalDate expectedReturnDate) {
        this(equipmentUuid, memberNusId, assignedDate, expectedReturnDate, Clock.systemDefaultZone());
    }

    /** Supplies a clock for deterministic command tests; construction performs no model or date lookup. */
    LoanAddCommand(UUID equipmentUuid, NusId memberNusId, LocalDate assignedDate,
            LocalDate expectedReturnDate, Clock clock) {
        requireAllNonNull(equipmentUuid, memberNusId, assignedDate, expectedReturnDate, clock);
        this.equipmentUuid = equipmentUuid;
        this.memberNusId = memberNusId;
        this.assignedDate = assignedDate;
        this.expectedReturnDate = expectedReturnDate;
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
            Member member = model.findMember(memberNusId).orElseThrow(MemberNotFoundException::new);
            String assignedMessage = model.findOpenLoan(equipmentUuid)
                    .map(loan -> String.format(MESSAGE_ASSIGNED, equipmentUuid, loan.getMemberNusId()))
                    .orElse("Equipment " + equipmentUuid + " has an inconsistent open-loan state.");
            Availability availability = model.getAvailability(equipmentUuid);
            if (availability == Availability.ASSIGNED) {
                throw new CommandException(assignedMessage);
            }
            if (availability != Availability.AVAILABLE) {
                throw new CommandException(String.format(MESSAGE_UNAVAILABLE, equipmentUuid, equipment.getCondition()));
            }
            LocalDate today = LocalDate.now(clock);
            if (assignedDate.isAfter(today)) {
                throw new CommandException(String.format(MESSAGE_FUTURE_ASSIGNMENT, today));
            }
            if (expectedReturnDate.isBefore(assignedDate)) {
                throw new CommandException(Loan.MESSAGE_EXPECTED_DATE_CONSTRAINTS);
            }
            try {
                model.addLoan(new Loan(equipmentUuid, memberNusId, assignedDate, expectedReturnDate, null));
            } catch (DuplicateOpenLoanException e) {
                throw new CommandException(assignedMessage, e);
            }
            return new CommandResult(String.format(MESSAGE_SUCCESS, equipment.getName(), equipmentUuid,
                    member.getName(), memberNusId, assignedDate, expectedReturnDate),
                    DisplayRequest.equipmentDetails(equipmentUuid));
        } catch (EquipmentNotFoundException e) {
            throw new CommandException(String.format(MESSAGE_EQUIPMENT_NOT_FOUND, equipmentUuid), e);
        } catch (MemberNotFoundException e) {
            throw new CommandException(String.format(MESSAGE_MEMBER_NOT_FOUND, memberNusId), e);
        } catch (IllegalArgumentException e) {
            throw new CommandException("Could not assign equipment " + equipmentUuid + ": " + e.getMessage(), e);
        }
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof LoanAddCommand command
                && equipmentUuid.equals(command.equipmentUuid) && memberNusId.equals(command.memberNusId)
                && assignedDate.equals(command.assignedDate) && expectedReturnDate.equals(command.expectedReturnDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(equipmentUuid, memberNusId, assignedDate, expectedReturnDate);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("equipmentUuid", equipmentUuid).add("memberNusId", memberNusId)
                .add("assignedDate", assignedDate).add("expectedReturnDate", expectedReturnDate).toString();
    }
}
