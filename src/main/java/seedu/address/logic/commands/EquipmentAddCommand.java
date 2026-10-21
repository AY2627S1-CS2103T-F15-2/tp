package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.equipment.exceptions.DuplicateEquipmentException;
import seedu.address.model.query.DirectoryQuery;
import seedu.address.model.query.DuplicateWarnings;

/**
 * Adds physical units with a separate generated identity for each unit.
 */
public class EquipmentAddCommand extends Command {

    public static final String COMMAND_WORD = "equipment add";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Adds equipment. Parameters: name/NAME category/CATEGORY condition/CONDITION"
            + " [notes/NOTES] [count/COUNT]\n"
            + "Example: equipment add name/Camera category/Photography condition/GOOD";
    public static final String MESSAGE_SUCCESS = "New equipment added: %1$s (%2$s), availability: %3$s";
    public static final String MESSAGE_COLLISION =
            "Equipment was not added because a unique identifier could not be generated. Try again.";

    public static final int MAX_COUNT = 100;
    public static final String MESSAGE_INVALID_COUNT = "Count must be a whole number from 1 to " + MAX_COUNT + ".";
    public static final String MESSAGE_BATCH_SUCCESS =
            "Added %1$d equipment units: %2$s, availability: %3$s.\nUUIDs: %4$s";

    static final int MAX_UUID_ATTEMPTS = 100;

    private final String name;
    private final String category;
    private final Condition condition;
    private final String notes;
    private final int count;
    private final Supplier<UUID> uuidSupplier;

    /**
     * Creates a command whose UUID is generated only when executed.
     */
    public EquipmentAddCommand(String name, String category, Condition condition, String notes) {
        this(name, category, condition, notes, 1);
    }

    /**
     * Creates a batch command with a count between one and {@link #MAX_COUNT}, inclusive.
     *
     * @throws IllegalArgumentException if the count is outside the supported range.
     */
    public EquipmentAddCommand(String name, String category, Condition condition, String notes, int count) {
        this(name, category, condition, notes, count, UUID::randomUUID);
    }

    /**
     * Supplies deterministic identity generation for single-unit command tests.
     */
    EquipmentAddCommand(String name, String category, Condition condition, String notes, Supplier<UUID> uuidSupplier) {
        this(name, category, condition, notes, 1, uuidSupplier);
    }

    /**
     * Supplies deterministic identity generation for batch command tests.
     */
    EquipmentAddCommand(String name, String category, Condition condition, String notes, int count,
            Supplier<UUID> uuidSupplier) {
        requireAllNonNull(name, category, condition, notes, uuidSupplier);
        if (count < 1 || count > MAX_COUNT) {
            throw new IllegalArgumentException(MESSAGE_INVALID_COUNT);
        }
        this.name = Equipment.normalizeName(name);
        this.category = Equipment.normalizeCategory(category);
        this.condition = condition;
        this.notes = Equipment.normalizeNotes(notes);
        this.count = count;
        this.uuidSupplier = uuidSupplier;
    }

    @Override
    public boolean isTransactional() {
        return true;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Equipment> units = prepareUnits(model);
        String warning = DuplicateWarnings.equipment(model.getAddressBook(), units.getFirst());
        try {
            for (Equipment unit : units) {
                model.addEquipment(unit);
            }
        } catch (DuplicateEquipmentException e) {
            throw new CommandException(MESSAGE_COLLISION, e);
        }
        UUID firstUuid = units.getFirst().getUuid();
        if (count == 1) {
            return new CommandResult(String.format(MESSAGE_SUCCESS, name, firstUuid,
                    model.getAvailability(firstUuid)) + warning, DisplayRequest.equipmentDetails(firstUuid));
        }
        String identities = units.stream().map(unit -> unit.getUuid().toString()).collect(Collectors.joining(", "));
        return new CommandResult(String.format(MESSAGE_BATCH_SUCCESS, count, name,
                model.getAvailability(firstUuid), identities) + warning,
                DisplayRequest.equipment(DirectoryQuery.all()));
    }

    /**
     * Prepares every unit before mutation, avoiding existing identities and identities reserved by this batch.
     */
    private List<Equipment> prepareUnits(Model model) throws CommandException {
        List<Equipment> units = new ArrayList<>();
        Set<UUID> reserved = new HashSet<>();
        for (int index = 0; index < count; index++) {
            UUID uuid = generateUniqueId(model, reserved);
            reserved.add(uuid);
            units.add(new Equipment(uuid, name, category, condition, notes));
        }
        return units;
    }

    /**
     * Generates one unused identity, with a bounded retry budget for each physical unit.
     */
    private UUID generateUniqueId(Model model, Set<UUID> reserved) throws CommandException {
        for (int attempt = 0; attempt < MAX_UUID_ATTEMPTS; attempt++) {
            UUID uuid = uuidSupplier.get();
            if (uuid == null) {
                throw new CommandException(MESSAGE_COLLISION);
            }
            if (!model.hasEquipment(uuid) && !reserved.contains(uuid)) {
                return uuid;
            }
        }
        throw new CommandException(MESSAGE_COLLISION);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof EquipmentAddCommand command
                && name.equals(command.name) && category.equals(command.category)
                && condition == command.condition && notes.equals(command.notes) && count == command.count;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, category, condition, notes, count);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("name", name).add("category", category)
                .add("condition", condition).add("notes", notes).add("count", count).toString();
    }
}
