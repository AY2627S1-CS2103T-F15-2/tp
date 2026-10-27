package seedu.address.model.query;

import static java.util.Objects.requireNonNull;

import java.util.Locale;

import seedu.address.model.loan.Loan;

/**
 * Immutable, case-insensitive identity substrings shared by CLI and GUI loan filtering.
 */
public record LoanQuery(String memberText, String equipmentText) {
    /**
     * Trims surrounding whitespace without interpreting partial identities as complete domain values.
     */
    public LoanQuery {
        memberText = requireNonNull(memberText).strip();
        equipmentText = requireNonNull(equipmentText).strip();
    }

    /**
     * Returns criteria matching the complete open and closed history.
     */
    public static LoanQuery all() {
        return new LoanQuery("", "");
    }

    /**
     * Returns whether both identity substrings match a loan, regardless of its lifecycle state.
     */
    public boolean matches(Loan loan) {
        requireNonNull(loan);
        return loan.getMemberNusId().toString().toLowerCase(Locale.ROOT).contains(memberText.toLowerCase(Locale.ROOT))
                && loan.getEquipmentUuid().toString().toLowerCase(Locale.ROOT)
                        .contains(equipmentText.toLowerCase(Locale.ROOT));
    }
}
