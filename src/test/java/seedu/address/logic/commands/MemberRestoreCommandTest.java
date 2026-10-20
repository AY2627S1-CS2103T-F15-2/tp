package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalMembers.ALEX;
import static seedu.address.testutil.TypicalMembers.BEN;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.Equipment;

/**
 * Checks recovery and rejected lifecycle commands without time-dependent behavior.
 */
public class MemberRestoreCommandTest {
    @Test
    public void execute_deletedMember_restoresSameDetailsAndRequestsDetails() throws Exception {
        ModelManager model = new ModelManager();
        model.addMember(ALEX);
        new MemberDeleteCommand(ALEX.getNusId()).execute(model);
        CommandException repeated = assertThrows(CommandException.class, () ->
                new MemberDeleteCommand(ALEX.getNusId()).execute(model));
        assertTrue(repeated.getMessage().contains("already deleted"));
        MemberRestoreCommand command = new MemberRestoreCommand(ALEX.getNusId());
        CommandResult result = command.execute(model);
        assertEquals(List.of(ALEX), model.getMemberList());
        assertEquals(DisplayRequest.memberDetails(ALEX.getNusId()), result.getDisplayRequest());
        assertTrue(result.getFeedbackToUser().contains("Restored member"));
        assertTrue(command.isTransactional());
    }

    @Test
    public void execute_activeMissingAndNullMembers_rejectsWithoutMutation() {
        ModelManager model = new ModelManager();
        model.addMember(ALEX);
        AddressBook before = new AddressBook(model.getAddressBook());
        assertThrows(CommandException.class, () -> new MemberRestoreCommand(ALEX.getNusId()).execute(model));
        assertThrows(CommandException.class, () -> new MemberRestoreCommand(BEN.getNusId()).execute(model));
        assertThrows(NullPointerException.class, () -> new MemberRestoreCommand(null));
        assertThrows(NullPointerException.class, () -> new MemberRestoreCommand(ALEX.getNusId()).execute(null));
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void execute_deletedThenRestoredMember_blocksAndAllowsNewLoans() throws Exception {
        ModelManager model = new ModelManager();
        model.addMember(ALEX);
        UUID item = new UUID(0, 1);
        model.addEquipment(new Equipment(item, "Camera", "Photography", Condition.GOOD, ""));
        new MemberDeleteCommand(ALEX.getNusId()).execute(model);
        AddressBook deleted = new AddressBook(model.getAddressBook());
        LocalDate date = LocalDate.of(2000, 1, 1);
        LoanAddCommand loan = new LoanAddCommand(item, ALEX.getNusId(), date, date.plusDays(1));
        CommandException error = assertThrows(CommandException.class, () -> loan.execute(model));
        assertTrue(error.getMessage().contains("deleted member"));
        assertEquals(deleted, model.getAddressBook());
        new MemberRestoreCommand(ALEX.getNusId()).execute(model);
        loan.execute(model);
        assertTrue(model.findOpenLoan(item).isPresent());
    }

    @Test
    public void equalsHashCodeAndToString_identity_distinguishesRestoreTargets() {
        MemberRestoreCommand command = new MemberRestoreCommand(ALEX.getNusId());
        assertEquals(command, command);
        assertEquals(command, new MemberRestoreCommand(ALEX.getNusId()));
        assertEquals(command.hashCode(), new MemberRestoreCommand(ALEX.getNusId()).hashCode());
        assertNotEquals(command, new MemberRestoreCommand(BEN.getNusId()));
        assertNotEquals(command, null);
        assertNotEquals(command, "member restore");
        assertTrue(command.toString().contains(ALEX.getNusId().toString()));
    }
}
