package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.equipment.Availability;
import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.testutil.FxTestUtil;

public class EquipmentCardTest {

    private static final UUID ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private static final Equipment ITEM = new Equipment(ID, "Canon EOS-R50 camera with extra long lens",
            "Photography and audiovisual production equipment", Condition.GOOD, "");

    @Test
    public void constructor_displaysFieldsAndHolderStates_withoutActivationHandlers() {
        FxTestUtil.runAndWait(() -> {
            Member member = new Member(new NusId("A0123456X"), new Name("Alex Tan"),
                    new Phone("91234567"), new Email("alex@example.com"));
            Region root = new EquipmentCard(ITEM, Availability.ASSIGNED, Optional.of(member)).getRoot();
            assertEquals(ITEM.getName(), label(root, "name").getText());
            assertEquals("Category: " + ITEM.getCategory(), label(root, "category").getText());
            assertEquals("UUID: " + ID, label(root, "uuid").getText());
            assertEquals("Condition: GOOD", label(root, "condition").getText());
            assertEquals("Availability: ASSIGNED", label(root, "availability").getText());
            assertEquals("Holder: Alex Tan (A0123456X)", label(root, "holder").getText());
            assertNull(root.getOnMouseClicked());
            assertNull(root.getOnKeyPressed());
            for (Availability availability : Availability.values()) {
                Region emptyHolder = new EquipmentCard(ITEM, availability, Optional.empty()).getRoot();
                assertEquals(availability == Availability.ASSIGNED
                        ? "Holder reference missing (assigned equipment)" : "No current holder",
                        label(emptyHolder, "holder").getText());
            }
        });
    }

    @Test
    public void constructor_nullArguments_rejectsBeforeLoadingControls() {
        assertThrows(NullPointerException.class, () ->
                new EquipmentCard(null, Availability.AVAILABLE, Optional.empty()));
        assertThrows(NullPointerException.class, () -> new EquipmentCard(ITEM, null, Optional.empty()));
        assertThrows(NullPointerException.class, () -> new EquipmentCard(ITEM, Availability.AVAILABLE, null));
    }

    @Test
    public void layout_themedNormalAndNarrowWidths_wrapsAllFieldsWithoutClipping() {
        FxTestUtil.runAndWait(() -> {
            for (int width : new int[] {310, 210}) {
                Region root = new EquipmentCard(ITEM, Availability.AVAILABLE, Optional.empty()).getRoot();
                VBox container = new VBox(root);
                container.getStyleClass().add("laplace-root");
                Scene scene = new Scene(container, width, 500);
                scene.getStylesheets().add(EquipmentCard.class.getResource("/view/LaplaceTheme.css").toExternalForm());
                container.applyCss();
                container.resize(width, 500);
                container.layout();
                assertTrue(root.getStylesheets().get(0).endsWith("EquipmentCard.css"));
                assertTrue(root.getStyleClass().contains("laplace-row"));
                for (String id : new String[] {"name", "category", "uuid", "condition", "availability", "holder"}) {
                    Label label = label(root, id);
                    assertTrue(label.isVisible());
                    assertTrue(label.isWrapText());
                    assertTrue(label.getHeight() + 0.1 >= label.prefHeight(label.getWidth()));
                    assertTrue(label.getBoundsInParent().getMaxX() <= root.getWidth());
                    assertTrue(label.getBoundsInParent().getMaxY() <= root.getHeight());
                }
                assertEquals("UUID: " + ID, label(root, "uuid").getText());
            }
        });
    }

    private static Label label(Region root, String id) {
        return (Label) root.lookup("#" + id);
    }
}
