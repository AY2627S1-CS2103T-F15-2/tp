package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.animation.PauseTransition;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

/**
 * Readable plain-text command with explicit copy and prepare actions; never executes a command.
 */
public class CommandPreview extends VBox {
    private static final Pattern COMMAND_NAME = Pattern.compile(
            "^(?:(?:member|equipment|loans|mode) \\w+|undo|redo|help|exit)");
    private static final Pattern PREFIX = Pattern.compile("(?<!\\S)[a-z]+/");

    /**
     * Creates a wrapping preview, identifying placeholders when this is a command template.
     */
    public CommandPreview(String command, boolean isTemplate, Consumer<String> onUse) {
        super(6);
        requireNonNull(command);
        requireNonNull(onUse);
        getStyleClass().add("command-preview");
        setMinWidth(0);
        TextFlow code = new TextFlow();
        code.setMinWidth(0);
        code.getStyleClass().add("command-code");
        Matcher matcher = PREFIX.matcher(command);
        int position = 0;
        Matcher commandName = COMMAND_NAME.matcher(command);
        if (commandName.find()) {
            Text name = new Text(commandName.group());
            name.getStyleClass().add("command-name");
            code.getChildren().add(name);
            position = commandName.end();
        }
        while (matcher.find()) {
            code.getChildren().add(new Text(command.substring(position, matcher.start())));
            Text prefix = new Text(matcher.group());
            prefix.getStyleClass().add("command-prefix");
            code.getChildren().add(prefix);
            position = matcher.end();
        }
        code.getChildren().add(new Text(command.substring(position)));
        Button use = new Button("Use command");
        use.setAccessibleText("Prepare command in the command input without executing");
        use.setOnAction(event -> onUse.accept(command));
        FlowPane actions = new FlowPane(8, 6, copyButton("Copy", command), use);
        Label hint = new Label(isTemplate ? "Replace uppercase placeholders (template)"
                : "Press Enter to execute (ready)");
        hint.setWrapText(true);
        hint.getStyleClass().add("laplace-muted");
        getChildren().addAll(code, actions, hint);
    }

    /**
     * Creates a copy action that preserves the exact value and confirms success briefly.
     */
    public static Button copyButton(String caption, String value) {
        requireNonNull(value);
        Button copy = new Button(caption);
        copy.setTooltip(new Tooltip(value));
        copy.setAccessibleText(caption);
        PauseTransition reset = new PauseTransition(Duration.seconds(2));
        reset.setOnFinished(event -> copy.setText(caption));
        copy.setOnAction(event -> {
            ClipboardContent content = new ClipboardContent();
            content.putString(value);
            if (Clipboard.getSystemClipboard().setContent(content)) {
                copy.setText("Copied");
                reset.playFromStart();
            }
        });
        return copy;
    }
}
