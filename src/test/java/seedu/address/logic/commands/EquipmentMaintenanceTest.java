package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalMembers.ALEX;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.equipment.Availability;
import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.equipment.UniqueEquipmentList;
import seedu.address.model.equipment.exceptions.DuplicateEquipmentException;
import seedu.address.model.equipment.exceptions.EquipmentDeletedException;
import seedu.address.model.equipment.exceptions.EquipmentNotFoundException;
import seedu.address.model.loan.Loan;
import seedu.address.model.query.InventoryGroups;

/**
 * Verifies identity-preserving maintenance through real routing and root-model invariants.
 */
public class EquipmentMaintenanceTest {
    private static final UUID ID = new UUID(0, 1);
    private static final Equipment CAMERA = new Equipment(ID, "Camera", "Photography", Condition.GOOD, "Strap");
    private static final Equipment SIBLING =
            new Equipment(new UUID(0, 2), "Camera", "Photography", Condition.GOOD, "Strap");
    private static final LocalDate DATE = LocalDate.of(2000, 1, 1);
    private static final Loan OPEN = new Loan(ID, ALEX.getNusId(), DATE, DATE.plusDays(1), null);
    private static final Loan CLOSED = OPEN.withReturnedDate(DATE.plusDays(1));
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void edit_allFieldsAndOpenLoan_preservesIdentitySiblingOrderAndReferences() throws Exception {
        ModelManager model = model();
        model.addLoan(OPEN);
        Command edit = parser.parseCommand("equipment edit uuid/" + ID
                + " name/  Renamed   Camera category/Video condition/damaged notes/");
        CommandResult result = edit.execute(model);
        Equipment updated = new Equipment(ID, "Renamed Camera", "Video", Condition.DAMAGED, "");
        assertEquals(List.of(updated, SIBLING), model.getEquipmentList());
        assertEquals(List.of(CLOSED, OPEN), model.getLoanList());
        assertEquals(Availability.ASSIGNED, model.getAvailability(ID));
        assertEquals(DisplayRequest.equipmentDetails(ID), result.getDisplayRequest());
        assertTrue(edit.isTransactional());
        model.closeLoan(ID, DATE.plusDays(2));
        assertEquals(Availability.UNAVAILABLE, model.getAvailability(ID));
    }

    @Test
    public void edit_partialEdit_retainsUnspecifiedLegacyText() throws Exception {
        ModelManager model = new ModelManager();
        model.addEquipment(new Equipment(ID, "Old  Camera", "Old  Category", Condition.FAIR, "Old  Notes"));
        parser.parseCommand("equipment edit uuid/" + ID + " condition/GOOD").execute(model);
        assertEquals(new Equipment(ID, "Old  Camera", "Old  Category", Condition.GOOD, "Old  Notes"),
                model.findEquipment(ID).orElseThrow());
    }

    @Test
    public void edit_categoryOnly_preservesOtherDetailsAndHistory() throws Exception {
        ModelManager model = model();
        parser.parseCommand("equipment edit uuid/" + ID + " category/  Video   Production ").execute(model);
        Equipment edited = new Equipment(ID, CAMERA.getName(), "Video Production", CAMERA.getCondition(),
                CAMERA.getNotes());
        assertEquals(List.of(edited, SIBLING), model.getEquipmentList());
        assertEquals(List.of(CLOSED), model.getLoanList());
    }

