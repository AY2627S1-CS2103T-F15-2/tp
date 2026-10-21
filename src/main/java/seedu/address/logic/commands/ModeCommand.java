package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.Model;

/**
 * Changes the appearance preference without modifying or saving domain records.
 */
public class ModeCommand extends Command {
    public static final String COMMAND_WORD = "mode";
    public static final String MESSAGE_USAGE = "mode: Changes appearance. Usage: mode dark or mode light";
    private final boolean isDark;

    /**
     * Creates an appearance command for the specified mode.
     */
    public ModeCommand(boolean isDark) {
        this.isDark = isDark;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.setGuiSettings(model.getGuiSettings().withDarkMode(isDark));
        return new CommandResult("Appearance: " + (isDark ? "dark" : "light") + ".", DisplayRequest.unchanged());
    }
}
