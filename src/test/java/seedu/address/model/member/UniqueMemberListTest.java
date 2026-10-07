package seedu.address.model.member;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalMembers.ALEX;
import static seedu.address.testutil.TypicalMembers.BEN;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.ObservableList;
import seedu.address.model.member.exceptions.DuplicateMemberException;
import seedu.address.model.member.exceptions.MemberNotFoundException;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;

public class UniqueMemberListTest {

    private final UniqueMemberList members = new UniqueMemberList();

    @Test
    public void contains_nullNusId_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> members.contains(null));
    }

    @Test
    public void contains_normalizedEquivalentNusId_returnsTrue() {
        members.add(ALEX);

        assertTrue(members.contains(new NusId("a0123456x")));
        assertFalse(members.contains(BEN.getNusId()));
    }

    @Test
    public void findByNusId_nullNusId_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> members.findByNusId(null));
    }

    @Test
    public void findByNusId_presentAndAbsentNusId_returnsExpectedResult() {
        members.add(ALEX);

        assertEquals(ALEX, members.findByNusId(new NusId("a0123456x")).orElseThrow());
        assertTrue(members.findByNusId(BEN.getNusId()).isEmpty());
    }

    @Test
    public void add_nullMember_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> members.add(null));
    }

    @Test
    public void add_sameNameWithDifferentNusId_addsBothMembers() {
        Member sameName = new Member(BEN.getNusId(), ALEX.getName(), BEN.getPhone(), BEN.getEmail());

        members.add(ALEX);
        members.add(sameName);

        assertEquals(List.of(ALEX, sameName), members.asUnmodifiableObservableList());
    }

    @Test
    public void add_sameNormalizedNusId_throwsDuplicateMemberExceptionAndDoesNotModifyList() {
        Member duplicateIdentity = memberWithAlexIdentityAndDifferentDetails();
        members.add(ALEX);

        assertThrows(DuplicateMemberException.class, () -> members.add(duplicateIdentity));
        assertEquals(List.of(ALEX), members.asUnmodifiableObservableList());
    }

    @Test
    public void remove_nullMember_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> members.remove(null));
    }

    @Test
    public void remove_memberWithSameIdentity_removesStoredMember() {
        members.add(ALEX);

        members.remove(memberWithAlexIdentityAndDifferentDetails());

        assertTrue(members.asUnmodifiableObservableList().isEmpty());
    }

    @Test
    public void remove_missingIdentity_throwsMemberNotFoundExceptionAndDoesNotModifyList() {
        members.add(ALEX);

        assertThrows(MemberNotFoundException.class, () -> members.remove(BEN));
        assertEquals(List.of(ALEX), members.asUnmodifiableObservableList());
    }

    @Test
    public void setMembers_nullListOrMember_throwsNullPointerExceptionAndDoesNotModifyList() {
        members.add(ALEX);
        List<Member> memberWithNull = new ArrayList<>();
        memberWithNull.add(BEN);
        memberWithNull.add(null);

        assertThrows(NullPointerException.class, () -> members.setMembers(null));
        assertThrows(NullPointerException.class, () -> members.setMembers(memberWithNull));
        assertEquals(List.of(ALEX), members.asUnmodifiableObservableList());
    }

    @Test
    public void setMembers_duplicateIdentities_throwsDuplicateMemberExceptionAndDoesNotModifyList() {
        members.add(BEN);

        assertThrows(DuplicateMemberException.class, () ->
                members.setMembers(List.of(ALEX, memberWithAlexIdentityAndDifferentDetails())));
        assertEquals(List.of(BEN), members.asUnmodifiableObservableList());
    }

    @Test
    public void setMembers_validList_copiesListAndReplacesMembers() {
        List<Member> replacement = new ArrayList<>(List.of(ALEX, BEN));

        members.setMembers(replacement);
        replacement.clear();

        assertEquals(List.of(ALEX, BEN), members.asUnmodifiableObservableList());
    }

    @Test
    public void asUnmodifiableObservableList_modification_throwsUnsupportedOperationException() {
        members.add(ALEX);
        ObservableList<Member> exposedList = members.asUnmodifiableObservableList();

        assertThrows(UnsupportedOperationException.class, () -> exposedList.add(BEN));
        assertEquals(List.of(ALEX), exposedList);
    }

    private Member memberWithAlexIdentityAndDifferentDetails() {
        return new Member(new NusId("a0123456x"), new Name("Different Name"),
                new Phone("81112222"), new Email("different@example.com"));
    }
}
