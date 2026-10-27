package seedu.address.logic.completion;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.CliSyntax;
import seedu.address.logic.parser.Prefix;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.equipment.Equipment;
import seedu.address.testutil.ViewTestData;

/** Exercises contextual replacement, reference values, and unsupported-input boundaries. */
public class CompletionServiceTest {
    private final CompletionService service = new CompletionService();
    private final ModelManager model = ViewTestData.model();

    @Test
    public void complete_invalidOffsetsAndNullInputs_rejectsProgrammerErrors() {
        assertThrows(NullPointerException.class, () -> service.complete(null, 0, model.getAddressBook()));
        assertThrows(NullPointerException.class, () -> service.complete("mem", 3, null));
        assertThrows(IllegalArgumentException.class, () -> service.complete("mem", -1, model.getAddressBook()));
        assertThrows(IllegalArgumentException.class, () -> service.complete("mem", 4, model.getAddressBook()));
        for (String text : List.of("", " ", "   mem")) {
            assertTrue(service.complete(text, 0, model.getAddressBook()).isEmpty());
        }
        assertTrue(service.complete("   mem", 2, model.getAddressBook()).isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> proposal("mem").candidates().add("other"));
    }

    @Test
    public void complete_commandWordsAndSubcommands_usesParserSpellingAndStableOrder() {
        assertCandidates("mem", "member");
        assertCandidates("member ad", "add");
        assertCandidates("member ", "add", "delete", "edit", "list", "restore", "search", "view");
        assertCandidates("equipment ", "add", "delete", "edit", "list", "restore", "search", "view");
        assertCandidates("loans ", "add", "list", "return");
        for (String word : List.of("exit", "help",
                "member", "equipment", "loans")) {
            assertCandidates(word, word);
        }
        for (String input : List.of("add", "edit", "delete", "clear", "find", "list", "MEM", "xyz",
                "member zz", "help ", "member unknown ", "member list ",
                "equipment list ", "loans list ")) {
            assertTrue(service.complete(input, input.length(), model.getAddressBook()).isEmpty(), input);
        }
        Completion middle = service.complete("  mex suffix", 4, model.getAddressBook()).orElseThrow();
        assertEquals(2, middle.start());
        assertEquals(5, middle.end());
        assertEquals(List.of("member"), middle.candidates());
        Completion gap = service.complete("member   list", 7, model.getAddressBook()).orElseThrow();
        assertEquals(7, gap.start());
        assertEquals(7, gap.end());
        assertEquals(List.of("add", "delete", "edit", "list", "restore", "search", "view"), gap.candidates());
    }

    @Test
    public void complete_fieldPrefixes_usesOnlyUnusedCommandFields() {
        assertCandidates("member add na", "name/");
        assertCandidates("member add ", "email/", "name/", "nus/", "phone/");
        assertCandidates("member add name/Alex ", "email/", "nus/", "phone/");
        assertCandidates("equipment add ", "category/", "condition/", "count/", "name/", "notes/");
        assertCandidates("loans add ", "assigned/", "expected/", "nus/", "uuid/");
        assertCandidates("loans return ", "condition/", "returned/", "uuid/");
        assertCandidates("member view n", "nus/");
        assertCandidates("member delete n", "nus/");
        assertCandidates("equipment view u", "uuid/");
        assertCandidates("equipment delete u", "uuid/");
        String text = "member add name/Alex phone/91234567";
        Completion prefix = service.complete(text, "member add na".length(), model.getAddressBook()).orElseThrow();
        assertEquals("member add name/Alex phone/91234567", apply(text, prefix));
        for (String invalid : List.of("member add unknown/x", "member add name/A name/B",
                "member add name/A na", "member add phone/9 ph", "member add NA")) {
            assertTrue(service.complete(invalid, invalid.length(), model.getAddressBook()).isEmpty(), invalid);
        }
    }

