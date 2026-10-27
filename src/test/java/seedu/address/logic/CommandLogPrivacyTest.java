package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Checks actual formatted file logs at every level for accepted and rejected private input.
 */
public class CommandLogPrivacyTest {
    @TempDir
    public Path directory;

    @Test
    public void execute_privateInput_omitsValuesFromPersistedDiagnostics() throws Exception {
        LogicManager logic = new LogicManager(new ModelManager(), new StorageManager(
                new JsonAddressBookStorage(directory.resolve("records.json")),
                new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
        List<Logger> loggers = List.of(LogsCenter.getLogger(LogicManager.class),
                LogsCenter.getLogger(AddressBookParser.class));
        List<Level> previousLevels = loggers.stream().map(Logger::getLevel).toList();
        Path log = directory.resolve("diagnostics.log");
        FileHandler handler = new FileHandler(log.toString());
        handler.setFormatter(new SimpleFormatter());
        handler.setLevel(Level.ALL);
        try {
            for (Logger logger : loggers) {
                logger.setLevel(Level.ALL);
                logger.addHandler(handler);
            }
            logic.execute("member add nus/A0000003X name/Privacy Example phone/91238888"
                    + " email/privacy-audit@example.com");
            logic.execute("member edit nus/A0000003X name/Changed Person phone/92348888"
                    + " email/changed-audit@example.com");
            logic.execute("member search q/Privacy Example");
            assertThrows(ParseException.class, () -> logic.execute("member add nus/INVALID"
                    + " name/Rejected Person phone/93458888 email/rejected-audit@example.com"));
            assertThrows(ParseException.class, () -> logic.execute("unknown-private@example.com name/Secret"));
        } finally {
            for (int index = 0; index < loggers.size(); index++) {
                loggers.get(index).removeHandler(handler);
                loggers.get(index).setLevel(previousLevels.get(index));
            }
            handler.close();
        }
        String diagnostic = Files.readString(log);
        assertTrue(diagnostic.contains("Received command input."));
        assertTrue(diagnostic.contains("MemberAddCommand"));
        assertTrue(diagnostic.contains("Rejected an unknown command."));
        for (String secret : List.of("Privacy Example", "Changed Person", "Rejected Person", "Secret",
                "91238888", "92348888", "93458888", "privacy-audit@example.com", "changed-audit@example.com",
                "rejected-audit@example.com", "unknown-private@example.com", "A0000003X", "INVALID")) {
            assertFalse(diagnostic.contains(secret), secret);
        }
    }
}
