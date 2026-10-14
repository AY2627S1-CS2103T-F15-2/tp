package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
                "condition/good", "notes/See https://example.com/a/b");
        for (List<String> order : permutations(fields)) {
            assertEquals(new EquipmentAddCommand("Canon EOS-R50 (kit) / 2", "Photo & video", Condition.GOOD,
                    "See https://example.com/a/b"), parser.parse("\t" + String.join("\t", order) + "  "));
        }
    }

    @Test
    public void parse_optionalNotesAndConditionAliases_acceptsSupportedForms() throws Exception {
        for (String notes : List.of("", " notes/", " notes/  \t")) {
            assertEquals(new EquipmentAddCommand("Camera", "Photo", Condition.GOOD, ""),
                    parser.parse("name/Camera category/Photo condition/GOOD" + notes));
        }
        for (String alias : List.of("UNDER_REPAIR", "under-repair", "under repair")) {
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
