package seedu.address.model.member;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.List;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.member.exceptions.DuplicateMemberException;
import seedu.address.model.member.exceptions.MemberDeletedException;
import seedu.address.model.member.exceptions.MemberNotFoundException;

/**
 * Contract for the identity-unique collection of Laplace members.
 */
public class UniqueMemberList {

    private final ObservableList<Member> internalList = FXCollections.observableArrayList();
    private final ObservableList<Member> internalUnmodifiableList =
            FXCollections.unmodifiableObservableList(internalList);

    /**
     * Returns whether a member with {@code nusId} exists.
     */
    public boolean contains(NusId nusId) {
        requireNonNull(nusId);
        return findByNusId(nusId).isPresent();
    }

    /**
     * Returns the member with {@code nusId}, if one exists.
     */
    public Optional<Member> findByNusId(NusId nusId) {
        requireNonNull(nusId);
        return internalList.stream()
                .filter(member -> member.getNusId().equals(nusId))
                .findFirst();
    }

    /**
     * Adds {@code member}, rejecting an identity already present in this collection.
     */
    public void add(Member member) {
        requireNonNull(member);
        if (contains(member.getNusId())) {
            throw new DuplicateMemberException();
        }
        internalList.add(member);
    }

    /**
     * Replaces only deletion state, retaining identity, details and collection position.
     */
    public void setDeleted(NusId nusId, boolean isDeleted) {
        requireNonNull(nusId);
        Member stored = findByNusId(nusId).orElseThrow(MemberNotFoundException::new);
        Member replacement = new Member(stored.getNusId(), stored.getName(), stored.getPhone(),
                stored.getEmail(), isDeleted);
        internalList.set(indexOfStoredMember(stored), replacement);
    }

    /**
     * Replaces the stored identity's details without changing its NUS ID or list position.
     * The target is resolved by identity, so an older snapshot of its details is accepted.
     * Deleted members must be restored before editing; replacements retain deletion state.
     */
    public void setMember(Member target, Member editedMember) {
        requireAllNonNull(target, editedMember);
        checkArgument(target.isSameMember(editedMember), "A member's NUS ID cannot be changed.");
        Member stored = findByNusId(target.getNusId()).orElseThrow(MemberNotFoundException::new);
        if (stored.isDeleted()) {
            throw new MemberDeletedException();
        }
        checkArgument(stored.isDeleted() == editedMember.isDeleted(),
                "Editing a member cannot change their deletion state.");
        internalList.set(indexOfStoredMember(stored), editedMember);
    }

    /**
     * Removes the member with the same identity as {@code member}.
     */
    public void remove(Member member) {
        requireNonNull(member);
        Member memberToRemove = findByNusId(member.getNusId())
                .orElseThrow(MemberNotFoundException::new);
        internalList.remove(memberToRemove);
    }

    /**
     * Replaces this collection with a validated snapshot, including when the input is a view of this collection.
     * Null elements and duplicate identities are rejected before the current contents change.
     */
    public void setMembers(List<Member> members) {
        List<Member> replacement = List.copyOf(requireNonNull(members));
        if (!membersAreUnique(replacement)) {
            throw new DuplicateMemberException();
        }
        internalList.setAll(replacement);
    }

    /**
     * Returns an unmodifiable observable view of this collection.
     */
    public ObservableList<Member> asUnmodifiableObservableList() {
        return internalUnmodifiableList;
    }

    /**
     * Returns the position of a member already resolved from this collection.
     */
    private int indexOfStoredMember(Member storedMember) {
        int index = internalList.indexOf(storedMember);
        assert index >= 0 : "A resolved member must belong to this collection.";
        return index;
    }

    /**
     * Returns whether the validated, non-null members have distinct NUS IDs.
     */
    private boolean membersAreUnique(List<Member> members) {
        for (int i = 0; i < members.size() - 1; i++) {
            NusId nusId = members.get(i).getNusId();
            for (int j = i + 1; j < members.size(); j++) {
                if (nusId.equals(members.get(j).getNusId())) {
                    return false;
                }
            }
        }
        return true;
    }
}
