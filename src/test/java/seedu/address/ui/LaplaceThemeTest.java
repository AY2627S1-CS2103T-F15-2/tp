package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import seedu.address.testutil.FxTestUtil;

/**
 * Checks the real FXML/CSS loader and shared palette independently of future milestone panels.
 */
public class LaplaceThemeTest {

    @Test
    public void theme_fxmlAndCss_appliesSharedPaletteAndSelection() {
        FxTestUtil.runAndWait(() -> {
            ThemeProbe probe = new ThemeProbe();
            Region root = probe.getRoot();
            Scene scene = new Scene(root, 1100, 700);
            scene.getStylesheets().add(getThemeUrl());
            ListView<String> rows = probe.rows;
            populateRows(rows);
            rows.getSelectionModel().selectFirst();
            root.applyCss();
            root.layout();

            assertBackground(root, "#f6f8fa");
            assertBackground((Region) root.lookup("#surface"), "#FFFFFF");
            assertBackground((Region) root.lookup("#success"), "#dafbe1");
            assertBackground((Region) root.lookup("#error"), "#ffebe9");
            assertEquals(Color.web("#1f2328"), ((Label) root.lookup("#title")).getTextFill());
            ListCell<?> selected = rows.lookupAll(".list-cell").stream()
                    .map(node -> (ListCell<?>) node).filter(ListCell::isSelected).findFirst().orElseThrow();
            assertBackground(selected, "#ddf4ff");
        });
    }

    @Test
    public void typography_dynamicControlsAndStyleChanges_keepExactFacesAndMonospace() {
        FxTestUtil.runAndWait(() -> {
            LaplaceFonts.register();
            LaplaceFonts.register();
            VBox root = new VBox();
            root.getStyleClass().add("laplace-root");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getThemeUrl());
            root.applyCss();
            LaplaceFonts.install(root);
            LaplaceFonts.install(root);
            Label label = new Label("Member Directory");
            root.getChildren().add(label);
            label.getStyleClass().add("directory-list-title");
            root.applyCss();
            assertEquals(18, label.getFont().getSize());
            assertInterfaceFace(label, true);
            label.getStyleClass().setAll("label", "identity-value");
            root.applyCss();
            assertEquals("IBM Plex Mono", label.getFont().getFamily());
            assertEquals(13, label.getFont().getSize());
            label.getStyleClass().setAll("label", "laplace-muted");
            root.applyCss();
            assertInterfaceFace(label, false);
            assertEquals(12, label.getFont().getSize());
            assertNotNull(getClass().getResource("/fonts/OFL.txt"));
        });
    }

    @Test
    public void theme_normalAndSmallWindow_rendersPreview() {
        FxTestUtil.runAndWait(() -> {
            ThemeProbe probe = new ThemeProbe();
            Region root = probe.getRoot();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getThemeUrl());
            populateRows(probe.rows);
            probe.rows.getSelectionModel().selectFirst();
            Stage stage = new Stage();
            try {
                stage.setScene(scene);
                stage.setTitle("Laplace foundation style probe");
                stage.show();
                renderPreview(root, 1100, 700);
                renderPreview(root, 760, 500);
            } finally {
                stage.close();
            }
        });
    }

    /**
     * Checks the resolved face, rather than accepting any font whose style merely says Bold.
     */
    static void assertInterfaceFace(Label label, boolean isBold) {
        List<String> candidates = isBold ? List.of("Helvetica Bold", "Arial Bold", "Segoe UI Bold")
                : List.of("Helvetica", "Arial", "Segoe UI");
        String expected = candidates.stream().filter(name -> new Font(name, 14).getName().equals(name))
                .findFirst().orElse(Font.font("System", isBold ? FontWeight.BOLD : FontWeight.NORMAL, 14).getName());
        assertEquals(expected, label.getFont().getName());
    }

    private static String getThemeUrl() {
        var theme = LaplaceThemeTest.class.getResource("/view/LaplaceTheme.css");
        assertNotNull(theme);
        return theme.toExternalForm();
    }

    private static void populateRows(ListView<String> rows) {
        // Keep the probe's test strings separate from any real application data.
        rows.getItems().setAll("Selected row style", "Unselected row style");
        rows.setPlaceholder(new Label("Row style probe"));
        rows.setCellFactory(unused -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean isEmpty) {
                super.updateItem(item, isEmpty);
                setText(null);
                VBox row = new VBox(new Label(item));
                row.getStyleClass().add("laplace-row");
                setGraphic(isEmpty ? null : row);
            }
        });
    }

    private static void assertBackground(Region region, String expected) {
        assertEquals(Color.web(expected), region.getBackground().getFills().get(0).getFill());
    }

    /**
     * Writes an untracked snapshot for visual inspection, including the minimum target content size.
     */
    private static void renderPreview(Region root, int width, int height) {
        root.resize(width, height);
        root.applyCss();
        root.layout();
        Region command = (Region) root.lookup("#command");
        assertTrue(command.localToScene(command.getBoundsInLocal()).getMaxY() <= height);
        WritableImage image = root.snapshot(null, new WritableImage(width, height));
        int[] pixels = new int[width * height];
        image.getPixelReader().getPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), pixels, 0, width);
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        output.setRGB(0, 0, width, height, pixels, 0, width);
        Path path = Path.of("build", "reports", "laplace", "theme-" + width + "x" + height + ".png");
        try {
            Files.createDirectories(path.getParent());
            assertTrue(ImageIO.write(output, "png", path.toFile()));
        } catch (IOException e) {
            throw new AssertionError("Could not write theme preview", e);
        }
    }

    /**
     * Uses the same UiPart controller/packaged-resource loading convention as production components.
     */
    private static class ThemeProbe extends UiPart<Region> {

        @FXML
        private ListView<String> rows;

        ThemeProbe() {
            super("LaplaceThemeTest/theme.fxml");
        }
    }
}
