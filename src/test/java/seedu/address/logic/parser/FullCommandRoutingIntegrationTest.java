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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.equipment.Availability;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.model.query.DirectoryQuery;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/** All ten routes through real model/storage; controlled overrides exercise missing references and save failures. */
public class FullCommandRoutingIntegrationTest {

    private static final NusId FIRST = new NusId("A0000001X");
    private static final NusId SECOND = new NusId("A0000002X");
    private static final UUID UNKNOWN = UUID.fromString("00000000-0000-0000-0000-000000000099");

    @TempDir
    public Path directory;

    private ModelManager model;
    private CountingStorage storage;
    private LogicManager logic;
    private UUID firstItem;
    private UUID secondItem;

    @BeforeEach
    public void setUp() throws Exception {
        model = new ModelManager();
        model.addPerson(new PersonBuilder().build());
        storage = new CountingStorage(directory);
        logic = new LogicManager(model, storage);
        execute("member add nus/a0000001x name/Alex Tan phone/91234567 email/alex@example.com",
                DisplayRequest.memberDetails(FIRST));
        execute("member add nus/A0000002X name/Alex Tan phone/91234567 email/alex@example.com",
                DisplayRequest.memberDetails(SECOND));
        firstItem = addEquipment("GOOD");
        secondItem = addEquipment("GOOD");
    }

    @Test
    public void allTenRoutes_memberAndEquipmentDeletionRetainsHistory() throws Exception {
        LocalDate assigned = LocalDate.now().minusDays(3);
        LocalDate expected = assigned.plusDays(10);
        LocalDate returned = assigned.plusDays(1);
        assertNotEquals(firstItem, secondItem);
        assertEquals(2, model.getMemberList().size());
        assertEquals(2, model.getEquipmentList().size());
        execute("member list", DisplayRequest.members(DirectoryQuery.all()));
        execute("equipment list", DisplayRequest.equipment(DirectoryQuery.all()));
        execute("member view nus/a0000001x", DisplayRequest.memberDetails(FIRST));
        execute("equipment view uuid/" + firstItem, DisplayRequest.equipmentDetails(firstItem));
        execute("loans\tadd\texpected/" + expected + " nus/ a0000001x assigned/ " + assigned
                + " uuid/ " + firstItem, DisplayRequest.equipmentDetails(firstItem));
        var originalLoan = model.findOpenLoan(firstItem).orElseThrow();
        reload();
        assertEquals(assigned, model.findOpenLoan(firstItem).orElseThrow().getAssignedDate());
        assertEquals(expected, model.findOpenLoan(firstItem).orElseThrow().getExpectedReturnDate());
        assertEquals(FIRST, model.findOpenLoan(firstItem).orElseThrow().getMemberNusId());
        assertEquals(Availability.ASSIGNED, model.getAvailability(firstItem));
        assertEquals(1, model.findOpenLoansForMember(FIRST).size());
        assertTrue(execute("member view nus/" + FIRST, DisplayRequest.memberDetails(FIRST))
                .getFeedbackToUser().contains("1 item(s)"));
        assertTrue(execute("equipment view uuid/" + firstItem, DisplayRequest.equipmentDetails(firstItem))
                .getFeedbackToUser().contains("ASSIGNED"));
        assertRejected(CommandException.class, "member delete nus/" + FIRST);
        assertRejected(CommandException.class, "equipment delete uuid/" + firstItem);
        execute("loans return returned/" + returned + "\tuuid/" + firstItem,
                DisplayRequest.equipmentDetails(firstItem));
        assertTrue(originalLoan.isOpen());
        reload();
        assertTrue(model.findOpenLoan(firstItem).isEmpty());
        assertTrue(model.findOpenLoansForMember(FIRST).isEmpty());
        assertEquals(Availability.AVAILABLE, model.getAvailability(firstItem));
        assertEquals(originalLoan.withReturnedDate(returned), model.getLoanList().getFirst());
        assertRejected(CommandException.class, "loans return uuid/" + firstItem + " returned/" + returned);
        execute(addLoan(firstItem, SECOND, returned.plusDays(1), expected), DisplayRequest.equipmentDetails(firstItem));
        reload();
        assertEquals(2, model.getLoanList().size());
        assertEquals(SECOND, model.findOpenLoan(firstItem).orElseThrow().getMemberNusId());
        execute("loans return uuid/" + firstItem + " returned/" + LocalDate.now(),
                DisplayRequest.equipmentDetails(firstItem));
        assertEquals(2, model.getLoanList().size());
        assertTrue(model.getLoanList().stream().noneMatch(loan -> loan.isOpen()));
        execute("member delete nus/" + FIRST, DisplayRequest.members());
        assertEquals(2, model.getLoanList().size());
        execute("equipment delete uuid/" + firstItem, DisplayRequest.equipment());
        assertEquals(2, model.getLoanList().size());
        assertTrue(model.hasMember(SECOND));
        assertTrue(model.hasEquipment(secondItem));
        assertEquals(1, model.getAddressBook().getPersonList().size());
        reload();
        String json = Files.readString(storage.getAddressBookFilePath());
        assertFalse(json.contains("\"availability\""));
        assertFalse(json.contains("\"holder\""));
    }

