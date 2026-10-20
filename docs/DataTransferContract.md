# v1.4 data-transfer contract

Keith owns the data-transfer service, snapshot format, preview component and shell
integration. This contract extends the v1.3 contracts for the approved data-transfer
workstream. Member/equipment lifecycle, loan behavior, search, undo/redo and audit
remain with their feature owners.

## Commands and paths

| Command | Effect |
| --- | --- |
| `data export file/PATH` | Write a complete JSON snapshot to a new file. |
| `data backup` | Write a uniquely named snapshot in `backups/` beside the active data file. |
| `data import file/PATH` | Validate and preview replacement of all collections. |
| `data restore file/PATH` | Use the same preview/confirmation flow for a saved snapshot. |
| `data confirm` | Back up current data, persist the previewed replacement atomically, then publish it. |
| `data cancel` | Discard the pending replacement without changing data. |

Relative paths resolve from `AppPaths.getHomeDirectory()`: the JAR folder for a
packaged launch, or the project working directory during development. Absolute
paths are supported. Existing destinations, including the active save file, cannot
be overwritten by export. Parent folders are created as needed.

Quote paths containing marker-like folder segments, for example
`data export file/"backup copies/October snapshot.json"`. Double quotes enclose the
whole path and are not included in the filename; there is no escape syntax.
Ordinary unquoted spaces are supported, but whitespace followed by `word/` is a
reserved parameter marker. Missing, repeated, unknown and blank parameters fail.

## Snapshot compatibility

Transfer snapshots use the existing root collections plus integer `schemaVersion: 1`:
`persons`, `members`, `equipment` and `loans`. Every export includes all four arrays.
Current member NUS IDs, physical equipment UUIDs, exact stored fields, open loans
and completed loan history survive round trips. Derived availability is not stored.
Preferences, logs and audit/undo state are not part of a data snapshot.

Advanced users can edit the live JSON while the application is closed, or edit an
exported snapshot and validate it through the import preview. This supports the
manual-data-file workflow described in the User Guide; schema validation does not
prohibit valid manual edits. Back up first and preserve cross-collection identities.

Unversioned existing save files are accepted. As with existing startup loading,
`persons` is required and missing newer arrays mean empty collections; the preview
makes their zero counts explicit. Explicit null/non-array collections are rejected.
The ordinary live-save format remains backward compatible and unversioned.

Interchange validation rejects unknown fields at every adapter level, duplicate
JSON keys, unsupported versions, trailing JSON values, null required entries,
invalid domain fields, duplicate identities/open loans and dangling references,
including references in closed history. Unknown fields are rejected rather than
silently discarded. When domain owners introduce lifecycle or loan-condition
fields, their adapter changes must be included before transferring those fields.

## Execution and integration API

- `Command.requiresPersistence()` defaults to true. Data commands return false;
  their transfer service owns all required file operations. The new
  `execute(Model, DataTransferService)` overload delegates to existing domain
  `execute(Model)` implementations, preserving their model-only API.
- `DataCommand` carries an `Action` and a `Path` for IMPORT/EXPORT/RESTORE. Other
  actions carry no path. Standalone execution without session services fails.
- `DataTransferService` owns a private candidate and baseline. `TransferPreviewResult`
  exposes only `DataTransferPreview` source/counts to the shell. UI buttons dispatch
  the same `data confirm` and `data cancel` commands as typed input.
- Confirmation uses the in-memory validated candidate, even if the source file
  changes. Only the latest preview is pending. A failed new preview discards the
  older request. Successful application data changes invalidate pending state;
  confirmation also compares the complete current root against its baseline.
- Queries, exports, backups, cancellation, previews and failed requests do not
  become replacement transactions. No undo/audit storage is introduced. Isaac can
  integrate one committed replacement at the existing persist-before-publish
  boundary; preview/export operations must remain outside undo history.

## Failure and UI behavior

Backup creation must succeed before replacement starts. A save failure leaves the
live model and prior save bytes unchanged and retains the pending preview for
retry. Completed backups are kept even when a later save fails. Generated snapshot
files use a private sibling candidate and a non-replacing move; failed installation
cleans the candidate. There is no fallback to truncating the live data file.

After success, the existing observable root is preserved, directories refresh and
the Members directory is displayed. Missing selections are recovered by the
existing identity-based shell logic. Cancellation returns to Members. Preview
buttons and typed confirmation have the same state and validation rules.

## Verification

Use Java 25 (`sdk use java 25.0.3.fx-zulu` on the project Mac):

```text
./gradlew test
./gradlew check coverage shadowJar
python3 scripts/check_transfer_coverage.py
python3 scripts/run_transfer_ui_tests.py
./gradlew --init-script scripts/packaged-transfer.gradle packagedTransferTest
```

The coverage check requires 100% line and branch coverage for every new production
file and every changed executable line against the recorded team starting point.
It does not exclude application classes or lower the existing coverage rules.
Run it after the full suite; the UI-only runner replaces Gradle's test result set.
The latter adapts the test-ui skill to the JavaFX application, reads
`test/ui-test-plan.md`, stops the recorded session on its first mismatch and writes
the complete input/output record to `_temp/ui-test-session.txt`.
