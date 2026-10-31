package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalMembers.ALEX;
import static seedu.address.testutil.TypicalMembers.BEN;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;

public class MemberCommandInvariantTest {
    @Test
    public void execute_wrongIdentityFromModel_throwsAssertionErrorBeforeMutation() {
        assertTrue(MemberEditCommand.class.desiredAssertionStatus());
        assertTrue(MemberRestoreCommand.class.desiredAssertionStatus());
        ModelManager model = new ModelManager() {
            @Override
            public Optional<Member> findMember(NusId nusId) {
                return Optional.of(getMemberList().getFirst());
            }
        };
        model.addMember(new Member(BEN.getNusId(), BEN.getName(), BEN.getPhone(), BEN.getEmail(), true));
        AddressBook before = new AddressBook(model.getAddressBook());
        List<Command> commands = List.of(new MemberEditCommand(ALEX.getNusId(), Optional.of(ALEX.getName()),
                Optional.empty(), Optional.empty()), new MemberRestoreCommand(ALEX.getNusId()));

        for (Command command : commands) {
            AssertionError error = assertThrows(AssertionError.class, () -> command.execute(model));
            assertEquals("Member lookup must preserve the requested identity.", error.getMessage());
            assertEquals(before, model.getAddressBook());
        }
    }
}
