package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import org.junit.jupiter.api.Test;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.CommandResult;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.loan.Loan;
import seedu.address.model.member.Member;
import seedu.address.model.person.Person;
import seedu.address.testutil.FxTestUtil;
import seedu.address.testutil.ViewTestData;

/** Uses real JavaFX event dispatch and focus traversal rather than calling private handlers. */
public class CompletionControllerTest {
    @Test
    public void recordChange_activeCycle_logsOneReasonWithoutPrivateValues() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = ViewTestData.model();
            TextField field = new TextField();
            new CompletionController(field, model::getAddressBook);
            new Scene(new VBox(field));
            Logger logger = LogsCenter.getLogger(CompletionController.class);
            Level previousLevel = logger.getLevel();
            boolean wasUsingParentHandlers = logger.getUseParentHandlers();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            StreamHandler handler = new StreamHandler(output, new SimpleFormatter());
            handler.setLevel(Level.FINE);
            try {
                logger.setLevel(Level.FINE);
                logger.setUseParentHandlers(false);
                logger.addHandler(handler);
                type(field, "member edit nus/");
                tab(field);
                model.deleteEquipment(ViewTestData.SPEAKER);
                model.restoreEquipment(ViewTestData.SPEAKER);
                type(field, "member ");
                tab(field);
                model.deleteEquipment(ViewTestData.SPEAKER);
                handler.flush();

                String diagnostic = output.toString(StandardCharsets.UTF_8);
                assertEquals(1, diagnostic.lines().filter(line -> line.contains("records changed")).count());
                assertFalse(diagnostic.contains(ViewTestData.ALEX.getNusId().value));
                assertFalse(diagnostic.contains(ViewTestData.ALEX.getName().fullName));
                assertFalse(diagnostic.contains("member edit nus/"));
            } finally {
                logger.removeHandler(handler);
                logger.setLevel(previousLevel);
                logger.setUseParentHandlers(wasUsingParentHandlers);
                handler.close();
            }
        });
    }

    @Test
    public void tab_staticVocabularyAndCycles_neverReadRecords() {
        FxTestUtil.runAndWait(() -> {
            TextField field = new TextField();
            new CompletionController(field, () -> {
                throw new AssertionError("Static suggestions must not acquire model data");
            });
            new Scene(new VBox(field));
            type(field, "member ");
            tab(field);
            assertEquals("member add", field.getText());
            tab(field);
            assertEquals("member delete", field.getText());
            key(field, KeyCode.TAB, true, false, false, false);
            assertEquals("member add", field.getText());
            type(field, "equipment add co");
            tab(field);
            assertEquals("equipment add condition/", field.getText());
            type(field, "equipment add condition/");
            tab(field);
            assertEquals("equipment add condition/DAMAGED", field.getText());
            tab(field);
            assertEquals("equipment add condition/FAIR", field.getText());
        });
    }

    @Test
    public void tab_recordCycles_reuseCandidatesWithoutCopyingOrScanningUnrelatedCollections() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = ViewTestData.model();
            CountingData data = new CountingData(model.getAddressBook());
            TextField field = new TextField();
            new CompletionController(field, () -> data);
            new Scene(new VBox(field));
            type(field, "equipment add name/");
            tab(field);
            assertEquals("equipment add name/Camera", field.getText());
            int reads = data.recordReads;
            tab(field);
            assertEquals("equipment add name/Speaker", field.getText());
            key(field, KeyCode.TAB, true, false, false, false);
            assertEquals("equipment add name/Camera", field.getText());
            assertEquals(reads, data.recordReads);
            type(field, "member ");
            tab(field);
            model.deleteEquipment(ViewTestData.SPEAKER);
            tab(field);
            assertEquals("member delete", field.getText());
        });
    }

    @Test
    public void tab_replacedRoot_detachesOldListenersAndObservesNewRecords() {
        FxTestUtil.runAndWait(() -> {
            ModelManager oldModel = ViewTestData.model();
            ModelManager newModel = ViewTestData.model();
            AtomicReference<ReadOnlyAddressBook> root = new AtomicReference<>(oldModel.getAddressBook());
            TextField field = new TextField();
            new CompletionController(field, root::get);
            new Scene(new VBox(field));
            type(field, "equipment add name/");
            tab(field);
            root.set(newModel.getAddressBook());
            tab(field);
            assertEquals("equipment add name/Camera", field.getText());
            type(field, "equipment add name/");
            tab(field);
            oldModel.deleteEquipment(ViewTestData.SPEAKER);
            tab(field);
            assertEquals("equipment add name/Speaker", field.getText());
            newModel.deleteEquipment(ViewTestData.SPEAKER);
            tab(field);
            assertEquals("equipment add name/Speaker", field.getText());
        });
    }

    /**
     * Counts value-source reads and rejects the unrelated collections a full root copy would touch.
     */
    private static class CountingData implements ReadOnlyAddressBook {
        private final ReadOnlyAddressBook delegate;
        private int recordReads;

        CountingData(ReadOnlyAddressBook delegate) {
            this.delegate = delegate;
        }

        @Override
        public ObservableList<Person> getPersonList() {
            throw new AssertionError("Completion must not copy people");
        }

        @Override
        public ObservableList<Member> getMemberList() {
            recordReads++;
            return delegate.getMemberList();
        }

        @Override
        public ObservableList<Equipment> getEquipmentList() {
            recordReads++;
            return delegate.getEquipmentList();
        }

        @Override
        public ObservableList<Loan> getLoanList() {
            throw new AssertionError("Completion must not copy or validate loan history");
        }
    }

    @Test
    public void tab_fieldAfterLoanIdentities_preservesBatchAndFollowingFields() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = ViewTestData.model();
            List<String> executed = new ArrayList<>();
            CommandBox box = new CommandBox(command -> {
                executed.add(command);
                return new CommandResult("OK");
            }, model::getAddressBook);
            new Scene(new VBox(box.getRoot()));
            TextField field = (TextField) box.getRoot().lookup("#commandTextField");
            String first = ViewTestData.CAMERA.getUuid().toString();
            String second = ViewTestData.OTHER_CAMERA.getUuid().toString();
            for (String identities : List.of(first, first + ", " + second)) {
                Map<String, String> replacements = Map.of(
                        "loans add uuid/" + identities + " nu", "loans add uuid/" + identities + " nus/",
                        "loans return uuid/" + identities + " re", "loans return uuid/" + identities + " returned/",
                        "loans return uuid/" + identities + " co", "loans return uuid/" + identities + " condition/");
                replacements.forEach((input, expected) -> {
                    String following = input.startsWith("loans add") ? "   expected/2026-01-08"
                            : input.endsWith(" re") ? "   condition/GOOD" : "   returned/2026-01-08";
                    type(field, input + following);
                    field.positionCaret(input.length());
                    tab(field);
                    assertEquals(expected + following, field.getText());
                    assertEquals(expected.length(), field.getCaretPosition());
                });
            }
            assertTrue(executed.isEmpty());
            assertTrue(model.getLoanList().isEmpty());
        });
    }

    @Test
    public void tab_multiwordValueOverlapsPrefix_completesValueWithoutExecuting() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = ViewTestData.model();
            Equipment equipment = new Equipment(ViewTestData.CAMERA.getUuid(), "Camera Cover", "Photo Cover",
                    ViewTestData.CAMERA.getCondition(), "");
            model.setEquipment(ViewTestData.CAMERA, equipment);
            List<String> executed = new ArrayList<>();
            CommandBox box = new CommandBox(command -> {
                executed.add(command);
                return new CommandResult("OK");
            }, model::getAddressBook);
            new Scene(new VBox(box.getRoot()));
            TextField field = (TextField) box.getRoot().lookup("#commandTextField");
            type(field, "equipment add name/Camera co category/Photo");
            field.positionCaret(field.getText().indexOf(" category/"));
            tab(field);
            assertEquals("equipment add name/Camera Cover category/Photo", field.getText());
            assertEquals("equipment add name/Camera Cover".length(), field.getCaretPosition());
            assertTrue(executed.isEmpty());
            assertEquals(equipment, model.findEquipment(equipment.getUuid()).orElseThrow());
        });
    }

    @Test
    public void tab_equivalentWhitespace_preservesFollowingFieldAndStoredSpelling() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = ViewTestData.model();
            List<String> executed = new ArrayList<>();
            CommandBox box = new CommandBox(command -> {
                executed.add(command);
                return new CommandResult("OK");
            }, model::getAddressBook);
            new Scene(new VBox(box.getRoot()));
            TextField field = (TextField) box.getRoot().lookup("#commandTextField");
            for (String separator : List.of("  ", "\u2003", "\u00a0")) {
                type(field, "member edit name/alex" + separator + "T" + separator + "phone/91234567");
                field.positionCaret(field.getText().indexOf("phone/") - separator.length());
                tab(field);
                assertEquals("member edit name/Alex Tan" + separator + "phone/91234567", field.getText());
                assertEquals("member edit name/Alex Tan".length(), field.getCaretPosition());
            }
            assertTrue(executed.isEmpty());
            assertEquals(ViewTestData.ALEX, model.findMember(ViewTestData.ALEX.getNusId()).orElseThrow());
        });
    }

    @Test
    public void tab_cycleAndReverse_wrapsAndDoesNotExecute() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = ViewTestData.model();
            List<String> executed = new ArrayList<>();
            CommandBox box = new CommandBox(command -> {
                executed.add(command);
                return new CommandResult("OK");
            }, model::getAddressBook);
            new Scene(new VBox(box.getRoot()));
            TextField field = (TextField) box.getRoot().lookup("#commandTextField");
            box.getRoot().applyCss();
            type(field, "member ");
            key(field, KeyCode.TAB, false, false, false, false);
            assertEquals("member add", field.getText());
            assertEquals(field.getText().length(), field.getCaretPosition());
            for (String candidate : List.of("delete", "edit", "list", "restore", "search", "view", "add")) {
                key(field, KeyCode.TAB, false, false, false, false);
                assertEquals("member " + candidate, field.getText());
            }
            key(field, KeyCode.SHIFT, true, false, false, false);
            key(field, KeyCode.TAB, true, false, false, false);
            assertEquals("member view", field.getText());
            assertTrue(executed.isEmpty());
            key(field, KeyCode.ENTER, false, false, false, false);
            // JavaFX Enter dispatches the existing ActionEvent once.
            assertEquals(List.of("member view"), executed);
            assertEquals("", field.getText());
            type(field, "mem");
            key(field, KeyCode.TAB, false, false, false, false);
            assertEquals("member", field.getText());
            key(field, KeyCode.TAB, false, false, false, false);
            assertEquals("member", field.getText());
        });
    }

    @Test
    public void tab_editsSelectionAndModelChange_resetCycle() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = ViewTestData.model();
            TextField field = new TextField();
            new CompletionController(field, model::getAddressBook);
            new Scene(new VBox(field));
            type(field, "equipment add name/");
            tab(field);
            assertEquals("equipment add name/Camera", field.getText());
            var original = ViewTestData.CAMERA;
            var renamed = new Equipment(original.getUuid(), "Tripod",
                    original.getCategory(), original.getCondition(), original.getNotes());
            model.setEquipment(original, renamed);
            // A changed snapshot resets cycling, so the surviving Camera match remains selected.
            tab(field);
            assertEquals("equipment add name/Camera", field.getText());
            type(field, "equipment add name/");
            tab(field);
            assertEquals("equipment add name/Camera", field.getText());
            tab(field);
            assertEquals("equipment add name/Speaker", field.getText());
            model.deleteEquipment(ViewTestData.OTHER_CAMERA);
            tab(field);
            assertEquals("equipment add name/Speaker", field.getText());
            model.setEquipment(renamed, original);
            type(field, "equipment add name/");
            tab(field);
            assertEquals("equipment add name/Camera", field.getText());
            type(field, "member ");
            tab(field);
            field.positionCaret(3);
            tab(field);
            assertEquals("member add", field.getText());
            field.selectAll();
            tab(field);
            assertEquals("member add", field.getText());
            field.deselect();
            type(field, "member ");
            tab(field);
            field.fireEvent(new ActionEvent());
            field.appendText(" na");
            tab(field);
            assertEquals("member add name/", field.getText());
            assertThrows(NullPointerException.class, () -> new CompletionController(null, model::getAddressBook));
            assertThrows(NullPointerException.class, () -> new CompletionController(field, null));
            assertThrows(NullPointerException.class, () -> new CommandBox(command -> new CommandResult("OK"), null));
        });
    }

    @Test
    public void tab_existingFieldBoundary_keepsMarkerValueAndModelUnchanged() {
        FxTestUtil.runAndWait(() -> {
            ModelManager model = ViewTestData.model();
            List<String> executed = new ArrayList<>();
            CommandBox box = new CommandBox(command -> {
                executed.add(command);
                return new CommandResult("OK");
            }, model::getAddressBook);
            new Scene(new VBox(box.getRoot()));
            TextField field = (TextField) box.getRoot().lookup("#commandTextField");
            box.getRoot().applyCss();
            String text = "member add name/Alex phone/91234567";
            type(field, text);
            field.positionCaret(text.indexOf("phone/"));
            tab(field);
            assertEquals(text, field.getText());
            assertTrue(executed.isEmpty());
            type(field, "member ed");
            tab(field);
            assertEquals("member edit", field.getText());
            type(field, "member edit name/al");
            tab(field);
            assertEquals("member edit name/Alex Tan", field.getText());
            assertTrue(executed.isEmpty());
        });
    }

    @Test
    public void tab_modifiersAndUnsupportedInput_passThroughWithoutEditing() {
        FxTestUtil.runAndWait(() -> {
            TextField field = new TextField();
            new CompletionController(field, () -> ViewTestData.model().getAddressBook());
            List<KeyEvent> passed = new ArrayList<>();
            field.addEventFilter(KeyEvent.KEY_PRESSED, passed::add);
            new Scene(new VBox(field));
            type(field, "mem");
            key(field, KeyCode.TAB, false, true, false, false);
            key(field, KeyCode.TAB, false, false, true, false);
            key(field, KeyCode.TAB, false, false, false, true);
            key(field, KeyCode.TAB, true, false, false, false);
            assertEquals(4, passed.size());
            assertEquals("mem", field.getText());
            for (String input : List.of("", "unknown", "member add notes/abc")) {
                type(field, input);
                int previous = passed.size();
                tab(field);
                assertEquals(previous + 1, passed.size());
                assertEquals(input, field.getText());
            }
        });
    }

    @Test
    public void tab_noMatchAndFocusLoss_preservesKeyboardNavigation() {
        FxTestUtil.runAndWait(() -> {
            Stage stage = new Stage();
            FocusField field = new FocusField();
            Button next = new Button("Next");
            new CompletionController(field, () -> ViewTestData.model().getAddressBook());
            stage.setScene(new Scene(new VBox(field, next)));
            stage.show();
            try {
                field.requestFocus();
                type(field, "member ");
                tab(field);
                assertEquals("member add", field.getText());
                // Exercise window-manager callbacks without requiring native foreground activation.
                field.changeFocus(true);
                field.changeFocus(false);
                key(field, KeyCode.TAB, true, false, false, false);
                assertEquals(next, stage.getScene().getFocusOwner());
                field.requestFocus();
                type(field, "unknown");
                tab(field);
                assertEquals(next, stage.getScene().getFocusOwner());
                assertFalse(field.isDisabled());
            } finally {
                stage.hide();
            }
        });
    }

    /** Exposes JavaFX's protected focus setter for deterministic window-manager notification tests. */
    private static class FocusField extends TextField {
        void changeFocus(boolean value) {
            setFocused(value);
        }
    }

    private static void type(TextField field, String text) {
        field.setText(text);
        field.positionCaret(text.length());
    }

    private static void tab(TextField field) {
        key(field, KeyCode.TAB, false, false, false, false);
    }

    private static void key(TextField field, KeyCode code, boolean shift, boolean control, boolean alt, boolean meta) {
        field.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, shift, control, alt, meta));
        field.fireEvent(new KeyEvent(KeyEvent.KEY_RELEASED, "", "", code, shift, control, alt, meta));
    }
}
