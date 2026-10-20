# Data-transfer UI test plan

Run `python3 scripts/run_transfer_ui_tests.py` with Java 25 active. This adapts the
test-ui skill to Laplace's JavaFX application: each case mounts a fresh real shell,
loads the full four-collection fixture and persists it to disposable storage.
`empty.json` is a prepared empty snapshot. Inputs go through CommandBox and the real
LogicManager. The runner stops at the first mismatch and records actual UI feedback.
`{{DIR}}` replaces the temporary directory; `{{BACKUP}}` replaces a generated backup
path. These are the only normalization rules. Error steps also assert unchanged
collections and saved bytes. The session is written even if an assertion fails.

The command behavior below remains unchanged by the packaging correction.
`StandaloneJarTest` additionally runs the real packaged application on Java SE
without external JavaFX files and checks the required scaled preview viewports;
see `docs/CourseCompliance.md`. Manual JSON-edit/reload validation is covered by
`TransferSnapshotStorageTest` alongside the recorded transfer cases.

## TC1: Preview, strict parsing, cancellation and no implicit save

**Aim:** Interleave valid requests and errors; ensure rejected inputs and previews
preserve every collection and saved byte, cancellation prevents confirmation and
preview actions remain visible below the scrollable text at the tested sizes.

### Input

```text
data confirm
data export file/{{DIR}}/export.json
data import file/{{DIR}}/export.json
data import file/{{DIR}}/export.json file/duplicate
data cancel
data confirm
```

### Expected output

```text
Error: No pending preview. Use data import or data restore first.
Success: Snapshot exported to {{DIR}}/export.json
Success: Replace all data from {{DIR}}/export.json
Persons: 1; members: 1; equipment: 1; loans (including history): 2.
All current records will be replaced. A backup is required before applying.
Use data confirm to apply or data cancel to discard.
Error: Repeated parameter: file/
Success: Data transfer cancelled. Current data is unchanged.
Error: No pending preview. Use data import or data restore first.
```

## TC2: Backup, confirmed replacement, selection recovery and reload

**Aim:** Restore an empty snapshot through the mounted shell, confirm once, retain
the original complete snapshot in backups, and reload the persisted replacement.

### Input

```text
data restore file/{{DIR}}/empty.json
data backup
data confirm
data confirm
member list
```

### Expected output

```text
Success: Replace all data from {{DIR}}/empty.json
Persons: 0; members: 0; equipment: 0; loans (including history): 0.
All current records will be replaced. A backup is required before applying.
Use data confirm to apply or data cancel to discard.
Success: Backup saved to {{BACKUP}}
Success: Snapshot applied. Previous data backed up to {{BACKUP}}
Error: No pending preview. Use data import or data restore first.
Success: Listed 0 active members.
```
