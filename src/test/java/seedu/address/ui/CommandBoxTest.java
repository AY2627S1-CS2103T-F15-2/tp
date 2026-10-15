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
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
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

    private static TextField field(CommandBox box) {
        new Scene(new StackPane(box.getRoot()));
        return (TextField) box.getRoot().lookup("#commandTextField");
    }
}
