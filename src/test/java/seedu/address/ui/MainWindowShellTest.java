package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
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
import javafx.scene.control.Labeled;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Screen;
import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DisplayRequest;
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
import seedu.address.model.query.InventoryGroups;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.FxTestUtil;
import seedu.address.testutil.PersonBuilder;

/** Real routed shell/storage tests; controlled results separately exercise the typed display contract. */
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
            // UiManager shows the stage before mounting components; preserve that production order.
            window.show();
            window.fillInnerParts();
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
    public void contextActions_prepareCommandsAndFollowIdentityWhileResetIsExplicit() {
        FxTestUtil.runAndWait(() -> {
            submit("member search q/alex");
            submit("member view nus/" + id(1));
            TitledPane commands = (TitledPane) node("directorySummaryPlaceholder").lookup(".titled-pane");
            commands.setAnimated(false);
            commands.setExpanded(true);
            layout();
            int executed = logic.commands.size();
            action("Use command").fire();
            assertTrue(field().getText().startsWith("member edit nus/" + id(1)));
            assertEquals(executed, logic.commands.size());
            action("View equipment").fire();
            assertEquals(new UUID(0, 1).toString(), detailLabel("equipmentUuid"));
            commands = (TitledPane) node("directorySummaryPlaceholder").lookup(".titled-pane");
            commands.setAnimated(false);
            commands.setExpanded(true);
            layout();
            executed = logic.commands.size();
            action("Use command").fire();
            assertTrue(field().getText().startsWith("equipment edit uuid/" + new UUID(0, 1)));
            assertEquals(executed, logic.commands.size());
            action("View member").fire();
            assertEquals(id(1).value, detailLabel("memberNusId"));
            action("Back to results").fire();
            assertTrue(label("queryContext").contains("alex"));
            action("Reset search").fire();
            assertEquals("Filters: All records", label("queryContext"));
            submit("equipment search q/Camera 1");
            rows().fireEvent(key(KeyCode.ENTER));
            action("Back to results").fire();
            assertTrue(label("queryContext").contains("camera 1"));
            action("Reset search").fire();
            assertEquals("Filters: All records", label("queryContext"));
        });
    }

    private Button action(String caption) {
        return window.getPrimaryStage().getScene().getRoot().lookupAll(".button").stream()
                .map(node -> (Button) node).filter(button -> button.getText().equals(caption))
                .findFirst().orElseThrow();
    }

    @Test
    public void appearance_commandsAndModeButton_preserveFiltersDataAndSavedPreference() throws Exception {
        FxTestUtil.runAndWait(() -> {
            submit("equipment search availability/available");
            String criteria = label("queryContext");
            int saves = storage.saves;
            submit("mode dark");
            assertTrue(model.getGuiSettings().isDarkMode());
            assertTrue(window.getPrimaryStage().getScene().getRoot().getStyleClass().contains("dark"));
            assertEquals(criteria, label("queryContext"));
            assertEquals(saves, storage.saves);
            assertEquals(Color.web("#0d1117"), ((Region) window.getPrimaryStage().getScene().getRoot())
                    .getBackground().getFills().getFirst().getFill());
            render("dark-equipment", 1100, 700);
            submit("equipment view uuid/" + new UUID(0, 1));
            render("dark-unit", 760, 500);
            button("loansNavigation").fire();
            render("dark-loans", 1100, 700);
            window.handleHelp();
            Stage help = Stage.getWindows().stream().filter(stage -> stage instanceof Stage
                    && ((Stage) stage).getTitle().equals("Command reference")).map(stage -> (Stage) stage)
                    .findFirst().orElseThrow();
            assertTrue(help.getScene().getRoot().getStyleClass().contains("dark"));
            Region helpRoot = (Region) help.getScene().getRoot();
            helpRoot.resize(720, 600);
            helpRoot.applyCss();
            helpRoot.layout();
            writeSnapshot(helpRoot, "dark-help", 720, 600);
            assertEquals("Light mode", button("modeButton").getText());
            button("modeButton").fire();
            assertFalse(model.getGuiSettings().isDarkMode());
            assertEquals("Dark mode", button("modeButton").getText());
            assertFalse(help.getScene().getRoot().getStyleClass().contains("dark"));
            button("modeButton").fire();
            assertTrue(model.getGuiSettings().isDarkMode());
            assertEquals("Light mode", button("modeButton").getText());
            submit("exit");
            assertTrue(model.getGuiSettings().isDarkMode());
        });
        JsonUserPrefsStorage prefs = new JsonUserPrefsStorage(temporaryDirectory.resolve("appearance.json"));
        prefs.saveUserPrefs(model.getUserPrefs());
        assertTrue(prefs.readUserPrefs().orElseThrow().getGuiSettings().isDarkMode());
    }

    @Test
    public void equipmentBatchAdd_resetsSearchAndShowsAllUnitsAfterOneSave() {
        FxTestUtil.runAndWait(() -> {
            int originalCount = model.getEquipmentList().size();
            submit("equipment search q/NoMatchingBatch");
            assertEquals(0, rows().getItems().size());
            succeed("equipment add name/Batch Camera category/Photo condition/GOOD count/3");
            assertEquals(originalCount + 1, rows().getItems().size());
            assertEquals(originalCount + 3, model.getEquipmentList().size());
            assertTrue(button("equipmentNavigation").getStyleClass().contains("laplace-navigation-active"));
            assertTrue(feedback().startsWith("Added 3 equipment units: Batch Camera"));
            assertEquals(1, storage.saves);
            rows().getSelectionModel().selectFirst();
            rows().fireEvent(key(KeyCode.ENTER));
            assertEquals("Batch Camera", label("groupTitle"));
            javafx.scene.control.TableView<?> units = (javafx.scene.control.TableView<?>) node("physicalUnits");
            assertEquals(3, units.getItems().size());
            render("grouped-equipment", 1280, 720);
            units.getSelectionModel().selectFirst();
            render("grouped-unit", 1280, 720);
            render("grouped-unit", 760, 500);
            render("grouped-unit", 853, 440);
        });
    }

    @Test
    public void search_realCommandInput_filtersSortsRetainsCriteriaAndResetsWithList() {
        FxTestUtil.runAndWait(() -> {
            int saves = storage.saves;
            submit("member search q/alex holding/yes sort/id order/desc");
            assertEquals("Success", feedbackStatus());
            assertEquals(1, rows().getItems().size());
            assertEquals("1", detailLabel("displayedCount"));
            assertTrue(detailLabel("criteria").contains("alex"));
            assertTrue(detailLabel("sortOrder").contains("descending"));
            assertEquals(saves, storage.saves);
            submit("member view nus/A0000001X");
            assertEquals(1, rows().getItems().size());
            submit("member search q/no matches");
            assertTrue(rows().getItems().isEmpty());
            assertEquals("0", detailLabel("displayedCount"));
            assertTrue(((Label) rows().getPlaceholder()).getText().contains("member list"));
            render("search-empty", 760, 500);
            submit("member list");
            assertEquals(3, rows().getItems().size());
            assertEquals("3", detailLabel("displayedCount"));
            submit("equipment search category/photo availability/available sort/id order/desc");
            assertEquals(1, rows().getItems().size());
            assertEquals(new UUID(0, 2), ((InventoryGroups.Group) rows().getItems().getFirst())
                    .matching().getFirst().getUuid());
            assertEquals("1", detailLabel("displayedCount"));
            assertTrue(detailLabel("inventoryGroups").contains("Photography · Camera 1"));
            render("search-equipment", 1100, 700);
            render("search-equipment", 760, 500);
            int dispatches = logic.commands.size();
            rows().fireEvent(key(KeyCode.ENTER));
            assertEquals(dispatches, logic.commands.size());
            assertEquals("Camera 2", label("groupTitle"));
            javafx.scene.control.TableView<?> units = (javafx.scene.control.TableView<?>) node("physicalUnits");
            assertEquals(null, units.getSelectionModel().getSelectedItem());
            units.getSelectionModel().selectFirst();
            assertEquals(dispatches + 1, logic.commands.size());
            assertEquals(new UUID(0, 2).toString(), detailLabel("equipmentUuid"));
            assertEquals(1, rows().getItems().size());
            submit("equipment search sort/id order/desc");
            assertEquals(new UUID(0, 3), ((InventoryGroups.Group) rows().getItems().getFirst())
                    .matching().getFirst().getUuid());
            submit("equipment list");
            assertEquals(new UUID(0, 1), ((InventoryGroups.Group) rows().getItems().getFirst())
                    .matching().getFirst().getUuid());
        });
    }

    @Test
    public void search_loanMutationRefreshAndInvalidInput_preserveQueryAndCorrection() {
        FxTestUtil.runAndWait(() -> {
            submit("equipment search availability/available");
            assertEquals(1, rows().getItems().size());
            String criteria = detailLabel("criteria");
            submit("equipment search availability/not-valid");
            assertEquals("Error", feedbackStatus());
            assertEquals("equipment search availability/not-valid", field().getText());
            assertEquals(criteria, detailLabel("criteria"));
            submit("loans return uuid/" + new UUID(0, 1) + " returned/2026-01-02");
            assertEquals("Success", feedbackStatus());
            assertEquals(2, rows().getItems().size());
            assertEquals(new UUID(0, 1).toString(), detailLabel("equipmentUuid"));
            submit("equipment search availability/available");
            assertEquals("2", detailLabel("displayedCount"));
            assertTrue(detailLabel("inventoryGroups").contains("Camera 1: 1 available"));
        });
    }

    @Test
    public void search_memberDeletion_preservesFiltersSortAndSelectionUntilExplicitListReset() {
        FxTestUtil.runAndWait(() -> {
            submit("member search q/member holding/no sort/id order/desc");
            assertEquals(List.of(id(3), id(2)), rows().getItems().stream()
                    .map(item -> ((Member) item).getNusId()).toList());
            String criteria = detailLabel("criteria");
            String sort = detailLabel("sortOrder");
            submit("member view nus/" + id(2));
            int saves = storage.saves;
            int dispatches = logic.commands.size();
            submit("member delete nus/" + id(2));
            assertEquals("Success", feedbackStatus());
            assertEquals(saves + 1, storage.saves);
            assertEquals(dispatches + 1, logic.commands.size());
            assertEquals(List.of(id(3)), rows().getItems().stream()
                    .map(item -> ((Member) item).getNusId()).toList());
            assertEquals(criteria, detailLabel("criteria"));
            assertEquals(sort, detailLabel("sortOrder"));
            assertEquals("1", detailLabel("displayedCount"));
            assertTrue(detailLabel("selection").contains(id(3).value));
            submit("member delete nus/" + id(3));
            assertEquals("Success", feedbackStatus());
            assertTrue(rows().getItems().isEmpty());
            assertEquals(criteria, detailLabel("criteria"));
            assertEquals(sort, detailLabel("sortOrder"));
            assertEquals("0", detailLabel("displayedCount"));
            assertEquals("No member selected", detailLabel("selection"));
            assertEquals(model.getAddressBook(), assertDoesNotThrow(() -> storage.readAddressBook()).orElseThrow());
            submit("member list");
            assertEquals(3, rows().getItems().size());
            assertEquals("1", detailLabel("activeCount"));
            assertEquals("2", detailLabel("deletedCount"));
            assertEquals("All records", detailLabel("criteria"));
            assertEquals("Name (A-Z), then NUS ID", detailLabel("sortOrder"));
        });
    }

    @Test
    public void equipmentMaintenance_editDeleteRestore_refreshesDetailsSearchCountsAndSavedIdentity() throws Exception {
        UUID target = new UUID(0, 2);
        FxTestUtil.runAndWait(() -> {
            submit("equipment search status/active sort/id");
            submit("equipment edit uuid/" + target + " name/Updated Camera condition/DAMAGED notes/Repair lens");
            assertEquals("Success", feedbackStatus());
            assertEquals(target.toString(), detailLabel("equipmentUuid"));
            assertEquals("Updated Camera", detailLabel("equipmentName"));
            assertEquals("Repair lens", detailLabel("notes"));
            assertEquals("Unavailable", detailLabel("availability"));
            submit("equipment delete uuid/" + target);
            assertEquals(2, rows().getItems().size());
            assertEquals("1", detailLabel("unavailableCount"));
            submit("equipment search status/deleted");
            assertEquals(1, rows().getItems().size());
            assertTrue(((InventoryGroups.Group) rows().getItems().getFirst()).matching().getFirst().isDeleted());
            submit("equipment view uuid/" + target);
            assertEquals("Deleted", detailLabel("equipmentStatus"));
            assertEquals("Repair lens", detailLabel("notes"));
            submit("equipment restore uuid/" + target);
            assertEquals("Active", detailLabel("equipmentStatus"));
            assertEquals(target.toString(), detailLabel("equipmentUuid"));
            assertEquals("Unavailable", detailLabel("availability"));
            assertTrue(rows().getItems().isEmpty());
            submit("equipment search status/active");
            assertEquals(3, rows().getItems().size());
            assertEquals("2", detailLabel("unavailableCount"));
            assertEquals(model.getAddressBook(), assertDoesNotThrow(() -> storage.readAddressBook()).orElseThrow());
        });
        reloadFreshShell();
        FxTestUtil.runAndWait(() -> {
            submit("equipment view uuid/" + target);
            assertEquals("Updated Camera", detailLabel("equipmentName"));
            assertEquals("Active", detailLabel("equipmentStatus"));
            assertEquals("Damaged", detailLabel("condition"));
            render("equipment-maintenance", 1100, 700);
        });
    }

    @Test
    public void search_equipmentDeletion_preservesAllCriteriaAndZeroMatchesUntilExplicitListReset() {
        FxTestUtil.runAndWait(() -> {
            UUID target = new UUID(0, 2);
            submit("equipment search q/camera category/photo condition/good availability/available"
                    + " sort/id order/desc");
            assertEquals(1, rows().getItems().size());
            String criteria = detailLabel("criteria");
            String sort = detailLabel("sortOrder");
            submit("equipment view uuid/" + target);
            int saves = storage.saves;
            int dispatches = logic.commands.size();
            submit("equipment delete uuid/" + target);
            assertEquals("Success", feedbackStatus());
            assertEquals(saves + 1, storage.saves);
            assertEquals(dispatches + 1, logic.commands.size());
            assertTrue(rows().getItems().isEmpty());
            assertEquals(criteria, detailLabel("criteria"));
            assertEquals(sort, detailLabel("sortOrder"));
            assertEquals("0", detailLabel("displayedCount"));
            assertEquals("No equipment selected", detailLabel("selection"));
            assertEquals(model.getAddressBook(), assertDoesNotThrow(() -> storage.readAddressBook()).orElseThrow());
            submit("equipment list");
            assertEquals(3, rows().getItems().size());
            assertEquals("All records", detailLabel("criteria"));
            assertEquals("Category, name, then UUID", detailLabel("sortOrder"));
        });
    }

    @Test
    public void search_deletedMembers_remainDiscoverableAndRestoreUpdatesQueryCountsAndPersistence() throws Exception {
        FxTestUtil.runAndWait(() -> {
            submit("member search q/jamie status/all sort/id order/desc");
            assertEquals(1, rows().getItems().size());
            String criteria = detailLabel("criteria");
            String sort = detailLabel("sortOrder");
            submit("member delete nus/" + id(2));
            assertEquals("Success", feedbackStatus());
            assertEquals(1, rows().getItems().size());
            assertTrue(((Member) rows().getItems().getFirst()).isDeleted());
            assertEquals(criteria, detailLabel("criteria"));
            assertEquals(sort, detailLabel("sortOrder"));
            assertEquals("2", detailLabel("activeCount"));
            assertEquals("1", detailLabel("deletedCount"));
            assertEquals("1", detailLabel("displayedCount"));
            assertTrue(detailLabel("selection").contains("(deleted)"));
            submit("member search q/jamie status/deleted sort/id order/desc");
            assertEquals(1, rows().getItems().size());
            assertTrue(detailLabel("criteria").contains("DELETED"));
            render("search-deleted-members", 1100, 700);
            render("search-deleted-members", 760, 500);
            submit("member view nus/" + id(2));
            assertEquals("Success", feedbackStatus());
            submit("member restore nus/" + id(2));
            assertEquals("Success", feedbackStatus());
            assertEquals("Jamie Lim", detailLabel("memberName"));
            assertTrue(rows().getItems().isEmpty());
            submit("member search q/jamie status/deleted");
            assertEquals("0", detailLabel("displayedCount"));
            assertEquals("3", detailLabel("activeCount"));
            assertEquals("0", detailLabel("deletedCount"));
            submit("member list");
            assertEquals(3, rows().getItems().size());
            assertEquals("All records", detailLabel("criteria"));
        });
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        reloadFreshShell();
        FxTestUtil.runAndWait(() -> {
            assertFalse(model.findMember(id(2)).orElseThrow().isDeleted());
            assertEquals(3, rows().getItems().size());
        });
    }

    @Test
    public void pastedMultilineNotes_realInput_preservesSeparatorsPersistsAndRetainsRejectedCorrection() {
        FxTestUtil.runAndWait(() -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            var previous = new HashMap<DataFormat, Object>();
            for (DataFormat format : clipboard.getContentTypes()) {
                Object value = clipboard.getContent(format);
                if (value != null) {
                    previous.put(format, value);
                }
            }
            try {
                for (String separator : List.of("\n", "\r\n", "\r", "\t", "\u000b", "\f",
                        "\u0085", "\u2028", "\u2029")) {
                    ClipboardContent content = new ClipboardContent();
                    String command = "equipment add name/Camera category/Test condition/GOOD notes/First line"
                            + separator + "Second line";
                    content.putString(command);
                    assertTrue(clipboard.setContent(content));
                    field().selectAll();
                    field().paste();
                    assertTrue(field().getText().contains("First line Second line")
                            || field().getText().contains("First line  Second line"));
                    int saves = storage.saves;
                    int dispatches = logic.commands.size();
                    field().fireEvent(new ActionEvent());
                    assertEquals("Success", feedbackStatus(), feedback());
                    assertEquals("First line Second line", detailLabel("notes"));
                    assertEquals(saves + 1, storage.saves);
                    assertEquals(dispatches + 1, logic.commands.size());
                    assertEquals(model.getAddressBook(), assertDoesNotThrow(() -> storage.readAddressBook())
                            .orElseThrow());
                }
                ClipboardContent invalid = new ClipboardContent();
                invalid.putString("equipment add name/Camera category/Test condition/GOOD notes/First\nunknown/value");
                clipboard.setContent(invalid);
                field().paste();
                String correction = field().getText();
                reject(correction);
                assertTrue(feedback().startsWith("Unknown parameter: unknown/"));
                assertEquals(field(), window.getPrimaryStage().getScene().getFocusOwner());
            } finally {
                clipboard.setContent(previous);
            }
        });
    }

    @Test
    public void startup_firstSortedMemberAndRepeatedFill_showsSummaryWithoutCommands() {
        FxTestUtil.runAndWait(() -> {
            window.fillInnerParts();
            assertEquals("Laplace", window.getPrimaryStage().getTitle());
            if (Boolean.getBoolean("laplace.packagedShell")) {
                assertTrue(MainWindow.class.getProtectionDomain().getCodeSource().getLocation().getPath()
                        .endsWith("Laplace.jar"));
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
    public void navigation_preservesFiltersAndUserSplit_withoutSavingOrExecutingCommands() {
        FxTestUtil.runAndWait(() -> {
            submit("member search q/Alex");
            int commands = logic.commands.size();
            int saves = storage.saves;
            button("equipmentNavigation").fire();
            button("membersNavigation").fire();
            assertEquals(1, rows().getItems().size());
            assertTrue(label("queryContext").contains("alex"));
            assertEquals(commands, logic.commands.size());
            assertEquals(saves, storage.saves);
            SplitPane split = (SplitPane) node("directory");
            split.setDividerPositions(0.55);
            Region root = (Region) window.getPrimaryStage().getScene().getRoot();
            root.resize(1000, 700);
            layout();
            assertTrue(split.getDividerPositions()[0] > 0.45);
        });
    }

    @Test
    public void navigation_tabsAndMenu_exposesHelpExitAndRejectsLegacyTypedRoutes() {
        FxTestUtil.runAndWait(() -> {
            model.addPerson(new PersonBuilder().withName("Legacy Person").build());
            AddressBook before = new AddressBook(model.getAddressBook());
            button("equipmentNavigation").fire();
            assertTrue(node("equipmentNavigation").getStyleClass().contains("laplace-navigation-active"));
            assertEquals("Ready", feedbackStatus());
            assertTrue(button("equipmentNavigation").getAccessibleText().contains("current view"));
            button("loansNavigation").fire();
            assertTrue(node("openLoans") != null);
            assertEquals("1", label("openCount"));
            button("membersNavigation").fire();
            MenuButton menu = (MenuButton) window.getPrimaryStage().getScene().getRoot().lookup(".menu-button");
            assertEquals(List.of("Help", "Exit"),
                    menu.getItems().stream().map(item -> item.getText()).toList());
            HBox header = (HBox) button("membersNavigation").getParent();
            assertEquals(List.of("Laplace", "Members", "Equipment", "Loans", "Dark mode", "Menu"),
                    header.getChildren().stream().filter(child -> child instanceof Labeled)
                            .map(child -> ((Labeled) child).getText()).toList());
            assertEquals("Laplace", window.getPrimaryStage().getTitle());
            assertEquals("IBM Plex Mono", field().getFont().getFamily());
            assertTrue(((Label) node("countBadge")).getFont().getStyle().contains("Bold"));
            Label queryLabel = (Label) node("queryContext");
            assertEquals(javafx.scene.text.Font.getDefault().getFamily(), queryLabel.getFont().getFamily());
            reject("list");
            assertEquals(MESSAGE_UNKNOWN_COMMAND, feedback());
            reject("list members");
            assertEquals(MESSAGE_UNKNOWN_COMMAND, feedback());
            assertTrue(window.getPersonListPanel().getRoot().getParent() == null);
            assertTrue(button("membersNavigation").getStyleClass().contains("laplace-navigation-active"));
            assertEquals(List.of("list", "list members"), logic.commands);
            assertEquals(0, storage.saves);
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
            assertEquals("Error", feedbackStatus());
            assertTrue(button("membersNavigation").getStyleClass().contains("laplace-navigation-active"));
            assertTrue(label("selection").contains("A0000001X"));
            assertEquals(before, model.getAddressBook());
            submit("equipment list");
            assertEquals("", field().getText());
            assertEquals("Success", feedbackStatus());
            assertEquals(2, logic.commands.size());
            assertEquals(0, storage.saves);
        });
    }

    @Test
    public void loansList_typedCommand_opensExistingLoansTabWithoutChangingRecords() {
        FxTestUtil.runAndWait(() -> {
            AddressBook before = new AddressBook(model.getAddressBook());
            submit("loans list");
            assertTrue(button("loansNavigation").getStyleClass().contains("laplace-navigation-active"));
            assertTrue(node("openLoans") != null);
            assertEquals("Showing current loans.", feedback());
            assertEquals("", field().getText());
            assertEquals(List.of("loans list"), logic.commands);
            assertEquals(before, model.getAddressBook());
            reject("loans list extra");
            assertTrue(button("loansNavigation").getStyleClass().contains("laplace-navigation-active"));
            assertEquals(before, model.getAddressBook());
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
            assertEquals("Success", feedbackStatus());
            assertEquals("A0000001X", detailLabel("memberNusId"));
            assertTrue(button("membersNavigation").getStyleClass().contains("laplace-navigation-active"));
            button("loansNavigation").fire();
            List<Button> links = node("openLoans").lookupAll(".loan-identity-link").stream()
                    .map(node -> (Button) node).toList();
            assertEquals(2, links.size());
            for (Button link : links) {
                link.fire();
            }
            assertEquals(4, logic.commands.size());
            assertTrue(logic.commands.contains("equipment view uuid/" + new UUID(0, 1)));
            assertEquals("Success", feedbackStatus());
            assertTrue(node("detailsTitle") != null);
            assertEquals(4, storage.saves);
        });
    }

    @Test
    public void activation_equipmentSpaceAndSelectionEvents_updatesSummaryAndRealTypedDetailsOnce() {
        FxTestUtil.runAndWait(() -> {
            button("equipmentNavigation").fire();
            rows().getSelectionModel().select(1);
            rows().fireEvent(new KeyEvent(KeyEvent.KEY_RELEASED, "", "", KeyCode.DOWN,
                    false, false, false, false));
            assertTrue(label("selection").contains("camera 2"));
            assertTrue(logic.commands.isEmpty());
            rows().requestFocus();
            rows().fireEvent(key(KeyCode.SPACE));
            assertEquals(rows(), window.getPrimaryStage().getScene().getFocusOwner());
            assertEquals("", field().getText());
            assertTrue(logic.commands.isEmpty());
            assertEquals("Camera 2", label("groupTitle"));
            assertEquals(null, ((javafx.scene.control.TableView<?>) node("physicalUnits"))
                    .getSelectionModel().getSelectedItem());
            button("membersNavigation").fire();
            rows().getSelectionModel().select(1);
            Region directoryList = (Region) node("directoryListPlaceholder");
            MouseEvent released = new MouseEvent(MouseEvent.MOUSE_RELEASED, 5, 5, 5, 5, MouseButton.PRIMARY, 1,
                    false, false, false, false, false, false, false, false, false, true, null);
            directoryList.getChildrenUnmodifiable().getFirst().fireEvent(released);
            assertTrue(label("selection").contains("A0000002X"));
            assertTrue(logic.commands.isEmpty());
            window.getPrimaryStage().hide();
            model.setAddressBook(new AddressBook());
        });
        FxTestUtil.runAndWait(() -> assertEquals(3, rows().getItems().size()));
    }

    @Test
    public void activation_spaceSaveFailure_returnsCorrectionFocusAfterKeyReleaseWithoutLeakingSpace() {
        FxTestUtil.runAndWait(() -> {
            AddressBook before = new AddressBook(model.getAddressBook());
            rows().requestFocus();
            storage.fail = true;
            rows().fireEvent(key(KeyCode.SPACE));
            assertEquals("Error", feedbackStatus());
            assertEquals(rows(), window.getPrimaryStage().getScene().getFocusOwner());
            rows().fireEvent(new KeyEvent(KeyEvent.KEY_TYPED, " ", " ", KeyCode.UNDEFINED,
                    false, false, false, false));
            rows().fireEvent(new KeyEvent(KeyEvent.KEY_RELEASED, "", "", KeyCode.SPACE,
                    false, false, false, false));
            assertEquals(field(), window.getPrimaryStage().getScene().getFocusOwner());
            assertEquals("", field().getText());
            assertEquals(before, model.getAddressBook());
            assertEquals(List.of("member view nus/A0000001X"), logic.commands);
            assertEquals(0, storage.saves);
        });
    }

    @Test
    public void activation_successfulSpaceRelease_keepsRowFocusWithoutDuplicateDispatch() {
        FxTestUtil.runAndWait(() -> {
            rows().requestFocus();
            rows().fireEvent(key(KeyCode.SPACE));
            rows().fireEvent(new KeyEvent(KeyEvent.KEY_RELEASED, "", "", KeyCode.SPACE,
                    false, false, false, false));
            assertEquals(rows(), window.getPrimaryStage().getScene().getFocusOwner());
            assertEquals("", field().getText());
            assertEquals(List.of("member view nus/A0000001X"), logic.commands);
            assertEquals(1, storage.saves);
        });
    }

    @Test
    public void activation_disposedBeforeSpaceRelease_detachesPendingCorrectionFocus() {
        FxTestUtil.runAndWait(() -> {
            rows().requestFocus();
            storage.fail = true;
            rows().fireEvent(key(KeyCode.SPACE));
            Node focusBeforeDisposal = window.getPrimaryStage().getScene().getFocusOwner();
            assertEquals(rows(), focusBeforeDisposal);
            assertEquals("Error", feedbackStatus());

            window.dispose();
            rows().fireEvent(new KeyEvent(KeyEvent.KEY_RELEASED, "", "", KeyCode.SPACE,
                    false, false, false, false));
            assertEquals(focusBeforeDisposal, window.getPrimaryStage().getScene().getFocusOwner());
            assertEquals(List.of("member view nus/A0000001X"), logic.commands);
            assertEquals(0, storage.saves);
        });
    }

    @Test
    public void fillInnerParts_disposedBeforeInitialization_doesNotMountComponentsOrObserveChanges() {
        FxTestUtil.runAndWait(() -> {
            window.dispose();
            window.getPrimaryStage().hide();
            window = new MainWindow(new Stage(), logic, temporaryDirectory.resolve("laplace.json"));
            window.dispose();
            window.fillInnerParts();
            model.setAddressBook(new AddressBook());
            assertEquals(null, window.getPersonListPanel());
            assertTrue(logic.commands.isEmpty());
            assertEquals(0, storage.saves);
        });
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
            logic.nextResult = new CommandResult("Requested equipment",
                    DisplayRequest.equipmentDetails(new UUID(0, 2)));
            submit("controlled equipment detail result");
            assertEquals(new UUID(0, 2).toString(), label("equipmentUuid"));
            assertEquals(new UUID(0, 2), ((Equipment) ((javafx.scene.control.TableView<?>) node("physicalUnits"))
                    .getSelectionModel().getSelectedItem()).getUuid());
            logic.nextResult = new CommandResult("Requested loans", DisplayRequest.loans());
            submit("controlled loans result");
            assertTrue(node("openLoans") != null);
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
            assertTrue(card.lookupAll(".label").stream().map(node -> ((Label) node).getText())
                    .anyMatch(text -> text.contains("1 available")));
            button("loansNavigation").fire();
            assertEquals("0", label("openCount"));
            assertEquals(null, node("historyCount"));
            assertTrue(logic.commands.isEmpty());
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
    public void refresh_softDeletedDetailSelectionAndReset_retainsIdentityThenClearsWithoutCommands() {
        FxTestUtil.runAndWait(() -> {
            logic.nextResult = new CommandResult("Controlled target", DisplayRequest.memberDetails(id(2)));
            submit("controlled detail result");
            model.deleteMember(model.findMember(id(2)).orElseThrow());
        });
        FxTestUtil.runAndWait(() -> {
            assertEquals(id(2), ((Member) rows().getSelectionModel().getSelectedItem()).getNusId());
            assertTrue(((Label) node("directorySummaryPlaceholder").lookup("#memberName"))
                    .getAccessibleText().contains("(deleted)"));
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
            replacement.setEquipment(replacement.findEquipment(new UUID(0, 2)).orElseThrow(),
                    new Equipment(new UUID(0, 2), "Renamed Camera", "Photography",
                    Condition.GOOD, ""));
            model.setAddressBook(replacement.getAddressBook());
        });
        FxTestUtil.runAndWait(() -> {
            assertEquals("Renamed Camera", label("equipmentName"));
            assertEquals(new UUID(0, 2).toString(), label("equipmentUuid"));
            model.deleteEquipment(model.findEquipment(new UUID(0, 2)).orElseThrow());
        });
        FxTestUtil.runAndWait(() -> {
            assertEquals("Deleted", detailLabel("equipmentStatus"));
            assertEquals("Renamed Camera", detailLabel("equipmentName"));
            assertEquals(1, logic.commands.size());
        });
    }

    @Test
    public void details_realViewResults_showContactsNotesDatesAndSameNameIdentities() {
        FxTestUtil.runAndWait(() -> {
            AddressBook replacement = new AddressBook();
            model.getAddressBook().getPersonList().forEach(replacement::addPerson);
            model.getMemberList().forEach(member -> replacement.addMember(member.getNusId().equals(id(2))
                    ? new Member(id(2), new Name("Alex Tan"), new Phone("98765432"),
                            new Email("other@example.com")) : member));
            model.getEquipmentList().forEach(replacement::addEquipment);
            model.getLoanList().forEach(replacement::addLoan);
            model.setAddressBook(replacement);
            model.setEquipment(model.findEquipment(new UUID(0, 2)).orElseThrow(),
                    new Equipment(new UUID(0, 2), "Camera 1", "Photography", Condition.GOOD,
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
            assertEquals("Assigned", detailLabel("availability"));
            assertEquals("Alex Tan", detailLabel("holder"));
            assertEquals("A0000001X", detailLabel("holderNusId"));
            assertEquals("91234567", detailLabel("holderPhone"));
            assertEquals("member1@example.com", detailLabel("holderEmail"));
            assertEquals("2026-01-01", detailLabel("assignedDate"));
            assertEquals("2026-01-03", detailLabel("expectedReturnDate"));
            showEquipmentDetails(new UUID(0, 1));
            assertEquals(new UUID(0, 1).toString(), detailLabel("equipmentUuid"));
            assertEquals("2026-01-02", detailLabel("expectedReturnDate"));
            assertEquals(4, storage.saves);
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
                    assertEquals("Assigned", detailLabel("availability"));
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
                    assertEquals("Available", detailLabel("availability"));
                    assertEquals("No current holder", detailLabel("holder"));
                    assertEquals(null, node("directorySummaryPlaceholder").lookup("#holderEmail"));
                    assertEquals(null, node("directorySummaryPlaceholder").lookup("#assignedDate"));
                } else {
                    assertEquals("1", label("openCount"));
                    assertEquals(null, node("historyCount"));
                }
                assertEquals(dispatchCount[0], logic.commands.size());
                model.closeLoan(new UUID(0, 2), LocalDate.of(2026, 1, 3));
            });
            FxTestUtil.runAndWait(() -> {
                if (active == DisplayRequest.Type.MEMBER_DETAILS) {
                    assertEquals("0 item(s) held", detailLabel("heldCount"));
                } else if (active == DisplayRequest.Type.LOANS) {
                    assertEquals("0", label("openCount"));
                    assertEquals(null, node("historyCount"));
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
    public void storageFailure_laplaceDelete_preservesDetailsSelectionInputAndLaterSavedRecords() throws Exception {
        FxTestUtil.runAndWait(() -> {
            succeed("member view nus/A0000002X");
            AddressBook before = new AddressBook(model.getAddressBook());
            storage.fail = true;
            submit("member delete nus/A0000002X");
            assertEquals("Error", feedbackStatus());
            assertEquals("member delete nus/A0000002X", field().getText());
            assertEquals(field(), window.getPrimaryStage().getScene().getFocusOwner());
            assertEquals("A0000002X", detailLabel("memberNusId"));
            assertEquals(id(2), ((Member) rows().getSelectionModel().getSelectedItem()).getNusId());
            assertEquals(before, model.getAddressBook());
        });
        ReadOnlyAddressBook saved = storage.readAddressBook().orElseThrow();
        FxTestUtil.runAndWait(() -> {
            assertEquals(saved, model.getAddressBook());
            storage.fail = false;
            succeed("member list");
            assertEquals(saved, model.getAddressBook());
            succeed("member delete nus/A0000002X");
            assertTrue(model.findMember(id(2)).orElseThrow().isDeleted());
            assertEquals(3, storage.saves);
        });
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void storageFailure_retiredCommands_preserveMemoryAndDoNotCreateDataFile() {
        FxTestUtil.runAndWait(() -> {
            AddressBook before = new AddressBook(model.getAddressBook());
            Object selected = rows().getSelectionModel().getSelectedItem();
            storage.fail = true;
            for (String input : List.of("clear", "add n/Test Person p/91234567 e/test@example.com a/Test Address")) {
                submit(input);
                assertEquals("Error", feedbackStatus());
                assertEquals(MESSAGE_UNKNOWN_COMMAND, feedback());
                assertEquals(input, field().getText());
                assertEquals(field(), window.getPrimaryStage().getScene().getFocusOwner());
                assertTrue(button("membersNavigation").getStyleClass().contains("laplace-navigation-active"));
                assertEquals(selected, rows().getSelectionModel().getSelectedItem());
                assertEquals(before, model.getAddressBook());
                assertEquals(0, storage.saves);
                assertFalse(Files.exists(temporaryDirectory.resolve("laplace.json")));
            }
            storage.fail = false;
            succeed("member list");
            assertEquals(before, model.getAddressBook());
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
                showEquipmentDetails(new UUID(0, 1));
                render("equipment-details", size[0], size[1]);
                showMemberDetails(id(1));
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
            showEquipmentDetails(new UUID(0, 1));
            render("equipment-details-long", 760, 500);
            showMemberDetails(id(1));
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
        assertEquals(model.getAddressBook(), restored);
        session.add("RELOAD: all collections equal saved JSON; fresh ModelManager, LogicManager and MainWindow.");
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
                session.add("STATUS: " + feedbackStatus());
                session.add("OUTPUT: " + feedback());
                session.add("RETAINED INPUT: " + field().getText());
                if (input.equals("list")) {
                    assertEquals("Error", feedbackStatus());
                    assertEquals(MESSAGE_UNKNOWN_COMMAND, feedback());
                    assertEquals(input, field().getText());
                }
            }
            button("loansNavigation").fire();
            session.add("Loans tab: " + label("openCount") + " open; closed history is not presented.");
            session.add("Commands executed: " + logic.commands);
            session.add("Successful saves: " + storage.saves);
            assertEquals(6, logic.commands.size());
            assertEquals(3, storage.saves);
        });
        Path path = Path.of("build", "reports", "laplace", "shell-session.txt");
        Files.createDirectories(path.getParent());
        Files.write(path, session);
    }

    @Test
    public void routedLifecycle_sameNamesAndDescriptions_persistsDetailsAndPermittedDeletion() throws Exception {
        UUID[] items = new UUID[2];
        LocalDate assigned = LocalDate.now().minusDays(2);
        LocalDate expected = assigned.plusDays(5);
        FxTestUtil.runAndWait(() -> {
            model.setAddressBook(new AddressBook());
            succeed("member add nus/A0000001X name/Alex Tan phone/91234567 email/alex@example.com");
            succeed("member add nus/A0000002X name/Alex Tan phone/98765432 email/other@example.com");
            assertEquals("A0000002X", detailLabel("memberNusId"));
            for (int index = 0; index < items.length; index++) {
                succeed("equipment add name/Camera category/Photography condition/GOOD notes/Keep lens cap on");
                items[index] = UUID.fromString(detailLabel("equipmentUuid"));
                assertEquals("Keep lens cap on", detailLabel("notes"));
            }
            assertFalse(items[0].equals(items[1]));
            succeed("member list");
            assertEquals(2, rows().getItems().size());
            succeed("equipment list");
            assertEquals(1, rows().getItems().size());
            for (UUID item : items) {
                succeed(assign(item, id(1), assigned, expected));
                assertEquals("Assigned", detailLabel("availability"));
                assertEquals("A0000001X", detailLabel("holderNusId"));
                assertEquals("91234567", detailLabel("holderPhone"));
                assertEquals("alex@example.com", detailLabel("holderEmail"));
                assertEquals(assigned.toString(), detailLabel("assignedDate"));
                assertEquals(expected.toString(), detailLabel("expectedReturnDate"));
                assertEquals(item, ((Equipment) ((javafx.scene.control.TableView<?>) node("physicalUnits"))
                    .getSelectionModel().getSelectedItem()).getUuid());
            }
            succeed("member view nus/A0000001X");
            assertEquals("2 item(s) held", detailLabel("heldCount"));
            assertEquals(2, node("directorySummaryPlaceholder").lookupAll("#equipmentUuid").size());
            succeed("member view nus/A0000002X");
            assertEquals("Alex Tan", detailLabel("memberName"));
            assertEquals("98765432", detailLabel("phone"));
            assertEquals("other@example.com", detailLabel("email"));
            assertEquals("0 item(s) held", detailLabel("heldCount"));
        });
        reloadFreshShell();
        FxTestUtil.runAndWait(() -> {
            assertEquals(2, model.getMemberList().size());
            assertEquals(2, model.getEquipmentList().size());
            assertEquals(2, model.getLoanList().size());
            succeed("member view nus/A0000001X");
            assertEquals("2 item(s) held", detailLabel("heldCount"));
            succeed("equipment view uuid/" + items[0]);
            assertEquals(expected.toString(), detailLabel("expectedReturnDate"));
            assertEquals("Keep lens cap on", detailLabel("notes"));
            reject("member delete nus/A0000001X");
            reject("equipment delete uuid/" + items[0]);
            for (UUID item : items) {
                succeed("loans return uuid/" + item + " returned/" + LocalDate.now());
                assertEquals("Available", detailLabel("availability"));
                assertEquals("No current holder", detailLabel("holder"));
                assertEquals(null, node("directorySummaryPlaceholder").lookup("#assignedDate"));
                reject("loans return uuid/" + item + " returned/" + LocalDate.now());
            }
            succeed("member view nus/A0000001X");
            assertEquals("0 item(s) held", detailLabel("heldCount"));
        });
        reloadFreshShell();
        FxTestUtil.runAndWait(() -> {
            assertEquals(2, model.getLoanList().size());
            assertTrue(model.getLoanList().stream().allMatch(loan ->
                    loan.getReturnedDate().orElseThrow().equals(LocalDate.now())));
            succeed("member view nus/A0000001X");
            assertEquals("0 item(s) held", detailLabel("heldCount"));
            succeed("member delete nus/A0000001X");
            assertEquals(id(1), ((Member) rows().getSelectionModel().getSelectedItem()).getNusId());
            assertEquals(null, node("detailsTitle"));
            succeed("equipment view uuid/" + items[0]);
            succeed("equipment delete uuid/" + items[0]);
            assertTrue(((InventoryGroups.Group) rows().getSelectionModel().getSelectedItem()).units().stream()
                    .anyMatch(unit -> unit.getUuid().equals(items[0])));
            assertEquals(null, node("detailsTitle"));
            button("loansNavigation").fire();
            assertEquals("0", label("openCount"));
            assertEquals(null, node("historyCount"));
            assertEquals(2, model.getLoanList().size());
        });
        reloadFreshShell();
        FxTestUtil.runAndWait(() -> {
            assertEquals(2, model.getMemberList().size());
            assertEquals(2, model.getEquipmentList().size());
            assertEquals(2, model.getLoanList().size());
            succeed("member list");
            succeed("member view nus/A0000002X");
            assertEquals("other@example.com", detailLabel("email"));
            succeed("equipment list");
            succeed("equipment view uuid/" + items[1]);
            assertEquals("Available", detailLabel("availability"));
        });
        Path transcript = Path.of("build", "reports", "laplace", "routed-shell-session.txt");
        Files.createDirectories(transcript.getParent());
        Files.write(transcript, session);
    }

    @Test
    public void routedRejections_parametersAndBusinessRules_preserveAllStateAndSavedBytes() {
        FxTestUtil.runAndWait(() -> {
            succeed("equipment add name/Repair item category/Test condition/UNDER_REPAIR");
            UUID repair = UUID.fromString(detailLabel("equipmentUuid"));
            succeed("member view nus/A0000001X");
            LocalDate past = LocalDate.now().minusDays(2);
            String available = assign(new UUID(0, 2), id(1), past, past.plusDays(5));
            for (String command : List.of(
                    "member add nus/A0000001X name/Duplicate phone/91234567 email/duplicate@example.com",
                    "member add nus/A0000004X name/Missing email/new@example.com",
                    "member view nus/A9999999X", "equipment view uuid/" + new UUID(0, 99),
                    "member delete nus/A9999999X", "equipment delete uuid/" + new UUID(0, 99),
                    "member view", "member view nus/A0000001X nus/A0000002X",
                    "member list unknown/value", "equipment list extra",
                    "equipment add name/Incomplete category/Test", "equipment view uuid/not-a-uuid",
                    available + " nus/A0000002X", available + " unknown/value",
                    available.replace(" assigned/" + past, ""),
                    available.replace("assigned/" + past, "assigned/2026-02-30"),
                    assign(new UUID(0, 99), id(1), past, past),
                    assign(new UUID(0, 2), id(9), past, past),
                    assign(new UUID(0, 1), id(2), past, past),
                    assign(new UUID(0, 3), id(1), past, past), assign(repair, id(1), past, past),
                    assign(new UUID(0, 2), id(1), LocalDate.now().plusDays(1), LocalDate.now().plusDays(2)),
                    assign(new UUID(0, 2), id(1), past, past.minusDays(1)),
                    "loans return uuid/" + new UUID(0, 1) + " returned/2025-12-31",
                    "loans return uuid/" + new UUID(0, 1) + " returned/" + LocalDate.now().plusDays(1),
                    "loans return uuid/" + new UUID(0, 2) + " returned/" + past,
                    "loans return uuid/" + new UUID(0, 1),
                    "loans return uuid/" + new UUID(0, 1) + " returned/" + past + " returned/" + past,
                    "member delete nus/A0000001X", "equipment delete uuid/" + new UUID(0, 1))) {
                reject(command);
                succeed("member view nus/A0000001X");
            }
            succeed(available);
            succeed("loans return uuid/" + new UUID(0, 2) + " returned/" + past);
            reject("loans return uuid/" + new UUID(0, 2) + " returned/" + past);
            assertEquals(Availability.AVAILABLE, model.getAvailability(new UUID(0, 2)));
        });
    }

    @Test
    public void routedLoanChanges_eachActiveTab_refreshesRealPanelsWithoutActivationCallbacks() {
        for (DisplayRequest.Type active : List.of(DisplayRequest.Type.MEMBER_DETAILS,
                DisplayRequest.Type.EQUIPMENT_DETAILS, DisplayRequest.Type.LOANS)) {
            FxTestUtil.runAndWait(() -> {
                model.setAddressBook(sample().getAddressBook());
                if (active == DisplayRequest.Type.MEMBER_DETAILS) {
                    showMemberDetails(id(1));
                } else if (active == DisplayRequest.Type.EQUIPMENT_DETAILS) {
                    showEquipmentDetails(new UUID(0, 2));
                } else {
                    button("loansNavigation").fire();
                }
                assertDoesNotThrow(() -> logic.execute(assign(new UUID(0, 2), id(1),
                        LocalDate.now(), LocalDate.now())));
            });
            int count = logic.commands.size();
            FxTestUtil.runAndWait(() -> {
                if (active == DisplayRequest.Type.MEMBER_DETAILS) {
                    assertEquals("2 item(s) held", detailLabel("heldCount"));
                    assertTrue(label("itemsHeld").contains("2"));
                } else if (active == DisplayRequest.Type.EQUIPMENT_DETAILS) {
                    assertEquals("Assigned", detailLabel("availability"));
                    assertEquals("A0000001X", detailLabel("holderNusId"));
                } else {
                    assertEquals("2", label("openCount"));
                }
                assertEquals(count, logic.commands.size());
                assertDoesNotThrow(() -> logic.execute("loans return uuid/" + new UUID(0, 2)
                        + " returned/" + LocalDate.now()));
            });
            FxTestUtil.runAndWait(() -> {
                if (active == DisplayRequest.Type.MEMBER_DETAILS) {
                    assertEquals("1 item(s) held", detailLabel("heldCount"));
                } else if (active == DisplayRequest.Type.EQUIPMENT_DETAILS) {
                    assertEquals("Available", detailLabel("availability"));
                    assertEquals("No current holder", detailLabel("holder"));
                } else {
                    assertEquals("1", label("openCount"));
                    assertEquals(null, node("historyCount"));
                }
                assertEquals(count + 1, logic.commands.size());
            });
        }
    }

    @Test
    public void memberLifecycle_deletedCardDetailsAndReload_retainsSelectionAndRestoresPresentation() throws Exception {
        FxTestUtil.runAndWait(() -> {
            showMemberDetails(id(2));
            succeed("member delete nus/" + id(2));
            assertEquals(3, rows().getItems().size());
            assertEquals(id(2), ((Member) rows().getSelectionModel().getSelectedItem()).getNusId());
            assertEquals("1", label("deletedCount"));
            assertEquals("2", label("activeCount"));
            assertEquals("3", label("displayedCount"));
            Region card = (Region) node("directoryListPlaceholder").lookup(".deleted-member");
            assertFalse(card.isDisabled());
            Label name = (Label) card.lookup("#name");
            assertEquals(Color.web("#59636e"), name.getTextFill());
            TextFlow flow = (TextFlow) name.getGraphic();
            Text value = (Text) flow.getChildren().getFirst();
            Text suffix = (Text) flow.getChildren().getLast();
            assertTrue(value.isStrikethrough());
            assertTrue(value.getFont().getStyle().toLowerCase(java.util.Locale.ROOT).contains("italic"));
            assertEquals(" (deleted)", suffix.getText());
            assertFalse(suffix.isStrikethrough());
            render("member-soft-delete", 1100, 700);
            showMemberDetails(id(2));
            assertTrue(((Label) node("directorySummaryPlaceholder").lookup("#memberName"))
                    .getAccessibleText().contains("(deleted)"));
            render("member-deleted-details", 760, 500);
        });
        reloadFreshShell();
        FxTestUtil.runAndWait(() -> {
            assertTrue(model.findMember(id(2)).orElseThrow().isDeleted());
            succeed("member restore nus/" + id(2));
            assertEquals("Jamie Lim", detailLabel("memberName"));
            assertEquals(null, node("directoryListPlaceholder").lookup(".deleted-member"));
            assertEquals(id(2), ((Member) rows().getSelectionModel().getSelectedItem()).getNusId());
        });
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void memberEdit_routedCommand_refreshesDetailsSortingSelectionAndSavedData() throws Exception {
        FxTestUtil.runAndWait(() -> {
            showMemberDetails(id(1));
            submit("member edit nus/" + id(1) + " name/Zelda Updated phone/98765432 email/zelda@example.com");
            assertEquals("Success", feedbackStatus());
            assertEquals("Zelda Updated", detailLabel("memberName"));
            assertEquals(id(1), ((Member) rows().getSelectionModel().getSelectedItem()).getNusId());
            assertEquals(id(1), ((Member) rows().getItems().getLast()).getNusId());
            assertTrue(node("directoryListPlaceholder").lookupAll(".member-name").stream()
                    .map(node -> ((Label) node).getText()).anyMatch("Zelda Updated"::equals));
            render("member-edit", 1100, 700);
        });
        Member persisted = storage.readAddressBook().orElseThrow().getMemberList().stream()
                .filter(member -> member.getNusId().equals(id(1))).findFirst().orElseThrow();
        assertEquals("Zelda Updated", persisted.getName().fullName);
        assertEquals("98765432", persisted.getPhone().value);
        assertEquals("zelda@example.com", persisted.getEmail().value);
        reloadFreshShell();
        FxTestUtil.runAndWait(() -> {
            showMemberDetails(id(1));
            assertEquals("Zelda Updated", detailLabel("memberName"));
            assertEquals(id(1), ((Member) rows().getItems().getLast()).getNusId());
        });
    }

    /** Checks one real successful dispatch and one successful JSON save. */
    private void succeed(String command) {
        int saves = storage.saves;
        int dispatched = logic.commands.size();
        submit(command);
        assertEquals("Success", feedbackStatus(), command + ": " + feedback());
        assertEquals("", field().getText(), command);
        assertEquals(saves + (command.equals("equipment list") ? 0 : 1), storage.saves, command);
        assertEquals(dispatched + 1, logic.commands.size(), command);
        recordOutcome(command);
    }

    /** Snapshots all collections, the displayed node, selection and bytes before a real rejected command. */
    private void reject(String command) {
        AddressBook before = new AddressBook(model.getAddressBook());
        Object selected = rows().getSelectionModel().getSelectedItem();
        Node displayed = ((javafx.scene.layout.StackPane) node("directorySummaryPlaceholder"))
                .getChildren().getFirst();
        int saves = storage.saves;
        try {
            String json = Files.exists(storage.getAddressBookFilePath())
                    ? Files.readString(storage.getAddressBookFilePath()) : null;
            submit(command);
            assertEquals("Error", feedbackStatus(), command);
            assertEquals(command, field().getText(), command);
            assertEquals(field(), window.getPrimaryStage().getScene().getFocusOwner(), command);
            assertEquals(before, model.getAddressBook(), command);
            assertEquals(saves, storage.saves, command);
            assertEquals(json, Files.exists(storage.getAddressBookFilePath())
                    ? Files.readString(storage.getAddressBookFilePath()) : null, command);
            assertEquals(selected, rows().getSelectionModel().getSelectedItem(), command);
            assertEquals(displayed, ((javafx.scene.layout.StackPane) node("directorySummaryPlaceholder"))
                    .getChildren().getFirst(), command);
            recordOutcome(command);
        } catch (IOException e) {
            throw new AssertionError("Rejection snapshot requires real saved JSON", e);
        }
    }

    /** Records actual pipeline outcomes for the checked lifecycle transcript. */
    private void recordOutcome(String command) {
        session.add("INPUT: " + command);
        session.add("STATUS: " + feedbackStatus());
        session.add("OUTPUT: " + feedback());
        session.add("RETAINED INPUT: " + field().getText());
        session.add("SUCCESSFUL SAVES: " + storage.saves);
    }

    /** Reconstructs model, logic and window from disk; no previous live root is reused. */
    private void reloadFreshShell() throws Exception {
        ReadOnlyAddressBook restored = storage.readAddressBook().orElseThrow();
        assertEquals(model.getAddressBook(), restored);
        session.add("RELOAD: all collections equal saved JSON; fresh ModelManager, LogicManager and MainWindow.");
        FxTestUtil.runAndWait(() -> {
            window.dispose();
            window.getPrimaryStage().hide();
            model = new ModelManager(restored, new seedu.address.model.UserPrefs());
            logic = new RecordingLogic(new LogicManager(model, storage));
            window = new MainWindow(new Stage(), logic, temporaryDirectory.resolve("laplace.json"));
            window.fillInnerParts();
            window.show();
            assertTrue(logic.commands.isEmpty());
        });
    }

    private static String assign(UUID item, NusId member, LocalDate assigned, LocalDate expected) {
        return "loans add uuid/" + item + " nus/" + member + " assigned/" + assigned + " expected/" + expected;
    }

    /** Uses the same real family route and autosave as command input. */
    private void showMemberDetails(NusId identity) {
        submit("member view nus/" + identity.value);
        assertEquals("Success", feedbackStatus());
    }

    /** Uses the actual equipment parser, command, model and storage pipeline. */
    private void showEquipmentDetails(UUID identity) {
        submit("equipment view uuid/" + identity);
        assertEquals("Success", feedbackStatus());
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

    /**
     * Returns the visible status independently of the result color.
     */
    private String feedbackStatus() {
        return label("feedbackStatus");
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
            double position = ((SplitPane) node("directory")).getDividerPositions()[0];
            assertTrue(position > 0 && position < 1);
        }
        for (Node control : node("feedbackActions").lookupAll(".button")) {
            Button action = (Button) control;
            assertTrue(action.getWidth() >= action.prefWidth(-1) - 1, "Feedback controls must not truncate");
        }
        writeSnapshot(root, name, width, height);
    }

    private static void writeSnapshot(Region root, String name, int width, int height) {
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
