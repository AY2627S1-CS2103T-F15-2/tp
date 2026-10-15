# Delete a member

`member delete nus/NUS_ID`

Example: `member delete nus/a0123456x`

The real `member delete` route is integrated by merged #95 and mounted in the shell by #93.
Final milestone acceptance remains tracked in #75.

## Input and behavior

Supply exactly one `nus/` parameter. Leading/trailing spaces and tabs are accepted, and the
NUS ID normalizes to uppercase using the existing value parser. Missing/blank IDs, repeated
parameters, unknown markers and text before the first marker are rejected. Malformed IDs
produce the existing NUS ID constraints. Row numbers and member names are not identifiers.
Whitespace-delimited `[A-Za-z][A-Za-z0-9_-]*/` tokens are reserved markers; quoting and
escaping are unsupported, following the shared Laplace grammar.

The command resolves the actual stored member through Model, then calls `deleteMember`.
It does not scan or mutate collection views or clean up loans itself. The root permits
deletion only when the member has no open loans and removes that member's closed history.
Other members, equipment, legacy persons and unrelated loans remain intact. Equal names
with distinct NUS IDs remain independent.

Success produces `Deleted member: A0123456X` and `DisplayRequest.members()`. Unknown IDs and
open-loan references produce `CommandException` with an identity-specific message and the
domain cause. Rejection preserves all model data. Return outstanding equipment before
retrying a blocked deletion.

## Command contract

- Public constructor: `MemberDeleteCommand(NusId nusId)`.
- Parser: `MemberDeleteCommandParser implements Parser<MemberDeleteCommand>`; pass the
  argument remainder only, such as `nus/A0123456X`.
- Shared dependencies are merged: display/Logic #80, parsing/sorting #83 and UI foundation #81.
- The shell consumes the typed directory request and recovers selection by identity after deletion.

Command classes never save. LogicManager owns persistence: it executes the deletion on a snapshot,
atomically saves it and only then publishes through the existing live root. Parsing/business rejection
never saves; write/replacement failures preserve every record and the old file. Current routed,
failure and fresh-reload evidence is in [MvpCompletion.md](../gui/MvpCompletion.md).

## Verification

Tests cover normalized IDs, same-name records, actual stored-entity delegation, null misuse,
unknown/deleted identities, blocked open loans with existing closed history, permitted
deletion and removal of only related history, and preservation after JSON reload. Invalid
inputs are interleaved with a return and valid deletions to check subsequent state. Legacy
persons and unrelated open loans are retained. No saved user data is read or overwritten.

Run the focused component checks with Java 25:

```sh
./gradlew test --tests '*MemberDeleteCommand*' --tests '*LaplaceDeletionIntegrationTest'
```

Run the full normal `./gradlew check coverage shadowJar` and `./.github/run-checks.sh`
before handoff. Routed deletion, failed-save preservation, selection recovery and reload are covered
by the current combined suite; remaining native/CI acceptance is recorded in #75.
