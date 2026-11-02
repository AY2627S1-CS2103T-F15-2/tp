package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.testutil.ViewTestData.ALEX;
import static seedu.address.testutil.ViewTestData.CAMERA;
import static seedu.address.testutil.ViewTestData.OTHER_CAMERA;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.loan.Loan;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.model.person.Name;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.ViewTestData;

/** Verifies patronymic names through parsing, transaction publication, and real JSON storage. */
public class MemberPatronymicIntegrationTest {
    @TempDir
    public Path directory;

    @Test
    public void addAndEdit_patronymicNames_preservesIdentityReferencesAndSavedSpelling() throws Exception {
        for (String name : List.of("Ravi s/o Kumar", "Mary D/O Tan", "Ravi S/o Kumar",
                "Muthu a/l Raju", "Priya a/p Suresh", "Muthu A/L Raju", "Priya A/p Suresh")) {
            ModelManager model = ViewTestData.model();
            model.addLoan(ViewTestData.loan(CAMERA, ALEX, true));
            model.addLoan(ViewTestData.loan(OTHER_CAMERA, ALEX, false));
            List<Loan> loans = List.copyOf(model.getLoanList());
            JsonAddressBookStorage records = new JsonAddressBookStorage(directory.resolve("data.json"));
            StorageManager storage = new StorageManager(records,
                    new JsonUserPrefsStorage(directory.resolve("preferences.json")));
            LogicManager logic = new LogicManager(model, storage);
            logic.execute("member add nus/A8400001X name/" + name + " phone/91112222 email/new@example.com");
            logic.execute("member edit nus/A0123456X name/" + name);
            assertEquals(name, model.findMember(new NusId("A8400001X")).orElseThrow().getName().fullName);
            assertEquals(name, model.findMember(ALEX.getNusId()).orElseThrow().getName().fullName);
            assertEquals(ALEX.getPhone(), model.findMember(ALEX.getNusId()).orElseThrow().getPhone());
            assertEquals(ALEX.getEmail(), model.findMember(ALEX.getNusId()).orElseThrow().getEmail());
            assertEquals(loans, model.getLoanList());
            ModelManager reopened = new ModelManager(records.readAddressBook().orElseThrow(), new UserPrefs());
            assertEquals(model.getAddressBook(), reopened.getAddressBook());

            byte[] saved = Files.readAllBytes(records.getAddressBookFilePath());
            AddressBook before = new AddressBook(model.getAddressBook());
            for (String invalid : List.of("Ravi s/only Kumar", "Ravi w/o Kumar", "Ravi / Kumar",
                    "s/o", "D/O", "Tan s/o", "d/o Devi", "a/l", "Priya a/p", "Ravi s/o d/o Kumar")) {
                assertThrows(ParseException.class, () -> logic.execute("member edit nus/A0123456X name/" + invalid));
                assertThrows(ParseException.class, () -> logic.execute("member add nus/A8400002X name/" + invalid
                        + " phone/91113333 email/invalid@example.com"));
                assertEquals(before, model.getAddressBook());
                assertArrayEquals(saved, Files.readAllBytes(records.getAddressBookFilePath()));
            }
            StorageManager failingStorage = new StorageManager(records,
                    new JsonUserPrefsStorage(directory.resolve("preferences.json"))) {
                @Override
                public void saveAddressBook(ReadOnlyAddressBook data) throws IOException {
                    throw new IOException("Injected save failure");
                }
            };
            assertThrows(CommandException.class, () -> new LogicManager(model, failingStorage)
                    .execute("member edit nus/A0123456X name/Another d/o Member"));
            assertEquals(before, model.getAddressBook());
            assertArrayEquals(saved, Files.readAllBytes(records.getAddressBookFilePath()));
        }
    }

    @Test
    public void load_previouslyAcceptedPatronymicNames_preservesSpellingAndLoanReferences() throws Exception {
        for (String name : List.of("s/o", "D/O", "Tan s/o", "d/o Devi")) {
            ModelManager model = ViewTestData.model();
            model.setMember(ALEX, new Member(ALEX.getNusId(),
                    Name.fromStoredMember(name), ALEX.getPhone(), ALEX.getEmail()));
            model.addLoan(ViewTestData.loan(CAMERA, ALEX, true));
            model.addLoan(ViewTestData.loan(OTHER_CAMERA, ALEX, false));
            JsonAddressBookStorage records = new JsonAddressBookStorage(directory.resolve("legacy.json"));
            records.saveAddressBook(model.getAddressBook());
            byte[] saved = Files.readAllBytes(records.getAddressBookFilePath());
            ModelManager reopened = new ModelManager(records.readAddressBook().orElseThrow(), new UserPrefs());
            assertEquals(model.getAddressBook(), reopened.getAddressBook());
            assertEquals(name, reopened.findMember(ALEX.getNusId()).orElseThrow().getName().fullName);
            records.saveAddressBook(reopened.getAddressBook());
            assertArrayEquals(saved, Files.readAllBytes(records.getAddressBookFilePath()));
            LogicManager logic = new LogicManager(reopened, new StorageManager(records,
                    new JsonUserPrefsStorage(directory.resolve("legacy-preferences.json"))));
            logic.execute("member edit nus/A0123456X phone/98765432");
            assertEquals(name, reopened.findMember(ALEX.getNusId()).orElseThrow().getName().fullName);
            assertEquals(model.getLoanList(), reopened.getLoanList());
            assertEquals(reopened.getAddressBook(), records.readAddressBook().orElseThrow());
        }
    }
}
