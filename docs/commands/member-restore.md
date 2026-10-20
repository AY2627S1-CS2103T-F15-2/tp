# Restore a member

```text
member restore nus/NUS_ID
```

Restore a soft-deleted member at any time. There are no timestamps, clocks, expiry rules,
automatic clearing or archival states. Exactly one `nus/` parameter is required. Normalization,
duplicate-marker rejection and invalid-field diagnostics follow `member delete`.

Success reports `Restored member NUS_ID: NAME.` and opens that member's details. Their card
returns to its active styling. Restoring an active or unknown member fails without saving.
Deletion requires no outstanding loans, retains every detail and closed loan, and reserves the
NUS ID. Repeated deletion is rejected. Deleted members remain visible but cannot receive new loans.

## Model and persistence contract

- `Member.isDeleted()` exposes immutable lifecycle state. The existing four-field constructor
  creates active members; the five-field constructor includes a boolean deletion flag.
- `Model.deleteMember(Member)` soft-deletes the stored identity. `Model.restoreMember(Member)`
  restores it. The root enforces eligibility, even for callers outside the command pipeline.
- Identity lookup and member collections include retained deleted records. The collection's
  `setDeleted(NusId, boolean)` changes only deletion state and preserves identity and list position.
- JSON saves include `deleted: true/false`. Missing legacy fields default to false. Explicit null,
  strings, numbers, arrays and objects are invalid. Closed loans may reference deleted members;
  an open loan referencing one makes the entire load invalid.
- Both commands execute on an isolated transaction snapshot. Only a successful atomic save
  publishes the replacement to the existing observable root. Failure leaves live state and saved
  bytes unchanged. Repeated/invalid operations do not save.

## GUI and feature integration

Active and deleted members share the existing sorted directory. Deleted cards are grey and
selectable, with an italic, struck-through name and separate `(deleted)` suffix. Details use the
same name treatment and show the restore command. Summaries distinguish active, deleted and
displayed counts. Deleting retains selection; restoring opens the same identity's details.

Member editing is integrated and preserves the lifecycle flag. Editing a deleted member is rejected
with restore guidance before any no-op check or save. An edit cannot silently restore a record.

Keith's transfer integration must retain all members and their deletion flags in export, preview,
confirmation, backups and reloads. Coordinate lifecycle-aware snapshot versioning: old/unversioned
snapshots default missing flags to active; lifecycle-aware snapshots must preserve the flags and
closed-loan references. Do not discard unknown lifecycle fields or certify compatibility using
only active-member fixtures. No transfer service is implemented in this branch.

Isaac's future history service can record one successful transaction per deletion/restoration
after the existing persistence boundary. No separate history mechanism is introduced here.
Equipment deletion and its history cleanup remain unchanged. Older releases that ignore member
deletion metadata must not be used to rewrite lifecycle-aware saves.
