package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.member.NusId;
import seedu.address.model.query.DirectoryQuery;
import seedu.address.model.query.InventoryGroups;
import seedu.address.model.query.LoanQuery;

/**
 * Immutable navigation intent, independent of JavaFX and persisted application data.
 * Only detail targets carry an identity; factories prevent invalid combinations.
 * Directory requests with a query replace the active criteria; requests without one preserve them.
 * Member/equipment list commands supply {@code DirectoryQuery.all()} to reset filters and sorting.
 * Loans requests optionally carry identity substrings; an absent query preserves its current filters.
 */
public final class DisplayRequest {
    /**
     * Supported display targets. Help and exit take precedence over navigation.
     */
    public enum Type {
        UNCHANGED, LEGACY_PERSONS, MEMBERS, MEMBER_DETAILS, EQUIPMENT, EQUIPMENT_DETAILS, EQUIPMENT_GROUP, LOANS
    }

    private final Type type;
    private final NusId memberNusId;
    private final UUID equipmentUuid;
    private final DirectoryQuery query;
    private final InventoryGroups.Key equipmentGroup;
    private final LoanQuery loanQuery;

    private DisplayRequest(Type type, NusId memberNusId, UUID equipmentUuid) {
        this(type, memberNusId, equipmentUuid, null);
    }

    private DisplayRequest(Type type, NusId memberNusId, UUID equipmentUuid, DirectoryQuery query) {
        this(type, memberNusId, equipmentUuid, query, null);
    }

    private DisplayRequest(Type type, NusId memberNusId, UUID equipmentUuid, DirectoryQuery query,
            InventoryGroups.Key equipmentGroup) {
        this(type, memberNusId, equipmentUuid, query, equipmentGroup, null);
    }

    private DisplayRequest(Type type, NusId memberNusId, UUID equipmentUuid, DirectoryQuery query,
            InventoryGroups.Key equipmentGroup, LoanQuery loanQuery) {
        this.loanQuery = loanQuery;
        this.equipmentGroup = equipmentGroup;
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

    /**
     * Opens a derived group without selecting an arbitrary physical unit.
     */
    public static DisplayRequest equipmentGroup(InventoryGroups.Key group) {
        return new DisplayRequest(Type.EQUIPMENT_GROUP, null, null, null, requireNonNull(group));
    }

    public Optional<InventoryGroups.Key> getEquipmentGroup() {
        return Optional.ofNullable(equipmentGroup);
    }

    public static DisplayRequest loans() {
        return new DisplayRequest(Type.LOANS, null, null);
    }

    /**
     * Opens Loans with explicit criteria; an empty query clears both filters.
     */
    public static DisplayRequest loans(LoanQuery query) {
        return new DisplayRequest(Type.LOANS, null, null, null, null, requireNonNull(query));
    }

    public Optional<LoanQuery> getLoanQuery() {
        return Optional.ofNullable(loanQuery);
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
                && Objects.equals(query, otherRequest.query)
                && Objects.equals(equipmentGroup, otherRequest.equipmentGroup)
                && Objects.equals(loanQuery, otherRequest.loanQuery);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, memberNusId, equipmentUuid, query, equipmentGroup, loanQuery);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("type", type)
                .add("memberNusId", memberNusId)
                .add("equipmentUuid", equipmentUuid)
                .add("query", query)
                .add("equipmentGroup", equipmentGroup)
                .add("loanQuery", loanQuery)
                .toString();
    }
}
