package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EQUIPMENT_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MEMBER_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTES;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NUS_ID;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.exceptions.ParseException;

public class LaplaceArgumentParserTest {

    @Test
    public void parse_requiredFields_acceptsEitherOrderAndTrimsValues() throws Exception {
        Set<Prefix> required = Set.of(PREFIX_MEMBER_NAME, PREFIX_NUS_ID);
        for (String input : new String[] {"name/Alex Tan nus/a0123456x", "\t nus/a0123456x\tname/ Alex Tan \t"}) {
            ArgumentMultimap result = LaplaceArgumentParser.parse(input, required, Set.of());
            assertEquals("Alex Tan", result.getValue(PREFIX_MEMBER_NAME).orElseThrow());
            assertEquals("a0123456x", result.getValue(PREFIX_NUS_ID).orElseThrow());
            assertEquals("", result.getPreamble());
            assertEquals(2, required.size());
        }
    }

    @Test
    public void parse_optionalNotes_acceptsOmittedEmptyOrTrimmedText() throws Exception {
        for (String input : new String[] {"", "notes/", "notes/ \t"}) {
            ArgumentMultimap result = LaplaceArgumentParser.parse(input, Set.of(), Set.of(PREFIX_NOTES));
            assertEquals("", result.getValue(PREFIX_NOTES).orElse(""));
            assertEquals(!input.isEmpty(), result.getValue(PREFIX_NOTES).isPresent());
        }
        ArgumentMultimap result = LaplaceArgumentParser.parse("notes/  camera body  ",
                Set.of(), Set.of(PREFIX_NOTES));
        assertEquals("camera body", result.getValue(PREFIX_NOTES).orElseThrow());
    }

    @Test
    public void parse_noParameters_acceptsOnlyWhitespace() throws Exception {
        for (String input : new String[] {"", " ", "\t \n"}) {
            assertEquals("", LaplaceArgumentParser.parse(input, Set.of(), Set.of()).getPreamble());
        }
        assertParseFailure("This command takes no arguments.", "extra", Set.of(), Set.of());
        assertParseFailure("This command takes no arguments.", "notes/", Set.of(), Set.of());
    }

    @Test
    public void parse_patronymicTokens_preservesLiteralTextAndRejectsOtherMarkers() throws Exception {
        for (String token : new String[] {"s/o", "S/O", "s/O", "d/o", "D/O", "D/o",
            "a/l", "A/L", "a/L", "a/p", "A/P", "A/p"}) {
            String name = "Ravi " + token + " Kumar";
            ArgumentMultimap result = LaplaceArgumentParser.parse("name/" + name + " nus/A0123456X",
                    Set.of(PREFIX_MEMBER_NAME, PREFIX_NUS_ID), Set.of());
            assertEquals(name, result.getValue(PREFIX_MEMBER_NAME).orElseThrow());
            assertEquals("A0123456X", result.getValue(PREFIX_NUS_ID).orElseThrow());
            assertEquals(token, LaplaceArgumentParser.parse("notes/" + token, Set.of(), Set.of(PREFIX_NOTES))
                    .getValue(PREFIX_NOTES).orElseThrow());
        }
        for (String suffix : new String[] {"only", "oKumar", "o/", "o,"}) {
            assertParseFailure("Unknown parameter: s/", "name/Ravi s/" + suffix,
                    Set.of(PREFIX_MEMBER_NAME), Set.of());
        }
        assertParseFailure("Unknown parameter: w/", "notes/Use w/ XLR cable", Set.of(), Set.of(PREFIX_NOTES));
        assertParseFailure("Repeated parameter: name/", "name/Ravi s/o Kumar name/Other",
                Set.of(PREFIX_MEMBER_NAME), Set.of());
    }

    @Test
    public void parse_missingOrBlankRequired_rejectsInput() {
        assertParseFailure("Missing required parameter: nus/", "", Set.of(PREFIX_NUS_ID), Set.of());
        for (String input : new String[] {"nus/", "nus/ \t"}) {
            assertParseFailure("Required parameter must not be blank: nus/", input, Set.of(PREFIX_NUS_ID), Set.of());
        }
        assertParseFailure("Required parameter must not be blank: name/", "name/\tnus/A0123456X",
                Set.of(PREFIX_MEMBER_NAME, PREFIX_NUS_ID), Set.of());
    }

    @Test
    public void parse_repeatedRequiredAndOptional_rejectsBoth() {
        assertParseFailure("Repeated parameter: nus/", "nus/A0123456X\tnus/A0234567X",
                Set.of(PREFIX_NUS_ID), Set.of());
        assertParseFailure("Repeated parameter: notes/", "notes/ notes/", Set.of(), Set.of(PREFIX_NOTES));
    }

    @Test
    public void parse_unknownMarkersInsideFreeText_rejectsInput() {
        for (String marker : new String[] {"unknown/", "extra-1/", "extra_2/", "Name/"}) {
            assertParseFailure("Unknown parameter: " + marker, "notes/camera\t" + marker + "value",
                    Set.of(), Set.of(PREFIX_NOTES));
        }
        assertParseFailure("Unknown parameter: unknown/", "unknown/value", Set.of(), Set.of(PREFIX_NOTES));
    }

