package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;

import org.junit.jupiter.api.Test;

import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import seedu.address.testutil.FxTestUtil;

public class ResultDisplayTest {
    @Test
    public void fullFeedback_expandsCollapsesAndCopiesEveryBatchIdentity() {
        FxTestUtil.runAndWait(() -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            HashMap<DataFormat, Object> previous = new HashMap<>();
            for (DataFormat format : clipboard.getContentTypes()) {
                Object value = clipboard.getContent(format);
                if (value != null) {
                    previous.put(format, value);
                }
            }
            try {
                ResultDisplay display = new ResultDisplay();
                TextArea area = (TextArea) display.getRoot().lookup("#resultDisplay");
                String batch = "Rejected batch: 100 of 100 units are invalid. No records changed.\n"
                        + "Equipment 00000000-0000-0000-0000-000000000001 has no open loan to return.\n".repeat(100);
                display.setFeedbackToUser(batch, true);
                Button details = display.getRoot().lookupAll(".button").stream().map(node -> (Button) node)
                        .filter(button -> button.getText().equals("Details")).findFirst().orElseThrow();
                details.fire();
                assertTrue(area.getPrefHeight() > 100);
                assertEquals("Collapse", details.getText());
                details.fire();
                assertTrue(area.getPrefHeight() < 50);
                display.getRoot().lookupAll(".button").stream().map(node -> (Button) node)
                        .filter(button -> button.getText().equals("Copy")).findFirst().orElseThrow().fire();
                assertEquals(batch, clipboard.getString());
            } finally {
                clipboard.setContent(previous);
            }
        });
    }

    @Test
    public void feedback_successAndError_displaysTextAndAccessibleStatus() {
        FxTestUtil.runAndWait(() -> {
            ResultDisplay display = new ResultDisplay();
            TextArea area = (TextArea) display.getRoot().lookup("#resultDisplay");
            assertEquals(null, display.getRoot().lookup("#feedbackStatus"));
            assertEquals("Last result", ((javafx.scene.control.Label) display.getRoot()
                    .getChildrenUnmodifiable().getFirst()).getText());
            assertTrue(area.getAccessibleText().startsWith("Ready: "));
            String longError = "Invalid command\n".repeat(100);
            display.setFeedbackToUser(longError, true);
            assertEquals(longError, area.getText());
            assertEquals("Error: " + longError, area.getAccessibleText());
            assertTrue(area.isWrapText());
            assertFalse(area.isEditable());
            assertTrue(display.getRoot().getStyleClass().contains("laplace-error"));
            assertFalse(display.getRoot().getStyleClass().contains("laplace-success"));
            assertThrows(NullPointerException.class, () -> display.setFeedbackToUser(null, false));
            assertEquals(longError, area.getText());
            display.setFeedbackToUser("Listed members");
            assertEquals("Success: Listed members", area.getAccessibleText());
            assertEquals("Listed members", area.getText());
            assertTrue(display.getRoot().getStyleClass().contains("laplace-success"));
            assertFalse(display.getRoot().getStyleClass().contains("laplace-error"));
        });
    }
}
