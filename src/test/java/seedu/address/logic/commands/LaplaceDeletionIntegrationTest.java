package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.commands.DeletionTestData.FIRST_EQUIPMENT;
import static seedu.address.logic.commands.DeletionTestData.FIRST_MEMBER;
import static seedu.address.logic.commands.DeletionTestData.RETURNED;
import static seedu.address.logic.commands.DeletionTestData.SECOND_EQUIPMENT;
import static seedu.address.logic.commands.DeletionTestData.SECOND_MEMBER;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.EquipmentDeleteCommandParser;
import seedu.address.logic.parser.MemberDeleteCommandParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.equipment.Availability;
import seedu.address.model.loan.Loan;
import seedu.address.storage.JsonAddressBookStorage;

/**
 * Exercises direct parser/command/model/storage integration before the family routes are activated.
 * Saving is explicit here; these tests do not claim execution through LogicManager's pipeline.
 */
public class LaplaceDeletionIntegrationTest {

    @TempDir
    public Path testFolder;

    @Test
    public void memberDelete_closedHistory_saveReloadPreservesOnlyUnrelatedData() throws Exception {
        ModelManager model = DeletionTestData.model();
        model.addLoan(DeletionTestData.loan(FIRST_EQUIPMENT, FIRST_MEMBER, false));
        Loan retained = DeletionTestData.loan(SECOND_EQUIPMENT, SECOND_MEMBER, true);
        model.addLoan(retained);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(
                testFolder.resolve("member.json"));
        storage.saveAddressBook(model.getAddressBook());
        new MemberDeleteCommandParser().parse(" \tnus/ a0123456x ").execute(model);
        storage.saveAddressBook(model.getAddressBook());
        ModelManager reloaded = reload(storage);
        assertEquals(model.getAddressBook(), reloaded.getAddressBook());
        assertFalse(reloaded.hasMember(FIRST_MEMBER.getNusId()));
        assertEquals(List.of(SECOND_MEMBER), reloaded.getMemberList());
        assertEquals(List.of(retained), reloaded.getLoanList());
        assertEquals(Availability.AVAILABLE, reloaded.getAvailability(FIRST_EQUIPMENT.getUuid()));
        assertEquals(Availability.ASSIGNED, reloaded.getAvailability(SECOND_EQUIPMENT.getUuid()));
    }

    @Test
    public void equipmentDelete_closedHistory_saveReloadPreservesOnlyUnrelatedData() throws Exception {
        ModelManager model = DeletionTestData.model();
        model.addLoan(DeletionTestData.loan(FIRST_EQUIPMENT, FIRST_MEMBER, false));
        Loan retained = DeletionTestData.loan(SECOND_EQUIPMENT, SECOND_MEMBER, true);
        model.addLoan(retained);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(
                testFolder.resolve("equipment.json"));
        storage.saveAddressBook(model.getAddressBook());
        new EquipmentDeleteCommandParser().parse("uuid/" + FIRST_EQUIPMENT.getUuid()).execute(model);
        storage.saveAddressBook(model.getAddressBook());
        ModelManager reloaded = reload(storage);
        assertEquals(model.getAddressBook(), reloaded.getAddressBook());
        assertFalse(reloaded.hasEquipment(FIRST_EQUIPMENT.getUuid()));
        assertEquals(List.of(SECOND_EQUIPMENT), reloaded.getEquipmentList());
        assertEquals(List.of(retained), reloaded.getLoanList());
        assertEquals(Availability.ASSIGNED, reloaded.getAvailability(SECOND_EQUIPMENT.getUuid()));
    }

