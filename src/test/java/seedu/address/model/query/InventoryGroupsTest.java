package seedu.address.model.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import seedu.address.model.ModelManager;
import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.Equipment;

/**
 * Verifies shared group identities without changing retained physical equipment.
 */
public class InventoryGroupsTest {
    @Test
    public void key_caseVariantsAndWhitespace_shareIdentityInBothFields() {
        InventoryGroups.Key expected = new InventoryGroups.Key("ΟΣ", "ΟΣ");
        for (String spelling : List.of("ΟΣ", "οσ", "ος", " \u2003ΟΣ\u00a0")) {
            assertEquals(expected, new InventoryGroups.Key(spelling, spelling));
        }
        assertNotEquals(expected, new InventoryGroups.Key("Audio", "ΟΣ"));
        assertNotEquals(expected, new InventoryGroups.Key("ΟΣ", "Camera"));
    }

    @Test
    public void groupAndSummarize_shareCaseKeysAndRetainDeletedUnits() {
        ModelManager model = new ModelManager();
        Equipment first = new Equipment(new UUID(0, 1), "İ", "ΟΣ", Condition.GOOD, "");
        Equipment second = new Equipment(new UUID(0, 2), "i", "οσ", Condition.DAMAGED, "");
        model.addEquipment(first);
        model.addEquipment(second);
        model.deleteEquipment(second);
        var units = List.copyOf(model.getEquipmentList());
        var groups = InventoryGroups.group(model.getAddressBook(), List.of(first));
        assertEquals(1, groups.size());
        assertEquals(units, groups.getFirst().units());
        assertEquals(List.of(first), groups.getFirst().matching());
        assertEquals(List.of(InventoryGroups.keyOf(first) + ": 1 available, 0 assigned, 0 unavailable"),
                InventoryGroups.summarize(model.getAddressBook(), model::getAvailability, unit -> true));
        assertEquals(units, model.getEquipmentList());
    }
}
