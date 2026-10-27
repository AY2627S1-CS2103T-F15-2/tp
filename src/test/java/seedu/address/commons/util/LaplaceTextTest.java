package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/** Checks Unicode text boundaries independently of individual command parsers. */
public class LaplaceTextTest {
    @Test
    public void normalize_unicodeWhitespace_preservesWordBoundaries() {
        assertEquals("First Second", LaplaceText.normalize("\u2003First\r\n\tSecond\u00a0"));
        assertEquals("", LaplaceText.normalize("\r\n\t\u2003"));
        assertThrows(NullPointerException.class, () -> LaplaceText.normalize(null));
    }

    @Test
    public void isPrintable_codePointBounds_countsSupplementaryCharactersOnce() {
        String camera = new String(Character.toChars(0x1f4f7));
        assertTrue(LaplaceText.isPrintable(camera, 1, 1));
        assertTrue(LaplaceText.isPrintable("", 0, 0));
        assertFalse(LaplaceText.isPrintable("", 1, 1));
        assertFalse(LaplaceText.isPrintable(camera + "A", 1, 1));
    }

    @Test
    public void isPrintable_invisibleOrInvalidUnicode_rejectsEveryExcludedCategory() {
        for (String value : List.of("\u0000", "\u200b", "\ud800", "\udc00", "\u0378")) {
            assertFalse(LaplaceText.isPrintable(value, 1, 1));
            assertFalse(LaplaceText.isPrintable("Camera" + value, 1, 100));
        }
        assertTrue(LaplaceText.isPrintable("Camera 李's #2", 1, 100));
    }

    @Test
    public void caseKey_contextAndSupplementaryLetters_comparesWithoutRewritingValues() {
        assertEquals(LaplaceText.caseKey("ΝΙΚΟΣ"), LaplaceText.caseKey("νικοσ"));
        assertEquals(LaplaceText.caseKey("Σ"), LaplaceText.caseKey("ς"));
        assertEquals("ascii", LaplaceText.caseKey("ASCII"));
        assertEquals(LaplaceText.caseKey("\uD801\uDC00"), LaplaceText.caseKey("\uD801\uDC28"));
        assertEquals("", LaplaceText.caseKey(""));
        assertThrows(NullPointerException.class, () -> LaplaceText.caseKey(null));
    }
}
