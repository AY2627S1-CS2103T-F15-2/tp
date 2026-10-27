package seedu.address.ui;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.loan.Loan;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.model.query.LoanQuery;

/**
 * Read-only loan directory. The application shell calls {@link #refresh()} after data changes.
 * Navigation callbacks are invoked only by the corresponding identity buttons.
 */
public class LoanPanel extends UiPart<Region> {
    static final Comparator<Loan> OPEN_ORDER = Comparator.comparing(Loan::getExpectedReturnDate)
            .thenComparing(loan -> loan.getEquipmentUuid().toString());
    private static final String FXML = "LoanPanel.fxml";

    private final ReadOnlyAddressBook data;
    private final Clock clock;
    private final Consumer<String> onUse;
    private final Consumer<UUID> onEquipmentSelected;
    private final Consumer<NusId> onMemberSelected;
    private boolean isApplyingQuery;

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
    @FXML
    private Label filterSummary;
    @FXML
    private Button resetFilters;

    /**
     * Creates a panel using current records and callbacks supplied by the application shell.
     */
    public LoanPanel(ReadOnlyAddressBook data, Consumer<UUID> onEquipmentSelected,
            Consumer<NusId> onMemberSelected) {
        this(data, onEquipmentSelected, onMemberSelected, unused -> { }, Clock.systemDefaultZone());
    }

    /**
     * Creates a panel with command preparation and a clock for date-sensitive presentation.
     */
    public LoanPanel(ReadOnlyAddressBook data, Consumer<UUID> onEquipmentSelected,
            Consumer<NusId> onMemberSelected, Consumer<String> onUse, Clock clock) {
        super(FXML);
        requireAllNonNull(data, onEquipmentSelected, onMemberSelected, onUse, clock);
        this.clock = clock;
        this.onUse = onUse;
        this.data = data;
        this.onEquipmentSelected = onEquipmentSelected;
        this.onMemberSelected = onMemberSelected;
        memberFilter.textProperty().addListener((observable, oldValue, newValue) -> refreshAfterFilterEdit());
        equipmentFilter.textProperty().addListener((observable, oldValue, newValue) -> refreshAfterFilterEdit());
        refresh();
    }

    /**
     * Rebuilds current and completed loans without invoking callbacks or installing listeners.
     */
    public void refresh() {
        LoanQuery query = getQuery();
        List<Loan> open = data.getLoanList().stream().filter(Loan::isOpen).filter(query::matches)
                .sorted(OPEN_ORDER).toList();
        List<Loan> closed = data.getLoanList().stream().filter(loan -> !loan.isOpen())
                .filter(query::matches).sorted(Comparator
                        .comparing((Loan loan) -> loan.getReturnedDate().orElseThrow()).reversed()
                        .thenComparing(Loan::getAssignedDate).thenComparing(loan -> loan.getEquipmentUuid().toString()))
                .toList();
        openCount.setText(Integer.toString(open.size()));
        boolean hasFilter = !query.equals(LoanQuery.all());
        long totalOpen = data.getLoanList().stream().filter(Loan::isOpen).count();
        long totalClosed = data.getLoanList().size() - totalOpen;
        filterSummary.setText("Showing " + open.size() + " of " + totalOpen + " open loans; "
                + closed.size() + " of " + totalClosed + " completed loans. "
                + (hasFilter ? "Filters are active; reset to reveal hidden records." : "No filters active."));
        resetFilters.setDisable(!hasFilter);
        String openEmptyMessage = hasFilter ? "No open loans match these filters."
                : "No open loans. Available equipment is ready to assign.";
        refreshSection(openLoans, open, openEmptyMessage);
        closedCount.setText(Integer.toString(closed.size()));
        refreshSection(closedLoans, closed, "No completed loans match these filters.");
    }

    /**
     * Reveals all current and completed records only when the user explicitly resets the filters.
     */
    @FXML
    private void handleResetFilters() {
        setQuery(LoanQuery.all());
    }

    public LoanQuery getQuery() {
        return new LoanQuery(memberFilter.getText(), equipmentFilter.getText());
    }

