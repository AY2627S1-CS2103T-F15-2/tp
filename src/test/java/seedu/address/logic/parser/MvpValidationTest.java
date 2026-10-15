package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** MVP input boundaries, command-reading order, and exact compatibility of previously stored values. */
public class MvpValidationTest {
    @TempDir
    public Path directory;

    @Test
    public void memberNames_punctuationUnicodeWhitespaceAndLength_acceptsOnlySpecifiedValues() throws Exception {
        assertEquals("Anne-Marie O'Connor.", ParserUtil.parseMemberName("  Anne-Marie\t O'Connor.  ").fullName);
        assertEquals("李 小龙", ParserUtil.parseMemberName("李\u2003小龙").fullName);
        assertEquals("A".repeat(100), ParserUtil.parseMemberName("A".repeat(100)).fullName);
        for (String value : List.of("", "  ", "123", "Alex1", ". - '", "Alex/Name", "Alex\u0000Name",
                "A".repeat(101))) {
            assertThrows(ParseException.class, () -> ParserUtil.parseMemberName(value), value);
        }
    }

    @Test
    public void memberPhones_formatsAndBoundaries_normalizesOrRejects() throws Exception {
        assertEquals("+6591234567", ParserUtil.parseMemberPhone(" +65 9123-4567 ").value);
        assertEquals("12345678", ParserUtil.parseMemberPhone("12\t34-5678").value);
        assertEquals("+123456789012345", ParserUtil.parseMemberPhone("+123456789012345").value);
        for (String value : List.of("123", "1234567", "1234567890123456", "++12345678", "1234+5678",
                "+", "1234a678", "1234\u0000678")) {
            assertThrows(ParseException.class, () -> ParserUtil.parseMemberPhone(value), value);
        }
    }

    @Test
    public void memberEmails_asciiSyntaxAndLength_normalizesWithoutChangingLegacyValues() throws Exception {
        assertEquals("a.%_+-@example.com", ParserUtil.parseMemberEmail(" A.%_+-@EXAMPLE.COM ").value);
        String maximum = "a".repeat(248) + "@b.com";
        assertEquals(254, maximum.length());
        assertEquals(maximum, ParserUtil.parseMemberEmail(maximum).value);
        for (String value : List.of(maximum + "m", "a@b", "a@@b.com", "a b@c.com", "a@-b.com", "a@b-.com",
                "a@b..com", "a@b.com.", "é@b.com", "a@b_c.com", "@b.com", "a@")) {
            assertThrows(ParseException.class, () -> ParserUtil.parseMemberEmail(value), value);
        }
        assertEquals("UPPER@EXAMPLE.COM", ParserUtil.parseEmail("UPPER@EXAMPLE.COM").value);
        assertEquals("123", ParserUtil.parsePhone("123").value);
        assertEquals("Member 123", ParserUtil.parseName("Member 123").fullName);
    }

    @Test
    public void equipmentText_printableLimits_normalizesUnicodeAndNotes() throws Exception {
        assertEquals("Canon EOS R50", ParserUtil.parseEquipmentName(" Canon\t EOS  R50 "));
        assertEquals("😀".repeat(100), ParserUtil.parseEquipmentName("😀".repeat(100)));
        assertEquals("C".repeat(50), ParserUtil.parseCategory("C".repeat(50)));
        assertEquals("Keep lens cap on", ParserUtil.parseNotes(" Keep\r\n lens\tcap  on "));
        assertEquals("", ParserUtil.parseNotes(" \n\t "));
        assertEquals("n".repeat(300), ParserUtil.parseNotes("n".repeat(300)));
        for (String value : List.of("", " ", "n".repeat(101), "ab\u0000cd", "ab\u200bcd",
                "\u0000name", "name\u0000", "\ud800")) {
            assertThrows(ParseException.class, () -> ParserUtil.parseEquipmentName(value), value);
        }
        assertThrows(ParseException.class, () -> ParserUtil.parseCategory("c".repeat(51)));
        assertThrows(ParseException.class, () -> ParserUtil.parseNotes("n".repeat(301)));
        assertThrows(ParseException.class, () -> ParserUtil.parseNotes("note\u0000"));
        assertThrows(ParseException.class, () -> new EquipmentAddCommandParser()
                .parse("name/Camera category/Test condition/GOOD notes/" + "n".repeat(301)));
    }

