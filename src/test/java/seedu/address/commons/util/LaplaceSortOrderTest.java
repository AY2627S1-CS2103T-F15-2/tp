package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import seedu.address.model.ModelManager;
import seedu.address.model.equipment.Condition;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;

public class LaplaceSortOrderTest {

    @Test
    public void members_normalizedNameThenNusId_preservesOriginalDataAndOrder() {
        Member zed = member("A0000003X", "Zed");
        Member alexLater = member("A0000002X", "Alex  ");
        Member alexFirst = member("a0000001x", "aLEX");
        ModelManager model = new ModelManager();
        model.addMember(zed);
        model.addMember(alexLater);
        model.addMember(alexFirst);
        List<Member> original = List.copyOf(model.getMemberList());
        assertEquals(List.of(alexFirst, alexLater, zed),
                model.getMemberList().stream().sorted(LaplaceSortOrder.MEMBERS).toList());
        assertEquals(original, model.getMemberList());
        assertEquals("Alex  ", alexLater.getName().fullName);
        assertEquals("aLEX", alexFirst.getName().fullName);
        assertEquals(0, LaplaceSortOrder.MEMBERS.compare(alexFirst, alexFirst));
    }

    @Test
    public void equipment_categoryThenNameThenUuid_preservesEveryPhysicalItem() {
        Equipment categoryFirst = equipment("ffffffff-ffff-ffff-ffff-ffffffffffff", "Zed", " Audio ");
        Equipment nameFirst = equipment("ffffffff-ffff-ffff-ffff-fffffffffffe", "Adapter", "camera");
        Equipment uuidFirst = equipment("00000000-0000-0000-0000-000000000001", " Lens ", " Camera");
        Equipment uuidLast = equipment("80000000-0000-0000-0000-000000000001", "lens", "CAMERA ");
        ModelManager model = new ModelManager();
        for (Equipment item : List.of(uuidLast, uuidFirst, nameFirst, categoryFirst)) {
            model.addEquipment(item);
        }
        List<Equipment> original = List.copyOf(model.getEquipmentList());
        assertEquals(List.of(categoryFirst, nameFirst, uuidFirst, uuidLast),
                model.getEquipmentList().stream().sorted(LaplaceSortOrder.EQUIPMENT).toList());
        assertEquals(original, model.getEquipmentList());
        assertEquals(" Lens ", uuidFirst.getName());
        assertEquals(" Camera", uuidFirst.getCategory());
        assertTrue(LaplaceSortOrder.EQUIPMENT.compare(uuidFirst, uuidLast) < 0);
        assertEquals(0, LaplaceSortOrder.EQUIPMENT.compare(uuidFirst, uuidFirst));
    }

    @Test
    public void comparators_turkishLocale_remainLocaleIndependent() {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            Member upper = member("A0000001X", "ISAAC");
            Member lower = member("A0000002X", "isaac");
            assertTrue(LaplaceSortOrder.MEMBERS.compare(upper, lower) < 0);
            Equipment first = equipment("00000000-0000-0000-0000-000000000001", "ITEM", "IMAGE");
            Equipment second = equipment("00000000-0000-0000-0000-000000000002", "item", "image");
            assertTrue(LaplaceSortOrder.EQUIPMENT.compare(first, second) < 0);
        } finally {
            Locale.setDefault(original);
        }
    }

    private Member member(String nusId, String name) {
        return new Member(new NusId(nusId), new Name(name), new Phone("91234567"), new Email("test@example.com"));
    }

    private Equipment equipment(String uuid, String name, String category) {
        return new Equipment(UUID.fromString(uuid), name, category, Condition.GOOD, "");
    }
}
