package seedu.address.ui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.completion.CompletionService;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.FxTestUtil;
import seedu.address.testutil.ViewTestData;

/** Standalone keyboard smoke test with production classes and FXML loaded exclusively from the built JAR. */
public final class PackagedAutocompleteProbe {
    private PackagedAutocompleteProbe() {}

    /** Runs a fail-fast packaged session and writes actual keyboard results, including any failure. */
    public static void main(String[] args) throws Exception {
        Path transcript = Path.of(args[0]);
        Path jar = Path.of(args[1]);
        Path loaded = Path.of(CompletionService.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        List<String> session = new ArrayList<>();
        session.add("Production code source: " + loaded);
        check(Files.isSameFile(jar, loaded), "Production classes must load from the packaged JAR");
        try {
            FxTestUtil.runAndWait(() -> runSession(session));
            session.add("PASS: packaged JAR keyboard session.");
        } catch (AssertionError | RuntimeException failure) {
            session.add("FAIL: " + failure.getMessage());
            throw failure;
        } finally {
            Files.createDirectories(transcript.getParent());
            Files.write(transcript, session);
            Platform.exit();
        }
    }

    private static void runSession(List<String> session) {
        var model = ViewTestData.model();
        RecordingStorage storage = new RecordingStorage();
        RecordingLogic logic = new RecordingLogic(model, storage);
        MainWindow window = new MainWindow(new Stage(), logic, Path.of("data.json"));
        window.show();
        window.fillInnerParts();
        window.getPrimaryStage().getScene().getRoot().applyCss();
        TextField field = (TextField) window.getPrimaryStage().getScene().lookup("#commandTextField");
        try {
            String[][] examples = {
                {"mo", "mode"}, {"mode da", "mode dark"}, {"mode li", "mode light"},
                {"mem", "member"}, {"member ad", "member add"}, {"member add na", "member add name/"},
                {"member add name/al", "member add name/Alex Tan"},
                {"member ed", "member edit"}, {"member edit na", "member edit name/"},
                {"member edit name/al", "member edit name/Alex Tan"},
                {"member edit nus/a012", "member edit nus/A0123456X"},
                {"member edit phone/91", "member edit phone/91234567"},
                {"member edit email/alex", "member edit email/alex@example.com"},
                {"equipment add category/pho", "equipment add category/Photography"},
                {"equipment add condition/g", "equipment add condition/GOOD"},
                {"member res", "member restore"},
                    {"member restore n", "member restore nus/"},
                    {"member se", "member search"},
                    {"member search holding/y", "member search holding/YES"},
                    {"member search sort/he", "member search sort/HELD"},
                    {"equipment ed", "equipment edit"},
                    {"equipment edit co", "equipment edit condition/"},
                    {"equipment res", "equipment restore"},
                    {"equipment restore u", "equipment restore uuid/"},
                    {"equipment search av", "equipment search availability/"},
                    {"equipment search availability/av", "equipment search availability/AVAILABLE"},
                    {"equipment search status/de", "equipment search status/DELETED"},
                    {"equipment search order/de", "equipment search order/desc"},
                    {"equipment add cou", "equipment add count/"},
                    {"loans return co", "loans return condition/"},
                    {"loans return condition/fa", "loans return condition/FAIR"},
                    {"loans add uuid/00000000-0000-0000-0000-000000000001,00000000",
                        "loans add uuid/00000000-0000-0000-0000-000000000001,"
                            + "00000000-0000-0000-0000-000000000002"},
                    {"loans return uuid/00000000-0000-0000-0000-000000000001,  ",
                        "loans return uuid/00000000-0000-0000-0000-000000000001,  "
                            + "00000000-0000-0000-0000-000000000002"},
                    {"member add nus/A", "member add nus/A"}
            };
            for (String[] example : examples) {
                field.setText(example[0]);
                field.positionCaret(field.getLength());
                AddressBook before = new AddressBook(model.getAddressBook());
                press(field, KeyCode.TAB, false);
                session.add("INPUT: " + example[0] + " [Tab]");
                session.add("EXPECTED: " + example[1]);
                session.add("ACTUAL: " + field.getText());
                check(example[1].equals(field.getText()), "Unexpected Tab result");
                check(before.equals(model.getAddressBook()), "Tab changed stored records");
                check(logic.commands == 0 && storage.saves == 0, "Tab executed or saved a command");
            }
            String untouched = "member add name/Alex phone/91234567";
            field.setText(untouched);
            field.positionCaret(untouched.indexOf("phone/"));
            AddressBook beforeBoundary = new AddressBook(model.getAddressBook());
            press(field, KeyCode.TAB, false);
            session.add("BOUNDARY: member add name/Alex |phone/91234567 [Tab]");
            session.add("EXPECTED: " + untouched);
            session.add("ACTUAL: " + field.getText());
            check(untouched.equals(field.getText()), "Tab must preserve an untouched following field");
            check(beforeBoundary.equals(model.getAddressBook()), "Boundary completion changed records");
            check(logic.commands == 0 && storage.saves == 0, "Boundary completion executed or saved a command");
            field.setText("member ");
            field.positionCaret(field.getLength());
            press(field, KeyCode.TAB, false);
            press(field, KeyCode.TAB, false);
            check(field.getText().equals("member delete"), "Tab must cycle forward");
            press(field, KeyCode.SHIFT, true);
            press(field, KeyCode.TAB, true);
            check(field.getText().equals("member add"), "Shift+Tab must cycle backward");
            session.add("CYCLE: member add -> member delete -> [Shift+Tab] member add");
            field.setText("member view nus/a012");
            field.positionCaret(field.getLength());
            press(field, KeyCode.TAB, false);
            check(field.getText().equals("member view nus/A0123456X"), "Reference ID must complete");
            check(logic.commands == 0 && storage.saves == 0, "Completion must remain read-only");
            press(field, KeyCode.ENTER, false);
            check(field.getText().isEmpty(), "Enter must submit successfully");
            check(logic.commands == 1 && storage.saves == 1, "Enter must execute and save exactly once");
            session.add("ENTER: member view nus/A0123456X; commands=1, saves=1, input cleared.");
            field.setText("member ed");
            field.positionCaret(field.getLength());
            press(field, KeyCode.TAB, false);
            field.appendText(" nus/A0123456X phone/98");
            field.positionCaret(field.getLength());
            press(field, KeyCode.TAB, false);
            check(field.getText().equals("member edit nus/A0123456X phone/98765432"),
                    "Member editing must complete a member phone value");
            check(logic.commands == 1 && storage.saves == 1, "Edit completion must remain read-only");
            press(field, KeyCode.ENTER, false);
            check(field.getText().isEmpty(), "Completed member edit must submit successfully");
            check(logic.commands == 2 && storage.saves == 2, "Member edit must execute and save exactly once");
            check(model.findMember(ViewTestData.ALEX.getNusId()).orElseThrow().getPhone().value.equals("98765432"),
                    "Member edit must update the selected member");
            session.add("ENTER: member edit nus/A0123456X phone/98765432; commands=2, saves=2, input cleared.");
            try {
                check(model.getAddressBook().equals(storage.readAddressBook().orElseThrow()),
                        "Reloaded JSON must match the completed edit");
                session.add("RELOAD: JSON matches the live model after completed edit.");
            } catch (DataLoadingException failure) {
                throw new AssertionError("Could not reload packaged test data", failure);
            }
        } finally {
            window.dispose();
            window.getPrimaryStage().hide();
        }
    }

    private static void press(TextField field, KeyCode code, boolean shift) {
        field.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, shift, false, false, false));
        field.fireEvent(new KeyEvent(KeyEvent.KEY_RELEASED, "", "", code, shift, false, false, false));
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    /** Counts real JSON writes in the disposable packaged-test working directory. */
    private static class RecordingStorage extends StorageManager {
        private int saves;

        RecordingStorage() {
            super(new JsonAddressBookStorage(Path.of("data.json")),
                    new JsonUserPrefsStorage(Path.of("preferences.json")));
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook root) throws IOException {
            super.saveAddressBook(root);
            saves++;
        }
    }

    /** Counts dispatches through the real packaged parser and command pipeline. */
    private static class RecordingLogic extends LogicManager {
        private int commands;

        RecordingLogic(seedu.address.model.Model model, RecordingStorage storage) {
            super(model, storage);
        }

        @Override
        public CommandResult execute(String text) throws CommandException, ParseException {
            commands++;
            return super.execute(text);
        }
    }
}
