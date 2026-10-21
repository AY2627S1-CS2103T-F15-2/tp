package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.EquipmentAddCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.equipment.Condition;

public class EquipmentAddCommandParserTest {

    private final EquipmentAddCommandParser parser = new EquipmentAddCommandParser();

    @Test
    public void parse_allParameterOrdersAndTabs_preservesPunctuationAndNotes() throws Exception {
        List<String> fields = List.of("name/Canon EOS-R50 (kit) / 2", "category/Photo & video",
                "condition/good", "notes/See https://example.com/a/b", "count/3");
        for (List<String> order : permutations(fields)) {
            assertEquals(new EquipmentAddCommand("Canon EOS-R50 (kit) / 2", "Photo & video", Condition.GOOD,
                    "See https://example.com/a/b", 3), parser.parse("\t" + String.join("\t", order) + "  "));
        }
    }

    @Test
    public void parse_optionalNotesAndConditionAliases_acceptsSupportedForms() throws Exception {
        for (String notes : List.of("", " notes/", " notes/  \t")) {
            assertEquals(new EquipmentAddCommand("Camera", "Photo", Condition.GOOD, ""),
                    parser.parse("name/Camera category/Photo condition/GOOD" + notes));
        }
        for (String alias : List.of("UNDER_REPAIR", "under_repair")) {
            assertEquals(new EquipmentAddCommand("Camera", "Photo", Condition.UNDER_REPAIR, ""),
                    parser.parse("name/Camera category/Photo condition/" + alias));
        }
        for (Condition condition : Condition.values()) {
            assertEquals(new EquipmentAddCommand("Camera", "Photo", condition, ""),
                    parser.parse("name/Camera category/Photo condition/" + condition));
        }
    }

    @Test
    public void parse_missingBlankRepeatedUnknownOrPreamble_rejects() {
        List<String> required = List.of("name/Camera", "category/Photo", "condition/GOOD");
        for (int index = 0; index < required.size(); index++) {
            List<String> fields = new ArrayList<>(required);
            fields.remove(index);
            assertThrows(ParseException.class, () -> parser.parse(String.join(" ", fields)));
            fields.add(required.get(index).split("/")[0] + "/ \t");
            assertThrows(ParseException.class, () -> parser.parse(String.join(" ", fields)));
        }
        String valid = String.join(" ", required);
        for (String field : List.of("name/Other", "category/Other", "condition/FAIR", "notes/one notes/two")) {
            assertThrows(ParseException.class, () -> parser.parse(valid + " " + field));
        }
        for (String invalid : List.of("", " ", "preamble " + valid, valid + " unknown/value",
                valid + " notes/text reserved/value", valid + " Name/Other",
                "name/Camera category/Photo condition/BROKEN")) {
            assertThrows(ParseException.class, () -> parser.parse(invalid));
        }
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_countBoundariesAndDefault_acceptsValidCounts() throws Exception {
        String fields = "name/Camera category/Photo condition/GOOD";
        assertEquals(parser.parse(fields), parser.parse(fields + " count/1"));
        for (String count : List.of("2", "100", "003")) {
            assertEquals(new EquipmentAddCommand("Camera", "Photo", Condition.GOOD, "", Integer.parseInt(count)),
                    parser.parse(fields + " count/" + count));
        }
    }

    @Test
    public void parse_invalidOrRepeatedCount_reportsFirstErrorWithUsage() {
        for (String count : List.of("", "0", "-1", "+2", "1.5", "1e2", "101", "2147483648", "two", "1 2", "３")) {
            ParseException error = assertThrows(ParseException.class, () -> parser.parse(
                    "count/" + count + " name/Camera category/Photo condition/GOOD"));
            assertTrue(error.getMessage().startsWith(EquipmentAddCommand.MESSAGE_INVALID_COUNT));
            assertTrue(error.getMessage().contains(EquipmentAddCommand.MESSAGE_USAGE));
        }
        ParseException repeated = assertThrows(ParseException.class, () -> parser.parse("count/2 count/3"));
        assertTrue(repeated.getMessage().startsWith("Repeated parameter: count/"));
        ParseException first = assertThrows(ParseException.class, () -> parser.parse("count/0 unknown/value"));
        assertTrue(first.getMessage().startsWith(EquipmentAddCommand.MESSAGE_INVALID_COUNT));
        ParseException unknown = assertThrows(ParseException.class, () -> parser.parse("unknown/value count/0"));
        assertTrue(unknown.getMessage().startsWith("Unknown parameter: unknown/"));
    }

    private static List<List<String>> permutations(List<String> fields) {
        if (fields.isEmpty()) {
            return List.of(List.of());
        }
        List<List<String>> result = new ArrayList<>();
        for (String first : fields) {
            List<String> remaining = new ArrayList<>(fields);
            remaining.remove(first);
            for (List<String> tail : permutations(remaining)) {
                List<String> order = new ArrayList<>();
                order.add(first);
                order.addAll(tail);
                result.add(order);
            }
        }
        return result;
    }
}