    @Test
    public void complete_values_usesStoredSpellingAndDeduplicatesMultiwordNames() {
        assertCandidates("member view nus/a0", "A0123456X", "A0234567R");
        assertCandidates("member delete nus/A012", "A0123456X");
        assertCandidates("loans add nus/a", "A0123456X", "A0234567R");
        assertCandidates("member add name/alex t", "Alex Tan");
        assertCandidates("member add name/", "Alex Tan");
        assertCandidates("member add phone/9", "91234567", "98765432");
        assertCandidates("member add email/ALEX", "alex@example.com");
        assertCandidates("equipment add name/ca", "Camera");
        assertCandidates("equipment add category/pho", "Photography");
        assertCandidates("equipment add condition/g", "GOOD");
        assertCandidates("equipment add condition/", "DAMAGED", "FAIR", "GOOD", "UNDER_REPAIR");
        String uuid = ViewTestData.CAMERA.getUuid().toString();
        for (String command : List.of("equipment view", "equipment delete", "loans add", "loans return")) {
            assertCandidates(command + " uuid/" + uuid, uuid);
        }
        for (String input : List.of("member add nus/A", "loans add assigned/2026", "loans add expected/2026",
                "loans return returned/2026", "equipment add notes/Keep", "member add name/Nobody")) {
            assertTrue(service.complete(input, input.length(), model.getAddressBook()).isEmpty(), input);
        }
        assertTrue(service.complete("member view nus/A", 17, new AddressBook()).isEmpty());
    }

    @Test
    public void complete_multiwordValueOverlapsPrefix_prefersStoredValue() {
        for (String word : List.of("Cover", "Case", "Notes")) {
            model.addEquipment(new Equipment(UUID.randomUUID(), "Camera " + word, "Photo " + word,
                    ViewTestData.CAMERA.getCondition(), ""));
            String fragment = word.substring(0, 2).toLowerCase();
            assertCandidates("equipment add name/Camera " + fragment, "Camera " + word);
            assertCandidates("equipment add category/Photo " + fragment, "Photo " + word);
        }
        String input = "equipment add name/Camera coJUNK   category/Photo condition/GOOD";
        Completion completion = service.complete(input, input.indexOf("JUNK"),
                model.getAddressBook()).orElseThrow();
        assertEquals("equipment add name/Camera Cover   category/Photo condition/GOOD", apply(input, completion));
    }

    @Test
    public void complete_noMatchingMultiwordValue_retainsPrefixCompletion() {
        assertCandidates("equipment add name/Camera co", "condition/", "count/");
        assertCandidates("equipment add category/Photo no", "notes/");
        assertCandidates("equipment add notes/Keep co", "condition/", "count/");
        assertCandidates("equipment add name/Camera ", "category/", "condition/", "count/", "notes/");
    }

    @Test
    public void complete_middleOfValue_preservesFollowingFieldsAndWhitespace() {
        String text = "member add name/  alex xx   phone/91234567";
        int caret = text.indexOf("alex") + 4;
        Completion completion = service.complete(text, caret, model.getAddressBook()).orElseThrow();
        assertEquals("member add name/  Alex Tan   phone/91234567", apply(text, completion));
        String second = "member add name/Unknown phone/91";
        assertCandidates(second, "91234567");
        String allFields = "member add   name/Alex nus/A0123456X phone/91234567 email/alex@example.com";
        assertTrue(service.complete(allFields, 11, model.getAddressBook()).isEmpty());
        String beforeMarker = "member add name/Alex phone/9";
        assertTrue(service.complete(beforeMarker, beforeMarker.indexOf("phone/") + 3,
                model.getAddressBook()).isPresent());
    }

    @Test
    public void complete_equivalentWhitespace_matchesValuesWithoutMovingReplacementBoundaries() {
        Equipment camera = ViewTestData.CAMERA;
        model.setEquipment(camera, new Equipment(camera.getUuid(), "Camera Cover", "Photo Cover",
                camera.getCondition(), camera.getNotes()));
        for (String separator : List.of("  ", "\u2003", "\u00a0", "\u202f", "\u0085", "\u3000", "\t")) {
            assertCandidates("member edit name/aLeX" + separator + "T", "Alex Tan");
            assertCandidates("member search q/alex" + separator + "t", "Alex Tan");
            assertCandidates("equipment add name/camera" + separator + "cov", "Camera Cover");
            assertCandidates("equipment add category/photo" + separator + "cov", "Photo Cover");

            String input = "member edit name/  aLeX" + separator + "TJUNK" + separator + "phone/91234567";
            Completion completion = service.complete(input, input.indexOf("JUNK"),
                    model.getAddressBook()).orElseThrow();
            assertEquals("member edit name/  ".length(), completion.start());
            assertEquals(input.indexOf("JUNK") + "JUNK".length(), completion.end());
            assertEquals("member edit name/  Alex Tan" + separator + "phone/91234567", apply(input, completion));
        }
    }

