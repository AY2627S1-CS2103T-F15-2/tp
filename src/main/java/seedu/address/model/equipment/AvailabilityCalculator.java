package seedu.address.model.equipment;

/**
 * Calculates derived equipment availability from condition and current-loan state.
 */
public final class AvailabilityCalculator {

    private AvailabilityCalculator() {}

    /**
     * Calculates availability for an equipment item.
     */
    public static Availability calculate(Condition condition, boolean hasOpenLoan) {
        throw new UnsupportedOperationException("Availability calculation is not implemented");
    }
}
