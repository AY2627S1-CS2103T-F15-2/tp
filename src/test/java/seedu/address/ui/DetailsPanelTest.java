package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.ViewTestData.ALEX;
import static seedu.address.testutil.ViewTestData.CAMERA;
import static seedu.address.testutil.ViewTestData.OTHER_ALEX;
import static seedu.address.testutil.ViewTestData.OTHER_CAMERA;
import static seedu.address.testutil.ViewTestData.SPEAKER;
import static seedu.address.testutil.ViewTestData.loan;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.equipment.Availability;
import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.equipment.exceptions.EquipmentNotFoundException;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.testutil.FxTestUtil;
import seedu.address.testutil.ViewTestData;

/** Actual FXML and rendered-field checks, using real models except deliberately inconsistent references. */
public class DetailsPanelTest {
    @Test
    public void constructionAndRefresh_withoutSelection_showEmptyStates() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = ViewTestData.model();
            MemberDetailsPanel members = members(model);
            EquipmentDetailsPanel equipment = equipment(model);
            members.refresh();
            equipment.refresh();
            assertEquals("Select a member to view their details.", value(members.getRoot(), "detailsStatus"));
            assertEquals("Select equipment to view its details.", value(equipment.getRoot(), "detailsStatus"));
            assertNull(members.getRoot().lookup("#memberName"));
            assertNull(equipment.getRoot().lookup("#equipmentName"));
        });
    }

    @Test
    public void memberDetails_completeContactsSameNamesAndNoHoldings_useNusIdentity() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = ViewTestData.model();
            AddressBook before = new AddressBook(model.getAddressBook());
            MemberDetailsPanel panel = members(model);
            panel.showMember(new NusId("a0123456x"));
            assertEquals("Alex Tan", value(panel.getRoot(), "memberName"));
            assertEquals(ALEX.getNusId().value, value(panel.getRoot(), "memberNusId"));
            assertEquals(ALEX.getPhone().value, value(panel.getRoot(), "phone"));
            assertEquals(ALEX.getEmail().value, value(panel.getRoot(), "email"));
            assertEquals("0 item(s) held", value(panel.getRoot(), "heldCount"));
            assertEquals("No equipment currently held.", value(panel.getRoot(), "detailsStatus"));
            panel.showMember(OTHER_ALEX.getNusId());
            assertEquals("Alex Tan", value(panel.getRoot(), "memberName"));
            assertEquals(OTHER_ALEX.getNusId().value, value(panel.getRoot(), "memberNusId"));
            assertEquals(OTHER_ALEX.getEmail().value, value(panel.getRoot(), "email"));
            assertEquals(before, model.getAddressBook());
        });
    }

    @Test
    public void memberDetails_holdingsSortedByCategoryNameAndUuid_excludesClosedAndOtherMembers() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = ViewTestData.model();
            Equipment microphone = new Equipment(new UUID(0, 4), "Microphone", " audio ", Condition.GOOD, "");
            model.addEquipment(microphone);
            model.addLoan(loan(OTHER_CAMERA, ALEX, true));
            model.addLoan(loan(CAMERA, ALEX, true));
            model.addLoan(loan(SPEAKER, ALEX, true));
            model.addLoan(loan(microphone, ALEX, true));
            model.addLoan(loan(CAMERA, ALEX, false));
            MemberDetailsPanel panel = members(model);
            AddressBook before = new AddressBook(model.getAddressBook());
            panel.showMember(ALEX.getNusId());
            assertEquals("4 item(s) held", value(panel.getRoot(), "heldCount"));
            VBox holdings = (VBox) panel.getRoot().lookup("#holdings");
            assertEquals(List.of(microphone.getUuid(), SPEAKER.getUuid(), CAMERA.getUuid(), OTHER_CAMERA.getUuid()),
                    holdings.getChildren().stream().map(row -> UUID.fromString(value((Parent) row, "equipmentUuid")))
                            .toList());
            Parent camera = (Parent) holdings.getChildren().get(2);
            assertEquals(CAMERA.getCategory(), value(camera, "category"));
            assertEquals("GOOD", value(camera, "condition"));
            assertEquals("ASSIGNED", value(camera, "availability"));
            assertEquals(ViewTestData.ASSIGNED.toString(), value(camera, "assignedDate"));
            assertEquals(ViewTestData.EXPECTED.toString(), value(camera, "expectedReturnDate"));
            panel.showMember(OTHER_ALEX.getNusId());
            assertEquals("0 item(s) held", value(panel.getRoot(), "heldCount"));
            assertNull(panel.getRoot().lookup("#holdings"));
            assertEquals(before, model.getAddressBook());
        });
    }

    @Test
    public void equipmentDetails_completeFieldsAndSameNames_useUuidIdentity() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = ViewTestData.model();
            model.addLoan(loan(CAMERA, ALEX, true));
            AddressBook before = new AddressBook(model.getAddressBook());
            EquipmentDetailsPanel panel = equipment(model);
            panel.showEquipment(CAMERA.getUuid());
            assertEquals("Camera", value(panel.getRoot(), "equipmentName"));
            assertEquals(CAMERA.getUuid().toString(), value(panel.getRoot(), "equipmentUuid"));
            assertEquals(CAMERA.getCategory(), value(panel.getRoot(), "category"));
            assertEquals("GOOD", value(panel.getRoot(), "condition"));
            assertEquals(CAMERA.getNotes(), value(panel.getRoot(), "notes"));
            assertEquals("ASSIGNED", value(panel.getRoot(), "availability"));
            assertEquals(ALEX.getName().fullName, value(panel.getRoot(), "holder"));
            assertEquals(ALEX.getNusId().value, value(panel.getRoot(), "holderNusId"));
            assertEquals(ALEX.getPhone().value, value(panel.getRoot(), "holderPhone"));
            assertEquals(ALEX.getEmail().value, value(panel.getRoot(), "holderEmail"));
            assertEquals(ViewTestData.ASSIGNED.toString(), value(panel.getRoot(), "assignedDate"));
            assertEquals(ViewTestData.EXPECTED.toString(), value(panel.getRoot(), "expectedReturnDate"));
            panel.showEquipment(OTHER_CAMERA.getUuid());
            assertEquals("Camera", value(panel.getRoot(), "equipmentName"));
            assertEquals(OTHER_CAMERA.getUuid().toString(), value(panel.getRoot(), "equipmentUuid"));
            assertEquals("No current holder", value(panel.getRoot(), "holder"));
            assertNull(panel.getRoot().lookup("#holderPhone"));
            assertNull(panel.getRoot().lookup("#assignedDate"));
            assertEquals(before, model.getAddressBook());
        });
    }

    @Test
    public void equipmentDetails_allConditions_usesDerivedAvailabilityAndEmptyNotes() {
        FxTestUtil.runAndWait(() -> {
            for (Condition condition : Condition.values()) {
                ModelManager model = new ModelManager();
                model.addEquipment(new Equipment(CAMERA.getUuid(), "Camera", "Photography", condition, ""));
                EquipmentDetailsPanel panel = equipment(model);
                panel.showEquipment(CAMERA.getUuid());
                assertEquals(condition.toString(), value(panel.getRoot(), "condition"));
                assertEquals(model.getAvailability(CAMERA.getUuid()).toString(),
                        value(panel.getRoot(), "availability"));
                assertEquals("No notes", value(panel.getRoot(), "notes"));
                assertEquals("No current holder", value(panel.getRoot(), "holder"));
            }
        });
    }

    @Test
    public void refresh_assignmentReturnAndReassignment_updatesBothPanelsWithoutEntityListChanges() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = ViewTestData.model();
            MemberDetailsPanel members = members(model);
            EquipmentDetailsPanel equipment = equipment(model);
            members.showMember(ALEX.getNusId());
            equipment.showEquipment(CAMERA.getUuid());
            List<Member> originalMembers = List.copyOf(model.getMemberList());
            List<Equipment> originalEquipment = List.copyOf(model.getEquipmentList());
            model.addLoan(loan(CAMERA, ALEX, true));
            members.refresh();
            equipment.refresh();
            assertEquals("1 item(s) held", value(members.getRoot(), "heldCount"));
            assertEquals(ALEX.getNusId().value, value(equipment.getRoot(), "holderNusId"));
            model.closeLoan(CAMERA.getUuid(), ViewTestData.EXPECTED);
            members.refresh();
            equipment.refresh();
            assertEquals("0 item(s) held", value(members.getRoot(), "heldCount"));
            assertEquals("AVAILABLE", value(equipment.getRoot(), "availability"));
            assertEquals("No current holder", value(equipment.getRoot(), "holder"));
            assertNull(equipment.getRoot().lookup("#assignedDate"));
            assertNull(equipment.getRoot().lookup("#expectedReturnDate"));
            model.addLoan(loan(CAMERA, OTHER_ALEX, true));
            members.refresh();
            equipment.refresh();
            assertEquals("0 item(s) held", value(members.getRoot(), "heldCount"));
            assertEquals(OTHER_ALEX.getNusId().value, value(equipment.getRoot(), "holderNusId"));
            assertEquals(originalMembers, model.getMemberList());
            assertEquals(originalEquipment, model.getEquipmentList());
        });
    }

    @Test
    public void refresh_removalClearsDetailsAndUnknownSelectionShowsMissingIdentity() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = ViewTestData.model();
            MemberDetailsPanel members = members(model);
            EquipmentDetailsPanel equipment = equipment(model);
            members.showMember(ALEX.getNusId());
            equipment.showEquipment(CAMERA.getUuid());
            model.deleteMember(ALEX);
            model.deleteEquipment(CAMERA);
            members.refresh();
            equipment.refresh();
            assertTrue(value(members.getRoot(), "detailsStatus").contains(ALEX.getNusId().value));
            assertTrue(value(equipment.getRoot(), "detailsStatus").contains(CAMERA.getUuid().toString()));
            assertNull(members.getRoot().lookup("#memberName"));
            assertNull(equipment.getRoot().lookup("#notes"));
            members.showMember(new NusId("A0999999Z"));
            equipment.showEquipment(new UUID(0, 999));
            assertTrue(value(members.getRoot(), "detailsStatus").contains("A0999999Z"));
            assertTrue(value(equipment.getRoot(), "detailsStatus").contains(new UUID(0, 999).toString()));
        });
    }

    @Test
    public void refresh_replacedEntitiesRereadsContactsNotesConditionAndNames() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = ViewTestData.model();
            MemberDetailsPanel members = members(model);
            EquipmentDetailsPanel equipment = equipment(model);
            members.showMember(ALEX.getNusId());
            equipment.showEquipment(CAMERA.getUuid());
            AddressBook replacement = new AddressBook();
            replacement.addMember(new Member(ALEX.getNusId(), new Name("Jamie Lim"), new Phone("88889999"),
                    new Email("jamie@example.com")));
            replacement.addEquipment(new Equipment(CAMERA.getUuid(), "New camera name", "Film", Condition.UNDER_REPAIR,
                    "Changed notes"));
            model.setAddressBook(replacement);
            members.refresh();
            equipment.refresh();
            assertEquals("Jamie Lim", value(members.getRoot(), "memberName"));
            assertEquals("88889999", value(members.getRoot(), "phone"));
            assertEquals("jamie@example.com", value(members.getRoot(), "email"));
            assertEquals("New camera name", value(equipment.getRoot(), "equipmentName"));
            assertEquals("Film", value(equipment.getRoot(), "category"));
            assertEquals("Changed notes", value(equipment.getRoot(), "notes"));
            assertEquals("UNAVAILABLE", value(equipment.getRoot(), "availability"));
        });
    }

    @Test
    public void inconsistentReferences_areExplicitAndDoNotInventHolderOrHideHolding() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = ViewTestData.model();
            model.addLoan(loan(CAMERA, ALEX, true));
            AddressBook missingMember = new AddressBook(model.getAddressBook()) {
                @Override
                public ObservableList<Member> getMemberList() {
                    return FXCollections.emptyObservableList();
                }
            };
            EquipmentDetailsPanel equipment = new EquipmentDetailsPanel(missingMember, id -> Availability.ASSIGNED);
            mount(equipment.getRoot());
            equipment.showEquipment(CAMERA.getUuid());
            assertTrue(value(equipment.getRoot(), "detailsStatus").contains("missing holder reference "
                    + ALEX.getNusId()));
            assertNull(equipment.getRoot().lookup("#holder"));
            assertNotNull(equipment.getRoot().lookup("#assignedDate"));
            AddressBook missingEquipment = new AddressBook(model.getAddressBook()) {
                @Override
                public ObservableList<Equipment> getEquipmentList() {
                    return FXCollections.emptyObservableList();
                }
            };
            MemberDetailsPanel members = new MemberDetailsPanel(missingEquipment, id -> {
                throw new AssertionError("Missing equipment must not be passed to availability lookup");
            });
            mount(members.getRoot());
            members.showMember(ALEX.getNusId());
            assertEquals("1 item(s) held", value(members.getRoot(), "heldCount"));
            assertTrue(value(members.getRoot(), "detailsStatus").contains(CAMERA.getUuid().toString()));
            assertNotNull(members.getRoot().lookup("#expectedReturnDate"));
        });
    }

    @Test
    public void inconsistentAvailability_statesRemainExplicit() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = ViewTestData.model();
            EquipmentDetailsPanel equipment = new EquipmentDetailsPanel(model.getAddressBook(),
                    id -> Availability.ASSIGNED);
            mount(equipment.getRoot());
            equipment.showEquipment(CAMERA.getUuid());
            assertTrue(value(equipment.getRoot(), "detailsStatus").contains("no open loan"));
            assertNull(equipment.getRoot().lookup("#holder"));
            model.addLoan(loan(CAMERA, ALEX, true));
            equipment = new EquipmentDetailsPanel(model.getAddressBook(), id -> Availability.AVAILABLE);
            mount(equipment.getRoot());
            equipment.showEquipment(CAMERA.getUuid());
            assertTrue(value(equipment.getRoot(), "detailsStatus").contains("conflicts"));
            MemberDetailsPanel members = new MemberDetailsPanel(model.getAddressBook(), id -> Availability.AVAILABLE);
            mount(members.getRoot());
            members.showMember(ALEX.getNusId());
            assertTrue(value(members.getRoot(), "detailsStatus").contains("not marked assigned"));
        });
    }

    @Test
    public void missingAvailability_keepsIdentityAndLoanInformationVisible() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = ViewTestData.model();
            model.addLoan(loan(CAMERA, ALEX, true));
            EquipmentDetailsPanel equipment = new EquipmentDetailsPanel(model.getAddressBook(), id -> {
                throw new EquipmentNotFoundException();
            });
            MemberDetailsPanel members = new MemberDetailsPanel(model.getAddressBook(), id -> {
                throw new EquipmentNotFoundException();
            });
            mount(equipment.getRoot());
            mount(members.getRoot());
            equipment.showEquipment(CAMERA.getUuid());
            members.showMember(ALEX.getNusId());
            assertEquals("Unknown", value(equipment.getRoot(), "availability"));
            assertEquals(ALEX.getNusId().value, value(equipment.getRoot(), "holderNusId"));
            assertTrue(value(equipment.getRoot(), "detailsStatus").contains("could not be determined"));
            assertTrue(value(members.getRoot(), "detailsStatus").contains("could not be determined"));
            assertEquals(CAMERA.getUuid().toString(), value(members.getRoot(), "equipmentUuid"));
        });
    }

    @Test
    public void nullArguments_rejectedWithoutLosingSelection() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = ViewTestData.model();
            assertThrows(NullPointerException.class, () -> new MemberDetailsPanel(null, model::getAvailability));
            assertThrows(NullPointerException.class, () -> new MemberDetailsPanel(model.getAddressBook(), null));
            assertThrows(NullPointerException.class, () -> new EquipmentDetailsPanel(null, model::getAvailability));
            assertThrows(NullPointerException.class, () -> new EquipmentDetailsPanel(model.getAddressBook(), null));
            MemberDetailsPanel members = members(model);
            EquipmentDetailsPanel equipment = equipment(model);
            members.showMember(ALEX.getNusId());
            equipment.showEquipment(CAMERA.getUuid());
            assertThrows(NullPointerException.class, () -> members.showMember(null));
            assertThrows(NullPointerException.class, () -> equipment.showEquipment(null));
            assertEquals(ALEX.getNusId().value, value(members.getRoot(), "memberNusId"));
            assertEquals(CAMERA.getUuid().toString(), value(equipment.getRoot(), "equipmentUuid"));
        });
    }

    @Test
    public void fxml_usesVerticalScrollingAndScopedStyles() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = ViewTestData.model();
            for (Region root : List.of(members(model).getRoot(), equipment(model).getRoot())) {
                ScrollPane scroll = (ScrollPane) root;
                assertTrue(scroll.isFitToWidth());
                assertEquals(ScrollPane.ScrollBarPolicy.NEVER, scroll.getHbarPolicy());
                assertTrue(root.getStylesheets().stream().anyMatch(url -> url.endsWith("DetailsPanel.css")));
                assertFalse(root.getStyleClass().isEmpty());
            }
        });
    }

    private static MemberDetailsPanel members(ModelManager model) {
        MemberDetailsPanel panel = new MemberDetailsPanel(model.getAddressBook(), model::getAvailability);
        mount(panel.getRoot());
        return panel;
    }

    private static EquipmentDetailsPanel equipment(ModelManager model) {
        EquipmentDetailsPanel panel = new EquipmentDetailsPanel(model.getAddressBook(), model::getAvailability);
        mount(panel.getRoot());
        return panel;
    }

    private static void mount(Region root) {
        root.getStyleClass().add("laplace-root");
        root.getStylesheets().add(DetailsPanelTest.class.getResource("/view/LaplaceTheme.css").toExternalForm());
        new Scene(root, 760, 500);
        root.applyCss();
        root.layout();
    }

    private static String value(Parent root, String id) {
        Node node = root.lookup("#" + id);
        assertNotNull(node, id);
        return ((Label) node).getText();
    }
}
