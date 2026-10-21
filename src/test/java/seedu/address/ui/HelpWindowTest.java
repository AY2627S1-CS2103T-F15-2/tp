package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;

import org.junit.jupiter.api.Test;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import seedu.address.testutil.FxTestUtil;

/** Checks the real Help message and Copy URL action against the published Laplace guide. */
public class HelpWindowTest {
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
