package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Checks Unicode member names without changing the separate legacy person-name rules.
 */
public class MemberNameTest {
    @Test
    public void forMember_unicodeNames_acceptsMarksAndTypographicApostrophes() {
        for (String name : List.of("அருண்", "Anne O\u2019Connor", "Anne-Marie O'Connor", "李明", "Alex Tan")) {
            assertEquals(name, Name.forMember(name).fullName);
            assertTrue(Name.isValidMemberName(name), name);
        }
        assertEquals("Jos\u00e9 Tan", Name.forMember(" \u2003Jose\u0301  Tan\u00a0").fullName);
        assertEquals(Name.forMember("Jos\u00e9 Tan"), Name.forMember("Jose\u0301 Tan"));
    }

    @Test
    public void forMember_lengthLimit_appliesAfterCanonicalComposition() {
        assertEquals("\u00e9".repeat(100), Name.forMember("e\u0301".repeat(100)).fullName);
        assertThrows(IllegalArgumentException.class, () -> Name.forMember("e\u0301".repeat(101)));
        assertEquals("A", Name.forMember(" A ").fullName);
    }

    @Test
    public void forMember_invalidCharactersAndUnattachedMarks_remainRejected() {
        for (String name : List.of("", " ", "\u0301", "\u0301Alex", "Alex \u0301Tan", "Anne-\u0301Marie",
                "...-'\u2019", "Alex2", "Alex@Tan", "Alex\u0000Tan", "Alex\u200bTan", "Alex\ud800Tan")) {
            assertThrows(IllegalArgumentException.class, () -> Name.forMember(name), name);
            assertFalse(Name.isValidMemberName(name), name);
        }
        assertThrows(NullPointerException.class, () -> Name.forMember(null));
    }

    @Test
    public void fromStoredMember_preservesSavedSpellingAndLegacyNames() {
        for (String name : List.of("Jose\u0301  Tan", "அருண்", "Anne O\u2019Connor", "Member  123")) {
            assertEquals(name, Name.fromStoredMember(name).fullName);
        }
        assertEquals("Member 123", new Name("Member 123").fullName);
        assertThrows(IllegalArgumentException.class, () -> new Name("Anne O\u2019Connor"));
    }
}
