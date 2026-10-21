package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * A UI component that displays the result of a command execution.
 */
public class ResultDisplay extends UiPart<Region> {

    private static final String FXML = "ResultDisplay.fxml";

    @FXML
    private TextArea resultDisplay;

    @FXML
    private Label feedbackStatus;

    @FXML
    private VBox feedbackActions;

    /** Creates compact feedback with neutral startup guidance. */
    public ResultDisplay() {
        super(FXML);
        setReady("Type a command or open Help for examples.");
    }

    public void setFeedbackToUser(String feedbackToUser) {
        setFeedbackToUser(feedbackToUser, false);
    }

    /** Shows success or failure with both a textual status and a distinct background. */
    public void setFeedbackToUser(String feedbackToUser, boolean error) {
        requireNonNull(feedbackToUser);
        getRoot().getStyleClass().removeAll("laplace-success", "laplace-error", "laplace-neutral");
        getRoot().getStyleClass().add(error ? "laplace-error" : "laplace-success");
        feedbackStatus.setText(error ? "Error" : "Success");
        resultDisplay.setText(feedbackToUser);
        resultDisplay.setPrefHeight(38);
        Button expand = new Button("Details");
        expand.setAccessibleText("Expand or collapse the complete command result");
        expand.setOnAction(event -> {
            boolean expanded = resultDisplay.getPrefHeight() > 38;
            resultDisplay.setPrefHeight(expanded ? 38 : 130);
            expand.setText(expanded ? "Details" : "Collapse");
        });
        feedbackActions.getChildren().setAll(expand, CommandPreview.copyButton("Copy", feedbackToUser));
    }

    /** Shows neutral startup guidance without describing it as a successful command. */
    public void setReady(String message) {
        setFeedbackToUser(message);
        feedbackStatus.setText("Ready");
        getRoot().getStyleClass().remove("laplace-success");
        getRoot().getStyleClass().add("laplace-neutral");
    }
}
