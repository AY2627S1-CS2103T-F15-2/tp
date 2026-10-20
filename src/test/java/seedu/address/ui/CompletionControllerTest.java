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
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import seedu.address.logic.commands.CommandResult;
import seedu.address.model.ModelManager;
import seedu.address.testutil.FxTestUtil;
import seedu.address.testutil.ViewTestData;

/** Uses real JavaFX event dispatch and focus traversal rather than calling private handlers. */
public class CompletionControllerTest {
    @Test
    public void tab_cycleAndReverse_wrapsAndDoesNotExecute() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = ViewTestData.model();
            List<String> executed = new ArrayList<>();
            CommandBox box = new CommandBox(command -> {
                executed.add(command);
                return new CommandResult("OK");
            }, model::getAddressBook);
            new Scene(new VBox(box.getRoot()));
            TextField field = (TextField) box.getRoot().lookup("#commandTextField");
            box.getRoot().applyCss();
            type(field, "member ");
            key(field, KeyCode.TAB, false, false, false, false);
            assertEquals("member add", field.getText());
            assertEquals(field.getText().length(), field.getCaretPosition());
            for (String candidate : List.of("delete", "list", "view", "add")) {
                key(field, KeyCode.TAB, false, false, false, false);
                assertEquals("member " + candidate, field.getText());
            }
            key(field, KeyCode.SHIFT, true, false, false, false);
            key(field, KeyCode.TAB, true, false, false, false);
            assertEquals("member view", field.getText());
            assertTrue(executed.isEmpty());
            key(field, KeyCode.ENTER, false, false, false, false);
            // JavaFX Enter dispatches the existing ActionEvent once.
            assertEquals(List.of("member view"), executed);
            assertEquals("", field.getText());
            type(field, "mem");
            key(field, KeyCode.TAB, false, false, false, false);
            assertEquals("member", field.getText());
            key(field, KeyCode.TAB, false, false, false, false);
            assertEquals("member", field.getText());
        });
    }

    @Test
    public void tab_editsSelectionAndModelChange_resetCycle() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = ViewTestData.model();
            TextField field = new TextField();
            new CompletionController(field, model::getAddressBook);
            new Scene(new VBox(field));
            type(field, "equipment add name/");
            tab(field);
            assertEquals("equipment add name/Camera", field.getText());
            model.deleteEquipment(ViewTestData.CAMERA);
            model.deleteEquipment(ViewTestData.OTHER_CAMERA);
            // The old candidate disappears; completing the actual input has no match.
            tab(field);
            assertEquals("equipment add name/Camera", field.getText());
            type(field, "equipment add name/");
            tab(field);
            assertEquals("equipment add name/Speaker", field.getText());
            model.addEquipment(ViewTestData.CAMERA);
            type(field, "equipment add name/");
            tab(field);
            assertEquals("equipment add name/Camera", field.getText());
            type(field, "member ");
            tab(field);
            field.positionCaret(3);
            tab(field);
            assertEquals("member add", field.getText());
            field.selectAll();
            tab(field);
            assertEquals("member add", field.getText());
            field.deselect();
            type(field, "member ");
            tab(field);
            field.fireEvent(new ActionEvent());
            field.appendText(" na");
            tab(field);
            assertEquals("member add name/", field.getText());
            assertThrows(NullPointerException.class, () -> new CompletionController(null, model::getAddressBook));
            assertThrows(NullPointerException.class, () -> new CompletionController(field, null));
            assertThrows(NullPointerException.class, () -> new CommandBox(command -> new CommandResult("OK"), null));
        });
    }

    @Test
    public void tab_modifiersAndUnsupportedInput_passThroughWithoutEditing() {
        FxTestUtil.runAndWait(() -> {
            TextField field = new TextField();
            new CompletionController(field, () -> ViewTestData.model().getAddressBook());
            List<KeyEvent> passed = new ArrayList<>();
            field.addEventFilter(KeyEvent.KEY_PRESSED, passed::add);
            new Scene(new VBox(field));
            type(field, "mem");
            key(field, KeyCode.TAB, false, true, false, false);
            key(field, KeyCode.TAB, false, false, true, false);
            key(field, KeyCode.TAB, false, false, false, true);
            key(field, KeyCode.TAB, true, false, false, false);
            assertEquals(4, passed.size());
            assertEquals("mem", field.getText());
            for (String input : List.of("", "unknown", "member add notes/abc")) {
                type(field, input);
                int previous = passed.size();
                tab(field);
                assertEquals(previous + 1, passed.size());
                assertEquals(input, field.getText());
            }
        });
    }

    @Test
    public void tab_noMatchAndFocusLoss_preservesKeyboardNavigation() {
        FxTestUtil.runAndWait(() -> {
            Stage stage = new Stage();
            FocusField field = new FocusField();
            Button next = new Button("Next");
            new CompletionController(field, () -> ViewTestData.model().getAddressBook());
            stage.setScene(new Scene(new VBox(field, next)));
            stage.show();
            try {
                field.requestFocus();
                type(field, "member ");
                tab(field);
                assertEquals("member add", field.getText());
                // Exercise window-manager callbacks without requiring native foreground activation.
                field.changeFocus(true);
                field.changeFocus(false);
                key(field, KeyCode.TAB, true, false, false, false);
                assertEquals(next, stage.getScene().getFocusOwner());
                field.requestFocus();
                type(field, "unknown");
                tab(field);
                assertEquals(next, stage.getScene().getFocusOwner());
                assertFalse(field.isDisabled());
            } finally {
                stage.hide();
            }
        });
    }

    /** Exposes JavaFX's protected focus setter for deterministic window-manager notification tests. */
    private static class FocusField extends TextField {
        void changeFocus(boolean value) {
            setFocused(value);
        }
    }

    private static void type(TextField field, String text) {
        field.setText(text);
        field.positionCaret(text.length());
    }

    private static void tab(TextField field) {
        key(field, KeyCode.TAB, false, false, false, false);
    }

    private static void key(TextField field, KeyCode code, boolean shift, boolean control, boolean alt, boolean meta) {
        field.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, shift, control, alt, meta));
        field.fireEvent(new KeyEvent(KeyEvent.KEY_RELEASED, "", "", code, shift, control, alt, meta));
    }
}
