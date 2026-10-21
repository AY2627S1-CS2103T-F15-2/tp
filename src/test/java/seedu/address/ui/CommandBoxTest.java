package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.testutil.FxTestUtil;

public class CommandBoxTest {
    @Test
    public void enter_blankAndSuccess_ignoresWhitespaceAndExecutesOnce() {
        FxTestUtil.runAndWait(() -> {
            List<String> executed = new ArrayList<>();
            CommandBox box = new CommandBox(command -> {
                executed.add(command);
                return new CommandResult("Success");
            });
            TextField field = field(box);
            for (String input : List.of("", " ", "\t  ")) {
                field.setText(input);
                field.fireEvent(new ActionEvent());
            }
            assertTrue(executed.isEmpty());
            field.setText("member list");
            field.fireEvent(new ActionEvent());
            assertEquals(List.of("member list"), executed);
            assertEquals("", field.getText());
            assertFalse(field.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
            assertEquals(field, field.getScene().getFocusOwner());
            assertThrows(NullPointerException.class, () -> new CommandBox(null));
        });
    }

    @Test
    public void enter_failures_retainsInputAndClearsErrorOnEdit() {
        FxTestUtil.runAndWait(() -> {
            CommandBox box = new CommandBox(command -> {
                if (command.equals("parse")) {
                    throw new ParseException("Bad syntax");
                }
                throw new CommandException("Cannot save");
            });
            TextField field = field(box);
            for (String input : List.of("parse", "save")) {
                field.setText(input);
                field.fireEvent(new ActionEvent());
                field.fireEvent(new ActionEvent());
                assertEquals(input, field.getText());
                assertEquals(1, field.getStyleClass().stream().filter("error"::equals).count());
                assertEquals(field, field.getScene().getFocusOwner());
                field.appendText(" correction");
                assertFalse(field.getStyleClass().contains("error"));
            }
        });
    }

    @Test
    public void multilineReplacement_preservesWordBoundariesCaretAndUndo() {
        FxTestUtil.runAndWait(() -> {
            CommandBox box = new CommandBox(command -> new CommandResult("Success"));
            TextField field = field(box);
            field.setText("notes/keep tail");
            field.selectRange(6, 10);
            field.replaceSelection("First\r\nSecond");
            assertEquals("notes/First  Second tail", field.getText());
            assertEquals(19, field.getCaretPosition());
            assertEquals(19, field.getAnchor());
            field.undo();
            assertEquals("notes/keep tail", field.getText());
            field.redo();
            assertEquals("notes/First  Second tail", field.getText());
            assertEquals(19, field.getCaretPosition());
        });
    }

    @Test
    public void history_andPreparedCommands_preserveDraftWithoutExecution() {
        FxTestUtil.runAndWait(() -> {
            List<String> executed = new ArrayList<>();
            CommandBox box = new CommandBox(command -> {
                executed.add(command);
                return new CommandResult("Success");
            });
            TextField field = field(box);
            box.prepare("equipment list");
            assertTrue(executed.isEmpty());
            field.fireEvent(new ActionEvent());
            box.prepare("member search q/draft");
            field.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.UP, false, false, false, false));
            assertEquals("equipment list", field.getText());
            field.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.DOWN, false, false, false, false));
            assertEquals("member search q/draft", field.getText());
            assertEquals(List.of("equipment list"), executed);
        });
    }

    @Test
    public void completion_popupPreparesWithoutExecutingAndEscapeDismisses() {
        FxTestUtil.runAndWait(() -> {
            List<String> executed = new ArrayList<>();
            CommandBox box = new CommandBox(command -> {
                executed.add(command);
                return new CommandResult("Success");
            });
            TextField field = field(box);
            Stage stage = new Stage();
            try {
                stage.setScene(field.getScene());
                stage.show();
                stage.getScene().getRoot().applyCss();
                stage.getScene().getRoot().layout();
                box.prepare("equipment ad");
                field.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.SPACE, false, true, false, false));
                ContextMenu menu = Window.getWindows().stream().filter(window -> window instanceof ContextMenu)
                        .map(window -> (ContextMenu) window).filter(Window::isShowing).findFirst().orElseThrow();
                assertEquals("equipment add ", menu.getItems().getFirst().getText());
                menu.getItems().getFirst().fire();
                assertEquals("equipment add ", field.getText());
                assertTrue(executed.isEmpty());
                field.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.SPACE, false, true, false, false));
                assertTrue(menu.isShowing());
                field.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ESCAPE, false, false, false, false));
                assertFalse(menu.isShowing());
                field.positionCaret(2);
                field.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.SPACE, false, true, false, false));
                assertFalse(menu.isShowing());
            } finally {
                stage.close();
            }
        });
    }

    @Test
    public void keyboard_escapeAndSelectionShortcut_preserveDraftWithoutSubmitting() {
        FxTestUtil.runAndWait(() -> {
            List<String> executed = new ArrayList<>();
            CommandBox box = new CommandBox(command -> {
                executed.add(command);
                return new CommandResult("Success");
            });
            TextField field = field(box);
            String draft = "equipment search q/camera";
            box.prepare(draft);
            field.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ESCAPE, false, false, false, false));
            assertEquals(draft, field.getText());
            field.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.A, false, true, false, false));
            assertEquals(draft, field.getText());
            assertTrue(executed.isEmpty());
        });
    }

    @Test
    public void history_boundsAndDuplicateSubmissions_keepTheLatestHundredAndDraft() {
        FxTestUtil.runAndWait(() -> {
            CommandBox box = new CommandBox(command -> new CommandResult("Success"));
            TextField field = field(box);
            for (int i = 0; i < 102; i++) {
                box.prepare("member search q/" + i);
                field.fireEvent(new ActionEvent());
                box.prepare("member search q/" + i);
                field.fireEvent(new ActionEvent());
            }
            box.prepare("unfinished");
            for (int i = 0; i < 110; i++) {
                field.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.UP, false, false, false, false));
            }
            assertEquals("member search q/2", field.getText());
            for (int i = 0; i < 110; i++) {
                field.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.DOWN, false, false, false, false));
            }
            assertEquals("unfinished", field.getText());
        });
    }

    private static TextField field(CommandBox box) {
        new Scene(new StackPane(box.getRoot()));
        return (TextField) box.getRoot().lookup("#commandTextField");
    }
}
