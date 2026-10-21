package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * The UI component that is responsible for receiving user command inputs.
 */
public class CommandBox extends UiPart<Region> {

    public static final String ERROR_STYLE_CLASS = "error";
    private static final String FXML = "CommandBox.fxml";

    private final CommandExecutor commandExecutor;
    private final List<String> history = new ArrayList<>();
    private final ContextMenu completions = new ContextMenu();
    private int historyIndex;
    private String draft = "";

    @FXML
    private TextField commandTextField;

    /**
     * Creates a {@code CommandBox} with the given {@code CommandExecutor}.
     */
    public CommandBox(CommandExecutor commandExecutor) {
        super(FXML);
        this.commandExecutor = requireNonNull(commandExecutor);
        commandTextField.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
        commandTextField.setPromptText("Type a command · Ctrl+Space completes · ↑ history");
        // TextField otherwise removes pasted line breaks and joins adjacent words. Replacing each
        // character separately preserves edit offsets; field parsers collapse runs where appropriate.
        commandTextField.setTextFormatter(new TextFormatter<>(change -> {
            change.setText(change.getText().replaceAll("[\\t\\v]", " "));
            return change;
        }));
        // calls #setStyleToDefault() whenever there is a change to the text of the command box.
        commandTextField.textProperty().addListener((unused1, unused2, unused3) -> setStyleToDefault());
    }

    /**
     * Handles the Enter button pressed event.
     */
    @FXML
    private void handleCommandEntered() {
        String commandText = commandTextField.getText();
        if (commandText.isBlank()) {
            return;
        }

        completions.hide();
        if (history.isEmpty() || !history.getLast().equals(commandText)) {
            history.add(commandText);
            if (history.size() > 100) {
                history.removeFirst();
            }
        }
        historyIndex = history.size();
        try {
            commandExecutor.execute(commandText);
            commandTextField.setText("");
        } catch (CommandException | ParseException e) {
            setStyleToIndicateCommandFailure();
        }
        requestFocus();
    }

    /**
     * Prepares a command without executing it or discarding the user's ability to edit it.
     */
    public void prepare(String command) {
        completions.hide();
        commandTextField.setText(requireNonNull(command));
        historyIndex = history.size();
        requestFocus();
        commandTextField.positionCaret(command.length());
    }

    private void handleKeyPressed(KeyEvent event) {
        if (event.isControlDown() && event.getCode() == KeyCode.SPACE) {
            completions.getItems().clear();
            if (commandTextField.getCaretPosition() == commandTextField.getLength()) {
                for (String candidate : CommandCatalog.complete(commandTextField.getText())) {
                    MenuItem item = new MenuItem(candidate);
                    item.setOnAction(unused -> prepare(candidate));
                    completions.getItems().add(item);
                }
            }
            if (!completions.getItems().isEmpty()) {
                completions.show(commandTextField, Side.TOP, 0, 0);
            }
            event.consume();
        } else if (!completions.isShowing() && (event.getCode() == KeyCode.UP || event.getCode() == KeyCode.DOWN)) {
            if (historyIndex == history.size()) {
                draft = commandTextField.getText();
            }
            historyIndex = Math.clamp(historyIndex + (event.getCode() == KeyCode.UP ? -1 : 1), 0, history.size());
            commandTextField.setText(historyIndex == history.size() ? draft : history.get(historyIndex));
            commandTextField.positionCaret(commandTextField.getLength());
            event.consume();
        } else if (event.getCode() == KeyCode.ESCAPE) {
            completions.hide();
        }
    }

    /**
     * Returns keyboard focus to command input after navigation or submission.
     */
    public void requestFocus() {
        commandTextField.requestFocus();
    }

    /**
     * Sets the command box style to use the default style.
     */
    private void setStyleToDefault() {
        commandTextField.getStyleClass().remove(ERROR_STYLE_CLASS);
    }

    /**
     * Sets the command box style to indicate a failed command.
     */
    private void setStyleToIndicateCommandFailure() {
        ObservableList<String> styleClass = commandTextField.getStyleClass();

        if (styleClass.contains(ERROR_STYLE_CLASS)) {
            return;
        }

        styleClass.add(ERROR_STYLE_CLASS);
    }

    /**
     * Represents a function that can execute commands.
     */
    @FunctionalInterface
    public interface CommandExecutor {
        /**
         * Executes the command and returns the result.
         *
         * @see seedu.address.logic.Logic#execute(String)
         */
        CommandResult execute(String commandText) throws CommandException, ParseException;
    }

}
