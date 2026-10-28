package seedu.address.ui;

import java.util.List;
import java.util.function.Function;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.loan.Loan;

/**
 * Keeps small loan sections inline and bounds the scene graph for large sections using reusable cells.
 */
final class LoanRows {
    private static final int MAX_INLINE_ROWS = 10;
    private static final double LIST_HEIGHT = 420;

    private LoanRows() {
    }

    /**
     * Replaces rows without eagerly constructing a control tree for every retained loan.
     */
    static void show(VBox section, List<Loan> loans, Function<Loan, Region> createRow) {
        section.getChildren().clear();
        if (loans.size() <= MAX_INLINE_ROWS) {
            loans.forEach(loan -> section.getChildren().add(createRow.apply(loan)));
            return;
        }
        ListView<Loan> list = new ListView<>(FXCollections.observableArrayList(loans));
        list.getStyleClass().add("loan-virtual-list");
        list.setMinHeight(LIST_HEIGHT);
        list.setPrefHeight(LIST_HEIGHT);
        list.setMaxHeight(LIST_HEIGHT);
        list.setCellFactory(unused -> new ListCell<>() {
            @Override
            protected void updateItem(Loan loan, boolean isEmpty) {
                super.updateItem(loan, isEmpty);
                setText(null);
                if (isEmpty || loan == null) {
                    setGraphic(null);
                    return;
                }
                Region row = createRow.apply(loan);
                row.prefWidthProperty().bind(Bindings.createDoubleBinding(() -> Math.max(0,
                        getWidth() - getInsets().getLeft() - getInsets().getRight()),
                        widthProperty(), insetsProperty()));
                setGraphic(row);
            }
        });
        section.getChildren().add(list);
    }
}
