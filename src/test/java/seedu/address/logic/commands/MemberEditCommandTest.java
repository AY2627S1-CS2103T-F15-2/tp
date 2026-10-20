package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalMembers.ALEX;
import static seedu.address.testutil.TypicalMembers.BEN;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.MemberEditCommandParser;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.loan.Loan;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.model.member.exceptions.MemberNotFoundException;
import seedu.address.model.person.Name;

/**
 * Checks edits against real member and loan collections, including rejected state preservation.
 */
public class MemberEditCommandTest {
    @Test
    public void execute_partialEdit_keepsUnspecifiedDetailsIdentityAndLoanHistory() throws Exception {
        ModelManager model = new ModelManager();
        model.addMember(ALEX);
        model.addMember(BEN);
        UUID uuid = UUID.randomUUID();
        model.addEquipment(new Equipment(uuid, "Camera", "Photography", Condition.GOOD, ""));
        LocalDate date = LocalDate.of(2026, 1, 1);
        Loan closed = new Loan(uuid, ALEX.getNusId(), date, date.plusDays(1), date);
        Loan open = new Loan(uuid, ALEX.getNusId(), date.plusDays(2), date.plusDays(3), null);
        model.addLoan(closed);
        model.addLoan(open);
        CommandResult result = parse("name/Updated Name").execute(model);
        Member edited = model.findMember(ALEX.getNusId()).orElseThrow();
        assertEquals(Name.forMember("Updated Name"), edited.getName());
        assertEquals(ALEX.getNusId(), edited.getNusId());
        assertEquals(ALEX.getPhone(), edited.getPhone());
        assertEquals(ALEX.getEmail(), edited.getEmail());
        assertEquals(List.of(edited, BEN), model.getMemberList());
        assertEquals(List.of(closed, open), model.getLoanList());
        assertEquals(DisplayRequest.memberDetails(ALEX.getNusId()), result.getDisplayRequest());
        assertTrue(result.getFeedbackToUser().contains("Updated Name"));
        assertTrue(parse("phone/98765432").isTransactional());
    }

    @Test
    public void execute_fullEdit_allowsMatchingAnotherMembersDetailsWithoutMergingIdentities() throws Exception {
        ModelManager model = new ModelManager();
        model.addMember(ALEX);
        model.addMember(BEN);
        parse("name/" + BEN.getName() + " phone/" + BEN.getPhone() + " email/" + BEN.getEmail()).execute(model);
        Member edited = model.findMember(ALEX.getNusId()).orElseThrow();
        assertEquals(BEN.getName(), edited.getName());
        assertEquals(BEN.getPhone(), edited.getPhone());
        assertEquals(BEN.getEmail(), edited.getEmail());
        assertEquals(2, model.getMemberList().size());
        assertEquals(BEN, model.findMember(BEN.getNusId()).orElseThrow());
    }

    @Test
    public void execute_missingMemberAndNoOp_rejectsWithoutChangingState() throws Exception {
        ModelManager model = new ModelManager();
        CommandException missing = assertThrows(CommandException.class, () -> parse("name/Alex").execute(model));
        assertInstanceOf(MemberNotFoundException.class, missing.getCause());
        model.addMember(ALEX);
        AddressBook before = new AddressBook(model.getAddressBook());
        CommandException unchanged = assertThrows(CommandException.class, () ->
                parse("name/" + ALEX.getName()).execute(model));
        assertEquals(MemberEditCommand.MESSAGE_NO_CHANGE, unchanged.getMessage());
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void constructorAndExecute_invalidArguments_rejectsMisuse() throws Exception {
        assertThrows(NullPointerException.class, () -> new MemberEditCommand(null,
                Optional.of(ALEX.getName()), Optional.empty(), Optional.empty()));
        assertThrows(NullPointerException.class, () -> new MemberEditCommand(ALEX.getNusId(),
                null, Optional.empty(), Optional.empty()));
        assertThrows(NullPointerException.class, () -> new MemberEditCommand(ALEX.getNusId(),
                Optional.empty(), null, Optional.empty()));
        assertThrows(NullPointerException.class, () -> new MemberEditCommand(ALEX.getNusId(),
                Optional.empty(), Optional.empty(), null));
        assertThrows(IllegalArgumentException.class, () -> new MemberEditCommand(ALEX.getNusId(),
                Optional.empty(), Optional.empty(), Optional.empty()));
        assertThrows(NullPointerException.class, () -> parse("name/Alex").execute(null));
    }

    @Test
    public void execute_phoneOnlyAndEmailOnly_preservesOtherDetails() throws Exception {
        ModelManager model = new ModelManager();
        model.addMember(ALEX);
        parse("phone/+65 9876-5432").execute(model);
        Member phoneEdited = model.findMember(ALEX.getNusId()).orElseThrow();
        assertEquals("+6598765432", phoneEdited.getPhone().value);
        assertEquals(ALEX.getName(), phoneEdited.getName());
        assertEquals(ALEX.getEmail(), phoneEdited.getEmail());
        parse("email/UPDATED@EXAMPLE.COM").execute(model);
        Member emailEdited = model.findMember(ALEX.getNusId()).orElseThrow();
        assertEquals("updated@example.com", emailEdited.getEmail().value);
        assertEquals(phoneEdited.getPhone(), emailEdited.getPhone());
        assertEquals(ALEX.getName(), emailEdited.getName());
    }

    @Test
    public void equalsHashCodeAndToString_retainedValues_distinguishesEdits() throws Exception {
        MemberEditCommand command = parse("name/Alex");
        assertEquals(command, command);
        assertEquals(command, parse("name/Alex"));
        assertEquals(command.hashCode(), parse("name/Alex").hashCode());
        assertNotEquals(command, parse("name/Ben"));
        assertNotEquals(command, parse("phone/98765432"));
        assertNotEquals(parse("name/Alex phone/98765432"), parse("name/Alex phone/91234567"));
        assertNotEquals(parse("name/Alex email/first@example.com"), parse("name/Alex email/second@example.com"));
        assertNotEquals(command, new MemberEditCommand(new NusId("A0000001X"),
                Optional.of(Name.forMember("Alex")), Optional.empty(), Optional.empty()));
        assertNotEquals(command, null);
        assertNotEquals(command, "member edit");
        assertTrue(command.toString().contains(ALEX.getNusId().toString()));
    }

    @Test
    public void setMember_invalidReplacement_rejectsAtModelBoundaryWithoutChangingState() {
        ModelManager model = new ModelManager();
        assertThrows(NullPointerException.class, () -> model.setMember(null, ALEX));
        assertThrows(NullPointerException.class, () -> model.setMember(ALEX, null));
        assertThrows(MemberNotFoundException.class, () -> model.setMember(ALEX, ALEX));
        model.addMember(ALEX);
        assertThrows(IllegalArgumentException.class, () -> model.setMember(ALEX, BEN));
        assertThrows(MemberNotFoundException.class, () -> model.setMember(BEN, BEN));
        assertEquals(List.of(ALEX), model.getMemberList());
    }

    private MemberEditCommand parse(String fields) throws Exception {
        return new MemberEditCommandParser().parse("nus/" + ALEX.getNusId() + " " + fields);
    }
}
