package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.MemberAddCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;

public class MemberAddCommandParserTest {

    private static final String VALID = "nus/A0123456X name/Alex Tan phone/91234567 email/alex@example.com";
    private final MemberAddCommandParser parser = new MemberAddCommandParser();

    @Test
    public void parse_validReorderedWhitespaceAndLowercase_normalizesMember() throws Exception {
        MemberAddCommand expected = new MemberAddCommand(new Member(new NusId("A0123456X"),
                new Name("Alex Tan"), new Phone("91234567"), new Email("alex@example.com")));
        assertEquals(expected, parser.parse(VALID));
        assertEquals(expected, parser.parse("  email/alex@example.com\tphone/91234567 "
                + "name/ Alex Tan \tnus/ a0123456x  "));
    }

    @Test
    public void parse_missingBlankAndRepeatedFields_rejectsWithUsage() {
        List<String> fields = List.of("nus/A0123456X", "name/Alex Tan", "phone/91234567", "email/alex@example.com");
        for (String field : fields) {
            assertGrammarFailure(VALID.replace(field, ""));
            assertGrammarFailure(VALID.replace(field, field.substring(0, field.indexOf('/') + 1)));
            assertGrammarFailure(VALID + " " + field);
        }
        assertGrammarFailure("");
        assertGrammarFailure(" \t ");
    }

    @Test
    public void parse_unknownPrefixesLegacyAliasesAndPreamble_rejectsWithUsage() {
        for (String args : List.of("unexpected " + VALID, VALID + " notes/hello", VALID + " unknown-marker/value",
                VALID.replace("name/", "n/"), VALID.replace("phone/", "p/"), VALID.replace("email/", "e/"),
                VALID.replace("Alex Tan", "Alex rogue/value"))) {
            assertGrammarFailure(args);
        }
    }

    @Test
    public void parse_invalidValues_providesFieldConstraints() {
        assertFieldFailure(VALID.replace("A0123456X", "123"), NusId.MESSAGE_CONSTRAINTS);
        assertFieldFailure(VALID.replace("Alex Tan", "Alex!"), Name.MESSAGE_CONSTRAINTS);
        assertFieldFailure(VALID.replace("91234567", "phone"), Phone.MESSAGE_CONSTRAINTS);
        assertFieldFailure(VALID.replace("alex@example.com", "invalid"), Email.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_null_rejectsProgrammerMisuse() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    private void assertGrammarFailure(String args) {
        ParseException error = assertThrows(ParseException.class, () -> parser.parse(args));
        assertTrue(error.getMessage().contains(MemberAddCommand.MESSAGE_USAGE));
    }

    private void assertFieldFailure(String args, String constraints) {
        ParseException error = assertThrows(ParseException.class, () -> parser.parse(args));
        assertTrue(error.getMessage().contains(constraints));
    }
}
