package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class RemarkTest {

    @Test
    public void equals() {
        Remark remark = new Remark("Hello");

        assertTrue(remark.equals(remark));
        assertTrue(remark.equals(new Remark("Hello")));
        assertFalse(remark.equals(new Remark("Bye")));
        assertFalse(remark.equals(null));
        assertFalse(remark.equals("Hello"));
    }
}
