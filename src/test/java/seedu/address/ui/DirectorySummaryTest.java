package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
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

public class DirectorySummaryTest {

    @Test
    public void members_emptyValidAndDeletedSelections_refreshesFromRealData() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = new ModelManager();
            MemberDirectorySummary panel = new MemberDirectorySummary(model.getAddressBook());
            mount(panel.getRoot(), 720, 500);
            assertLabel(panel, "activeCount", "0");
            assertLabel(panel, "displayedCount", "0");
            assertLabel(panel, "countBadge", "0 ACTIVE");
            assertLabel(panel, "selection", "No member selected");
            assertLabel(panel, "statusTitle", "No members yet");
            render(panel, "members-empty", 480, 480);
            assertTrue(label(panel, "statusMessage").contains("member add"));
            Member member = member(1, "Alex Tan");
            model.addMember(member);
            model.addMember(member(2, "Alex Tan"));
            panel.refresh(Optional.of(member.getNusId()));
            assertLabel(panel, "activeCount", "2");
            assertLabel(panel, "displayedCount", "2");
            assertLabel(panel, "countBadge", "2 ACTIVE");
            assertLabel(panel, "selection", "Alex Tan - A0000001X");
            assertLabel(panel, "sortOrder", "Name (A-Z), then NUS ID");
            assertLabel(panel, "statusTitle", "All active members displayed");
            model.deleteMember(member);
            panel.refresh(Optional.of(member.getNusId()));
            assertLabel(panel, "selection", "Alex Tan (deleted) - A0000001X");
            assertLabel(panel, "activeCount", "1");
            assertLabel(panel, "deletedCount", "1");
            assertLabel(panel, "displayedCount", "2");
            panel.refresh(Optional.empty());
            assertLabel(panel, "selection", "No member selected");
        });
    }

    @Test
    public void equipment_emptyAllConditionsAndRemovedSelection_showsAccurateCounts() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = new ModelManager();
            EquipmentDirectorySummary panel = new EquipmentDirectorySummary(model.getAddressBook(),
                    model::getAvailability);
            mount(panel.getRoot(), 720, 500);
            assertLabel(panel, "totalCount", "0");
            assertLabel(panel, "countBadge", "0 UNITS");
            assertLabel(panel, "statusTitle", "No equipment yet");
            assertTrue(label(panel, "statusMessage").contains("equipment add"));
            assertLabel(panel, "selection", "No equipment selected");
            for (Condition condition : Condition.values()) {
                model.addEquipment(item(condition.ordinal() + 1, condition));
            }
            UUID selected = new UUID(0, 1);
            panel.refresh(Optional.of(selected));
            assertLabel(panel, "totalCount", "4");
            assertLabel(panel, "displayedCount", "4");
            assertLabel(panel, "availableCount", "2");
            assertLabel(panel, "assignedCount", "0");
            assertLabel(panel, "unavailableCount", "2");
            assertLabel(panel, "sortOrder", "Category, name, then UUID");
            assertTrue(label(panel, "selection").contains(selected.toString()));
            model.deleteEquipment(model.findEquipment(selected).orElseThrow());
            panel.refresh(Optional.of(selected));
            assertTrue(label(panel, "selection").contains(selected.toString()));
            assertLabel(panel, "availableCount", "1");
            assertLabel(panel, "totalCount", "4");
            panel.refresh(Optional.empty());
            assertLabel(panel, "selection", "No equipment selected");
        });
    }

    @Test
    public void equipment_loanOnlyChanges_recalculatesAvailabilityWithoutMutation() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = populatedModel();
            EquipmentDirectorySummary panel = new EquipmentDirectorySummary(model.getAddressBook(),
                    model::getAvailability);
            mount(panel.getRoot(), 720, 500);
            UUID uuid = new UUID(0, 1);
            LocalDate date = LocalDate.of(2026, 1, 1);
            panel.refresh(Optional.of(uuid));
            assertLabel(panel, "availableCount", "2");
            model.addLoan(new Loan(uuid, new NusId("A0000001X"), date, date, null));
            AddressBook beforeRefresh = new AddressBook(model.getAddressBook());
            panel.refresh(Optional.of(uuid));
            assertLabel(panel, "availableCount", "1");
            assertLabel(panel, "assignedCount", "1");
            assertEquals(beforeRefresh, model.getAddressBook());
            model.closeLoan(uuid, date);
            panel.refresh(Optional.of(uuid));
            assertLabel(panel, "availableCount", "2");
            assertLabel(panel, "assignedCount", "0");
            assertTrue(label(panel, "selection").contains(uuid.toString()));
        });
    }

    @Test
    public void equipment_missingAvailability_displaysExplicitErrorAndRecovers() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = populatedModel();
            boolean[] missing = {true};
            EquipmentDirectorySummary panel = new EquipmentDirectorySummary(model.getAddressBook(), uuid -> {
                if (missing[0]) {
                    throw new EquipmentNotFoundException();
                }
                return model.getAvailability(uuid);
            });
            mount(panel.getRoot(), 720, 500);
            assertLabel(panel, "totalCount", "3");
            assertLabel(panel, "availableCount", "0");
            assertLabel(panel, "assignedCount", "0");
            assertLabel(panel, "unavailableCount", "0");
            assertLabel(panel, "statusTitle", "Inconsistent equipment data");
            render(panel, "equipment-error", 480, 480);
            assertTrue(label(panel, "statusMessage").contains("3 item(s)"));
            assertTrue(panel.getRoot().lookup("#status").getStyleClass().contains("laplace-error"));
            missing[0] = false;
            panel.refresh(Optional.empty());
            assertLabel(panel, "availableCount", "2");
            assertTrue(panel.getRoot().lookup("#status").getStyleClass().contains("laplace-success"));
        });
    }

    @Test
    public void summaries_nullInputs_rejectsProgrammerMisuse() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = new ModelManager();
            assertThrows(NullPointerException.class, () -> new MemberDirectorySummary(null));
            assertThrows(NullPointerException.class, () -> new EquipmentDirectorySummary(null, model::getAvailability));
            assertThrows(NullPointerException.class, () -> new EquipmentDirectorySummary(model.getAddressBook(), null));
            MemberDirectorySummary members = new MemberDirectorySummary(model.getAddressBook());
            EquipmentDirectorySummary equipment = new EquipmentDirectorySummary(model.getAddressBook(),
                    model::getAvailability);
            assertThrows(NullPointerException.class, () -> members.refresh(null));
            assertThrows(NullPointerException.class, () -> equipment.refresh(null));
        });
    }

    @Test
    public void summaries_normalAndNarrowSizes_wrapLongTextAndRenderEvidence() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = populatedModel();
            MemberDirectorySummary referenceMembers = new MemberDirectorySummary(model.getAddressBook());
            EquipmentDirectorySummary referenceEquipment = new EquipmentDirectorySummary(model.getAddressBook(),
                    model::getAvailability);
            referenceMembers.refresh(Optional.of(new NusId("A0000001X")));
            referenceEquipment.refresh(Optional.of(new UUID(0, 1)));
            render(referenceMembers, "members-reference", 780, 480);
            render(referenceEquipment, "equipment-reference", 780, 480);
            Member longName = member(4, "Long Member Name ".repeat(12).trim());
            model.addMember(longName);
            Equipment longItem = new Equipment(new UUID(0, 4), "Long Equipment Name ".repeat(12),
                    "Photography", Condition.GOOD, "");
            model.addEquipment(longItem);
            MemberDirectorySummary members = new MemberDirectorySummary(model.getAddressBook());
            EquipmentDirectorySummary equipment = new EquipmentDirectorySummary(model.getAddressBook(),
                    model::getAvailability);
            for (int width : new int[] {780, 480, 300}) {
                members.refresh(Optional.of(longName.getNusId()));
                render(members, "members", width, 480);
                equipment.refresh(Optional.of(longItem.getUuid()));
                render(equipment, "equipment", width, 480);
            }
            assertTrue(label(members, "selection").contains(longName.getName().fullName));
            assertTrue(label(equipment, "selection").contains(longItem.getUuid().toString()));
        });
    }

    private static ModelManager populatedModel() {
        ModelManager model = new ModelManager();
        model.addMember(member(1, "Alex Tan"));
        model.addMember(member(2, "Jamie Lim"));
        model.addMember(member(3, "Priya Nair"));
        model.addEquipment(item(1, Condition.GOOD));
        model.addEquipment(item(2, Condition.FAIR));
        model.addEquipment(item(3, Condition.DAMAGED));
        return model;
    }

    private static Member member(int id, String name) {
        return new Member(new NusId("A000000" + id + "X"), new Name(name),
                new Phone("91234567"), new Email("member@example.com"));
    }

    private static Equipment item(int id, Condition condition) {
        return new Equipment(new UUID(0, id), "Camera", "Photography", condition, "");
    }

    private static StackPane mount(Region content, int width, int height) {
        StackPane root = new StackPane(content);
        root.getStyleClass().add("laplace-root");
        root.getStylesheets().add(DirectorySummaryTest.class.getResource("/view/LaplaceTheme.css").toExternalForm());
        new Scene(root, width, height);
        root.resize(width, height);
        root.applyCss();
        root.layout();
        return root;
    }

    private static String label(UiPart<Region> panel, String id) {
        return ((Label) panel.getRoot().lookup("#" + id)).getText();
    }

    private static void assertLabel(UiPart<Region> panel, String id, String expected) {
        assertEquals(expected, label(panel, id));
    }

    private static void render(UiPart<Region> panel, String name, int width, int height) {
        Region root = mount(panel.getRoot(), width, height);
        ScrollPane scroll = (ScrollPane) panel.getRoot();
        Label selection = (Label) scroll.lookup("#selection");
        assertTrue(selection.isWrapText());
        assertTrue(selection.getBoundsInParent().getMaxX() <= scroll.getViewportBounds().getWidth());
        WritableImage image = root.snapshot(null, new WritableImage(width, height));
        int[] pixels = new int[width * height];
        image.getPixelReader().getPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), pixels, 0, width);
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        output.setRGB(0, 0, width, height, pixels, 0, width);
        Path path = Path.of("build", "reports", "laplace", "summary-" + name + "-" + width + ".png");
        try {
            Files.createDirectories(path.getParent());
            assertTrue(ImageIO.write(output, "png", path.toFile()));
        } catch (IOException e) {
            throw new AssertionError("Could not write summary preview", e);
        }
    }
}
