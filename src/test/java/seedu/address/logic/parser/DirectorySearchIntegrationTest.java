package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.DirectorySearchCommand.EQUIPMENT_USAGE;
import static seedu.address.logic.commands.DirectorySearchCommand.MEMBER_USAGE;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.DisplayRequest;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.query.DirectoryQuery;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.SearchData;

public class DirectorySearchIntegrationTest {
    @TempDir
    public Path directory;

    @Test
    public void search_greekSigmaAfterSaveReload_preservesSpellingAndNeverSaves() throws Exception {
        ModelManager model = new ModelManager();
        CountingStorage storage = new CountingStorage(directory);
        LogicManager logic = new LogicManager(model, storage);
        logic.execute("member add nus/A8400001X name/ΝΙΚΟΣ phone/91234567 email/nikos@example.com");
        ModelManager reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        LogicManager reopened = new LogicManager(reloaded, storage);
        byte[] saved = Files.readAllBytes(storage.getAddressBookFilePath());
        for (String query : List.of("Σ", "σ", "ς", "νικοσ", "νικος")) {
            assertEquals("Found 1 members.", reopened.execute("member search q/" + query).getFeedbackToUser());
        }
        assertEquals(1, storage.saves);
        assertTrue(java.util.Arrays.equals(saved, Files.readAllBytes(storage.getAddressBookFilePath())));
        assertEquals("ΝΙΚΟΣ", reloaded.getMemberList().getFirst().getName().fullName);
    }

    @Test
    public void search_realRoutes_neverSavesEvenWhenStorageIsUnwritable() throws Exception {
        ModelManager model = SearchData.sample();
        AddressBook before = new AddressBook(model.getAddressBook());
        CountingStorage storage = new CountingStorage(directory);
        storage.saveAddressBook(before);
        byte[] bytes = Files.readAllBytes(storage.getAddressBookFilePath());
        storage.fail = true;
        LogicManager logic = new LogicManager(model, storage);
        for (String text : List.of("member search", "member\tsearch q/ALEX holding/yes sort/held order/DESC",
                "equipment search q/camera category/photo condition/good availability/available sort/name",
                "equipment search sort/availability order/desc", "equipment search q/no matches")) {
            var result = logic.execute(text);
            DirectoryQuery query = result.getDisplayRequest().getQuery().orElseThrow();
            assertEquals(text.startsWith("member") ? DisplayRequest.Type.MEMBERS : DisplayRequest.Type.EQUIPMENT,
                    result.getDisplayRequest().getType());
            assertTrue(result.getFeedbackToUser().startsWith("Found "));
            assertEquals(before, model.getAddressBook());
            assertEquals(1, storage.saves);
            assertTrue(java.util.Arrays.equals(bytes, Files.readAllBytes(storage.getAddressBookFilePath())));
            assertFalse(query.description().isEmpty());
        }
        ModelManager reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(before, reloaded.getAddressBook());
    }

