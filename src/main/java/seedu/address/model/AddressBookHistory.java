package seedu.address.model;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Immutable session history with at most ten reversible changes and eleven independent record snapshots.
 * Snapshot containers are private; callers receive copies and can safely share history between transactions.
 */
public final class AddressBookHistory {
    public static final int MAX_CHANGES = 10;

    private final List<AddressBook> states;
    private final List<String> descriptions;
    private final int position;

    /**
     * Creates empty history. The first successful change supplies the initial loaded or prepared state.
     */
    public AddressBookHistory() {
        this(List.of(), List.of(), 0);
    }

    private AddressBookHistory(List<AddressBook> states, List<String> descriptions, int position) {
        this.states = List.copyOf(states);
        this.descriptions = List.copyOf(descriptions);
        this.position = position;
    }

    /**
     * Records a changed state, discarding redo and then the oldest changes beyond the ten-step limit.
     * An unchanged state returns this history without discarding redo.
     */
    public AddressBookHistory record(ReadOnlyAddressBook previous, ReadOnlyAddressBook current, String description) {
        requireAllNonNull(previous, current, description);
        AddressBook baseline = new AddressBook(previous);
        AddressBook next = new AddressBook(current);
        if (next.equals(baseline)) {
            return this;
        }
        if (!states.isEmpty() && !states.get(position).equals(baseline)) {
            throw new IllegalStateException("History does not match the preceding state.");
        }
        List<AddressBook> retained = new ArrayList<>(states.isEmpty()
                ? List.of(baseline) : states.subList(0, position + 1));
        List<String> actions = new ArrayList<>(descriptions.subList(0, position));
        retained.add(next);
        actions.add(description);
        if (actions.size() > MAX_CHANGES) {
            retained.removeFirst();
            actions.removeFirst();
        }
        return new AddressBookHistory(retained, actions, retained.size() - 1);
    }

    public boolean canUndo() {
        return position > 0;
    }

    public boolean canRedo() {
        return position < states.size() - 1;
    }

    /**
     * Returns history positioned one step earlier, without mutating this history.
     */
    public AddressBookHistory undo() {
        requireUndo();
        return new AddressBookHistory(states, descriptions, position - 1);
    }

    /**
     * Returns history positioned one step later, without replaying a command or mutating this history.
     */
    public AddressBookHistory redo() {
        requireRedo();
        return new AddressBookHistory(states, descriptions, position + 1);
    }

    /**
     * Returns an independent copy of the selected state.
     */
    public ReadOnlyAddressBook getCurrentState() {
        if (states.isEmpty()) {
            throw new IllegalStateException("History has no recorded state.");
        }
        return new AddressBook(states.get(position));
    }

    public String getUndoDescription() {
        requireUndo();
        return descriptions.get(position - 1);
    }

    public String getRedoDescription() {
        requireRedo();
        return descriptions.get(position);
    }

    private void requireUndo() {
        if (!canUndo()) {
            throw new IllegalStateException("No undo is available.");
        }
    }

    private void requireRedo() {
        if (!canRedo()) {
            throw new IllegalStateException("No redo is available.");
        }
    }
}
