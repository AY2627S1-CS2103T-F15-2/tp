package seedu.address.model.member;

import java.util.List;
import java.util.Optional;

import javafx.collections.ObservableList;

/**
 * Contract for the identity-unique collection of Laplace members.
 */
public class UniqueMemberList {

    public boolean contains(NusId nusId) {
        throw new UnsupportedOperationException("UniqueMemberList is not implemented");
    }

    public Optional<Member> findByNusId(NusId nusId) {
        throw new UnsupportedOperationException("UniqueMemberList is not implemented");
    }

    public void add(Member member) {
        throw new UnsupportedOperationException("UniqueMemberList is not implemented");
    }

    public void remove(Member member) {
        throw new UnsupportedOperationException("UniqueMemberList is not implemented");
    }

    public void setMembers(List<Member> members) {
        throw new UnsupportedOperationException("UniqueMemberList is not implemented");
    }

    public ObservableList<Member> asUnmodifiableObservableList() {
        throw new UnsupportedOperationException("UniqueMemberList is not implemented");
    }
}
