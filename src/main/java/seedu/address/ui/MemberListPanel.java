package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.function.Consumer;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Region;
import seedu.address.commons.util.LaplaceSortOrder;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;

/** Sorted read-only member rows. Only explicit user activation invokes the callback. */
public class MemberListPanel extends UiPart<Region> {

    private final ReadOnlyAddressBook data;
    private final Consumer<NusId> onSelect;

    @FXML
    private Label countLabel;
    @FXML
    private ListView<Member> rows;

    /** Creates the list from non-null dependencies; the shell owns listeners and initial selection. */
    public MemberListPanel(ReadOnlyAddressBook data, Consumer<NusId> onSelect) {
        super("MemberListPanel.fxml");
        this.data = requireNonNull(data);
        this.onSelect = requireNonNull(onSelect);
        rows.setCellFactory(unused -> new DirectoryCell());
        rows.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                getSelection().ifPresent(this.onSelect);
                event.consume();
            }
        });
        refresh();
    }

    /** Rebuilds real card snapshots, preserving an existing identity without invoking the callback. */
    public void refresh() {
        Optional<NusId> selected = getSelection();
        rows.getItems().setAll(data.getMemberList().stream().sorted(LaplaceSortOrder.MEMBERS).toList());
        countLabel.setText(rows.getItems().size() + " active");
        Label emptyState = new Label("No members yet. Use member add to get started.");
        emptyState.setWrapText(true);
        emptyState.setMaxWidth(Double.MAX_VALUE);
        emptyState.setMinWidth(0);
        rows.setPlaceholder(emptyState);
        rows.refresh();
        select(selected);
    }

    /** Returns the selected identity, if any. */
    public Optional<NusId> getSelection() {
        return Optional.ofNullable(rows.getSelectionModel().getSelectedItem()).map(Member::getNusId);
    }

    /** Selects an existing identity or clears an absent selection, without invoking the callback. */
    public void select(Optional<NusId> selected) {
        requireNonNull(selected);
        rows.getSelectionModel().clearSelection();
        selected.filter(id -> data.getMemberList().stream().anyMatch(item -> item.getNusId().equals(id)))
                .flatMap(id -> rows.getItems().stream()
                .filter(item -> item.getNusId().equals(id)).findFirst())
                .ifPresent(item -> {
                    rows.getSelectionModel().select(item);
                    rows.scrollTo(item);
                });
    }

    /** Reusable cells let the parent ListView own the selected-row style. */
    private class DirectoryCell extends ListCell<Member> {
        DirectoryCell() {
            setPrefWidth(0);
            setOnMouseClicked(event -> {
                if (!isEmpty() && getItem() != null && event.getButton() == MouseButton.PRIMARY
                        && event.getClickCount() == 1) {
                    rows.getSelectionModel().select(getItem());
                    onSelect.accept(getItem().getNusId());
                    event.consume();
                }
            });
        }

        @Override
        protected void updateItem(Member item, boolean empty) {
            super.updateItem(item, empty);
            setText(null);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                int held = (int) data.getLoanList().stream()
                        .filter(loan -> loan.isOpen() && loan.getMemberNusId().equals(item.getNusId())).count();
                Region card = new MemberCard(item, held).getRoot();
                card.prefWidthProperty().bind(Bindings.max(0, widthProperty().subtract(2)));
                setGraphic(card);
            }
        }
    }
}
