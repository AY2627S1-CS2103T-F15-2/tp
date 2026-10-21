package seedu.address.ui;

import static java.util.Objects.requireNonNull;
import static seedu.address.ui.DetailsPanelContent.field;
import static seedu.address.ui.DetailsPanelContent.status;
import static seedu.address.ui.DetailsPanelContent.text;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.equipment.Availability;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.equipment.exceptions.EquipmentNotFoundException;
import seedu.address.model.loan.Loan;
import seedu.address.model.member.Member;

/** Read-only equipment details. The shell owns selection, navigation and refresh listeners. */
public class EquipmentDetailsPanel extends UiPart<Region> {
    private final ReadOnlyAddressBook data;
    private final Function<UUID, Availability> availabilityLookup;
    private UUID selectedUuid;

    @FXML
    private VBox content;

    /** Creates an initially unselected panel from non-null read-only dependencies. */
    public EquipmentDetailsPanel(ReadOnlyAddressBook data, Function<UUID, Availability> availabilityLookup) {
        super("EquipmentDetailsPanel.fxml");
        this.data = requireNonNull(data);
        this.availabilityLookup = requireNonNull(availabilityLookup);
        refresh();
    }

    /** Selects an identity and resolves its current data. */
    public void showEquipment(UUID uuid) {
        requireNonNull(uuid);
        boolean identityChanged = !uuid.equals(selectedUuid);
        selectedUuid = uuid;
        refresh();
        if (identityChanged) {
            ScrollPane scroll = (ScrollPane) getRoot();
            scroll.setVvalue(0);
        }
    }

    /** Rebuilds fields, clearing stale holder/loan details after return, reset or removal. */
    public void refresh() {
        content.getChildren().clear();
        text(content, "detailsTitle", "Equipment details", "laplace-title");
        if (selectedUuid == null) {
            status(content, "Select equipment to view its details.", false);
            return;
        }
        Optional<Equipment> selected = data.getEquipmentList().stream()
                .filter(item -> item.getUuid().equals(selectedUuid)).findFirst();
        if (selected.isEmpty()) {
            status(content, "Equipment no longer exists: " + selectedUuid, true);
            return;
        }
        Equipment item = selected.orElseThrow();
        if (item.isDeleted()) {
            status(content, "Deleted equipment. Restore at any time with equipment restore uuid/" + selectedUuid + ".",
                    false);
        }
        text(content, "equipmentName", item.getName(), "details-name");
        field(content, "equipmentUuid", "UUID", item.getUuid());
        field(content, "equipmentStatus", "Status", item.isDeleted() ? "DELETED" : "ACTIVE");
        field(content, "category", "Category", item.getCategory());
        field(content, "condition", "Condition", item.getCondition());
        Availability availability;
        try {
            availability = requireNonNull(availabilityLookup.apply(selectedUuid));
        } catch (EquipmentNotFoundException e) {
            availability = null;
        }
        field(content, "availability", "Availability", availability == null ? "Unknown" : availability);
        field(content, "notes", "Notes", item.getNotes().isEmpty() ? "No notes" : item.getNotes());
        text(content, "holderTitle", "Current holder", "details-section-title");
        Optional<Loan> open = data.getLoanList().stream()
                .filter(loan -> loan.isOpen() && loan.getEquipmentUuid().equals(selectedUuid)).findFirst();
        if (open.isEmpty()) {
            if (availability == Availability.ASSIGNED) {
                status(content, "Inconsistent data: assigned equipment has no open loan.", true);
            } else {
                text(content, "holder", "No current holder", "laplace-muted");
            }
        } else {
            Loan loan = open.orElseThrow();
            Optional<Member> holder = data.getMemberList().stream()
                    .filter(member -> member.getNusId().equals(loan.getMemberNusId())).findFirst();
            if (holder.isPresent()) {
                Member member = holder.orElseThrow();
                field(content, "holder", "Name", member.getName());
                field(content, "holderNusId", "NUS ID", member.getNusId());
                field(content, "holderPhone", "Phone", member.getPhone());
                field(content, "holderEmail", "Email", member.getEmail());
            } else {
                status(content, "Inconsistent data: missing holder reference " + loan.getMemberNusId() + ".", true);
            }
            field(content, "assignedDate", "Assigned", loan.getAssignedDate());
            field(content, "expectedReturnDate", "Expected return", loan.getExpectedReturnDate());
            if (availability != null && availability != Availability.ASSIGNED) {
                status(content, "Inconsistent data: open loan conflicts with equipment availability.", true);
            }
        }
        if (availability == null) {
            status(content, "Inconsistent data: availability could not be determined for " + selectedUuid + ".", true);
        }
        LoanHistoryContent.add(content, data.getLoanList().stream()
                .filter(loan -> loan.getEquipmentUuid().equals(selectedUuid)).toList());
    }
}
