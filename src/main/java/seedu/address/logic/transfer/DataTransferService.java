package seedu.address.logic.transfer;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

import seedu.address.commons.core.AppPaths;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.CommittedReplacementResult;
import seedu.address.logic.commands.DataCommand;
import seedu.address.logic.commands.DisplayRequest;
import seedu.address.logic.commands.TransferPreviewResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.commands.exceptions.DataTransferException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.storage.Storage;
import seedu.address.storage.TransferSnapshotStorage;

/** Coordinates previews and snapshot files; only confirmed, persisted replacements publish live state. */
public class DataTransferService {
    private final Storage storage;
    private final TransferSnapshotStorage snapshots;
    private final Path home;
    private AddressBook pending;
    private AddressBook baseline;

    /** Creates a session using the application's existing runtime home and JSON snapshot format. */
    public DataTransferService(Storage storage) {
        this(storage, new TransferSnapshotStorage(), AppPaths.getHomeDirectory());
    }

    /** Supplies snapshot I/O and runtime home for deterministic filesystem and failure tests. */
    public DataTransferService(Storage storage, TransferSnapshotStorage snapshots, Path home) {
        this.storage = requireNonNull(storage);
        this.snapshots = requireNonNull(snapshots);
        this.home = requireNonNull(home);
    }

    /** Executes a transfer without an unrelated application save or history entry. */
    public CommandResult execute(DataCommand command, Model model) throws CommandException {
        boolean hadPreview = pending != null;
        try {
            return switch (command.getAction()) {
                case IMPORT, RESTORE -> preview(command.getPath(), model);
                case EXPORT -> export(command.getPath(), model.getAddressBook());
                case BACKUP -> new CommandResult("Backup saved to " + backup(model.getAddressBook()),
                        DisplayRequest.unchanged());
                case CONFIRM -> confirm(model);
                case CANCEL -> cancel();
            };
        } catch (IOException | IllegalValueException exception) {
            throw new DataTransferException("Data transfer failed: " + exception.getMessage(), exception,
                    hadPreview && pending == null);
        }
    }

    /** Discards pending confirmation after a successfully committed application data change. */
    public void invalidate() {
        pending = null;
        baseline = null;
    }

    private CommandResult preview(Path source, Model model) throws IOException, IllegalValueException {
        // A failed replacement preview must not leave an older, invisible request confirmable.
        invalidate();
        Path resolved = home.resolve(source).toAbsolutePath().normalize();
        AddressBook candidate = new AddressBook(snapshots.read(resolved));
        baseline = new AddressBook(model.getAddressBook());
        pending = candidate;
        return new TransferPreviewResult(DataTransferPreview.from(resolved, candidate));
    }

    private CommandResult export(Path path, ReadOnlyAddressBook data) throws IOException {
        Path destination = home.resolve(path).toAbsolutePath().normalize();
        if (destination.equals(storage.getAddressBookFilePath().toAbsolutePath().normalize())) {
            throw new IOException("Export destination is the active data file.");
        }
        snapshots.writeNew(destination, data);
        return new CommandResult("Snapshot exported to " + destination, DisplayRequest.unchanged());
    }

    private Path backup(ReadOnlyAddressBook data) throws IOException {
        Path directory = storage.getAddressBookFilePath().toAbsolutePath().getParent().resolve("backups");
        Path destination = directory.resolve("laplace-" + UUID.randomUUID() + ".json");
        snapshots.writeNew(destination, data);
        return destination;
    }

    private CommandResult confirm(Model model) throws IOException, CommandException {
        if (pending == null) {
            throw new CommandException("No pending preview. Use data import or data restore first.");
        }
        if (!baseline.equals(model.getAddressBook())) {
            invalidate();
            throw new DataTransferException("Data changed after the preview. Preview the snapshot again.", null, true);
        }
        AddressBook before = new AddressBook(model.getAddressBook());
        Path backupPath = backup(before);
        // The normal atomic persistence boundary is reused, with no extra save by LogicManager.
        storage.saveAddressBook(pending);
        model.setAddressBook(pending);
        CommandResult result = new CommittedReplacementResult(
                "Snapshot applied. Previous data backed up to " + backupPath, before, model.getAddressBook());
        invalidate();
        return result;
    }

    private CommandResult cancel() {
        invalidate();
        return new CommandResult("Data transfer cancelled. Current data is unchanged.", DisplayRequest.members());
    }
}
