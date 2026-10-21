package seedu.address.ui;

import static java.util.Objects.requireNonNull;
import static seedu.address.ui.DetailsPanelContent.field;
import static seedu.address.ui.DetailsPanelContent.status;
import static seedu.address.ui.DetailsPanelContent.text;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.commons.util.LaplaceText;
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
    private final Consumer<String> onUse;
    private final Consumer<UUID> onNavigate;
    private final Function<UUID, Availability> availabilityLookup;
    private NusId selectedNusId;

    @FXML
    private VBox content;

    /** Creates an initially unselected panel from non-null read-only dependencies. */
    public MemberDetailsPanel(ReadOnlyAddressBook data, Function<UUID, Availability> availabilityLookup) {
        this(data, availabilityLookup, unused -> { }, unused -> { });
    }

    /** Creates details with explicit command preparation and related-record navigation callbacks. */
    public MemberDetailsPanel(ReadOnlyAddressBook data, Function<UUID, Availability> availabilityLookup,
            Consumer<String> onUse, Consumer<UUID> onNavigate) {
        super("MemberDetailsPanel.fxml");
        this.data = requireNonNull(data);
        this.onUse = requireNonNull(onUse);
        this.onNavigate = requireNonNull(onNavigate);
        this.availabilityLookup = requireNonNull(availabilityLookup);
        refresh();
    }

    /** Selects an identity and resolves its current data. */
    public void showMember(NusId nusId) {
        requireNonNull(nusId);
        boolean identityChanged = !nusId.equals(selectedNusId);
        selectedNusId = nusId;
        refresh();
        if (identityChanged) {
            ScrollPane scroll = (ScrollPane) getRoot();
            scroll.setVvalue(0);
        }
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
        MemberNameStyle.apply(text(content, "memberName", member.getName().fullName, "details-name"), member);
        if (member.isDeleted()) {
            status(content, "Deleted member. Restore at any time with member restore nus/" + selectedNusId + ".",
                    false);
        }
        VBox commands = new VBox(8);
        String target = " nus/" + member.getNusId();
        if (member.isDeleted()) {
            commands.getChildren().add(new CommandPreview("member restore" + target, false, onUse));
        } else {
            commands.getChildren().addAll(new CommandPreview("member edit" + target + " name/NAME", true, onUse),
                    new CommandPreview("member delete" + target, false, onUse));
        }
        TitledPane actions = new TitledPane("Commands for this member", commands);
        actions.setExpanded(false);
        content.getChildren().add(actions);
        field(content, "memberNusId", "NUS ID", member.getNusId());
        field(content, "phone", "Phone", member.getPhone());
        field(content, "email", "Email", member.getEmail());
        List<Loan> loans = data.getLoanList().stream()
                .filter(loan -> loan.isOpen() && loan.getMemberNusId().equals(selectedNusId)).toList();
        List<UUID> missing = loans.stream().map(Loan::getEquipmentUuid)
                .filter(id -> data.getEquipmentList().stream().noneMatch(item -> item.getUuid().equals(id)))
                .sorted(Comparator.comparing(UUID::toString)).toList();
        if (!missing.isEmpty()) {
            status(content, "Member details could not be displayed because the stored loan data is inconsistent. "
                    + "Missing equipment references: " + missing + ". No incomplete holdings list is shown.", true);
            return;
        }
        text(content, "holdingsTitle", "Current holdings", "details-section-title");
        text(content, "heldCount", loans.size() + " item(s) held", "laplace-count-badge")
                .setMaxWidth(Region.USE_PREF_SIZE);
        if (loans.isEmpty()) {
            status(content, "This member currently holds no equipment.", false);
            return;
        }
        VBox holdings = new VBox(12);
        holdings.setId("holdings");
        holdings.setMinWidth(0);
        content.getChildren().add(holdings);
        loans.stream().sorted(Comparator.comparing(Loan::getExpectedReturnDate)
                .thenComparing(loan -> LaplaceText.normalize(holdingEquipment(loan).getName())
                        .toLowerCase(Locale.ROOT))
                .thenComparing(loan -> loan.getEquipmentUuid().toString()))
                .forEach(loan -> addHolding(holdings, holdingEquipment(loan), loan));
    }

    /** Resolves a prevalidated loan without detaching its dates from the corresponding physical item. */
    private Equipment holdingEquipment(Loan loan) {
        return data.getEquipmentList().stream().filter(item -> item.getUuid().equals(loan.getEquipmentUuid()))
                .findFirst().orElseThrow();
    }

    private void addHolding(VBox holdings, Equipment item, Loan loan) {
        VBox row = new VBox(6);
        row.setMinWidth(0);
        row.getStyleClass().add("details-holding");
        text(row, "equipmentName", item.getName(), "details-section-title");
        Button view = new Button("View equipment");
        view.setOnAction(event -> onNavigate.accept(item.getUuid()));
        row.getChildren().add(view);
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
