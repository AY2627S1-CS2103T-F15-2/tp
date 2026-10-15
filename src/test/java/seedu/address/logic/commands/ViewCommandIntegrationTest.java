package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.ViewTestData.ALEX;
import static seedu.address.testutil.ViewTestData.CAMERA;
import static seedu.address.testutil.ViewTestData.OTHER_ALEX;
import static seedu.address.testutil.ViewTestData.OTHER_CAMERA;
import static seedu.address.testutil.ViewTestData.loan;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.EquipmentViewCommandParser;
import seedu.address.logic.parser.MemberViewCommandParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.equipment.Availability;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.testutil.ViewTestData;

/** Direct parser/command/model/reload checks. Routed LogicManager saving belongs to Duy's integration. */
public class ViewCommandIntegrationTest {
    @TempDir
    public Path directory;

    @Test
    public void views_reloadedLoanTransitions_resolveCurrentIdentitiesWithoutWrites() throws Exception {
        ModelManager model = ViewTestData.model();
        model.addLoan(loan(CAMERA, ALEX, true));
        model.addLoan(loan(OTHER_CAMERA, OTHER_ALEX, false));
        JsonAddressBookStorage storage = new JsonAddressBookStorage(directory.resolve("views.json"));
        storage.saveAddressBook(model.getAddressBook());
        model = reload(storage);
        assertViews(model, storage, 1, Availability.ASSIGNED);
        model.closeLoan(CAMERA.getUuid(), ViewTestData.EXPECTED);
        storage.saveAddressBook(model.getAddressBook());
        model = reload(storage);
        assertViews(model, storage, 0, Availability.AVAILABLE);
        model.addLoan(loan(CAMERA, OTHER_ALEX, true));
        storage.saveAddressBook(model.getAddressBook());
        model = reload(storage);
        assertViews(model, storage, 0, Availability.ASSIGNED);
        assertEquals(OTHER_ALEX.getNusId(), model.findOpenLoan(CAMERA.getUuid()).orElseThrow().getMemberNusId());
        assertEquals(3, model.getLoanList().size());
        assertEquals(CAMERA, model.findEquipment(CAMERA.getUuid()).orElseThrow());
        assertEquals(ViewTestData.ASSIGNED, model.findOpenLoan(CAMERA.getUuid()).orElseThrow().getAssignedDate());
        assertEquals(ViewTestData.EXPECTED, model.findOpenLoan(CAMERA.getUuid()).orElseThrow().getExpectedReturnDate());
    }

    @Test
    public void invalidViews_preserveAllLoadedCollectionsAndSavedBytes() throws Exception {
        JsonAddressBookStorage storage = new JsonAddressBookStorage(directory.resolve("rejections.json"));
        storage.saveAddressBook(ViewTestData.model().getAddressBook());
        ModelManager model = reload(storage);
        AddressBook before = new AddressBook(model.getAddressBook());
        byte[] bytes = Files.readAllBytes(storage.getAddressBookFilePath());
        MemberViewCommandParser members = new MemberViewCommandParser();
        EquipmentViewCommandParser equipment = new EquipmentViewCommandParser();
        assertThrows(ParseException.class, () -> members.parse("nus/A0123456X nus/A0234567R").execute(model));
        assertThrows(ParseException.class, () -> equipment.parse("uuid/invalid").execute(model));
        assertThrows(ParseException.class, () -> equipment.parse(
                "uuid/" + CAMERA.getUuid() + " notes/x").execute(model));
        assertThrows(CommandException.class, () -> members.parse("nus/A0999999Z").execute(model));
        assertThrows(CommandException.class, () -> equipment.parse("uuid/00000000-0000-0000-0000-000000000099")
                .execute(model));
        assertEquals(before, model.getAddressBook());
        org.junit.jupiter.api.Assertions.assertArrayEquals(bytes, Files.readAllBytes(storage.getAddressBookFilePath()));
        assertEquals(before, reload(storage).getAddressBook());
    }

    private static void assertViews(ModelManager model, JsonAddressBookStorage storage,
            int holdings, Availability availability) throws Exception {
        AddressBook before = new AddressBook(model.getAddressBook());
        String json = Files.readString(storage.getAddressBookFilePath());
        CommandResult member = new MemberViewCommandParser().parse("nus/a0123456x").execute(model);
        CommandResult equipment = new EquipmentViewCommandParser().parse("uuid/" + CAMERA.getUuid()).execute(model);
        assertEquals(DisplayRequest.memberDetails(ALEX.getNusId()), member.getDisplayRequest());
        assertEquals(DisplayRequest.equipmentDetails(CAMERA.getUuid()), equipment.getDisplayRequest());
        assertTrue(member.getFeedbackToUser().contains(holdings + " item(s)"));
        assertTrue(equipment.getFeedbackToUser().contains(availability.toString()));
        assertEquals(before, model.getAddressBook());
        assertEquals(json, Files.readString(storage.getAddressBookFilePath()));
    }

    private static ModelManager reload(JsonAddressBookStorage storage) throws Exception {
        return new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
    }
}
