package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.ViewTestData;

/** Verifies rejected email input cannot mutate or save a real member/loan dataset. */
public class MemberEmailValidationIntegrationTest {
    @TempDir
    public Path directory;

    @Test
    public void execute_kelvinSignEmail_rejectsAddAndEditWithoutChangingMemoryOrDisk() throws Exception {
        ModelManager model = ViewTestData.model();
        model.addLoan(ViewTestData.loan(ViewTestData.CAMERA, ViewTestData.ALEX, true));
        JsonAddressBookStorage disk = new JsonAddressBookStorage(directory.resolve("data.json"));
        StorageManager storage = new StorageManager(disk,
                new JsonUserPrefsStorage(directory.resolve("prefs.json")));
        storage.saveAddressBook(model.getAddressBook());
        LogicManager logic = new LogicManager(model, storage);
        AddressBook before = new AddressBook(model.getAddressBook());
        byte[] bytes = Files.readAllBytes(disk.getAddressBookFilePath());
        for (String command : List.of(
                "member add nus/A8000099X name/Kelvin Probe phone/91234567 email/K@example.com",
                "member edit nus/A0123456X email/K.updated@example.com")) {
            assertThrows(ParseException.class, () -> logic.execute(command));
            assertEquals(before, model.getAddressBook());
            assertArrayEquals(bytes, Files.readAllBytes(disk.getAddressBookFilePath()));
        }
        logic.execute("member edit nus/A0123456X email/ALEX.UPDATED@EXAMPLE.COM");
        assertEquals("alex.updated@example.com", model.findMember(ViewTestData.ALEX.getNusId())
                .orElseThrow().getEmail().value);
        assertEquals(before.getLoanList(), model.getLoanList());
        assertEquals(model.getAddressBook(), disk.readAddressBook().orElseThrow());
        assertEquals(model.getAddressBook(), new ModelManager(disk.readAddressBook().orElseThrow(),
                new UserPrefs()).getAddressBook());
    }
}
