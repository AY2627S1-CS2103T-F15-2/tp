package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.EquipmentViewCommand;
import seedu.address.logic.parser.exceptions.ParseException;

public class EquipmentViewCommandParserTest {
    private final EquipmentViewCommandParser parser = new EquipmentViewCommandParser();

    @Test
    public void parse_validIdentityAndWhitespace_returnsExactCommand() throws Exception {
        for (String prefix : List.of("", " ", "\t", " \t ")) {
            assertEquals(new EquipmentViewCommand(UUID.fromString("550e8400-e29b-41d4-a716-446655440000")),
                    parser.parse(prefix + "uuid/ 550E8400-E29B-41D4-A716-446655440000 \t"));
        }
    }

    @Test
    public void parse_invalidArguments_reportsUsageAndCause() {
        for (String input : List.of("",
                " ",
                "\t",
                "uuid/",
                "uuid/  ",
                "uuid/invalid",
                "UUID/00000000-0000-0000-0000-000000000001",
                "00000000-0000-0000-0000-000000000001",
                "junk uuid/00000000-0000-0000-0000-000000000001",
                "uuid/00000000-0000-0000-0000-000000000001 uuid/00000000-0000-0000-0000-000000000001",
                "uuid/00000000-0000-0000-0000-000000000001\tuuid/",
                "uuid/00000000-0000-0000-0000-000000000001 notes/test",
                "uuid/00000000-0000-0000-0000-000000000001 extra-x/test",
                "uuid/00000000-0000-0000-0000-000000000001 a1/test",
                "uuid/00000000-0000-0000-0000-000000000001 tail",
                "uuid/00000000-0000-0000-0000-000000000001 name/Camera",
                "uuid/00000000-0000-0000-0000-00000000000g")) {
            ParseException error = assertThrows(ParseException.class, () -> parser.parse(input), input);
            assertTrue(error.getMessage().contains(EquipmentViewCommand.MESSAGE_USAGE), input);
            assertNotNull(error.getCause());
        }
    }

    @Test
    public void parse_abbreviatedJavaUuidSyntax_rejectsNonCanonicalInput() throws Exception {
        assertThrows(ParseException.class, () -> parser.parse("uuid/1-1-1-1-1"));
    }

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
