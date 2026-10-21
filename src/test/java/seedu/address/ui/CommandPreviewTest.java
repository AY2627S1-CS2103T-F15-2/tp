package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import seedu.address.testutil.FxTestUtil;

class CommandPreviewTest {
    @Test
    void hints_readyAndTemplate_keepApprovedWording() {
        FxTestUtil.runAndWait(() -> {
            CommandPreview ready = new CommandPreview("member list", false, unused -> { });
            CommandPreview template = new CommandPreview("member view nus/NUS_ID", true, unused -> { });
            assertEquals("Press Enter to execute (ready)", ((Label) ready.getChildren().getLast()).getText());
            Label templateHint = (Label) template.getChildren().getLast();
            assertEquals("Replace uppercase placeholders (template)", templateHint.getText());
        });
    }

    @Test
    void actions_preserveExactUnicodeCommandAndOnlyPrepareOnUse() {
        FxTestUtil.runAndWait(() -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            HashMap<DataFormat, Object> previous = new HashMap<>();
            for (DataFormat format : clipboard.getContentTypes()) {
                Object value = clipboard.getContent(format);
                if (value != null) {
                    previous.put(format, value);
                }
            }
            try {
                String command = "equipment add name/Caméra α category/Photo condition/GOOD notes/With  strap";
                List<String> prepared = new ArrayList<>();
                CommandPreview preview = new CommandPreview(command, false, prepared::add);
                Button copy = preview.lookupAll(".button").stream().map(node -> (Button) node)
                        .filter(button -> button.getText().equals("Copy")).findFirst().orElseThrow();
                copy.fire();
                assertEquals(command, clipboard.getString());
                assertEquals("Copied", copy.getText());
                assertTrue(prepared.isEmpty());
                preview.lookupAll(".button").stream().map(node -> (Button) node)
                        .filter(button -> button.getText().equals("Use command")).findFirst().orElseThrow().fire();
                assertEquals(List.of(command), prepared);
            } finally {
                clipboard.setContent(previous);
            }
        });
    }

    @Test
    void completion_commandNamesAndUnusedPrefixes_preservesValues() {
        assertEquals(List.of("equipment add "), CommandCatalog.complete("equipment ad"));
        assertEquals(List.of("equipment add name/Camera category/"),
                CommandCatalog.complete("equipment add name/Camera cat"));
        assertTrue(CommandCatalog.complete("equipment add name/Camera na").isEmpty());
        assertTrue(CommandCatalog.complete("equipment add notes/keep").isEmpty());
        assertTrue(CommandCatalog.complete("unknown").isEmpty());
    }
}
