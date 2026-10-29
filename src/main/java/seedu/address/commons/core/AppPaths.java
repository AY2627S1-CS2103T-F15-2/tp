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

    /**
     * Returns the JAR folder, or the working directory when running unpackaged development classes.
     */
    public static Path getHomeDirectory() {
        return HOME_DIRECTORY;
    }

    /**
     * Returns the application's member, equipment and loan data path.
     */
    public static Path getDataFilePath() {
        return HOME_DIRECTORY.resolve("data").resolve("addressbook.json");
    }

    /**
     * Returns the application's window-preferences path.
     */
    public static Path getPreferencesFilePath() {
        return HOME_DIRECTORY.resolve("preferences.json");
    }

    /**
     * Returns the base log path before the logging framework appends rotation numbers.
     */
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

    /**
     * Resolves the application code location before selecting its runtime folder.
     */
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
