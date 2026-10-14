# Member view

Usage: `member view nus/NUS_ID`

Example: `member view nus/a0123456x`

Resolves one member by normalized NUS ID, validates references for their open loans, and
requests `DisplayRequest.memberDetails(nusId)`. NUS IDs are case-insensitive and stored in
uppercase. Same-name members remain separate identities. Feedback includes the requested
NUS ID and current holdings count; structured details are rendered by the panel.

Only one required `nus/` parameter is accepted. Spaces and tabs are allowed, and values are
trimmed. Missing, blank, repeated, unknown or malformed parameters and preamble text produce
a parse error with usage. Parameter markers are reserved tokens; quoting/escaping is not
supported. Unknown members and missing equipment references produce `CommandException`,
with original causes retained for expected domain lookup failures.

`MemberDetailsPanel` displays name, NUS ID, phone, email and the current held-item count.
Each open holding shows equipment name, full UUID, category, condition, availability,
assigned date and expected-return date. Holdings use the shared category/name/UUID order;
closed loans do not count. An unresolved equipment reference remains an explicit error
record with its UUID and dates instead of being silently dropped from the holdings count.

The panel keeps only the selected identity. `refresh()` resolves current contacts and loans,
including changes caused only by assignment/return. No selection, no holdings and a deleted
member have explicit states. Long text wraps and many holdings scroll vertically. Neither
the command nor panel modifies collections, persists selection or installs root listeners.

## Integration status

The individual command/parser and detail panel are implemented and independently tested in
v1.3.2 (#92). Duy owns family-route activation (#73); Keith owns shell assembly, typed
navigation and coordinated refresh (#91). Until those changes merge, the full command line
still reports member view as not implemented. Component/persistence tests do not claim
execution through the activated LogicManager route.

When routed, the existing LogicManager save-after-success behavior still applies to views.
Save errors remain visible; no alternate persistence or rollback mechanism is introduced.
