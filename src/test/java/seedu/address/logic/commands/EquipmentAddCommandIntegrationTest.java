package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.EquipmentAddCommandParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.equipment.exceptions.DuplicateEquipmentException;
import seedu.address.model.query.DirectoryQuery;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Equipment registration through parsing, transactions, and real JSON persistence.
 */
public class EquipmentAddCommandIntegrationTest {

    @TempDir
    public Path testFolder;

    @Test
    public void addSaveReload_allConditions_preservesIdentityNotesAndDerivedAvailability() throws Exception {
        ModelManager model = new ModelManager();
        EquipmentAddCommandParser parser = new EquipmentAddCommandParser();
        for (Condition condition : Condition.values()) {
            parser.parse("name/Camera (kit) category/Photo condition/" + condition
                    + " notes/Includes battery & charger").execute(model);
        }
        JsonAddressBookStorage storage = new JsonAddressBookStorage(testFolder.resolve("equipment.json"));
        storage.saveAddressBook(model.getAddressBook());
        ModelManager reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(model.getAddressBook(), reloaded.getAddressBook());
        assertEquals(4, reloaded.getEquipmentList().size());
        for (var equipment : model.getEquipmentList()) {
            assertEquals(model.getAvailability(equipment.getUuid()), reloaded.getAvailability(equipment.getUuid()));
            assertEquals("Includes battery & charger",
                    reloaded.findEquipment(equipment.getUuid()).orElseThrow().getNotes());
        }
        String saved = Files.readString(storage.getAddressBookFilePath());
        assertFalse(saved.contains("availability"));
        assertFalse(saved.contains("holder"));
        AddressBook before = new AddressBook(reloaded.getAddressBook());
        assertThrows(ParseException.class, () -> parser.parse(
                "name/Camera category/Photo condition/GOOD notes/one notes/two").execute(reloaded));
        assertEquals(before, reloaded.getAddressBook());
        assertEquals(saved, Files.readString(storage.getAddressBookFilePath()));
    }
    @Test
    public void execute_batch_savesOnceAndReloadsSeparateRecords() throws Exception {
        ModelManager model = new ModelManager(SampleDataUtil.getSampleAddressBook(), new UserPrefs());
        int initialCount = model.getEquipmentList().size();
        CountingStorage storage = new CountingStorage(testFolder);
        LogicManager logic = new LogicManager(model, storage);
        CommandResult result = logic.execute(
                "equipment add name/Batch Camera category/Photo condition/GOOD count/3 notes/Shared kit");
        assertEquals(1, storage.saves);
        assertEquals(DisplayRequest.equipment(DirectoryQuery.all()), result.getDisplayRequest());
        ReadOnlyAddressBook reloaded = storage.readAddressBook().orElseThrow();
        assertEquals(model.getAddressBook(), reloaded);
        assertEquals(initialCount + 3, reloaded.getEquipmentList().size());
        assertEquals(initialCount + 3, reloaded.getEquipmentList().stream().map(Equipment::getUuid).distinct().count());
        assertEquals(3, reloaded.getEquipmentList().stream().filter(unit -> unit.getName().equals("Batch Camera"))
                .filter(unit -> unit.getNotes().equals("Shared kit")).count());
        String saved = Files.readString(storage.getAddressBookFilePath());
        assertFalse(saved.contains("\"count\""));
        assertFalse(saved.contains("\"availability\""));
    }

    @Test
    public void execute_invalidBatchCounts_preservesRecordsAndSavedBytesWithoutSaving() throws Exception {
        ModelManager model = new ModelManager(SampleDataUtil.getSampleAddressBook(), new UserPrefs());
        CountingStorage storage = new CountingStorage(testFolder);
        storage.saveAddressBook(model.getAddressBook());
        LogicManager logic = new LogicManager(model, storage);
        AddressBook before = new AddressBook(model.getAddressBook());
        byte[] saved = Files.readAllBytes(storage.getAddressBookFilePath());
        for (String count : new String[] {"0", "-1", "101", "1.5", "", "2 count/3"}) {
            assertThrows(ParseException.class, () -> logic.execute(
                    "equipment add name/Camera category/Photo condition/GOOD count/" + count));
            assertEquals(before, model.getAddressBook());
            assertArrayEquals(saved, Files.readAllBytes(storage.getAddressBookFilePath()));
            assertEquals(1, storage.saves);
        }
    }

    @Test
    public void execute_secondInsertionFails_discardsCandidateAndNeverSavesPartialBatch() throws Exception {
        AtomicInteger additions = new AtomicInteger();
        DuplicateEquipmentException cause = new DuplicateEquipmentException();
        ModelManager model = new ModelManager(SampleDataUtil.getSampleAddressBook(), new UserPrefs()) {
            @Override
            public Model createTransactionSnapshot() {
                return new ModelManager(getAddressBook(), getUserPrefs()) {
                    @Override
                    public void addEquipment(Equipment unit) {
                        if (additions.incrementAndGet() == 2) {
                            throw cause;
                        }
                        super.addEquipment(unit);
                    }
                };
            }
        };
        CountingStorage storage = new CountingStorage(testFolder);
        storage.saveAddressBook(model.getAddressBook());
        AddressBook before = new AddressBook(model.getAddressBook());
        byte[] saved = Files.readAllBytes(storage.getAddressBookFilePath());
        LogicManager logic = new LogicManager(model, storage);
        CommandException failure = assertThrows(CommandException.class, () -> logic.execute(
                "equipment add name/Batch Camera category/Photo condition/GOOD count/3"));
        assertSame(cause, failure.getCause());
        assertEquals(2, additions.get());
        assertEquals(1, storage.saves);
        assertEquals(before, model.getAddressBook());
        assertArrayEquals(saved, Files.readAllBytes(storage.getAddressBookFilePath()));
        logic.execute("member list");
        assertEquals(before, storage.readAddressBook().orElseThrow());
        assertTrue(model.getEquipmentList().stream().noneMatch(unit -> unit.getName().equals("Batch Camera")));
    }

    /**
     * Counts successful saves while using the normal JSON implementation.
     */
    private static class CountingStorage extends StorageManager {
        private int saves;

        CountingStorage(Path folder) {
            super(new JsonAddressBookStorage(folder.resolve("batch.json")),
                    new JsonUserPrefsStorage(folder.resolve("prefs.json")));
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook data) throws IOException {
            super.saveAddressBook(data);
            saves++;
        }
    }

}
