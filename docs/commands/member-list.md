# Member list

Usage: `member list`

Takes no parameters. Spaces and tabs after the subcommand are allowed; text or parameter
markers are rejected. Families and subcommands are lowercase and case-sensitive.

Reports active counts and any deleted count, and requests the full directory through `DisplayRequest`.
The command does not change entities, loans or stored collection order. It uses the existing
LogicManager save pipeline, which may report a save error even for a read-only command.

The directory uses the shared Laplace sort order: normalized name then NUS ID.
Active and deleted records remain together. Deleted cards are grey with an italic, struck-through
name and separate `(deleted)` suffix. Counts distinguish active, deleted and displayed records;
equal names with distinct IDs remain separate.

The existing application shell renders the sorted directory and refreshes cards and counts after
successful deletion/restoration. Identity-based selection is retained while the command itself
remains independent of JavaFX presentation.
