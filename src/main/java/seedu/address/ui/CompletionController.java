package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.function.Supplier;

import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import seedu.address.logic.completion.Completion;
import seedu.address.logic.completion.CompletionService;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;

/** Owns one input field's Tab cycle; every key uses fresh read-only model data. */
final class CompletionController {
    private final TextField field;
    private final Supplier<ReadOnlyAddressBook> data;
    private final CompletionService service = new CompletionService();
    private Completion completion;
    /** A detached copy detects model changes even though the live root keeps its object identity. */
    private AddressBook snapshot;
    private String originalText;
    private int candidateIndex;
    /** Prevents our own text/caret notifications from canceling an active cycle. */
    private boolean isApplying;

    CompletionController(TextField field, Supplier<ReadOnlyAddressBook> data) {
        this.field = requireNonNull(field);
        this.data = requireNonNull(data);
        field.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKey);
        field.textProperty().addListener((unused, previous, current) -> resetAfterEdit());
        field.caretPositionProperty().addListener((unused, previous, current) -> resetAfterEdit());
        field.anchorProperty().addListener((unused, previous, current) -> resetAfterEdit());
        field.focusedProperty().addListener((unused, previous, current) -> resetAfterEdit());
    }

    /** Consumes only an unmodified Tab with candidates, or Shift+Tab within an active cycle. */
    private void handleKey(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            completion = null;
        }
        if (event.getCode() != KeyCode.TAB) {
            return;
        }
        if (event.isControlDown() || event.isAltDown() || event.isMetaDown() || field.getSelection().getLength() != 0) {
            resetAfterEdit();
            return;
        }
        AddressBook current = new AddressBook(data.get());
        if (completion != null && !snapshot.equals(current)) {
            completion = null;
        }
        if (completion == null) {
            if (event.isShiftDown()) {
                return;
            }
            originalText = field.getText();
            int caret = field.getCaretPosition();
            Optional<Completion> suggestion = service.complete(originalText, caret, current);
            if (suggestion.isEmpty()) {
                return;
            }
            completion = suggestion.orElseThrow();
            snapshot = current;
            candidateIndex = 0;
        } else {
            int direction = event.isShiftDown() ? -1 : 1;
            candidateIndex = Math.floorMod(candidateIndex + direction, completion.candidates().size());
        }
        String candidate = completion.candidates().get(candidateIndex);
        isApplying = true;
        try {
            field.setText(originalText.substring(0, completion.start()) + candidate
                    + originalText.substring(completion.end()));
            field.positionCaret(completion.start() + candidate.length());
        } finally {
            isApplying = false;
        }
        event.consume();
    }

    /** Text/caret changes caused by our own insertion belong to the same cycle. */
    private void resetAfterEdit() {
        if (!isApplying) {
            completion = null;
        }
    }
}
