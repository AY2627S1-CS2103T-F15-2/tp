package seedu.address.model.member;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalMembers.ALEX;
import static seedu.address.testutil.TypicalMembers.BEN;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public class UniqueMemberListInvariantTest {
    @Test
    public void setDeleted_lookupReturnsUnstoredMember_throwsAssertionErrorBeforeMutation() {
        assertTrue(UniqueMemberList.class.desiredAssertionStatus());
        UniqueMemberList members = new UniqueMemberList() {
            @Override
            public Optional<Member> findByNusId(NusId nusId) {
                return Optional.of(BEN);
            }
        };

        AssertionError error = assertThrows(AssertionError.class, () -> members.setDeleted(ALEX.getNusId(), true));

        assertEquals("A resolved member must belong to this collection.", error.getMessage());
        assertEquals(List.of(), members.asUnmodifiableObservableList());
    }
}
