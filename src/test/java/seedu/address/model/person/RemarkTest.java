package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class RemarkTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Remark(null));
    }

    @Test
    public void constructor_anyText_preservesValue() {
        for (String text : new String[] {"", " ", "Likes baseball!", "备注 — café", "line one\nline two"}) {
            Remark remark = new Remark(text);
            assertEquals(text, remark.value);
            assertEquals(text, remark.toString());
        }
    }

    @Test
    public void equals() {
        Remark remark = new Remark("note");
        assertTrue(remark.equals(remark));
        assertEquals(remark, new Remark("note"));
        assertEquals(remark.hashCode(), new Remark("note").hashCode());
        assertFalse(remark.equals(null));
        assertFalse(remark.equals("note"));
        assertFalse(remark.equals(new Remark("other")));
    }
}