    @Test
    public void lifecycle_deleteRestore_retainsHistoryBlocksLoansAndDoesNotRepairCondition() throws Exception {
        ModelManager model = model();
        model.deleteEquipment(CAMERA);
        Equipment deleted = model.findEquipment(ID).orElseThrow();
        assertTrue(deleted.isDeleted());
        assertTrue(deleted.isSameEquipment(CAMERA));
        assertEquals(List.of(CLOSED), model.getLoanList());
        assertEquals(Availability.UNAVAILABLE, model.getAvailability(ID));
        assertThrows(EquipmentDeletedException.class, () -> model.addLoan(OPEN));
        assertThrows(DuplicateEquipmentException.class, () -> model.addEquipment(CAMERA));
        CommandException error = assertThrows(CommandException.class, () ->
                new LoanAddCommand(ID, ALEX.getNusId(), DATE, DATE.plusDays(1)).execute(model));
        assertTrue(error.getMessage().contains("deleted equipment"));
        assertEquals(List.of("\"photography\" / \"camera\": 1 available, 0 assigned, 0 unavailable"),
                InventoryGroups.summarize(model.getAddressBook(), model::getAvailability, item -> true));
        Command restore = parser.parseCommand("equipment restore uuid/" + ID);
        assertTrue(restore.isTransactional());
        assertEquals(DisplayRequest.equipmentDetails(ID), restore.execute(model).getDisplayRequest());
        assertEquals(CAMERA, model.findEquipment(ID).orElseThrow());
        model.addLoan(OPEN);
        model.closeLoan(ID, DATE.plusDays(2));
        parser.parseCommand("equipment edit uuid/" + ID + " condition/DAMAGED").execute(model);
        model.deleteEquipment(CAMERA);
        restore.execute(model);
        assertEquals(Availability.UNAVAILABLE, model.getAvailability(ID));
        assertEquals(2, model.getLoanList().size());
    }

    @Test
    public void rejectedCommands_missingNoChangeRepeatedAndDeleted_preserveState() throws Exception {
        ModelManager model = model();
        for (String text : List.of("equipment edit uuid/" + ID + " name/Camera",
                "equipment restore uuid/" + ID, "equipment edit uuid/" + new UUID(0, 99) + " notes/x",
                "equipment restore uuid/" + new UUID(0, 99))) {
            assertRejectedWithoutMutation(text, model);
        }
        model.addLoan(OPEN);
        assertRejectedWithoutMutation("equipment delete uuid/" + ID, model);
        model.closeLoan(ID, DATE.plusDays(2));
        parser.parseCommand("equipment delete uuid/" + ID).execute(model);
        for (String text : List.of("equipment delete uuid/" + ID,
                "equipment edit uuid/" + ID + " name/Camera", "equipment edit uuid/" + ID + " notes/Changed")) {
            assertRejectedWithoutMutation(text, model);
        }
    }

    @Test
    public void search_statusDefaultsAndComposedFilters_keepDeletedUnitsDiscoverable() throws Exception {
        ModelManager model = model();
        model.deleteEquipment(CAMERA);
        assertEquals("Found 1 equipment item.", parser.parseCommand("equipment search").execute(model)
                .getFeedbackToUser());
        assertEquals("Found 2 equipment items.", parser.parseCommand("equipment search status/all").execute(model)
                .getFeedbackToUser());
        assertEquals("Found 1 equipment item.", parser.parseCommand("equipment search status/DELETED q/Camera"
                + " category/photo condition/GOOD availability/UNAVAILABLE sort/id order/desc").execute(model)
                .getFeedbackToUser());
        assertTrue(parser.parseCommand("equipment list").isReadOnly());
        assertTrue(parser.parseCommand("equipment search status/deleted").isReadOnly());
    }

    @Test
    public void parsers_invalidFieldsAndGrammar_rejectBeforeExecutionWithUsage() {
        for (String fields : List.of("", "uuid/", "uuid/1-1-1-1-1 name/x", "name/x", "uuid/" + ID,
                "uuid/" + ID + " name/", "uuid/" + ID + " category/", "uuid/" + ID + " condition/broken",
                "uuid/" + ID + " name/x name/y", "uuid/" + ID + " uuid/" + ID + " name/x",
                "uuid/" + ID + " count/2", "extra uuid/" + ID + " notes/x",
                "uuid/" + ID + " notes/x unknown/y", "uuid/" + ID + " name/" + "x".repeat(101),
                "uuid/" + ID + " category/" + "x".repeat(51), "uuid/" + ID + " notes/" + "x".repeat(301))) {
            ParseException error = assertThrows(ParseException.class, () -> parser.parseCommand("equipment edit "
                    + fields), fields);
            assertTrue(error.getMessage().contains(EquipmentEditCommand.MESSAGE_USAGE));
        }
        for (String fields : List.of("", "uuid/", "uuid/invalid", "uuid/" + ID + " uuid/" + ID,
                "uuid/" + ID + " notes/x", "extra uuid/" + ID)) {
            ParseException error = assertThrows(ParseException.class, () -> parser.parseCommand("equipment restore "
                    + fields), fields);
            assertTrue(error.getMessage().contains(EquipmentRestoreCommand.MESSAGE_USAGE));
        }
    }

