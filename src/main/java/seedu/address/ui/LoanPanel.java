package seedu.address.ui;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.loan.Loan;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;

/**
 * Read-only loan directory. The application shell calls {@link #refresh()} after data changes.
 * Navigation callbacks are invoked only by the corresponding identity buttons.
 */
public class LoanPanel extends UiPart<Region> {
    static final Comparator<Loan> OPEN_ORDER = Comparator.comparing(Loan::getExpectedReturnDate)
            .thenComparing(loan -> loan.getEquipmentUuid().toString());
    private static final String FXML = "LoanPanel.fxml";

    private final ReadOnlyAddressBook data;
    private final Consumer<UUID> onEquipmentSelected;
    private final Consumer<NusId> onMemberSelected;

    @FXML
    private Label openCount;
    @FXML
    private VBox openLoans;
    @FXML
    private Label closedCount;
    @FXML
    private VBox closedLoans;
    @FXML
    private TextField memberFilter;
    @FXML
    private TextField equipmentFilter;

    /**
     * Creates a panel using current records and callbacks supplied by the application shell.
     */
    public LoanPanel(ReadOnlyAddressBook data, Consumer<UUID> onEquipmentSelected,
            Consumer<NusId> onMemberSelected) {
        super(FXML);
        requireAllNonNull(data, onEquipmentSelected, onMemberSelected);
        this.data = data;
        this.onEquipmentSelected = onEquipmentSelected;
        this.onMemberSelected = onMemberSelected;
        memberFilter.textProperty().addListener((observable, oldValue, newValue) -> refresh());
        equipmentFilter.textProperty().addListener((observable, oldValue, newValue) -> refresh());
        refresh();
    }

    /**
     * Rebuilds current assignments from current data without invoking callbacks or installing listeners.
     */
    public void refresh() {
        List<Loan> open = data.getLoanList().stream().filter(Loan::isOpen).filter(this::matchesFilters)
                .sorted(OPEN_ORDER).toList();
        List<Loan> closed = data.getLoanList().stream().filter(loan -> !loan.isOpen())
                .filter(this::matchesFilters).sorted(Comparator
                        .comparing((Loan loan) -> loan.getReturnedDate().orElseThrow()).reversed()
                        .thenComparing(Loan::getAssignedDate).thenComparing(loan -> loan.getEquipmentUuid().toString()))
                .toList();
        openCount.setText(Integer.toString(open.size()));
        refreshSection(openLoans, open, "No open loans. Available equipment is ready to assign.");
        closedCount.setText(Integer.toString(closed.size()));
        refreshSection(closedLoans, closed, "No completed loans match these filters.");
    }

    private boolean matchesFilters(Loan loan) {
        return loan.getMemberNusId().toString().toLowerCase(java.util.Locale.ROOT)
                .contains(memberFilter.getText().strip().toLowerCase(java.util.Locale.ROOT))
                && loan.getEquipmentUuid().toString().toLowerCase(java.util.Locale.ROOT)
                .contains(equipmentFilter.getText().strip().toLowerCase(java.util.Locale.ROOT));
    }

    private void refreshSection(VBox section, List<Loan> loans, String emptyMessage) {
        section.getChildren().clear();
        if (loans.isEmpty()) {
            section.getChildren().add(label(emptyMessage, "loan-empty"));
        } else {
            loans.forEach(loan -> section.getChildren().add(createLoanRow(loan)));
        }
    }

    private VBox createLoanRow(Loan loan) {
        Optional<Equipment> equipment = data.getEquipmentList().stream()
                .filter(item -> item.getUuid().equals(loan.getEquipmentUuid())).findFirst();
        Optional<Member> member = data.getMemberList().stream()
                .filter(item -> item.getNusId().equals(loan.getMemberNusId())).findFirst();
        String equipmentName = equipment.map(Equipment::getName).orElse("Missing equipment reference");
        String memberName = member.map(item -> item.getName().fullName).orElse("Missing member reference");

        Button equipmentLink = identityButton(equipmentName + "\nUUID: " + loan.getEquipmentUuid(),
                "loan-equipment-link");
        equipmentLink.setDisable(equipment.isEmpty());
        equipmentLink.setOnAction(event -> onEquipmentSelected.accept(loan.getEquipmentUuid()));
        Button memberLink = identityButton(memberName + "\nNUS ID: " + loan.getMemberNusId(),
                "loan-member-link");
        memberLink.setDisable(member.isEmpty());
        memberLink.setOnAction(event -> onMemberSelected.accept(loan.getMemberNusId()));

        Label status = label(loan.isOpen() ? "Open loan" : "Completed loan", "loan-status");
        status.getStyleClass().add(loan.isOpen() ? "loan-open-status" : "loan-closed-status");
        VBox row = new VBox(6, status, equipmentLink, memberLink,
                label("Assigned: " + loan.getAssignedDate(), "loan-date"),
                label("Expected return: " + loan.getExpectedReturnDate(), "loan-date"),
                label("Issue condition: " + loan.getIssueCondition().map(Enum::name).orElse("Not recorded"),
                        "loan-date"));
        if (!loan.isOpen()) {
            row.getChildren().add(label("Returned: " + loan.getReturnedDate().orElseThrow(), "loan-date"));
            row.getChildren().add(label("Return condition: "
                    + loan.getReturnCondition().map(Enum::name).orElse("Not recorded"), "loan-date"));
        }
        row.getStyleClass().addAll("laplace-row", "loan-row");
        row.setMinWidth(0);
        row.setMaxWidth(Double.MAX_VALUE);
        return row;
    }

    private static Button identityButton(String text, String styleClass) {
        Button button = new Button(requireNonNull(text));
        button.getStyleClass().addAll("loan-identity-link", styleClass);
        button.setWrapText(true);
        button.setMinWidth(0);
        button.setMaxWidth(Double.MAX_VALUE);
        return button;
    }

    private static Label label(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        label.setWrapText(true);
        label.setMinWidth(0);
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }
}