    @Test
    public void complete_unicodeFieldSeparators_preservesValuesPrefixesAndParserBoundaries() {
        for (String separator : List.of("\u2003", "\u00a0", "\u202f", "\u0085", "\u3000", "\t")) {
            String input = "member edit nus/A0123456X name/Alex" + separator + "phone/91234567"
                    + separator + "email/alex@example.com";
            int caret = input.indexOf("Alex") + 4;
            Completion completion = service.complete(input, caret, model.getAddressBook()).orElseThrow();
            String expected = input.replace("name/Alex", "name/Alex Tan");
            assertEquals(expected, apply(input, completion));
            assertDoesNotThrow(() -> new AddressBookParser().parseCommand(expected));
            assertTrue(service.complete(input, input.indexOf("phone/"), model.getAddressBook()).isEmpty());

            String prefix = "member edit name/Alex" + separator + "ph" + separator + "email/alex@example.com";
            Completion field = service.complete(prefix, prefix.indexOf("ph") + 2,
                    model.getAddressBook()).orElseThrow();
            assertEquals(prefix.replace("ph" + separator, "phone/" + separator), apply(prefix, field));
            String leading = "member add name/" + separator + "al" + separator + "phone/91234567";
            Completion value = service.complete(leading, leading.indexOf("al") + 2,
                    model.getAddressBook()).orElseThrow();
            assertEquals(leading.replace("al" + separator, "Alex Tan" + separator), apply(leading, value));
            String unknown = "member edit name/Alex" + separator + "unknown/value";
            assertTrue(service.complete(unknown, unknown.indexOf("Alex") + 4,
                    model.getAddressBook()).isEmpty());
        }
    }

    @Test
    public void complete_unicodeBatchSeparators_preservesSpacingAndExcludesNeighborIdentities() {
        String first = ViewTestData.CAMERA.getUuid().toString();
        String second = ViewTestData.OTHER_CAMERA.getUuid().toString();
        String third = ViewTestData.SPEAKER.getUuid().toString();
        for (String separator : List.of("\u2003", "\u00a0", "\u202f", "\u0085", "\u3000")) {
            assertCandidates("loans add uuid/" + first + separator + "," + separator, second, third);
            String input = "loans return uuid/" + first + separator + "," + separator + "00000000junk"
                    + separator + "," + separator + third + separator + "returned/2026-01-08";
            Completion completion = service.complete(input, input.indexOf("junk"),
                    model.getAddressBook()).orElseThrow();
            assertEquals(input.replace("00000000junk", second), apply(input, completion));
        }
    }

    @Test
    public void complete_caretAtExistingFieldStart_preservesFollowingMarkerAndValue() {
        String text = "member add name/Alex phone/91234567";
        int caret = text.indexOf("phone/");
        var completion = service.complete(text, caret, model.getAddressBook());
        String actual = completion.map(value -> apply(text, value)).orElse(text);
        assertEquals(text, actual);
        assertTrue(completion.isEmpty());
    }

    @Test
    public void complete_memberEdit_usesMergedCommandFieldsAndMemberValues() {
        assertCandidates("member ed", "edit");
        assertCandidates("member edit ", "email/", "name/", "nus/", "phone/");
        assertCandidates("member edit na", "name/");
        assertCandidates("member edit nus/a012", "A0123456X");
        assertCandidates("member edit name/al", "Alex Tan");
        assertCandidates("member edit phone/91", "91234567");
        assertCandidates("member edit email/ALEX", "alex@example.com");
        assertTrue(service.complete("member edit name/Cam", 20, model.getAddressBook()).isEmpty());
    }

