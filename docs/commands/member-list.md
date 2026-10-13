# Member list

Usage: `member list`

Takes no parameters. Spaces and tabs after the subcommand are allowed; text or parameter
markers are rejected. Families and subcommands are lowercase and case-sensitive.

Reports the total count and requests the member directory through `DisplayRequest`.
The command does not change entities, loans or stored collection order. It uses the existing
LogicManager save pipeline, which may report a save error even for a read-only command.

The directory uses the shared Laplace sort order: normalized name then NUS ID.
All stored members count as active; equal names with distinct IDs remain separate.

Stage B activates this list route. Rendering the sorted directory, row details and selection
requires the later directory panels and application shell; this command alone does not render
JavaFX nodes or establish complete GUI integration.
