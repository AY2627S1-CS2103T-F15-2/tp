package seedu.address.storage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.DosFileAttributeView;
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
        if (!Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
            return;
        }
        PosixFileAttributeView posix = Files.getFileAttributeView(target, PosixFileAttributeView.class);
        if (posix != null) {
            var attributes = posix.readAttributes();
            Set<PosixFilePermission> permissions = attributes.permissions();
            if (!permissions.contains(PosixFilePermission.OWNER_WRITE)
                    && !permissions.contains(PosixFilePermission.GROUP_WRITE)
                    && !permissions.contains(PosixFilePermission.OTHERS_WRITE)) {
                throw new AccessDeniedException(target.toString(), null, "Data file is marked read-only");
            }
            var candidate = Files.getFileAttributeView(temporary, PosixFileAttributeView.class);
            if (!candidate.readAttributes().owner().equals(attributes.owner())) {
                candidate.setOwner(attributes.owner());
            }
            if (!candidate.readAttributes().group().equals(attributes.group())) {
                candidate.setGroup(attributes.group());
            }
            candidate.setPermissions(permissions);
        }
        AclFileAttributeView acl = Files.getFileAttributeView(target, AclFileAttributeView.class);
        if (acl != null) {
            var candidate = Files.getFileAttributeView(temporary, AclFileAttributeView.class);
            if (!candidate.getOwner().equals(acl.getOwner())) {
                candidate.setOwner(acl.getOwner());
            }
            candidate.setAcl(acl.getAcl());
        }
        DosFileAttributeView dos = Files.getFileAttributeView(target, DosFileAttributeView.class);
        if (dos != null && dos.readAttributes().isReadOnly()) {
            throw new AccessDeniedException(target.toString(), null, "Data file is marked read-only");
        }
    }

    /** A failed replacement must never fall back to truncating or copying over the live data file. */
    void replaceFile(Path temporary, Path target) throws IOException {
        Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    }
}
