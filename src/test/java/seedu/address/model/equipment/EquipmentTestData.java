package seedu.address.model.equipment;

import java.util.UUID;

/**
 * Equipment fixtures owned by the equipment module tests.
 */
public class EquipmentTestData {

    public static final UUID CAMERA_UUID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    public static final UUID SECOND_CAMERA_UUID = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
    public static final UUID TRIPOD_UUID = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");

    public static final Equipment CAMERA =
            new Equipment(CAMERA_UUID, "Camera", "Photography", Condition.GOOD, "");
    public static final Equipment SAME_NAME_DIFFERENT_UUID =
            new Equipment(SECOND_CAMERA_UUID, "Camera", "Photography", Condition.FAIR, "Spare body");
    public static final Equipment SAME_UUID_DIFFERENT_DETAILS =
            new Equipment(CAMERA_UUID, "Video camera", "Video", Condition.DAMAGED, "Cracked screen");
    public static final Equipment TRIPOD =
            new Equipment(TRIPOD_UUID, "Tripod", "Support", Condition.UNDER_REPAIR, "Loose leg");

    private EquipmentTestData() {}
}
