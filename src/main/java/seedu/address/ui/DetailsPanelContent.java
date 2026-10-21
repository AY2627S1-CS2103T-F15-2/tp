package seedu.address.ui;

import java.util.Set;

import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** Small presentation helpers shared only by the two detail panels. */
final class DetailsPanelContent {
    private DetailsPanelContent() {}

    static Label text(VBox parent, String id, String text, String... styles) {
        Label label = label(id, text, styles);
        parent.getChildren().add(label);
        return label;
    }

    private static Label label(String id, String text, String... styles) {
        Label label = new Label(text);
        label.setId(id);
        label.setWrapText(true);
        label.setMinWidth(0);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMinHeight(Region.USE_PREF_SIZE);
        label.getStyleClass().addAll(styles);
        return label;
    }

    static void field(VBox parent, String id, String caption, Object value) {
        GridPane field = new GridPane();
        field.setHgap(12);
        field.setMinWidth(0);
        ColumnConstraints key = new ColumnConstraints();
        key.setPercentWidth(30);
        key.setMinWidth(0);
        ColumnConstraints data = new ColumnConstraints();
        data.setPercentWidth(70);
        data.setMinWidth(0);
        field.getColumnConstraints().addAll(key, data);
        field.getStyleClass().add("details-field");
        field.add(label(id + "Caption", caption, "laplace-muted", "details-caption"), 0, 0);
        Label valueLabel = label(id, value.toString(), "details-value");
        if (Set.of("UUID", "NUS ID", "Phone", "Email").contains(caption)) {
            field.add(new VBox(4, valueLabel, CommandPreview.copyButton("Copy " + caption, value.toString())), 1, 0);
        } else {
            field.add(valueLabel, 1, 0);
        }
        parent.getChildren().add(field);
    }

    static void status(VBox parent, String message, boolean error) {
        text(parent, "detailsStatus", message, error ? "laplace-error" : "laplace-neutral", "details-status");
    }
}
