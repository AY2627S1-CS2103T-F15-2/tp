package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.loan.Loan;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.testutil.FxTestUtil;

/** Tests the real FXML panel on the shared FX thread without changing application data. */
public class LoanPanelTest {
    private static final LocalDate ASSIGNED = LocalDate.of(2026, 10, 1);
    private static final UUID FIRST_UUID = UUID.fromString("10000000-0000-0000-0000-000000000000");
    private static final UUID SECOND_UUID = UUID.fromString("90000000-0000-0000-0000-000000000000");
    private static final Member FIRST_MEMBER = member("A0123456X", "Alex Tan");
    private static final Member SECOND_MEMBER = member("A0234567R", "Alex Tan");
    private static final Equipment FIRST_ITEM = new Equipment(FIRST_UUID, "Camera", "Photography",
            Condition.GOOD, "Case included");
    private static final Equipment SECOND_ITEM = new Equipment(SECOND_UUID, "Camera", "Photography",
            Condition.FAIR, "Strap included");

    @Test
    public void constructor_emptyBook_showsZeroCountsAndMeaningfulEmptySections() {
        FxTestUtil.runAndWait(() -> {
            LoanPanel panel = panel(new AddressBook());
            assertCounts(panel, 0, 0);
            Label openEmpty = (Label) section(panel, "openLoans").getChildren().get(0);
            Label historyEmpty = (Label) section(panel, "loanHistory").getChildren().get(0);
            assertEquals("No open loans. Available equipment is ready to assign.",
                    openEmpty.getText());
            assertEquals("No returned loans yet. Completed loans will appear here.",
                    historyEmpty.getText());
        });
    }

    @Test
    public void constructor_mixedLoans_rendersStoredIdentitiesDatesAndStatusInSeparateSections() {
        FxTestUtil.runAndWait(() -> {
            AddressBook data = mixedBook();
            AddressBook before = new AddressBook(data);
            LoanPanel panel = panel(data);
            assertCounts(panel, 2, 3);
            VBox openRow = row(panel, "openLoans", 0);
            assertEquals("Camera\nUUID: " + FIRST_UUID, equipmentButton(openRow).getText());
            assertEquals("Alex Tan\nNUS ID: " + FIRST_MEMBER.getNusId(), memberButton(openRow).getText());
            assertEquals(List.of("Assigned: 2026-10-01", "Expected return: 2026-10-08",
                    "Returned: Not yet returned"), dates(openRow));
            assertEquals("Open loan", ((Label) openRow.lookup(".loan-status")).getText());
            assertTrue(openRow.lookup(".loan-status").getStyleClass().contains("loan-open-status"));

            VBox historyRow = row(panel, "loanHistory", 0);
            assertEquals("Returned", ((Label) historyRow.lookup(".loan-status")).getText());
            assertTrue(historyRow.lookup(".loan-status").getStyleClass().contains("loan-returned-status"));
            assertEquals(List.of("Assigned: 2026-09-21", "Expected return: 2026-09-26",
                    "Returned: 2026-09-29"), dates(historyRow));
            assertEquals(before, data);
        });
    }

    @Test
    public void constructor_sameNames_usesIdentityAndRendersSortedRecords() {
        FxTestUtil.runAndWait(() -> {
            LoanPanel panel = panel(mixedBook());
            assertEquals(List.of(FIRST_UUID, SECOND_UUID), equipmentIds(panel, "openLoans"));
            assertEquals(List.of(FIRST_UUID, SECOND_UUID, FIRST_UUID), equipmentIds(panel, "loanHistory"));
            assertEquals("Alex Tan\nNUS ID: " + SECOND_MEMBER.getNusId(),
                    memberButton(row(panel, "openLoans", 1)).getText());
            assertEquals("Camera\nUUID: " + SECOND_UUID, equipmentButton(row(panel, "openLoans", 1)).getText());
        });
    }

    @Test
    public void identityButtons_userActivation_invokesOnlyMatchingCallbackOnce() {
        FxTestUtil.runAndWait(() -> {
            AddressBook data = mixedBook();
            AddressBook before = new AddressBook(data);
            List<UUID> equipmentSelections = new ArrayList<>();
            List<NusId> memberSelections = new ArrayList<>();
            LoanPanel panel = panel(data, equipmentSelections::add, memberSelections::add);
            assertTrue(equipmentSelections.isEmpty());
            assertTrue(memberSelections.isEmpty());
            equipmentButton(row(panel, "openLoans", 1)).fire();
            assertEquals(List.of(SECOND_UUID), equipmentSelections);
            assertTrue(memberSelections.isEmpty());
            memberButton(row(panel, "loanHistory", 0)).fire();
            assertEquals(List.of(FIRST_MEMBER.getNusId()), memberSelections);
            panel.refresh();
            panel.refresh();
            assertEquals(List.of(SECOND_UUID), equipmentSelections);
            assertEquals(List.of(FIRST_MEMBER.getNusId()), memberSelections);
            assertEquals(before, data);
        });
    }

