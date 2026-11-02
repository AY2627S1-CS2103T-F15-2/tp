package seedu.address.model.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;

/**
 * Checks advisory case matching without changing identity or lifecycle eligibility.
 */
public class DuplicateWarningsTest {
    @Test
    public void equipment_caseEquivalentGroupFields_warnsForRetainedUnitsWithoutMutation() {
        for (List<String> spellings : List.of(List.of("ΟΣ", "οσ"), List.of("İ", "i"))) {
            ModelManager model = new ModelManager();
            Equipment first = new Equipment(new UUID(0, 1), spellings.getFirst(), spellings.getFirst(),
                    Condition.GOOD, "");
            Equipment candidate = new Equipment(new UUID(0, 2), spellings.getLast(), spellings.getLast(),
                    Condition.DAMAGED, "");
            model.addEquipment(first);
            AddressBook before = new AddressBook(model.getAddressBook());
            assertTrue(DuplicateWarnings.equipment(model.getAddressBook(), candidate)
                    .contains("Possible duplicate(s): " + first.getUuid()));
            assertEquals("", DuplicateWarnings.equipment(model.getAddressBook(), first));
            assertEquals("", DuplicateWarnings.equipment(model.getAddressBook(), new Equipment(candidate.getUuid(),
                    candidate.getName(), "Lighting", Condition.GOOD, "")));
            assertEquals("", DuplicateWarnings.equipment(model.getAddressBook(), new Equipment(candidate.getUuid(),
                    "Tripod", candidate.getCategory(), Condition.GOOD, "")));
            assertEquals(before, model.getAddressBook());
            model.deleteEquipment(first);
            assertTrue(DuplicateWarnings.equipment(model.getAddressBook(), candidate)
                    .contains("Possible duplicate(s): " + first.getUuid()));
        }
    }

    @Test
    public void member_caseEquivalentNames_warnsOnlyForOtherActiveIdentities() {
        for (List<String> spellings : List.of(List.of("ΝΙΚΟΣ", "νικοσ"), List.of("İ", "i"))) {
            ModelManager model = new ModelManager();
            Member first = new Member(new NusId("A8400001X"), Name.forMember(spellings.getFirst()),
                    Phone.forMember("91234567"), Email.forMember("first@example.com"));
            Member candidate = new Member(new NusId("A8400002X"), Name.forMember(spellings.getLast()),
                    Phone.forMember("98765432"), Email.forMember("other@example.com"));
            model.addMember(first);
            AddressBook before = new AddressBook(model.getAddressBook());
            assertTrue(DuplicateWarnings.member(model.getAddressBook(), candidate)
                    .contains("Possible duplicate(s): " + first.getNusId()));
            assertEquals("", DuplicateWarnings.member(model.getAddressBook(), first));
            assertEquals(before, model.getAddressBook());
            model.deleteMember(first);
            assertEquals("", DuplicateWarnings.member(model.getAddressBook(), candidate));
        }
    }
}
