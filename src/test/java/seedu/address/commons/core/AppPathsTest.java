package seedu.address.commons.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Checks packaged path resolution and verifies that a foreign launch directory receives no runtime files. */
public class AppPathsTest {
    @TempDir
    public Path directory;

    @Test
    public void homeDirectoryFor_packagedOrDevelopment_resolvesExpectedHome() {
        Path home = directory.resolve("release folder");
        Path launch = directory.resolve("launch folder");
        assertEquals(home, AppPaths.homeDirectoryFor(home.resolve("Laplace.jar"), launch));
        assertEquals(home, AppPaths.homeDirectoryFor(home.resolve("Laplace.JAR"), launch));
        assertEquals(launch, AppPaths.homeDirectoryFor(home.resolve("classes"), launch));
        assertEquals(AppPaths.getHomeDirectory().resolve("data/addressbook.json"), AppPaths.getDataFilePath());
        assertEquals(AppPaths.getHomeDirectory().resolve("preferences.json"), AppPaths.getPreferencesFilePath());
        assertEquals(AppPaths.getHomeDirectory().resolve("addressbook.log"), AppPaths.getLogFilePath());
    }

    @Test
    public void homeDirectoryFor_nullPaths_rejectsNull() {
        assertThrows(NullPointerException.class, () -> AppPaths.homeDirectoryFor(null, directory));
        assertThrows(NullPointerException.class, () -> AppPaths.homeDirectoryFor(directory, null));
    }

    @Test
    public void homeDirectoryForLocation_escapedOrMalformedUri_decodesOrFailsWithoutFallback() {
        Path home = directory.resolve("release %t with spaces");
        assertEquals(home, AppPaths.homeDirectoryForLocation(home.resolve("Laplace.jar").toUri().toString(),
                directory));
        IllegalStateException failure = assertThrows(IllegalStateException.class, () ->
                AppPaths.homeDirectoryForLocation("file:/invalid%location/Laplace.jar", directory));
        assertEquals("Could not locate the Laplace application folder.", failure.getMessage());
        assertInstanceOf(URISyntaxException.class, failure.getCause());
    }

    @Test
    public void packagedRun_foreignWorkingDirectory_keepsDataPreferencesAndLogsBesideJar() throws Exception {
        Path home = Files.createDirectory(directory.resolve("release %t with spaces"));
        Path launch = Files.createDirectory(directory.resolve("outside"));
        Path jar = home.resolve("Laplace.jar");
        createProbeJar(jar);
        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        Process process = new ProcessBuilder(java, "-cp", jar.toString(), AppPathsProbe.class.getName())
                .directory(launch.toFile()).redirectErrorStream(true).start();
        try {
            assertTrue(process.waitFor(30, TimeUnit.SECONDS), "Packaged sandbox probe timed out");
            assertEquals(0, process.exitValue(), new String(process.getInputStream().readAllBytes()));
        } finally {
            process.destroyForcibly();
        }
        assertEquals("sample data marker", Files.readString(home.resolve("data/addressbook.json")));
        assertEquals("preferences marker", Files.readString(home.resolve("preferences.json")));
        try (var files = Files.list(home)) {
            assertTrue(files.anyMatch(path -> path.getFileName().toString().startsWith("addressbook.log")));
        }
        try (var files = Files.list(launch)) {
            assertTrue(files.findAny().isEmpty(), "Runtime files escaped into the working directory");
        }
    }

    /** Packages the real path resolver and logger, rather than faking their code-source locations. */
    private static void createProbeJar(Path jar) throws IOException {
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(jar))) {
            for (Class<?> type : List.of(AppPaths.class, LogsCenter.class, AppPathsProbe.class)) {
                String resource = type.getName().replace('.', '/') + ".class";
                output.putNextEntry(new JarEntry(resource));
                try (var input = type.getResourceAsStream("/" + resource)) {
                    output.write(input.readAllBytes());
                }
                output.closeEntry();
            }
        }
    }
}
