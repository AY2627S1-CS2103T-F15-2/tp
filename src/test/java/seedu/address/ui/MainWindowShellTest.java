package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.imageio.ImageIO;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DisplayRequest;
import seedu.address.logic.commands.EquipmentViewCommand;
import seedu.address.logic.commands.MemberViewCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.equipment.Availability;
import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.loan.Loan;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.FxTestUtil;
import seedu.address.testutil.PersonBuilder;

/** Real shell tests. Future typed results are explicitly injected only where production routes are still pending. */
public class MainWindowShellTest {
    @TempDir
    public Path temporaryDirectory;

    private ModelManager model;
    private RecordingLogic logic;
    private RecordingStorage storage;
    private MainWindow window;
    private final List<String> session = new ArrayList<>();

    @BeforeEach
    public void setUp() {
        FxTestUtil.runAndWait(() -> {
            model = sample();
            storage = new RecordingStorage(temporaryDirectory);
            logic = new RecordingLogic(new LogicManager(model, storage));
            window = new MainWindow(new Stage(), logic, temporaryDirectory.resolve("laplace.json"));
            window.fillInnerParts();
            window.show();
            layout();
        });
    }

    @AfterEach
    public void tearDown() {
        FxTestUtil.runAndWait(() -> {
            window.dispose();
            window.getPrimaryStage().hide();
        });
    }

    @Test
    public void startup_firstSortedMemberAndRepeatedFill_showsSummaryWithoutCommands() {
        FxTestUtil.runAndWait(() -> {
            window.fillInnerParts();
            assertEquals("Laplace", window.getPrimaryStage().getTitle());
            if (Boolean.getBoolean("laplace.packagedShell")) {
                assertTrue(MainWindow.class.getProtectionDomain().getCodeSource().getLocation().getPath()
                        .endsWith("addressbook.jar"));
                for (String resource : List.of("MainWindow.fxml", "CommandBox.fxml", "ResultDisplay.fxml",
                        "LaplaceTheme.css", "MemberListPanel.fxml", "EquipmentListPanel.fxml", "LoanPanel.fxml",
                        "MemberDetailsPanel.fxml", "EquipmentDetailsPanel.fxml", "DetailsPanel.css")) {
                    assertEquals("jar", MainWindow.class.getResource("/view/" + resource).getProtocol());
                }
            }
            assertEquals("Alex Tan", ((Member) rows().getSelectionModel().getSelectedItem()).getName().fullName);
            assertTrue(label("selection").contains("A0000001X"));
            assertTrue(button("membersNavigation").getStyleClass().contains("laplace-navigation-active"));
            assertEquals(1, ((javafx.scene.layout.StackPane) node("commandBoxPlaceholder")).getChildren().size());
            assertTrue(logic.commands.isEmpty());
            assertEquals(0, storage.saves);
            assertTrue(((SplitPane) node("directory")).getDividerPositions()[0] < 0.35);
        });
    }

    @Test
    public void navigation_tabsAndLegacy_useRealRoutesExceptReadOnlyLoans() {
        FxTestUtil.runAndWait(() -> {
            model.addPerson(new PersonBuilder().withName("Legacy Person").build());
            AddressBook before = new AddressBook(model.getAddressBook());
            button("equipmentNavigation").fire();
            assertTrue(node("equipmentNavigation").getStyleClass().contains("laplace-navigation-active"));
            assertEquals("Success", label("feedbackStatus"));
            assertTrue(button("equipmentNavigation").getAccessibleText().contains("current view"));
            button("loansNavigation").fire();
            assertTrue(node("openLoans") != null);
            assertEquals("1", label("openCount"));
            button("membersNavigation").fire();
            MenuButton menu = (MenuButton) window.getPrimaryStage().getScene().getRoot().lookup(".menu-button");
            menu.getItems().stream().filter(item -> item.getText().equals("Legacy persons"))
                    .findFirst().orElseThrow().fire();
            assertTrue(window.getPersonListPanel().getRoot().getParent() != null);
            layout();
            assertEquals(1, ((ListView<?>) node("personListView")).getItems().size());
            assertTrue(node("personListView").lookupAll(".label").stream()
                    .filter(item -> item instanceof Label).map(item -> ((Label) item).getText())
                    .anyMatch(text -> text.contains("Legacy Person")));
            ListCell<?> personCell = node("personListView").lookupAll(".list-cell").stream()
                    .map(item -> (ListCell<?>) item).filter(item -> !item.isEmpty()).findFirst().orElseThrow();
            assertEquals(Color.web("#3C3E3F"), personCell.getBackground().getFills().getFirst().getFill());
            assertEquals(Color.WHITE, ((Label) personCell.getGraphic().lookup("#name")).getTextFill());
            assertFalse(button("membersNavigation").getStyleClass().contains("laplace-navigation-active"));
            assertEquals(List.of("equipment list", "member list", "list"), logic.commands);
            assertEquals(3, storage.saves);
            assertEquals(before, model.getAddressBook());
        });
    }