    @Test
    public void refresh_realReturnAndReassignment_movesRowsAndPreservesHistoryWithoutCallbacks() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = new ModelManager(mixedBook(), new UserPrefs());
            List<UUID> equipmentSelections = new ArrayList<>();
            List<NusId> memberSelections = new ArrayList<>();
            LoanPanel panel = panel(model.getAddressBook(), equipmentSelections::add, memberSelections::add);
            Loan original = model.findOpenLoan(FIRST_UUID).orElseThrow();
            model.closeLoan(FIRST_UUID, ASSIGNED.plusDays(2));
            panel.refresh();
            assertCounts(panel, 1, 4);
            assertEquals(List.of(SECOND_UUID), equipmentIds(panel, "openLoans"));
            assertEquals("Returned: 2026-10-03", dates(row(panel, "loanHistory", 0)).get(2));
            assertTrue(original.isOpen());

            model.addLoan(new Loan(FIRST_UUID, SECOND_MEMBER.getNusId(), ASSIGNED.plusDays(3),
                    ASSIGNED.plusDays(9), null));
            panel.refresh();
            assertCounts(panel, 2, 4);
            assertEquals(List.of(SECOND_UUID, FIRST_UUID), equipmentIds(panel, "openLoans"));
            assertEquals("Alex Tan\nNUS ID: " + SECOND_MEMBER.getNusId(),
                    memberButton(row(panel, "openLoans", 1)).getText());
            assertTrue(equipmentSelections.isEmpty());
            assertTrue(memberSelections.isEmpty());
        });
    }

    @Test
    public void refresh_deletedEquipmentAndClosedHistory_removesStaleRowsAndUpdatesCounts() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = new ModelManager(mixedBook(), new UserPrefs());
            LoanPanel panel = panel(model.getAddressBook());
            model.closeLoan(FIRST_UUID, ASSIGNED);
            model.deleteEquipment(FIRST_ITEM);
            panel.refresh();
            assertCounts(panel, 1, 1);
            assertEquals(List.of(SECOND_UUID), equipmentIds(panel, "openLoans"));
            assertEquals(List.of(SECOND_UUID), equipmentIds(panel, "loanHistory"));
        });
    }

    @Test
    public void refresh_replacedAndEmptyData_resolvesCurrentNamesAndClearsOldRows() {
        FxTestUtil.runAndWait(() -> {
            AddressBook data = mixedBook();
            LoanPanel panel = panel(data);
            AddressBook replacement = new AddressBook();
            replacement.addMember(member(FIRST_MEMBER.getNusId().value, "Jamie Lim"));
            replacement.addEquipment(new Equipment(FIRST_UUID, "Updated Camera", "Photography",
                    Condition.GOOD, ""));
            replacement.addLoan(openLoan(FIRST_UUID, FIRST_MEMBER));
            data.resetData(replacement);
            panel.refresh();
            assertCounts(panel, 1, 0);
            assertEquals("Updated Camera\nUUID: " + FIRST_UUID, equipmentButton(row(panel, "openLoans", 0)).getText());
            assertEquals("Jamie Lim\nNUS ID: " + FIRST_MEMBER.getNusId(),
                    memberButton(row(panel, "openLoans", 0)).getText());
            data.resetData(new AddressBook());
            panel.refresh();
            assertCounts(panel, 0, 0);
            assertTrue(section(panel, "openLoans").getChildren().get(0) instanceof Label);
            assertTrue(section(panel, "loanHistory").getChildren().get(0) instanceof Label);
        });
    }

    @Test
    public void constructor_historyOnly_keepsReturnedRecordAndShowsNoOpenLoans() {
        FxTestUtil.runAndWait(() -> {
            AddressBook data = new AddressBook();
            data.addMember(FIRST_MEMBER);
            data.addEquipment(FIRST_ITEM);
            data.addLoan(new Loan(FIRST_UUID, FIRST_MEMBER.getNusId(), ASSIGNED, ASSIGNED, ASSIGNED));
            LoanPanel panel = panel(data);
            assertCounts(panel, 0, 1);
            assertTrue(section(panel, "openLoans").getChildren().get(0) instanceof Label);
            assertEquals("Returned: 2026-10-01", dates(row(panel, "loanHistory", 0)).get(2));
        });
    }

    @Test
    public void constructor_missingReferences_keepsRecordAndDisablesOnlyMissingIdentityLinks() {
        FxTestUtil.runAndWait(() -> {
            for (boolean missingMember : List.of(false, true)) {
                for (boolean missingEquipment : List.of(false, true)) {
                    List<UUID> equipmentSelections = new ArrayList<>();
                    List<NusId> memberSelections = new ArrayList<>();
                    ReadOnlyAddressBook data = new InconsistentBook(missingMember, missingEquipment);
                    LoanPanel panel = panel(data, equipmentSelections::add, memberSelections::add);
                    assertCounts(panel, 1, 0);
                    VBox openRow = row(panel, "openLoans", 0);
                    Button equipmentLink = equipmentButton(openRow);
                    Button memberLink = memberButton(openRow);
                    assertEquals(missingEquipment, equipmentLink.isDisabled());
                    assertEquals(missingMember, memberLink.isDisabled());
                    assertEquals((missingEquipment ? "Missing equipment reference" : "Camera")
                            + "\nUUID: " + FIRST_UUID, equipmentLink.getText());
                    assertEquals((missingMember ? "Missing member reference" : "Alex Tan")
                            + "\nNUS ID: " + FIRST_MEMBER.getNusId(), memberLink.getText());
                    assertEquals("Open loan", ((Label) openRow.lookup(".loan-status")).getText());
                    equipmentLink.fire();
                    memberLink.fire();
                    assertEquals(missingEquipment ? List.of() : List.of(FIRST_UUID), equipmentSelections);
                    assertEquals(missingMember ? List.of() : List.of(FIRST_MEMBER.getNusId()), memberSelections);
                }
            }
        });
    }

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        FxTestUtil.runAndWait(() -> {
            assertThrows(NullPointerException.class, () -> new LoanPanel(null, id -> {}, id -> {}));
            assertThrows(NullPointerException.class, () -> new LoanPanel(new AddressBook(), null, id -> {}));
            assertThrows(NullPointerException.class, () -> new LoanPanel(new AddressBook(), id -> {}, null));
        });
    }

    @Test
    public void layout_longTextAtNormalAndSmallSizes_wrapsIdentitiesAndUsesVerticalScrolling() {
        FxTestUtil.runAndWait(() -> {
            AddressBook data = new AddressBook();
            Member member = member(FIRST_MEMBER.getNusId().value, "Long Member Name ".repeat(12).trim());
            data.addMember(member);
            data.addEquipment(new Equipment(FIRST_UUID, "A long camera description ".repeat(12),
                    "Photography", Condition.GOOD, ""));
            data.addLoan(openLoan(FIRST_UUID, member));
            for (UUID uuid : List.of(SECOND_UUID, UUID.fromString("a0000000-0000-0000-0000-000000000000"))) {
                data.addEquipment(new Equipment(uuid, "A long camera description ".repeat(12),
                        "Photography", Condition.FAIR, ""));
                data.addLoan(openLoan(uuid, member));
            }
            LoanPanel panel = panel(data);
            Stage stage = new Stage();
            try {
                stage.setScene(panel.getRoot().getScene());
                stage.show();
                for (int[] dimensions : List.of(new int[] {1100, 700}, new int[] {760, 500})) {
                    Region root = panel.getRoot();
                    root.resize(dimensions[0], dimensions[1]);
                    root.applyCss();
                    root.layout();
                    ScrollPane scroll = (ScrollPane) root.lookup(".scroll-pane");
                    assertTrue(scroll.isFitToWidth());
                    assertEquals(ScrollPane.ScrollBarPolicy.NEVER, scroll.getHbarPolicy());
                    VBox openRow = row(panel, "openLoans", 0);
                    Button equipmentLink = equipmentButton(openRow);
                    Button memberLink = memberButton(openRow);
                    assertTrue(equipmentLink.isWrapText());
                    assertTrue(memberLink.isWrapText());
                    assertTrue(equipmentLink.getWidth() <= scroll.getViewportBounds().getWidth());
                    assertTrue(memberLink.getWidth() <= scroll.getViewportBounds().getWidth());
                    assertTrue(equipmentLink.getText().contains(FIRST_UUID.toString()));
                    assertTrue(memberLink.getText().contains(member.getNusId().value));
                    assertTrue(scroll.getContent().getBoundsInLocal().getHeight()
                            > scroll.getViewportBounds().getHeight());
                    writePreview(root, dimensions[0], dimensions[1]);
                }
            } finally {
                stage.close();
            }
        });
    }

    private static LoanPanel panel(ReadOnlyAddressBook data) {
        return panel(data, id -> {}, id -> {});
    }

    private static LoanPanel panel(ReadOnlyAddressBook data, Consumer<UUID> onEquipment, Consumer<NusId> onMember) {
        LoanPanel panel = new LoanPanel(data, onEquipment, onMember);
        Region root = panel.getRoot();
        root.getStyleClass().add("laplace-root");
        Scene scene = new Scene(root, 760, 500);
        var theme = LoanPanelTest.class.getResource("/view/LaplaceTheme.css");
        assertNotNull(theme);
        scene.getStylesheets().add(theme.toExternalForm());
        root.applyCss();
        root.layout();
        assertTrue(root.getStylesheets().stream().anyMatch(url -> url.endsWith("LoanPanel.css")));
        return panel;
    }

    private static void assertCounts(LoanPanel panel, int open, int history) {
        assertEquals(Integer.toString(open), ((Label) panel.getRoot().lookup("#openCount")).getText());
        assertEquals(Integer.toString(history), ((Label) panel.getRoot().lookup("#historyCount")).getText());
    }

    private static VBox section(LoanPanel panel, String name) {
        return (VBox) panel.getRoot().lookup("#" + name);
    }

    private static VBox row(LoanPanel panel, String section, int index) {
        return (VBox) section(panel, section).getChildren().get(index);
    }

    private static Button equipmentButton(VBox row) {
        return (Button) row.lookup(".loan-equipment-link");
    }

    private static Button memberButton(VBox row) {
        return (Button) row.lookup(".loan-member-link");
    }

    private static List<String> dates(VBox row) {
        return row.getChildren().stream().filter(node -> node.getStyleClass().contains("loan-date"))
                .map(node -> ((Label) node).getText()).toList();
    }

    private static List<UUID> equipmentIds(LoanPanel panel, String name) {
        return section(panel, name).getChildren().stream().map(node -> equipmentButton((VBox) node).getText())
                .map(text -> UUID.fromString(text.substring(text.indexOf("UUID: ") + 6))).toList();
    }

    private static AddressBook mixedBook() {
        AddressBook data = new AddressBook();
        data.addMember(FIRST_MEMBER);
        data.addMember(SECOND_MEMBER);
        data.addEquipment(FIRST_ITEM);
        data.addEquipment(SECOND_ITEM);
        data.addLoan(openLoan(SECOND_UUID, SECOND_MEMBER));
        data.addLoan(openLoan(FIRST_UUID, FIRST_MEMBER));
        data.addLoan(new Loan(FIRST_UUID, FIRST_MEMBER.getNusId(), ASSIGNED.minusDays(20),
                ASSIGNED.minusDays(15), ASSIGNED.minusDays(12)));
        data.addLoan(new Loan(SECOND_UUID, SECOND_MEMBER.getNusId(), ASSIGNED.minusDays(10),
                ASSIGNED.minusDays(5), ASSIGNED.minusDays(2)));
        data.addLoan(new Loan(FIRST_UUID, FIRST_MEMBER.getNusId(), ASSIGNED.minusDays(10),
                ASSIGNED.minusDays(5), ASSIGNED.minusDays(2)));
        return data;
    }

    private static Loan openLoan(UUID uuid, Member member) {
        return new Loan(uuid, member.getNusId(), ASSIGNED, ASSIGNED.plusDays(7), null);
    }

    private static Member member(String nusId, String name) {
        return new Member(new NusId(nusId), new Name(name), new Phone("91234567"), new Email("member@example.com"));
    }

    private static void writePreview(Region root, int width, int height) {
        WritableImage image = root.snapshot(null, new WritableImage(width, height));
        int[] pixels = new int[width * height];
        image.getPixelReader().getPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), pixels, 0, width);
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        output.setRGB(0, 0, width, height, pixels, 0, width);
        Path path = Path.of("build", "reports", "laplace", "loans-" + width + "x" + height + ".png");
        try {
            Files.createDirectories(path.getParent());
            assertTrue(ImageIO.write(output, "png", path.toFile()));
        } catch (IOException e) {
            throw new AssertionError("Could not write loan panel preview", e);
        }
    }

    /** Controlled inconsistent references; production AddressBook correctly rejects this state. */
    private static class InconsistentBook implements ReadOnlyAddressBook {
        private final ObservableList<Member> members;
        private final ObservableList<Equipment> equipment;

        InconsistentBook(boolean missingMember, boolean missingEquipment) {
            members = missingMember ? FXCollections.emptyObservableList()
                    : FXCollections.unmodifiableObservableList(FXCollections.observableArrayList(FIRST_MEMBER));
            equipment = missingEquipment ? FXCollections.emptyObservableList()
                    : FXCollections.unmodifiableObservableList(FXCollections.observableArrayList(FIRST_ITEM));
        }

        @Override
        public ObservableList<Person> getPersonList() {
            return FXCollections.emptyObservableList();
        }

        @Override
        public ObservableList<Member> getMemberList() {
            return members;
        }

        @Override
        public ObservableList<Equipment> getEquipmentList() {
            return equipment;
        }

        @Override
        public ObservableList<Loan> getLoanList() {
            return FXCollections.unmodifiableObservableList(FXCollections.observableArrayList(
                    openLoan(FIRST_UUID, FIRST_MEMBER)));
        }
    }
}
