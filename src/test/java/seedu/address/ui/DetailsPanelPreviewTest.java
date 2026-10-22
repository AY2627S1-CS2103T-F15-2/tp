package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.ViewTestData.ALEX;
import static seedu.address.testutil.ViewTestData.CAMERA;
import static seedu.address.testutil.ViewTestData.OTHER_CAMERA;
import static seedu.address.testutil.ViewTestData.loan;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.member.Member;
import seedu.address.model.person.Name;
import seedu.address.testutil.FxTestUtil;
import seedu.address.testutil.ViewTestData;

/** Renders real list/detail components; this fixture is not the application shell or routed navigation. */
public class DetailsPanelPreviewTest {
    @Test
    public void details_normalAndSmallPreviews_showIdentitiesAndResponsiveColumns() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = ViewTestData.model();
            model.addLoan(loan(CAMERA, ALEX, true));
            model.addLoan(loan(OTHER_CAMERA, ALEX, true));
            AddressBook before = new AddressBook(model.getAddressBook());
            for (boolean equipment : new boolean[] {false, true}) {
                for (int[] size : List.of(new int[] {1100, 700}, new int[] {760, 500})) {
                    Region root = preview(model, equipment, true, size[0], size[1]);
                    Label identity = (Label) root.lookup(equipment ? "#equipmentUuid" : "#memberNusId");
                    assertEquals(equipment ? CAMERA.getUuid().toString() : ALEX.getNusId().value, identity.getText());
                    render(root, equipment ? "equipment" : "member", size[0], size[1]);
                    ScrollPane details = (ScrollPane) root.lookup(".details-scroll");
                    details.setVvalue(1);
                    root.layout();
                    render(root, equipment ? "equipment-bottom" : "member-bottom", size[0], size[1]);
                }
            }
            assertEquals(before, model.getAddressBook());
        });
    }

    @Test
    public void details_longTextAndManyHoldings_wrapAndScrollWithoutClippingLabels() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = new ModelManager();
            Member member = new Member(ALEX.getNusId(), new Name("Long Member Name ".repeat(20).trim()),
                    ALEX.getPhone(), ALEX.getEmail());
            model.addMember(member);
            for (int i = 1; i <= 12; i++) {
                Equipment item = new Equipment(new UUID(0, i), "Long Camera Name ".repeat(15),
                        "Long Category ".repeat(5), Condition.GOOD,
                        "Long multiline notes. ".repeat(120) + "\nLast line.");
                model.addEquipment(item);
                model.addLoan(loan(item, member, true));
            }
            for (boolean equipment : new boolean[] {false, true}) {
                Region root = preview(model, equipment, true, 760, 500);
                ScrollPane details = (ScrollPane) root.lookup(".details-scroll");
                assertTrue(details.getContent().getBoundsInLocal().getHeight()
                        > details.getViewportBounds().getHeight());
                Parent content = (Parent) details.getContent();
                for (var node : content.lookupAll(".label")) {
                    Label label = (Label) node;
                    assertTrue(label.isWrapText());
                    assertTrue(label.getWidth() <= details.getViewportBounds().getWidth(), label.getId());
                    assertTrue(label.getHeight() + 1 >= label.prefHeight(label.getWidth()), label.getId());
                }
                render(root, equipment ? "equipment-long" : "member-long", 760, 500);
            }
        });
    }

    @Test
    public void details_emptyMissingAndNoHolderStates_renderPreviews() {
        FxTestUtil.runAndWait(() -> {
            for (boolean equipment : new boolean[] {false, true}) {
                render(preview(new ModelManager(), equipment, false, 760, 500),
                        equipment ? "equipment-empty" : "member-empty", 760, 500);
                Region missing = preview(new ModelManager(), equipment, true, 760, 500);
                assertTrue(((Label) missing.lookup("#detailsStatus")).getText().contains("no longer exists"));
                render(missing, equipment ? "equipment-missing" : "member-missing", 760, 500);
            }
            Region idle = preview(ViewTestData.model(), true, true, 760, 500);
            assertEquals("No current holder", ((Label) idle.lookup("#holder")).getText());
            render(idle, "equipment-no-holder", 760, 500);
        });
    }

    private static Region preview(ModelManager model, boolean equipment, boolean select, int width, int height) {
        Region list;
        Region details;
        if (equipment) {
            EquipmentDetailsPanel panel = new EquipmentDetailsPanel(model.getAddressBook(), model::getAvailability);
            EquipmentListPanel rows = new EquipmentListPanel(model.getAddressBook(), model::getAvailability,
                    unused -> { });
            if (select) {
                panel.showEquipment(CAMERA.getUuid());
                rows.select(Optional.of(CAMERA.getUuid()));
            }
            list = rows.getRoot();
            details = panel.getRoot();
        } else {
            MemberDetailsPanel panel = new MemberDetailsPanel(model.getAddressBook(), model::getAvailability);
            MemberListPanel rows = new MemberListPanel(model.getAddressBook(), panel::showMember);
            if (select) {
                panel.showMember(ALEX.getNusId());
                rows.select(Optional.of(ALEX.getNusId()));
            }
            list = rows.getRoot();
            details = panel.getRoot();
        }
        HBox header = new HBox(16, new Label("Laplace"), new Label("Detail component preview"));
        header.getStyleClass().add("laplace-header");
        list.setPrefWidth((width - 48) * 0.28);
        list.setMinWidth(160);
        details.setPrefWidth(0);
        HBox.setHgrow(details, Priority.ALWAYS);
        HBox columns = new HBox(16, list, details);
        columns.setPadding(new Insets(16));
        VBox.setVgrow(columns, Priority.ALWAYS);
        VBox root = new VBox(header, columns);
        root.getStyleClass().add("laplace-root");
        root.getStylesheets().add(DetailsPanelPreviewTest.class.getResource("/view/LaplaceTheme.css").toExternalForm());
        new Scene(root, width, height);
        root.resize(width, height);
        root.applyCss();
        root.layout();
        assertNotNull(details.lookup("#detailsTitle"));
        Region viewport = (Region) details.lookup(".viewport");
        assertEquals(Color.WHITE, viewport.getBackground().getFills().getFirst().getFill());
        assertTrue(details.localToScene(details.getBoundsInLocal()).getMaxX() <= width);
        assertTrue(details.localToScene(details.getBoundsInLocal()).getMaxY() <= height);
        return root;
    }

    private static void render(Region root, String name, int width, int height) {
        WritableImage image = root.snapshot(null, new WritableImage(width, height));
        int[] pixels = new int[width * height];
        image.getPixelReader().getPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), pixels, 0, width);
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        output.setRGB(0, 0, width, height, pixels, 0, width);
        Path path = Path.of("build", "reports", "laplace", "details-" + name + "-" + width + "x" + height + ".png");
        try {
            Files.createDirectories(path.getParent());
            assertTrue(ImageIO.write(output, "png", path.toFile()));
        } catch (IOException e) {
            throw new AssertionError("Could not write detail preview", e);
        }
    }
}
