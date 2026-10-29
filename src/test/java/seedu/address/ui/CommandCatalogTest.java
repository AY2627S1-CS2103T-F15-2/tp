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
        assertEquals(Set.of("uuid/", "category/", "name/"), fields);
        assertEquals(fields, Set.copyOf(new CompletionService()
                .complete(input, input.length(), ViewTestData.model().getAddressBook()).orElseThrow().candidates()));
        assertDoesNotThrow(() -> new AddressBookParser().parseCommand("equipment view category/Photo name/Camera"));
    }

    @Test
    public void complete_commandAliasesAndCase_offersCanonicalNamesWithoutChangingValues() {
        for (String[] inputs : List.of(new String[] {"MEM", "mem"}, new String[] {"members ", "member "},
                new String[] {"LOAN ", "loans "}, new String[] {"Members Ed", "member ed"},
                new String[] {"MODE DARK", "mode dark"}, new String[] {"UNDO", "undo"})) {
            assertTrue(!CommandCatalog.complete(inputs[0]).isEmpty());
            assertEquals(CommandCatalog.complete(inputs[1]), CommandCatalog.complete(inputs[0]));
        }
        assertEquals(List.of("member edit "), CommandCatalog.complete("MEMBERS EDIT"));
        assertEquals(List.of("MEMBERS EDIT name/MiXeD Name phone/"),
                CommandCatalog.complete("MEMBERS EDIT name/MiXeD Name ph"));
        for (String separator : List.of(" ", "\t", "\u2003", "\u00a0")) {
            String input = "Members" + separator + "Edit" + separator + "name/MiXeD Name" + separator;
            assertTrue(CommandCatalog.complete(input + "ph").contains(input + "phone/"));
            assertTrue(CommandCatalog.complete(input + "na").isEmpty());
        }
        assertTrue(CommandCatalog.complete("equipments ").isEmpty());
        assertTrue(CommandCatalog.complete("LOAN RETURNS ").isEmpty());
        assertTrue(CommandCatalog.complete("MEMBERS EDIT PH").isEmpty());
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
