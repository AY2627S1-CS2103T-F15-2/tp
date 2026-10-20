package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.equipment.exceptions.DuplicateEquipmentException;
import seedu.address.model.query.DuplicateWarnings;

/** Adds a physical item with a newly generated identity. */
public class EquipmentAddCommand extends Command {

    public static final String COMMAND_WORD = "equipment add";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Adds equipment. Parameters: name/NAME category/CATEGORY condition/CONDITION [notes/NOTES]\n"
            + "Example: equipment add name/Camera category/Photography condition/GOOD";
    public static final String MESSAGE_SUCCESS = "New equipment added: %1$s (%2$s), availability: %3$s";
    public static final String MESSAGE_COLLISION =
            "Equipment was not added because a unique identifier could not be generated. Try again.";

    static final int MAX_UUID_ATTEMPTS = 100;

    private final String name;
    private final String category;
    private final Condition condition;
    private final String notes;
    private final Supplier<UUID> uuidSupplier;

    /** Creates a command whose UUID is generated only when executed. */
    public EquipmentAddCommand(String name, String category, Condition condition, String notes) {
        this(name, category, condition, notes, UUID::randomUUID);
    }

    /** Supplies deterministic identity generation for command tests. */
    EquipmentAddCommand(String name, String category, Condition condition, String notes, Supplier<UUID> uuidSupplier) {
        requireAllNonNull(name, category, condition, notes, uuidSupplier);
        this.name = Equipment.normalizeName(name);
        this.category = Equipment.normalizeCategory(category);
        this.condition = condition;
        this.notes = Equipment.normalizeNotes(notes);
        this.uuidSupplier = uuidSupplier;
    }

    @Override
    public boolean isTransactional() {
        return true;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        DuplicateEquipmentException lastCollision = null;
        for (int attempt = 0; attempt < MAX_UUID_ATTEMPTS; attempt++) {
            UUID uuid = uuidSupplier.get();
            if (uuid == null) {
                throw new CommandException(MESSAGE_COLLISION);
            }
            if (model.hasEquipment(uuid)) {
                continue;
            }
            Equipment equipment = new Equipment(uuid, name, category, condition, notes);
            String warning = DuplicateWarnings.equipment(model.getAddressBook(), equipment);
            try {
                model.addEquipment(equipment);
            } catch (DuplicateEquipmentException e) {
                lastCollision = e;
                continue;
            }
            return new CommandResult(String.format(MESSAGE_SUCCESS, name, uuid, model.getAvailability(uuid)) + warning,
                    DisplayRequest.equipmentDetails(uuid));
        }
        if (lastCollision != null) {
            throw new CommandException(MESSAGE_COLLISION, lastCollision);
        }
        throw new CommandException(MESSAGE_COLLISION);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof EquipmentAddCommand command
                && name.equals(command.name) && category.equals(command.category)
                && condition == command.condition && notes.equals(command.notes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, category, condition, notes);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("name", name).add("category", category)
                .add("condition", condition).add("notes", notes).toString();
    }
}
