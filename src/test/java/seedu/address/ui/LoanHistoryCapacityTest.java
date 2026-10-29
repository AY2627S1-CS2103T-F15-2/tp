package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.ViewTestData.ALEX;
import static seedu.address.testutil.ViewTestData.CAMERA;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.ModelManager;
import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.loan.Loan;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.testutil.FxTestUtil;

/**
 * Verifies bounded rendering and access to complete histories at the documented record capacity.
 */
class LoanHistoryCapacityTest {
    @Test
    void largeHistory_reusesVisibleRowsAndKeepsEveryLoanReachable() {
        List<Loan> loans = history();
        FxTestUtil.runAndWait(() -> {
            VBox section = new VBox();
            int[] rendered = {0};
            LoanRows.show(section, loans, loan -> {
                rendered[0]++;
                return new Label(loan.getAssignedDate().toString());
            });
            assertEquals(0, rendered[0]);
            mount(section);
            ListView<?> list = (ListView<?>) section.getChildren().getFirst();
            assertEquals(10_000, list.getItems().size());
            assertTrue(rendered[0] > 0 && rendered[0] < 100, "Only viewport cells should be constructed");
            list.scrollTo(9_999);
            section.layout();
            assertTrue(section.lookupAll(".label").stream().map(node -> ((Label) node).getText())
                    .anyMatch(text -> text.equals(loans.getLast().getAssignedDate().toString())));
            assertTrue(rendered[0] < 200, "Scrolling must not construct all intervening rows");
            LoanRows.show(section, List.of(loans.getFirst()), loan -> new Label("One loan"));
            assertEquals(1, section.getChildren().size());
            assertEquals("One loan", ((Label) section.getChildren().getFirst()).getText());
        });
    }

    @Test
    void documentedCapacity_detailsAndDirectory_boundControlsAndRefreshFilters() {
        ModelManager model = modelAtCapacity();
        FxTestUtil.runAndWait(() -> {
            MemberDetailsPanel member = new MemberDetailsPanel(model.getAddressBook(), model::getAvailability);
            member.showMember(ALEX.getNusId());
            mount(member.getRoot());
            assertBoundedHistory(member.getRoot());
            EquipmentDetailsPanel equipment = new EquipmentDetailsPanel(model.getAddressBook(), model::getAvailability);
            equipment.showEquipment(CAMERA.getUuid());
            mount(equipment.getRoot());
            assertBoundedHistory(equipment.getRoot());
            LoanPanel directory = new LoanPanel(model.getAddressBook(), unused -> { }, unused -> { });
            mount(directory.getRoot());
            assertEquals("10000", ((Label) directory.getRoot().lookup("#closedCount")).getText());
            assertBoundedHistory(directory.getRoot());
            TextField filter = (TextField) directory.getRoot().lookup("#memberFilter");
            filter.setText("A9999999X");
            assertEquals("0", ((Label) directory.getRoot().lookup("#closedCount")).getText());
            assertTrue(directory.getRoot().lookupAll(".loan-virtual-list").isEmpty());
            filter.clear();
            directory.refresh();
            mount(directory.getRoot());
            assertBoundedHistory(directory.getRoot());
            assertEquals(10_000, model.getLoanList().size());
        });
    }

    private static void assertBoundedHistory(Region root) {
        ListView<?> list = (ListView<?>) root.lookup(".loan-virtual-list");
        assertEquals(10_000, list.getItems().size());
        assertTrue(root.lookupAll(".label").size() < 500, "The scene graph must remain bounded");
    }

    private static void mount(Region root) {
        root.getStyleClass().add("laplace-root");
        Scene scene = root.getScene() == null ? new Scene(root, 800, 700) : root.getScene();
        scene.getStylesheets().add(LoanRows.class.getResource("/view/LaplaceTheme.css").toExternalForm());
        root.resize(800, 700);
        root.applyCss();
        root.layout();
    }

    private static ModelManager modelAtCapacity() {
        ModelManager model = new ModelManager();
        model.addMember(ALEX);
        model.addEquipment(CAMERA);
        for (int index = 1; index < 1_000; index++) {
            model.addMember(new Member(new NusId(String.format("A%07dX", index)), ALEX.getName(),
                    ALEX.getPhone(), ALEX.getEmail()));
        }
        for (int index = 1; index < 5_000; index++) {
            model.addEquipment(new Equipment(new UUID(1, index), "Spare", "Photo", Condition.GOOD, ""));
        }
        for (Loan loan : history()) {
            model.addLoan(loan);
        }
        return model;
    }

    private static List<Loan> history() {
        return IntStream.range(0, 10_000).mapToObj(index -> {
            LocalDate date = LocalDate.of(1980, 1, 1).plusDays(index);
            return new Loan(CAMERA.getUuid(), ALEX.getNusId(), date, date, date);
        }).toList();
    }
}
