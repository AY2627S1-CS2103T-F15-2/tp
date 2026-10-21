package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import seedu.address.model.ModelManager;
import seedu.address.model.equipment.Availability;
import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.loan.Loan;
import seedu.address.model.query.DirectoryQuery;
import seedu.address.model.query.InventoryGroups;
import seedu.address.testutil.FxTestUtil;
import seedu.address.testutil.TypicalMembers;

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
            mount(panel);
            DirectoryQuery available = new DirectoryQuery("", "", null, Availability.AVAILABLE,
                    DirectoryQuery.Holding.ANY, DirectoryQuery.Sort.DEFAULT, false, DirectoryQuery.Status.ACTIVE);
            panel.showGroup(InventoryGroups.keyOf(CAMERA), available);
            TableView<?> units = (TableView<?>) panel.lookup("#physicalUnits");
            assertEquals(List.of(CAMERA), units.getItems());
            assertEquals(null, units.getSelectionModel().getSelectedItem());
            panel.showUnit(DAMAGED.getUuid(), available);
            assertEquals(DAMAGED, units.getSelectionModel().getSelectedItem());
            assertEquals(2, units.getItems().size());
            verifySortedRefresh(panel);
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

    @Test
    void exactUnitOutsideFilter_canReturnToMatchingRowsWithoutLosingDetails() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = model();
            List<UUID> activations = new ArrayList<>();
            EquipmentGroupPanel panel = new EquipmentGroupPanel(model.getAddressBook(), model::getAvailability,
                    unused -> { }, unused -> { }, activations::add);
            mount(panel);
            DirectoryQuery available = new DirectoryQuery("", "", null, Availability.AVAILABLE,
                    DirectoryQuery.Holding.ANY, DirectoryQuery.Sort.DEFAULT, false, DirectoryQuery.Status.ACTIVE);
            panel.showUnit(DAMAGED.getUuid(), available);
            CheckBox all = (CheckBox) panel.lookup("#showAllUnits");
            assertTrue(all.isSelected());
            all.fire();
            panel.refresh();
            TableView<?> units = (TableView<?>) panel.lookup("#physicalUnits");
            assertEquals(false, all.isSelected());
            assertEquals(List.of(CAMERA), units.getItems());
            assertEquals(null, units.getSelectionModel().getSelectedItem());
            assertEquals(DAMAGED.getUuid().toString(), ((Label) panel.lookup("#equipmentUuid")).getText());
            assertTrue(((Label) panel.lookup("#groupCounts")).getText().contains("outside the table filter"));
            assertTrue(activations.isEmpty());
            all.fire();
            assertEquals(DAMAGED, units.getSelectionModel().getSelectedItem());
        });
    }

    @Test
    void mixedUnits_countsRespectFilterLoansAndDeletedLifecycle() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = model();
            model.addMember(TypicalMembers.ALEX);
            Equipment spare = new Equipment(new UUID(0, 3), "Camera", "Photo", Condition.GOOD, "");
            model.addEquipment(spare);
            LocalDate assigned = LocalDate.of(2026, 10, 1);
            model.addLoan(new Loan(CAMERA.getUuid(), TypicalMembers.ALEX.getNusId(), assigned, assigned, null));
            EquipmentGroupPanel panel = new EquipmentGroupPanel(model.getAddressBook(), model::getAvailability,
                    unused -> { }, unused -> { }, unused -> { });
            mount(panel);
            DirectoryQuery available = new DirectoryQuery("", "", null, Availability.AVAILABLE,
                    DirectoryQuery.Holding.ANY, DirectoryQuery.Sort.DEFAULT, false);
            panel.showGroup(InventoryGroups.keyOf(CAMERA), available);
            Label counts = (Label) panel.lookup("#groupAvailability");
            assertEquals("Matching units: 1 available · 0 assigned · 0 unavailable", counts.getText());
            CheckBox all = (CheckBox) panel.lookup("#showAllUnits");
            all.fire();
            assertEquals("All units: 1 available · 1 assigned · 1 unavailable", counts.getText());
            model.deleteEquipment(spare);
            panel.refresh();
            assertEquals("All units: 0 available · 1 assigned · 1 unavailable · 1 deleted", counts.getText());
            model.closeLoan(CAMERA.getUuid(), assigned);
            panel.refresh();
            assertEquals("All units: 1 available · 0 assigned · 1 unavailable · 1 deleted", counts.getText());
            all.fire();
            assertEquals("Matching units: 1 available · 0 assigned · 0 unavailable", counts.getText());
            model.setAddressBook(new seedu.address.model.AddressBook());
            panel.refresh();
            assertEquals("Equipment group no longer exists", ((Label) panel.lookup("#groupTitle")).getText());
            assertTrue(((TableView<?>) panel.lookup("#physicalUnits")).getItems().isEmpty());
        });
    }

    @SuppressWarnings("unchecked")
    private static void verifySortedRefresh(EquipmentGroupPanel panel) {
        TableView<Equipment> units = (TableView<Equipment>) panel.lookup("#physicalUnits");
        TableColumn<Equipment, ?> condition = units.getColumns().get(1);
        units.getSortOrder().add(condition);
        panel.refresh();
        assertEquals(List.of(DAMAGED, CAMERA), units.getItems());
        assertEquals(DAMAGED, units.getSelectionModel().getSelectedItem());
    }

    private static void mount(EquipmentGroupPanel panel) {
        panel.getStyleClass().add("laplace-root");
        Scene scene = new Scene(panel, 700, 600);
        scene.getStylesheets().add(EquipmentGroupPanel.class.getResource("/view/LaplaceTheme.css").toExternalForm());
        panel.applyCss();
    }

    private ModelManager model() {
        ModelManager model = new ModelManager();
        model.addEquipment(CAMERA);
        model.addEquipment(DAMAGED);
        return model;
    }
}
