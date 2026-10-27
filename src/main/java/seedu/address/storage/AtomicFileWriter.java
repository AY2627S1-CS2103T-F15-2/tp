package seedu.address.storage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

/** Replaces a data file atomically; an unsupported atomic move fails without a non-atomic fallback. */
class AtomicFileWriter {
    /** Writes and flushes a sibling file, then installs it with one atomic filesystem operation. */
    void write(Path destination, byte[] content) throws IOException {
        Path target = destination.toAbsolutePath();
        Files.createDirectories(target.getParent());
        Path temporary = Files.createTempFile(target.getParent(), ".laplace-", ".tmp");
        try {
            writeTemporaryFile(temporary, content);
            preserveAccessPolicy(temporary, target);
            replaceFile(temporary, target);
        } catch (IOException | RuntimeException failure) {
            try {
                Files.deleteIfExists(temporary);
            } catch (IOException cleanupFailure) {
                failure.addSuppressed(cleanupFailure);
            }
            throw failure;
        }
    }

    /** Kept separate so tests can reproduce a failure after only part of the candidate has been written. */
    void writeTemporaryFile(Path temporary, byte[] content) throws IOException {
        try (FileChannel channel = FileChannel.open(temporary, StandardOpenOption.WRITE)) {
            ByteBuffer bytes = ByteBuffer.wrap(content);
            while (bytes.hasRemaining()) {
                channel.write(bytes);
            }
            channel.force(true);
        }
    }

    /**
     * Copies the existing access policy before replacement, rejecting an explicitly read-only target.
     * New files retain the temporary file's platform defaults. Metadata failures abort the save.
     */
    void preserveAccessPolicy(Path temporary, Path target) throws IOException {
        try {
            Files.readAttributes(target, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        } catch (NoSuchFileException exception) {
            return;
        }
        PosixFileAttributeView posix = attributeView(target, PosixFileAttributeView.class);
        if (posix != null) {
            var attributes = posix.readAttributes();
            Set<PosixFilePermission> permissions = attributes.permissions();
            if (!permissions.contains(PosixFilePermission.OWNER_WRITE)
                    && !permissions.contains(PosixFilePermission.GROUP_WRITE)
                    && !permissions.contains(PosixFilePermission.OTHERS_WRITE)) {
                throw new AccessDeniedException(target.toString(), null, "Data file is marked read-only");
            }
            var candidate = attributeView(temporary, PosixFileAttributeView.class);
            if (!candidate.readAttributes().owner().equals(attributes.owner())) {
                candidate.setOwner(attributes.owner());
            }
            if (!candidate.readAttributes().group().equals(attributes.group())) {
                candidate.setGroup(attributes.group());
            }
            candidate.setPermissions(permissions);
        }
        AclFileAttributeView acl = attributeView(target, AclFileAttributeView.class);
        if (acl != null) {
            var candidate = attributeView(temporary, AclFileAttributeView.class);
            if (!candidate.getOwner().equals(acl.getOwner())) {
                candidate.setOwner(acl.getOwner());
            }
            candidate.setAcl(acl.getAcl());
        }
        DosFileAttributeView dos = attributeView(target, DosFileAttributeView.class);
        if (dos != null && dos.readAttributes().isReadOnly()) {
            throw new AccessDeniedException(target.toString(), null, "Data file is marked read-only");
        }
    }

    /**
     * Returns a platform attribute view, allowing policy tests to exercise other filesystem capabilities.
     */
    <V extends FileAttributeView> V attributeView(Path path, Class<V> type) {
        return Files.getFileAttributeView(path, type);
    }

    /** A failed replacement must never fall back to truncating or copying over the live data file. */
    void replaceFile(Path temporary, Path target) throws IOException {
        Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    }
}
