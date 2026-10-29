package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.logic.completion.CompletionService;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.equipment.Condition;
import seedu.address.testutil.ViewTestData;

/**
 * Checks agreement between return syntax, Tab completion and the shared Help/popup catalog.
 */
public class CommandCatalogTest {
    @Test
    public void complete_equipmentBrowseForms_matchesTabVocabularyAndParser() {
        String input = "equipment view ";
        Set<String> fields = CommandCatalog.complete(input).stream()
                .map(candidate -> candidate.substring(input.length())).collect(Collectors.toSet());
        assertEquals(Set.of("uuid/", "short/", "category/", "name/"), fields);
        assertEquals(fields, Set.copyOf(new CompletionService()
                .complete(input, input.length(), ViewTestData.model().getAddressBook()).orElseThrow().candidates()));
        assertDoesNotThrow(() -> new AddressBookParser().parseCommand("equipment view short/abcdef12"));
        assertDoesNotThrow(() -> new AddressBookParser().parseCommand("equipment view category/Photo name/Camera"));
    }

    @Test
    public void complete_returnCondition_offersUnusedPrefixAndPreservesEarlierFields() {
        String uuid = ViewTestData.CAMERA.getUuid().toString();
        for (String input : List.of("loans return co", "loans return uuid/" + uuid + " co")) {
            assertEquals(List.of(input.substring(0, input.length() - 2) + "condition/"),
                    CommandCatalog.complete(input));
        }
        assertTrue(CommandCatalog.complete("loans return condition/GOOD co").isEmpty());
        assertTrue(CommandCatalog.complete("loans return uuid/" + uuid + " uu").isEmpty());
        assertTrue(CommandCatalog.complete("loans return returned/2026-01-08 re").isEmpty());
    }

    @Test
    public void complete_returnFields_matchTabVocabularyAndAcceptedParserSyntax() {
        String input = "loans return ";
        Set<String> explicitFields = CommandCatalog.complete(input).stream()
                .map(candidate -> candidate.substring(input.length())).collect(Collectors.toSet());
        var tab = new CompletionService().complete(input, input.length(), ViewTestData.model().getAddressBook())
                .orElseThrow();
        assertEquals(Set.copyOf(tab.candidates()), explicitFields);
        AddressBookParser parser = new AddressBookParser();
        String command = input + "uuid/" + ViewTestData.CAMERA.getUuid() + " returned/2026-01-08";
        assertDoesNotThrow(() -> parser.parseCommand(command));
        for (Condition condition : Condition.values()) {
            assertDoesNotThrow(() -> parser.parseCommand(command + " condition/" + condition));
        }
    }
}
