package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;
import seedu.address.model.ModelManager;
import seedu.address.model.equipment.Availability;
import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.equipment.exceptions.EquipmentNotFoundException;
import seedu.address.model.loan.Loan;
import seedu.address.model.query.DirectoryQuery;
import seedu.address.model.query.InventoryGroups;
import seedu.address.testutil.FxTestUtil;
import seedu.address.testutil.TypicalMembers;

class EquipmentGroupPanelTest {
    private static final Equipment CAMERA = new Equipment(new UUID(0, 1), "Camera", "Photo", Condition.GOOD, "");
    private static final Equipment DAMAGED = new Equipment(new UUID(0, 2), "camera", "PHOTO", Condition.DAMAGED, "");

    @Test
    void showUnit_identityChanges_resetVisibleScrollWhileRefreshPreservesPosition() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = model();
            EquipmentGroupPanel panel = new EquipmentGroupPanel(model.getAddressBook(), model::getAvailability,
                    unused -> { }, unused -> { }, unused -> { });
            mount(panel);
            panel.showUnit(CAMERA.getUuid(), DirectoryQuery.all());
            ScrollPane scroll = (ScrollPane) panel.getChildren().getFirst();
            scroll.setVvalue(1);
            panel.refresh();
            assertEquals(1, scroll.getVvalue());
            panel.showUnit(CAMERA.getUuid(), DirectoryQuery.all());
            assertEquals(1, scroll.getVvalue());
            panel.showUnit(DAMAGED.getUuid(), DirectoryQuery.all());
            assertEquals(0, scroll.getVvalue());
            assertEquals(DAMAGED.getUuid().toString(), ((Label) panel.lookup("#equipmentUuid")).getText());
            scroll.setVvalue(0.5);
            panel.showUnit(CAMERA.getUuid(), DirectoryQuery.all());
            assertEquals(0, scroll.getVvalue());
        });
    }

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
    void unitColumn_showsUuidPrefixAndSuffix() {
        UUID first = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        UUID second = UUID.fromString("a1b2c3d4-e5f6-47a8-9b0c-123455440000");
        ModelManager model = new ModelManager();
        model.addEquipment(new Equipment(first, "Camera", "Photo", Condition.GOOD, ""));
        model.addEquipment(new Equipment(second, "Camera", "Photo", Condition.GOOD, ""));
        FxTestUtil.runAndWait(() -> {
            EquipmentGroupPanel panel = new EquipmentGroupPanel(model.getAddressBook(), model::getAvailability,
                    unused -> { }, unused -> { }, unused -> { });
            mount(panel);
            panel.showGroup(InventoryGroups.keyOf(CAMERA), DirectoryQuery.all());
            TableView<?> units = (TableView<?>) panel.lookup("#physicalUnits");
            TableColumn<?, ?> identity = units.getColumns().getFirst();
            assertEquals("550e8400…0000", identity.getCellObservableValue(0).getValue());
            assertEquals("a1b2c3d4…0000", identity.getCellObservableValue(1).getValue());
        });
    }

    @Test
    void unitColumn_atNarrowWidths_keepsBothUuidEndsVisible() {
        UUID unitUuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        ModelManager model = new ModelManager();
        model.addEquipment(new Equipment(unitUuid, "Camera", "Photo", Condition.GOOD, ""));
        for (int width : List.of(500, 400)) {
            FxTestUtil.runAndWait(() -> {
                EquipmentGroupPanel panel = new EquipmentGroupPanel(model.getAddressBook(), model::getAvailability,
                        unused -> { }, unused -> { }, unused -> { });
                mount(panel, width);
                panel.showGroup(InventoryGroups.keyOf(CAMERA), DirectoryQuery.all());
                panel.applyCss();
                panel.layout();
                TableCell<?, ?> cell = panel.lookupAll(".table-cell").stream()
                        .map(TableCell.class::cast)
                        .filter(rendered -> "550e8400…0000".equals(rendered.getText()))
                        .findFirst().orElseThrow();
                Text renderedText = (Text) cell.lookup(".text");
                double contentWidth = cell.getWidth() - cell.getInsets().getLeft() - cell.getInsets().getRight();
                assertTrue(renderedText.getLayoutBounds().getWidth() <= contentWidth, () ->
                        "UUID clipped at panel width " + width + ": content=" + contentWidth
                                + ", text=" + renderedText.getLayoutBounds().getWidth()
                                + ", column=" + identityWidth(panel));
            });
        }
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
    void showUnit_removedTarget_doesNotSelectOrRevealAnUnrelatedRow() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = model();
            List<UUID> activations = new ArrayList<>();
            EquipmentGroupPanel panel = new EquipmentGroupPanel(model.getAddressBook(), model::getAvailability,
                    unused -> { }, unused -> { }, activations::add);
            mount(panel);
            panel.showUnit(CAMERA.getUuid(), DirectoryQuery.all());
            TableView<?> units = (TableView<?>) panel.lookup("#physicalUnits");
            assertEquals(CAMERA, units.getSelectionModel().getSelectedItem());
            // A queued navigation request can outlive the records it originally referred to.
            model.setAddressBook(new seedu.address.model.AddressBook());
            panel.showUnit(CAMERA.getUuid(), DirectoryQuery.all());
            assertTrue(units.getItems().isEmpty());
            assertEquals(-1, units.getSelectionModel().getSelectedIndex());
            assertEquals("Equipment group no longer exists", ((Label) panel.lookup("#groupTitle")).getText());
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

    @Test
    void refresh_unchangedUnits_updatesRenderedLoanCellsWithoutCallbacks() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = model();
            model.addMember(TypicalMembers.ALEX);
            List<UUID> activations = new ArrayList<>();
            EquipmentGroupPanel panel = new EquipmentGroupPanel(model.getAddressBook(), model::getAvailability,
                    unused -> { }, unused -> { }, activations::add);
            mount(panel);
            panel.showUnit(CAMERA.getUuid(), DirectoryQuery.all());
            assertTrue(renderedCells(panel).contains("Available"));
            LocalDate date = LocalDate.of(2026, 10, 1);
            model.addLoan(new Loan(CAMERA.getUuid(), TypicalMembers.ALEX.getNusId(), date, date, null));
            panel.refresh();
            assertTrue(renderedCells(panel).contains("Assigned"));
            assertTrue(renderedCells(panel).contains(TypicalMembers.ALEX.getName().fullName));
            model.closeLoan(CAMERA.getUuid(), date);
            panel.refresh();
            assertTrue(renderedCells(panel).contains("Available"));
            assertFalse(renderedCells(panel).contains("Assigned"));
            assertFalse(renderedCells(panel).contains(TypicalMembers.ALEX.getName().fullName));
            assertTrue(activations.isEmpty());
        });
    }

    /**
     * Returns rendered table text after layout so stale derived holder/availability cells are detected.
     */
    private static List<String> renderedCells(EquipmentGroupPanel panel) {
        panel.layout();
        return panel.lookupAll(".table-cell").stream().map(TableCell.class::cast)
                .filter(cell -> !cell.isEmpty()).map(TableCell::getText).toList();
    }

    @Test
    void unavailableLookup_displaysUnknownInsteadOfInventingAvailability() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = model();
            List<UUID> selected = new ArrayList<>();
            EquipmentGroupPanel panel = new EquipmentGroupPanel(model.getAddressBook(), uuid -> {
                throw new EquipmentNotFoundException();
            }, unused -> { }, unused -> { }, selected::add);
            mount(panel);
            panel.showGroup(InventoryGroups.keyOf(CAMERA), DirectoryQuery.all());
            TableView<?> units = (TableView<?>) panel.lookup("#physicalUnits");
            TableColumn<?, ?> availability = units.getColumns().stream()
                    .filter(column -> column.getText().equals("Availability")).findFirst().orElseThrow();
            assertEquals(2, units.getItems().size());
            for (int index = 0; index < units.getItems().size(); index++) {
                assertEquals("Unknown", availability.getCellObservableValue(index).getValue());
            }
            assertTrue(((Label) panel.lookup("#groupAvailability")).getText()
                    .contains("Availability could not be determined"));
            assertTrue(selected.isEmpty());
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
        mount(panel, 700);
    }

    private static void mount(EquipmentGroupPanel panel, int width) {
        panel.getStyleClass().add("laplace-root");
        Scene scene = new Scene(panel, width, 600);
        scene.getStylesheets().add(EquipmentGroupPanel.class.getResource("/view/LaplaceTheme.css").toExternalForm());
        panel.applyCss();
        panel.layout();
    }

    private static double identityWidth(EquipmentGroupPanel panel) {
        TableView<?> units = (TableView<?>) panel.lookup("#physicalUnits");
        return units.getColumns().getFirst().getWidth();
    }

    private ModelManager model() {
        ModelManager model = new ModelManager();
        model.addEquipment(CAMERA);
        model.addEquipment(DAMAGED);
        return model;
    }
}
