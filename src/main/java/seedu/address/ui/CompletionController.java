package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Logger;

import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.completion.Completion;
import seedu.address.logic.completion.CompletionService;
import seedu.address.model.ReadOnlyAddressBook;

/**
 * Owns one input field's Tab cycle, invalidating record candidates without copying the model.
 */
final class CompletionController {
    private static final Logger logger = LogsCenter.getLogger(CompletionController.class);

    private final TextField field;
    private final Supplier<ReadOnlyAddressBook> data;
    private final CompletionService service = new CompletionService();
    private Completion completion;
    private final InvalidationListener recordChanged = unused -> invalidateRecordCandidates();
    private final WeakInvalidationListener weakRecordChanged = new WeakInvalidationListener(recordChanged);
    private ReadOnlyAddressBook observedData;
    private boolean usesRecordData;
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
        if (completion != null && usesRecordData && observedData != data.get()) {
            logger.fine("Restarting completion cycle because the data changed.");
            completion = null;
        }
        if (completion == null) {
            if (event.isShiftDown()) {
                return;
            }
            originalText = field.getText();
            int caret = field.getCaretPosition();
            usesRecordData = false;
            Optional<Completion> suggestion = service.completeWithDataSource(originalText, caret, this::readData);
            if (suggestion.isEmpty()) {
                return;
            }
            completion = suggestion.orElseThrow();
            candidateIndex = 0;
            logger.fine("Started completion cycle with " + completion.candidates().size()
                    + " candidate(s).");
        } else {
            int direction = event.isShiftDown() ? -1 : 1;
            candidateIndex = Math.floorMod(candidateIndex + direction, completion.candidates().size());
            logger.fine("Cycled to completion candidate " + (candidateIndex + 1) + " of "
                    + completion.candidates().size() + ".");
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

    /**
     * Observes value-source collections only when a record-dependent completion needs them.
     * Weak listeners avoid retaining discarded fields through the live model.
     */
    private ReadOnlyAddressBook readData() {
        ReadOnlyAddressBook current = requireNonNull(data.get());
        usesRecordData = true;
        if (current != observedData) {
            if (observedData != null) {
                observedData.getMemberList().removeListener(weakRecordChanged);
                observedData.getEquipmentList().removeListener(weakRecordChanged);
            }
            observedData = current;
            observedData.getMemberList().addListener(weakRecordChanged);
            observedData.getEquipmentList().addListener(weakRecordChanged);
        }
        return current;
    }

    /**
     * Leaves static vocabulary cycles independent of unrelated record notifications.
     */
    private void invalidateRecordCandidates() {
        if (usesRecordData && completion != null) {
            logger.fine("Invalidated completion cycle because member or equipment records changed.");
            completion = null;
        }
    }

    /** Text/caret changes caused by our own insertion belong to the same cycle. */
    private void resetAfterEdit() {
        if (!isApplying && completion != null) {
            logger.fine("Ended completion cycle after the input changed.");
            completion = null;
        }
    }
}
