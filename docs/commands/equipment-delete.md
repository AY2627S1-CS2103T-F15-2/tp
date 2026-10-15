# Delete equipment

`equipment delete uuid/UUID`

Example: `equipment delete uuid/550e8400-e29b-41d4-a716-446655440000`

The real `equipment delete` route is integrated by merged #95 and mounted in the shell by #93.
Final milestone acceptance remains tracked in #75.

## Input and behavior

Supply exactly one `uuid/` parameter. Leading/trailing spaces and tabs are accepted.
UUID values require canonical 8-4-4-4-12 syntax, ignoring hexadecimal case. Shortened
forms accepted by UUID.fromString are rejected. Feedback uses canonical UUID text. Missing/blank values, repeated parameters, unknown markers and text before the
first marker are rejected. Malformed values produce the existing equipment UUID explanation.
Row numbers and equipment names cannot identify a physical item.
Whitespace-delimited `[A-Za-z][A-Za-z0-9_-]*/` tokens are reserved markers; quoting and
escaping are unsupported, following the shared Laplace grammar.

The command resolves the actual stored item through Model, then calls `deleteEquipment`.
It does not scan or mutate collection views or clean up loans itself. The root permits
deletion only when the item has no open loan and removes that item's closed history.
Other items, members, legacy persons and unrelated loans remain intact. Items with
identical descriptions but different UUIDs remain independent.

Success produces `Deleted equipment: UUID` with the actual UUID and
`DisplayRequest.equipment()`. Unknown UUIDs and open-loan references produce CommandException
with an identity-specific message and the domain cause. Rejection preserves all model data.
Record the item's return before retrying a blocked deletion.

## Command contract

- Public constructor: `EquipmentDeleteCommand(UUID uuid)`.
- Parser: `EquipmentDeleteCommandParser implements Parser<EquipmentDeleteCommand>`; pass
  the argument remainder only, such as `uuid/550e8400-e29b-41d4-a716-446655440000`.
- Shared dependencies are merged: display/Logic #80, parsing/sorting #83 and UI foundation #81.
- The shell consumes the typed directory request and recovers selection by identity after deletion.

Command classes never save. LogicManager owns persistence: it executes the deletion on a snapshot,
atomically saves it and only then publishes through the existing live root. Parsing/business rejection
never saves; write/replacement failures preserve every record and the old file. Current routed,
failure and fresh-reload evidence is in [MvpCompletion.md](../gui/MvpCompletion.md).

## Verification

Tests cover canonical identity, identical physical-item descriptions, actual stored-entity
delegation, null misuse, unknown/deleted UUIDs, blocked open loans with existing history,
permitted deletion and removal of only related history, and preservation after JSON reload.
Invalid inputs are interleaved with a return and valid deletions to check subsequent state.
Legacy persons and unrelated open loans/derived availability are retained. No saved user
data is read or overwritten.

Run the focused component checks with Java 25:

```sh
./gradlew test --tests '*EquipmentDeleteCommand*' --tests '*LaplaceDeletionIntegrationTest'
```

Run the full normal `./gradlew check coverage shadowJar` and `./.github/run-checks.sh`
before handoff. Routed deletion, failed-save preservation, selection recovery and reload are covered
by the current combined suite; remaining native/CI acceptance is recorded in #75.
