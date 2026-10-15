package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
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
import seedu.address.model.loan.Loan;
import seedu.address.model.member.NusId;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/** Member/equipment top-level routing checks, including controlled loan fixtures for deletion boundaries. */
public class MemberEquipmentRoutingIntegrationTest {

    private static final NusId FIRST_MEMBER = new NusId("A0000001X");
    private static final NusId SECOND_MEMBER = new NusId("A0000002X");
    private static final LocalDate ASSIGNED = LocalDate.of(2026, 1, 1);
    private static final String MEMBER_FIELDS = "nus/A0000001X name/Alex Tan phone/91234567 email/alex@example.com";
    private static final String EQUIPMENT_FIELDS = "name/Camera (kit) category/Photography condition/good";

    @TempDir
    public Path directory;

    private ModelManager model;
    private CountingStorage storage;
    private LogicManager logic;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        model.addPerson(new PersonBuilder().build());
        storage = new CountingStorage(directory);
        logic = new LogicManager(model, storage);
    }

    @Test
    public void routes_addListViewAndDelete_roundTripAllIdentitiesWithoutNameDeduplication() throws Exception {
        addMembers();
        UUID first = addEquipment();
        UUID second = addEquipment();
        assertNotEquals(first, second);
        assertEquals(2, model.getMemberList().size());
        assertEquals(2, model.getEquipmentList().size());
        assertEquals("Alex Tan", model.findMember(FIRST_MEMBER).orElseThrow().getName().fullName);
        assertEquals("https://example.com/camera", model.findEquipment(first).orElseThrow().getNotes());
        assertEquals(4, storage.saves);
        assertReloadMatches();
        AddressBook beforeReads = new AddressBook(model.getAddressBook());
        assertDisplay("member list", DisplayRequest.members());
        assertDisplay("equipment list", DisplayRequest.equipment());
        assertDisplay("member view\tnus/a0000001x", DisplayRequest.memberDetails(FIRST_MEMBER));
        assertDisplay("equipment view uuid/" + first, DisplayRequest.equipmentDetails(first));
        assertEquals(beforeReads, model.getAddressBook());
        assertEquals(8, storage.saves);
        assertReloadMatches();
        assertDisplay("member delete nus/a0000001x", DisplayRequest.members());
        assertDisplay("equipment delete\tuuid/" + first, DisplayRequest.equipment());
        assertFalse(model.hasMember(FIRST_MEMBER));
        assertTrue(model.hasMember(SECOND_MEMBER));
        assertFalse(model.hasEquipment(first));
        assertTrue(model.hasEquipment(second));
        assertEquals(1, model.getAddressBook().getPersonList().size());
        assertEquals(10, storage.saves);
        assertReloadMatches();
    }

    @Test
    public void routes_malformedArguments_preserveAllDataAndSavedBytesWithoutSaving() throws Exception {
        addMembers();
        UUID uuid = addEquipment();
        String equipmentIdentity = "uuid/" + uuid;
        String[] invalid = {
            "member add", "member view", "member delete", "equipment add", "equipment view", "equipment delete",
            "member add " + MEMBER_FIELDS + " unknown/x",
            "member add " + MEMBER_FIELDS + " nus/A0000002X",
            "member add preamble " + MEMBER_FIELDS,
            "member add nus/A0000003X name/ phone/91234567 email/alex@example.com",
            "member add nus/bad name/Alex phone/91234567 email/alex@example.com",
            "member view nus/A0000001X nus/A0000002X", "member view nus/A0000001X unknown/x",
            "member delete nus/A0000001X nus/A0000002X", "member delete nus/A0000001X unknown/x",
            "member list unknown/x", "member list extra",
            "equipment add " + EQUIPMENT_FIELDS + " unknown/x",
            "equipment add " + EQUIPMENT_FIELDS + " notes/ notes/",
            "equipment add " + EQUIPMENT_FIELDS + " notes/text reserved-marker/x",
            "equipment add preamble " + EQUIPMENT_FIELDS,
            "equipment add name/ category/Photography condition/good",
            "equipment add name/Camera category/Photography condition/broken",
            "equipment view " + equipmentIdentity + " " + equipmentIdentity,
            "equipment view " + equipmentIdentity + " unknown/x", "equipment view uuid/not-a-uuid",
            "equipment delete " + equipmentIdentity + " " + equipmentIdentity,
            "equipment delete " + equipmentIdentity + " unknown/x", "equipment delete uuid/not-a-uuid",
            "equipment list unknown/x", "equipment list extra"
        };
        for (String input : invalid) {
            assertRejected(ParseException.class, input);
        }
    }

    @Test
    public void routes_domainFailures_preserveOpenLoansAndNeverSave() throws Exception {
        addMembers();
        UUID uuid = addEquipment();
        model.addLoan(new Loan(uuid, FIRST_MEMBER, ASSIGNED, ASSIGNED.plusDays(1), null));
        storage.saveAddressBook(model.getAddressBook());
        for (String input : new String[] {
            "member add nus/a0000001x name/Different phone/98765432 email/other@example.com",
            "member view nus/A9999999X", "member delete nus/A9999999X",
            "equipment view uuid/00000000-0000-0000-0000-000000000099",
            "equipment delete uuid/00000000-0000-0000-0000-000000000099",
            "member delete nus/A0000001X", "equipment delete uuid/" + uuid
        }) {
            assertRejected(CommandException.class, input);
        }
        assertEquals(Availability.ASSIGNED, model.getAvailability(uuid));
        assertReloadMatches();
    }

    @Test
    public void routes_viewsTrackRealLoanFixturesAndPermittedDeletionRemovesClosedHistory() throws Exception {
        addMembers();
        UUID first = addEquipment();
        UUID second = addEquipment();
        model.addLoan(new Loan(first, FIRST_MEMBER, ASSIGNED, ASSIGNED, ASSIGNED));
        model.addLoan(new Loan(second, SECOND_MEMBER, ASSIGNED, ASSIGNED.plusDays(1), null));
        storage.saveAddressBook(model.getAddressBook());
        AddressBook beforeViews = new AddressBook(model.getAddressBook());
        CommandResult member = logic.execute("member view nus/A0000002X");
        CommandResult equipment = logic.execute("equipment view uuid/" + second);
        assertEquals(DisplayRequest.memberDetails(SECOND_MEMBER), member.getDisplayRequest());
        assertTrue(member.getFeedbackToUser().contains("1 item(s)"));
        assertEquals(DisplayRequest.equipmentDetails(second), equipment.getDisplayRequest());
        assertTrue(equipment.getFeedbackToUser().contains("ASSIGNED"));
        assertEquals(SECOND_MEMBER, model.findOpenLoan(second).orElseThrow().getMemberNusId());
        assertEquals(beforeViews, model.getAddressBook());
        assertDisplay("member delete nus/A0000001X", DisplayRequest.members());
        assertEquals(1, model.getLoanList().size());
        assertTrue(model.hasEquipment(first));
        model.closeLoan(second, ASSIGNED.plusDays(2));
        storage.saveAddressBook(model.getAddressBook());
        assertTrue(logic.execute("member view nus/A0000002X").getFeedbackToUser().contains("0 item(s)"));
        assertTrue(logic.execute("equipment view uuid/" + second).getFeedbackToUser().contains("AVAILABLE"));
        assertDisplay("equipment delete uuid/" + second, DisplayRequest.equipment());
        assertTrue(model.getLoanList().isEmpty());
        assertTrue(model.hasMember(SECOND_MEMBER));
        assertReloadMatches();
    }

    @Test
    public void routes_saveFailure_reportsCauseWithoutClaimingInMemoryRollback() throws Exception {
        addMembers();
        byte[] beforeSave = Files.readAllBytes(storage.getAddressBookFilePath());
        IOException failure = new IOException("test save failure");
        storage.saveFailure = failure;
        CommandException actual = assertThrows(CommandException.class, () -> logic.execute(
                "member add nus/A0000003X name/Third Member phone/91234567 email/third@example.com"));
        assertSame(failure, actual.getCause());
        assertTrue(model.hasMember(new NusId("A0000003X")));
        assertArrayEquals(beforeSave, Files.readAllBytes(storage.getAddressBookFilePath()));
        ModelManager reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertFalse(reloaded.hasMember(new NusId("A0000003X")));
    }

    @Test
    public void routes_legacyPersonCommands_preserveLaplaceData() throws Exception {
        addMembers();
        addEquipment();
        AddressBook before = new AddressBook(model.getAddressBook());
        assertDisplay("add n/Legacy Person p/98765432 e/legacy@example.com a/NUS", DisplayRequest.legacyPersons());
        assertDisplay("find Legacy", DisplayRequest.legacyPersons());
        assertEquals(1, model.getFilteredPersonList().size());
        assertDisplay("delete 1", DisplayRequest.legacyPersons());
        assertDisplay("list", DisplayRequest.legacyPersons());
        assertEquals(before, model.getAddressBook());
        assertReloadMatches();
    }

    private void addMembers() throws Exception {
        assertDisplay("member\tadd\temail/alex@example.com name/ Alex Tan phone/91234567 nus/ a0000001x",
                DisplayRequest.memberDetails(FIRST_MEMBER));
        assertDisplay("member add nus/A0000002X name/Alex Tan phone/91234567 email/alex@example.com",
                DisplayRequest.memberDetails(SECOND_MEMBER));
    }

    private UUID addEquipment() throws Exception {
        CommandResult result = logic.execute("equipment\tadd\tcondition/ good category/ Photography"
                + " name/ Camera (kit) notes/ https://example.com/camera");
        UUID uuid = result.getDisplayRequest().getEquipmentUuid().orElseThrow();
        assertEquals(DisplayRequest.equipmentDetails(uuid), result.getDisplayRequest());
        return uuid;
    }

    private void assertDisplay(String input, DisplayRequest expected) throws Exception {
        assertEquals(expected, logic.execute(input).getDisplayRequest());
    }

    private void assertRejected(Class<? extends Exception> expected, String input) throws Exception {
        AddressBook before = new AddressBook(model.getAddressBook());
        byte[] saved = Files.readAllBytes(storage.getAddressBookFilePath());
        int saves = storage.saves;
        assertThrows(expected, () -> logic.execute(input), input);
        assertEquals(before, model.getAddressBook(), input);
        assertArrayEquals(saved, Files.readAllBytes(storage.getAddressBookFilePath()), input);
        assertEquals(saves, storage.saves, input);
    }

    private void assertReloadMatches() throws Exception {
        ModelManager reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(model.getAddressBook(), reloaded.getAddressBook());
    }

    /** Observes save calls while retaining real JSON storage and its normal error propagation. */
    private static class CountingStorage extends StorageManager {
        private int saves;
        private IOException saveFailure;

        CountingStorage(Path directory) {
            super(new JsonAddressBookStorage(directory.resolve("routing.json")),
                    new JsonUserPrefsStorage(directory.resolve("prefs.json")));
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook data) throws IOException {
            saves++;
            if (saveFailure != null) {
                throw saveFailure;
            }
            super.saveAddressBook(data);
        }
    }
}
