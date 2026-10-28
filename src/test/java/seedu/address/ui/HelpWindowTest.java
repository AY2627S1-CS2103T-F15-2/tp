package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import seedu.address.testutil.FxTestUtil;

/** Checks the real Help message and Copy URL action against the published Laplace guide. */
public class HelpWindowTest {
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
