package seedu.address.ui;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Labeled;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Registers bundled command text and applies real Helvetica faces.
 * Full face names bypass JavaFX family/weight substitution that can clip interface glyphs.
 */
final class LaplaceFonts {
    private static final String INSTALLED = "laplaceFontsInstalled";
    private static boolean isRegistered;

    private LaplaceFonts() {
    }

    /**
     * Loads IBM Plex Mono so command text resolves on machines that have not installed it.
     */
    static void register() {
        if (isRegistered) {
            return;
        }
        load("/fonts/IBMPlexMono-Regular.ttf");
        load("/fonts/IBMPlexMono-Bold.ttf");
        isRegistered = true;
    }

    /**
     * Applies interface faces to this node and to nodes added later.
     */
    static void install(Node node) {
        register();
        if (node == null || node.getProperties().put(INSTALLED, Boolean.TRUE) != null) {
            return;
        }
        paint(node);
        if (node instanceof Labeled) {
            node.getStyleClass().addListener((ListChangeListener<String>) change -> paint(node));
        }
        if (node instanceof Parent parent) {
            parent.getChildrenUnmodifiable().addListener((ListChangeListener<Node>) change -> {
                while (change.next()) {
                    if (change.wasAdded()) {
                        change.getAddedSubList().forEach(LaplaceFonts::install);
                    }
                }
            });
            for (Node child : List.copyOf(parent.getChildrenUnmodifiable())) {
                install(child);
            }
        }
        if (node instanceof Labeled labeled) {
            labeled.graphicProperty().addListener((unused, previous, graphic) -> {
                if (graphic != null) {
                    install(graphic);
                }
            });
            if (labeled.getGraphic() != null) {
                install(labeled.getGraphic());
            }
        }
    }

    /**
     * Applies the interface size and exact face for a control's semantic style classes.
     * Identity labels retain their bundled monospace CSS styling.
     */
    private static void paint(Node node) {
        if (!(node instanceof Labeled labeled) || labeled.getStyleClass().contains("identity-value")) {
            return;
        }
        boolean isBold = false;
        double size = 14;
        for (String style : labeled.getStyleClass()) {
            if ("laplace-title".equals(style)) {
                size = 22;
                isBold = true;
            } else if ("laplace-wordmark".equals(style)) {
                size = 21;
                isBold = true;
            } else if ("laplace-muted".equals(style)) {
                size = 12;
            } else if ("laplace-count-badge".equals(style)) {
                size = 12;
                isBold = true;
            } else if ("directory-list-title".equals(style)) {
                size = 18;
                isBold = true;
            } else if ("directory-selection-hint".equals(style)) {
                size = 11;
            } else if ("member-name".equals(style) || "equipment-name".equals(style)) {
                size = 15;
                isBold = true;
            } else if ("loan-title".equals(style)) {
                size = 21;
                isBold = true;
            } else if ("loan-section-title".equals(style)) {
                size = 18;
                isBold = true;
            } else if ("details-name".equals(style)) {
                size = 23;
                isBold = true;
            } else if ("details-section-title".equals(style)) {
                size = 17;
                isBold = true;
            } else if ("directory-key".equals(style) || "directory-status-title".equals(style)
                    || "details-caption".equals(style) || "loan-count".equals(style)
                    || "loan-status".equals(style)) {
                isBold = true;
            }
        }
        if (hasAncestor(labeled, "column-header")) {
            size = 13;
            isBold = true;
        } else if (hasAncestor(labeled, "equipment-group-card")
                && labeled.getStyleClass().contains("details-section-title")) {
            size = 15;
        }
        labeled.setFont(face(isBold, size));
    }

    /**
     * Identifies the table-header and group-card contexts that refine label typography.
     */
    private static boolean hasAncestor(Node node, String style) {
        for (Node current = node.getParent(); current != null; current = current.getParent()) {
            if (current.getStyleClass().contains(style)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Selects an installed face by its full name, falling back only when it is unavailable.
     */
    private static Font face(boolean isBold, double size) {
        List<String> names = isBold
                ? List.of("Helvetica Bold", "Arial Bold", "Segoe UI Bold")
                : List.of("Helvetica", "Arial", "Segoe UI");
        for (String name : names) {
            Font font = new Font(name, size);
            if (font.getName().equals(name)) {
                return font;
            }
        }
        return Font.font("System", isBold ? FontWeight.BOLD : FontWeight.NORMAL, size);
    }

    /**
     * Registers one packaged face and closes its resource stream.
     */
    private static void load(String resource) {
        InputStream input = LaplaceFonts.class.getResourceAsStream(resource);
        if (input == null) {
            throw new IllegalStateException("Missing font resource " + resource);
        }
        try (input) {
            if (Font.loadFont(input, 12) == null) {
                throw new IllegalStateException("Could not read font resource " + resource);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read font resource " + resource, exception);
        }
    }
}
