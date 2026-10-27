package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Exercises supported access-policy views on every OS, alongside the real-filesystem integration tests.
 */
public class AtomicAccessPolicyTest {
    @TempDir
    public Path directory;

    @Test
    public void write_posixOwnershipAndWritePolicies_copyIdentityAndEachWriteBitWithoutTruncating() throws Exception {
        Path target = directory.resolve("data.json");
        UserPrincipal owner = () -> "existing owner";
        GroupPrincipal group = () -> "existing group";
        for (String mode : List.of("rw-------", "r--rw----", "r-----rw-")) {
            Files.writeString(target, "original");
            PosixView existing = new PosixView(owner, group, PosixFilePermissions.fromString(mode));
            PosixView candidate = new PosixView(() -> "new owner", () -> "new group", Set.of());
            policyWriter(target, Map.of(PosixFileAttributeView.class, existing),
                    Map.of(PosixFileAttributeView.class, candidate)).write(target, new byte[] {1, 2});
            assertEquals(owner, candidate.owner);
            assertEquals(group, candidate.group);
            assertEquals(existing.permissions, candidate.permissions);
            assertEquals(2, Files.size(target));
        }
    }

    @Test
    public void write_aclSameOrDifferentOwner_preservesAclBeforeInstallingCandidate() throws Exception {
        Path target = directory.resolve("data.json");
        UserPrincipal owner = () -> "existing owner";
        AclEntry entry = AclEntry.newBuilder().setType(AclEntryType.ALLOW).setPrincipal(owner)
                .setPermissions(AclEntryPermission.READ_DATA, AclEntryPermission.WRITE_DATA).build();
        for (UserPrincipal candidateOwner : List.<UserPrincipal>of(owner, () -> "new owner")) {
            Files.writeString(target, "original");
            AclView existing = new AclView(owner, List.of(entry));
            AclView candidate = new AclView(candidateOwner, List.of());
            policyWriter(target, Map.of(AclFileAttributeView.class, existing),
                    Map.of(AclFileAttributeView.class, candidate)).write(target, new byte[] {1, 2});
            assertEquals(owner, candidate.owner);
            assertEquals(existing.entries, candidate.entries);
            assertEquals(2, Files.size(target));
        }
    }

    @Test
    public void write_aclApplicationFails_keepsPreviousBytesAndRemovesCandidate() throws Exception {
        Path target = directory.resolve("data.json");
        Files.writeString(target, "original");
        UserPrincipal owner = () -> "existing owner";
        AclView existing = new AclView(owner, List.of());
        AclView candidate = new AclView(owner, List.of());
        candidate.failure = new IOException("ACL cannot be applied");
        AtomicFileWriter writer = policyWriter(target, Map.of(AclFileAttributeView.class, existing),
                Map.of(AclFileAttributeView.class, candidate));
        assertSame(candidate.failure, assertThrows(IOException.class, () -> writer.write(target, new byte[] {1})));
        assertEquals("original", Files.readString(target));
        try (var files = Files.list(directory)) {
            assertEquals(List.of(target), files.toList());
        }
    }

    @Test
    public void write_dosReadOnlyAttribute_rejectsMarkedTargetAndAcceptsWritableTarget() throws Exception {
        Path target = directory.resolve("data.json");
        for (boolean isReadOnly : List.of(false, true)) {
            Files.writeString(target, "original");
            DosFileAttributes attributes = attributes(DosFileAttributes.class, Map.of("isReadOnly", isReadOnly));
            DosFileAttributeView view = attributes(DosFileAttributeView.class, Map.of("readAttributes", attributes));
            AtomicFileWriter writer = policyWriter(target, Map.of(DosFileAttributeView.class, view), Map.of());
            if (isReadOnly) {
                assertThrows(AccessDeniedException.class, () -> writer.write(target, new byte[] {1}));
                assertEquals("original", Files.readString(target));
                try (var files = Files.list(directory)) {
                    assertEquals(List.of(target), files.toList());
                }
            } else {
                writer.write(target, new byte[] {1});
                assertEquals(1, Files.size(target));
            }
        }
    }

    @Test
    public void write_noSupportedViews_retainsPlatformDefaults() throws Exception {
        Path target = directory.resolve("data.json");
        Files.writeString(target, "original");
        policyWriter(target, Map.of(), Map.of()).write(target, new byte[] {1});
        assertEquals(1, Files.size(target));
    }

    /**
     * Supplies view capabilities independently of the host OS while retaining real atomic file I/O.
     */
    private static AtomicFileWriter policyWriter(Path target, Map<Class<?>, FileAttributeView> existing,
            Map<Class<?>, FileAttributeView> candidate) {
        return new AtomicFileWriter() {
            @Override
            <V extends FileAttributeView> V attributeView(Path path, Class<V> type) {
                return type.cast((path.equals(target.toAbsolutePath()) ? existing : candidate).get(type));
            }
        };
    }

    /**
     * Supplies only the immutable attribute getters used by the policy, rejecting unexpected operations.
     */
    private static <T> T attributes(Class<T> type, Map<String, ?> values) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, (proxy, method, args) -> {
            if (!values.containsKey(method.getName())) {
                throw new UnsupportedOperationException(method.getName());
            }
            return values.get(method.getName());
        }));
    }

    /**
     * Records POSIX access-policy application without needing permission to change real OS ownership.
     */
    private static class PosixView implements PosixFileAttributeView {
        private UserPrincipal owner;
        private GroupPrincipal group;
        private Set<PosixFilePermission> permissions;

        PosixView(UserPrincipal owner, GroupPrincipal group, Set<PosixFilePermission> permissions) {
            this.owner = owner;
            this.group = group;
            this.permissions = permissions;
        }

        @Override
        public String name() {
            return "posix";
        }

        @Override
        public PosixFileAttributes readAttributes() {
            return attributes(PosixFileAttributes.class,
                    Map.of("owner", owner, "group", group, "permissions", permissions));
        }

        @Override
        public UserPrincipal getOwner() {
            return owner;
        }

        @Override
        public void setOwner(UserPrincipal newOwner) {
            owner = newOwner;
        }

        @Override
        public void setGroup(GroupPrincipal newGroup) {
            group = newGroup;
        }

        @Override
        public void setPermissions(Set<PosixFilePermission> newPermissions) {
            permissions = Set.copyOf(newPermissions);
        }

        @Override
        public void setTimes(FileTime modified, FileTime accessed, FileTime created) {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Records ACL replacement and reproduces an actual attribute-application failure.
     */
    private static class AclView implements AclFileAttributeView {
        private UserPrincipal owner;
        private List<AclEntry> entries;
        private IOException failure;

        AclView(UserPrincipal owner, List<AclEntry> entries) {
            this.owner = owner;
            this.entries = entries;
        }

        @Override
        public String name() {
            return "acl";
        }

        @Override
        public UserPrincipal getOwner() {
            return owner;
        }

        @Override
        public void setOwner(UserPrincipal newOwner) {
            owner = newOwner;
        }

        @Override
        public List<AclEntry> getAcl() {
            return entries;
        }

        @Override
        public void setAcl(List<AclEntry> newEntries) throws IOException {
            if (failure != null) {
                throw failure;
            }
            entries = List.copyOf(newEntries);
        }
    }
}
