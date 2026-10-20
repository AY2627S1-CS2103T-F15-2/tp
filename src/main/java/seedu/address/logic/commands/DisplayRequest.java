package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.member.NusId;
import seedu.address.model.query.DirectoryQuery;

/**
 * Immutable navigation intent, independent of JavaFX and persisted application data.
 * Only detail targets carry an identity; factories prevent invalid combinations.
 */
public final class DisplayRequest {
    /** Supported display targets. Help and exit take precedence over navigation. */
    public enum Type {
        UNCHANGED, LEGACY_PERSONS, MEMBERS, MEMBER_DETAILS, EQUIPMENT, EQUIPMENT_DETAILS, LOANS
    }

    private final Type type;
    private final NusId memberNusId;
    private final UUID equipmentUuid;
    private final DirectoryQuery query;

    private DisplayRequest(Type type, NusId memberNusId, UUID equipmentUuid) {
        this(type, memberNusId, equipmentUuid, null);
    }

    private DisplayRequest(Type type, NusId memberNusId, UUID equipmentUuid, DirectoryQuery query) {
        this.type = type;
        this.memberNusId = memberNusId;
        this.equipmentUuid = equipmentUuid;
        this.query = query;
    }

    public Optional<DirectoryQuery> getQuery() {
        return Optional.ofNullable(query);
    }

    public static DisplayRequest unchanged() {
        return new DisplayRequest(Type.UNCHANGED, null, null);
    }

    public static DisplayRequest legacyPersons() {
        return new DisplayRequest(Type.LEGACY_PERSONS, null, null);
    }

    public static DisplayRequest members() {
        return new DisplayRequest(Type.MEMBERS, null, null);
    }

    public static DisplayRequest members(DirectoryQuery query) {
        return new DisplayRequest(Type.MEMBERS, null, null, requireNonNull(query));
    }

    public static DisplayRequest memberDetails(NusId nusId) {
        return new DisplayRequest(Type.MEMBER_DETAILS, requireNonNull(nusId), null);
    }

    public static DisplayRequest equipment() {
        return new DisplayRequest(Type.EQUIPMENT, null, null);
    }

    public static DisplayRequest equipment(DirectoryQuery query) {
        return new DisplayRequest(Type.EQUIPMENT, null, null, requireNonNull(query));
    }

    public static DisplayRequest equipmentDetails(UUID uuid) {
        return new DisplayRequest(Type.EQUIPMENT_DETAILS, null, requireNonNull(uuid));
    }

    public static DisplayRequest loans() {
        return new DisplayRequest(Type.LOANS, null, null);
    }

    public Type getType() {
        return type;
    }

    public Optional<NusId> getMemberNusId() {
        return Optional.ofNullable(memberNusId);
    }

    public Optional<UUID> getEquipmentUuid() {
        return Optional.ofNullable(equipmentUuid);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof DisplayRequest otherRequest)) {
            return false;
        }
        return type == otherRequest.type
                && Objects.equals(memberNusId, otherRequest.memberNusId)
                && Objects.equals(equipmentUuid, otherRequest.equipmentUuid)
                && Objects.equals(query, otherRequest.query);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, memberNusId, equipmentUuid, query);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("type", type)
                .add("memberNusId", memberNusId)
                .add("equipmentUuid", equipmentUuid)
                .add("query", query)
                .toString();
    }
}
