package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.equipment.Availability;
import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.equipment.exceptions.EquipmentNotFoundException;
import seedu.address.model.loan.Loan;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.query.DirectoryQuery;
import seedu.address.model.query.InventoryGroups;
import seedu.address.testutil.FxTestUtil;

public class DirectoryListPanelTest {

    @Test
    public void members_sortAndProgrammaticSelection_preservesRootOrderAndDoesNotActivate() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = model();
            AddressBook before = new AddressBook(model.getAddressBook());
            List<NusId> callbacks = new ArrayList<>();
            MemberListPanel panel = new MemberListPanel(model.getAddressBook(), callbacks::add);
            mount(panel.getRoot());
            assertEquals(List.of("A0000001X", "A0000002X", "A0000003X"), rows(panel).getItems().stream()
                    .map(item -> ((Member) item).getNusId().value).toList());
            panel.select(Optional.of(new NusId("a0000001x")));
            assertEquals(Optional.of(new NusId("A0000001X")), panel.getSelection());
            panel.refresh();
            assertEquals(Optional.of(new NusId("A0000001X")), panel.getSelection());
            panel.select(Optional.of(new NusId("A9999999X")));
            assertTrue(panel.getSelection().isEmpty());
            panel.select(Optional.empty());
            assertTrue(callbacks.isEmpty());
            assertEquals(before, model.getAddressBook());
            assertEquals("3 active", label(panel.getRoot(), "countLabel"));
        });
    }

    @Test
    public void equipment_sortAndProgrammaticSelection_preservesEveryUuid() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = model();
            AddressBook before = new AddressBook(model.getAddressBook());
            List<InventoryGroups.Key> callbacks = new ArrayList<>();
            EquipmentListPanel panel = new EquipmentListPanel(model.getAddressBook(), model::getAvailability,
                    callbacks::add);
            mount(panel.getRoot());
            assertEquals(List.of(new UUID(0, 1), new UUID(0, 2), new UUID(0, 3)), rows(panel).getItems().stream()
                    .flatMap(item -> ((InventoryGroups.Group) item).matching().stream())
                    .map(Equipment::getUuid).toList());
            panel.select(Optional.of(new UUID(0, 2)));
            panel.refresh();
            assertEquals(Optional.of(new UUID(0, 2)), panel.getSelection());
            panel.select(Optional.of(new UUID(0, 99)));
            assertTrue(panel.getSelection().isEmpty());
            assertTrue(callbacks.isEmpty());
            assertEquals(before, model.getAddressBook());
            assertEquals("1 groups · 3 matching units / 3 retained", label(panel.getRoot(), "countLabel"));
        });
    }

    @Test
    public void equipment_editSelectedUnit_followsCurrentNameAndCategoryWithoutActivating() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = model();
            List<InventoryGroups.Key> callbacks = new ArrayList<>();
            EquipmentListPanel panel = new EquipmentListPanel(model.getAddressBook(), model::getAvailability,
                    callbacks::add);
            UUID uuid = new UUID(0, 1);
            panel.select(Optional.of(uuid));
            for (Equipment edited : List.of(
                    new Equipment(uuid, "Tripod", "Photography", Condition.GOOD, ""),
                    new Equipment(uuid, "Tripod", "Accessories", Condition.GOOD, ""))) {
                model.setEquipment(model.findEquipment(uuid).orElseThrow(), edited);
                panel.refresh();
                panel.select(Optional.of(uuid));
                assertEquals(Optional.of(uuid), panel.getSelection());
                assertEquals(Optional.of(InventoryGroups.keyOf(edited)), panel.getSelectedGroup());
                assertEquals(2, rows(panel).getItems().size());
                assertTrue(callbacks.isEmpty());
            }
        });
    }

    @Test
    public void equipment_editOutsideFilter_clearsOldGroupAndRetainsUnitUntilFilterReset() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = model();
            List<InventoryGroups.Key> callbacks = new ArrayList<>();
            EquipmentListPanel panel = new EquipmentListPanel(model.getAddressBook(), model::getAvailability,
                    callbacks::add);
            panel.setQuery(new DirectoryQuery("", "Photography", null, null,
                    DirectoryQuery.Holding.ANY, DirectoryQuery.Sort.DEFAULT, false));
            panel.refresh();
            UUID uuid = new UUID(0, 1);
            panel.select(Optional.of(uuid));
            Equipment edited = new Equipment(uuid, "Camera", "Accessories", Condition.GOOD, "");
            model.setEquipment(model.findEquipment(uuid).orElseThrow(), edited);
            panel.refresh();
            panel.select(Optional.of(uuid));
            assertEquals(Optional.of(uuid), panel.getSelection());
            assertTrue(panel.getSelectedGroup().isEmpty());
            assertEquals(1, rows(panel).getItems().size());
            panel.setQuery(DirectoryQuery.all());
            panel.refresh();
            panel.select(Optional.of(uuid));
            assertEquals(Optional.of(InventoryGroups.keyOf(edited)), panel.getSelectedGroup());
            assertEquals(Optional.of(uuid), panel.getSelection());
            assertTrue(callbacks.isEmpty());
        });
    }

    @Test
    public void members_mouseAndKeyboardActivation_onlyInvokesOncePerAction() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = model();
            List<NusId> callbacks = new ArrayList<>();
            MemberListPanel panel = new MemberListPanel(model.getAddressBook(), callbacks::add);
            StackPane root = mount(panel.getRoot());
            panel.select(Optional.of(new NusId("A0000001X")));
            root.layout();
            ListCell<?> cell = cellFor(rows(panel), model.findMember(new NusId("A0000001X")).orElseThrow());
            cell.fireEvent(click(MouseButton.PRIMARY, 1));
            cell.fireEvent(click(MouseButton.PRIMARY, 1));
            assertEquals(2, callbacks.size());
            cell.fireEvent(click(MouseButton.PRIMARY, 2));
            cell.fireEvent(click(MouseButton.SECONDARY, 1));
            assertEquals(2, callbacks.size());
            rows(panel).fireEvent(key(KeyCode.ENTER));
            rows(panel).fireEvent(key(KeyCode.SPACE));
            assertEquals(4, callbacks.size());
            rows(panel).fireEvent(key(KeyCode.DOWN));
            panel.refresh();
            assertEquals(4, callbacks.size());
            panel.select(Optional.empty());
            rows(panel).fireEvent(key(KeyCode.ENTER));
            assertEquals(4, callbacks.size());
        });
    }

    @Test
    public void equipment_activation_usesExactUuidAndIgnoresEmptySelection() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = model();
            List<InventoryGroups.Key> callbacks = new ArrayList<>();
            EquipmentListPanel panel = new EquipmentListPanel(model.getAddressBook(), model::getAvailability,
                    callbacks::add);
            StackPane root = mount(panel.getRoot());
            UUID id = new UUID(0, 2);
            panel.select(Optional.of(id));
            root.layout();
            ListCell<?> cell = cellFor(rows(panel), model.findEquipment(id).orElseThrow());
            cell.fireEvent(click(MouseButton.PRIMARY, 1));
            rows(panel).fireEvent(key(KeyCode.ENTER));
            rows(panel).fireEvent(key(KeyCode.SPACE));
            assertEquals(List.of(InventoryGroups.keyOf(model.findEquipment(id).orElseThrow()),
                    InventoryGroups.keyOf(model.findEquipment(id).orElseThrow()),
                    InventoryGroups.keyOf(model.findEquipment(id).orElseThrow())), callbacks);
            cell.fireEvent(click(MouseButton.PRIMARY, 2));
            cell.fireEvent(click(MouseButton.SECONDARY, 1));
            rows(panel).fireEvent(key(KeyCode.DOWN));
            assertEquals(List.of(InventoryGroups.keyOf(model.findEquipment(id).orElseThrow()),
                    InventoryGroups.keyOf(model.findEquipment(id).orElseThrow()),
                    InventoryGroups.keyOf(model.findEquipment(id).orElseThrow())), callbacks);
            panel.refresh();
            assertEquals(3, callbacks.size());
            panel.select(Optional.empty());
            rows(panel).fireEvent(key(KeyCode.ENTER));
            assertEquals(3, callbacks.size());
        });
    }

    @Test
    public void deletedSelections_retainsBothIdentitiesAndNeverActivates() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = model();
            List<NusId> memberCallbacks = new ArrayList<>();
            List<InventoryGroups.Key> equipmentCallbacks = new ArrayList<>();
            MemberListPanel members = new MemberListPanel(model.getAddressBook(), memberCallbacks::add);
            EquipmentListPanel equipment = new EquipmentListPanel(model.getAddressBook(), model::getAvailability,
                    equipmentCallbacks::add);
            NusId nusId = new NusId("A0000001X");
            UUID uuid = new UUID(0, 1);
            members.select(Optional.of(nusId));
            equipment.select(Optional.of(uuid));
            model.deleteMember(model.findMember(nusId).orElseThrow());
            model.deleteEquipment(model.findEquipment(uuid).orElseThrow());
            members.select(Optional.of(nusId));
            equipment.select(Optional.of(uuid));
            assertEquals(Optional.of(nusId), members.getSelection());
            assertEquals(Optional.of(uuid), equipment.getSelection());
            members.refresh();
            equipment.refresh();
            assertTrue(memberCallbacks.isEmpty());
            assertTrue(equipmentCallbacks.isEmpty());
            assertEquals(3, rows(members).getItems().size());
            assertEquals(1, rows(equipment).getItems().size());
        });
    }

    @Test
    public void loanOnlyRefresh_rebuildsRealCardsAndPreservesSelection() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = model();
            MemberListPanel members = new MemberListPanel(model.getAddressBook(), unused -> { });
            EquipmentListPanel equipment = new EquipmentListPanel(model.getAddressBook(), model::getAvailability,
                    unused -> { });
            NusId nusId = new NusId("A0000001X");
            UUID uuid = new UUID(0, 1);
            members.select(Optional.of(nusId));
            equipment.select(Optional.of(uuid));
            StackPane memberRoot = mount(members.getRoot());
            StackPane equipmentRoot = mount(equipment.getRoot());
            LocalDate date = LocalDate.of(2026, 1, 1);
            model.addLoan(new Loan(uuid, nusId, date, date, null));
            AddressBook before = new AddressBook(model.getAddressBook());
            members.refresh();
            equipment.refresh();
            memberRoot.layout();
            equipmentRoot.layout();
            Region memberCard = (Region) cellFor(rows(members), model.findMember(nusId).orElseThrow()).getGraphic();
            Region equipmentCard = (Region) cellFor(rows(equipment),
                    model.findEquipment(uuid).orElseThrow()).getGraphic();
            assertEquals("1 item held", label(memberCard, "itemsHeld"));
            assertEquals("alex@example.com", label(memberCard, "email"));
            assertTrue(label(memberCard, "identityAndPhone").contains(nusId.value));
            assertTrue(equipmentCard.lookupAll(".label").stream().map(node -> ((Label) node).getText())
                    .anyMatch(text -> text.contains("2 available")));
            assertEquals(before, model.getAddressBook());
            assertEquals(Optional.of(nusId), members.getSelection());
            assertEquals(Optional.of(uuid), equipment.getSelection());
            model.closeLoan(uuid, date);
            members.refresh();
            equipment.refresh();
            memberRoot.layout();
            equipmentRoot.layout();
            memberCard = (Region) cellFor(rows(members), model.findMember(nusId).orElseThrow()).getGraphic();
            equipmentCard = (Region) cellFor(rows(equipment), model.findEquipment(uuid).orElseThrow()).getGraphic();
            assertEquals("0 items held", label(memberCard, "itemsHeld"));
            assertTrue(equipmentCard.lookupAll(".label").stream().map(node -> ((Label) node).getText())
                    .anyMatch(text -> text.contains("3 available")));
        });
    }

    @Test
    public void equipment_inconsistentAvailability_doesNotInventAvailableOrUnassignedState() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = model();
            EquipmentListPanel panel = new EquipmentListPanel(model.getAddressBook(), uuid -> {
                throw new EquipmentNotFoundException();
            }, unused -> { });
            mount(panel.getRoot());
            Region row = (Region) cellFor(rows(panel), model.findEquipment(new UUID(0, 1)).orElseThrow()).getGraphic();
            assertTrue(row.lookupAll(".label").stream().map(node -> ((Label) node).getText())
                    .anyMatch(text -> text.contains("Availability could not be determined")));
        });
    }

    @Test
    public void equipment_missingHolderAndConflictingAvailability_areExplicit() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = model();
            UUID uuid = new UUID(0, 1);
            LocalDate date = LocalDate.of(2026, 1, 1);
            model.addLoan(new Loan(uuid, new NusId("A0000001X"), date, date, null));
            AddressBook missingMembers = new AddressBook(model.getAddressBook()) {
                @Override
                public ObservableList<Member> getMemberList() {
                    return FXCollections.emptyObservableList();
                }
            };
            EquipmentDetailsPanel missing = new EquipmentDetailsPanel(missingMembers, model::getAvailability);
            missing.showEquipment(uuid);
            mount(missing.getRoot());
            assertTrue(label(missing.getRoot(), "detailsStatus").contains("missing holder reference"));
            EquipmentDetailsPanel conflicting = new EquipmentDetailsPanel(model.getAddressBook(),
                    unused -> Availability.AVAILABLE);
            conflicting.showEquipment(uuid);
            mount(conflicting.getRoot());
            assertTrue(label(conflicting.getRoot(), "detailsStatus").contains("open loan conflicts"));
        });
    }

    @Test
    public void cells_reusedForEmptyAndNullSlots_clearGraphicsAndNeverActivate() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = model();
            AddressBook before = new AddressBook(model.getAddressBook());
            List<NusId> memberCallbacks = new ArrayList<>();
            List<InventoryGroups.Key> equipmentCallbacks = new ArrayList<>();
            MemberListPanel members = new MemberListPanel(model.getAddressBook(), memberCallbacks::add);
            EquipmentListPanel equipment = new EquipmentListPanel(model.getAddressBook(), model::getAvailability,
                    equipmentCallbacks::add);
            verifyCellReuse(typedRows(members));
            verifyCellReuse(typedRows(equipment));
            assertTrue(memberCallbacks.isEmpty());
            assertTrue(equipmentCallbacks.isEmpty());
            assertEquals(before, model.getAddressBook());
            members.refresh();
            equipment.refresh();
            assertEquals(3, rows(members).getItems().size());
            assertEquals(1, rows(equipment).getItems().size());
        });
    }

    @Test
    public void emptyAndNullInputs_haveUsefulPlaceholderAndRejectMisuse() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = new ModelManager();
            MemberListPanel members = new MemberListPanel(model.getAddressBook(), unused -> { });
            EquipmentListPanel equipment = new EquipmentListPanel(model.getAddressBook(), model::getAvailability,
                    unused -> { });
            assertEquals("No members yet", ((Label) rows(members).getPlaceholder()).getText());
            assertTrue(((Label) rows(equipment).getPlaceholder()).getText().contains("equipment add"));
            assertTrue(members.getSelection().isEmpty());
            assertTrue(equipment.getSelection().isEmpty());
            assertThrows(NullPointerException.class, () -> new MemberListPanel(null, unused -> { }));
            assertThrows(NullPointerException.class, () -> new MemberListPanel(model.getAddressBook(), null));
            assertThrows(NullPointerException.class, () ->
                    new EquipmentListPanel(null, model::getAvailability, unused -> { }));
            assertThrows(NullPointerException.class, () ->
                    new EquipmentListPanel(model.getAddressBook(), null, unused -> { }));
            assertThrows(NullPointerException.class, () ->
                    new EquipmentListPanel(model.getAddressBook(), model::getAvailability, null));
            assertThrows(NullPointerException.class, () -> members.select(null));
            assertThrows(NullPointerException.class, () -> equipment.select(null));
        });
    }

    private static ModelManager model() {
        ModelManager model = new ModelManager();
        for (int id : new int[] {3, 2, 1}) {
            model.addMember(new Member(new NusId("A000000" + id + "X"), new Name(id == 3 ? "Zed" : "Alex"),
                    new Phone("91234567"), new Email("alex@example.com")));
            model.addEquipment(new Equipment(new UUID(0, id), "Camera", "Photography", Condition.GOOD, ""));
        }
        return model;
    }

    private static StackPane mount(Region content) {
        StackPane root = new StackPane(content);
        root.getStyleClass().add("laplace-root");
        root.getStylesheets().add(DirectoryListPanelTest.class.getResource("/view/LaplaceTheme.css").toExternalForm());
        new Scene(root, 400, 700);
        root.resize(400, 700);
        root.applyCss();
        root.layout();
        return root;
    }

    private static ListView<?> rows(UiPart<Region> panel) {
        return (ListView<?>) panel.getRoot().lookup("#rows");
    }

    private static ListCell<?> cellFor(ListView<?> rows, Object item) {
        return rows.lookupAll(".list-cell").stream().map(node -> (ListCell<?>) node)
                .filter(cell -> item.equals(cell.getItem()) || item instanceof Equipment unit
                        && cell.getItem() instanceof InventoryGroups.Group group && group.units().contains(unit))
                .findFirst().orElseThrow();
    }

    private static String label(Region root, String id) {
        return ((Label) root.lookup("#" + id)).getText();
    }

    /** Exercise the real JavaFX virtual-cell lifecycle without reflection or production test seams. */
    private static <T> void verifyCellReuse(ListView<T> list) {
        ListCell<T> cell = list.getCellFactory().call(list);
        cell.updateListView(list);
        cell.updateIndex(0);
        assertTrue(cell.getGraphic() != null);
        list.getItems().clear();
        cell.updateIndex(-1);
        assertTrue(cell.isEmpty());
        assertEquals(null, cell.getGraphic());
        cell.fireEvent(click(MouseButton.PRIMARY, 1));

        // A null display slot is not a domain record and must not leave the previous card visible.
        list.getItems().add(null);
        cell.updateIndex(0);
        assertTrue(!cell.isEmpty());
        assertEquals(null, cell.getItem());
        assertEquals(null, cell.getGraphic());
        cell.fireEvent(click(MouseButton.PRIMARY, 1));
    }

    @SuppressWarnings("unchecked")
    private static <T> ListView<T> typedRows(UiPart<Region> panel) {
        return (ListView<T>) rows(panel);
    }

    private static MouseEvent click(MouseButton button, int count) {
        return new MouseEvent(MouseEvent.MOUSE_CLICKED, 5, 5, 5, 5, button, count,
                false, false, false, false, false, false, false, false, false, true, null);
    }

    private static KeyEvent key(KeyCode code) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, false);
    }
}
