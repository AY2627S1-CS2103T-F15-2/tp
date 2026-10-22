package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.Equipment;

class AddressBookHistoryTest {
    @Test
    void emptyHistory_rejectsNavigationAndHasNoSelectedState() {
        AddressBookHistory history = new AddressBookHistory();
        assertFalse(history.canUndo());
        assertFalse(history.canRedo());
        assertThrows(IllegalStateException.class, history::undo);
        assertThrows(IllegalStateException.class, history::redo);
        assertThrows(IllegalStateException.class, history::getCurrentState);
        assertThrows(IllegalStateException.class, history::getUndoDescription);
        assertThrows(IllegalStateException.class, history::getRedoDescription);
    }

    @Test
    void record_moreThanTenChanges_retainsExactlyTenUndoAndRedoSteps() {
        AddressBookHistory history = new AddressBookHistory();
        AddressBook data = new AddressBook();
        for (int index = 1; index <= 12; index++) {
            AddressBook before = new AddressBook(data);
            data.addEquipment(unit(index));
            history = history.record(before, data, "change " + index);
        }
        for (int index = 12; index > 2; index--) {
            assertEquals("change " + index, history.getUndoDescription());
            history = history.undo();
            assertEquals(index - 1, history.getCurrentState().getEquipmentList().size());
        }
        assertFalse(history.canUndo());
        for (int index = 3; index <= 12; index++) {
            assertEquals("change " + index, history.getRedoDescription());
            history = history.redo();
            assertEquals(index, history.getCurrentState().getEquipmentList().size());
        }
        assertFalse(history.canRedo());
    }

    @Test
    void branching_discardsRedoWithoutAffectingOriginalHistory() {
        AddressBook empty = new AddressBook();
        AddressBook first = new AddressBook();
        first.addEquipment(unit(1));
        AddressBook second = new AddressBook(first);
        second.addEquipment(unit(2));
        AddressBookHistory original = new AddressBookHistory().record(empty, first, "first")
                .record(first, second, "second");
        AddressBookHistory undone = original.undo();
        assertSame(undone, undone.record(first, first, "no change"));
        AddressBook branch = new AddressBook(first);
        branch.addEquipment(unit(3));
        AddressBookHistory branched = undone.record(first, branch, "branch");
        assertFalse(branched.canRedo());
        assertEquals(first, branched.undo().getCurrentState());
        assertEquals(branch, branched.getCurrentState());
        assertEquals(second, original.getCurrentState());
        assertEquals(second, undone.redo().getCurrentState());
    }

    @Test
    void snapshots_doNotShareMutableCollectionsWithInputsOrOutputs() {
        AddressBook empty = new AddressBook();
        AddressBook current = new AddressBook();
        current.addEquipment(unit(1));
        AddressBookHistory history = new AddressBookHistory().record(empty, current, "add");
        current.addEquipment(unit(2));
        empty.addEquipment(unit(3));
        AddressBook returned = (AddressBook) history.getCurrentState();
        returned.addEquipment(unit(4));
        assertEquals(1, history.getCurrentState().getEquipmentList().size());
        assertTrue(history.undo().getCurrentState().getEquipmentList().isEmpty());
        assertNotSame(returned, history.getCurrentState());
    }

    @Test
    void record_rejectsNullsAndAnUnrelatedPreviousState() {
        AddressBook empty = new AddressBook();
        AddressBook current = new AddressBook();
        current.addEquipment(unit(1));
        AddressBookHistory history = new AddressBookHistory().record(empty, current, "add");
        assertThrows(NullPointerException.class, () -> history.record(null, current, "add"));
        assertThrows(NullPointerException.class, () -> history.record(empty, null, "add"));
        assertThrows(NullPointerException.class, () -> history.record(empty, current, null));
        assertThrows(IllegalStateException.class, () -> history.record(empty, current, "unrelated"));
    }

    private Equipment unit(int identity) {
        return new Equipment(new UUID(0, identity), "Camera", "Photo", Condition.GOOD, "");
    }
}
