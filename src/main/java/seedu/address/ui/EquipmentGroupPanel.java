package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.equipment.Availability;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.equipment.exceptions.EquipmentNotFoundException;
import seedu.address.model.member.NusId;
import seedu.address.model.query.DirectoryQuery;
import seedu.address.model.query.InventoryGroups;

/**
 * A group overview and physical-unit table with exact-identity details and explicit filter scope.
 */
public class EquipmentGroupPanel extends VBox {
    private final ReadOnlyAddressBook data;
    private final Function<UUID, Availability> lookup;
    private final EquipmentDetailsPanel details;
    private final Region detailBody;
    private final Label title = new Label();
    private final Label counts = new Label();
    private final Label availabilityCounts = new Label();
    private final CheckBox showAll = new CheckBox("Show all units in this group, including deleted");
    private final TableView<Equipment> units = new TableView<>();
    private final StackPane detailArea = new StackPane();
    private final ScrollPane scroll = new ScrollPane();
    private final Label prompt = new Label("Select a physical unit to view details and prepare commands.");
    private InventoryGroups.Key key;
    private DirectoryQuery query = DirectoryQuery.all();
    private UUID selectedUuid;
    private boolean isRefreshing;

    /**
     * Creates a read-only group view; user unit selection requests navigation through the shell.
     */
    public EquipmentGroupPanel(ReadOnlyAddressBook data, Function<UUID, Availability> lookup,
            Consumer<String> onUse, Consumer<NusId> onMember, Consumer<UUID> onUnit) {
        super(8);
        this.data = requireNonNull(data);
        this.lookup = requireNonNull(lookup);
        details = new EquipmentDetailsPanel(data, lookup, onUse, onMember);
        detailBody = details.detachContent();
        requireNonNull(onUnit);
        getStyleClass().addAll("laplace-surface", "group-panel");
        getStylesheets().add(getClass().getResource("/view/DetailsPanel.css").toExternalForm());
        setMinHeight(0);
        title.setId("groupTitle");
        title.getStyleClass().add("laplace-title");
        title.setWrapText(true);
        counts.setId("groupCounts");
        counts.setWrapText(true);
        counts.setMinHeight(USE_PREF_SIZE);
        counts.setMinWidth(0);
        counts.getStyleClass().add("laplace-muted");
        availabilityCounts.setId("groupAvailability");
        availabilityCounts.setWrapText(true);
        availabilityCounts.setMinHeight(USE_PREF_SIZE);
        showAll.setId("showAllUnits");
        showAll.setWrapText(true);
        showAll.setOnAction(event -> refresh());
        units.setId("physicalUnits");
        units.setMinHeight(0);
        units.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        units.setPlaceholder(new Label("No units match these filters. Try Show all units or reset search."));
        addColumn("Unit", unit -> "…" + unit.getUuid().toString().substring(28));
        addColumn("Condition", unit -> EquipmentPresentation.label(unit.getCondition()));
        addColumn("Availability", this::availability);
        addColumn("Holder", this::holder);
        addColumn("Status", unit -> unit.isDeleted() ? "Deleted" : "Active");
        units.getSelectionModel().selectedItemProperty().addListener((unused, previous, current) -> {
            if (!isRefreshing && current != null) {
                onUnit.accept(current.getUuid());
            }
        });
        prompt.setWrapText(true);
        detailArea.setMinHeight(0);
        detailArea.getChildren().setAll(prompt);
        VBox body = new VBox(8, title, counts, availabilityCounts, showAll, units, detailArea);
        body.setMinWidth(0);
        scroll.setContent(body);
        scroll.setFitToWidth(true);
        scroll.setMinHeight(0);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        getChildren().add(scroll);
    }

    /**
     * Opens a group with no implicit physical-unit selection.
     */
    public void showGroup(InventoryGroups.Key key, DirectoryQuery query) {
        this.key = requireNonNull(key);
        this.query = requireNonNull(query);
        selectedUuid = null;
        showAll.setSelected(false);
        refresh();
    }

