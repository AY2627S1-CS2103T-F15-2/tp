package seedu.address.model.equipment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javafx.collections.ObservableList;

/**
 * Contract for the UUID-unique collection of equipment.
 */
public class UniqueEquipmentList {

    public boolean contains(UUID uuid) {
        throw new UnsupportedOperationException("UniqueEquipmentList is not implemented");
    }

    public Optional<Equipment> findByUuid(UUID uuid) {
        throw new UnsupportedOperationException("UniqueEquipmentList is not implemented");
    }

    public void add(Equipment equipment) {
        throw new UnsupportedOperationException("UniqueEquipmentList is not implemented");
    }

    public void remove(Equipment equipment) {
        throw new UnsupportedOperationException("UniqueEquipmentList is not implemented");
    }

    public void setEquipment(List<Equipment> equipment) {
        throw new UnsupportedOperationException("UniqueEquipmentList is not implemented");
    }

    public ObservableList<Equipment> asUnmodifiableObservableList() {
        throw new UnsupportedOperationException("UniqueEquipmentList is not implemented");
    }
}
