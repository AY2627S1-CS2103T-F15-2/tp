package seedu.address.model.equipment;

import static java.util.Objects.requireNonNull;

import java.util.Locale;

/**
 * The physical condition of an equipment item.
 */
public enum Condition {
    GOOD,
    FAIR,
    DAMAGED,
    UNDER_REPAIR;

    /**
     * Returns the condition named by case-insensitive text after stripping surrounding whitespace.
     *
     * @throws IllegalArgumentException if the text is not a declared condition.
     * @throws NullPointerException if the text is null.
     */
    public static Condition fromText(String value) {
        return valueOf(requireNonNull(value).strip().toUpperCase(Locale.ROOT));
    }
}