    @Test
    public void loanRoutes_parameterErrors_preserveAllCollectionsAndNeverSave() throws Exception {
        LocalDate date = LocalDate.now().minusDays(3);
        execute(addLoan(firstItem, FIRST, date, date.plusDays(5)), DisplayRequest.equipmentDetails(firstItem));
        List<String> fields = List.of("uuid/" + firstItem, "nus/" + FIRST,
                "assigned/" + date, "expected/" + date.plusDays(5));
        String validAdd = "loans add " + String.join(" ", fields);
        for (int index = 0; index < fields.size(); index++) {
            List<String> reduced = new ArrayList<>(fields);
            String removed = reduced.remove(index);
            assertRejected(ParseException.class, "loans add " + String.join(" ", reduced));
            assertRejected(ParseException.class, validAdd + " " + removed);
            reduced.add(removed.substring(0, removed.indexOf('/') + 1));
            assertRejected(ParseException.class, "loans add " + String.join("\t", reduced));
        }
        for (String input : new String[] {
            validAdd + " unknown/value", validAdd + " notes/text", "loans add preamble " + String.join(" ", fields),
            validAdd.replace("assigned/" + date, "assigned/2026-02-30"),
            validAdd.replace(firstItem.toString(), "not-a-uuid"), validAdd.replace(FIRST.value, "bad-id"),
            "loans return", "loans return uuid/" + firstItem, "loans return returned/" + date,
            "loans return uuid/ returned/" + date, "loans return uuid/" + firstItem + " returned/",
            "loans return uuid/" + firstItem + " returned/" + date + " uuid/" + firstItem,
            "loans return uuid/" + firstItem + " returned/" + date + " returned/" + date,
            "loans return uuid/" + firstItem + " returned/" + date + " unknown/value",
            "loans return uuid/not-a-uuid returned/" + date,
            "loans return uuid/" + firstItem + " returned/2026-02-30",
            "loans return preamble uuid/" + firstItem + " returned/" + date
        }) {
            assertRejected(ParseException.class, input);
        }
    }

    @Test
    public void loanRoutes_businessFailures_preserveSavedDataAndReportInvalidAssignmentsAndReturns() throws Exception {
        LocalDate assigned = LocalDate.now().minusDays(3);
        for (String condition : List.of("DAMAGED", "UNDER_REPAIR")) {
            UUID unavailable = addEquipment(condition);
            assertRejected(CommandException.class, addLoan(unavailable, FIRST, assigned, assigned.plusDays(1)));
            assertEquals(Availability.UNAVAILABLE, model.getAvailability(unavailable));
        }
        assertRejected(CommandException.class, addLoan(UNKNOWN, FIRST, assigned, assigned));
        assertRejected(CommandException.class, addLoan(firstItem, new NusId("A9999999X"), assigned, assigned));
        assertRejected(CommandException.class, addLoan(firstItem, FIRST,
                LocalDate.now().plusDays(2), LocalDate.now().plusDays(3)));
        assertRejected(CommandException.class, addLoan(firstItem, FIRST, assigned, assigned.minusDays(1)));
        assertRejected(CommandException.class, "loans return uuid/" + UNKNOWN + " returned/" + assigned);
        assertRejected(CommandException.class, "loans return uuid/" + secondItem + " returned/" + assigned);
        execute(addLoan(firstItem, FIRST, assigned, assigned.plusDays(1)), DisplayRequest.equipmentDetails(firstItem));
        assertRejected(CommandException.class, addLoan(firstItem, SECOND, assigned, assigned));
        assertRejected(CommandException.class, "loans return uuid/" + firstItem + " returned/" + assigned.minusDays(1));
        assertRejected(CommandException.class, "loans return uuid/" + firstItem
                + " returned/" + LocalDate.now().plusDays(2));
        // A return later than expected is valid and remains returned after reload.
        execute("loans return uuid/" + firstItem + " returned/" + LocalDate.now(),
                DisplayRequest.equipmentDetails(firstItem));
        reload();
        assertEquals(Availability.AVAILABLE, model.getAvailability(firstItem));
        assertRejected(CommandException.class, "loans return uuid/" + firstItem + " returned/" + LocalDate.now());
    }

    @Test
    public void loanRoutes_sameDayDatesAndFairCondition_acceptBoundaryValues() throws Exception {
        UUID fair = addEquipment("FAIR");
        for (UUID uuid : List.of(firstItem, fair)) {
            LocalDate date = LocalDate.now();
            execute(addLoan(uuid, FIRST, date, date), DisplayRequest.equipmentDetails(uuid));
            execute("loans return uuid/" + uuid + " returned/" + date, DisplayRequest.equipmentDetails(uuid));
            assertEquals(Availability.AVAILABLE, model.getAvailability(uuid));
            assertTrue(model.findOpenLoansForMember(FIRST).isEmpty());
        }
        reload();
        assertEquals(2, model.getLoanList().size());
    }

