package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.equipment.Availability;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.query.DirectoryQuery;
import seedu.address.model.query.InventoryGroups;

/**
 * Groups equipment for browsing while preserving physical-unit identity for explicit navigation.
 */
public class EquipmentListPanel extends UiPart<Region> {
    private final ReadOnlyAddressBook data;
    private final Consumer<InventoryGroups.Key> onSelect;
    private final Function<UUID, Availability> availabilityLookup;
    private DirectoryQuery query = DirectoryQuery.all();
    private UUID selectedUuid;

    @FXML
    private Label countLabel;
    @FXML
    private ListView<InventoryGroups.Group> rows;

    /**
     * Creates a grouped directory; only explicit activation invokes the navigation callback.
     */
    public EquipmentListPanel(ReadOnlyAddressBook data, Function<UUID, Availability> availabilityLookup,
            Consumer<InventoryGroups.Key> onSelect) {
        super("EquipmentListPanel.fxml");
        this.data = requireNonNull(data);
        this.availabilityLookup = requireNonNull(availabilityLookup);
        this.onSelect = requireNonNull(onSelect);
        rows.setCellFactory(unused -> new DirectoryCell());
        rows.getSelectionModel().selectedItemProperty().addListener((unused, previous, current) -> selectedUuid = null);
        rows.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                getSelectedGroup().ifPresent(this.onSelect);
                event.consume();
            }
        });
        refresh();
    }

    public void setQuery(DirectoryQuery query) {
        this.query = requireNonNull(query);
    }

    public DirectoryQuery getQuery() {
        return query;
    }

    /**
     * Returns individual query matches for identity recovery, independently of grouped rows.
     */
    public List<Equipment> getDisplayedItems() {
        return query.equipment(data, availabilityLookup);
    }

    /**
     * Rebuilds derived groups while retaining group selection and any explicitly selected unit.
     */
    public void refresh() {
        UUID unit = selectedUuid;
        Optional<InventoryGroups.Key> selected = getSelectedGroup();
        List<Equipment> matching = getDisplayedItems();
        List<InventoryGroups.Group> groups = InventoryGroups.group(data, matching);
        if (!rows.getItems().equals(groups)) {
            rows.getItems().setAll(groups);
        }
        rows.refresh();
        countLabel.setText(rows.getItems().size() + " groups · " + matching.size() + " matching units / "
                + data.getEquipmentList().size() + " retained");
        Label empty = new Label(data.getEquipmentList().isEmpty()
                ? "No equipment yet. Use equipment add to get started."
                : "No matching equipment. Reset search to show all retained units.");
        empty.setWrapText(true);
        rows.setPlaceholder(empty);
        selected.ifPresent(this::selectGroup);
        selectedUuid = unit;
    }

    public Optional<UUID> getSelection() {
        return Optional.ofNullable(selectedUuid);
    }

    public Optional<InventoryGroups.Key> getSelectedGroup() {
        return Optional.ofNullable(rows.getSelectionModel().getSelectedItem()).map(InventoryGroups.Group::key);
    }

    /**
     * Selects a group without choosing a unit or invoking a command.
     */
    public void selectGroup(InventoryGroups.Key key) {
        rows.getItems().stream().filter(group -> group.key().equals(key)).findFirst()
                .ifPresent(group -> rows.getSelectionModel().select(group));
    }

    /**
     * Retains the exact UUID even when its group is outside the current directory filter.
     */
    public void select(Optional<UUID> selected) {
        requireNonNull(selected);
        Optional<Equipment> unit = selected.flatMap(id -> data.getEquipmentList().stream()
                .filter(item -> item.getUuid().equals(id)).findFirst());
        Optional<InventoryGroups.Group> group = unit.flatMap(item -> rows.getItems().stream()
                .filter(row -> row.key().equals(InventoryGroups.keyOf(item))).findFirst());
        Optional<UUID> currentUuid = unit.map(Equipment::getUuid);
        // An unchanged UUID may now belong to a different group, or no visible group.
        if (getSelection().equals(currentUuid) && getSelectedGroup().equals(group.map(InventoryGroups.Group::key))) {
            return;
        }
        rows.getSelectionModel().clearSelection();
        group.ifPresent(row -> rows.getSelectionModel().select(row));
        selectedUuid = currentUuid.orElse(null);
    }

    /**
     * Describes the group and availability within the matching result scope.
     */
    private Region createCard(InventoryGroups.Group group) {
        Label name = new Label(group.name());
        name.getStyleClass().add("details-section-title");
        Label category = new Label(group.category());
        category.getStyleClass().add("laplace-muted");
        Label count = new Label(group.matching().size() + " matching / " + group.units().size() + " retained units");
        Label availability = new Label("In results: "
                + EquipmentPresentation.counts(group.matching(), availabilityLookup));
        VBox card = new VBox(5, name, category, count, availability);
        card.getStyleClass().addAll("laplace-row", "equipment-group-card");
        for (Label label : List.of(name, category, count, availability)) {
            label.setWrapText(true);
            label.setMinWidth(0);
        }
        return card;
    }

    private class DirectoryCell extends ListCell<InventoryGroups.Group> {
        DirectoryCell() {
            setPrefWidth(0);
            setOnMouseClicked(event -> {
                if (!isEmpty() && getItem() != null && event.getButton() == MouseButton.PRIMARY
                        && event.getClickCount() == 1) {
                    rows.getSelectionModel().select(getItem());
                    onSelect.accept(getItem().key());
                    event.consume();
                }
            });
        }

        @Override
        protected void updateItem(InventoryGroups.Group item, boolean empty) {
            super.updateItem(item, empty);
            setText(null);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                Region card = createCard(item);
                card.prefWidthProperty().bind(Bindings.max(0, widthProperty().subtract(2)));
                setGraphic(card);
            }
        }
    }
}
