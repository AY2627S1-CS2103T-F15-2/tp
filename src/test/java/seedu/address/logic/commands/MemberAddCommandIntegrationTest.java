package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.MemberAddCommandParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.member.NusId;
import seedu.address.storage.JsonAddressBookStorage;

/**
 * Verifies direct parser/command/storage component integration before family routing is activated.
 */
public class MemberAddCommandIntegrationTest {

    @TempDir
    public Path testFolder;

    @Test
    public void addSaveReload_realStorage_retainsNormalizedIdentityAndContacts() throws Exception {
        ModelManager model = new ModelManager();
        MemberAddCommandParser parser = new MemberAddCommandParser();
        parser.parse("nus/a0123456x name/Alex Tan phone/91234567 email/alex@example.com").execute(model);
        parser.parse("nus/A0234567R name/Alex Tan phone/98765432 email/second@example.com").execute(model);
        Path file = testFolder.resolve("members.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        storage.saveAddressBook(model.getAddressBook());
        ModelManager reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(model.getAddressBook(), reloaded.getAddressBook());
        assertEquals(2, reloaded.getMemberList().size());
        assertEquals("alex@example.com", reloaded.findMember(new NusId("A0123456X"))
                .orElseThrow().getEmail().value);

        String before = Files.readString(file);
        assertThrows(CommandException.class, () -> parser.parse(
                "nus/a0123456x name/Other Name phone/99999999 email/other@example.com").execute(reloaded));
        assertEquals(model.getAddressBook(), reloaded.getAddressBook());
        assertEquals(before, Files.readString(file));
    }

    @Test
    public void parse_invalidArguments_rejectsBeforeMutationOrSaving() throws Exception {
        ModelManager model = new ModelManager();
        MemberAddCommandParser parser = new MemberAddCommandParser();
        parser.parse("nus/A0123456X name/Alex Tan phone/91234567 email/alex@example.com").execute(model);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(testFolder.resolve("unchanged.json"));
        storage.saveAddressBook(model.getAddressBook());
        AddressBook before = new AddressBook(model.getAddressBook());
        String saved = Files.readString(storage.getAddressBookFilePath());
        assertThrows(ParseException.class, () -> parser.parse(
                "nus/A0234567R name/Alex Tan phone/invalid email/alex@example.com").execute(model));
        assertEquals(before, model.getAddressBook());
        assertEquals(saved, Files.readString(storage.getAddressBookFilePath()));
    }
}
