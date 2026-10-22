# Undo a record change

```text
undo
```

This lowercase, no-argument command restores the state before the most recent successful record mutation.
Whitespace is accepted; extra text, prefixes, counts and wrong-case command words are rejected.

Success reports `Undid: ACTION.`, such as `Undid: equipment add.`. No available step reports
`Nothing to undo.` without saving or changing data. The typed display request is `unchanged()`:
the shell refreshes current data and returns to the directory if a displayed exact record disappears.

History retains at most ten changes (eleven complete record snapshots) per session. Each member/equipment
add, edit, delete or restore, and each loan issue/return, is a single step. Batches remain one step.
Original identifiers, conditions, loan dates and relationships are restored together; commands are not
replayed. Read-only actions, navigation, help, appearance and rejected/no-change commands do not add
history entries or clear redo. A new successful record mutation after undo discards the redo branch.

Records and history are staged on an isolated transaction candidate. The existing atomic JSON save
runs once, and only then are both published. A failed save preserves live records, history position,
redo availability and the prior file; retry is supported. Search criteria and preferences are outside
history. History is not serialized: restarting loads the last saved records with no undo/redo available.

See [redo](./redo.md) for the opposite operation.

The [Developer Guide](../DeveloperGuide.md#undo-redo-feature) includes class, sequence, history-state
and retention-limit diagrams for this implementation.
