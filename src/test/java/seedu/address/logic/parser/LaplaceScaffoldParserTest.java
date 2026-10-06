package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ASSIGNED_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CATEGORY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CONDITION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EQUIPMENT_UUID;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EXPECTED_RETURN_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MEMBER_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MEMBER_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MEMBER_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTES;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NUS_ID;
import static seedu.address.logic.parser.CliSyntax.PREFIX_RETURNED_DATE;

import org.junit.jupiter.api.Test;

public class LaplaceScaffoldParserTest {

    @Test
    public void prefixes_useContractTokens() {
        new CliSyntax();

        assertEquals("nus/", PREFIX_NUS_ID.toString());
        assertEquals("name/", PREFIX_MEMBER_NAME.toString());
        assertEquals("phone/", PREFIX_MEMBER_PHONE.toString());
        assertEquals("email/", PREFIX_MEMBER_EMAIL.toString());
        assertEquals("uuid/", PREFIX_EQUIPMENT_UUID.toString());
        assertEquals("category/", PREFIX_CATEGORY.toString());
        assertEquals("condition/", PREFIX_CONDITION.toString());
        assertEquals("notes/", PREFIX_NOTES.toString());
        assertEquals("assigned/", PREFIX_ASSIGNED_DATE.toString());
        assertEquals("expected/", PREFIX_EXPECTED_RETURN_DATE.toString());
        assertEquals("returned/", PREFIX_RETURNED_DATE.toString());
    }

}
