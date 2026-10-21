package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import seedu.address.model.member.NusId;
import seedu.address.model.query.InventoryGroups;

public class DisplayRequestTest {
    private static final NusId NUS_ID = new NusId("a0123456x");
    private static final UUID UUID_ONE = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    @Test
    public void groupTarget_normalizesDescriptionsWithoutChoosingAUnit() {
        DisplayRequest group = DisplayRequest.equipmentGroup(new InventoryGroups.Key(" Photo ", "Camera"));
        DisplayRequest same = DisplayRequest.equipmentGroup(new InventoryGroups.Key("photo", "camera"));
        assertEquals(group, same);
        assertEquals(group.hashCode(), same.hashCode());
        assertNotEquals(group, DisplayRequest.equipmentGroup(new InventoryGroups.Key("photo", "Tripod")));
        assertEquals(DisplayRequest.Type.EQUIPMENT_GROUP, group.getType());
        assertTrue(group.getEquipmentUuid().isEmpty());
        assertEquals("camera", group.getEquipmentGroup().orElseThrow().name());
        assertTrue(group.toString().contains("camera"));
        assertThrows(NullPointerException.class, () -> DisplayRequest.equipmentGroup(null));
    }

    @Test
    public void factories_directoryTargets_haveNoIdentity() {
        List<DisplayRequest> requests = List.of(DisplayRequest.unchanged(), DisplayRequest.legacyPersons(),
                DisplayRequest.members(), DisplayRequest.equipment(), DisplayRequest.loans());
        List<DisplayRequest.Type> types = List.of(DisplayRequest.Type.UNCHANGED, DisplayRequest.Type.LEGACY_PERSONS,
                DisplayRequest.Type.MEMBERS, DisplayRequest.Type.EQUIPMENT, DisplayRequest.Type.LOANS);
        for (int i = 0; i < requests.size(); i++) {
            DisplayRequest request = requests.get(i);
            assertEquals(types.get(i), request.getType());
            assertTrue(request.getMemberNusId().isEmpty());
            assertTrue(request.getEquipmentUuid().isEmpty());
        }
        assertEquals(requests, List.of(DisplayRequest.unchanged(), DisplayRequest.legacyPersons(),
                DisplayRequest.members(), DisplayRequest.equipment(), DisplayRequest.loans()));
    }

    @Test
    public void factories_detailTargets_haveOnlyTheirIdentity() {
        DisplayRequest member = DisplayRequest.memberDetails(NUS_ID);
        assertEquals(DisplayRequest.Type.MEMBER_DETAILS, member.getType());
        assertEquals(Optional.of(new NusId("A0123456X")), member.getMemberNusId());
        assertTrue(member.getEquipmentUuid().isEmpty());
        DisplayRequest equipment = DisplayRequest.equipmentDetails(UUID_ONE);
        assertEquals(DisplayRequest.Type.EQUIPMENT_DETAILS, equipment.getType());
        assertEquals(Optional.of(UUID_ONE), equipment.getEquipmentUuid());
        assertTrue(equipment.getMemberNusId().isEmpty());
    }

    @Test
    public void factories_nullIdentity_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> DisplayRequest.memberDetails(null));
        assertThrows(NullPointerException.class, () -> DisplayRequest.equipmentDetails(null));
    }

    @Test
    public void equalsAndHashCode_compareTypeAndIdentity() {
        List<DisplayRequest> requests = List.of(DisplayRequest.unchanged(), DisplayRequest.legacyPersons(),
                DisplayRequest.members(), DisplayRequest.memberDetails(NUS_ID), DisplayRequest.equipment(),
                DisplayRequest.equipmentDetails(UUID_ONE), DisplayRequest.loans());
        for (DisplayRequest request : requests) {
            assertEquals(request, request);
            assertNotEquals(request, null);
            assertNotEquals(request, "request");
            for (DisplayRequest other : requests) {
                if (request.getType() != other.getType()) {
                    assertNotEquals(request, other);
                }
            }
        }
        DisplayRequest member = DisplayRequest.memberDetails(NUS_ID);
        DisplayRequest equalMember = DisplayRequest.memberDetails(new NusId("A0123456X"));
        assertEquals(member, equalMember);
        assertEquals(member.hashCode(), equalMember.hashCode());
        assertNotEquals(member, DisplayRequest.memberDetails(new NusId("A0123457X")));
        DisplayRequest equipment = DisplayRequest.equipmentDetails(UUID_ONE);
        DisplayRequest equalEquipment = DisplayRequest.equipmentDetails(UUID.fromString(UUID_ONE.toString()));
        assertEquals(equipment, equalEquipment);
        assertEquals(equipment.hashCode(), equalEquipment.hashCode());
        assertNotEquals(equipment, DisplayRequest.equipmentDetails(new UUID(0, 1)));
    }

    @Test
    public void toString_includesTargetAndIdentity() {
        assertTrue(DisplayRequest.memberDetails(NUS_ID).toString().contains("MEMBER_DETAILS"));
        assertTrue(DisplayRequest.memberDetails(NUS_ID).toString().contains("A0123456X"));
        assertTrue(DisplayRequest.equipmentDetails(UUID_ONE).toString().contains(UUID_ONE.toString()));
        assertTrue(DisplayRequest.unchanged().toString().contains("UNCHANGED"));
    }
}
