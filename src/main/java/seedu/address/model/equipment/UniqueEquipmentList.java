package seedu.address.model.equipment;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.equipment.exceptions.DuplicateEquipmentException;
import seedu.address.model.equipment.exceptions.EquipmentNotFoundException;

/**
 * A list of equipment that enforces uniqueness by UUID and does not allow nulls.
 * Identity is the equipment UUID, so two items may share a name. Adding, removal, and bulk replacement use that
 * identity. Full-value equality remains {@link Equipment#equals(Object)}.
 */
public class UniqueEquipmentList {

    private final ObservableList<Equipment> internalList = FXCollections.observableArrayList();
    private final ObservableList<Equipment> internalUnmodifiableList =
            FXCollections.unmodifiableObservableList(internalList);

    /**
     * Returns true if the list contains equipment with {@code uuid}.
     */
    public boolean contains(UUID uuid) {
        requireNonNull(uuid);
        return indexOf(uuid).isPresent();
    }

    /**
     * Returns the equipment with {@code uuid}, if present.
     */
    public Optional<Equipment> findByUuid(UUID uuid) {
        requireNonNull(uuid);
        return indexOf(uuid).map(internalList::get);
    }

    /**
     * Adds {@code toAdd} to the list.
     * The equipment UUID must not already exist in the list.
     */
    public void add(Equipment toAdd) {
        requireNonNull(toAdd);
        if (contains(toAdd.getUuid())) {
            throw new DuplicateEquipmentException();
        }
        internalList.add(toAdd);
    }

    /**
     * Removes the equipment with the same UUID as {@code toRemove}.
     * Other fields may differ. The equipment must exist in the list.
     */
    public void remove(Equipment toRemove) {
        requireNonNull(toRemove);
        int index = indexOf(toRemove.getUuid()).orElseThrow(EquipmentNotFoundException::new);
        internalList.remove(index);
    }

    /**
     * Replaces the contents of this list with {@code equipment}.
     * {@code equipment} must not contain duplicate UUIDs. The supplied list is copied and is not retained.
     * A rejected replacement leaves this list unchanged.
     */
    public void setEquipment(List<Equipment> equipment) {
        requireAllNonNull(equipment);
        List<Equipment> copiedEquipment = new ArrayList<>(equipment);
        if (!hasUniqueUuids(copiedEquipment)) {
            throw new DuplicateEquipmentException();
        }
        internalList.setAll(copiedEquipment);
    }

    /**
     * Returns the backing list as an unmodifiable {@code ObservableList}.
     */
    public ObservableList<Equipment> asUnmodifiableObservableList() {
        return internalUnmodifiableList;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof UniqueEquipmentList otherEquipmentList)) {
            return false;
        }

        return internalList.equals(otherEquipmentList.internalList);
    }

    @Override
    public int hashCode() {
        return internalList.hashCode();
    }

    @Override
    public String toString() {
        return internalList.toString();
    }

    private Optional<Integer> indexOf(UUID uuid) {
        for (int i = 0; i < internalList.size(); i++) {
            if (internalList.get(i).getUuid().equals(uuid)) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }

    /**
     * Returns true if {@code equipment} contains only unique UUIDs.
     */
    private boolean hasUniqueUuids(List<Equipment> equipment) {
        Set<UUID> seenUuids = new HashSet<>();
        for (Equipment item : equipment) {
            if (!seenUuids.add(item.getUuid())) {
                return false;
            }
        }
        return true;
    }
}
