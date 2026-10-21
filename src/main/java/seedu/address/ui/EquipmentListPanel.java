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
import seedu.address.model.equipment.exceptions.EquipmentNotFoundException;
import seedu.address.model.loan.Loan;
import seedu.address.model.member.Member;
import seedu.address.model.query.DirectoryQuery;

/** Sorted read-only equipment rows. Only explicit user activation invokes the callback. */
public class EquipmentListPanel extends UiPart<Region> {

    private final ReadOnlyAddressBook data;
    private final Consumer<UUID> onSelect;
    private final Function<UUID, Availability> availabilityLookup;
    private DirectoryQuery query = DirectoryQuery.all();

    @FXML
    private Label countLabel;
    @FXML
    private ListView<Equipment> rows;

    /** Creates the list from non-null dependencies; the shell owns listeners and initial selection. */
    public EquipmentListPanel(ReadOnlyAddressBook data,
            Function<UUID, Availability> availabilityLookup, Consumer<UUID> onSelect) {
        super("EquipmentListPanel.fxml");
        this.data = requireNonNull(data);
        this.onSelect = requireNonNull(onSelect);
        this.availabilityLookup = requireNonNull(availabilityLookup);
        rows.setCellFactory(unused -> new DirectoryCell());
        rows.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                getSelection().ifPresent(this.onSelect);
                event.consume();
            }
        });
        refresh();
    }

    /** Sets transient criteria; the parent refreshes once after coordinating both directory components. */
    public void setQuery(DirectoryQuery query) {
        this.query = requireNonNull(query);
    }

    /** Returns the active transient directory criteria. */
    public DirectoryQuery getQuery() {
        return query;
    }

    /** Returns a fresh sorted query snapshot for identity-preserving shell selection recovery. */
    public List<Equipment> getDisplayedItems() {
        return query.equipment(data, availabilityLookup);
    }

    /** Rebuilds real card snapshots, preserving an existing identity without invoking the callback. */
    public void refresh() {
        Optional<UUID> selected = getSelection();
        rows.getItems().setAll(getDisplayedItems());
        countLabel.setText(query.equals(DirectoryQuery.all()) ? rows.getItems().size() + " items"
                : rows.getItems().size() + " of " + data.getEquipmentList().size() + " shown");
        Label emptyState = new Label(data.getEquipmentList().isEmpty()
                ? "No equipment yet. Use equipment add to get started."
                : "No matching equipment. Use equipment list to reset the search.");
        emptyState.setWrapText(true);
        emptyState.setMaxWidth(Double.MAX_VALUE);
        emptyState.setMinWidth(0);
        rows.setPlaceholder(emptyState);
        rows.refresh();
        select(selected);
    }

    /** Returns the selected identity, if any. */
    public Optional<UUID> getSelection() {
        return Optional.ofNullable(rows.getSelectionModel().getSelectedItem()).map(Equipment::getUuid);
    }

    /** Selects an existing identity or clears an absent selection, without invoking the callback. */
    public void select(Optional<UUID> selected) {
        requireNonNull(selected);
        if (getSelection().equals(selected)) {
            return;
        }
        rows.getSelectionModel().clearSelection();
        selected.filter(id -> data.getEquipmentList().stream().anyMatch(item -> item.getUuid().equals(id)))
                .flatMap(id -> rows.getItems().stream()
                .filter(item -> item.getUuid().equals(id)).findFirst())
                .ifPresent(item -> {
                    rows.getSelectionModel().select(item);
                    rows.scrollTo(item);
                });
    }

    private Region createCard(Equipment equipment) {
        Availability availability;
        try {
            availability = requireNonNull(availabilityLookup.apply(equipment.getUuid()));
        } catch (EquipmentNotFoundException e) {
            return inconsistentRow(equipment, "Availability could not be determined.");
        }
        Optional<Loan> loan = data.getLoanList().stream()
                .filter(value -> value.isOpen() && value.getEquipmentUuid().equals(equipment.getUuid())).findFirst();
        if (loan.isPresent() && availability != Availability.ASSIGNED) {
            return inconsistentRow(equipment, "Open loan conflicts with derived availability.");
        }
        Optional<Member> holder = loan.flatMap(value -> data.getMemberList().stream()
                .filter(member -> member.getNusId().equals(value.getMemberNusId())).findFirst());
        return new EquipmentCard(equipment, availability, holder).getRoot();
    }

    private Region inconsistentRow(Equipment equipment, String message) {
        Label details = new Label(equipment.getName() + "\nUUID: " + equipment.getUuid() + "\n" + message);
        details.setWrapText(true);
        details.setMinWidth(0);
        VBox row = new VBox(details);
        row.getStyleClass().addAll("laplace-row", "laplace-error", "equipment-inconsistent-row");
        return row;
    }

    /** Reusable cells let the parent ListView own the selected-row style. */
    private class DirectoryCell extends ListCell<Equipment> {
        DirectoryCell() {
            setPrefWidth(0);
            setOnMouseClicked(event -> {
                if (!isEmpty() && getItem() != null && event.getButton() == MouseButton.PRIMARY
                        && event.getClickCount() == 1) {
                    rows.getSelectionModel().select(getItem());
                    onSelect.accept(getItem().getUuid());
                    event.consume();
                }
            });
        }

        @Override
        protected void updateItem(Equipment item, boolean empty) {
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