    /**
     * Applies both command criteria together without triggering navigation or command callbacks.
     */
    public void setQuery(LoanQuery query) {
        requireNonNull(query);
        isApplyingQuery = true;
        try {
            memberFilter.setText(query.memberText());
            equipmentFilter.setText(query.equipmentText());
        } finally {
            isApplyingQuery = false;
        }
        refresh();
    }

    /**
     * Refreshes direct GUI edits after any programmatic two-field update has completed.
     */
    private void refreshAfterFilterEdit() {
        if (!isApplyingQuery) {
            refresh();
        }
    }

    /**
     * Replaces displayed loans with the current snapshot or a useful empty state.
     */
    private void refreshSection(VBox section, List<Loan> loans, String emptyMessage) {
        section.getChildren().clear();
        if (loans.isEmpty()) {
            section.getChildren().add(label(emptyMessage, "loan-empty"));
        } else {
            loans.forEach(loan -> section.getChildren().add(createLoanRow(loan)));
        }
    }

    /**
     * Builds a responsive loan row with date-derived urgency and exact-identity actions.
     */
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

        Label status = createStatus(loan);
        VBox equipmentColumn = new VBox(4, equipmentLink,
                label("Assigned: " + loan.getAssignedDate(), "loan-date"),
                label("Issue condition: " + loan.getIssueCondition().map(Enum::name).orElse("Not recorded"),
                        "loan-date"));
        VBox memberColumn = new VBox(4, memberLink,
                label("Expected return: " + loan.getExpectedReturnDate(), "loan-date"));
        GridPane identities = new GridPane();
        identities.setHgap(16);
        identities.setVgap(6);
        identities.add(equipmentColumn, 0, 0);
        identities.add(memberColumn, 0, 1);
        identities.widthProperty().addListener((unused, previous, width) -> {
            boolean isWide = width.doubleValue() >= 650;
            identities.getColumnConstraints().clear();
            for (int i = 0; i < (isWide ? 2 : 1); i++) {
                ColumnConstraints column = new ColumnConstraints();
                column.setPercentWidth(isWide ? 50 : 100);
                column.setMinWidth(0);
                identities.getColumnConstraints().add(column);
            }
            GridPane.setColumnIndex(memberColumn, isWide ? 1 : 0);
            GridPane.setRowIndex(memberColumn, isWide ? 0 : 1);
        });
        VBox row = new VBox(6, status, identities);
        if (loan.isOpen()) {
            Button prepareReturn = new Button("Prepare return command");
            prepareReturn.setOnAction(event -> onUse.accept("loans return uuid/" + loan.getEquipmentUuid()
                    + " returned/" + LocalDate.now(clock)));
            row.getChildren().add(prepareReturn);
        } else {
            memberColumn.getChildren().addAll(
                    label("Returned: " + loan.getReturnedDate().orElseThrow(), "loan-date"),
                    label("Return condition: " + loan.getReturnCondition().map(Enum::name).orElse("Not recorded"),
                            "loan-date"));
        }
        row.getStyleClass().addAll("laplace-row", "loan-row");
        row.setMinWidth(0);
        row.setMaxWidth(Double.MAX_VALUE);
        return row;
    }

    /**
     * Shows urgency only for open loans and a completed state for returned loans.
     */
    private Label createStatus(Loan loan) {
        if (!loan.isOpen()) {
            Label status = label("Completed loan", "loan-status");
            status.getStyleClass().add("loan-closed-status");
            return status;
        }
        long overdueDays = ChronoUnit.DAYS.between(loan.getExpectedReturnDate(), LocalDate.now(clock));
        String statusText = overdueDays > 0 ? "Overdue · " + overdueDays + (overdueDays == 1 ? " day" : " days")
                : overdueDays == 0 ? "Due today" : "Open loan";
        Label status = label(statusText, "loan-status");
        status.getStyleClass().add(overdueDays > 0 ? "loan-overdue-status"
                : overdueDays == 0 ? "loan-due-status" : "loan-open-status");
        return status;
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
