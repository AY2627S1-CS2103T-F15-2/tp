package seedu.address.model.equipment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.equipment.EquipmentTestData.CAMERA;
import static seedu.address.model.equipment.EquipmentTestData.CAMERA_UUID;
import static seedu.address.model.equipment.EquipmentTestData.SAME_NAME_DIFFERENT_UUID;
import static seedu.address.model.equipment.EquipmentTestData.SAME_UUID_DIFFERENT_DETAILS;
import static seedu.address.model.equipment.EquipmentTestData.SECOND_CAMERA_UUID;
import static seedu.address.model.equipment.EquipmentTestData.TRIPOD;
import static seedu.address.model.equipment.EquipmentTestData.TRIPOD_UUID;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import seedu.address.model.equipment.exceptions.DuplicateEquipmentException;
import seedu.address.model.equipment.exceptions.EquipmentNotFoundException;

public class UniqueEquipmentListTest {

    @Test
    public void contains_nullUuid_throwsNullPointerException() {
        UniqueEquipmentList equipmentList = new UniqueEquipmentList();
        assertThrows(NullPointerException.class, () -> equipmentList.contains(null));
    }

    @Test
    public void contains_missingUuid_returnsFalse() {
        UniqueEquipmentList equipmentList = new UniqueEquipmentList();
        equipmentList.add(CAMERA);

        assertFalse(equipmentList.contains(TRIPOD_UUID));
    }

    @Test
    public void contains_presentUuid_returnsTrue() {
        UniqueEquipmentList equipmentList = new UniqueEquipmentList();
        equipmentList.add(CAMERA);

        assertTrue(equipmentList.contains(CAMERA_UUID));
    }

    @Test
    public void findByUuid_nullUuid_throwsNullPointerException() {
        UniqueEquipmentList equipmentList = new UniqueEquipmentList();
        assertThrows(NullPointerException.class, () -> equipmentList.findByUuid(null));
    }

    @Test
    public void findByUuid_missingUuid_returnsEmpty() {
        UniqueEquipmentList equipmentList = new UniqueEquipmentList();
        equipmentList.add(CAMERA);

        assertEquals(Optional.empty(), equipmentList.findByUuid(TRIPOD_UUID));
    }

    @Test
    public void findByUuid_presentUuid_returnsStoredEquipment() {
        UniqueEquipmentList equipmentList = new UniqueEquipmentList();
        equipmentList.add(CAMERA);

        assertEquals(Optional.of(CAMERA), equipmentList.findByUuid(CAMERA_UUID));
    }

    @Test
    public void add_nullEquipment_throwsNullPointerException() {
        UniqueEquipmentList equipmentList = new UniqueEquipmentList();
        assertThrows(NullPointerException.class, () -> equipmentList.add(null));
    }

    @Test
    public void add_sameNameDifferentUuid_acceptsBoth() {
        UniqueEquipmentList equipmentList = new UniqueEquipmentList();
        equipmentList.add(CAMERA);
        equipmentList.add(SAME_NAME_DIFFERENT_UUID);

        assertEquals(List.of(CAMERA, SAME_NAME_DIFFERENT_UUID), equipmentList.asUnmodifiableObservableList());
        assertTrue(equipmentList.contains(CAMERA_UUID));
        assertTrue(equipmentList.contains(SECOND_CAMERA_UUID));
    }

    @Test
    public void add_duplicateUuid_throwsDuplicateEquipmentExceptionAndLeavesListUnchanged() {
        UniqueEquipmentList equipmentList = new UniqueEquipmentList();
        equipmentList.add(CAMERA);

        assertThrows(DuplicateEquipmentException.class, () -> equipmentList.add(SAME_UUID_DIFFERENT_DETAILS));
        assertEquals(List.of(CAMERA), equipmentList.asUnmodifiableObservableList());
    }

    @Test
    public void remove_nullEquipment_throwsNullPointerException() {
        UniqueEquipmentList equipmentList = new UniqueEquipmentList();
        assertThrows(NullPointerException.class, () -> equipmentList.remove(null));
    }

    @Test
    public void remove_missingEquipment_throwsEquipmentNotFoundExceptionAndLeavesListUnchanged() {
        UniqueEquipmentList equipmentList = new UniqueEquipmentList();
        equipmentList.add(CAMERA);

        assertThrows(EquipmentNotFoundException.class, () -> equipmentList.remove(TRIPOD));
        assertEquals(List.of(CAMERA), equipmentList.asUnmodifiableObservableList());
    }

    @Test
    public void remove_sameUuidDifferentDetails_removesStoredEquipment() {
        UniqueEquipmentList equipmentList = new UniqueEquipmentList();
        equipmentList.add(CAMERA);
        equipmentList.add(TRIPOD);

        equipmentList.remove(SAME_UUID_DIFFERENT_DETAILS);

        assertEquals(List.of(TRIPOD), equipmentList.asUnmodifiableObservableList());
        assertFalse(equipmentList.contains(CAMERA_UUID));
    }

