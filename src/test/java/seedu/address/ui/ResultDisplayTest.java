package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import seedu.address.testutil.FxTestUtil;

public class ResultDisplayTest {
    @Test
    public void feedback_successAndError_displaysTextAndAccessibleStatus() {
        FxTestUtil.runAndWait(() -> {
            ResultDisplay display = new ResultDisplay();
            TextArea area = (TextArea) display.getRoot().lookup("#resultDisplay");
            Label status = (Label) display.getRoot().lookup("#feedbackStatus");
            String longError = "Invalid command\n".repeat(100);
            display.setFeedbackToUser(longError, true);
            assertEquals(longError, area.getText());
            assertEquals("Error", status.getText());
            assertTrue(area.isWrapText());
            assertFalse(area.isEditable());
            assertTrue(display.getRoot().getStyleClass().contains("laplace-error"));
            assertFalse(display.getRoot().getStyleClass().contains("laplace-success"));
            assertThrows(NullPointerException.class, () -> display.setFeedbackToUser(null, false));
            assertEquals(longError, area.getText());
            display.setFeedbackToUser("Listed members");
            assertEquals("Success", status.getText());
            assertEquals("Listed members", area.getText());
            assertTrue(display.getRoot().getStyleClass().contains("laplace-success"));
            assertFalse(display.getRoot().getStyleClass().contains("laplace-error"));
        });
    }
}
