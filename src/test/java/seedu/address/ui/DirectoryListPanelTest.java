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
            List<UUID> callbacks = new ArrayList<>();
            EquipmentListPanel panel = new EquipmentListPanel(model.getAddressBook(), model::getAvailability,
                    callbacks::add);
            mount(panel.getRoot());
            assertEquals(List.of(new UUID(0, 1), new UUID(0, 2), new UUID(0, 3)), rows(panel).getItems().stream()
                    .map(item -> ((Equipment) item).getUuid()).toList());
            panel.select(Optional.of(new UUID(0, 2)));
            panel.refresh();
            assertEquals(Optional.of(new UUID(0, 2)), panel.getSelection());
            panel.select(Optional.of(new UUID(0, 99)));
            assertTrue(panel.getSelection().isEmpty());
            assertTrue(callbacks.isEmpty());
            assertEquals(before, model.getAddressBook());
            assertEquals("3 items", label(panel.getRoot(), "countLabel"));
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
            List<UUID> callbacks = new ArrayList<>();
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
            assertEquals(List.of(id, id, id), callbacks);
            panel.refresh();
            assertEquals(3, callbacks.size());
            panel.select(Optional.empty());
            rows(panel).fireEvent(key(KeyCode.ENTER));
            assertEquals(3, callbacks.size());
        });
    }

    @Test
    public void removedSelections_clearEvenBeforeRefreshAndNeverActivate() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = model();
            List<NusId> memberCallbacks = new ArrayList<>();
            List<UUID> equipmentCallbacks = new ArrayList<>();
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
            assertTrue(members.getSelection().isEmpty());
            assertTrue(equipment.getSelection().isEmpty());
            members.refresh();
            equipment.refresh();
            assertTrue(memberCallbacks.isEmpty());
            assertTrue(equipmentCallbacks.isEmpty());
            assertEquals(2, rows(members).getItems().size());
            assertEquals(2, rows(equipment).getItems().size());
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
            assertEquals("Availability: ASSIGNED", label(equipmentCard, "availability"));
            assertTrue(label(equipmentCard, "holder").contains(nusId.value));
            assertEquals("Condition: GOOD", label(equipmentCard, "condition"));
            assertEquals("Category: Photography", label(equipmentCard, "category"));
            assertTrue(label(equipmentCard, "uuid").contains(uuid.toString()));
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
            assertEquals("No current holder", label(equipmentCard, "holder"));
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
            assertTrue(((Label) row.getChildrenUnmodifiable().getFirst()).getText()
                    .contains("Availability could not be determined"));
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
            EquipmentListPanel missing = new EquipmentListPanel(missingMembers,
                    model::getAvailability, unused -> { });
            mount(missing.getRoot());
            Region card = (Region) cellFor(rows(missing), model.findEquipment(uuid).orElseThrow()).getGraphic();
            assertTrue(label(card, "holder").contains("Holder reference missing"));
            assertEquals("Availability: ASSIGNED", label(card, "availability"));
            EquipmentListPanel conflicting = new EquipmentListPanel(model.getAddressBook(),
                    unused -> Availability.AVAILABLE, unused -> { });
            mount(conflicting.getRoot());
            Region row = (Region) cellFor(rows(conflicting), model.findEquipment(uuid).orElseThrow()).getGraphic();
            assertTrue(((Label) row.getChildrenUnmodifiable().getFirst()).getText()
                    .contains("Open loan conflicts with derived availability"));
        });
    }

    @Test
    public void emptyAndNullInputs_haveUsefulPlaceholderAndRejectMisuse() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = new ModelManager();
            MemberListPanel members = new MemberListPanel(model.getAddressBook(), unused -> { });
            EquipmentListPanel equipment = new EquipmentListPanel(model.getAddressBook(), model::getAvailability,
                    unused -> { });
            assertTrue(((Label) rows(members).getPlaceholder()).getText().contains("member add"));
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
                .filter(cell -> item.equals(cell.getItem())).findFirst().orElseThrow();
    }

    private static String label(Region root, String id) {
        return ((Label) root.lookup("#" + id)).getText();
    }

    private static MouseEvent click(MouseButton button, int count) {
        return new MouseEvent(MouseEvent.MOUSE_CLICKED, 5, 5, 5, 5, button, count,
                false, false, false, false, false, false, false, false, false, true, null);
    }

    private static KeyEvent key(KeyCode code) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, false);
    }
}
