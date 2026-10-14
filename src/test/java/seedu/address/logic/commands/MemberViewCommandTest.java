package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.ViewTestData.ALEX;
import static seedu.address.testutil.ViewTestData.CAMERA;
import static seedu.address.testutil.ViewTestData.OTHER_ALEX;
import static seedu.address.testutil.ViewTestData.OTHER_CAMERA;
import static seedu.address.testutil.ViewTestData.SPEAKER;
import static seedu.address.testutil.ViewTestData.loan;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.equipment.exceptions.EquipmentNotFoundException;
import seedu.address.model.loan.Loan;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.model.member.exceptions.MemberNotFoundException;
import seedu.address.testutil.ViewTestData;

public class MemberViewCommandTest {
    @Test
    public void execute_sameNamesZeroAndMultipleHoldings_resolvesIdentityWithoutMutation() throws Exception {
        ModelManager model = ViewTestData.model();
        assertView(model, ALEX.getNusId(), 0);
        model.addLoan(loan(CAMERA, ALEX, true));
        model.addLoan(loan(OTHER_CAMERA, ALEX, true));
        model.addLoan(loan(SPEAKER, ALEX, false));
        assertView(model, new NusId("a0123456x"), 2);
        assertView(model, OTHER_ALEX.getNusId(), 0);
    }

    @Test
    public void execute_returnAndReassignment_readsCurrentState() throws Exception {
        ModelManager model = ViewTestData.model();
        MemberViewCommand command = new MemberViewCommand(ALEX.getNusId());
        model.addLoan(loan(CAMERA, ALEX, true));
        assertTrue(command.execute(model).getFeedbackToUser().contains("1 item(s)"));
        model.closeLoan(CAMERA.getUuid(), ViewTestData.EXPECTED);
        assertTrue(command.execute(model).getFeedbackToUser().contains("0 item(s)"));
        model.addLoan(loan(CAMERA, OTHER_ALEX, true));
        assertView(model, ALEX.getNusId(), 0);
        assertView(model, OTHER_ALEX.getNusId(), 1);
    }

    @Test
    public void execute_unknownIdentity_reportsIdAndPreservesAllCollections() {
        ModelManager model = ViewTestData.model();
        assertFailure(model, new NusId("A0999999Z"), "A0999999Z");
    }

    @Test
    public void execute_missingEquipmentReference_reportsUuidAndPreservesState() {
        ModelManager model = new ModelManager(ViewTestData.model().getAddressBook(), new UserPrefs()) {
            @Override
            public Optional<Equipment> findEquipment(UUID uuid) {
                assertEquals(CAMERA.getUuid(), uuid);
                return Optional.empty();
            }
        };
        model.addLoan(loan(CAMERA, ALEX, true));
        assertFailure(model, ALEX.getNusId(), "Missing equipment reference " + CAMERA.getUuid());
    }

    @Test
    public void execute_invalidCurrentLoanQuery_reportsInconsistentData() {
        for (Loan loan : List.of(loan(CAMERA, ALEX, false), loan(CAMERA, OTHER_ALEX, true))) {
            ModelManager model = new ModelManager(ViewTestData.model().getAddressBook(), new UserPrefs()) {
                @Override
                public List<Loan> findOpenLoansForMember(NusId id) {
                    assertEquals(ALEX.getNusId(), id);
                    return List.of(loan);
                }
            };
            assertFailure(model, ALEX.getNusId(), "Inconsistent current holdings");
        }
    }

    @Test
    public void execute_domainLookupFailures_preservesCauseAndState() {
        MemberNotFoundException memberFailure = new MemberNotFoundException();
        ModelManager missingMember = new ModelManager() {
            @Override
            public Optional<Member> findMember(NusId id) {
                throw memberFailure;
            }
        };
        assertSame(memberFailure, assertFailure(missingMember, ALEX.getNusId(), "Could not resolve").getCause());
        EquipmentNotFoundException equipmentFailure = new EquipmentNotFoundException();
        ModelManager missingEquipment = new ModelManager(ViewTestData.model().getAddressBook(), new UserPrefs()) {
            @Override
            public Optional<Equipment> findEquipment(UUID uuid) {
                throw equipmentFailure;
            }
        };
        missingEquipment.addLoan(loan(CAMERA, ALEX, true));
        assertSame(equipmentFailure, assertFailure(missingEquipment, ALEX.getNusId(), "Could not resolve").getCause());
    }

    @Test
    public void valueSemanticsAndNullArguments() {
        MemberViewCommand command = new MemberViewCommand(ALEX.getNusId());
        assertEquals(command, command);
        assertEquals(command, new MemberViewCommand(new NusId("a0123456x")));
        assertEquals(command.hashCode(), new MemberViewCommand(ALEX.getNusId()).hashCode());
        assertNotEquals(command, new MemberViewCommand(OTHER_ALEX.getNusId()));
        assertNotEquals(command, null);
        assertNotEquals(command, "view");
        assertTrue(command.toString().contains(ALEX.getNusId().value));
        assertThrows(NullPointerException.class, () -> new MemberViewCommand(null));
        assertThrows(NullPointerException.class, () -> command.execute(null));
    }

    private static void assertView(ModelManager model, NusId id, int count) throws Exception {
        AddressBook before = new AddressBook(model.getAddressBook());
        CommandResult result = new MemberViewCommand(id).execute(model);
        assertEquals(DisplayRequest.memberDetails(id), result.getDisplayRequest());
        assertTrue(result.getFeedbackToUser().contains(id.value));
        assertTrue(result.getFeedbackToUser().contains(count + " item(s)"));
        assertEquals(before, model.getAddressBook());
    }

    private static CommandException assertFailure(ModelManager model, NusId id, String message) {
        AddressBook before = new AddressBook(model.getAddressBook());
        CommandException failure = assertThrows(CommandException.class, () -> new MemberViewCommand(id).execute(model));
        assertTrue(failure.getMessage().contains(message), failure.getMessage());
        assertEquals(before, model.getAddressBook());
        return failure;
    }
}
