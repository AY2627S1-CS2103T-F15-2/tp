# Equipment view

Usage: `equipment view uuid/UUID`

Example: `equipment view uuid/550e8400-e29b-41d4-a716-446655440000`

Resolves one physical item by its UUID, including its derived availability, open loan and
current holder. Equal names do not make equipment interchangeable. Feedback includes the
requested UUID and a typed `DisplayRequest.equipmentDetails(uuid)`; it is not a serialization
of the detail panel.

Only one required `uuid/` parameter is accepted. Values must use canonical 8-4-4-4-12
hexadecimal UUID syntax, ignoring case and surrounding whitespace. Abbreviated Java forms
are rejected. Output uses the canonical full UUID. Spaces and tabs are accepted. Missing, blank, repeated, unknown or
malformed parameters and preamble text produce a parse error with command usage. Parameter
markers are reserved tokens; there is no quoting or escaping.

Unknown items, missing holders and inconsistent open-loan/availability references produce
`CommandException`. Expected domain lookup failures retain their original cause. The command
changes neither entity collections nor loan history and never writes storage directly.

`EquipmentDetailsPanel` displays name, category, full UUID, condition, notes, derived
availability, holder name/NUS ID/phone/email, and assigned/expected-return dates. No open loan
means no current holder or loan dates. A missing holder reference is an explicit data error,
not an unassigned item. Notes wrap; vertical scrolling keeps long content accessible.
`refresh()` rereads current data and removes stale fields after return, replacement or deletion.

## Integration status

Opening a different UUID resets the visible group/details scroll area to the top. Refreshing or
reopening the same UUID preserves the scroll position.


Merged #95 activates the real route, and merged #93 mounts the existing details panel and
coordinates exact selection and refresh. View commands do not mutate records. LogicManager retains
the existing read-command save behavior; Laplace mutations use the new staged/atomic boundary.
No alternate panel persistence or stored UI selection is introduced. See [MVP fields](MvpFields.md)
and [completion evidence](../gui/MvpCompletion.md).
