package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.testutil.FxTestUtil;

public class MemberCardTest {

    @Test
    public void constructor_memberAndCount_displaysSnapshotWithoutActivationHandlers() {
        FxTestUtil.runAndWait(() -> {
            Region root = new MemberCard(member("A0123456X", "Alex Tan", "alex@example.com"), 2).getRoot();
            assertEquals("Alex Tan", label(root, "name").getText());
            assertEquals("A0123456X · 91234567", label(root, "identityAndPhone").getText());
            assertEquals("alex@example.com", label(root, "email").getText());
            assertEquals("2 items held", label(root, "itemsHeld").getText());
            assertTrue(root.getStyleClass().contains("laplace-row"));
            assertNull(root.getOnMouseClicked());
            assertNull(root.getOnKeyPressed());
            assertEquals("0 items held", label(new MemberCard(member(
                    "A0123456X", "Alex Tan", "alex@example.com"), 0).getRoot(), "itemsHeld").getText());
            assertEquals("1 item held", label(new MemberCard(member(
                    "A0123456X", "Alex Tan", "alex@example.com"), 1).getRoot(), "itemsHeld").getText());
        });
    }

    @Test
    public void constructor_nullMemberAndNegativeCount_rejectsMisuse() {
        FxTestUtil.runAndWait(() -> {
            assertThrows(NullPointerException.class, () -> new MemberCard(null, 0));
            assertThrows(IllegalArgumentException.class, () -> {
                new MemberCard(member("A0123456X", "Alex Tan", "alex@example.com"), -1);
            });
        });
    }

    @Test
    public void constructor_sameNames_distinguishesIdentities() {
        FxTestUtil.runAndWait(() -> {
            Region first = new MemberCard(member("A0123456X", "Alex Tan", "alex@example.com"), 0).getRoot();
            Region second = new MemberCard(member("A0234567R", "Alex Tan", "alex@example.com"), 0).getRoot();
            assertEquals(label(first, "name").getText(), label(second, "name").getText());
            assertNotEquals(label(first, "identityAndPhone").getText(), label(second, "identityAndPhone").getText());
        });
    }

    @Test
    public void layout_longFieldsAtNarrowWidth_wrapsWithoutLosingText() {
        FxTestUtil.runAndWait(() -> {
            String longName = "Alexandra Catherine Elizabeth Tan Wei Ling";
            String longEmail = "alexandra.catherine.elizabeth.tan@example.com";
            Region root = new MemberCard(member("A0123456X", longName, longEmail), 12).getRoot();
            VBox container = new VBox(root);
            new Scene(container, 210, 400);
            container.getStyleClass().add("laplace-root");
            container.getScene().getStylesheets().add(
                    MemberCard.class.getResource("/view/LaplaceTheme.css").toExternalForm());
            container.applyCss();
            container.resize(210, 400);
            container.layout();
            assertEquals(longName, label(root, "name").getText());
            assertEquals(longEmail, label(root, "email").getText());
            for (String id : new String[] {"name", "identityAndPhone", "email", "itemsHeld"}) {
                Label label = label(root, id);
                assertTrue(label.isWrapText());
                assertTrue(label.getHeight() + 0.1 >= label.prefHeight(label.getWidth()));
                assertTrue(label.getBoundsInParent().getMaxY() <= root.getHeight());
                assertTrue(label.getBoundsInParent().getMaxX() <= root.getWidth());
            }
        });
    }

    @Test
    public void deletedName_longText_wrapsWithoutDisablingOrChangingStoredName() {
        FxTestUtil.runAndWait(() -> {
            String longName = "Long Member Name ".repeat(6).trim();
            Member member = new Member(new NusId("A0123456X"), new Name(longName), new Phone("91234567"),
                    new Email("alex@example.com"), true);
            Region root = new MemberCard(member, 0).getRoot();
            VBox container = new VBox(root);
            new Scene(container, 210, 500);
            container.getStyleClass().add("laplace-root");
            container.getScene().getStylesheets().add(
                    MemberCard.class.getResource("/view/LaplaceTheme.css").toExternalForm());
            container.applyCss();
            container.resize(210, 500);
            container.layout();
            Label name = label(root, "name");
            TextFlow flow = (TextFlow) name.getGraphic();
            assertEquals(longName, member.getName().fullName);
            assertEquals(longName + " (deleted)", name.getAccessibleText());
            assertTrue(((Text) flow.getChildren().getFirst()).isStrikethrough());
            assertTrue(root.getStyleClass().contains("deleted-member"));
            assertFalse(root.isDisabled());
            assertTrue(flow.getHeight() > name.getFont().getSize());
            assertTrue(name.getHeight() + 0.1 >= name.prefHeight(name.getWidth()));
            assertTrue(name.getBoundsInParent().getMaxX() <= root.getWidth());
        });
    }

    private static Label label(Region root, String id) {
        return (Label) root.lookup("#" + id);
    }

    private static Member member(String nusId, String name, String email) {
        return new Member(new NusId(nusId), new Name(name), new Phone("91234567"), new Email(email));
    }
}
