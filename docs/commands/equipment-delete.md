# Delete equipment

`equipment delete uuid/UUID`

Example: `equipment delete uuid/550e8400-e29b-41d4-a716-446655440000`

The individual parser and command are implemented. The application family route is still
a placeholder on this PR's baseline; Duy owns its activation. These instructions specify
the implemented component behavior, not a claim that typing the command already deletes
equipment in the application. The shell and final acceptance remain under issues #74 and #75.

## Input and behavior

Supply exactly one `uuid/` parameter. Leading/trailing spaces and tabs are accepted.
UUID values use the existing ParserUtil Java UUID syntax, including hexadecimal case
normalization and shortened forms accepted by UUID.fromString. Feedback uses canonical
UUID text. Missing/blank values, repeated parameters, unknown markers and text before the
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

## Integration handoff

- Public constructor: `EquipmentDeleteCommand(UUID uuid)`.
- Parser: `EquipmentDeleteCommandParser implements Parser<EquipmentDeleteCommand>`; pass
  the argument remainder only, such as `uuid/550e8400-e29b-41d4-a716-446655440000`.
- Shared dependencies are merged: display/Logic #80, parsing/sorting #83 and UI foundation #81.
- This component PR starts from team master `6230007d781478bc2a740a73fce6036e8a534996`.
- Duy activates `equipment delete` and updates routing expectations. Keith's shell consumes
  the typed directory request and handles identity selection recovery after deletion.

Command classes never save. Once routed, the existing LogicManager parse/execute/save
pipeline owns persistence. A failed parse/command must not reach saving; an I/O save failure
must be reported and does not promise rollback of an already changed in-memory model.
This PR's direct parser/command/JSON round trips save explicitly to temporary files and
reload a fresh ModelManager. They do not prove that the unactivated route reaches saving.

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
before handoff. Final routed pipeline and GUI deletion/selection tests remain pending.
