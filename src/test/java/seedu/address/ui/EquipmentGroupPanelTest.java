package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import seedu.address.model.ModelManager;
import seedu.address.model.equipment.Availability;
import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.query.DirectoryQuery;
import seedu.address.model.query.InventoryGroups;
import seedu.address.testutil.FxTestUtil;

class EquipmentGroupPanelTest {
    private static final Equipment CAMERA = new Equipment(new UUID(0, 1), "Camera", "Photo", Condition.GOOD, "");
    private static final Equipment DAMAGED = new Equipment(new UUID(0, 2), "camera", "PHOTO", Condition.DAMAGED, "");

    @Test
    void normalizedGroups_keepDistinctUnitsAndSeparateMatchingCounts() {
        ModelManager model = model();
        var groups = InventoryGroups.group(model.getAddressBook(), List.of(CAMERA));
        assertEquals(1, groups.size());
        assertEquals(List.of(CAMERA, DAMAGED), groups.getFirst().units());
        assertEquals(List.of(CAMERA), groups.getFirst().matching());
        assertEquals(InventoryGroups.keyOf(CAMERA), InventoryGroups.keyOf(DAMAGED));
    }

    @Test
    void exactNavigation_outsideFilterAndRegrouping_preservesUuidWithoutCallbacks() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = model();
            List<UUID> activations = new ArrayList<>();
            EquipmentGroupPanel panel = new EquipmentGroupPanel(model.getAddressBook(), model::getAvailability,
                    unused -> { }, unused -> { }, activations::add);
            new Scene(panel, 700, 600);
            panel.applyCss();
            DirectoryQuery available = new DirectoryQuery("", "", null, Availability.AVAILABLE,
                    DirectoryQuery.Holding.ANY, DirectoryQuery.Sort.DEFAULT, false, DirectoryQuery.Status.ACTIVE);
            panel.showGroup(InventoryGroups.keyOf(CAMERA), available);
            TableView<?> units = (TableView<?>) panel.lookup("#physicalUnits");
            assertEquals(List.of(CAMERA), units.getItems());
            assertEquals(null, units.getSelectionModel().getSelectedItem());
            panel.showUnit(DAMAGED.getUuid(), available);
            assertEquals(DAMAGED, units.getSelectionModel().getSelectedItem());
            assertEquals(2, units.getItems().size());
            Equipment renamed = new Equipment(DAMAGED.getUuid(), "Tripod", "Photo", Condition.DAMAGED, "");
            model.setEquipment(DAMAGED, renamed);
            panel.refresh();
            assertEquals(List.of(renamed), units.getItems());
            assertEquals(renamed, units.getSelectionModel().getSelectedItem());
            assertEquals("Tripod", ((Label) panel.lookup("#groupTitle")).getText());
            model.deleteEquipment(renamed);
            panel.refresh();
            assertTrue(((Equipment) units.getSelectionModel().getSelectedItem()).isDeleted());
            assertTrue(((Label) panel.lookup("#groupCounts")).getText().contains("0 active · 1 deleted"));
            model.restoreEquipment(renamed);
            panel.refresh();
            assertEquals(renamed, units.getSelectionModel().getSelectedItem());
            assertTrue(activations.isEmpty());
        });
    }

    private ModelManager model() {
        ModelManager model = new ModelManager();
        model.addEquipment(CAMERA);
        model.addEquipment(DAMAGED);
        return model;
    }
}