    @Test
    public void complete_liveData_afterDeletionAndAdditionReflectsCurrentRoot() {
        assertCandidates("equipment add name/", "Camera", "Speaker");
        model.deleteEquipment(ViewTestData.SPEAKER);
        assertCandidates("equipment add name/", "Camera", "Speaker");
        assertTrue(model.findEquipment(ViewTestData.SPEAKER.getUuid()).orElseThrow().isDeleted());
        model.restoreEquipment(model.findEquipment(ViewTestData.SPEAKER.getUuid()).orElseThrow());
        assertCandidates("equipment add name/", "Camera", "Speaker");
        assertEquals(2, model.getMemberList().size());
        assertEquals(3, model.getEquipmentList().size());
        assertTrue(model.getLoanList().isEmpty());
    }

    @Test
    public void complete_routesAndFields_matchEveryCurrentFamilyParser() throws Exception {
        for (String family : List.of("Member", "Equipment", "Loans", "Mode")) {
            String source = Files.readString(Path.of("src/main/java/seedu/address/logic/parser/"
                    + family + "CommandParser.java"));
            Set<String> accepted = Pattern.compile("case \"([^\"]+)\"").matcher(source).results()
                    .map(match -> match.group(1)).collect(Collectors.toSet());
            assertEquals(accepted, Set.copyOf(proposal(family.toLowerCase() + " ").candidates()), family);
            if (family.equals("Mode")) {
                continue;
            }
            var routes = Pattern.compile("case \"([^\"]+)\" -> new ([A-Za-z]+)\\(").matcher(source);
            while (routes.find()) {
                String parserName = routes.group(2);
                if (parserName.equals("DirectorySearchCommandParser")) {
                    continue;
                }
                String leaf = Files.readString(Path.of("src/main/java/seedu/address/logic/parser/"
                        + parserName + ".java"));
                Set<String> symbols = Pattern.compile("PREFIX_[A-Z_]+").matcher(leaf).results()
                        .map(match -> match.group()).collect(Collectors.toSet());
                Set<String> fields = new HashSet<>();
                for (String symbol : symbols) {
                    fields.add(((Prefix) CliSyntax.class.getField(symbol).get(null)).getPrefix());
                }
                String input = family.toLowerCase() + " " + routes.group(1) + " ";
                List<String> candidates = service.complete(input, input.length(), model.getAddressBook())
                        .map(Completion::candidates).orElse(List.of());
                assertEquals(fields, Set.copyOf(candidates), input);
            }
        }
        assertCandidates("m", "member", "mode");
        assertCandidates("e", "equipment", "exit");
        assertCandidates("mode ", "dark", "light");
        assertCandidates("mode da", "dark");
        assertCandidates("mode li", "light");
        assertTrue(service.complete("mode DARK", 9, model.getAddressBook()).isEmpty());
        assertCandidates("h", "help");
        assertCandidates("l", "loans");
    }

    @Test
    public void complete_newMasterFieldsAndOptions_roundTripThroughActualParser() {
        assertCandidates("member restore ", "nus/");
        assertCandidates("equipment restore ", "uuid/");
        assertCandidates("equipment edit ", "category/", "condition/", "name/", "notes/", "uuid/");
        assertCandidates("member search ", "holding/", "order/", "q/", "sort/", "status/");
        assertCandidates("equipment search ", "availability/", "category/", "condition/", "order/", "q/",
                "sort/", "status/");
        assertCandidates("member restore nus/a012", "A0123456X");
        assertCandidates("equipment edit name/ca", "Camera");
        assertCandidates("equipment restore uuid/00000000-0000-0000-0000-000000000003",
                "00000000-0000-0000-0000-000000000003");
        assertCandidates("member search sort/", "DEFAULT", "HELD", "ID", "NAME");
        assertCandidates("equipment search sort/", "AVAILABILITY", "CATEGORY", "CONDITION", "DEFAULT", "ID", "NAME");
        assertCandidates("member search holding/", "ANY", "NO", "YES");
        assertCandidates("equipment search availability/", "ASSIGNED", "AVAILABLE", "UNAVAILABLE");
        assertCandidates("member search status/", "ACTIVE", "ALL", "DELETED");
        assertCandidates("equipment search order/", "asc", "desc");
        assertCandidates("member search status/de", "DELETED");
        assertCandidates("equipment search condition/un", "UNDER_REPAIR");
        AddressBookParser parser = new AddressBookParser();
        for (String command : List.of("member search", "equipment search")) {
            for (String prefix : proposal(command + " ").candidates()) {
                if (prefix.equals("q/") || prefix.equals("category/")) {
                    continue;
                }
                String input = command + " " + prefix;
                for (String value : proposal(input).candidates()) {
                    assertDoesNotThrow(() -> parser.parseCommand(input + value), input + value);
                }
            }
        }
        assertTrue(service.complete("equipment add count/2", 21, model.getAddressBook()).isEmpty());
    }

