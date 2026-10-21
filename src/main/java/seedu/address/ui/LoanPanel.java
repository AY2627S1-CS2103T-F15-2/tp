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
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
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
    private final Clock clock;
    private final Consumer<String> onUse;
    private final Consumer<UUID> onEquipmentSelected;
    private final Consumer<NusId> onMemberSelected;

    @FXML
    private Label openCount;
    @FXML
    private VBox openLoans;

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
        refresh();
    }

    /**
     * Rebuilds current assignments from current data without invoking callbacks or installing listeners.
     */
    public void refresh() {
        List<Loan> open = data.getLoanList().stream().filter(Loan::isOpen).sorted(OPEN_ORDER).toList();
        openCount.setText(Integer.toString(open.size()));
        refreshSection(openLoans, open, "No open loans. Available equipment is ready to assign.");
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

        long overdueDays = ChronoUnit.DAYS.between(loan.getExpectedReturnDate(), LocalDate.now(clock));
        String statusText = overdueDays > 0 ? "Overdue · " + overdueDays + (overdueDays == 1 ? " day" : " days")
                : overdueDays == 0 ? "Due today" : "Open loan";
        Label status = label(statusText, "loan-status");
        status.getStyleClass().add(overdueDays > 0 ? "loan-overdue-status"
                : overdueDays == 0 ? "loan-due-status" : "loan-open-status");
        VBox equipmentColumn = new VBox(4, equipmentLink,
                label("Assigned: " + loan.getAssignedDate(), "loan-date"));
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
        Button prepareReturn = new Button("Prepare return command");
        prepareReturn.setOnAction(event -> onUse.accept("loans return uuid/" + loan.getEquipmentUuid()
                + " returned/" + LocalDate.now(clock)));
        VBox row = new VBox(6, status, identities, prepareReturn);
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