    /**
     * Resolves an exact UUID, showing all group units if that target lies outside the active filter.
     */
    public void showUnit(UUID uuid, DirectoryQuery query) {
        boolean hasIdentityChanged = !requireNonNull(uuid).equals(selectedUuid);
        selectedUuid = requireNonNull(uuid);
        this.query = requireNonNull(query);
        if (query.equipment(data, lookup).stream().noneMatch(unit -> unit.getUuid().equals(uuid))) {
            showAll.setSelected(true);
        }
        refresh();
        int index = units.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            units.scrollTo(index);
        }
        if (hasIdentityChanged) {
            scroll.setVvalue(0);
        }
    }

    /**
     * Recomputes group membership, counts and selection after edits, loans and lifecycle changes.
     */
    public void refresh() {
        isRefreshing = true;
        try {
            Optional<Equipment> selected = data.getEquipmentList().stream()
                    .filter(unit -> unit.getUuid().equals(selectedUuid)).findFirst();
            selected.ifPresent(unit -> key = InventoryGroups.keyOf(unit));
            if (key == null) {
                return;
            }
            List<Equipment> all = data.getEquipmentList().stream()
                    .filter(unit -> InventoryGroups.keyOf(unit).equals(key))
                    .sorted(Comparator.comparing(unit -> unit.getUuid().toString())).toList();
            List<Equipment> matching = query.equipment(data, lookup).stream()
                    .filter(unit -> InventoryGroups.keyOf(unit).equals(key)).toList();
            title.setText(all.isEmpty() ? "Equipment group no longer exists" : all.getFirst().getName());
            long active = all.stream().filter(unit -> !unit.isDeleted()).count();
            counts.setText(key.category() + " · " + matching.size() + " matching / " + all.size()
                    + " retained units · " + active + " active · " + (all.size() - active) + " deleted"
                    + (showAll.isSelected() ? " · Table shows all units" : " · Table shows matching units"));
            List<Equipment> shown = showAll.isSelected() ? all : matching;
            availabilityCounts.setText((showAll.isSelected() ? "All units: " : "Matching units: ")
                    + EquipmentPresentation.counts(shown, lookup));
            List<Equipment> ordered = units.getComparator() == null ? shown
                    : shown.stream().sorted(units.getComparator()).toList();
            // Replacing unchanged items resets the virtualized table's browsing position.
            if (!units.getItems().equals(ordered)) {
                units.getItems().setAll(ordered);
                units.sort();
            }
            units.refresh();
            units.setPrefHeight(Math.min(230, Math.max(90, units.getItems().size() * 30 + 34)));
            units.setMinHeight(USE_PREF_SIZE);
            units.setMaxHeight(USE_PREF_SIZE);
            units.getSelectionModel().clearSelection();
            if (selected.isPresent()) {
                if (shown.contains(selected.orElseThrow())) {
                    units.getSelectionModel().select(selected.orElseThrow());
                } else {
                    counts.setText(counts.getText() + " · Selected unit is outside the table filter");
                }
                details.showEquipment(selectedUuid);
                detailArea.getChildren().setAll(detailBody);
            } else {
                selectedUuid = null;
                detailArea.getChildren().setAll(prompt);
            }
        } finally {
            isRefreshing = false;
        }
    }

    /**
     * Adds a readable column whose cells retain the complete unit identity in their tooltip.
     */
    private void addColumn(String caption, Function<Equipment, String> value) {
        TableColumn<Equipment, String> column = new TableColumn<>(caption);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        column.setCellFactory(unused -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean isEmpty) {
                super.updateItem(item, isEmpty);
                setText(isEmpty ? null : item);
                Equipment unit = getTableRow() == null ? null : getTableRow().getItem();
                setTooltip(isEmpty || unit == null ? null
                        : new Tooltip(caption + ": " + item + "\nUUID: " + unit.getUuid()));
            }
        });
        column.setMinWidth(60);
        column.setPrefWidth(110);
        units.getColumns().add(column);
    }

    /**
     * Formats current availability, preserving a visible fallback for inconsistent references.
     */
    private String availability(Equipment unit) {
        try {
            return EquipmentPresentation.label(lookup.apply(unit.getUuid()));
        } catch (EquipmentNotFoundException exception) {
            return "Unknown";
        }
    }

    /**
     * Resolves the current holder through the open loan instead of storing a separate holder field.
     */
    private String holder(Equipment unit) {
        return data.getLoanList().stream().filter(loan -> loan.isOpen()
                && loan.getEquipmentUuid().equals(unit.getUuid())).findFirst().map(loan ->
                    data.getMemberList().stream().filter(member -> member.getNusId().equals(loan.getMemberNusId()))
                            .findFirst().map(member -> member.getName().fullName)
                            .orElse("Missing member: " + loan.getMemberNusId())).orElse("—");
    }
}
