package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.ReadOnlyAddressBook;

/**
 * A class to access AddressBook data stored as a JSON file on the hard disk.
 */
public class JsonAddressBookStorage {

    private static final Logger logger = LogsCenter.getLogger(JsonAddressBookStorage.class);

    private Path filePath;
    private final AtomicFileWriter fileWriter;
    private final Clock clock;

    public JsonAddressBookStorage(Path filePath) {
        this(filePath, new AtomicFileWriter());
    }

    /** Supplies filesystem operations for deterministic partial-write and replacement-failure tests. */
    JsonAddressBookStorage(Path filePath, AtomicFileWriter fileWriter) {
        this(filePath, fileWriter, Clock.systemDefaultZone());
    }

    /**
     * Supplies a clock for reproducible diagnostics without making valid files depend on today's date.
     */
    JsonAddressBookStorage(Path filePath, AtomicFileWriter fileWriter, Clock clock) {
        this.filePath = requireNonNull(filePath);
        this.fileWriter = requireNonNull(fileWriter);
        this.clock = requireNonNull(clock);
    }

    public Path getAddressBookFilePath() {
        return filePath;
    }

    /**
     * Returns AddressBook data as a {@link ReadOnlyAddressBook}.
     * Returns {@code Optional.empty()} if storage file is not found.
     *
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyAddressBook> readAddressBook() throws DataLoadingException {
        return readAddressBook(filePath);
    }

    /**
     * Similar to {@link #readAddressBook()}.
     *
     * @param filePath location of the data. Cannot be null.
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyAddressBook> readAddressBook(Path filePath) throws DataLoadingException {
        requireNonNull(filePath);

        Optional<JsonSerializableAddressBook> jsonAddressBook = JsonUtil.readJsonFile(
                filePath, JsonSerializableAddressBook.class);
        if (!jsonAddressBook.isPresent()) {
            return Optional.empty();
        }

        try {
            ReadOnlyAddressBook loaded = jsonAddressBook.get().toModelType();
            warnAboutFutureDates(loaded);
            return Optional.of(loaded);
        } catch (IllegalValueException ive) {
            logger.info("Illegal values found in " + filePath + ": " + ive.getMessage());
            throw new DataLoadingException(ive);
        }
    }

    /**
     * Reports suspicious recorded events while preserving files that predate a clock rollback.
     */
    private void warnAboutFutureDates(ReadOnlyAddressBook loaded) {
        LocalDate today = LocalDate.now(clock);
        loaded.getLoanList().forEach(loan -> {
            if (loan.getAssignedDate().isAfter(today)
                    || loan.getReturnedDate().filter(date -> date.isAfter(today)).isPresent()) {
                logger.warning("Loan equipment " + loan.getEquipmentUuid() + ", member " + loan.getMemberNusId()
                        + ": recorded assignment " + loan.getAssignedDate() + ", actual return "
                        + loan.getReturnedDate().map(Object::toString).orElse("not recorded")
                        + " is after the computer date " + today + ". Verify the computer date or recover "
                        + "a backed-up data file. Records were retained; future expected return dates are allowed.");
            }
        });
    }

    /**
     * Saves the given {@link ReadOnlyAddressBook} to the storage.
     * @param addressBook cannot be null.
     * @throws IOException if there was any problem writing to the file.
     */
    public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
        saveAddressBook(addressBook, filePath);
    }

    /**
     * Similar to {@link #saveAddressBook(ReadOnlyAddressBook)}.
     *
     * @param filePath location of the data. Cannot be null.
     */
    public void saveAddressBook(ReadOnlyAddressBook addressBook, Path filePath) throws IOException {
        requireNonNull(addressBook);
        requireNonNull(filePath);

        // Serialize before touching the destination; partial writes can only affect a private sibling file.
        String json = JsonUtil.toJsonString(new JsonSerializableAddressBook(addressBook));
        fileWriter.write(filePath, json.getBytes(StandardCharsets.UTF_8));
    }

}
