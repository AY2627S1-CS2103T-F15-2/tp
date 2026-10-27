package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Exercises failure cleanup without depending on platform-specific file permission behavior. */
public class AtomicFileWriterTest {
    @TempDir
    public Path directory;

    @Test
    public void write_existingFile_preservesSupportedAccessPolicy() throws Exception {
        Path target = directory.resolve("data.json");
        Files.writeString(target, "original");
        var posix = Files.getFileAttributeView(target, PosixFileAttributeView.class);
        if (posix != null) {
            posix.setPermissions(PosixFilePermissions.fromString("rw-r-----"));
        }
        var acl = Files.getFileAttributeView(target, AclFileAttributeView.class);
        var expectedAcl = acl == null ? null : acl.getAcl();
        var expectedPosix = posix == null ? null : posix.readAttributes();

        new AtomicFileWriter().write(target, new byte[] {1, 2});

        assertEquals(2, Files.size(target));
        if (posix != null) {
            assertEquals(expectedPosix.permissions(), posix.readAttributes().permissions());
            assertEquals(expectedPosix.owner(), posix.readAttributes().owner());
            assertEquals(expectedPosix.group(), posix.readAttributes().group());
        }
        if (acl != null) {
            assertEquals(expectedAcl, acl.getAcl());
        }
    }

    @Test
    public void write_readOnlyFile_rejectsAndPreservesBytes() throws Exception {
        Path target = directory.resolve("data.json");
        Files.writeString(target, "original");
        var posix = Files.getFileAttributeView(target, PosixFileAttributeView.class);
        var dos = Files.getFileAttributeView(target, DosFileAttributeView.class);
        if (posix != null) {
            posix.setPermissions(PosixFilePermissions.fromString("r--r--r--"));
        } else if (dos != null) {
            dos.setReadOnly(true);
        } else {
            return; // This filesystem exposes neither supported read-only attribute.
        }
        try {
            assertThrows(AccessDeniedException.class, () -> new AtomicFileWriter().write(target, new byte[] {1}));
            assertEquals("original", Files.readString(target));
            try (var files = Files.list(directory)) {
                assertEquals(List.of(target), files.toList());
            }
        } finally {
            if (posix != null) {
                posix.setPermissions(PosixFilePermissions.fromString("rw-------"));
            } else {
                dos.setReadOnly(false);
            }
        }
    }

    @Test
    public void write_newFile_keepsTemporaryDefaults() throws Exception {
        Path target = directory.resolve("new.json");
        Path control = Files.createTempFile(directory, "control", ".tmp");
        new AtomicFileWriter().write(target, new byte[] {1});
        var posix = Files.getFileAttributeView(target, PosixFileAttributeView.class);
        if (posix != null) {
            assertEquals(Files.getPosixFilePermissions(control), posix.readAttributes().permissions());
        }
        assertEquals(1, Files.size(target));
    }

    @Test
    public void write_accessPolicyFails_preservesLiveFileAndCleansCandidate() throws Exception {
        Path target = directory.resolve("data.json");
        Files.writeString(target, "original");
        IOException failure = new IOException("Cannot copy access policy");
        AtomicFileWriter writer = new AtomicFileWriter() {
            @Override
            void preserveAccessPolicy(Path temporary, Path destination) throws IOException {
                throw failure;
            }
        };
        assertSame(failure, assertThrows(IOException.class, () -> writer.write(target, new byte[] {1})));
        assertEquals("original", Files.readString(target));
        try (var files = Files.list(directory)) {
            assertEquals(List.of(target), files.toList());
        }
    }

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
