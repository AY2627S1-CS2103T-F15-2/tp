package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DisplayRequest;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.equipment.Availability;
import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.loan.Loan;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class LaplaceListIntegrationTest {

    @TempDir
    public Path temporaryFolder;

    private ModelManager model;
    private CountingStorage storage;
    private LogicManager logic;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        storage = new CountingStorage(temporaryFolder);
        logic = new LogicManager(model, storage);
        model.addPerson(new PersonBuilder().build());
        model.updateFilteredPersonList(person -> false);
        for (int id : new int[] {2, 1}) {
            Member member = new Member(new NusId("A000000" + id + "X"), new Name("Alex"),
                    new Phone("91234567"), new Email("alex@example.com"));
            model.addMember(member);
            model.addEquipment(new Equipment(new UUID(0, id), "Camera", "Photography",
                    id == 1 ? Condition.GOOD : Condition.DAMAGED, "notes"));
        }
        LocalDate date = LocalDate.of(2026, 1, 1);
        model.addLoan(new Loan(new UUID(0, 1), new NusId("A0000001X"), date, date.plusDays(1), null));
        model.addLoan(new Loan(new UUID(0, 2), new NusId("A0000002X"), date, date, date));
    }

    @Test
    public void execute_listRoutes_preservesAndReloadsAllDataThroughRealStorage() throws Exception {
        AddressBook before = new AddressBook(model.getAddressBook());
        assertEquals(new CommandResult("Listed 2 active members.", DisplayRequest.members()),
                logic.execute("member\tlist \t"));
        assertEquals(new CommandResult("Listed 2 equipment items.", DisplayRequest.equipment()),
                logic.execute("equipment list"));
        assertEquals(2, storage.saves);
        assertEquals(before, model.getAddressBook());
        assertTrue(model.getFilteredPersonList().isEmpty());
        ModelManager reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(before, reloaded.getAddressBook());
        assertEquals(List.copyOf(before.getMemberList()), reloaded.getMemberList());
        assertEquals(List.copyOf(before.getEquipmentList()), reloaded.getEquipmentList());
        assertEquals(List.copyOf(before.getLoanList()), reloaded.getLoanList());
        assertEquals(Availability.ASSIGNED, reloaded.getAvailability(new UUID(0, 1)));
        assertEquals(Availability.UNAVAILABLE, reloaded.getAvailability(new UUID(0, 2)));
    }

    @Test
    public void execute_rejectedListArguments_preservesStateAndNeverSaves() throws Exception {
        storage.saveAddressBook(model.getAddressBook());
        String persisted = Files.readString(storage.getAddressBookFilePath());
        AddressBook before = new AddressBook(model.getAddressBook());
        for (String route : new String[] {"member list", "equipment list"}) {
            for (String args : new String[] {"extra", "unknown/x", "notes/ notes/", "nus/A0000001X"}) {
                assertThrows(ParseException.class, () -> logic.execute(route + "\t" + args));
                assertEquals(before, model.getAddressBook());
                assertEquals(persisted, Files.readString(storage.getAddressBookFilePath()));
                assertEquals(1, storage.saves);
            }
        }
    }

    @Test
    public void execute_saveFailure_preservesCauseAndUnchangedReadOnlyState() {
        IOException cause = new IOException("test save failure");
        StorageManager failingStorage = new StorageManager(
                new JsonAddressBookStorage(temporaryFolder.resolve("failed.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("failed-prefs.json"))) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook data) throws IOException {
                throw cause;
            }
        };
        LogicManager failingLogic = new LogicManager(model, failingStorage);
        AddressBook before = new AddressBook(model.getAddressBook());
        for (String route : new String[] {"member list", "equipment list"}) {
            CommandException failure = org.junit.jupiter.api.Assertions.assertThrows(CommandException.class, () ->
                    failingLogic.execute(route));
            assertSame(cause, failure.getCause());
            assertEquals(before, model.getAddressBook());
        }
    }

    /** Counts calls while retaining the real JSON persistence implementation. */
    private static class CountingStorage extends StorageManager {
        private int saves;

        CountingStorage(Path folder) {
            super(new JsonAddressBookStorage(folder.resolve("data.json")),
                    new JsonUserPrefsStorage(folder.resolve("prefs.json")));
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook data) throws IOException {
            saves++;
            super.saveAddressBook(data);
        }
    }
}
