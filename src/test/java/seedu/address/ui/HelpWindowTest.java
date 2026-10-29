package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import seedu.address.testutil.FxTestUtil;
import seedu.address.testutil.ViewTestData;

/** Checks the real Help message and Copy URL action against the published Laplace guide. */
public class HelpWindowTest {
    @Test
    public void historyCommands_helpAndBothCompletionPaths_prepareWithoutExecuting() {
        FxTestUtil.runAndWait(() -> {
            List<String> prepared = new ArrayList<>();
            HelpWindow help = new HelpWindow(new Stage(), prepared::add);
            TextField field = new TextField();
            new CompletionController(field, () -> ViewTestData.model().getAddressBook());
            try {
                for (String command : List.of("undo", "redo")) {
                    String fragment = command.substring(0, 3);
                    assertEquals(List.of(command + " "), CommandCatalog.complete(fragment));
                    field.setText(fragment);
                    field.positionCaret(fragment.length());
                    field.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.TAB,
                            false, false, false, false));
                    assertEquals(command, field.getText());
                    help.show();
                    help.getRoot().getScene().getRoot().applyCss();
                    Text name = help.getRoot().getScene().getRoot().lookupAll(".command-name").stream()
                            .map(node -> (Text) node).filter(text -> text.getText().equals(command))
                            .findFirst().orElseThrow();
                    CommandPreview preview = (CommandPreview) name.getParent().getParent();
                    preview.lookupAll(".button").stream().map(node -> (Button) node)
                            .filter(button -> button.getText().equals("Use command")).findFirst().orElseThrow().fire();
                    assertFalse(help.isShowing());
                }
                assertEquals(List.of("undo", "redo"), prepared);
            } finally {
                help.hide();
            }
        });
    }

    @Test
    public void returnExample_includesObservedConditionAndPreparesWithoutExecuting() {
        FxTestUtil.runAndWait(() -> {
            List<String> prepared = new ArrayList<>();
            HelpWindow help = new HelpWindow(new Stage(), prepared::add);
            try {
                help.show();
                help.getRoot().getScene().getRoot().applyCss();
                CommandPreview example = help.getRoot().getScene().getRoot().lookupAll(".command-preview").stream()
                        .map(node -> (CommandPreview) node)
                        .filter(preview -> ((TextFlow) preview.getChildren().getFirst()).getChildren().stream()
                                .map(node -> ((Text) node).getText()).reduce("", String::concat)
                                .startsWith("loans return "))
                        .findFirst().orElseThrow();
                example.lookupAll(".button").stream().map(node -> (Button) node)
                        .filter(button -> button.getText().equals("Use command")).findFirst().orElseThrow().fire();
                assertEquals(List.of("loans return uuid/UUID returned/YYYY-MM-DD condition/GOOD"), prepared);
                assertFalse(help.isShowing());
            } finally {
                help.hide();
            }
        });
    }

    @Test
    public void useExample_preparesTemplateAndClosesHelp() {
        FxTestUtil.runAndWait(() -> {
            List<String> prepared = new ArrayList<>();
            HelpWindow help = new HelpWindow(new Stage(), prepared::add);
            try {
                help.show();
                help.getRoot().getScene().getRoot().applyCss();
                CommandPreview example = (CommandPreview) help.getRoot().getScene().lookup(".command-preview");
                example.lookupAll(".button").stream().map(node -> (Button) node)
                        .filter(button -> button.getText().equals("Use command")).findFirst().orElseThrow().fire();
                assertEquals(1, prepared.size());
                assertTrue(prepared.getFirst().startsWith("member add nus/NUS_ID"));
                assertFalse(help.isShowing());
            } finally {
                help.hide();
            }
        });
    }

    @Test
    public void fitWithin_shortScaledScreen_keepsLastExampleReachableByScrolling() {
        FxTestUtil.runAndWait(() -> {
            HelpWindow help = new HelpWindow(new Stage());
            try {
                help.fitWithin(new Rectangle2D(0, 0, 1920, 1000));
                assertEquals(720, help.getRoot().getWidth(), 1);
                assertEquals(600, help.getRoot().getHeight(), 1);
                // 1280x720 at 150% scaling leaves 853x432 logical pixels above a 48-pixel taskbar.
                help.fitWithin(new Rectangle2D(0, 0, 853, 432));
                help.show();
                assertEquals(720, help.getRoot().getWidth(), 1);
                assertEquals(432, help.getRoot().getHeight(), 1);
                ScrollPane scroll = (ScrollPane) help.getRoot().getScene().getRoot();
                scroll.applyCss();
                scroll.layout();
                scroll.setVvalue(scroll.getVmax());
                scroll.layout();
                Button last = scroll.lookupAll(".button").stream().map(node -> (Button) node)
                        .filter(button -> button.getText().equals("Use command")).reduce((first, next) -> next)
                        .orElseThrow();
                Bounds bounds = last.localToScene(last.getBoundsInLocal());
                assertTrue(bounds.getMaxY() <= help.getRoot().getScene().getHeight(),
                        "The last example must scroll into the fitted window");
            } finally {
                help.hide();
            }
        });
    }

    @Test
    public void helpMessageAndCopyUrl_usePublishedLaplaceUserGuide() {
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
                HelpWindow help = new HelpWindow();
                help.getRoot().getScene().getRoot().applyCss();
                String expected = "https://ay2627s1-cs2103t-f15-2.github.io/tp/UserGuide.html";
                Label message = (Label) help.getRoot().getScene().lookup("#helpMessage");
                assertEquals("Refer to the user guide: " + expected, message.getText());
                Button copy = (Button) help.getRoot().getScene().lookup("#copyButton");
                copy.fire();
                assertEquals(expected, clipboard.getString());
            } finally {
                clipboard.setContent(previous);
            }
        });
    }
}
