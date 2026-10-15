package seedu.address.commons.core;

import java.io.IOException;
import java.nio.file.Files;

/** Runs inside a test JAR to exercise path discovery and real file logging in a separate JVM. */
public final class AppPathsProbe {
    private AppPathsProbe() {
    }

    /** Writes markers using the same paths used by application startup. */
    public static void main(String[] args) throws IOException {
        LogsCenter.getLogger(AppPathsProbe.class).info("Packaged sandbox probe");
        Files.createDirectories(AppPaths.getDataFilePath().getParent());
        Files.writeString(AppPaths.getDataFilePath(), "sample data marker");
        Files.writeString(AppPaths.getPreferencesFilePath(), "preferences marker");
    }
}
