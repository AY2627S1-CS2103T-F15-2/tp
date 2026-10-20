package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Validates the delivered artifact, without adding the Gradle JavaFX test dependencies to its runtime. */
public class StandaloneJarTest {
    @TempDir
    public Path directory;

    @Test
    public void jar_universalNativesAndSize_preservesBothMacArchitecturesWithinProductLimit() throws Exception {
        Path artifact = Path.of("build/libs/Laplace.jar");
        assertTrue(Files.size(artifact) < 100_000_000);
        try (JarFile jar = new JarFile(artifact.toFile())) {
            List<String> natives = jar.stream().map(entry -> entry.getName())
                    .filter(name -> name.endsWith(".dylib")).toList();
            assertTrue(natives.contains("libglass.dylib"));
            for (String name : natives) {
                byte[] bytes = jar.getInputStream(jar.getJarEntry(name)).readAllBytes();
                ByteBuffer header = ByteBuffer.wrap(bytes);
                assertEquals(0xcafebabe, header.getInt());
                assertEquals(2, header.getInt());
                int intelCpu = header.getInt(8);
                int armCpu = header.getInt(28);
                assertEquals(Set.of(0x01000007, 0x0100000c), Set.of(intelCpu, armCpu));
                for (int entry : List.of(8, 28)) {
                    int offset = header.getInt(entry + 8);
                    int length = header.getInt(entry + 12);
                    assertEquals(0, offset % 16384);
                    assertTrue(offset + length <= bytes.length);
                }
            }
        }
    }

    @Test
    public void jar_javaSeOnlyLaunch_executesTransfersAndScaledLayoutsWithoutExternalDependencies() throws Exception {
        Path artifact = directory.resolve("Laplace.jar");
        Files.copy(Path.of("build/libs/Laplace.jar"), artifact);
        Path tests = Path.of(StandaloneAppProbe.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        Path log = directory.resolve("standalone-session.txt");
        Process process = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "--limit-modules=java.se,jdk.unsupported", "--enable-native-access=ALL-UNNAMED",
                "-Djavafx.cachedir=" + directory.resolve("native-cache"),
                "-cp", artifact + java.io.File.pathSeparator + tests, StandaloneAppProbe.class.getName())
                .directory(directory.toFile()).redirectErrorStream(true).redirectOutput(log.toFile()).start();
        try {
            assertTrue(process.waitFor(60, TimeUnit.SECONDS), "Standalone JAR timed out: " + log);
            assertEquals(0, process.exitValue(), Files.readString(log));
        } finally {
            process.destroyForcibly();
        }
        String session = Files.readString(log);
        assertTrue(session.contains("PASS: standalone Java SE transfers and scaled viewports"), session);
        Path report = Path.of("build/reports/laplace/standalone-session.txt");
        Files.createDirectories(report.getParent());
        Files.copy(log, report, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        for (int scale : List.of(100, 125, 150)) {
            Files.copy(directory.resolve("viewport-" + scale + ".png"),
                    report.getParent().resolve("viewport-" + scale + ".png"),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