    @Test
    public void parse_preamble_rejectsTextBeforeFirstMarker() {
        assertParseFailure("Unexpected text before parameters.", "Alex name/Alex",
                Set.of(PREFIX_MEMBER_NAME), Set.of());
        assertParseFailure("Unexpected text before parameters.", "not a marker", Set.of(PREFIX_MEMBER_NAME), Set.of());
    }

    @Test
    public void parse_urlsAndEmbeddedSlashes_preservesText() throws Exception {
        String value = "https://example.com/path?q=x lens(camera/body) /shelf/1 C:/equipment";
        ArgumentMultimap result = LaplaceArgumentParser.parse("notes/" + value, Set.of(), Set.of(PREFIX_NOTES));
        assertEquals(value, result.getValue(PREFIX_NOTES).orElseThrow());
    }

    @Test
    public void parse_markersRequireBoundary_preservesUndelimitedText() throws Exception {
        ArgumentMultimap result = LaplaceArgumentParser.parse("name/Alexnotes/text",
                Set.of(PREFIX_MEMBER_NAME), Set.of());
        assertEquals("Alexnotes/text", result.getValue(PREFIX_MEMBER_NAME).orElseThrow());
    }

    @Test
    public void parse_reservedRecognizedMarkersInsideNotes_areParameters() throws Exception {
        ArgumentMultimap result = LaplaceArgumentParser.parse("notes/camera name/Alex",
                Set.of(PREFIX_MEMBER_NAME), Set.of(PREFIX_NOTES));
        assertEquals("camera", result.getValue(PREFIX_NOTES).orElseThrow());
        assertEquals("Alex", result.getValue(PREFIX_MEMBER_NAME).orElseThrow());
    }

    @Test
    public void parse_diagnostics_haveStableOrdering() {
        Set<Prefix> required = Set.of(PREFIX_NUS_ID, PREFIX_MEMBER_NAME);
        assertParseFailure("Missing required parameter: name/", "", required, Set.of());
        assertParseFailure("Unexpected text before parameters.", "preamble unknown/x", required, Set.of());
        assertParseFailure("Unknown parameter: unknown/", "nus/x unknown/y nus/z", required, Set.of());
        assertParseFailure("Repeated parameter: nus/", "nus/x nus/z unknown/y", required, Set.of());
    }

    @Test
    public void parse_nullInputSetsAndEntries_rejectsProgrammerMisuse() {
        assertThrows(NullPointerException.class, () -> LaplaceArgumentParser.parse(null, Set.of(), Set.of()));
        assertThrows(NullPointerException.class, () -> LaplaceArgumentParser.parse("", null, Set.of()));
        assertThrows(NullPointerException.class, () -> LaplaceArgumentParser.parse("", Set.of(), null));
        Set<Prefix> nullEntry = new HashSet<>();
        nullEntry.add(null);
        assertThrows(NullPointerException.class, () -> LaplaceArgumentParser.parse("", nullEntry, Set.of()));
        assertThrows(NullPointerException.class, () -> LaplaceArgumentParser.parse("", Set.of(), nullEntry));
        assertThrows(NullPointerException.class, () ->
                LaplaceArgumentParser.parse("", Set.of(new Prefix(null)), Set.of()));
    }

    @Test
    public void parse_overlappingAliases_rejectsProgrammerMisuse() {
        assertSame(PREFIX_MEMBER_NAME, PREFIX_EQUIPMENT_NAME);
        assertThrows(IllegalArgumentException.class, () ->
                LaplaceArgumentParser.parse("name/Camera", Set.of(PREFIX_MEMBER_NAME), Set.of(PREFIX_EQUIPMENT_NAME)));
        assertThrows(IllegalArgumentException.class, () ->
                LaplaceArgumentParser.parse("", Set.of(new Prefix("name/")), Set.of(new Prefix("name/"))));
    }

    @Test
    public void parse_invalidPrefixDeclarations_rejectsProgrammerMisuse() {
        for (String token : new String[] {"", "1name/", "bad prefix/", "name", "https://"}) {
            assertThrows(IllegalArgumentException.class, () ->
                    LaplaceArgumentParser.parse("", Set.of(new Prefix(token)), Set.of()));
            assertThrows(IllegalArgumentException.class, () ->
                    LaplaceArgumentParser.parse("", Set.of(), Set.of(new Prefix(token))));
        }
    }

    @Test
    public void parse_returnsCompatibleMultimapWithoutChangingInputSets() throws Exception {
        Set<Prefix> required = new HashSet<>(Set.of(PREFIX_NUS_ID));
        Set<Prefix> optional = new HashSet<>(Set.of(PREFIX_NOTES));
        ArgumentMultimap result = LaplaceArgumentParser.parse("nus/a0123456x", required, optional);
        assertEquals("A0123456X", ParserUtil.parseNusId(result.getValue(PREFIX_NUS_ID).orElseThrow()).value);
        result.verifyNoDuplicatePrefixesFor(PREFIX_NUS_ID, PREFIX_NOTES);
        result.getAllValues(PREFIX_NUS_ID).clear();
        assertTrue(result.getValue(PREFIX_NUS_ID).isPresent());
        assertEquals(Set.of(PREFIX_NUS_ID), required);
        assertEquals(Set.of(PREFIX_NOTES), optional);
    }

    private void assertParseFailure(String message, String args, Set<Prefix> required, Set<Prefix> optional) {
        assertThrows(ParseException.class, message, () -> LaplaceArgumentParser.parse(args, required, optional));
    }
}
