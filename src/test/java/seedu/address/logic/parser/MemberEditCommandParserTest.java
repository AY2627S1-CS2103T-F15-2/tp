package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.MemberEditCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.member.NusId;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;

/**
 * Checks strict edit grammar and the same normalization used when adding members.
 */
public class MemberEditCommandParserTest {
    private final MemberEditCommandParser parser = new MemberEditCommandParser();

    @Test
    public void parse_partialAndReorderedEdits_preservesOmittedFieldsAndNormalizesSuppliedValues() throws Exception {
        MemberEditCommand expected = new MemberEditCommand(new NusId("A0123456X"),
                Optional.of(Name.forMember("Anne-Marie O'Connor")), Optional.of(Phone.forMember("+6598765432")),
                Optional.of(Email.forMember("anne@example.com")));
        assertEquals(expected, parser.parse(" email/ANNE@EXAMPLE.COM phone/+65 9876-5432"
                + " nus/a0123456x name/ Anne-Marie  O'Connor "));
        assertEquals(new MemberEditCommand(new NusId("A0123456X"), Optional.empty(),
                Optional.of(Phone.forMember("98765432")), Optional.empty()),
                parser.parse("nus/A0123456X phone/98765432"));
        assertEquals(expected, new AddressBookParser().parseCommand("member edit nus/A0123456X"
                + " name/Anne-Marie O'Connor phone/+6598765432 email/anne@example.com"));
    }

    @Test
    public void parse_missingBlankDuplicateUnknownAndInvalidFields_rejectsWithUsage() {
        for (String args : List.of("", "nus/A0123456X", "name/Alex", "nus/invalid name/Alex",
                "nus/A0123456X name/", "nus/A0123456X phone/", "nus/A0123456X email/",
                "nus/A0123456X name/Alex name/Ben", "nus/A0123456X nus/A0234567R name/Alex",
                "nus/A0123456X phone/98765432 phone/91234567", "nus/A0123456X email/a@b.co email/b@c.co",
                "extra nus/A0123456X name/Alex", "nus/A0123456X n/Alex", "nus/A0123456X deleted/true",
                "nus/A0123456X name/Alex!", "nus/A0123456X phone/invalid", "nus/A0123456X email/invalid")) {
            ParseException error = assertThrows(ParseException.class, () -> parser.parse(args), args);
            assertTrue(error.getMessage().contains(MemberEditCommand.MESSAGE_USAGE), args);
        }
        assertThrows(NullPointerException.class, () -> parser.parse(null));
        assertThrows(ParseException.class, () -> new AddressBookParser().parseCommand("member EDIT nus/A0123456X"));
    }

    @Test
    public void parse_multipleInvalidFields_reportsFirstSuppliedInvalidValue() {
        ParseException error = assertThrows(ParseException.class, () ->
                parser.parse("phone/invalid name/Alex! nus/A0123456X"));
        assertTrue(error.getMessage().contains(Phone.MEMBER_MESSAGE_CONSTRAINTS));
    }
}
