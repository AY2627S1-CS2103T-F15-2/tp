package seedu.address.logic.commands;

import seedu.address.logic.transfer.DataTransferPreview;

/** Typed preview result consumed by the shell without giving it access to the candidate model. */
public class TransferPreviewResult extends CommandResult {
    private final DataTransferPreview preview;

    /** Creates feedback and a typed presentation of the validated replacement. */
    public TransferPreviewResult(DataTransferPreview preview) {
        super(preview.summary(), DisplayRequest.unchanged());
        this.preview = preview;
    }

    public DataTransferPreview getPreview() {
        return preview;
    }
}
