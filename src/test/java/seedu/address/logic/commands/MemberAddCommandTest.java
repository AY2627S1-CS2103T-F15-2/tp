package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.model.member.exceptions.DuplicateMemberException;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;

public class MemberAddCommandTest {

    private static final Member ALEX = member("A0123456X");

    @Test
    public void execute_validMember_addsAndRequestsDetails() throws Exception {
        ModelManager model = new ModelManager();
        CommandResult result = new MemberAddCommand(ALEX).execute(model);
        assertEquals(1, model.getMemberList().size());
        assertEquals(ALEX, model.findMember(ALEX.getNusId()).orElseThrow());
        assertEquals(DisplayRequest.memberDetails(ALEX.getNusId()), result.getDisplayRequest());
        assertTrue(result.getFeedbackToUser().contains("Alex Tan"));
        assertTrue(result.getFeedbackToUser().contains("A0123456X"));
        assertFalse(result.isExit());
        assertFalse(result.isShowHelp());
    }

    @Test
    public void execute_duplicateNormalizedId_preservesAllCollectionsAndCause() throws Exception {
        ModelManager model = new ModelManager();
        new MemberAddCommand(ALEX).execute(model);
        AddressBook before = new AddressBook(model.getAddressBook());
        Member duplicate = new Member(new NusId("a0123456x"), new Name("Different Name"),
                new Phone("98765432"), new Email("different@example.com"));
        CommandException error = assertThrows(CommandException.class, () -> {
            new MemberAddCommand(duplicate).execute(model);
        });
        assertInstanceOf(DuplicateMemberException.class, error.getCause());
        assertTrue(error.getMessage().contains("A0123456X"));
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void execute_sameNameDifferentIds_retainsBoth() throws Exception {
        ModelManager model = new ModelManager();
        new MemberAddCommand(ALEX).execute(model);
        Member second = member("A0234567R");
        new MemberAddCommand(second).execute(model);
        assertEquals(2, model.getMemberList().size());
        assertEquals(ALEX, model.findMember(ALEX.getNusId()).orElseThrow());
        assertEquals(second, model.findMember(second.getNusId()).orElseThrow());
    }

    @Test
    public void constructorAndExecute_nullArguments_rejectsMisuse() {
        assertThrows(NullPointerException.class, () -> new MemberAddCommand(null));
        assertThrows(NullPointerException.class, () -> new MemberAddCommand(ALEX).execute(null));
    }

    @Test
    public void equalsAndToString_retainedMember_valueBehavior() {
        MemberAddCommand command = new MemberAddCommand(ALEX);
        MemberAddCommand equal = new MemberAddCommand(member("a0123456x"));
        assertEquals(command, command);
        assertEquals(command, equal);
        assertEquals(command.hashCode(), equal.hashCode());
        assertNotEquals(command, new MemberAddCommand(member("A0234567R")));
        assertNotEquals(command, null);
        assertNotEquals(command, "member add");
        assertTrue(command.toString().contains(ALEX.toString()));
    }

    private static Member member(String nusId) {
        return new Member(new NusId(nusId), new Name("Alex Tan"),
                new Phone("91234567"), new Email("alex@example.com"));
    }
}
