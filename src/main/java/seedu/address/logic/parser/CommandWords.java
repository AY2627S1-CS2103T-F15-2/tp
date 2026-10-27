package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.Locale;

/** Provides the explicit command-family vocabulary shared by routing and completion. */
public final class CommandWords {
    private CommandWords() {
    }

    /** Returns the canonical family spelling without modifying any parameter text. */
    public static String canonicalFamily(String word) {
        return switch (requireNonNull(word).toLowerCase(Locale.ROOT)) {
            case "members" -> "member";
            case "loan" -> "loans";
            default -> word.toLowerCase(Locale.ROOT);
        };
    }
}