    @Test
    public void memberDelete_rejectionsThenReturnAndDelete_preservesStateAndReloads() throws Exception {
        ModelManager model = DeletionTestData.model();
        model.addLoan(DeletionTestData.loan(FIRST_EQUIPMENT, FIRST_MEMBER, true));
        JsonAddressBookStorage storage = new JsonAddressBookStorage(
                testFolder.resolve("member-flow.json"));
        storage.saveAddressBook(model.getAddressBook());
        String saved = Files.readString(storage.getAddressBookFilePath());
        AddressBook before = new AddressBook(model.getAddressBook());
        MemberDeleteCommandParser parser = new MemberDeleteCommandParser();
        for (String args : List.of("nus/", "nus/A0123456X nus/A0234567R", "nus/A0123456X notes/extra")) {
            assertThrows(ParseException.class, () -> parser.parse(args).execute(model));
            assertEquals(before, model.getAddressBook());
        }
        assertThrows(CommandException.class, () -> parser.parse("nus/A0999999Z").execute(model));
        assertThrows(CommandException.class, () -> parser.parse("nus/a0123456x").execute(model));
        assertEquals(before, model.getAddressBook());
        assertEquals(saved, Files.readString(storage.getAddressBookFilePath()));

        model.closeLoan(FIRST_EQUIPMENT.getUuid(), RETURNED);
        parser.parse("nus/a0123456x").execute(model);
        AddressBook afterSuccess = new AddressBook(model.getAddressBook());
        assertThrows(CommandException.class, () -> parser.parse("nus/a0123456x").execute(model));
        assertEquals(afterSuccess, model.getAddressBook());
        parser.parse("nus/A0234567R").execute(model);
        storage.saveAddressBook(model.getAddressBook());
        assertEquals(model.getAddressBook(), reload(storage).getAddressBook());
        assertEquals(List.of(), model.getMemberList());
        assertEquals(List.of(), model.getLoanList());
        assertEquals(List.of(FIRST_EQUIPMENT, SECOND_EQUIPMENT), model.getEquipmentList());
    }

    @Test
    public void equipmentDelete_rejectionsThenReturnAndDelete_preservesStateAndReloads() throws Exception {
        ModelManager model = DeletionTestData.model();
        model.addLoan(DeletionTestData.loan(FIRST_EQUIPMENT, FIRST_MEMBER, true));
        JsonAddressBookStorage storage = new JsonAddressBookStorage(
                testFolder.resolve("equipment-flow.json"));
        storage.saveAddressBook(model.getAddressBook());
        String saved = Files.readString(storage.getAddressBookFilePath());
        AddressBook before = new AddressBook(model.getAddressBook());
        EquipmentDeleteCommandParser parser = new EquipmentDeleteCommandParser();
        String target = "uuid/" + FIRST_EQUIPMENT.getUuid();
        for (String args : List.of("uuid/", target + " " + target, target + " notes/extra")) {
            assertThrows(ParseException.class, () -> parser.parse(args).execute(model));
            assertEquals(before, model.getAddressBook());
        }
        assertThrows(CommandException.class, () -> parser.parse("uuid/00000000-0000-0000-0000-000000000099")
                .execute(model));
        assertThrows(CommandException.class, () -> parser.parse(target).execute(model));
        assertEquals(before, model.getAddressBook());
        assertEquals(saved, Files.readString(storage.getAddressBookFilePath()));

        model.closeLoan(FIRST_EQUIPMENT.getUuid(), RETURNED);
        parser.parse(target).execute(model);
        AddressBook afterSuccess = new AddressBook(model.getAddressBook());
        assertThrows(CommandException.class, () -> parser.parse(target).execute(model));
        assertEquals(afterSuccess, model.getAddressBook());
        parser.parse("uuid/" + SECOND_EQUIPMENT.getUuid()).execute(model);
        storage.saveAddressBook(model.getAddressBook());
        assertEquals(model.getAddressBook(), reload(storage).getAddressBook());
        assertEquals(List.of(), model.getEquipmentList());
        assertEquals(List.of(), model.getLoanList());
        assertEquals(List.of(FIRST_MEMBER, SECOND_MEMBER), model.getMemberList());
    }

    /**
     * Reloads the existing storage format into a fresh real model.
     */
    private static ModelManager reload(JsonAddressBookStorage storage) throws Exception {
        return new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
    }
}
