package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.ViewTestData.ALEX;
import static seedu.address.testutil.ViewTestData.OTHER_ALEX;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.loan.Loan;
import seedu.address.testutil.FxTestUtil;

/** Verifies bounded current holdings independently of completed-history capacity. */
class MemberHoldingsCapacityTest {
    private static final LocalDate ASSIGNED = LocalDate.of(2026, 1, 1);

    @Test
    void largeHoldings_scrollAndRefresh_keepEveryIdentityAndNavigationWithoutWrites() {
        ModelManager model = modelWithHoldings(1_000);
        FxTestUtil.runAndWait(() -> {
            AddressBook before = new AddressBook(model.getAddressBook());
            List<UUID> navigation = new ArrayList<>();
            MemberDetailsPanel panel = new MemberDetailsPanel(model.getAddressBook(), model::getAvailability,
                    unused -> { }, navigation::add);
            panel.showMember(ALEX.getNusId());
            mount(panel.getRoot());
            ListView<?> list = holdingsList(panel, 1_000);
            assertEquals(IntStream.range(0, 1_000).mapToObj(MemberHoldingsCapacityTest::uuid).toList(),
                    list.getItems().stream().map(item -> ((Loan) item).getEquipmentUuid()).toList());
            for (int index : List.of(0, 499, 999)) {
                list.scrollTo(index);
                panel.getRoot().layout();
                Parent row = visibleHolding(panel, uuid(index));
                assertEquals(String.format("Camera %04d", index), value(row, "equipmentName"));
                assertEquals(ASSIGNED.toString(), value(row, "assignedDate"));
                assertEquals(ASSIGNED.plusDays(index / 100).toString(), value(row, "expectedReturnDate"));
                Button view = row.getChildrenUnmodifiable().stream().filter(Button.class::isInstance)
                        .map(Button.class::cast).findFirst().orElseThrow();
                view.fire();
                assertEquals(uuid(index), navigation.getLast());
                assertTrue(panel.getRoot().lookupAll(".label").size() < 500);
            }
            assertEquals(before, model.getAddressBook());
            assertEquals(3, navigation.size());
            model.closeLoan(uuid(999), ASSIGNED);
            panel.refresh();
            mount(panel.getRoot());
            list = holdingsList(panel, 999);
            assertTrue(list.getItems().stream().noneMatch(item -> ((Loan) item).getEquipmentUuid().equals(uuid(999))));
            model.addLoan(new Loan(uuid(999), ALEX.getNusId(), ASSIGNED, ASSIGNED.plusDays(20), null));
            before = new AddressBook(model.getAddressBook());
            panel.refresh();
            mount(panel.getRoot());
            list = holdingsList(panel, 1_000);
            list.scrollTo(999);
            panel.getRoot().layout();
            assertEquals(ASSIGNED.plusDays(20).toString(),
                    value(visibleHolding(panel, uuid(999)), "expectedReturnDate"));
            assertEquals(ALEX.getNusId().value, value(panel.getRoot(), "memberNusId"));
            assertEquals(3, navigation.size(), "Refresh must not navigate");
            assertEquals(before, model.getAddressBook());
            panel.showMember(OTHER_ALEX.getNusId());
            assertNull(panel.getRoot().lookup("#holdings"));
            assertEquals("0 item(s) held", value(panel.getRoot(), "heldCount"));
        });
    }

    @Test
    void holdings_inlineBoundaryAndReturn_keepsAllRowsAndRetainedHistory() {
        ModelManager model = modelWithHoldings(11);
        FxTestUtil.runAndWait(() -> {
            MemberDetailsPanel panel = new MemberDetailsPanel(model.getAddressBook(), model::getAvailability);
            panel.showMember(ALEX.getNusId());
            mount(panel.getRoot());
            holdingsList(panel, 11);
            model.closeLoan(uuid(10), ASSIGNED);
            panel.refresh();
            VBox holdings = (VBox) panel.getRoot().lookup("#holdings");
            assertNull(holdings.lookup(".loan-virtual-list"));
            assertEquals(10, holdings.getChildren().size());
            assertEquals(IntStream.range(0, 10).mapToObj(MemberHoldingsCapacityTest::uuid).toList(),
                    holdings.getChildren().stream().map(row -> UUID.fromString(value((Parent) row, "equipmentUuid")))
                            .toList());
            for (int index = 0; index < 10; index++) {
                model.closeLoan(uuid(index), ASSIGNED);
            }
            panel.refresh();
            mount(panel.getRoot());
            assertNull(panel.getRoot().lookup("#holdings"));
            assertEquals("0 item(s) held", value(panel.getRoot(), "heldCount"));
            assertEquals(11, model.getLoanList().size());
            assertTrue(model.getLoanList().stream().noneMatch(Loan::isOpen));
        });
    }

    private static ModelManager modelWithHoldings(int count) {
        ModelManager model = new ModelManager();
        model.addMember(ALEX);
        model.addMember(OTHER_ALEX);
        for (int index = count - 1; index >= 0; index--) {
            model.addEquipment(new Equipment(uuid(index), String.format("Camera %04d", index), "Testing",
                    Condition.GOOD, ""));
            model.addLoan(new Loan(uuid(index), ALEX.getNusId(), ASSIGNED, ASSIGNED.plusDays(index / 100), null));
        }
        return model;
    }

    private static UUID uuid(int index) {
        return new UUID(1, index);
    }

    private static ListView<?> holdingsList(MemberDetailsPanel panel, int count) {
        Parent holdings = (Parent) panel.getRoot().lookup("#holdings");
        ListView<?> list = (ListView<?>) holdings.lookup(".loan-virtual-list");
        assertNotNull(list, "Large current holdings must use bounded rows");
        assertEquals(count, list.getItems().size());
        assertTrue(panel.getRoot().lookupAll(".label").size() < 500);
        assertEquals(count + " item(s) held", value(panel.getRoot(), "heldCount"));
        return list;
    }

    private static Parent visibleHolding(MemberDetailsPanel panel, UUID uuid) {
        return panel.getRoot().lookup("#holdings").lookupAll(".details-holding").stream().map(Parent.class::cast)
                .filter(row -> uuid.toString().equals(value(row, "equipmentUuid"))).findFirst().orElseThrow();
    }

    private static String value(Parent root, String id) {
        return ((Label) root.lookup("#" + id)).getText();
    }

    private static void mount(Region root) {
        root.getStyleClass().add("laplace-root");
        Scene scene = root.getScene() == null ? new Scene(root, 800, 700) : root.getScene();
        scene.getStylesheets().add(LoanRows.class.getResource("/view/LaplaceTheme.css").toExternalForm());
        root.resize(800, 700);
        root.applyCss();
        root.layout();
    }
}
