package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.EnumMap;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.equipment.Availability;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.equipment.exceptions.EquipmentNotFoundException;
import seedu.address.model.query.DirectoryQuery;
import seedu.address.model.query.InventoryGroups;

/** Read-only equipment directory summary with freshly derived availability on every refresh. */
public class EquipmentDirectorySummary extends UiPart<Region> {

    private final ReadOnlyAddressBook data;
    private final Function<UUID, Availability> availabilityLookup;
    private DirectoryQuery query = DirectoryQuery.all();

    @FXML
    private Label sortOrder;
    @FXML
    private Label criteria;
    @FXML
    private Label inventoryGroups;

    @FXML
    private Label countBadge;
    @FXML
    private Label totalCount;
    @FXML
    private Label displayedCount;
    @FXML
    private Label availableCount;
    @FXML
    private Label assignedCount;
    @FXML
    private Label unavailableCount;
    @FXML
    private Label selection;
    @FXML
    private VBox status;
    @FXML
    private Label statusTitle;
    @FXML
    private Label statusMessage;

    /** Creates a standalone summary from non-null read-only dependencies. */
    public EquipmentDirectorySummary(ReadOnlyAddressBook data, Function<UUID, Availability> availabilityLookup) {
        super("EquipmentDirectorySummary.fxml");
        this.data = requireNonNull(data);
        this.availabilityLookup = requireNonNull(availabilityLookup);
        countBadge.setMinWidth(Region.USE_PREF_SIZE);
        refresh(Optional.empty());
    }

    /** Sets transient criteria; the parent refreshes once after coordinating both directory components. */
    public void setQuery(DirectoryQuery query) {
        this.query = requireNonNull(query);
    }

    /** Refreshes counts, derived availability and selection; no collection writes or listeners are installed. */
    public void refresh(Optional<UUID> selected) {
        requireNonNull(selected);
        int count = data.getEquipmentList().size();
        EnumMap<Availability, Integer> counts = new EnumMap<>(Availability.class);
        for (Availability availability : Availability.values()) {
            counts.put(availability, 0);
        }
        int unknown = 0;
        for (Equipment equipment : data.getEquipmentList()) {
            if (equipment.isDeleted()) {
                continue;
            }
            try {
                Availability availability = requireNonNull(availabilityLookup.apply(equipment.getUuid()));
                counts.merge(availability, 1, Integer::sum);
            } catch (EquipmentNotFoundException e) {
                unknown++;
            }
        }
        countBadge.setText(count + " ITEMS");
        totalCount.setText(Integer.toString(count));
        int displayed = query.equipment(data, availabilityLookup).size();
        displayedCount.setText(Integer.toString(displayed));
        sortOrder.setText(query.sortDescription(false));
        criteria.setText(query.description());
        inventoryGroups.setText(unknown == 0 ? String.join("\n",
                InventoryGroups.summarize(data, availabilityLookup, item -> true))
                : "Inventory groups unavailable: inconsistent equipment data.");
        availableCount.setText(Integer.toString(counts.get(Availability.AVAILABLE)));
        assignedCount.setText(Integer.toString(counts.get(Availability.ASSIGNED)));
        unavailableCount.setText(Integer.toString(counts.get(Availability.UNAVAILABLE)));
        Optional<Equipment> item = selected.flatMap(uuid -> data.getEquipmentList().stream()
                .filter(candidate -> candidate.getUuid().equals(uuid)).findFirst());
        selection.setText(selected.isEmpty() ? "No equipment selected"
                : item.map(value -> value.getName() + " - " + value.getUuid())
                        .orElse("Equipment no longer exists: " + selected.orElseThrow()));
        status.setVisible(unknown > 0 || count == 0);
        status.setManaged(status.isVisible());
        status.getStyleClass().removeAll("laplace-success", "laplace-error");
        status.getStyleClass().add(unknown == 0 ? "laplace-success" : "laplace-error");
        statusTitle.setText(unknown > 0 ? "Inconsistent equipment data"
                : count == 0 ? "No equipment yet" : "All equipment displayed");
        if (unknown == 0 && displayed != count) {
            statusTitle.setText("Showing " + displayed + " matching equipment items");
        }
        statusMessage.setText(unknown > 0 ? "Availability could not be determined for " + unknown + " item(s)."
                : count == 0 ? "Add an item with equipment add name/NAME category/CATEGORY condition/CONDITION."
                : "Availability counts and inventory groups include active units only; total includes deleted units.");
    }
}
