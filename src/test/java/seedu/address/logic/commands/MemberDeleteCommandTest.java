package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.DeletionTestData.FIRST_EQUIPMENT;
import static seedu.address.logic.commands.DeletionTestData.FIRST_MEMBER;
import static seedu.address.logic.commands.DeletionTestData.LEGACY_PERSON;
import static seedu.address.logic.commands.DeletionTestData.SECOND_EQUIPMENT;
import static seedu.address.logic.commands.DeletionTestData.SECOND_MEMBER;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.loan.Loan;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.model.member.exceptions.MemberNotFoundException;

/**
 * Verifies normalized identity deletion and the root's history and open-loan integrity rules.
 */
public class MemberDeleteCommandTest {

    @Test
    public void execute_normalizedIdentity_preservesEqualNameAndOtherCollections() throws Exception {
        ModelManager model = DeletionTestData.model();
        var equipment = List.copyOf(model.getEquipmentList());
        CommandResult result = new MemberDeleteCommand(new NusId("a0123456x")).execute(model);
        assertEquals(List.of(SECOND_MEMBER), model.getMemberList());
        assertEquals(equipment, model.getEquipmentList());
        assertEquals(List.of(LEGACY_PERSON), model.getFilteredPersonList());
        assertEquals(DisplayRequest.members(), result.getDisplayRequest());
        assertEquals("Deleted member: A0123456X", result.getFeedbackToUser());
        assertFalse(result.isShowHelp());
        assertFalse(result.isExit());
    }

    @Test
    public void execute_resolvesStoredMember_passesActualEntityToModel() throws Exception {
        ModelManager model = new ModelManager() {
            @Override
            public void deleteMember(Member member) {
                assertSame(FIRST_MEMBER, member);
                super.deleteMember(member);
            }
        };
        model.addMember(FIRST_MEMBER);
        new MemberDeleteCommand(new NusId("a0123456x")).execute(model);
        assertTrue(model.getMemberList().isEmpty());
    }

    @Test
    public void execute_unknownIdentity_preservesAllDataAndReportsIdentity() {
        ModelManager model = DeletionTestData.model();
        model.addLoan(DeletionTestData.loan(FIRST_EQUIPMENT, FIRST_MEMBER, false));
        AddressBook before = new AddressBook(model.getAddressBook());
        NusId unknown = new NusId("A0999999Z");
        CommandException error = assertThrows(
                CommandException.class, () -> new MemberDeleteCommand(unknown).execute(model));
        assertInstanceOf(MemberNotFoundException.class, error.getCause());
        assertEquals(String.format(MemberDeleteCommand.MESSAGE_NOT_FOUND, unknown), error.getMessage());
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void execute_openLoan_preservesMemberAndClosedHistory() {
        ModelManager model = DeletionTestData.model();
        model.addLoan(DeletionTestData.loan(FIRST_EQUIPMENT, FIRST_MEMBER, false));
        model.addLoan(DeletionTestData.loan(SECOND_EQUIPMENT, FIRST_MEMBER, true));
        AddressBook before = new AddressBook(model.getAddressBook());
        CommandException error = assertThrows(
                CommandException.class, () -> new MemberDeleteCommand(FIRST_MEMBER.getNusId()).execute(model));
        assertInstanceOf(IllegalStateException.class, error.getCause());
        assertEquals(String.format(MemberDeleteCommand.MESSAGE_OPEN_LOAN, FIRST_MEMBER.getNusId()),
                error.getMessage());
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void execute_closedHistory_removesOnlyDeletedMembersLoans() throws Exception {
        ModelManager model = DeletionTestData.model();
        model.addLoan(DeletionTestData.loan(FIRST_EQUIPMENT, FIRST_MEMBER, false));
        model.addLoan(DeletionTestData.loan(SECOND_EQUIPMENT, FIRST_MEMBER, false));
        Loan retainedHistory = DeletionTestData.loan(FIRST_EQUIPMENT, SECOND_MEMBER, false);
        Loan retainedOpen = DeletionTestData.loan(SECOND_EQUIPMENT, SECOND_MEMBER, true);
        model.addLoan(retainedHistory);
        model.addLoan(retainedOpen);
        new MemberDeleteCommand(FIRST_MEMBER.getNusId()).execute(model);
        assertEquals(List.of(SECOND_MEMBER), model.getMemberList());
        assertEquals(List.of(retainedHistory, retainedOpen), model.getLoanList());
        assertEquals(List.of(FIRST_EQUIPMENT, SECOND_EQUIPMENT), model.getEquipmentList());
        assertEquals(List.of(LEGACY_PERSON), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_expectedModelFailure_preservesOriginalCause() {
        MemberNotFoundException failure = new MemberNotFoundException();
        ModelManager model = new ModelManager() {
            @Override
            public void deleteMember(Member member) {
                throw failure;
            }
        };
        model.addMember(FIRST_MEMBER);
        AddressBook before = new AddressBook(model.getAddressBook());
        CommandException error = assertThrows(
                CommandException.class, () -> new MemberDeleteCommand(FIRST_MEMBER.getNusId()).execute(model));
        assertSame(failure, error.getCause());
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void execute_openLoanModelFailure_preservesOriginalCause() {
        IllegalStateException failure = new IllegalStateException("Member still has an open loan");
        ModelManager model = new ModelManager() {
            @Override
            public void deleteMember(Member member) {
                throw failure;
            }
        };
        model.addMember(FIRST_MEMBER);
        model.addEquipment(FIRST_EQUIPMENT);
        model.addLoan(DeletionTestData.loan(FIRST_EQUIPMENT, FIRST_MEMBER, true));
        AddressBook before = new AddressBook(model.getAddressBook());
        CommandException error = assertThrows(
                CommandException.class, () -> new MemberDeleteCommand(FIRST_MEMBER.getNusId()).execute(model));
        assertSame(failure, error.getCause());
        assertEquals(String.format(MemberDeleteCommand.MESSAGE_OPEN_LOAN, FIRST_MEMBER.getNusId()),
                error.getMessage());
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void constructorAndExecute_nullArguments_rejectsMisuse() {
        assertThrows(NullPointerException.class, () -> new MemberDeleteCommand(null));
        assertThrows(NullPointerException.class, () -> new MemberDeleteCommand(FIRST_MEMBER.getNusId()).execute(null));
    }

    @Test
    public void equalsHashCodeAndToString_normalizedIdentity_valueBehavior() {
        MemberDeleteCommand command = new MemberDeleteCommand(FIRST_MEMBER.getNusId());
        MemberDeleteCommand equal = new MemberDeleteCommand(new NusId("a0123456x"));
        assertEquals(command, command);
        assertEquals(command, equal);
        assertEquals(command.hashCode(), equal.hashCode());
        assertNotEquals(command, new MemberDeleteCommand(SECOND_MEMBER.getNusId()));
        assertNotEquals(command, null);
        assertNotEquals(command, "member delete");
        assertTrue(command.toString().contains("A0123456X"));
    }
}
