package seedu.address.model.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.SearchData.item;
import static seedu.address.testutil.SearchData.member;
import static seedu.address.testutil.SearchData.sample;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.equipment.Availability;
import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;

public class DirectoryQueryTest {
    @Test
    public void members_formattedPhoneSubstring_matchesWithoutChangingOtherFields() {
        ModelManager model = sample();
        AddressBook before = new AddressBook(model.getAddressBook());
        for (String text : List.of("91234567", "9123 4567", "9123-4567", " 9123\u00a04567 ", "23-45")) {
            assertEquals(List.of(member(1), member(2)), query(text, "", null, null,
                    DirectoryQuery.Holding.ANY, DirectoryQuery.Sort.ID, false).members(model.getAddressBook()), text);
        }
        assertEquals(List.of(member(1)), query("9123-4567", "", null, null,
                DirectoryQuery.Holding.YES, DirectoryQuery.Sort.ID, false).members(model.getAddressBook()));
        for (String text : List.of("-", " - - ", "9123x4567", "alex tan@example.com", "alex-tan")) {
            assertTrue(query(text, "", null, null, DirectoryQuery.Holding.ANY,
                    DirectoryQuery.Sort.ID, false).members(model.getAddressBook()).isEmpty(), text);
        }
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void members_phoneFormatting_preservesNameAndEmailPunctuationAndInternationalPrefix() {
        AddressBook data = new AddressBook();
        Member member = new Member(new NusId("A8400001X"), Name.forMember("Anne-Marie Lim"),
                Phone.forMember("+65 9123-4567"), Email.forMember("anne-marie@example.com"));
        data.addMember(member);
        for (String text : List.of("+65 9123-4567", "65-9123", "Anne-Marie", "anne-marie@", "Marie Lim")) {
            assertEquals(List.of(member), query(text, "", null, null, DirectoryQuery.Holding.ANY,
                    DirectoryQuery.Sort.DEFAULT, false).members(data), text);
        }
        assertTrue(query("AnneMarie", "", null, null, DirectoryQuery.Holding.ANY,
                DirectoryQuery.Sort.DEFAULT, false).members(data).isEmpty());
        ModelManager equipmentModel = new ModelManager();
        equipmentModel.addEquipment(new Equipment(new UUID(0, 1), "91234567", "Numbers", Condition.GOOD, ""));
        assertTrue(query("9123-4567", "", null, null, DirectoryQuery.Holding.ANY,
                DirectoryQuery.Sort.DEFAULT, false).equipment(equipmentModel.getAddressBook(),
                        equipmentModel::getAvailability).isEmpty());
    }

    @Test
    public void members_greekSigmaVariants_matchesWithoutChangingStoredSpelling() {
        AddressBook data = new AddressBook();
        Member member = new Member(new NusId("A8400001X"), Name.forMember("ΝΙΚΟΣ"),
                Phone.forMember("91234567"), Email.forMember("nikos@example.com"));
        data.addMember(member);
        for (String text : List.of("ΝΙΚΟΣ", "Σ", "σ", "ς", "νικοσ", "νικος")) {
            assertEquals(List.of(member), query(text, "", null, null, DirectoryQuery.Holding.ANY,
                    DirectoryQuery.Sort.DEFAULT, false).members(data), text);
        }
        assertEquals("ΝΙΚΟΣ", data.getMemberList().getFirst().getName().fullName);
    }

    @Test
    public void equipment_greekSigmaCategoryAndText_matchesWithoutChangingStoredSpelling() {
        ModelManager model = new ModelManager();
        Equipment item = new Equipment(new UUID(0, 1), "ΟΣ", "ΝΙΚΟΣ", Condition.GOOD, "");
        model.addEquipment(item);
        assertEquals(List.of(item), query("σ", "νικοσ", null, null, DirectoryQuery.Holding.ANY,
                DirectoryQuery.Sort.DEFAULT, false).equipment(model.getAddressBook(), model::getAvailability));
        assertEquals("ΝΙΚΟΣ", item.getCategory());
    }

    @Test
    public void members_partialIdentityContactAndHolding_combinesWithoutMutating() {
        ModelManager model = sample();
        AddressBook before = new AddressBook(model.getAddressBook());
        assertEquals(List.of(member(1), member(2)), query(" aLeX ", "", null, null,
                DirectoryQuery.Holding.ANY, DirectoryQuery.Sort.DEFAULT, false).members(model.getAddressBook()));
        assertEquals(List.of(member(2)), query("a0000002", "", null, null,
                DirectoryQuery.Holding.NO, DirectoryQuery.Sort.ID, false).members(model.getAddressBook()));
        assertEquals(List.of(member(1)), query("9123", "", null, null,
                DirectoryQuery.Holding.YES, DirectoryQuery.Sort.HELD, true).members(model.getAddressBook()));
        assertEquals(2, query("example.com", "", null, null, DirectoryQuery.Holding.ANY,
                DirectoryQuery.Sort.NAME, false).members(model.getAddressBook()).size());
        assertTrue(query("missing", "", null, null, DirectoryQuery.Holding.ANY,
                DirectoryQuery.Sort.NAME, false).members(model.getAddressBook()).isEmpty());
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void equipment_filtersAndSorts_retainsIdenticalPhysicalUnitsAndStableTies() {
        ModelManager model = sample();
        AddressBook before = new AddressBook(model.getAddressBook());
        for (DirectoryQuery.Sort sort : List.of(DirectoryQuery.Sort.DEFAULT, DirectoryQuery.Sort.NAME,
                DirectoryQuery.Sort.ID, DirectoryQuery.Sort.CATEGORY, DirectoryQuery.Sort.CONDITION,
                DirectoryQuery.Sort.AVAILABILITY)) {
            List<Equipment> ascending = query("", "", null, null, DirectoryQuery.Holding.ANY, sort, false)
                    .equipment(model.getAddressBook(), model::getAvailability);
            List<Equipment> descending = query("", "", null, null, DirectoryQuery.Holding.ANY, sort, true)
                    .equipment(model.getAddressBook(), model::getAvailability);
            assertEquals(ascending.reversed(), descending);
            assertEquals(3, ascending.size());
        }
        assertEquals(List.of(item(2, Condition.GOOD)), query("CAM", " photo ", Condition.GOOD,
                Availability.AVAILABLE, DirectoryQuery.Holding.ANY, DirectoryQuery.Sort.ID, false)
                .equipment(model.getAddressBook(), model::getAvailability));
        assertEquals(1, query("000000000001", "", null, Availability.ASSIGNED,
                DirectoryQuery.Holding.ANY, DirectoryQuery.Sort.DEFAULT, false)
                .equipment(model.getAddressBook(), model::getAvailability).size());
        assertEquals(3, query("travel", "", null, null, DirectoryQuery.Holding.ANY,
                DirectoryQuery.Sort.DEFAULT, false).equipment(model.getAddressBook(), model::getAvailability).size());
        assertTrue(query("", "audio", null, null, DirectoryQuery.Holding.ANY,
                DirectoryQuery.Sort.DEFAULT, false)
                .equipment(model.getAddressBook(), model::getAvailability).isEmpty());
        assertTrue(query("", "", Condition.FAIR, null, DirectoryQuery.Holding.ANY,
                DirectoryQuery.Sort.DEFAULT, false)
                .equipment(model.getAddressBook(), model::getAvailability).isEmpty());
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void lifecycleEligibility_precedesAvailabilityAndGroupedCounts() {
        ModelManager model = sample();
        assertEquals(List.of(member(1)), DirectoryQuery.all().members(model.getAddressBook(),
                member -> member.getNusId().equals(member(1).getNusId())));
        assertEquals(List.of(item(2, Condition.GOOD)), DirectoryQuery.all().equipment(model.getAddressBook(),
                id -> {
                    assertEquals(new UUID(0, 2), id);
                    return Availability.AVAILABLE;
                }, item -> item.getUuid().equals(new UUID(0, 2))));
        assertEquals(List.of("\"photography\" / \"camera\": 1 available, 1 assigned, 1 unavailable"),
                InventoryGroups.summarize(model.getAddressBook(), model::getAvailability, item -> true));
        assertEquals(List.of("\"photography\" / \"camera\": 1 available, 0 assigned, 0 unavailable"),
                InventoryGroups.summarize(model.getAddressBook(), model::getAvailability,
                        item -> item.getUuid().equals(new UUID(0, 2))));
        model.closeLoan(new UUID(0, 1), LocalDate.of(2026, 1, 2));
        assertEquals(0, DirectoryQuery.holdings(model.getAddressBook(), member(1)));
        assertEquals(List.of("\"photography\" / \"camera\": 2 available, 0 assigned, 1 unavailable"),
                InventoryGroups.summarize(model.getAddressBook(), model::getAvailability, item -> true));
        assertTrue(InventoryGroups.summarize(model.getAddressBook(), model::getAvailability, item -> false).isEmpty());
    }

    @Test
    public void memberLifecycle_statusFiltersAndWarnings_followDeleteAndRestoreWithoutErasingRecords() {
        ModelManager model = sample();
        model.deleteMember(member(2));
        AddressBook before = new AddressBook(model.getAddressBook());
        DirectoryQuery active = new DirectoryQuery("alex", "", null, null, DirectoryQuery.Holding.ANY,
                DirectoryQuery.Sort.ID, false, DirectoryQuery.Status.ACTIVE);
        DirectoryQuery deleted = new DirectoryQuery("alex", "", null, null, DirectoryQuery.Holding.ANY,
                DirectoryQuery.Sort.ID, false, DirectoryQuery.Status.DELETED);
        assertEquals(List.of(member(1)), active.members(model.getAddressBook()));
        assertEquals(List.of(model.findMember(member(2).getNusId()).orElseThrow()),
                deleted.members(model.getAddressBook()));
        assertEquals(2, DirectoryQuery.all().members(model.getAddressBook()).size());
        assertTrue(active.members(model.getAddressBook(), member -> member.isDeleted()).isEmpty());
        assertTrue(active.description().contains("ACTIVE"));
        assertTrue(deleted.description().contains("DELETED"));
        Member candidate = new Member(new NusId("A0000009X"), member(1).getName(), member(1).getPhone(),
                member(1).getEmail());
        String warning = DuplicateWarnings.member(model.getAddressBook(), candidate);
        assertTrue(warning.contains(member(1).getNusId().value));
        assertFalse(warning.contains(member(2).getNusId().value));
        assertEquals(before, model.getAddressBook());
        model.restoreMember(model.findMember(member(2).getNusId()).orElseThrow());
        assertEquals(2, active.members(model.getAddressBook()).size());
        assertTrue(deleted.members(model.getAddressBook()).isEmpty());
        assertTrue(DuplicateWarnings.member(model.getAddressBook(), candidate).contains(member(2).getNusId().value));
        assertThrows(NullPointerException.class, () -> new DirectoryQuery("", "", null, null,
                DirectoryQuery.Holding.ANY, DirectoryQuery.Sort.DEFAULT, false, null));
    }

    @Test
    public void inventoryGroups_separatorAndQuoteCharacters_doNotMergeDistinctCategoryTypePairs() {
        ModelManager model = new ModelManager();
        model.addEquipment(new Equipment(new UUID(0, 1), "c", "a / b", Condition.GOOD, ""));
        model.addEquipment(new Equipment(new UUID(0, 2), "b / c", "a", Condition.DAMAGED, ""));
        model.addEquipment(new Equipment(new UUID(0, 3), " C ", " A / B ", Condition.FAIR, ""));
        model.addEquipment(new Equipment(new UUID(0, 4), "\"Camera\"", "Quotes", Condition.GOOD, ""));
        AddressBook before = new AddressBook(model.getAddressBook());
        assertEquals(List.of("\"a\" / \"b / c\": 0 available, 0 assigned, 1 unavailable",
                "\"a / b\" / \"c\": 2 available, 0 assigned, 0 unavailable",
                "\"quotes\" / \"\"\"camera\"\"\": 1 available, 0 assigned, 0 unavailable"),
                InventoryGroups.summarize(model.getAddressBook(), model::getAvailability, item -> true));
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void warnings_areAdvisoryNormalizedAndIdentityBearing() {
        ModelManager model = sample();
        assertTrue(DuplicateWarnings.member(model.getAddressBook(), member(1)).contains("A0000002X"));
        assertFalse(DuplicateWarnings.member(model.getAddressBook(), member(1)).contains("A0000001X"));
        Member unique = new Member(new NusId("A0000009X"), new Name("Priya"), new Phone("98888888"),
                new Email("unique@different.com"));
        assertEquals("", DuplicateWarnings.member(model.getAddressBook(), unique));
        Member sameEmail = new Member(new NusId("A0000008X"), new Name("Jamie"), new Phone("97777777"),
                new Email("alex1@example.com"));
        assertTrue(DuplicateWarnings.member(model.getAddressBook(), sameEmail).contains("A0000001X"));
        Member samePhone = new Member(new NusId("A0000007X"), new Name("Jamie"), new Phone("91234567"),
                new Email("unique@example.com"));
        assertTrue(DuplicateWarnings.member(model.getAddressBook(), samePhone).contains("A0000002X"));
        assertTrue(DuplicateWarnings.equipment(model.getAddressBook(), item(1, Condition.GOOD))
                .contains(new UUID(0, 2).toString()));
        assertEquals("", DuplicateWarnings.equipment(model.getAddressBook(),
                new Equipment(new UUID(0, 9), "Microphone", "Audio", Condition.GOOD, "")));
        assertEquals("", DuplicateWarnings.equipment(model.getAddressBook(),
                new Equipment(new UUID(0, 9), "Camera", "Audio", Condition.GOOD, "")));
        assertTrue(DuplicateWarnings.equipment(model.getAddressBook(),
                new Equipment(new UUID(0, 9), " CAMERA ", " PHOTOGRAPHY ", Condition.GOOD, ""))
                .contains(new UUID(0, 1).toString()));
    }

    @Test
    public void query_valueContractAndDescriptions_areStable() {
        assertEquals(DirectoryQuery.all(), DirectoryQuery.all());
        assertEquals(DirectoryQuery.all().hashCode(), DirectoryQuery.all().hashCode());
        assertEquals("All records", DirectoryQuery.all().description());
        assertTrue(query("x", "y", Condition.GOOD, Availability.ASSIGNED, DirectoryQuery.Holding.YES,
                DirectoryQuery.Sort.NAME, true).description().contains("ASSIGNED"));
        assertTrue(DirectoryQuery.all().sortDescription(true).contains("Name"));
        assertTrue(DirectoryQuery.all().sortDescription(false).contains("Category"));
        assertTrue(query("", "", null, null, DirectoryQuery.Holding.ANY, DirectoryQuery.Sort.ID, true)
                .sortDescription(false).contains("descending"));
        assertTrue(query("", "", null, null, DirectoryQuery.Holding.ANY, DirectoryQuery.Sort.ID, false)
                .sortDescription(true).contains("ascending"));
        assertTrue(query("", "", null, null, DirectoryQuery.Holding.ANY, DirectoryQuery.Sort.DEFAULT, true)
                .sortDescription(true).contains("descending"));
        assertThrows(NullPointerException.class, () -> query(null, "", null, null,
                DirectoryQuery.Holding.ANY, DirectoryQuery.Sort.ID, true));
        assertThrows(NullPointerException.class, () -> query("", "", null, null,
                null, DirectoryQuery.Sort.ID, true));
    }

    private static DirectoryQuery query(String text, String category, Condition condition, Availability availability,
            DirectoryQuery.Holding holding, DirectoryQuery.Sort sort, boolean descending) {
        return new DirectoryQuery(text, category, condition, availability, holding, sort, descending);
    }

}
