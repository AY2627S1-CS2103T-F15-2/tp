# Equipment view

Usage: `equipment view uuid/UUID`

Example: `equipment view uuid/550e8400-e29b-41d4-a716-446655440000`

Resolves one physical item by its UUID, including its derived availability, open loan and
current holder. Equal names do not make equipment interchangeable. Feedback includes the
requested UUID and a typed `DisplayRequest.equipmentDetails(uuid)`; it is not a serialization
of the detail panel.

Only one required `uuid/` parameter is accepted. Values are trimmed and use the existing
Java UUID parser (including abbreviated forms accepted by `UUID.fromString`). Output uses
the canonical full UUID. Spaces and tabs are accepted. Missing, blank, repeated, unknown or
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

The individual command/parser and detail panel are implemented and independently tested in
v1.3.2 (#92). The team family router still needs Duy's view-route activation (#73), and Keith's
shell must consume the typed display request and coordinate selection/refresh (#91). Until
then, the full command line still reports the route as not implemented. Panel previews are
component evidence, not full application navigation acceptance.

The existing LogicManager saves after every successful routed command, including views.
That pipeline can report an I/O save failure; it does not guarantee in-memory rollback.
No alternate save mechanism or persisted UI selection is introduced here.
