# Equipment list

Usage: `equipment list`

Takes no parameters. Spaces and tabs after the subcommand are allowed; text or parameter
markers are rejected. Families and subcommands are lowercase and case-sensitive.

Reports the total count and requests the equipment directory through `DisplayRequest`.
The command does not change entities, loans or stored collection order. Its read-only declaration
bypasses record saving, so a record-save failure cannot block the list.

The directory uses the shared Laplace sort order: normalized category, name, then UUID text.
Each physical UUID remains a separate item.

Stage B activates this list route. Rendering the sorted directory, row details and selection
requires the later directory panels and application shell; this command alone does not render
JavaFX nodes or establish complete GUI integration.