    @Test
    public void rootAndCollection_replacementGuards_preventIdentityAndLifecycleBypass() {
        ModelManager model = model();
        AddressBook before = new AddressBook(model.getAddressBook());
        assertThrows(IllegalArgumentException.class, () -> model.setEquipment(CAMERA, SIBLING));
        assertThrows(IllegalArgumentException.class, () -> model.setEquipment(CAMERA, CAMERA.withDeleted(true)));
        assertThrows(NullPointerException.class, () -> model.setEquipment(CAMERA, null));
        assertThrows(NullPointerException.class, () -> model.restoreEquipment(null));
        assertThrows(EquipmentNotFoundException.class, () -> new ModelManager().restoreEquipment(CAMERA));
        assertEquals(before, model.getAddressBook());
        model.deleteEquipment(CAMERA);
        assertThrows(EquipmentDeletedException.class, () -> model.setEquipment(CAMERA, CAMERA));
        UniqueEquipmentList units = new UniqueEquipmentList();
        assertThrows(EquipmentNotFoundException.class, () -> units.setEquipment(CAMERA, CAMERA));
        units.add(CAMERA);
        assertThrows(IllegalArgumentException.class, () -> units.setEquipment(CAMERA, SIBLING));
        assertEquals(List.of(CAMERA), units.asUnmodifiableObservableList());
    }

    @Test
    public void model_repeatedLifecycleChanges_resolvesStoredStateAndPreservesData() {
        ModelManager model = model();
        AddressBook active = new AddressBook(model.getAddressBook());
        // A stale caller snapshot must not bypass the root's lifecycle checks.
        assertThrows(IllegalStateException.class, () -> model.restoreEquipment(CAMERA.withDeleted(true)));
        assertEquals(active, model.getAddressBook());
        model.deleteEquipment(CAMERA);
        AddressBook deleted = new AddressBook(model.getAddressBook());
        assertThrows(EquipmentDeletedException.class, () -> model.deleteEquipment(CAMERA));
        assertEquals(deleted, model.getAddressBook());
        model.restoreEquipment(CAMERA);
        assertEquals(active, model.getAddressBook());
    }

    @Test
    public void resetData_deletedUnitWithOpenLoan_rejectsBeforePublication() {
        AddressBook invalid = new AddressBook() {
            @Override
            public ObservableList<Loan> getLoanList() {
                return FXCollections.observableArrayList(OPEN);
            }
        };
        invalid.addMember(ALEX);
        invalid.addEquipment(CAMERA.withDeleted(true));
        ModelManager model = model();
        AddressBook before = new AddressBook(model.getAddressBook());
        assertThrows(EquipmentDeletedException.class, () -> model.setAddressBook(invalid));
        assertEquals(before, model.getAddressBook());
        assertFalse(model.findEquipment(ID).orElseThrow().isDeleted());
    }

    private void assertRejectedWithoutMutation(String text, ModelManager model) throws Exception {
        AddressBook before = new AddressBook(model.getAddressBook());
        Command command = parser.parseCommand(text);
        assertThrows(CommandException.class, () -> command.execute(model));
        assertEquals(before, model.getAddressBook());
    }

    private ModelManager model() {
        ModelManager model = new ModelManager();
        model.addMember(ALEX);
        model.addEquipment(CAMERA);
        model.addEquipment(SIBLING);
        model.addLoan(CLOSED);
        return model;
    }
}
