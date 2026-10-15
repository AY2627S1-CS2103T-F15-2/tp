package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.EquipmentDeleteCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Verifies the single-UUID equipment deletion grammar and value validation.
 */
public class EquipmentDeleteCommandParserTest {

    private static final String UUID_TEXT = "550e8400-e29b-41d4-a716-446655440000";

    private final EquipmentDeleteCommandParser parser = new EquipmentDeleteCommandParser();

    @Test
    public void parse_caseAndWhitespace_resolvesSameUuid() throws Exception {
        EquipmentDeleteCommand expected = new EquipmentDeleteCommand(UUID.fromString(UUID_TEXT));
        assertEquals(expected, parser.parse("uuid/" + UUID_TEXT));
        assertThrows(ParseException.class, () -> parser.parse("uuid/1-1-1-1-1"));
        assertEquals(expected, parser.parse(" \tuuid/ 550E8400-E29B-41D4-A716-446655440000 \t "));
    }

    @Test
    public void parse_missingBlankRepeatedUnknownAndPreamble_rejectsWithUsageAndCause() {
        for (String args : List.of("", " \t ", "uuid/", "uuid/ \t", UUID_TEXT, "1",
                "unexpected uuid/" + UUID_TEXT, "uuid/" + UUID_TEXT + " uuid/" + UUID_TEXT,
                "uuid/" + UUID_TEXT + "\tuuid/" + UUID_TEXT, "uuid/" + UUID_TEXT + " name/Camera",
                "uuid/" + UUID_TEXT + " unknown-marker/value", "UUID/" + UUID_TEXT, "u/" + UUID_TEXT)) {
            ParseException error = assertThrows(ParseException.class, () -> parser.parse(args), args);
            assertTrue(error.getMessage().contains(EquipmentDeleteCommand.MESSAGE_USAGE), args);
            assertInstanceOf(ParseException.class, error.getCause());
        }
    }

    @Test
    public void parse_malformedUuid_providesFieldConstraints() {
        for (String value : List.of("invalid", UUID_TEXT + " extra", UUID_TEXT + "/extra", "g-1-1-1-1")) {
            ParseException error = assertThrows(ParseException.class, () -> parser.parse("uuid/" + value));
            assertTrue(error.getMessage().contains("UUID"));
        }
    }

    @Test
    public void parse_nullArguments_rejectsMisuse() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
