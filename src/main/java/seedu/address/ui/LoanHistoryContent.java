package seedu.address.ui;

import java.util.Comparator;
import java.util.List;

import javafx.scene.layout.VBox;
import seedu.address.model.loan.Loan;

/** Renders a read-only loan timeline inside an existing details panel. */
final class LoanHistoryContent {
    private LoanHistoryContent() {
    }

    /** Adds current and completed loan facts for the selected identity. */
    static void add(VBox content, List<Loan> loans) {
        DetailsPanelContent.text(content, "loanHistoryTitle", "Loan history", "details-section-title");
        if (loans.isEmpty()) {
            DetailsPanelContent.status(content, "No loan history for this record.", false);
            return;
        }
        VBox rows = new VBox(10);
        rows.setId("loanHistory");
        content.getChildren().add(rows);
        loans.stream().sorted(Comparator.comparing(Loan::getAssignedDate).reversed()
                .thenComparing(loan -> loan.getEquipmentUuid().toString())).forEach(loan -> {
                    VBox row = new VBox(4);
                    row.getStyleClass().add("details-holding");
                    DetailsPanelContent.field(row, "historyEquipmentUuid", "Equipment UUID", loan.getEquipmentUuid());
                    DetailsPanelContent.field(row, "historyMemberNusId", "NUS ID", loan.getMemberNusId());
                    DetailsPanelContent.field(row, "historyAssigned", "Assigned", loan.getAssignedDate());
                    DetailsPanelContent.field(row, "historyExpected", "Expected return", loan.getExpectedReturnDate());
                    DetailsPanelContent.field(row, "historyIssueCondition", "Issue condition",
                            loan.getIssueCondition().map(Enum::name).orElse("Not recorded"));
                    DetailsPanelContent.field(row, "historyReturned", "Returned",
                            loan.getReturnedDate().map(Object::toString).orElse("Open"));
                    if (!loan.isOpen()) {
                        DetailsPanelContent.field(row, "historyReturnCondition", "Return condition",
                                loan.getReturnCondition().map(Enum::name).orElse("Not recorded"));
                    }
                    rows.getChildren().add(row);
                });
    }
}
