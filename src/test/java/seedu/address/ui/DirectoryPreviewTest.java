package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import seedu.address.model.ModelManager;
import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.loan.Loan;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.testutil.FxTestUtil;

/** Component previews only: this fixture is not the application shell or a navigation acceptance test. */
public class DirectoryPreviewTest {

    @Test
    public void directories_realCardsThemeAndSelection_renderNormalAndSmallPreviews() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = sample();
            for (boolean equipment : new boolean[] {false, true}) {
                for (int[] size : new int[][] {{1100, 700}, {760, 500}}) {
                    Region root = preview(model, equipment, size[0], size[1]);
                    ListView<?> rows = (ListView<?>) root.lookup("#rows");
                    ListCell<?> selected = rows.lookupAll(".list-cell").stream().map(node -> (ListCell<?>) node)
                            .filter(cell -> cell.isSelected() && !cell.isEmpty()).findFirst().orElseThrow();
                    assertEquals(Color.web("#E3F2FD"), selected.getBackground().getFills().getFirst().getFill());
                    assertTrue(selected.getGraphic().getStyleClass()
                            .contains(equipment ? "equipment-card" : "member-card"));
                    render(root, equipment ? "equipment" : "members", size[0], size[1]);
                }
            }
        });
    }

    @Test
    public void directories_emptyAndLongText_renderWithoutChangingData() {
        FxTestUtil.runAndWait(() -> {
            for (boolean equipment : new boolean[] {false, true}) {
                ModelManager empty = new ModelManager();
                Region root = preview(empty, equipment, 760, 500);
                assertTrue(((ListView<?>) root.lookup("#rows")).getItems().isEmpty());
                render(root, equipment ? "equipment-empty" : "members-empty", 760, 500);
            }
            ModelManager longText = new ModelManager();
            longText.addMember(new Member(new NusId("A0000001X"), new Name("Long Member Name ".repeat(16).trim()),
                    new Phone("91234567"), new Email("verylongmemberemailaddress@example.com")));
            longText.addEquipment(new Equipment(new UUID(0, 1), "Long Equipment Name ".repeat(16),
                    "Long Category ".repeat(8), Condition.GOOD, ""));
            for (boolean equipment : new boolean[] {false, true}) {
                Region root = preview(longText, equipment, 760, 500);
                render(root, equipment ? "equipment-long" : "members-long", 760, 500);
            }
            assertEquals(1, longText.getMemberList().size());
            assertEquals(1, longText.getEquipmentList().size());
        });
    }

    private static Region preview(ModelManager model, boolean equipment, int width, int height) {
        Region list;
        Region summary;
        if (equipment) {
            EquipmentDirectorySummary details = new EquipmentDirectorySummary(model.getAddressBook(),
                    model::getAvailability);
            EquipmentListPanel panel = new EquipmentListPanel(model.getAddressBook(), model::getAvailability,
                    uuid -> details.refresh(Optional.of(uuid)));
            Optional<UUID> selected = model.getEquipmentList().stream().map(Equipment::getUuid).findFirst();
            panel.select(selected);
            details.refresh(selected);
            list = panel.getRoot();
            summary = details.getRoot();
        } else {
            MemberDirectorySummary details = new MemberDirectorySummary(model.getAddressBook());
            MemberListPanel panel = new MemberListPanel(model.getAddressBook(),
                    nusId -> details.refresh(Optional.of(nusId)));
            Optional<NusId> selected = model.getMemberList().stream().map(Member::getNusId).findFirst();
            panel.select(selected);
            details.refresh(selected);
            list = panel.getRoot();
            summary = details.getRoot();
        }
        HBox header = new HBox(16, new Label("Laplace"), new Label("Directory component preview"));
        header.getStyleClass().add("laplace-header");
        list.setPrefWidth((width - 48) * 0.28);
        list.setMinWidth(160);
        summary.setMinWidth(0);
        summary.setPrefWidth(0);
        HBox columns = new HBox(16, list, summary);
        columns.setPadding(new Insets(16));
        HBox.setHgrow(summary, Priority.ALWAYS);
        VBox.setVgrow(columns, Priority.ALWAYS);
        VBox root = new VBox(header, columns);
        root.getStyleClass().add("laplace-root");
        root.getStylesheets().add(DirectoryPreviewTest.class.getResource("/view/LaplaceTheme.css").toExternalForm());
        new Scene(root, width, height);
        root.resize(width, height);
        root.applyCss();
        root.layout();
        assertTrue(summary.localToScene(summary.getBoundsInLocal()).getMaxX() <= width);
        assertTrue(list.localToScene(list.getBoundsInLocal()).getMaxY() <= height);
        return root;
    }

    private static ModelManager sample() {
        ModelManager model = new ModelManager();
        String[] names = {"Alex Tan", "Jamie Lim", "Priya Nair"};
        for (int id = 1; id <= 3; id++) {
            model.addMember(new Member(new NusId("A000000" + id + "X"), new Name(names[id - 1]),
                    new Phone("91234567"), new Email("member" + id + "@example.com")));
            model.addEquipment(new Equipment(new UUID(0, id), "Camera " + id, "Photography",
                    id == 3 ? Condition.DAMAGED : Condition.GOOD, ""));
        }
        LocalDate date = LocalDate.of(2026, 1, 1);
        model.addLoan(new Loan(new UUID(0, 1), new NusId("A0000001X"), date, date.plusDays(1), null));
        return model;
    }

    private static void render(Region root, String name, int width, int height) {
        WritableImage image = root.snapshot(null, new WritableImage(width, height));
        int[] pixels = new int[width * height];
        image.getPixelReader().getPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), pixels, 0, width);
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        output.setRGB(0, 0, width, height, pixels, 0, width);
        Path path = Path.of("build", "reports", "laplace", "directory-" + name + "-" + width + "x" + height + ".png");
        try {
            Files.createDirectories(path.getParent());
            assertTrue(ImageIO.write(output, "png", path.toFile()));
        } catch (IOException e) {
            throw new AssertionError("Could not write directory preview", e);
        }
    }
}
