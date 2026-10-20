package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.util.LaplaceSortOrder;
import seedu.address.logic.Logic;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DisplayRequest;
import seedu.address.logic.commands.TransferPreviewResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.commands.exceptions.DataTransferException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;

/** Application shell. Owns navigation and refresh subscriptions, while panels remain read-only views. */
public class MainWindow extends UiPart<Stage> {
    private static final String FXML = "MainWindow.fxml";
    private static final String ACTIVE_STYLE = "laplace-navigation-active";

    private final Stage primaryStage;
    private final Logic logic;
    private final Path dataFilePath;
    private final ReadOnlyAddressBook data;
    private final HelpWindow helpWindow;
    private final InvalidationListener dataListener = unused -> queueRefresh();
    private final EventHandler<KeyEvent> helpFilter = event -> {
        if (event.getCode() == KeyCode.SPACE) {
            spacePressed = true;
        }
        if (event.getCode() == KeyCode.F1) {
            handleHelp();
            event.consume();
        }
    };
    private final EventHandler<KeyEvent> activationReleaseFilter = this::handleActivationRelease;
    private final EventHandler<KeyEvent> selectionKeyHandler = unused -> refreshSummaries();
    private final EventHandler<MouseEvent> selectionMouseHandler = unused -> refreshSummaries();
    private final EventHandler<WindowEvent> hiddenHandler = unused -> dispose();

    private PersonListPanel personListPanel;
    private MemberListPanel members;
    private EquipmentListPanel equipment;
    private MemberDirectorySummary memberSummary;
    private EquipmentDirectorySummary equipmentSummary;
    private MemberDetailsPanel memberDetails;
    private EquipmentDetailsPanel equipmentDetails;
    private LoanPanel loans;
    private CommandBox commandBox;
    private ResultDisplay resultDisplay;
    private DisplayRequest currentDisplay = DisplayRequest.members();
    private List<NusId> memberOrder = List.of();
    private List<UUID> equipmentOrder = List.of();
    private boolean initialized;
    private boolean spacePressed;
    private boolean focusAfterActivationKey;
    private boolean refreshQueued;
    private boolean disposed;

    @FXML
    private StackPane commandBoxPlaceholder;
    @FXML
    private StackPane resultDisplayPlaceholder;
    @FXML
    private StackPane statusbarPlaceholder;
    @FXML
    private SplitPane directory;
    @FXML
    private StackPane directoryListPlaceholder;
    @FXML
    private StackPane directorySummaryPlaceholder;
    @FXML
    private StackPane singlePanelPlaceholder;
    @FXML
    private Button membersNavigation;
    @FXML
    private Button equipmentNavigation;
    @FXML
    private Button loansNavigation;
    @FXML
    private MenuItem helpMenuItem;

