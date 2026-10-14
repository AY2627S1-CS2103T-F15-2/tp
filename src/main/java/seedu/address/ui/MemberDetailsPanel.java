package seedu.address.ui;

import static java.util.Objects.requireNonNull;
import static seedu.address.ui.DetailsPanelContent.field;
import static seedu.address.ui.DetailsPanelContent.status;
import static seedu.address.ui.DetailsPanelContent.text;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import javafx.fxml.FXML;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.commons.util.LaplaceSortOrder;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.equipment.Availability;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.equipment.exceptions.EquipmentNotFoundException;
import seedu.address.model.loan.Loan;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;

/** Read-only member details and current holdings, resolved afresh on each refresh. */
public class MemberDetailsPanel extends UiPart<Region> {
    private final ReadOnlyAddressBook data;
    private final Function<UUID, Availability> availabilityLookup;
    private NusId selectedNusId;

    @FXML
    private VBox content;

    /** Creates an initially unselected panel from non-null read-only dependencies. */
    public MemberDetailsPanel(ReadOnlyAddressBook data, Function<UUID, Availability> availabilityLookup) {
        super("MemberDetailsPanel.fxml");
        this.data = requireNonNull(data);
        this.availabilityLookup = requireNonNull(availabilityLookup);
        refresh();
    }

    /** Selects an identity and resolves its current data. */
    public void showMember(NusId nusId) {
        selectedNusId = requireNonNull(nusId);
        refresh();
    }

    /** Refreshes contacts and holdings; no listeners, callbacks or writes are installed. */
    public void refresh() {
        content.getChildren().clear();
        text(content, "detailsTitle", "Member details", "laplace-title");
        if (selectedNusId == null) {
            status(content, "Select a member to view their details.", false);
            return;
        }
        Optional<Member> selected = data.getMemberList().stream()
                .filter(member -> member.getNusId().equals(selectedNusId)).findFirst();
        if (selected.isEmpty()) {
            status(content, "Member no longer exists: " + selectedNusId, true);
            return;
        }
        Member member = selected.orElseThrow();
        text(content, "memberName", member.getName().fullName, "details-name");
        field(content, "memberNusId", "NUS ID", member.getNusId());
        field(content, "phone", "Phone", member.getPhone());
        field(content, "email", "Email", member.getEmail());
        List<Loan> loans = data.getLoanList().stream()
                .filter(loan -> loan.isOpen() && loan.getMemberNusId().equals(selectedNusId)).toList();
        text(content, "holdingsTitle", "Current holdings", "details-section-title");
        text(content, "heldCount", loans.size() + " item(s) held", "laplace-count-badge")
                .setMaxWidth(Region.USE_PREF_SIZE);
        if (loans.isEmpty()) {
            status(content, "No equipment currently held.", false);
            return;
        }
        VBox holdings = new VBox(12);
        holdings.setId("holdings");
        holdings.setMinWidth(0);
        content.getChildren().add(holdings);
        data.getEquipmentList().stream()
                .filter(item -> loans.stream().anyMatch(loan -> loan.getEquipmentUuid().equals(item.getUuid())))
                .sorted(LaplaceSortOrder.EQUIPMENT)
                .forEach(item -> addHolding(holdings, item, loans.stream()
                        .filter(loan -> loan.getEquipmentUuid().equals(item.getUuid())).findFirst().orElseThrow()));
        loans.stream().filter(loan -> data.getEquipmentList().stream()
                .noneMatch(item -> item.getUuid().equals(loan.getEquipmentUuid())))
                .sorted((first, second) -> first.getEquipmentUuid().toString()
                        .compareTo(second.getEquipmentUuid().toString()))
                .forEach(loan -> {
                    VBox missing = new VBox(6);
                    missing.getStyleClass().add("details-holding");
                    status(missing, "Inconsistent data: missing equipment reference " + loan.getEquipmentUuid() + ".",
                            true);
                    field(missing, "assignedDate", "Assigned", loan.getAssignedDate());
                    field(missing, "expectedReturnDate", "Expected return", loan.getExpectedReturnDate());
                    holdings.getChildren().add(missing);
                });
    }

    private void addHolding(VBox holdings, Equipment item, Loan loan) {
        VBox row = new VBox(6);
        row.setMinWidth(0);
        row.getStyleClass().add("details-holding");
        text(row, "equipmentName", item.getName(), "details-section-title");
        field(row, "equipmentUuid", "UUID", item.getUuid());
        field(row, "category", "Category", item.getCategory());
        field(row, "condition", "Condition", item.getCondition());
        try {
            Availability availability = requireNonNull(availabilityLookup.apply(item.getUuid()));
            field(row, "availability", "Availability", availability);
            if (availability != Availability.ASSIGNED) {
                status(row, "Inconsistent data: current holding is not marked assigned.", true);
            }
        } catch (EquipmentNotFoundException e) {
            status(row, "Inconsistent data: availability could not be determined.", true);
        }
        field(row, "assignedDate", "Assigned", loan.getAssignedDate());
        field(row, "expectedReturnDate", "Expected return", loan.getExpectedReturnDate());
        holdings.getChildren().add(row);
    }
}
