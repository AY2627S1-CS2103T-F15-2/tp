package seedu.address.logic.completion;

import java.util.List;

/** An immutable replacement range and the ordered alternatives for a single Tab completion. */
public record Completion(int start, int end, List<String> candidates) {
    public Completion {
        candidates = List.copyOf(candidates);
    }
}
