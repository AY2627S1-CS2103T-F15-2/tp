package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.AMY;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DisplayRequest;
import seedu.address.logic.commands.MemberListCommand;
import seedu.address.logic.commands.MemberViewCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.equipment.Availability;
import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.equipment.exceptions.EquipmentNotFoundException;
import seedu.address.model.loan.Loan;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.model.query.DirectoryQuery;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class LogicManagerTest {
    private static final IOException DUMMY_IO_EXCEPTION = new IOException("dummy IO exception");
    private static final IOException DUMMY_AD_EXCEPTION = new AccessDeniedException("dummy access denied exception");

    @TempDir
    public Path temporaryFolder;

    private Model model = new ModelManager();
    private Logic logic;

    @BeforeEach
    public void setUp() {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_invalidCommandFormat_throwsParseException() {
        String invalidCommand = "uicfhmowqewca";
        assertParseException(invalidCommand, MESSAGE_UNKNOWN_COMMAND);
    }

    @Test
    public void execute_commandExecutionError_throwsCommandException() {
        String missingId = "A0000001X";
        assertCommandException("member view nus/" + missingId,
                String.format(MemberViewCommand.MESSAGE_NOT_FOUND, missingId));
    }

    @Test
    public void describeAction_singleWordWithWhitespace_preservesCommandName() {
        assertEquals("undo", LogicManager.describeAction("undo"));
        assertEquals("redo", LogicManager.describeAction(" \tredo \n"));
    }

    @Test
    public void describeAction_supportedWhitespace_omitsParameters() {
        for (String separator : new String[] {" ", "\t", "\u2003", "\u00a0", " \u2003\t\u00a0"}) {
            assertEquals("member edit", LogicManager.describeAction(separator + "member" + separator
                    + "edit" + separator + "nus/A0123456X" + separator + "name/SpaceProbe" + separator));
            assertEquals("undo", LogicManager.describeAction(separator + "undo" + separator));
        }
    }

    @Test
    public void execute_unicodeSeparatedEdit_undoRedoRestoresAndPersistsWithConciseFeedback() throws Exception {
        Member original = new Member(new NusId("A0123456X"), AMY.getName(), AMY.getPhone(), AMY.getEmail());
        model.addMember(original);
        JsonAddressBookStorage saved = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        for (String separator : new String[] {" ", "\u2003", "\u00a0"}) {
            logic.execute(String.join(separator, "member", "edit", "nus/A0123456X", "name/SpaceProbe"));
            assertEquals("SpaceProbe", model.getMemberList().getFirst().getName().fullName);
            assertEquals(model.getAddressBook(), saved.readAddressBook().orElseThrow());
            assertEquals("Undid: member edit.", logic.execute("undo").getFeedbackToUser());
            assertEquals(original, model.getMemberList().getFirst());
            assertEquals(model.getAddressBook(), saved.readAddressBook().orElseThrow());
            assertEquals("Redid: member edit.", logic.execute("redo").getFeedbackToUser());
            assertEquals("SpaceProbe", model.getMemberList().getFirst().getName().fullName);
            assertEquals(model.getAddressBook(), saved.readAddressBook().orElseThrow());
            logic.execute("undo");
        }
    }

    @Test
    public void execute_validCommand_success() throws Exception {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertCommandSuccess("member list", String.format(MemberListCommand.MESSAGE_SUCCESS, 0), expectedModel);
    }

    @Test
    public void execute_storageThrowsIoException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_IO_EXCEPTION, String.format(
                LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_storageThrowsAdException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_AD_EXCEPTION, String.format(
                LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, temporaryFolder.resolve("ExceptionData.json")));
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> logic.getFilteredPersonList().remove(0));
    }

    @Test
    public void getAddressBook_returnsSameRootAndLiveUnmodifiableLists() {
        ReadOnlyAddressBook root = logic.getAddressBook();
        assertSame(model.getAddressBook(), root);
        var persons = root.getPersonList();
        var members = root.getMemberList();
        var equipment = root.getEquipmentList();
        var loans = root.getLoanList();
        Member member = new Member(new NusId("A0123456X"), AMY.getName(), AMY.getPhone(), AMY.getEmail());
        Equipment item = new Equipment(new UUID(0, 1), "Camera", "Photography", Condition.GOOD, "");
        LocalDate date = LocalDate.of(2026, 1, 1);
        Loan loan = new Loan(item.getUuid(), member.getNusId(), date, date, null);
        model.addPerson(AMY);
        model.addMember(member);
        model.addEquipment(item);
        model.addLoan(loan);
        assertSame(root, logic.getAddressBook());
        assertSame(members, root.getMemberList());
        assertSame(equipment, root.getEquipmentList());
        assertSame(loans, root.getLoanList());
        assertEquals(AMY, persons.get(0));
        assertEquals(member, members.get(0));
        assertEquals(item, equipment.get(0));
        assertEquals(loan, loans.get(0));
        assertThrows(UnsupportedOperationException.class, () -> persons.remove(0));
        assertThrows(UnsupportedOperationException.class, () -> members.remove(0));
        assertThrows(UnsupportedOperationException.class, () -> equipment.remove(0));
        assertThrows(UnsupportedOperationException.class, () -> loans.remove(0));
        assertFalse(Files.exists(temporaryFolder.resolve("addressBook.json")));
    }

    @Test
    public void getAvailability_tracksRealLoansAndAllConditions() {
        Member member = new Member(new NusId("A0123456X"), AMY.getName(), AMY.getPhone(), AMY.getEmail());
        model.addMember(member);
        LocalDate date = LocalDate.of(2026, 1, 1);
        for (Condition condition : Condition.values()) {
            UUID uuid = new UUID(0, condition.ordinal() + 1);
            model.addEquipment(new Equipment(uuid, "Camera", "Photography", condition, ""));
            Availability idle = condition == Condition.GOOD || condition == Condition.FAIR
                    ? Availability.AVAILABLE : Availability.UNAVAILABLE;
            assertEquals(idle, logic.getAvailability(uuid));
            model.addLoan(new Loan(uuid, member.getNusId(), date, date, null));
            assertEquals(Availability.ASSIGNED, logic.getAvailability(uuid));
            model.closeLoan(uuid, date);
            assertEquals(idle, logic.getAvailability(uuid));
        }
        assertThrows(EquipmentNotFoundException.class, () -> logic.getAvailability(new UUID(0, 100)));
        assertThrows(NullPointerException.class, () -> logic.getAvailability(null));
        assertFalse(Files.exists(temporaryFolder.resolve("addressBook.json")));
    }

    @Test
    public void getAvailability_delegatesExactIdentityAndPreservesFailure() {
        UUID uuid = new UUID(0, 7);
        EquipmentNotFoundException failure = new EquipmentNotFoundException();
        Model boundary = new ModelManager() {
            @Override
            public Availability getAvailability(UUID requested) {
                assertSame(uuid, requested);
                throw failure;
            }
        };
        JsonAddressBookStorage addressStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("boundary.json"));
        Logic boundaryLogic = new LogicManager(boundary, new StorageManager(addressStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("boundary-prefs.json"))));
        EquipmentNotFoundException actual = org.junit.jupiter.api.Assertions.assertThrows(
                EquipmentNotFoundException.class, () -> boundaryLogic.getAvailability(uuid));
        assertSame(failure, actual);
    }

    /**
     * Executes the command and confirms that
     * - no exceptions are thrown <br>
     * - the feedback message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandSuccess(String inputCommand, String expectedMessage,
            Model expectedModel) throws CommandException, ParseException {
        CommandResult result = logic.execute(inputCommand);
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(DisplayRequest.members(DirectoryQuery.all()), result.getDisplayRequest());
        assertEquals(expectedModel, model);
    }

    /**
     * Executes the command, confirms that a ParseException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertParseException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, ParseException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that a CommandException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, CommandException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that the exception is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage) {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertCommandFailure(inputCommand, expectedException, expectedMessage, expectedModel);
    }

    /**
     * Executes the command and confirms that
     * - the {@code expectedException} is thrown <br>
     * - the resulting error message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandSuccess(String, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage, Model expectedModel) {
        assertThrows(expectedException, expectedMessage, () -> logic.execute(inputCommand));
        assertEquals(expectedModel, model);
    }

    /**
     * Tests the Logic component's handling of an {@code IOException} thrown by the Storage component.
     *
     * @param e the exception to be thrown by the Storage component
     * @param expectedMessage the message expected inside exception thrown by the Logic component
     */
    private void assertCommandFailureForExceptionFromStorage(IOException e, String expectedMessage) {
        Path dataPath = temporaryFolder.resolve("ExceptionData.json");

        // Inject LogicManager with a JsonAddressBookStorage that throws the IOException e when saving
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(dataPath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw e;
            }
        };

        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("ExceptionUserPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);

        logic = new LogicManager(model, storage);

        // A transactional member add must propagate the save error without publishing its candidate state.
        String addCommand = "member add nus/A0000001X name/Amy Bee phone/91234567 email/amy@example.com";
        assertCommandFailure(addCommand, CommandException.class, expectedMessage);
    }
}
