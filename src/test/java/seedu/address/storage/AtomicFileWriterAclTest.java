package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryFlag;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.util.EnumSet;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Checks the real Windows inherited-ACL interval before and after candidate content is written.
 */
public class AtomicFileWriterAclTest {
    @TempDir
    public Path directory;

    @Test
    public void write_readableParent_restrictsCandidateThroughoutWriteForExistingAndNewFiles() throws Exception {
        assumeTrue(System.getProperty("os.name").startsWith("Windows"), "Requires real Windows ACL inheritance");
        Path shared = Files.createDirectory(directory.resolve("shared"));
        AclFileAttributeView parent = Files.getFileAttributeView(shared, AclFileAttributeView.class);
        assumeTrue(parent != null, "Requires an ACL-capable filesystem");
        UserPrincipal owner = parent.getOwner();
        UserPrincipal everyone = shared.getFileSystem().getUserPrincipalLookupService()
                .lookupPrincipalByName("Everyone");
        AclEntry ownerAccess = AclEntry.newBuilder().setType(AclEntryType.ALLOW).setPrincipal(owner)
                .setPermissions(EnumSet.allOf(AclEntryPermission.class))
                .setFlags(AclEntryFlag.FILE_INHERIT, AclEntryFlag.DIRECTORY_INHERIT).build();
        AclEntry inheritedRead = AclEntry.newBuilder().setType(AclEntryType.ALLOW).setPrincipal(everyone)
                .setPermissions(AclEntryPermission.READ_DATA, AclEntryPermission.READ_ATTRIBUTES,
                        AclEntryPermission.READ_ACL, AclEntryPermission.SYNCHRONIZE)
                .setFlags(AclEntryFlag.FILE_INHERIT, AclEntryFlag.DIRECTORY_INHERIT).build();
        parent.setAcl(List.of(ownerAccess, inheritedRead));
        Path control = Files.createTempFile(shared, "control", ".tmp");
        assertTrue(hasReadAccess(control, everyone), "The fixture must reproduce inherited read access");
        Files.delete(control);

        byte[] content = "candidate data".getBytes(StandardCharsets.UTF_8);
        for (boolean existing : List.of(true, false)) {
            Path target = shared.resolve(existing ? "existing.json" : "new.json");
            if (existing) {
                Files.createFile(target);
                AclFileAttributeView policy = Files.getFileAttributeView(target, AclFileAttributeView.class);
                policy.setAcl(List.of(AclEntry.newBuilder(ownerAccess).setFlags().build()));
                Files.writeString(target, "previous data");
                assertFalse(hasReadAccess(target, everyone));
            }
            AtomicFileWriter writer = new AtomicFileWriter() {
                @Override
                void writeTemporaryFile(Path temporary, byte[] bytes) throws IOException {
                    assertEquals(0, Files.size(temporary));
                    assertOwnerOnly(temporary);
                    super.writeTemporaryFile(temporary, bytes);
                    assertArrayEquals(content, Files.readAllBytes(temporary));
                    assertOwnerOnly(temporary);
                }
            };
            writer.write(target, content);
            assertArrayEquals(content, Files.readAllBytes(target));
            assertOwnerOnly(target);
            try (var files = Files.list(shared)) {
                assertFalse(files.anyMatch(path -> path.getFileName().toString().startsWith(".laplace-")));
            }
        }
    }

    /**
     * Returns whether an allow entry grants this principal direct read access to file contents.
     */
    private static boolean hasReadAccess(Path path, UserPrincipal principal) throws IOException {
        return Files.getFileAttributeView(path, AclFileAttributeView.class).getAcl().stream()
                .anyMatch(entry -> entry.type() == AclEntryType.ALLOW && entry.principal().equals(principal)
                        && entry.permissions().contains(AclEntryPermission.READ_DATA));
    }

    /**
     * Requires content-read grants to be limited to the candidate's actual owner, without assuming an account name.
     */
    private static void assertOwnerOnly(Path path) throws IOException {
        AclFileAttributeView view = Files.getFileAttributeView(path, AclFileAttributeView.class);
        UserPrincipal owner = view.getOwner();
        assertTrue(hasReadAccess(path, owner));
        assertTrue(view.getAcl().stream().filter(entry -> entry.type() == AclEntryType.ALLOW)
                .filter(entry -> entry.permissions().contains(AclEntryPermission.READ_DATA))
                .allMatch(entry -> entry.principal().equals(owner)), "Non-owner can read candidate contents");
    }
}
