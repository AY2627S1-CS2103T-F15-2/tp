package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.equipment.Availability;
import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.loan.Loan;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.query.LoanQuery;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.FxTestUtil;

/** Tests draft return observations across virtualized rows and loan lifecycle changes. */
public class LoanPanelDraftTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 2);
    private static final NusId MEMBER_ID = new NusId("A0123456X");
    private static final UUID FIRST_UUID = new UUID(0, 1);

    @TempDir
    public Path temporaryFolder;

    @Test
    public void prepareReturn_recycledRows_preservesObservationThroughSaveAndReload() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = new ModelManager(book(100), new UserPrefs());
            AddressBook before = new AddressBook(model.getAddressBook());
            List<String> commands = new ArrayList<>();
            LoanPanel panel = panel(model, commands);
            JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("loans.json"));
            LogicManager logic = new LogicManager(model, new StorageManager(storage,
                    new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
            Stage stage = show(panel);
            try {
                ListView<?> list = (ListView<?>) panel.getRoot().lookup(".loan-virtual-list");
                Region first = scrollToRow(list, 0);
                ComboBox<?> original = observation(first);
                select(first, "DAMAGED");
                prepare(first);
                for (int index : List.of(25, 50, 75, 99)) {
                    Region other = scrollToRow(list, index);
                    assertEquals("Not recorded", observation(other).getValue());
                }
                Region recycled = scrollToRow(list, 0);
                assertNotSame(original, observation(recycled), "The row controls must actually be recreated");
                assertEquals("DAMAGED", observation(recycled).getValue());
                prepare(recycled);
                assertEquals(List.of(returnCommand(" condition/DAMAGED"), returnCommand(" condition/DAMAGED")),
                        commands);
                assertEquals(before, model.getAddressBook());
                assertFalse(Files.exists(storage.getAddressBookFilePath()));

                assertDoesNotThrow(() -> logic.execute(commands.getLast()));
                panel.refresh();
                assertEquals(Availability.UNAVAILABLE, model.getAvailability(FIRST_UUID));
                assertEquals(Condition.DAMAGED, model.findEquipment(FIRST_UUID).orElseThrow().getCondition());
                var reloaded = assertDoesNotThrow(() -> storage.readAddressBook().orElseThrow());
                assertEquals(model.getAddressBook(), reloaded);
                Loan returned = reloaded.getLoanList().stream()
                        .filter(loan -> loan.getEquipmentUuid().equals(FIRST_UUID)).findFirst().orElseThrow();
                assertEquals(Condition.DAMAGED, returned.getReturnCondition().orElseThrow());
                assertFalse(returned.isOpen());
            } finally {
                stage.close();
            }
        });
    }

    @Test
    public void refresh_filterAndReset_preservesIndependentChoicesAndIntentionalOmission() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = new ModelManager(book(2), new UserPrefs());
            AddressBook before = new AddressBook(model.getAddressBook());
            List<String> commands = new ArrayList<>();
            LoanPanel panel = panel(model, commands);
            select(inlineRow(panel, 0), "DAMAGED");
            select(inlineRow(panel, 1), "FAIR");
            panel.setQuery(new LoanQuery("NO_MATCH", ""));
            panel.setQuery(LoanQuery.all());
            assertEquals("DAMAGED", observation(inlineRow(panel, 0)).getValue());
            assertEquals("FAIR", observation(inlineRow(panel, 1)).getValue());
            select(inlineRow(panel, 0), "Not recorded");
            panel.refresh();
            assertEquals("Not recorded", observation(inlineRow(panel, 0)).getValue());
            prepare(inlineRow(panel, 0));
            assertEquals(List.of(returnCommand("")), commands);
            observation(inlineRow(panel, 1)).setValue(null);
            panel.refresh();
            assertEquals("Not recorded", observation(inlineRow(panel, 1)).getValue());
            assertEquals(before, model.getAddressBook());
        });
    }

    @Test
    public void refresh_returnAndReassignment_discardsPreviousLoansDraft() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = new ModelManager(book(1), new UserPrefs());
            List<String> commands = new ArrayList<>();
            LoanPanel panel = panel(model, commands);
            Loan original = model.findOpenLoan(FIRST_UUID).orElseThrow();
            select(inlineRow(panel, 0), "DAMAGED");
            model.closeLoan(FIRST_UUID, TODAY);
            // A new loan for the same unit must not inherit the old observation, even before refresh.
            model.addLoan(new Loan(FIRST_UUID, MEMBER_ID, TODAY, TODAY.plusDays(2), null));
            panel.refresh();
            assertEquals("Not recorded", observation(inlineRow(panel, 0)).getValue());
            prepare(inlineRow(panel, 0));
            assertEquals(List.of(returnCommand("")), commands);
            // Restoring the original record after it left the open set must not resurrect its draft.
            AddressBook restored = book(1);
            assertEquals(original, restored.getLoanList().getFirst());
            model.setAddressBook(restored);
            panel.refresh();
            assertEquals("Not recorded", observation(inlineRow(panel, 0)).getValue());
        });
    }

    private static AddressBook book(int count) {
        AddressBook data = new AddressBook();
        data.addMember(new Member(MEMBER_ID, new Name("Test Member"), new Phone("91234567"),
                new Email("test@example.com")));
        for (int i = 1; i <= count; i++) {
            UUID uuid = new UUID(0, i);
            data.addEquipment(new Equipment(uuid, "Camera", "Testing", Condition.GOOD, ""));
            data.addLoan(new Loan(uuid, MEMBER_ID, TODAY.minusDays(1), TODAY, null));
        }
        return data;
    }

    private static LoanPanel panel(ModelManager model, List<String> commands) {
        LoanPanel panel = new LoanPanel(model.getAddressBook(), unused -> { }, unused -> { }, commands::add,
                Clock.fixed(TODAY.atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC));
        Region root = panel.getRoot();
        root.getStyleClass().add("laplace-root");
        Scene scene = new Scene(root, 1000, 800);
        scene.getStylesheets().add(LoanPanel.class.getResource("/view/LaplaceTheme.css").toExternalForm());
        root.applyCss();
        root.layout();
        return panel;
    }

    private static Stage show(LoanPanel panel) {
        Region root = panel.getRoot();
        Stage stage = new Stage();
        stage.setScene(root.getScene());
        stage.show();
        root.applyCss();
        root.layout();
        return stage;
    }

    private static Region scrollToRow(ListView<?> list, int index) {
        list.scrollTo(index);
        list.applyCss();
        list.layout();
        list.applyCss();
        list.layout();
        ListCell<?> cell = list.lookupAll(".list-cell").stream().map(node -> (ListCell<?>) node)
                .filter(item -> !item.isEmpty() && item.getIndex() == index).findFirst().orElseThrow();
        assertTrue(cell.isVisible());
        return (Region) cell.getGraphic();
    }

    private static Region inlineRow(LoanPanel panel, int index) {
        return (Region) ((VBox) panel.getRoot().lookup("#openLoans")).getChildren().get(index);
    }

    private static ComboBox<?> observation(Region row) {
        return (ComboBox<?>) row.lookup(".loan-return-condition");
    }

    private static void select(Region row, String value) {
        ComboBox<?> choice = observation(row);
        choice.getSelectionModel().select(choice.getItems().indexOf(value));
    }

    private static void prepare(Region row) {
        Button prepare = (Button) row.lookup(".loan-prepare-return");
        prepare.fire();
    }

    private static String returnCommand(String suffix) {
        return "loans return uuid/" + FIRST_UUID + " returned/" + TODAY + suffix;
    }
}
