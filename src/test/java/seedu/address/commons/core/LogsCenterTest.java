package seedu.address.commons.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Checks that file-log failures leave the existing logger operational without creating fallback files. */
public class LogsCenterTest {
    @TempDir
    public Path directory;

    @Test
    public void addFileHandler_missingParent_reportsWarningWithoutCreatingFallbackFiles() {
        Logger logger = Logger.getAnonymousLogger();
        logger.setUseParentHandlers(false);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        StreamHandler recording = new StreamHandler(output, new SimpleFormatter());
        logger.addHandler(recording);
        Path missingParent = directory.resolve("missing");
        try {
            LogsCenter.addFileHandler(logger, missingParent.resolve("addressbook.log"));
            recording.flush();
            assertTrue(output.toString(StandardCharsets.UTF_8)
                    .contains("Could not create a log file in the application folder."));
            assertEquals(1, logger.getHandlers().length);
            assertEquals(recording, logger.getHandlers()[0]);
            assertFalse(Files.exists(missingParent));
        } finally {
            recording.close();
        }
    }
}