    @Test
    public void memberStatus_realDeleteSearchAndRestore_preservesRetainedIdentityAndReadOnlyQueries() throws Exception {
        ModelManager model = SearchData.sample();
        CountingStorage storage = new CountingStorage(directory);
        LogicManager logic = new LogicManager(model, storage);
        logic.execute("member delete nus/A0000002X");
        assertEquals(2, model.getMemberList().size());
        byte[] saved = Files.readAllBytes(storage.getAddressBookFilePath());
        assertEquals("Found 1 members.", logic.execute("member search").getFeedbackToUser());
        assertEquals("Found 1 members.", logic.execute("member search status/DELETED").getFeedbackToUser());
        assertEquals("Found 2 members.", logic.execute("member search status/all").getFeedbackToUser());
        assertEquals(DirectoryQuery.Status.ACTIVE,
                logic.execute("member search").getDisplayRequest().getQuery().orElseThrow().status());
        assertEquals(1, storage.saves);
        assertTrue(java.util.Arrays.equals(saved, Files.readAllBytes(storage.getAddressBookFilePath())));
        assertTrue(storage.readAddressBook().orElseThrow().getMemberList().stream()
                .anyMatch(member -> member.isDeleted() && member.getNusId().value.equals("A0000002X")));
        logic.execute("member restore nus/A0000002X");
        assertEquals(2, storage.saves);
        assertEquals("Found 0 members.", logic.execute("member search status/deleted").getFeedbackToUser());
        assertEquals("Found 2 members.", logic.execute("member search status/active").getFeedbackToUser());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void suppliedFieldErrors_followInputOrderAndRetainUsage() {
        AddressBookParser parser = new AddressBookParser();
        assertThrows(ParseException.class, "Invalid Holding: maybe\n" + MEMBER_USAGE, () ->
                parser.parseCommand("member search holding/maybe order/invalid"));
        assertThrows(ParseException.class, "order/ must be asc or desc.\n" + MEMBER_USAGE, () ->
                parser.parseCommand("member search order/invalid holding/maybe"));
        assertThrows(ParseException.class, "Search parameter must not be blank: q/\n" + EQUIPMENT_USAGE, () ->
                parser.parseCommand("equipment search q/ unknown/value"));
        assertThrows(NullPointerException.class, () -> new DirectorySearchCommandParser(true).parse(null));
    }

    @Test
    public void invalidSearch_rejectedAtTopLevel_withoutWritesOrStateChanges() throws Exception {
        ModelManager model = SearchData.sample();
        CountingStorage storage = new CountingStorage(directory);
        LogicManager logic = new LogicManager(model, storage);
        AddressBook before = new AddressBook(model.getAddressBook());
        for (String input : List.of("member search words", "member search q/", "member search q/a q/b",
                "member search category/photo", "member search holding/maybe", "member search sort/condition",
                "member search order/reverse", "equipment search holding/yes", "equipment search condition/broken",
                "equipment search availability/free", "equipment search sort/held", "equipment search q/x unknown/y",
                "equipment search category/", "equipment search sort/", "equipment search order/",
                "member search status/archived", "member search status/", "member search status/all status/active",
                "equipment search status/archived", "equipment search status/",
                "equipment search status/all status/active")) {
            assertThrows(ParseException.class, () -> logic.execute(input));
            assertEquals(before, model.getAddressBook());
            assertEquals(0, storage.saves);
            assertFalse(Files.exists(storage.getAddressBookFilePath()));
        }
    }

    @Test
    public void successfulAdds_warnButRetainDistinctIdentitiesAndPersist() throws Exception {
        ModelManager model = SearchData.sample();
        CountingStorage storage = new CountingStorage(directory);
        LogicManager logic = new LogicManager(model, storage);
        String member = logic.execute("member add nus/A0000009X name/Alex Tan phone/91234567"
                + " email/alex9@example.com").getFeedbackToUser();
        assertTrue(member.contains("Possible duplicate(s): A0000001X, A0000002X"));
        String equipment = logic.execute("equipment add name/Camera category/Photography condition/GOOD")
                .getFeedbackToUser();
        assertTrue(equipment.contains("Possible duplicate(s):"));
        assertEquals(3, model.getMemberList().size());
        assertEquals(4, model.getEquipmentList().size());
        assertEquals(2, storage.saves);
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void displayRequest_queryIsImmutableAndPartOfValueIdentity() {
        DirectoryQuery query = DirectoryQuery.all();
        assertEquals(DisplayRequest.members(query), DisplayRequest.members(DirectoryQuery.all()));
        assertEquals(DisplayRequest.members(query).hashCode(), DisplayRequest.members(query).hashCode());
        assertNotEquals(DisplayRequest.members(), DisplayRequest.members(query));
        assertNotEquals(DisplayRequest.members(query), DisplayRequest.equipment(query));
        assertThrows(NullPointerException.class, () -> DisplayRequest.members(null));
        assertThrows(NullPointerException.class, () -> DisplayRequest.equipment(null));
    }

    private static class CountingStorage extends StorageManager {
        private int saves;
        private boolean fail;

        CountingStorage(Path path) {
            super(new JsonAddressBookStorage(path.resolve("data.json")),
                    new JsonUserPrefsStorage(path.resolve("preferences.json")));
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook data) throws IOException {
            if (fail) {
                throw new IOException("Read-only query must never save");
            }
            saves++;
            super.saveAddressBook(data);
        }
    }
}
