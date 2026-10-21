package seedu.address.ui;

import java.io.IOException;
import java.io.InputStream;

import javafx.scene.text.Font;

/**
 * Registers bundled command fonts once; stylesheets control all typography.
 */
final class LaplaceFonts {
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
