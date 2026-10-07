package seedu.address.model.equipment;

import static java.util.Objects.requireNonNull;

/**
 * Calculates derived equipment availability from condition and current-loan state.
 * An open loan is represented by {@code hasOpenLoan}; this calculator does not read loan collections.
 */
public final class AvailabilityCalculator {

    private AvailabilityCalculator() {}

    /**
     * Calculates availability for an equipment item.
     * An open loan produces {@code ASSIGNED}. Otherwise {@code GOOD} and {@code FAIR} produce {@code AVAILABLE},
     * and {@code DAMAGED} and {@code UNDER_REPAIR} produce {@code UNAVAILABLE}.
     */
    public static Availability calculate(Condition condition, boolean hasOpenLoan) {
        requireNonNull(condition);
        if (hasOpenLoan) {
            return Availability.ASSIGNED;
        }
        if (condition == Condition.GOOD || condition == Condition.FAIR) {
            return Availability.AVAILABLE;
        }
        return Availability.UNAVAILABLE;
    }
}