    @Test
    public void enter_invalidAndBlank_preservesInputViewAndStateThenRecovers() {
        FxTestUtil.runAndWait(() -> {
            AddressBook before = new AddressBook(model.getAddressBook());
            for (String input : List.of("", "   ", "\t")) {
                submit(input);
            }
            assertTrue(logic.commands.isEmpty());
            submit("equipment list unexpected");
            assertEquals("equipment list unexpected", field().getText());
            assertEquals("Error", label("feedbackStatus"));
            assertTrue(button("membersNavigation").getStyleClass().contains("laplace-navigation-active"));
            assertTrue(label("selection").contains("A0000001X"));
            assertEquals(before, model.getAddressBook());
            submit("equipment list");
            assertEquals("", field().getText());
            assertEquals("Success", label("feedbackStatus"));
            assertEquals(2, logic.commands.size());
            assertEquals(1, storage.saves);
        });
    }

    @Test
    public void activation_selectedRowAndLoanLinks_executeExactIdentityOnce() {
        FxTestUtil.runAndWait(() -> {
            ListCell<?> selected = rows().lookupAll(".list-cell").stream().map(node -> (ListCell<?>) node)
                    .filter(ListCell::isSelected).findFirst().orElseThrow();
            selected.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED, 5, 5, 5, 5, MouseButton.PRIMARY, 1,
                    false, false, false, false, false, false, false, false, false, true, null));
            rows().fireEvent(key(KeyCode.ENTER));
            assertEquals(List.of("member view nus/A0000001X", "member view nus/A0000001X"), logic.commands);
            assertEquals("Error", label("feedbackStatus"));
            assertTrue(feedback().contains("not implemented"));
            assertTrue(button("membersNavigation").getStyleClass().contains("laplace-navigation-active"));
            button("loansNavigation").fire();
            List<Button> links = node("openLoans").lookupAll(".button").stream()
                    .map(node -> (Button) node).toList();
            assertEquals(2, links.size());
            for (Button link : links) {
                link.fire();
            }
            assertEquals(4, logic.commands.size());
            assertTrue(logic.commands.contains("equipment view uuid/" + new UUID(0, 1)));
            assertEquals("Error", label("feedbackStatus"));
            assertTrue(node("openLoans") != null);
        });
    }

    @Test
    public void activation_equipmentSpaceAndSelectionEvents_updatesSummaryAndRealTypedDetailsOnce() {
        FxTestUtil.runAndWait(() -> {
            button("equipmentNavigation").fire();
            rows().getSelectionModel().select(1);
            rows().fireEvent(new KeyEvent(KeyEvent.KEY_RELEASED, "", "", KeyCode.DOWN,
                    false, false, false, false));
            assertTrue(label("selection").contains(new UUID(0, 2).toString()));
            assertEquals(List.of("equipment list"), logic.commands);
            assertDoesNotThrow(() -> {
                logic.nextResult = new EquipmentViewCommand(new UUID(0, 2)).execute(model);
            });
            rows().fireEvent(key(KeyCode.SPACE));
            assertEquals(List.of("equipment list", "equipment view uuid/" + new UUID(0, 2)), logic.commands);
            assertEquals(new UUID(0, 2).toString(), detailLabel("equipmentUuid"));
            assertFalse(node("featureNotice").isVisible());
            button("membersNavigation").fire();
            rows().getSelectionModel().select(1);
            Region directoryList = (Region) node("directoryListPlaceholder");
            MouseEvent released = new MouseEvent(MouseEvent.MOUSE_RELEASED, 5, 5, 5, 5, MouseButton.PRIMARY, 1,
                    false, false, false, false, false, false, false, false, false, true, null);
            directoryList.getChildrenUnmodifiable().getFirst().fireEvent(released);
            assertTrue(label("selection").contains("A0000002X"));
            assertEquals(3, logic.commands.size());
            window.getPrimaryStage().hide();
            model.setAddressBook(new AddressBook());
        });
        FxTestUtil.runAndWait(() -> assertEquals(3, rows().getItems().size()));
    }

    @Test
    public void displayRequests_typedAndUnchanged_useIntentInsteadOfFeedback() {
        FxTestUtil.runAndWait(() -> {
            logic.nextResult = new CommandResult("member list", DisplayRequest.equipment());
            submit("controlled typed result");
            assertTrue(button("equipmentNavigation").getStyleClass().contains("laplace-navigation-active"));
            logic.nextResult = new CommandResult("equipment list", DisplayRequest.unchanged());
            submit("controlled unchanged result");
            assertTrue(button("equipmentNavigation").getStyleClass().contains("laplace-navigation-active"));
            logic.nextResult = new CommandResult("Requested member", DisplayRequest.memberDetails(id(2)));
            submit("controlled detail result");
            assertEquals("A0000002X", label("memberNusId"));
            assertEquals(id(2), ((Member) rows().getSelectionModel().getSelectedItem()).getNusId());
            assertFalse(node("featureNotice").isVisible());
            logic.nextResult = new CommandResult("Requested equipment",
                    DisplayRequest.equipmentDetails(new UUID(0, 2)));
            submit("controlled equipment detail result");
            assertEquals(new UUID(0, 2).toString(), label("equipmentUuid"));
            assertEquals(new UUID(0, 2), ((Equipment) rows().getSelectionModel().getSelectedItem()).getUuid());
            assertFalse(node("featureNotice").isVisible());
            logic.nextResult = new CommandResult("Requested loans", DisplayRequest.loans());
            submit("controlled loans result");
            assertTrue(node("openLoans") != null);
            assertFalse(node("featureNotice").isVisible());
        });
    }

    @Test
    public void helpAndExit_precedence_preservesNavigationAndDisposesOnExit() {
        FxTestUtil.runAndWait(() -> {
            submit("equipment list");
            submit("help");
            assertTrue(button("equipmentNavigation").getStyleClass().contains("laplace-navigation-active"));
            field().fireEvent(key(KeyCode.F1));
            assertEquals(2, logic.commands.size());
            assertEquals(2, Stage.getWindows().stream().filter(javafx.stage.Window::isShowing).count());
            submit("exit");
            assertFalse(window.getPrimaryStage().isShowing());
            assertEquals(0, Stage.getWindows().stream().filter(javafx.stage.Window::isShowing).count());
            assertEquals((int) window.getPrimaryStage().getX(), model.getGuiSettings().getWindowCoordinates().x);
        });
    }

    @Test
    public void refresh_loanOnlyChange_updatesRealCardsSummariesAndLoanRowsSilently() {
        FxTestUtil.runAndWait(() -> model.closeLoan(new UUID(0, 1), LocalDate.of(2026, 1, 2)));
        FxTestUtil.runAndWait(() -> {
            layout();
            Region card = (Region) rows().lookupAll(".list-cell").stream().map(node -> (ListCell<?>) node)
                    .filter(ListCell::isSelected).findFirst().orElseThrow().getGraphic();
            assertEquals("0 items held", ((Label) card.lookup("#itemsHeld")).getText());
            button("equipmentNavigation").fire();
            assertEquals("2", label("availableCount"));
            layout();
            card = (Region) rows().lookupAll(".list-cell").stream().map(node -> (ListCell<?>) node)
                    .filter(ListCell::isSelected).findFirst().orElseThrow().getGraphic();
            assertEquals("No current holder", ((Label) card.lookup("#holder")).getText());
            button("loansNavigation").fire();
            assertEquals("0", label("openCount"));
            assertEquals("1", label("historyCount"));
            assertEquals(List.of("equipment list"), logic.commands);
        });
    }

    @Test
    public void refresh_explicitlyEmptySelection_preservesAbsenceAcrossDataChanges() {
        FxTestUtil.runAndWait(() -> {
            rows().getSelectionModel().clearSelection();
            model.closeLoan(new UUID(0, 1), LocalDate.of(2026, 1, 2));
        });
        FxTestUtil.runAndWait(() -> {
            assertEquals(null, rows().getSelectionModel().getSelectedItem());
            assertEquals("No member selected", label("selection"));
            assertTrue(logic.commands.isEmpty());
        });
    }

    @Test
    public void refresh_deletedDetailSelectionAndReset_recoversNextThenEmptyWithoutCommands() {
        FxTestUtil.runAndWait(() -> {
            logic.nextResult = new CommandResult("Controlled target", DisplayRequest.memberDetails(id(2)));
            submit("controlled detail result");
            model.deleteMember(model.findMember(id(2)).orElseThrow());
        });
        FxTestUtil.runAndWait(() -> {
            assertTrue(label("selection").contains("A0000003X"));
            assertFalse(node("featureNotice").isVisible());
            model.setAddressBook(new AddressBook());
        });
        FxTestUtil.runAndWait(() -> {
            assertTrue(rows().getItems().isEmpty());
            assertEquals("No member selected", label("selection"));
            assertEquals(1, logic.commands.size());
        });
    }

    @Test
    public void refresh_identityAcrossReplacement_retainsSelectionAndRejectsStaleDetails() {
        FxTestUtil.runAndWait(() -> {
            logic.nextResult = new CommandResult("Controlled target", DisplayRequest.equipmentDetails(new UUID(0, 2)));
            submit("controlled detail result");
            ModelManager replacement = sample();
            replacement.deleteEquipment(replacement.findEquipment(new UUID(0, 2)).orElseThrow());
            replacement.addEquipment(new Equipment(new UUID(0, 2), "Renamed Camera", "Photography",
                    Condition.GOOD, ""));
            model.setAddressBook(replacement.getAddressBook());
        });
        FxTestUtil.runAndWait(() -> {
            assertEquals("Renamed Camera", label("equipmentName"));
            assertEquals(new UUID(0, 2).toString(), label("equipmentUuid"));
            assertFalse(node("featureNotice").isVisible());
            model.deleteEquipment(model.findEquipment(new UUID(0, 2)).orElseThrow());
        });
        FxTestUtil.runAndWait(() -> {
            assertFalse(node("featureNotice").isVisible());
            assertFalse(label("selection").contains(new UUID(0, 2).toString()));
            assertEquals(1, logic.commands.size());
        });
    }

    @Test
    public void details_realViewResults_showContactsNotesDatesAndSameNameIdentities() {
        FxTestUtil.runAndWait(() -> {
            model.deleteMember(model.findMember(id(2)).orElseThrow());
            model.addMember(new Member(id(2), new Name("Alex Tan"), new Phone("98765432"),
                    new Email("other@example.com")));
            model.deleteEquipment(model.findEquipment(new UUID(0, 2)).orElseThrow());
            model.addEquipment(new Equipment(new UUID(0, 2), "Camera 1", "Photography", Condition.GOOD,
                    "Keep the lens cap on.\nCase included."));
            model.addLoan(new Loan(new UUID(0, 2), id(1), LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 1, 3), null));
            showMemberDetails(id(1));
            assertEquals("A0000001X", detailLabel("memberNusId"));
            assertEquals("91234567", detailLabel("phone"));
            assertEquals("member1@example.com", detailLabel("email"));
            assertEquals("2 item(s) held", detailLabel("heldCount"));
            assertEquals(2, node("directorySummaryPlaceholder").lookupAll("#equipmentUuid").size());
            showMemberDetails(id(2));
            assertEquals("Alex Tan", detailLabel("memberName"));
            assertEquals("A0000002X", detailLabel("memberNusId"));
            assertEquals("98765432", detailLabel("phone"));
            assertEquals("other@example.com", detailLabel("email"));
            assertEquals("0 item(s) held", detailLabel("heldCount"));
            showEquipmentDetails(new UUID(0, 2));
            assertEquals(new UUID(0, 2).toString(), detailLabel("equipmentUuid"));
            assertEquals("Keep the lens cap on.\nCase included.", detailLabel("notes"));
            assertEquals("ASSIGNED", detailLabel("availability"));
            assertEquals("Alex Tan", detailLabel("holder"));
            assertEquals("A0000001X", detailLabel("holderNusId"));
            assertEquals("91234567", detailLabel("holderPhone"));
            assertEquals("member1@example.com", detailLabel("holderEmail"));
            assertEquals("2026-01-01", detailLabel("assignedDate"));
            assertEquals("2026-01-03", detailLabel("expectedReturnDate"));
            showEquipmentDetails(new UUID(0, 1));
            assertEquals(new UUID(0, 1).toString(), detailLabel("equipmentUuid"));
            assertEquals("2026-01-02", detailLabel("expectedReturnDate"));
            assertEquals(0, storage.saves);
        });
    }

    @Test
    public void details_loanChangesOnEachActiveView_refreshesWithoutActivationCommands() {
        for (DisplayRequest.Type active : List.of(DisplayRequest.Type.MEMBER_DETAILS,
                DisplayRequest.Type.EQUIPMENT_DETAILS, DisplayRequest.Type.LOANS)) {
            int[] dispatchCount = new int[1];
            FxTestUtil.runAndWait(() -> {
                model.setAddressBook(sample().getAddressBook());
                if (active == DisplayRequest.Type.MEMBER_DETAILS) {
                    showMemberDetails(id(1));
                } else if (active == DisplayRequest.Type.EQUIPMENT_DETAILS) {
                    showEquipmentDetails(new UUID(0, 1));
                } else {
                    button("loansNavigation").fire();
                }
                dispatchCount[0] = logic.commands.size();
                model.addLoan(new Loan(new UUID(0, 2), id(1), LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 1, 3), null));
            });
            FxTestUtil.runAndWait(() -> {
                if (active == DisplayRequest.Type.MEMBER_DETAILS) {
                    assertEquals("2 item(s) held", detailLabel("heldCount"));
                    assertEquals(2, node("directorySummaryPlaceholder").lookupAll("#equipmentUuid").size());
                } else if (active == DisplayRequest.Type.EQUIPMENT_DETAILS) {
                    assertEquals("ASSIGNED", detailLabel("availability"));
                    assertEquals("A0000001X", detailLabel("holderNusId"));
                } else {
                    assertEquals("2", label("openCount"));
                }
                assertEquals(dispatchCount[0], logic.commands.size());
                model.closeLoan(new UUID(0, 1), LocalDate.of(2026, 1, 2));
            });
            FxTestUtil.runAndWait(() -> {
                if (active == DisplayRequest.Type.MEMBER_DETAILS) {
                    assertEquals("1 item(s) held", detailLabel("heldCount"));
                    assertEquals(new UUID(0, 2).toString(), detailLabel("equipmentUuid"));
                    assertEquals("2026-01-03", detailLabel("expectedReturnDate"));
                } else if (active == DisplayRequest.Type.EQUIPMENT_DETAILS) {
                    assertEquals("AVAILABLE", detailLabel("availability"));
                    assertEquals("No current holder", detailLabel("holder"));
                    assertEquals(null, node("directorySummaryPlaceholder").lookup("#holderEmail"));
                    assertEquals(null, node("directorySummaryPlaceholder").lookup("#assignedDate"));
                } else {
                    assertEquals("1", label("openCount"));
                    assertEquals("1", label("historyCount"));
                }
                assertEquals(dispatchCount[0], logic.commands.size());
                model.closeLoan(new UUID(0, 2), LocalDate.of(2026, 1, 3));
            });
            FxTestUtil.runAndWait(() -> {
                if (active == DisplayRequest.Type.MEMBER_DETAILS) {
                    assertEquals("0 item(s) held", detailLabel("heldCount"));
                } else if (active == DisplayRequest.Type.LOANS) {
                    assertEquals("0", label("openCount"));
                    assertEquals("2", label("historyCount"));
                }
                assertEquals(dispatchCount[0], logic.commands.size());
            });
        }
    }

    @Test
    public void details_missingIdentityResetAndDisposal_showsMissingThenDirectoryAndDetaches() {
        FxTestUtil.runAndWait(() -> {
            logic.nextResult = new CommandResult("Controlled missing member", DisplayRequest.memberDetails(id(9)));
            submit("controlled missing target");
            assertTrue(detailLabel("detailsStatus").contains("no longer exists"));
            showEquipmentDetails(new UUID(0, 1));
            model.setAddressBook(new AddressBook());
        });
        FxTestUtil.runAndWait(() -> {
            assertEquals(null, node("detailsTitle"));
            assertTrue(rows().getItems().isEmpty());
            assertEquals("No equipment selected", label("selection"));
            model.setAddressBook(sample().getAddressBook());
            showMemberDetails(id(1));
            model.closeLoan(new UUID(0, 1), LocalDate.of(2026, 1, 2));
            window.dispose();
            model.setAddressBook(new AddressBook());
        });
        FxTestUtil.runAndWait(() -> {
            assertEquals("1 item(s) held", detailLabel("heldCount"));
            assertEquals("A0000001X", detailLabel("memberNusId"));
        });
    }

    @Test
    public void settings_savedOnscreenCoordinates_retainsPositionAndDimensions() {
        FxTestUtil.runAndWait(() -> {
            window.dispose();
            window.getPrimaryStage().hide();
            int x = (int) Screen.getPrimary().getVisualBounds().getMinX() + 20;
            int y = (int) Screen.getPrimary().getVisualBounds().getMinY() + 20;
            model.setGuiSettings(new GuiSettings(850, 550, x, y));
            window = new MainWindow(new Stage(), logic, temporaryDirectory.resolve("laplace.json"));
            window.fillInnerParts();
            assertEquals(x, window.getPrimaryStage().getX(), 1);
            assertEquals(y, window.getPrimaryStage().getY(), 1);
            assertEquals(Math.min(850, Screen.getPrimary().getVisualBounds().getWidth()),
                    window.getPrimaryStage().getWidth(), 1);
            window.show();
        });
    }

    @Test
    public void storageFailure_realLegacyMutation_reportsFailureAndRefreshesActualMemory() {
        FxTestUtil.runAndWait(() -> {
            storage.fail = true;
            submit("add n/Test Person p/91234567 e/test@example.com a/Test Address");
            assertEquals("Error", label("feedbackStatus"));
            assertTrue(feedback().contains("Could not save data"));
            assertTrue(field().getText().startsWith("add"));
            assertTrue(button("membersNavigation").getStyleClass().contains("laplace-navigation-active"));
            assertEquals(1, model.getFilteredPersonList().size());
            assertFalse(Files.exists(temporaryDirectory.resolve("laplace.json")));
            button("equipmentNavigation").fire();
            assertTrue(button("membersNavigation").getStyleClass().contains("laplace-navigation-active"));
        });
        FxTestUtil.runAndWait(() -> {
            assertEquals(1, ((ListView<?>) window.getPersonListPanel().getRoot().lookup("#personListView"))
                    .getItems().size());
            storage.fail = false;
            submit("list");
            assertTrue(Files.exists(temporaryDirectory.resolve("laplace.json")));
        });
    }

    @Test
    public void dispose_queuedAndLaterChanges_detachesAndDoesNotRefreshOldViews() {
        FxTestUtil.runAndWait(() -> {
            model.closeLoan(new UUID(0, 1), LocalDate.of(2026, 1, 2));
            window.dispose();
            window.dispose();
            model.setAddressBook(new AddressBook());
        });
        FxTestUtil.runAndWait(() -> {
            assertEquals(3, rows().getItems().size());
            assertEquals("3 ACTIVE", label("countBadge"));
            assertTrue(logic.commands.isEmpty());
        });
    }

    @Test
    public void layout_actualShellViewsAndErrors_rendersNormalSmallEmptyAndLongPreviews() {
        FxTestUtil.runAndWait(() -> {
            model.addPerson(new PersonBuilder().withName("Legacy Person").build());
            for (int[] size : new int[][] {{1100, 700}, {760, 500}}) {
                render("members", size[0], size[1]);
                button("equipmentNavigation").fire();
                render("equipment", size[0], size[1]);
                logic.nextResult = new CommandResult("Controlled equipment details",
                        DisplayRequest.equipmentDetails(new UUID(0, 1)));
                submit("controlled equipment details");
                render("equipment-details", size[0], size[1]);
                logic.nextResult = new CommandResult("Controlled member details", DisplayRequest.memberDetails(id(1)));
                submit("controlled member details");
                render("member-details", size[0], size[1]);
                button("loansNavigation").fire();
                render("loans", size[0], size[1]);
                submit("list");
                render("legacy", size[0], size[1]);
                button("membersNavigation").fire();
            }
            submit("invalid ".repeat(100));
            render("error", 760, 500);
            model.setAddressBook(new AddressBook());
        });
        FxTestUtil.runAndWait(() -> {
            render("empty", 760, 500);
            model.addMember(new Member(id(1), new Name("Long Member Name ".repeat(16).trim()),
                    new Phone("91234567"), new Email("verylongmemberemailaddress@example.com")));
            model.addEquipment(new Equipment(new UUID(0, 1), "Long Equipment Name ".repeat(16),
                    "Long Category ".repeat(8), Condition.GOOD, ""));
        });
        FxTestUtil.runAndWait(() -> {
            render("members-long", 760, 500);
            button("equipmentNavigation").fire();
            render("equipment-long", 760, 500);
            logic.nextResult = new CommandResult("Controlled long equipment details",
                    DisplayRequest.equipmentDetails(new UUID(0, 1)));
            submit("controlled equipment details");
            render("equipment-details-long", 760, 500);
            logic.nextResult = new CommandResult("Controlled long member details", DisplayRequest.memberDetails(id(1)));
            submit("controlled member details");
            render("member-details-long", 760, 500);
            logic.nextResult = new CommandResult("Controlled missing details", DisplayRequest.memberDetails(id(9)));
            submit("controlled missing details");
            assertTrue(label("detailsStatus").contains("no longer exists"));
            render("member-details-missing", 760, 500);
        });
    }

    @Test
    public void settings_savedDimensionsAndOffscreenLocation_clampsToUsableScreen() {
        FxTestUtil.runAndWait(() -> {
            window.dispose();
            window.getPrimaryStage().hide();
            model.setGuiSettings(new GuiSettings(900, 650, -100000, -100000));
            window = new MainWindow(new Stage(), logic, temporaryDirectory.resolve("laplace.json"));
            window.fillInnerParts();
            window.show();
            assertEquals(Math.min(900, Screen.getPrimary().getVisualBounds().getWidth()),
                    window.getPrimaryStage().getWidth(), 2);
            assertEquals(Math.min(650, Screen.getPrimary().getVisualBounds().getHeight()),
                    window.getPrimaryStage().getHeight(), 2);
            assertTrue(window.getPrimaryStage().getX() >= Screen.getPrimary().getVisualBounds().getMinX());
            assertTrue(window.getPrimaryStage().getY() >= Screen.getPrimary().getVisualBounds().getMinY());
        });
    }

    @Test
    public void reload_savedData_rebuildsInitialSelectionFromRealJson() throws Exception {
        FxTestUtil.runAndWait(() -> submit("member list"));
        ReadOnlyAddressBook restored = storage.readAddressBook().orElseThrow();
        FxTestUtil.runAndWait(() -> {
            window.dispose();
            window.getPrimaryStage().hide();
            model.setAddressBook(restored);
            logic.commands.clear();
            window = new MainWindow(new Stage(), logic, temporaryDirectory.resolve("laplace.json"));
            window.fillInnerParts();
            window.show();
            assertTrue(label("selection").contains("A0000001X"));
            assertEquals("3 ACTIVE", label("countBadge"));
            assertTrue(logic.commands.isEmpty());
        });
    }

    @Test
    public void session_actualInputAndFeedback_recordsRealParserAndStorageOutcomes() throws IOException {
        FxTestUtil.runAndWait(() -> {
            session.add("JavaFX shell; actual LogicManager, ModelManager and JSON storage.");
            session.add("Startup: Members, first sorted member selected; no command dispatched.");
            for (String input : List.of("   ", "member list", "equipment list extra", "equipment list",
                    "member view nus/A0000001X", "list", "member list")) {
                submit(input);
                session.add("INPUT: " + (input.isBlank() ? "<whitespace only>" : input));
                session.add("STATUS: " + label("feedbackStatus"));
                session.add("OUTPUT: " + feedback());
                session.add("RETAINED INPUT: " + field().getText());
            }
            button("loansNavigation").fire();
            session.add("Loans tab: " + label("openCount") + " open; " + label("historyCount") + " history.");
            session.add("Commands executed: " + logic.commands);
            session.add("Successful saves: " + storage.saves);
            assertEquals(6, logic.commands.size());
            assertEquals(5, storage.saves);
        });
        Path path = Path.of("build", "reports", "laplace", "shell-session.txt");
        Files.createDirectories(path.getParent());
        Files.write(path, session);
    }

    /** Exercises the real component command/result without claiming an activated family route or autosave. */
    private void showMemberDetails(NusId identity) {
        try {
            logic.nextResult = new MemberViewCommand(identity).execute(model);
            submit("member view nus/" + identity.value);
        } catch (CommandException e) {
            throw new AssertionError("Fixture member must resolve through the actual view command", e);
        }
    }

    /** Exercises the actual equipment view command before giving its result to the shell boundary. */
    private void showEquipmentDetails(UUID identity) {
        try {
            logic.nextResult = new EquipmentViewCommand(identity).execute(model);
            submit("equipment view uuid/" + identity);
        } catch (CommandException e) {
            throw new AssertionError("Fixture equipment must resolve through the actual view command", e);
        }
    }

    private String detailLabel(String id) {
        return ((Label) node("directorySummaryPlaceholder").lookup("#" + id)).getText();
    }

    private void submit(String input) {
        field().setText(input);
        field().fireEvent(new ActionEvent());
    }

    private TextField field() {
        return (TextField) node("commandTextField");
    }

    private String feedback() {
        return ((TextArea) node("resultDisplay")).getText();
    }

    private Button button(String id) {
        return (Button) node(id);
    }

    private String label(String id) {
        return ((Label) node(id)).getText();
    }

    private Node node(String id) {
        layout();
        return window.getPrimaryStage().getScene().getRoot().lookup("#" + id);
    }

    private ListView<?> rows() {
        return (ListView<?>) node("rows");
    }

    private void layout() {
        window.getPrimaryStage().getScene().getRoot().applyCss();
        Region root = (Region) window.getPrimaryStage().getScene().getRoot();
        root.layout();
    }

    /** Snapshots the actual shell root, including all FXML-mounted components, at the requested content size. */
    private void render(String name, int width, int height) {
        Region root = (Region) window.getPrimaryStage().getScene().getRoot();
        root.resize(width, height);
        layout();
        assertTrue(node("commandTextField").localToScene(node("commandTextField").getBoundsInLocal()).getMaxY()
                <= height);
        assertTrue(node("commandTextField").localToScene(node("commandTextField").getBoundsInLocal()).getMaxX()
                <= width);
        if (node("directory").isVisible()) {
            assertEquals(0.28, ((SplitPane) node("directory")).getDividerPositions()[0], 0.02);
        }
        WritableImage image = root.snapshot(null, new WritableImage(width, height));
        int[] pixels = new int[width * height];
        image.getPixelReader().getPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), pixels, 0, width);
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        output.setRGB(0, 0, width, height, pixels, 0, width);
        Path path = Path.of("build", "reports", "laplace", "shell-" + name + "-" + width + "x" + height + ".png");
        try {
            Files.createDirectories(path.getParent());
            assertTrue(ImageIO.write(output, "png", path.toFile()));
        } catch (IOException e) {
            throw new AssertionError("Could not write shell preview", e);
        }
    }

    private static NusId id(int index) {
        return new NusId("A000000" + index + "X");
    }

    private static KeyEvent key(KeyCode code) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, false);
    }

    private static ModelManager sample() {
        ModelManager result = new ModelManager();
        String[] names = {"Alex Tan", "Jamie Lim", "Priya Nair"};
        for (int index : new int[] {3, 2, 1}) {
            result.addMember(new Member(id(index), new Name(names[index - 1]), new Phone("91234567"),
                    new Email("member" + index + "@example.com")));
            result.addEquipment(new Equipment(new UUID(0, index), "Camera " + index, "Photography",
                    index == 3 ? Condition.DAMAGED : Condition.GOOD, ""));
        }
        LocalDate date = LocalDate.of(2026, 1, 1);
        result.addLoan(new Loan(new UUID(0, 1), id(1), date, date.plusDays(1), null));
        return result;
    }

    /** Uses real disk storage unless the test deliberately injects a save failure. */
    private static class RecordingStorage extends StorageManager {
        private int saves;
        private boolean fail;

        RecordingStorage(Path directory) {
            super(new JsonAddressBookStorage(directory.resolve("laplace.json")),
                    new JsonUserPrefsStorage(directory.resolve("preferences.json")));
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
            if (fail) {
                throw new IOException("Controlled disk failure");
            }
            super.saveAddressBook(addressBook);
            saves++;
        }
    }

    /** Records calls while delegating to real logic; nextResult is solely a typed-result contract fixture. */
    private static class RecordingLogic implements Logic {
        private final Logic delegate;
        private final List<String> commands = new ArrayList<>();
        private CommandResult nextResult;

        RecordingLogic(Logic delegate) {
            this.delegate = delegate;
        }

        @Override
        public CommandResult execute(String command) throws CommandException, ParseException {
            commands.add(command);
            if (nextResult != null) {
                CommandResult result = nextResult;
                nextResult = null;
                return result;
            }
            return delegate.execute(command);
        }

        @Override
        public ObservableList<Person> getFilteredPersonList() {
            return delegate.getFilteredPersonList();
        }

        @Override
        public ReadOnlyAddressBook getAddressBook() {
            return delegate.getAddressBook();
        }

        @Override
        public Availability getAvailability(UUID uuid) {
            return delegate.getAvailability(uuid);
        }

        @Override
        public GuiSettings getGuiSettings() {
            return delegate.getGuiSettings();
        }

        @Override
        public void setGuiSettings(GuiSettings settings) {
            delegate.setGuiSettings(settings);
        }
    }
}
