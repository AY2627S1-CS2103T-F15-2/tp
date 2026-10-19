package seedu.address.ui;

import java.util.function.Consumer;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import seedu.address.logic.transfer.DataTransferPreview;

/** Read-only replacement preview; both buttons dispatch ordinary commands through the shell. */
public class DataTransferPanel extends UiPart<Region> {
    @FXML
    private Label previewSummary;
    @FXML
    private Button confirmTransfer;
    @FXML
    private Button cancelTransfer;

    /** Mounts the exact pending snapshot's summary and command callbacks. */
    public DataTransferPanel(DataTransferPreview preview, Consumer<String> execute) {
        super("DataTransferPanel.fxml");
        previewSummary.setText(preview.summary());
        confirmTransfer.setOnAction(event -> execute.accept("data confirm"));
        cancelTransfer.setOnAction(event -> execute.accept("data cancel"));
    }
}
