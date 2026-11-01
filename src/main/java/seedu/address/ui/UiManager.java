package seedu.address.ui;

import java.nio.file.Path;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import seedu.address.MainApp;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.Logic;

/**
 * The manager of the UI component.
 */
public class UiManager implements Ui {

    public static final String ALERT_DIALOG_PANE_FIELD_ID = "alertDialogPane";

    private static final Logger logger = LogsCenter.getLogger(UiManager.class);
    private static final String ICON_APPLICATION = "/images/address_book_32.png";

    private Logic logic;
    private Path dataFilePath;
    private MainWindow mainWindow;

    /**
     * Creates a {@code UiManager} with the given {@code Logic} and the data file path
     * to show in the status bar.
     */
    public UiManager(Logic logic, Path dataFilePath) {
        this.logic = logic;
        this.dataFilePath = dataFilePath;
    }

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting UI...");

        //Set the application icon.
        primaryStage.getIcons().add(getImage(ICON_APPLICATION));

        try {
            mainWindow = new MainWindow(primaryStage, logic, dataFilePath);
            mainWindow.show(); //This should be called before creating other UI parts
            mainWindow.fillInnerParts();

        } catch (Throwable e) {
            logger.severe(StringUtil.getDetails(e));
            showFatalErrorDialogAndShutdown("Fatal error during initializing", e);
        }
    }

    /**
     * Shows recovery guidance before startup aborts, without opening an editable main window.
     */
    public static void showDataLoadingError(Path dataFilePath, DataLoadingException failure) {
        Throwable cause = failure.getCause() == null ? failure : failure.getCause();
        String diagnostic = cause instanceof JsonProcessingException jsonFailure
                ? jsonFailure.getOriginalMessage() : cause.getMessage();
        Alert alert = createAlert(null, AlertType.ERROR, "Could not load saved data",
                "Laplace cannot safely open this data file.",
                "Data file: " + dataFilePath.toAbsolutePath() + "\n\n"
                        + "Problem: " + (diagnostic == null ? "The saved data is invalid."
                                : diagnostic.replaceAll("\\s+", " ").strip()) + "\n\n"
                        + "The original data file has been preserved. Close Laplace and back up this file. "
                        + "Repair it or move it to a safe location before restarting. "
                        + "Moving the file starts a separate sample dataset; it does not recover your records.");
        TextArea details = new TextArea(alert.getContentText());
        details.setEditable(false);
        details.setWrapText(true);
        details.setPrefRowCount(8);
        alert.getDialogPane().setContent(details);
        alert.setResizable(true);
        alert.showAndWait();
    }

    private Image getImage(String imagePath) {
        return new Image(MainApp.class.getResourceAsStream(imagePath));
    }

    void showAlertDialogAndWait(Alert.AlertType type, String title, String headerText, String contentText) {
        createAlert(mainWindow.getPrimaryStage(), type, title, headerText, contentText).showAndWait();
    }

    /**
     * Creates an alert dialog on {@code owner} with the given parameters.
     */
    private static Alert createAlert(Stage owner, AlertType type, String title, String headerText,
                                               String contentText) {
        final Alert alert = new Alert(type);
        alert.getDialogPane().getStylesheets().add("view/DarkTheme.css");
        alert.initOwner(owner);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.getDialogPane().setId(ALERT_DIALOG_PANE_FIELD_ID);
        return alert;
    }

    /**
     * Shows an error alert dialog with {@code title} and error message, {@code e},
     * and exits the application after the user has closed the alert dialog.
     */
    private void showFatalErrorDialogAndShutdown(String title, Throwable e) {
        logger.severe(title + " " + e.getMessage() + StringUtil.getDetails(e));
        showAlertDialogAndWait(Alert.AlertType.ERROR, title, e.getMessage(), e.toString());
        Platform.exit();
        System.exit(1);
    }

}
