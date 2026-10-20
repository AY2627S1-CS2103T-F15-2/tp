package seedu.address.logic.completion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
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
        assertCandidates("member ", "add", "delete", "list", "view");
        assertCandidates("equipment ", "add", "delete", "list", "view");
        assertCandidates("loans ", "add", "list", "return");
        for (String word : List.of("add", "edit", "delete", "clear", "find", "list", "exit", "help",
                "member", "equipment", "loans")) {
            assertCandidates(word, word);
        }
        for (String input : List.of("MEM", "xyz", "member zz", "help ", "member unknown ", "member list ",
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
        assertEquals(List.of("add", "delete", "list", "view"), gap.candidates());
    }

    @Test
    public void complete_fieldPrefixes_usesOnlyUnusedCommandFields() {
        assertCandidates("member add na", "name/");
        assertCandidates("member add ", "email/", "name/", "nus/", "phone/");
        assertCandidates("member add name/Alex ", "email/", "nus/", "phone/");
        assertCandidates("equipment add ", "category/", "condition/", "name/", "notes/");
        assertCandidates("loans add ", "assigned/", "expected/", "nus/", "uuid/");
        assertCandidates("loans return ", "returned/", "uuid/");
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
    public void complete_liveData_afterDeletionAndAdditionReflectsCurrentRoot() {
        assertCandidates("equipment add name/", "Camera", "Speaker");
        model.deleteEquipment(ViewTestData.SPEAKER);
        assertCandidates("equipment add name/", "Camera");
        model.addEquipment(ViewTestData.SPEAKER);
        assertCandidates("equipment add name/", "Camera", "Speaker");
        assertEquals(2, model.getMemberList().size());
        assertEquals(3, model.getEquipmentList().size());
        assertTrue(model.getLoanList().isEmpty());
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
