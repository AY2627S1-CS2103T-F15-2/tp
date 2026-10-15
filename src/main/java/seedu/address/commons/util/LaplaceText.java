package seedu.address.commons.util;

import static java.util.Objects.requireNonNull;

/** Shared normalization for newly entered Laplace text; persisted legacy values are not rewritten. */
public final class LaplaceText {
    private LaplaceText() {
    }

    /** Trims Unicode whitespace and collapses each run, including line breaks, to an ordinary space. */
    public static String normalize(String value) {
        return requireNonNull(value).replaceAll("(?U)\\s+", " ").strip();
    }

    /** Counts Unicode code points rather than UTF-16 units, rejecting invisible/control and invalid characters. */
    public static boolean isPrintable(String value, int minimum, int maximum) {
        int length = value.codePointCount(0, value.length());
        return length >= minimum && length <= maximum && value.codePoints().allMatch(codePoint -> {
            int type = Character.getType(codePoint);
            return !Character.isISOControl(codePoint) && type != Character.FORMAT
                    && type != Character.SURROGATE && type != Character.UNASSIGNED;
        });
    }
}
