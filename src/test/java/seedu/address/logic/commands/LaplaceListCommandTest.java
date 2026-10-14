package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;

public class LaplaceListCommandTest {

    @Test
    public void execute_emptyModel_reportsZeroAndDirectoryTarget() throws Exception {
        ModelManager model = new ModelManager();
        assertEquals(new CommandResult("Listed 0 active members.", DisplayRequest.members()),
                new MemberListCommand().execute(model));
        assertEquals(new CommandResult("Listed 0 equipment items.", DisplayRequest.equipment()),
                new EquipmentListCommand().execute(model));
    }

    @Test
    public void execute_sameNamesAndDescriptions_countsAllIdentitiesWithoutMutation() throws Exception {
        ModelManager model = new ModelManager();
        Member later = new Member(new NusId("A0000002X"), new Name("Alex"),
                new Phone("91234567"), new Email("alex@example.com"));
        Member first = new Member(new NusId("A0000001X"), later.getName(), later.getPhone(), later.getEmail());
        Equipment second = new Equipment(new UUID(0, 2), "Camera", "Photography", Condition.GOOD, "");
        Equipment one = new Equipment(new UUID(0, 1), "Camera", "Photography", Condition.GOOD, "");
        model.addMember(later);
        model.addMember(first);
        model.addEquipment(second);
        model.addEquipment(one);
        AddressBook before = new AddressBook(model.getAddressBook());
        CommandResult members = new MemberListCommand().execute(model);
        CommandResult equipment = new EquipmentListCommand().execute(model);
        assertEquals("Listed 2 active members.", members.getFeedbackToUser());
        assertEquals(DisplayRequest.members(), members.getDisplayRequest());
        assertEquals("Listed 2 equipment items.", equipment.getFeedbackToUser());
        assertEquals(DisplayRequest.equipment(), equipment.getDisplayRequest());
        assertFalse(members.isExit());
        assertFalse(members.isShowHelp());
        assertFalse(equipment.isExit());
        assertFalse(equipment.isShowHelp());
        assertEquals(before, model.getAddressBook());
        assertEquals(List.of(later, first), model.getMemberList());
        assertEquals(List.of(second, one), model.getEquipmentList());
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new MemberListCommand().execute(null));
        assertThrows(NullPointerException.class, () -> new EquipmentListCommand().execute(null));
    }
}