    @Test
    public void setEquipment_nullList_throwsNullPointerException() {
        UniqueEquipmentList equipmentList = new UniqueEquipmentList();
        assertThrows(NullPointerException.class, () -> equipmentList.setEquipment(null));
    }

    @Test
    public void setEquipment_nullElement_throwsNullPointerExceptionAndLeavesListUnchanged() {
        UniqueEquipmentList equipmentList = new UniqueEquipmentList();
        equipmentList.add(CAMERA);
        List<Equipment> replacement = new ArrayList<>();
        replacement.add(TRIPOD);
        replacement.add(null);

        assertThrows(NullPointerException.class, () -> equipmentList.setEquipment(replacement));
        assertEquals(List.of(CAMERA), equipmentList.asUnmodifiableObservableList());
    }

    @Test
    public void setEquipment_duplicateUuid_throwsDuplicateEquipmentExceptionAndLeavesListUnchanged() {
        UniqueEquipmentList equipmentList = new UniqueEquipmentList();
        equipmentList.add(CAMERA);
        List<Equipment> replacement = List.of(TRIPOD, SAME_UUID_DIFFERENT_DETAILS, TRIPOD);

        assertThrows(DuplicateEquipmentException.class, () -> equipmentList.setEquipment(replacement));
        assertEquals(List.of(CAMERA), equipmentList.asUnmodifiableObservableList());
    }

    @Test
    public void setEquipment_validReplacement_replacesExistingEquipment() {
        UniqueEquipmentList equipmentList = new UniqueEquipmentList();
        equipmentList.add(CAMERA);

        equipmentList.setEquipment(List.of(TRIPOD, SAME_NAME_DIFFERENT_UUID));

        assertEquals(List.of(TRIPOD, SAME_NAME_DIFFERENT_UUID), equipmentList.asUnmodifiableObservableList());
    }

    @Test
    public void setEquipment_emptyReplacement_clearsExistingEquipment() {
        UniqueEquipmentList equipmentList = new UniqueEquipmentList();
        equipmentList.add(CAMERA);

        equipmentList.setEquipment(List.of());

        assertEquals(List.of(), equipmentList.asUnmodifiableObservableList());
    }

    @Test
    public void setEquipment_callerMutatesSuppliedList_internalListStaysUnchanged() {
        UniqueEquipmentList equipmentList = new UniqueEquipmentList();
        List<Equipment> callerOwned = new ArrayList<>();
        callerOwned.add(CAMERA);

        equipmentList.setEquipment(callerOwned);
        callerOwned.clear();
        callerOwned.add(TRIPOD);

        assertEquals(List.of(CAMERA), equipmentList.asUnmodifiableObservableList());
    }

    @Test
    public void asUnmodifiableObservableList_callerMutation_throwsUnsupportedOperationException() {
        UniqueEquipmentList equipmentList = new UniqueEquipmentList();
        equipmentList.add(CAMERA);
        ObservableList<Equipment> view = equipmentList.asUnmodifiableObservableList();

        assertThrows(UnsupportedOperationException.class, () -> view.add(TRIPOD));
        assertThrows(UnsupportedOperationException.class, () -> view.remove(0));
        assertEquals(List.of(CAMERA), view);
    }

    @Test
    public void asUnmodifiableObservableList_internalAdd_notifiesListeners() {
        UniqueEquipmentList equipmentList = new UniqueEquipmentList();
        ObservableList<Equipment> view = equipmentList.asUnmodifiableObservableList();
        List<Equipment> added = new ArrayList<>();
        view.addListener((ListChangeListener<Equipment>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    added.addAll(change.getAddedSubList());
                }
            }
        });

        equipmentList.add(CAMERA);

        assertEquals(List.of(CAMERA), added);
        assertEquals(List.of(CAMERA), view);
    }

    @Test
    public void equals_sameEquipmentInSameOrder_returnsTrue() {
        UniqueEquipmentList first = new UniqueEquipmentList();
        UniqueEquipmentList second = new UniqueEquipmentList();
        first.add(CAMERA);
        second.add(CAMERA);

        assertTrue(first.equals(second));
        assertTrue(first.equals(first));
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void equals_differentContents_returnsFalse() {
        UniqueEquipmentList first = new UniqueEquipmentList();
        UniqueEquipmentList second = new UniqueEquipmentList();
        first.add(CAMERA);
        second.add(TRIPOD);

        assertFalse(first.equals(second));
        assertFalse(first.equals(null));
        assertFalse(first.equals("equipment"));
    }

    @Test
    public void toStringMethod() {
        UniqueEquipmentList equipmentList = new UniqueEquipmentList();
        equipmentList.add(CAMERA);

        assertEquals(equipmentList.asUnmodifiableObservableList().toString(), equipmentList.toString());
    }

    @Test
    public void exceptionMessages_followContract() {
        assertEquals("Operation would result in duplicate equipment",
                new DuplicateEquipmentException().getMessage());
        assertNull(new EquipmentNotFoundException().getMessage());
    }
}
