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
import seedu.address.model.equipment.Availability;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.equipment.exceptions.EquipmentNotFoundException;
import seedu.address.model.loan.Loan;
import seedu.address.model.loan.exceptions.DuplicateOpenLoanException;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.model.member.exceptions.MemberNotFoundException;

/** Assigns available physical units to a member in one transaction. */
public class LoanAddCommand extends Command {
    public static final String COMMAND_WORD = "loans add";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Parameters: uuid/UUID[,UUID...] nus/NUS_ID assigned/DATE expected/DATE\n"
            + "Dates must use yyyy-MM-dd.";
    public static final String MESSAGE_SUCCESS = "Assigned %1$s (%2$s) to %3$s (%4$s) on %5$s; expected return: %6$s.";
    public static final String MESSAGE_EQUIPMENT_NOT_FOUND = "No equipment with UUID %1$s exists.";
    public static final String MESSAGE_MEMBER_NOT_FOUND = "No member with NUS ID %1$s exists.";
    public static final String MESSAGE_ASSIGNED = "Equipment %1$s is unavailable because it is assigned to %2$s.";
    public static final String MESSAGE_UNAVAILABLE = "Equipment %1$s is unavailable (condition: %2$s).";
    public static final String MESSAGE_FUTURE_ASSIGNMENT = "Assigned date cannot be after today (%1$s).";

    private final List<UUID> equipmentUuids;
    private final NusId memberNusId;
    private final LocalDate assignedDate;
    private final LocalDate expectedReturnDate;
    private final Clock clock;

    /** Creates an individual assignment. */
    public LoanAddCommand(UUID equipmentUuid, NusId memberNusId, LocalDate assignedDate, LocalDate expectedReturnDate) {
        this(List.of(requireNonNull(equipmentUuid)), memberNusId, assignedDate,
                expectedReturnDate, Clock.systemDefaultZone());
    }

    /** Supplies a clock for deterministic single-item tests. */
    LoanAddCommand(UUID equipmentUuid, NusId memberNusId, LocalDate assignedDate,
            LocalDate expectedReturnDate, Clock clock) {
        this(List.of(requireNonNull(equipmentUuid)), memberNusId, assignedDate, expectedReturnDate, clock);
    }

    /** Supplies a clock for deterministic batch tests. */
    LoanAddCommand(List<UUID> equipmentUuids, NusId memberNusId, LocalDate assignedDate,
            LocalDate expectedReturnDate, Clock clock) {
        requireAllNonNull(equipmentUuids, memberNusId, assignedDate, expectedReturnDate, clock);
        this.equipmentUuids = List.copyOf(equipmentUuids);
        if (this.equipmentUuids.isEmpty() || new HashSet<>(this.equipmentUuids).size() != this.equipmentUuids.size()) {
            throw new IllegalArgumentException("Assignment requires distinct equipment UUIDs");
        }
        this.memberNusId = memberNusId;
        this.assignedDate = assignedDate;
        this.expectedReturnDate = expectedReturnDate;
        this.clock = clock;
    }

    /** Creates a batch assignment. */
    public static LoanAddCommand batch(List<UUID> equipmentUuids, NusId memberNusId, LocalDate assignedDate,
            LocalDate expectedReturnDate) {
        return new LoanAddCommand(equipmentUuids, memberNusId, assignedDate,
                expectedReturnDate, Clock.systemDefaultZone());
    }

    @Override
    public boolean isTransactional() {
        return true;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Member member;
        try {
            member = model.findMember(memberNusId).orElseThrow(MemberNotFoundException::new);
        } catch (MemberNotFoundException exception) {
            throw new CommandException(String.format(MESSAGE_MEMBER_NOT_FOUND, memberNusId), exception);
        }
        LocalDate today = LocalDate.now(clock);
        if (assignedDate.isAfter(today)) {
            throw new CommandException(String.format(MESSAGE_FUTURE_ASSIGNMENT, today));
        }
        if (expectedReturnDate.isBefore(assignedDate)) {
            throw new CommandException(Loan.MESSAGE_EXPECTED_DATE_CONSTRAINTS);
        }
        List<Loan> newLoans = new ArrayList<>();
        List<String> names = new ArrayList<>();
        for (UUID uuid : equipmentUuids) {
            Equipment equipment;
            try {
                equipment = model.findEquipment(uuid).orElseThrow(EquipmentNotFoundException::new);
            } catch (EquipmentNotFoundException exception) {
                throw new CommandException(String.format(MESSAGE_EQUIPMENT_NOT_FOUND, uuid), exception);
            }
            Availability availability = model.getAvailability(uuid);
            if (availability == Availability.ASSIGNED) {
                String holder = model.findOpenLoan(uuid).map(loan -> loan.getMemberNusId().toString())
                        .orElse("an unknown member");
                throw new CommandException(String.format(MESSAGE_ASSIGNED, uuid, holder));
            }
            if (availability != Availability.AVAILABLE) {
                throw new CommandException(String.format(MESSAGE_UNAVAILABLE, uuid, equipment.getCondition()));
            }
            newLoans.add(new Loan(uuid, memberNusId, assignedDate, expectedReturnDate,
                    null, equipment.getCondition(), null));
            names.add(equipment.getName());
        }
        try {
            if (newLoans.size() == 1) {
                model.addLoan(newLoans.get(0));
            } else {
                model.addLoans(newLoans);
            }
        } catch (DuplicateOpenLoanException exception) {
            UUID uuid = equipmentUuids.get(0);
            String holder = model.findOpenLoan(uuid).map(loan -> loan.getMemberNusId().toString())
                    .orElse("an unknown member");
            throw new CommandException(String.format(MESSAGE_ASSIGNED, uuid, holder), exception);
        } catch (EquipmentNotFoundException | MemberNotFoundException exception) {
            throw new CommandException("Could not assign equipment: " + exception.getMessage(), exception);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw new CommandException("Could not assign equipment: " + exception.getMessage(), exception);
        }
        UUID first = equipmentUuids.get(0);
        String name = equipmentUuids.size() == 1 ? names.get(0) : equipmentUuids.size() + " units";
        String identifiers = equipmentUuids.size() == 1 ? first.toString() : equipmentUuids.toString();
        DisplayRequest display = equipmentUuids.size() == 1
                ? DisplayRequest.equipmentDetails(first) : DisplayRequest.loans();
        return new CommandResult(String.format(MESSAGE_SUCCESS, name, identifiers, member.getName(), memberNusId,
                assignedDate, expectedReturnDate), display);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof LoanAddCommand command
                && equipmentUuids.equals(command.equipmentUuids) && memberNusId.equals(command.memberNusId)
                && assignedDate.equals(command.assignedDate) && expectedReturnDate.equals(command.expectedReturnDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(equipmentUuids, memberNusId, assignedDate, expectedReturnDate);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("equipmentUuids", equipmentUuids).add("memberNusId", memberNusId)
                .add("assignedDate", assignedDate).add("expectedReturnDate", expectedReturnDate).toString();
    }
}
