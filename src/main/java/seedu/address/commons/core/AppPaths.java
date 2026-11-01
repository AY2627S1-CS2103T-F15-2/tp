package seedu.address.commons.core;

import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Resolves packaged runtime files beside the application JAR, independently of the launch directory.
 */
public final class AppPaths {
    private static final Path HOME_DIRECTORY = locateHomeDirectory();

    private AppPaths() {
    }

    public static Path getHomeDirectory() {
        return HOME_DIRECTORY;
    }

    public static Path getDataFilePath() {
        return HOME_DIRECTORY.resolve("data").resolve("addressbook.json");
    }

    public static Path getPreferencesFilePath() {
        return HOME_DIRECTORY.resolve("preferences.json");
    }

    public static Path getLogFilePath() {
        return HOME_DIRECTORY.resolve("addressbook.log");
    }

    /**
     * Selects the runtime home without changing the JVM's working-directory properties.
     */
    static Path homeDirectoryFor(Path codeLocation, Path workingDirectory) {
        requireNonNull(codeLocation);
        requireNonNull(workingDirectory);
        Path location = codeLocation.toAbsolutePath().normalize();
        if (location.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".jar")) {
            return location.getParent();
        }
        return workingDirectory.toAbsolutePath().normalize();
    }

    private static Path locateHomeDirectory() {
        String location = AppPaths.class.getProtectionDomain().getCodeSource().getLocation().toExternalForm();
        return homeDirectoryForLocation(location, Path.of(""));
    }

    /**
     * Decodes the code-source location and rejects malformed URIs without falling back outside the JAR.
     */
    static Path homeDirectoryForLocation(String codeLocation, Path workingDirectory) {
        try {
            Path location = Path.of(new URI(codeLocation));
            return homeDirectoryFor(location, workingDirectory);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Could not locate the Laplace application folder.", e);
        }
    }
}
