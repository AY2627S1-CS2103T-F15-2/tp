package seedu.address.model.member;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class NusIdTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new NusId(null));
    }

    @Test
    public void constructor_invalidNusId_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new NusId(""));
        assertThrows(IllegalArgumentException.class, () -> new NusId("A123456"));
        assertThrows(IllegalArgumentException.class, () -> new NusId("AA1234567"));
        assertThrows(IllegalArgumentException.class, () -> new NusId("A1234567XY"));
    }

    @Test
    public void constructor_lowercaseNusId_normalizesToUppercase() {
        assertEquals("A0123456X", new NusId("a0123456x").value);
        assertEquals("E0123456", new NusId("e0123456").value);
    }

    @Test
    public void isValidNusId() {
        assertThrows(NullPointerException.class, () -> NusId.isValidNusId(null));

        assertFalse(NusId.isValidNusId(""));
        assertFalse(NusId.isValidNusId("A123456"));
        assertFalse(NusId.isValidNusId("11234567"));
        assertFalse(NusId.isValidNusId("A1234567XY"));

        assertTrue(NusId.isValidNusId("A0123456X"));
        assertTrue(NusId.isValidNusId("e0123456"));
    }

    @Test
    public void equals() {
        NusId nusId = new NusId("A0123456X");

        assertTrue(nusId.equals(nusId));
        assertTrue(nusId.equals(new NusId("a0123456x")));
        assertFalse(nusId.equals(null));
        assertFalse(nusId.equals("A0123456X"));
        assertFalse(nusId.equals(new NusId("A7654321X")));
        assertEquals(nusId.hashCode(), new NusId("a0123456x").hashCode());
        assertEquals("A0123456X", nusId.toString());
    }
}
