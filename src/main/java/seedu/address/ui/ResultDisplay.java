package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

/**
 * A UI component that displays the result of a command execution.
 */
public class ResultDisplay extends UiPart<Region> {

    private static final String FXML = "ResultDisplay.fxml";

    @FXML
    private TextArea resultDisplay;

    @FXML
    private HBox feedbackActions;

    /**
     * Creates compact feedback with neutral startup guidance.
     */
    public ResultDisplay() {
        super(FXML);
        setReady("Type a command or open Help for examples.");
    }

    public void setFeedbackToUser(String feedbackToUser) {
        setFeedbackToUser(feedbackToUser, false);
    }

    /**
     * Shows status through the background and exposes status plus the full message to assistive technology.
     */
    public void setFeedbackToUser(String feedbackToUser, boolean isError) {
        requireNonNull(feedbackToUser);
        getRoot().getStyleClass().removeAll("laplace-success", "laplace-error", "laplace-neutral");
        getRoot().getStyleClass().add(isError ? "laplace-error" : "laplace-success");
        resultDisplay.setAccessibleText((isError ? "Error: " : "Success: ") + feedbackToUser);
        resultDisplay.setText(feedbackToUser);
        resultDisplay.setPrefHeight(38);
        Button expand = new Button("Details");
        expand.setAccessibleText("Expand or collapse the complete command result");
        expand.setOnAction(event -> {
            boolean isExpanded = resultDisplay.getPrefHeight() > 38;
            resultDisplay.setPrefHeight(isExpanded ? 38 : 130);
            expand.setText(isExpanded ? "Details" : "Collapse");
        });
        feedbackActions.getChildren().setAll(expand, CommandPreview.copyButton("Copy", feedbackToUser));
    }

    /**
     * Shows neutral startup guidance without describing it as a successful command.
     */
    public void setReady(String message) {
        setFeedbackToUser(message);
        resultDisplay.setAccessibleText("Ready: " + message);
        getRoot().getStyleClass().remove("laplace-success");
        getRoot().getStyleClass().add("laplace-neutral");
    }
}
