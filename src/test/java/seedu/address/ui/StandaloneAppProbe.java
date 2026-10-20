package seedu.address.ui;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Region;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import seedu.address.MainApp;
import seedu.address.commons.core.AppPaths;

/** Separate-JVM acceptance entrypoint; all production classes and JavaFX libraries come from Laplace.jar. */
public final class StandaloneAppProbe {
    private StandaloneAppProbe() {
    }

    /** Launches an actual application window without any test-framework runtime dependency. */
    public static void main(String[] args) {
        Application.launch(ProbeApplication.class, args);
    }

    /** Exercises real application startup and typed transfers, then closes the disposable instance. */
    public static class ProbeApplication extends Application {
        @Override
        public void start(Stage stage) throws Exception {
            check(ModuleLayer.boot().findModule("javafx.graphics").isEmpty(), "External JavaFX module was loaded");
            check(MainApp.class.getProtectionDomain().getCodeSource().getLocation().getPath().endsWith("Laplace.jar"),
                    "Production classes did not load from the artifact");
            MainApp app = new MainApp();
            app.init();
            app.start(stage);
            Platform.runLater(() -> {
                try {
                    verify(stage);
                    app.stop();
                    stage.hide();
                    Platform.exit();
                } catch (Throwable failure) {
                    failure.printStackTrace();
                    System.exit(1);
                }
            });
        }

        private void verify(Stage stage) throws Exception {
            Region root = (Region) stage.getScene().getRoot();
            Path exported = AppPaths.getHomeDirectory().resolve("manual snapshot.json");
            submit(root, "data export file/" + exported);
            check(Files.exists(exported), "Export was not created");
            submit(root, "data import file/" + exported);
            for (int[] scenario : List.of(new int[] {1920, 1080, 100}, new int[] {1920, 1080, 125},
                    new int[] {1280, 720, 150})) {
                double scale = scenario[2] / 100.0;
                // Reserve 40 logical pixels for window decorations/taskbar instead of assuming a borderless display.
                double width = scenario[0] / scale;
                double height = scenario[1] / scale - 40;
                root.resize(width, height);
                root.applyCss();
                root.layout();
                for (String id : List.of("commandTextField", "confirmTransfer", "cancelTransfer")) {
                    Node control = root.lookup("#" + id);
                    var bounds = control.localToScene(control.getBoundsInLocal());
                    check(bounds.getMinX() >= 0 && bounds.getMaxX() <= width, id + " outside viewport width");
                    check(bounds.getMinY() >= 0 && bounds.getMaxY() <= height, id + " outside viewport height");
                }
                var parameters = new javafx.scene.SnapshotParameters();
                parameters.setTransform(new Scale(scale, scale));
                WritableImage image = root.snapshot(parameters, null);
                int pixelsWide = (int) image.getWidth();
                int pixelsHigh = (int) image.getHeight();
                int[] pixels = new int[pixelsWide * pixelsHigh];
                image.getPixelReader().getPixels(0, 0, pixelsWide, pixelsHigh,
                        PixelFormat.getIntArgbInstance(), pixels, 0, pixelsWide);
                BufferedImage output = new BufferedImage(pixelsWide, pixelsHigh, BufferedImage.TYPE_INT_ARGB);
                output.setRGB(0, 0, pixelsWide, pixelsHigh, pixels, 0, pixelsWide);
                Path screenshot = AppPaths.getHomeDirectory().resolve("viewport-" + scenario[2] + ".png");
                check(ImageIO.write(output, "png", screenshot.toFile()), "Screenshot could not be written");
                System.out.println("PASS: viewport " + scenario[0] + "x" + scenario[1] + " at " + scenario[2] + "%");
            }
            Button cancel = (Button) root.lookup("#cancelTransfer");
            cancel.fire();
            submit(root, "data restore file/" + exported);
            Button confirm = (Button) root.lookup("#confirmTransfer");
            confirm.fire();
            check(((TextArea) root.lookup("#resultDisplay")).getText().contains("Snapshot applied"),
                    "Confirmation failed");
            submit(root, "member list");
            check(Files.exists(AppPaths.getDataFilePath()), "Data was not saved beside the JAR");
            System.out.println("PASS: standalone Java SE transfers and scaled viewports");
        }

        private void submit(Region root, String command) {
            TextField field = (TextField) root.lookup("#commandTextField");
            field.setText(command);
            field.fireEvent(new ActionEvent());
            root.applyCss();
            root.layout();
            String status = ((Label) root.lookup("#feedbackStatus")).getText();
            String feedback = ((TextArea) root.lookup("#resultDisplay")).getText();
            System.out.println("INPUT: " + command + "\nOUTPUT: " + status + ": " + feedback);
            check(status.equals("Success"), feedback);
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
