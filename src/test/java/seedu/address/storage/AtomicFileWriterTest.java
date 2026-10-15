package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Exercises failure cleanup without depending on platform-specific file permission behavior. */
public class AtomicFileWriterTest {
    @TempDir
    public Path directory;

    @Test
    public void write_cleanupFails_preservesOriginalFailureAndLiveFile() throws Exception {
        Path target = directory.resolve("data.json");
        Files.writeString(target, "original bytes");
        IOException failure = new IOException("Candidate write failed");
        AtomicFileWriter writer = new AtomicFileWriter() {
            @Override
            void writeTemporaryFile(Path temporary, byte[] content) throws IOException {
                // A nonempty directory makes deletion fail on all three supported operating systems.
                Files.delete(temporary);
                Files.createDirectory(temporary);
                Files.writeString(temporary.resolve("remaining.txt"), "candidate bytes");
                throw failure;
            }
        };

        IOException actual = assertThrows(IOException.class, () -> writer.write(target, new byte[] {1}));
        assertSame(failure, actual);
        assertEquals(1, actual.getSuppressed().length);
        assertInstanceOf(DirectoryNotEmptyException.class, actual.getSuppressed()[0]);
        assertEquals("original bytes", Files.readString(target));
    }

    @Test
    public void write_unexpectedRuntimeFailure_removesCandidateAndPreservesLiveFile() throws Exception {
        Path target = directory.resolve("data.json");
        Files.writeString(target, "original bytes");
        IllegalStateException failure = new IllegalStateException("Unexpected candidate failure");
        AtomicFileWriter writer = new AtomicFileWriter() {
            @Override
            void writeTemporaryFile(Path temporary, byte[] content) {
                throw failure;
            }
        };

        assertSame(failure, assertThrows(IllegalStateException.class, () -> writer.write(target, new byte[] {1})));
        assertEquals("original bytes", Files.readString(target));
        try (var files = Files.list(directory)) {
            assertEquals(List.of(target), files.toList());
        }
    }
}
