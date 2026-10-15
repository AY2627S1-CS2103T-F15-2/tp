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
assigned date and expected-return date. Holdings sort by expected return date, normalized
equipment name and UUID; closed loans do not count. Unresolved equipment references produce
an explicit error listing the missing UUIDs instead of rendering an incomplete holdings list.

The panel keeps only the selected identity. `refresh()` resolves current contacts and loans,
including changes caused only by assignment/return. No selection, no holdings and a deleted
member have explicit states. Long text wraps and many holdings scroll vertically. Neither
the command nor panel modifies collections, persists selection or installs root listeners.

## Integration status

Merged #95 activates the real route; merged #93 mounts this existing detail panel and coordinates
identity selection and refresh. Read-only view commands retain the existing LogicManager save behavior.
Save errors remain visible. Laplace mutations use the staged/atomic boundary described in
[MVP fields](MvpFields.md). Current combined acceptance is in
[MvpCompletion.md](../gui/MvpCompletion.md).
