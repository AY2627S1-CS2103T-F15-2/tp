package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;

/**
 * A UI component that displays the result of a command execution.
 */
public class ResultDisplay extends UiPart<Region> {

    private static final String FXML = "ResultDisplay.fxml";

    @FXML
    private TextArea resultDisplay;

    @FXML
    private Label feedbackStatus;

    public ResultDisplay() {
        super(FXML);
    }

    public void setFeedbackToUser(String feedbackToUser) {
        setFeedbackToUser(feedbackToUser, false);
    }

    /** Shows success or failure with both a textual status and a distinct background. */
    public void setFeedbackToUser(String feedbackToUser, boolean error) {
        requireNonNull(feedbackToUser);
        getRoot().getStyleClass().removeAll("laplace-success", "laplace-error");
        getRoot().getStyleClass().add(error ? "laplace-error" : "laplace-success");
        feedbackStatus.setText(error ? "Error" : "Success");
        resultDisplay.setText(feedbackToUser);
    }

}