    @Test
    public void complete_searchQueries_reusesEverySearchableStoredField() {
        assertCandidates("member search q/alex t", "Alex Tan");
        assertCandidates("member search q/a012", "A0123456X");
        assertCandidates("member search q/91", "91234567");
        assertCandidates("member search q/other@", "other@example.com");
        assertCandidates("equipment search q/ca", "Camera");
        assertCandidates("equipment search q/pho", "Photography");
        assertCandidates("equipment search q/spare", "Spare battery included.");
        assertCandidates("equipment search q/00000000-0000-0000-0000-000000000001",
                "00000000-0000-0000-0000-000000000001");
        String text = "equipment search q/spare junk   status/deleted";
        Completion completion = service.complete(text, text.indexOf(" junk"), model.getAddressBook()).orElseThrow();
        assertEquals("equipment search q/Spare battery included.   status/deleted", apply(text, completion));
    }

    @Test
    public void complete_batchLoanIdentities_preservesNeighborsAndExcludesRepeatedEntries() {
        String first = ViewTestData.CAMERA.getUuid().toString();
        String second = ViewTestData.OTHER_CAMERA.getUuid().toString();
        String third = ViewTestData.SPEAKER.getUuid().toString();
        assertCandidates("loans add uuid/" + first + ",", second, third);
        assertCandidates("loans add uuid/" + first + ",  ", second, third);
        assertCandidates("loans return uuid/" + first + ", " + second + ",", third);
        String text = "loans return uuid/" + first + ", 00000000junk  , " + third + " returned/2026-01-08";
        int caret = text.indexOf("junk");
        Completion completion = service.complete(text, caret, model.getAddressBook()).orElseThrow();
        assertEquals("loans return uuid/" + first + ", " + second + "  , " + third + " returned/2026-01-08",
                apply(text, completion));
        String withLaterComma = "loans add uuid/00000000 nus/A0123456X,anything";
        assertTrue(service.complete(withLaterComma, "loans add uuid/00000000".length(),
                model.getAddressBook()).isPresent());
        assertCandidates("loans return condition/g", "GOOD");
        String exhausted = "loans return uuid/" + first + "," + second + "," + third + ",";
        assertTrue(service.complete(exhausted, exhausted.length(), model.getAddressBook()).isEmpty());
    }

    @Test
    public void complete_fieldAfterLoanIdentities_fallsBackToUnusedPrefix() {
        String first = ViewTestData.CAMERA.getUuid().toString();
        String second = ViewTestData.OTHER_CAMERA.getUuid().toString();
        for (String identities : List.of(first, first + ", " + second)) {
            assertCandidates("loans add uuid/" + identities + " nu", "nus/");
            assertCandidates("loans return uuid/" + identities + " re", "returned/");
            assertCandidates("loans return uuid/" + identities + " co", "condition/");
            String input = "loans add uuid/" + identities + " nuJUNK   expected/2026-01-08";
            Completion completion = service.complete(input, input.indexOf("JUNK"),
                    model.getAddressBook()).orElseThrow();
            assertEquals("loans add uuid/" + identities + " nus/   expected/2026-01-08", apply(input, completion));
        }
    }

    private Completion proposal(String text) {
        return service.complete(text, text.length(), model.getAddressBook()).orElseThrow();
    }

    private void assertCandidates(String text, String... expected) {
        assertEquals(List.of(expected), proposal(text).candidates(), text);
    }

    private String apply(String text, Completion completion) {
        return text.substring(0, completion.start()) + completion.candidates().getFirst()
                + text.substring(completion.end());
    }
}