    @Test
    public void equipmentNotes_fullCommandWithLineBreaks_normalizesAndPersistsWithoutJoiningWords() throws Exception {
        ModelManager model = new ModelManager();
        JsonAddressBookStorage disk = new JsonAddressBookStorage(directory.resolve("data.json"));
        LogicManager logic = new LogicManager(model,
                new StorageManager(disk, new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
        for (String separator : List.of("\n", "\r\n", "\r", "\t", "\u000b", "\f", "\u0085", "\u2028", "\u2029")) {
            logic.execute("equipment add name/Camera category/Test condition/GOOD notes/First line"
                    + separator + "Second line");
            assertEquals("First line Second line", model.getEquipmentList().getLast().getNotes());
        }
        assertEquals(model.getAddressBook(), disk.readAddressBook().orElseThrow());
        String saved = Files.readString(disk.getAddressBookFilePath());
        assertThrows(ParseException.class, () -> logic.execute(
                "equipment add name/Camera category/Test condition/GOOD notes/First line\nunknown/value"));
        assertEquals(saved, Files.readString(disk.getAddressBookFilePath()));
    }

    @Test
    public void feedback_realRoutes_reportsCurrentCountHolderAndDeletedNames() throws Exception {
        ModelManager model = new ModelManager();
        JsonAddressBookStorage disk = new JsonAddressBookStorage(directory.resolve("data.json"));
        LogicManager logic = new LogicManager(model,
                new StorageManager(disk, new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
        logic.execute("member add nus/A0000001X name/Alice Tan phone/91234567 email/alice@example.com");
        logic.execute("member add nus/A0000002X name/Bob Tan phone/98765432 email/bob@example.com");
        for (int item = 0; item < 3; item++) {
            logic.execute("equipment add name/Camera " + item + " category/Test condition/GOOD");
            UUID uuid = model.getEquipmentList().getLast().getUuid();
            logic.execute("loans add uuid/" + uuid
                    + " nus/A0000001X assigned/2026-01-01 expected/2026-01-02");
        }
        UUID first = model.getEquipmentList().getFirst().getUuid();
        UUID second = model.getEquipmentList().get(1).getUuid();
        UUID closed = model.getEquipmentList().getLast().getUuid();
        logic.execute("loans return uuid/" + closed + " returned/2026-01-02");
        String saved = Files.readString(disk.getAddressBookFilePath());
        assertEquals("Cannot delete A0000001X because the member holds 2 item(s). Return the items first.",
                assertThrows(CommandException.class, () -> logic.execute("member delete nus/A0000001X")).getMessage());
        assertEquals("Cannot delete " + first + " because it is assigned to A0000001X. Return the item first.",
                assertThrows(CommandException.class, () -> logic.execute("equipment delete uuid/" + first))
                        .getMessage());
        assertEquals("Equipment " + first + " is unavailable because it is assigned to A0000001X.",
                assertThrows(CommandException.class, () -> logic.execute("loans add uuid/" + first
                        + " nus/A0000002X assigned/2026-01-01 expected/2026-01-02")).getMessage());
        assertEquals(saved, Files.readString(disk.getAddressBookFilePath()));
        assertEquals(model.getAddressBook(), disk.readAddressBook().orElseThrow());
        logic.execute("loans return uuid/" + first + " returned/2026-01-02");
        assertEquals("Cannot delete A0000001X because the member holds 1 item(s). Return the items first.",
                assertThrows(CommandException.class, () -> logic.execute("member delete nus/A0000001X")).getMessage());
        logic.execute("loans return uuid/" + second + " returned/2026-01-02");
        assertEquals("Deleted member A0000001X: Alice Tan.",
                logic.execute("member delete nus/A0000001X").getFeedbackToUser());
        assertEquals("Deleted equipment " + first + ": Camera 0.",
                logic.execute("equipment delete uuid/" + first).getFeedbackToUser());
        assertTrue(model.hasMember(new NusId("A0000002X")));
        assertEquals(model.getAddressBook(), disk.readAddressBook().orElseThrow());
    }

    @Test
    public void identitiesConditionsAndDates_exactForms_rejectsAbbreviationsAndAliases() throws Exception {
        String uuid = "550e8400-e29b-41d4-a716-446655440000";
        assertEquals(uuid, ParserUtil.parseUuid(uuid.toUpperCase()).toString());
        assertEquals("A0123456X", ParserUtil.parseNusId(" a0123456x ").value);
        assertEquals(Condition.UNDER_REPAIR, ParserUtil.parseCondition("under_repair"));
        for (String value : List.of("under repair", "under-repair", "GOOD extra")) {
            assertThrows(ParseException.class, () -> ParserUtil.parseCondition(value));
        }
        for (String command : List.of("equipment view uuid/1-1-1-1-1", "equipment delete uuid/1-1-1-1-1",
                "loans add uuid/1-1-1-1-1 nus/A0000001X assigned/2026-01-01 expected/2026-01-01",
                "loans return uuid/1-1-1-1-1 returned/2026-01-01")) {
            assertTrue(assertThrows(ParseException.class, () -> new AddressBookParser().parseCommand(command))
                    .getMessage().startsWith("Invalid equipment UUID"));
        }
        assertEquals("2024-02-29", ParserUtil.parseDate("2024-02-29").toString());
        for (String value : List.of("2025-02-29", "2026-02-30", "2026-1-01", "0000-01-01", "2026-01-01Z")) {
            assertThrows(ParseException.class, () -> ParserUtil.parseDate(value));
        }
    }

    @Test
    public void parseUuid_canonicalBoundaries_acceptsCompleteHexadecimalValues() throws Exception {
        for (String value : List.of("00000000-0000-0000-0000-000000000000",
                "ffffffff-ffff-ffff-ffff-ffffffffffff", "550E8400-E29B-41D4-A716-446655440000")) {
            assertEquals(UUID.fromString(value), ParserUtil.parseUuid(" \t" + value + "\r\n"));
        }
    }

    @Test
    public void parseUuid_noncanonicalInputs_reportsValidationErrorBeforeConversion() {
        String canonical = "550e8400-e29b-41d4-a716-446655440000";
        for (String value : List.of("", "1-1-1-1-1", canonical.substring(1), canonical + "0",
                canonical.replace('e', 'g'), canonical.replace('-', '_'), canonical + "\nextra")) {
            ParseException failure = assertThrows(ParseException.class, () -> ParserUtil.parseUuid(value));
            assertEquals("Invalid equipment UUID. Use the UUID shown by Laplace.", failure.getMessage());
        }
        assertThrows(NullPointerException.class, () -> ParserUtil.parseUuid(null));
    }

    @Test
    public void firstError_reorderedFieldsAndStructuralErrors_followsReadingOrderBeforeMissingFields() {
        assertFirst("member add phone/123 nus/bad", Phone.MEMBER_MESSAGE_CONSTRAINTS);
        assertFirst("member add nus/bad phone/123", NusId.MESSAGE_CONSTRAINTS);
        assertFirst("member add name/123 phone/123", Name.MEMBER_MESSAGE_CONSTRAINTS);
        assertFirst("member add email/not-an-email name/123", Email.MEMBER_MESSAGE_CONSTRAINTS);
        assertFirst("member add phone/123 unknown/value", Phone.MEMBER_MESSAGE_CONSTRAINTS);
        assertFirst("member add unknown/value phone/123", "Unknown parameter: unknown/");
        assertFirst("member add phone/ phone/91234567 nus/bad", "Required parameter must not be blank: phone/");
        assertFirst("member add phone/91234567 phone/123 nus/bad", "Repeated parameter: phone/");
        assertFirst("member add name/Valid Name", "Missing required parameter: email/");
        assertFirst("member view nus/bad unknown/value", NusId.MESSAGE_CONSTRAINTS);
        assertFirst("member delete nus/bad nus/A0000001X", NusId.MESSAGE_CONSTRAINTS);
        assertFirst("equipment add condition/invalid name/", "Condition must be");
        assertFirst("equipment add notes/" + "n".repeat(301) + " condition/invalid", "Invalid notes");
        assertFirst("equipment view uuid/bad unknown/value", "Invalid equipment UUID");
        assertFirst("equipment delete uuid/bad uuid/bad", "Invalid equipment UUID");
        assertFirst("loans add assigned/2026-02-30 uuid/bad", "Date must be");
        assertFirst("loans add uuid/bad assigned/2026-02-30", "Invalid equipment UUID");
        assertFirst("loans return returned/2026-02-30 uuid/bad", "Date must be");
        assertFirst("loans return uuid/bad returned/2026-02-30", "Invalid equipment UUID");
        assertFirst("member list extra unknown/value", "Unexpected text");
        assertFirst("equipment list unknown/value extra", "Unknown parameter");
    }

    @Test
    public void saveReload_newNormalizedAndLegacyStoredValues_preservesBothWithoutRewritingOnRead() throws Exception {
        ModelManager model = new ModelManager();
        Member legacy = new Member(new NusId("A0000001X"), new Name("Member  123"), new Phone("123"),
                new Email("UPPER@EXAMPLE.COM"));
        model.addMember(legacy);
        Equipment old = new Equipment(new UUID(0, 1), "n".repeat(150), "c".repeat(60),
                Condition.GOOD, "n".repeat(500));
        model.addEquipment(old);
        JsonAddressBookStorage disk = new JsonAddressBookStorage(directory.resolve("data.json"));
        StorageManager storage = new StorageManager(disk, new JsonUserPrefsStorage(directory.resolve("prefs.json")));
        LogicManager logic = new LogicManager(model, storage);
        logic.execute("member add phone/+65 9123-4567 email/ANNE@EXAMPLE.COM name/Anne-Marie   Lim nus/A0000002X");
        Member added = model.findMember(new NusId("A0000002X")).orElseThrow();
        assertEquals("Anne-Marie Lim", added.getName().fullName);
        assertEquals("+6591234567", added.getPhone().value);
        assertEquals("anne@example.com", added.getEmail().value);
        String bytes = Files.readString(disk.getAddressBookFilePath());
        var restored = disk.readAddressBook().orElseThrow();
        assertEquals(model.getAddressBook(), restored);
        assertEquals(bytes, Files.readString(disk.getAddressBookFilePath()));
        assertTrue(restored.getMemberList().contains(legacy));
        assertTrue(restored.getEquipmentList().contains(old));
        logic.execute("add n/Legacy 123 p/123 e/UPPER@EXAMPLE.COM a/NUS");
        assertEquals("123", model.getFilteredPersonList().getFirst().getPhone().value);
        assertEquals("UPPER@EXAMPLE.COM", model.getFilteredPersonList().getFirst().getEmail().value);
        assertFalse(restored.getMemberList().isEmpty());
    }

    private void assertFirst(String command, String prefix) {
        ParseException failure = assertThrows(ParseException.class, () ->
                new AddressBookParser().parseCommand(command));
        assertTrue(failure.getMessage().startsWith(prefix), command + ": " + failure.getMessage());
    }
}
