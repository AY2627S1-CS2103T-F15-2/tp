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
    public void forMember_patronymicTokens_preservesCaseAndRestrictsOtherSlashes() {
        for (String name : List.of("Ravi s/o Kumar", "Mary D/O Tan", "Ravi S/o Kumar",
                "Muthu a/l Raju", "Priya a/p Suresh", "Muthu A/L Raju", "Priya A/p Suresh")) {
            assertEquals(name, Name.forMember(name).fullName);
            assertEquals(name, Name.fromStoredMember(name).fullName);
            assertTrue(Name.isValidMemberName(name), name);
            assertThrows(IllegalArgumentException.class, () -> new Name(name));
        }
        assertEquals("Ravi s/o Kumar", Name.forMember(" Ravi\u2003s/o\u00a0Kumar ").fullName);
        assertEquals("Muthu a/l Raju", Name.forMember(" Muthu\u2003a/l\u00a0Raju ").fullName);
        assertEquals("A".repeat(94) + " s/o B", Name.forMember("A".repeat(94) + " s/o B").fullName);
        for (String name : List.of("Ravi s/only Kumar", "Ravi as/o Kumar", "Ravi s/oKumar", "Ravi / Kumar",
                "Ravi w/o Kumar", "Ravi s/o, Kumar", "A".repeat(95) + " s/o B",
                "Muthu a/long Raju", "Priya a/pSuresh", "Muthu aa/l Raju")) {
            assertFalse(Name.isValidMemberName(name), name);
            assertThrows(IllegalArgumentException.class, () -> Name.forMember(name));
        }
    }

    @Test
    public void forMember_patronymicConnectors_requireNamesOnBothSides() {
        for (String name : List.of("s/o", "D/O", "Tan s/o", "d/o Devi", "a/l", "A/P",
                "Muthu a/l", "a/p Suresh", "Ravi s/o d/o Kumar", "Ravi a/l ...", "- s/o Kumar")) {
            assertFalse(Name.isValidMemberName(name), name);
            assertThrows(IllegalArgumentException.class, () -> Name.forMember(name), name);
        }
    }

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
        for (String name : List.of("Jose\u0301  Tan", "அருண்", "Anne O\u2019Connor", "Member  123",
                "s/o", "D/O", "Tan s/o", "d/o Devi", "A".repeat(96) + " s/o")) {
            assertEquals(name, Name.fromStoredMember(name).fullName);
        }
        assertEquals("Member 123", new Name("Member 123").fullName);
        assertThrows(IllegalArgumentException.class, () -> new Name("Anne O\u2019Connor"));
    }
}