    /** Creates the shell with the actual command pipeline and the existing live root. */
    public MainWindow(Stage primaryStage, Logic logic, Path dataFilePath) {
        super(FXML, requireNonNull(primaryStage));
        this.primaryStage = primaryStage;
        this.logic = requireNonNull(logic);
        this.dataFilePath = requireNonNull(dataFilePath);
        data = requireNonNull(logic.getAddressBook());
        helpWindow = new HelpWindow();
        setWindowDefaultSize(logic.getGuiSettings());
        directory.widthProperty().addListener((unused, previous, current) -> directory.setDividerPositions(0.28));
        helpMenuItem.setAccelerator(KeyCombination.valueOf("F1"));
        primaryStage.addEventFilter(KeyEvent.KEY_PRESSED, helpFilter);
        primaryStage.addEventFilter(KeyEvent.KEY_RELEASED, activationReleaseFilter);
        primaryStage.addEventHandler(WindowEvent.WINDOW_HIDDEN, hiddenHandler);
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    /** Mounts each component and subscribes exactly once. Initial selection never executes a command. */
    void fillInnerParts() {
        if (initialized || disposed) {
            return;
        }
        personListPanel = new PersonListPanel(logic.getFilteredPersonList());
        personListPanel.getRoot().getStyleClass().add("laplace-legacy");
        personListPanel.getRoot().getStylesheets().add(getClass().getResource("/view/DarkTheme.css").toExternalForm());
        members = new MemberListPanel(data, id -> activate("member view nus/" + id.value));
        equipment = new EquipmentListPanel(data, logic::getAvailability,
                id -> activate("equipment view uuid/" + id));
        memberSummary = new MemberDirectorySummary(data);
        equipmentSummary = new EquipmentDirectorySummary(data, logic::getAvailability);
        memberDetails = new MemberDetailsPanel(data, logic::getAvailability);
        equipmentDetails = new EquipmentDetailsPanel(data, logic::getAvailability);
        loans = new LoanPanel(data, id -> activate("equipment view uuid/" + id),
                id -> activate("member view nus/" + id.value));
        resultDisplay = new ResultDisplay();
        commandBox = new CommandBox(this::executeCommand);
        commandBoxPlaceholder.getChildren().setAll(commandBox.getRoot());
        resultDisplayPlaceholder.getChildren().setAll(resultDisplay.getRoot());
        statusbarPlaceholder.getChildren().setAll(new StatusBarFooter(dataFilePath).getRoot());
        data.getMemberList().addListener(dataListener);
        data.getEquipmentList().addListener(dataListener);
        data.getLoanList().addListener(dataListener);
        logic.getFilteredPersonList().addListener(dataListener);
        members.getRoot().addEventHandler(KeyEvent.KEY_RELEASED, selectionKeyHandler);
        members.getRoot().addEventHandler(MouseEvent.MOUSE_RELEASED, selectionMouseHandler);
        equipment.getRoot().addEventHandler(KeyEvent.KEY_RELEASED, selectionKeyHandler);
        equipment.getRoot().addEventHandler(MouseEvent.MOUSE_RELEASED, selectionMouseHandler);
        initialized = true;
        refreshViews();
        display(DisplayRequest.members());
        // UiManager shows the stage before mounting panels; measure their minimum widths before restoring the split.
        primaryStage.getScene().getRoot().applyCss();
        primaryStage.getScene().getRoot().layout();
        directory.setDividerPositions(0.28);
        resultDisplay.setFeedbackToUser("Ready. Use member list, equipment list, or the navigation buttons.");
        commandBox.requestFocus();
    }

    /** Coalesces synchronous root changes, so observers render after a whole model operation finishes. */
    private void queueRefresh() {
        if (disposed || refreshQueued) {
            return;
        }
        refreshQueued = true;
        Platform.runLater(() -> {
            if (!disposed && refreshQueued) {
                refreshViews();
            }
        });
    }

    /** Rebuilds all derived views, retaining identities or choosing the next sorted row after deletion. */
    private void refreshViews() {
        refreshQueued = false;
        List<NusId> newMembers = data.getMemberList().stream().sorted(LaplaceSortOrder.MEMBERS)
                .map(Member::getNusId).toList();
        List<UUID> newEquipment = data.getEquipmentList().stream().sorted(LaplaceSortOrder.EQUIPMENT)
                .map(Equipment::getUuid).toList();
        Optional<NusId> memberSelection = recoverSelection(members.getSelection(), memberOrder, newMembers);
        Optional<UUID> equipmentSelection = recoverSelection(equipment.getSelection(), equipmentOrder, newEquipment);
        members.refresh();
        equipment.refresh();
        members.select(memberSelection);
        equipment.select(equipmentSelection);
        memberOrder = newMembers;
        equipmentOrder = newEquipment;
        refreshSummaries();
        loans.refresh();
        memberDetails.refresh();
        equipmentDetails.refresh();
        if (currentDisplay.getType() == DisplayRequest.Type.MEMBER_DETAILS
                && !newMembers.contains(currentDisplay.getMemberNusId().orElseThrow())) {
            display(DisplayRequest.members());
        } else if (currentDisplay.getType() == DisplayRequest.Type.EQUIPMENT_DETAILS
                && !newEquipment.contains(currentDisplay.getEquipmentUuid().orElseThrow())) {
            display(DisplayRequest.equipment());
        }
    }

    /** Resolves a deleted selection by its former position, without firing a panel activation callback. */
    private static <T> Optional<T> recoverSelection(Optional<T> selected, List<T> previous, List<T> current) {
        if (selected.isPresent() && current.contains(selected.get())) {
            return selected;
        }
        if (current.isEmpty() || selected.isEmpty() && !previous.isEmpty()) {
            return Optional.empty();
        }
        int previousIndex = selected.map(previous::indexOf).orElse(0);
        return Optional.of(current.get(Math.min(Math.max(previousIndex, 0), current.size() - 1)));
    }

    private void refreshSummaries() {
        memberSummary.refresh(members.getSelection());
        equipmentSummary.refresh(equipment.getSelection());
    }

    /** Mounts the real summary or detail panel according to the typed display request. */
    private void display(DisplayRequest request) {
        if (request.getType() == DisplayRequest.Type.UNCHANGED) {
            return;
        }
        currentDisplay = request;
        boolean memberView = request.getType() == DisplayRequest.Type.MEMBERS
                || request.getType() == DisplayRequest.Type.MEMBER_DETAILS;
        boolean equipmentView = request.getType() == DisplayRequest.Type.EQUIPMENT
                || request.getType() == DisplayRequest.Type.EQUIPMENT_DETAILS;
        boolean directoryView = memberView || equipmentView;
        directory.setVisible(directoryView);
        directory.setManaged(directoryView);
        singlePanelPlaceholder.setVisible(!directoryView);
        singlePanelPlaceholder.setManaged(!directoryView);
        if (directoryView) {
            singlePanelPlaceholder.getChildren().clear();
            directoryListPlaceholder.getChildren().setAll(memberView ? members.getRoot() : equipment.getRoot());
            if (request.getType() == DisplayRequest.Type.MEMBER_DETAILS) {
                memberDetails.showMember(request.getMemberNusId().orElseThrow());
                directorySummaryPlaceholder.getChildren().setAll(memberDetails.getRoot());
            } else if (request.getType() == DisplayRequest.Type.EQUIPMENT_DETAILS) {
                equipmentDetails.showEquipment(request.getEquipmentUuid().orElseThrow());
                directorySummaryPlaceholder.getChildren().setAll(equipmentDetails.getRoot());
            } else {
                directorySummaryPlaceholder.getChildren().setAll(memberView
                        ? memberSummary.getRoot() : equipmentSummary.getRoot());
            }
        } else {
            singlePanelPlaceholder.getChildren().setAll(request.getType() == DisplayRequest.Type.LOANS
                    ? loans.getRoot() : personListPanel.getRoot());
        }
        request.getMemberNusId().ifPresent(id -> members.select(Optional.of(id)));
        request.getEquipmentUuid().ifPresent(id -> equipment.select(Optional.of(id)));
        setActive(membersNavigation, memberView);
        setActive(equipmentNavigation, equipmentView);
        setActive(loansNavigation, request.getType() == DisplayRequest.Type.LOANS);
        refreshSummaries();
    }

    private void setActive(Button button, boolean active) {
        button.getStyleClass().remove(ACTIVE_STYLE);
        if (active) {
            button.getStyleClass().add(ACTIVE_STYLE);
        }
        button.setAccessibleText(button.getText() + (active ? ", current view" : ""));
    }

    @FXML
    private void handleMembers() {
        activate("member list");
        commandBox.requestFocus();
    }

    @FXML
    private void handleEquipment() {
        activate("equipment list");
        commandBox.requestFocus();
    }

    @FXML
    private void handleLoans() {
        display(DisplayRequest.loans());
        commandBox.requestFocus();
    }

    /** Returns correction focus only after Space's typed event has finished on its originating control. */
    private void handleActivationRelease(KeyEvent event) {
        if (event.getCode() == KeyCode.SPACE) {
            spacePressed = false;
            if (focusAfterActivationKey) {
                focusAfterActivationKey = false;
                commandBox.requestFocus();
            }
        }
    }

    /** Routes explicit UI activation through exactly the same pipeline as command input. */
    private void activate(String commandText) {
        try {
            executeCommand(commandText);
        } catch (CommandException | ParseException e) {
            // Delay Space's correction focus until release so its typed event cannot enter the command box.
            if (spacePressed) {
                focusAfterActivationKey = true;
            } else {
                commandBox.requestFocus();
            }
        }
        // Keep row/link focus until its key finishes; moving it here leaks Space into command input.
    }

    private CommandResult executeCommand(String commandText) throws CommandException, ParseException {
        try {
            CommandResult result = logic.execute(commandText);
            resultDisplay.setFeedbackToUser(result.getFeedbackToUser());
            refreshViews();
            if (result.isShowHelp()) {
                handleHelp();
            }
            if (result.isExit()) {
                handleExit();
            } else if (!result.isShowHelp()) {
                display(result.getDisplayRequest());
                if (result instanceof TransferPreviewResult preview) {
                    directory.setVisible(false);
                    directory.setManaged(false);
                    singlePanelPlaceholder.setVisible(true);
                    singlePanelPlaceholder.setManaged(true);
                    singlePanelPlaceholder.getChildren().setAll(
                            new DataTransferPanel(preview.getPreview(), this::activate).getRoot());
                }
            }
            return result;
        } catch (CommandException | ParseException e) {
            if (e instanceof DataTransferException failure && failure.isPreviewInvalidated()) {
                display(DisplayRequest.members());
            }
            resultDisplay.setFeedbackToUser(e.getMessage(), true);
            queueRefresh();
            throw e;
        }
    }

    /** Keeps saved geometry usable on the current screens, while choosing a wider first-run shell. */
    private void setWindowDefaultSize(GuiSettings settings) {
        Rectangle2D bounds = Screen.getScreens().stream()
                .filter(screen -> settings.getWindowCoordinates() != null
                        && screen.getVisualBounds().contains(settings.getWindowCoordinates().getX(),
                                settings.getWindowCoordinates().getY()))
                .findFirst().orElse(Screen.getPrimary()).getVisualBounds();
        boolean firstRun = settings.equals(new GuiSettings());
        double width = Math.min(bounds.getWidth(), Math.max(760, firstRun ? 1100 : settings.getWindowWidth()));
        double height = Math.min(bounds.getHeight(), Math.max(500, firstRun ? 700 : settings.getWindowHeight()));
        primaryStage.setMinWidth(Math.min(760, bounds.getWidth()));
        primaryStage.setMinHeight(Math.min(500, bounds.getHeight()));
        primaryStage.setWidth(width);
        primaryStage.setHeight(height);
        if (settings.getWindowCoordinates() != null) {
            primaryStage.setX(Math.max(bounds.getMinX(), Math.min(settings.getWindowCoordinates().getX(),
                    bounds.getMaxX() - width)));
            primaryStage.setY(Math.max(bounds.getMinY(), Math.min(settings.getWindowCoordinates().getY(),
                    bounds.getMaxY() - height)));
        }
    }

    /** Opens help or brings its existing window to the front. */
    @FXML
    public void handleHelp() {
        if (helpWindow.isShowing()) {
            helpWindow.focus();
        } else {
            helpWindow.show();
        }
    }

    void show() {
        primaryStage.show();
        directory.setDividerPositions(0.28);
    }

    /** Saves window preferences and closes the shell and its owned help window. */
    @FXML
    private void handleExit() {
        logic.setGuiSettings(new GuiSettings(primaryStage.getWidth(), primaryStage.getHeight(),
                (int) primaryStage.getX(), (int) primaryStage.getY()));
        dispose();
        primaryStage.hide();
    }

    /** Detaches subscriptions and handlers; queued refreshes become harmless after shutdown. */
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        primaryStage.removeEventFilter(KeyEvent.KEY_PRESSED, helpFilter);
        primaryStage.removeEventFilter(KeyEvent.KEY_RELEASED, activationReleaseFilter);
        primaryStage.removeEventHandler(WindowEvent.WINDOW_HIDDEN, hiddenHandler);
        helpWindow.hide();
        if (initialized) {
            data.getMemberList().removeListener(dataListener);
            data.getEquipmentList().removeListener(dataListener);
            data.getLoanList().removeListener(dataListener);
            logic.getFilteredPersonList().removeListener(dataListener);
            members.getRoot().removeEventHandler(KeyEvent.KEY_RELEASED, selectionKeyHandler);
            members.getRoot().removeEventHandler(MouseEvent.MOUSE_RELEASED, selectionMouseHandler);
            equipment.getRoot().removeEventHandler(KeyEvent.KEY_RELEASED, selectionKeyHandler);
            equipment.getRoot().removeEventHandler(MouseEvent.MOUSE_RELEASED, selectionMouseHandler);
        }
    }

    public PersonListPanel getPersonListPanel() {
        return personListPanel;
    }
}
