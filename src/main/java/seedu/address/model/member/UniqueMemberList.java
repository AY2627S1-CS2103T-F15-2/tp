package seedu.address.model.member;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.List;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.member.exceptions.DuplicateMemberException;
import seedu.address.model.member.exceptions.MemberNotFoundException;

/**
 * Contract for the identity-unique collection of Laplace members.
 */
public class UniqueMemberList {

    private final ObservableList<Member> internalList = FXCollections.observableArrayList();
    private final ObservableList<Member> internalUnmodifiableList =
            FXCollections.unmodifiableObservableList(internalList);

    /** Returns whether a member with {@code nusId} exists. */
    public boolean contains(NusId nusId) {
        requireNonNull(nusId);
        return findByNusId(nusId).isPresent();
    }

    /** Returns the member with {@code nusId}, if one exists. */
    public Optional<Member> findByNusId(NusId nusId) {
        requireNonNull(nusId);
        return internalList.stream()
                .filter(member -> member.getNusId().equals(nusId))
                .findFirst();
    }

    /** Adds {@code member}. */
    public void add(Member member) {
        requireNonNull(member);
        if (contains(member.getNusId())) {
            throw new DuplicateMemberException();
        }
        internalList.add(member);
    }

    /** Removes the member with the same identity as {@code member}. */
    public void remove(Member member) {
        requireNonNull(member);
        Member memberToRemove = findByNusId(member.getNusId())
                .orElseThrow(MemberNotFoundException::new);
        internalList.remove(memberToRemove);
    }

    /** Replaces this collection with a validated copy of {@code members}. */
    public void setMembers(List<Member> members) {
        requireAllNonNull(members);
        if (!membersAreUnique(members)) {
            throw new DuplicateMemberException();
        }
        internalList.setAll(members);
    }

    /** Returns an unmodifiable observable view of this collection. */
    public ObservableList<Member> asUnmodifiableObservableList() {
        return internalUnmodifiableList;
    }

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
