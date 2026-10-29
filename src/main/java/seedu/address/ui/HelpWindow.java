package seedu.address.ui;

import java.util.function.Consumer;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import seedu.address.commons.core.LogsCenter;

/**
 * Controller for a help page
 */
public class HelpWindow extends UiPart<Stage> {

    public static final String USERGUIDE_URL = "https://ay2627s1-cs2103t-f15-2.github.io/tp/UserGuide.html";
    public static final String HELP_MESSAGE = "Refer to the user guide: " + USERGUIDE_URL;

    private static final Logger logger = LogsCenter.getLogger(HelpWindow.class);
    private static final String FXML = "HelpWindow.fxml";

    @FXML
    private Button copyButton;

    @FXML
    private Label helpMessage;

    @FXML
    private VBox examples;

    /**
     * Creates a new HelpWindow.
     *
     * @param root Stage to use as the root of the HelpWindow.
     */
    public HelpWindow(Stage root) {
        this(root, unused -> { });
    }

    /**
     * Creates a reference whose examples can prepare commands in the main window.
     */
    public HelpWindow(Stage root, Consumer<String> onUse) {
        super(FXML, root);
        helpMessage.setText(HELP_MESSAGE);
        for (String example : CommandCatalog.EXAMPLES) {
            boolean isTemplate = example.matches(".*\\b(NAME|NUS_ID|UUID|PHONE|EMAIL|TEXT|CATEGORY|NOTES|YYYY).*");
            examples.getChildren().add(new CommandPreview(example, isTemplate, command -> {
                onUse.accept(command);
                hide();
            }));
        }
    }

    /**
     * Creates a new HelpWindow.
     */
    public HelpWindow() {
        this(new Stage());
    }

    /**
     * Shows the help window.
     * @throws IllegalStateException
     *     <ul>
     *         <li>
     *             if this method is called on a thread other than the JavaFX Application Thread.
     *         </li>
     *         <li>
     *             if this method is called during animation or layout processing.
     *         </li>
     *         <li>
     *             if this method is called on the primary stage.
     *         </li>
     *         <li>
     *             if {@code dialogStage} is already showing.
     *         </li>
     *     </ul>
     */
    public void show() {
        logger.fine("Showing help page about the application.");
        Stage stage = getRoot();
        fitWithin(Screen.getScreensForRectangle(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight())
                .stream().findFirst().orElse(Screen.getPrimary()).getVisualBounds());
        stage.show();
        stage.centerOnScreen();
    }

    /**
     * Shrinks the window to fit the bounds; on a short scaled screen, an oversized window would place the end
     * of the scrolling reference below the screen, where scrolling cannot reveal it.
     */
    void fitWithin(Rectangle2D bounds) {
        getRoot().setWidth(Math.min(getRoot().getWidth(), bounds.getWidth()));
        getRoot().setHeight(Math.min(getRoot().getHeight(), bounds.getHeight()));
    }

    /**
     * Returns true if the help window is currently being shown.
     */
    public boolean isShowing() {
        return getRoot().isShowing();
    }

    /**
     * Hides the help window.
     */
    public void hide() {
        getRoot().hide();
    }

    /**
     * Focuses on the help window.
     */
    public void focus() {
        getRoot().requestFocus();
    }

    /**
     * Copies the URL to the user guide to the clipboard.
     */
    @FXML
    private void copyUrl() {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent url = new ClipboardContent();
        url.putString(USERGUIDE_URL);
        if (clipboard.setContent(url)) {
            copyButton.setText("Copied");
        }
    }
}
