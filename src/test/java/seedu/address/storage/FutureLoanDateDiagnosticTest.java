package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.node.ObjectNode;

import seedu.address.model.AddressBook;

/**
 * Verifies future recorded events warn without rejecting history after a clock rollback.
 */
public class FutureLoanDateDiagnosticTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-20T00:00:00Z"), ZoneOffset.UTC);

    @TempDir
    public Path directory;

    @Test
    public void read_futureRecordedEvents_warnsWithIdentityAndPreservesHistory() throws Exception {
        for (String field : List.of("assignedDate", "returnedDate")) {
            ObjectNode json = RootPersistenceTestData.jsonSnapshot();
            ObjectNode loan = (ObjectNode) json.get("loans").get(field.equals("assignedDate") ? 0 : 1);
            loan.put(field, "2099-01-01");
            if (field.equals("assignedDate")) {
                loan.put("expectedReturnDate", "2099-01-02");
            }
            Path file = directory.resolve(field + ".json");
            String original = json.toString();
            Files.writeString(file, original);
            var storage = new JsonAddressBookStorage(file, new AtomicFileWriter(), CLOCK);
            List<String> messages = new ArrayList<>();
            Logger logger = Logger.getLogger("ab3.JsonAddressBookStorage");
            Handler handler = warningHandler(messages);
            logger.addHandler(handler);
            try {
                var loaded = storage.readAddressBook().orElseThrow();
                assertEquals(2, loaded.getLoanList().size());
                assertEquals(original, Files.readString(file));
                assertEquals(1, messages.size());
                String message = messages.get(0);
                assertTrue(message.contains(loan.get("equipmentUuid").asText()));
                assertTrue(message.contains(loan.get("memberNusId").asText()));
                assertTrue(message.contains("2099-01-01"));
                assertTrue(message.contains("Verify the computer date or recover"));
                storage.saveAddressBook(loaded);
                assertEquals(new AddressBook(loaded), new AddressBook(storage.readAddressBook().orElseThrow()));
            } finally {
                logger.removeHandler(handler);
            }
        }
    }

    @Test
    public void read_futureExpectedDateAndToday_doesNotWarn() throws Exception {
        ObjectNode json = RootPersistenceTestData.jsonSnapshot();
        ObjectNode loan = (ObjectNode) json.get("loans").get(0);
        loan.put("assignedDate", "2026-10-20");
        loan.put("expectedReturnDate", "2099-01-01");
        Path file = directory.resolve("valid.json");
        Files.writeString(file, json.toString());
        var storage = new JsonAddressBookStorage(file, new AtomicFileWriter(), CLOCK);
        List<String> messages = new ArrayList<>();
        Logger logger = Logger.getLogger("ab3.JsonAddressBookStorage");
        Handler handler = warningHandler(messages);
        logger.addHandler(handler);
        try {
            storage.readAddressBook().orElseThrow();
            assertTrue(messages.isEmpty());
        } finally {
            logger.removeHandler(handler);
        }
    }

    /**
     * Captures only actionable warnings, without modifying logger configuration or persistent logs.
     */
    private Handler warningHandler(List<String> messages) {
        return new Handler() {
            @Override
            public void publish(LogRecord record) {
                if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
                    messages.add(record.getMessage());
                }
            }

            @Override
            public void flush() { }

            @Override
            public void close() { }
        };
    }
}
