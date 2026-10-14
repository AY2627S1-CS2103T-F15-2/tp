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
import static seedu.address.testutil.ViewTestData.loan;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.equipment.Availability;
import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.equipment.exceptions.EquipmentNotFoundException;
import seedu.address.model.loan.Loan;
import seedu.address.model.loan.exceptions.OpenLoanNotFoundException;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.model.member.exceptions.MemberNotFoundException;
import seedu.address.testutil.ViewTestData;

public class EquipmentViewCommandTest {
    @Test
    public void execute_allConditionsAndLoanStates_returnsCurrentAvailabilityWithoutMutation() throws Exception {
        for (Condition condition : Condition.values()) {
            ModelManager model = new ModelManager();
            model.addMember(ALEX);
            model.addEquipment(new Equipment(CAMERA.getUuid(), "Camera", "Photography", condition, "notes"));
            Availability idle = condition == Condition.GOOD || condition == Condition.FAIR
                    ? Availability.AVAILABLE : Availability.UNAVAILABLE;
            assertView(model, CAMERA.getUuid(), idle);
            model.addLoan(loan(CAMERA, ALEX, true));
            assertView(model, CAMERA.getUuid(), Availability.ASSIGNED);
            model.closeLoan(CAMERA.getUuid(), ViewTestData.EXPECTED);
            assertView(model, CAMERA.getUuid(), idle);
        }
    }

    @Test
    public void execute_sameNamesAndChangedHolder_resolvesExactIdentity() throws Exception {
        ModelManager model = ViewTestData.model();
        model.addLoan(loan(CAMERA, ALEX, true));
        assertView(model, OTHER_CAMERA.getUuid(), Availability.AVAILABLE);
        EquipmentViewCommand command = new EquipmentViewCommand(CAMERA.getUuid());
        model.closeLoan(CAMERA.getUuid(), ViewTestData.EXPECTED);
        model.addLoan(loan(CAMERA, OTHER_ALEX, true));
        assertEquals(DisplayRequest.equipmentDetails(CAMERA.getUuid()), command.execute(model).getDisplayRequest());
    }

    @Test
    public void execute_unknownEquipment_reportsUuidWithoutMutation() {
        UUID missing = new UUID(0, 999);
        assertFailure(ViewTestData.model(), missing, "No equipment with UUID " + missing);
    }

    @Test
    public void execute_missingHolder_reportsReferenceWithoutMutation() {
        ModelManager model = new ModelManager(ViewTestData.model().getAddressBook(), new UserPrefs()) {
            @Override
            public Optional<Member> findMember(NusId id) {
                assertEquals(ALEX.getNusId(), id);
                return Optional.empty();
            }
        };
        model.addLoan(loan(CAMERA, ALEX, true));
        assertFailure(model, CAMERA.getUuid(), "Missing holder reference " + ALEX.getNusId());
    }

    @Test
    public void execute_inconsistentLoanOrAvailability_rejectsWithoutMutation() {
        for (Loan loan : List.of(loan(CAMERA, ALEX, false), loan(OTHER_CAMERA, ALEX, true))) {
            ModelManager model = new ModelManager(ViewTestData.model().getAddressBook(), new UserPrefs()) {
                @Override
                public Optional<Loan> findOpenLoan(UUID uuid) {
                    return Optional.of(loan);
                }
            };
            assertFailure(model, CAMERA.getUuid(), "Inconsistent loan or availability");
        }
        ModelManager wrongAvailability = new ModelManager(ViewTestData.model().getAddressBook(), new UserPrefs()) {
            @Override
            public Availability getAvailability(UUID uuid) {
                return Availability.AVAILABLE;
            }
        };
        wrongAvailability.addLoan(loan(CAMERA, ALEX, true));
        assertFailure(wrongAvailability, CAMERA.getUuid(), "Inconsistent loan or availability");
        ModelManager missingLoan = new ModelManager(ViewTestData.model().getAddressBook(), new UserPrefs()) {
            @Override
            public Availability getAvailability(UUID uuid) {
                return Availability.ASSIGNED;
            }
        };
        assertFailure(missingLoan, CAMERA.getUuid(), "Missing open loan");
    }

    @Test
    public void execute_lookupFailures_preservesCauseAndState() {
        for (RuntimeException failure : List.of(new EquipmentNotFoundException(), new MemberNotFoundException(),
                new OpenLoanNotFoundException())) {
            ModelManager model = new ModelManager(ViewTestData.model().getAddressBook(), new UserPrefs()) {
                @Override
                public Availability getAvailability(UUID uuid) {
                    throw failure;
                }
            };
            assertSame(failure, assertFailure(model, CAMERA.getUuid(), "Could not resolve").getCause());
        }
    }

    @Test
    public void valueSemanticsAndNullArguments() {
        EquipmentViewCommand command = new EquipmentViewCommand(CAMERA.getUuid());
        assertEquals(command, command);
        assertEquals(command, new EquipmentViewCommand(UUID.fromString(CAMERA.getUuid().toString())));
        assertEquals(command.hashCode(), new EquipmentViewCommand(CAMERA.getUuid()).hashCode());
        assertNotEquals(command, new EquipmentViewCommand(OTHER_CAMERA.getUuid()));
        assertNotEquals(command, null);
        assertNotEquals(command, "view");
        assertTrue(command.toString().contains(CAMERA.getUuid().toString()));
        assertThrows(NullPointerException.class, () -> new EquipmentViewCommand(null));
        assertThrows(NullPointerException.class, () -> command.execute(null));
    }

    private static void assertView(ModelManager model, UUID id, Availability availability) throws Exception {
        AddressBook before = new AddressBook(model.getAddressBook());
        CommandResult result = new EquipmentViewCommand(id).execute(model);
        assertEquals(DisplayRequest.equipmentDetails(id), result.getDisplayRequest());
        assertTrue(result.getFeedbackToUser().contains(id.toString()));
        assertTrue(result.getFeedbackToUser().contains(availability.toString()));
        assertEquals(before, model.getAddressBook());
    }

    private static CommandException assertFailure(ModelManager model, UUID id, String message) {
        AddressBook before = new AddressBook(model.getAddressBook());
        CommandException failure = assertThrows(
                CommandException.class, () -> new EquipmentViewCommand(id).execute(model));
        assertTrue(failure.getMessage().contains(message), failure.getMessage());
        assertEquals(before, model.getAddressBook());
        return failure;
    }
}