    @Test
    public void loanRoutes_missingReferencedMember_rejectsWithoutSaving() throws Exception {
        LocalDate date = LocalDate.now().minusDays(3);
        execute(addLoan(firstItem, FIRST, date, date), DisplayRequest.equipmentDetails(firstItem));
        model = new ModelManager(model.getAddressBook(), new UserPrefs()) {
            @Override
            public Model createTransactionSnapshot() {
                return new ModelManager(getAddressBook(), getUserPrefs()) {
                    @Override
                    public Optional<Member> findMember(NusId nusId) {
                        return nusId.equals(FIRST) ? Optional.empty() : super.findMember(nusId);
                    }
                };
            }

            @Override
            public Optional<Member> findMember(NusId nusId) {
                return nusId.equals(FIRST) ? Optional.empty() : super.findMember(nusId);
            }
        };
        logic = new LogicManager(model, storage);
        assertRejected(CommandException.class, "loans return uuid/" + firstItem + " returned/" + date);
        assertRejected(CommandException.class, addLoan(secondItem, FIRST, date, date));
    }

    @Test
    public void loanRoutes_saveErrors_preserveCausesMemoryAndLastSavedFile() throws Exception {
        LocalDate date = LocalDate.now().minusDays(3);
        IOException assignmentFailure = new IOException("assignment save failed");
        byte[] initialFile = Files.readAllBytes(storage.getAddressBookFilePath());
        storage.failure = assignmentFailure;
        CommandException assigned = assertThrows(CommandException.class, () ->
                logic.execute(addLoan(firstItem, FIRST, date, date.plusDays(1))));
        assertSame(assignmentFailure, assigned.getCause());
        assertTrue(model.findOpenLoan(firstItem).isEmpty());
        assertArrayEquals(initialFile, Files.readAllBytes(storage.getAddressBookFilePath()));
        assertTrue(new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs())
                .findOpenLoan(firstItem).isEmpty());
        storage.failure = null;
        logic.execute(addLoan(firstItem, FIRST, date, date.plusDays(1)));
        byte[] openFile = Files.readAllBytes(storage.getAddressBookFilePath());
        IOException returnFailure = new IOException("return save failed");
        storage.failure = returnFailure;
        CommandException returned = assertThrows(CommandException.class, () ->
                logic.execute("loans return uuid/" + firstItem + " returned/" + date));
        assertSame(returnFailure, returned.getCause());
        assertTrue(model.findOpenLoan(firstItem).isPresent());
        assertArrayEquals(openFile, Files.readAllBytes(storage.getAddressBookFilePath()));
        assertTrue(new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs())
                .findOpenLoan(firstItem).isPresent());
    }

    private UUID addEquipment(String condition) throws Exception {
        CommandResult result = execute("equipment add name/Camera category/Photography condition/" + condition
                + " notes/Case included", null);
        UUID uuid = result.getDisplayRequest().getEquipmentUuid().orElseThrow();
        assertEquals(DisplayRequest.equipmentDetails(uuid), result.getDisplayRequest());
        return uuid;
    }

    private static String addLoan(UUID uuid, NusId nusId, LocalDate assigned, LocalDate expected) {
        return "loans add uuid/" + uuid + " nus/" + nusId + " assigned/" + assigned + " expected/" + expected;
    }

    private CommandResult execute(String input, DisplayRequest display) throws Exception {
        int saves = storage.saves;
        CommandResult result = logic.execute(input);
        assertEquals(saves + (input.equals("equipment list") ? 0 : 1), storage.saves, input);
        if (display != null) {
            assertEquals(display, result.getDisplayRequest(), input);
        }
        assertFalse(result.isExit());
        assertFalse(result.isShowHelp());
        return result;
    }

    private void assertRejected(Class<? extends Exception> expected, String input) throws Exception {
        AddressBook before = new AddressBook(model.getAddressBook());
        byte[] file = Files.readAllBytes(storage.getAddressBookFilePath());
        int saves = storage.saves;
        assertThrows(expected, () -> logic.execute(input), input);
        assertEquals(before, model.getAddressBook(), input);
        assertArrayEquals(file, Files.readAllBytes(storage.getAddressBookFilePath()), input);
        assertEquals(saves, storage.saves, input);
    }

    private void reload() throws Exception {
        AddressBook before = new AddressBook(model.getAddressBook());
        model = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        logic = new LogicManager(model, storage);
        assertEquals(before, model.getAddressBook());
    }

    /** Observe calls while retaining the real JSON save/read implementation. */
    private static class CountingStorage extends StorageManager {
        private int saves;
        private IOException failure;

        CountingStorage(Path directory) {
            super(new JsonAddressBookStorage(directory.resolve("full-routing.json")),
                    new JsonUserPrefsStorage(directory.resolve("prefs.json")));
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook data) throws IOException {
            saves++;
            if (failure != null) {
                throw failure;
            }
            super.saveAddressBook(data);
        }
    }
}
