# PR #107 review follow-up

This records the implementation response to Keegan4's review of commit
`480283bc7d60579d87701518a775ac05e90f29f9`. It does not claim reviewer approval.

## Obsolete preview controls

[Review comment](https://github.com/AY2627S1-CS2103T-F15-2/tp/pull/107#discussion_r4321774828)

A failed import/restore that invalidates an existing candidate now reports that
state through a typed exception. The mounted shell unmounts the old controls and
returns to Members while preserving the failed input, all collections and saved
bytes. Returning to a directory also removes obsolete hidden single-panel controls.
Backup/save failures during confirmation keep the exact preview retryable.

`DataTransferUiPlanTest` exercises missing/malformed files for both verbs, rejected
schema versions, unrelated export failure, blocked backup creation and controlled
save failure. It verifies real Confirm controls, save counts, unchanged data/bytes
and successful retry. Recorded case TC3 interleaves valid and rejected previews.

## Committed replacement handoff

[Review comment](https://github.com/AY2627S1-CS2103T-F15-2/tp/pull/107#discussion_r4321774833)

Successful confirmation returns `CommittedReplacementResult` through the normal
logic API after persistence and publication. Its before/after snapshots are
defensive copies of all collections. Read-only transfer operations and cancellation
return other result types; failures throw without returning a commit signal.
Repeated confirmation cannot generate another result.

`DataTransferTransactionTest` verifies this distinction through routed commands,
including rejected inputs, partial-write/atomic-move failures and persisted reload.
`DataTransferServiceTest` verifies snapshots remain stable after caller/model changes.
Isaac's common history consumer is a tracked integration gate, not implemented here.

## Future domain-field migration

[Review comment](https://github.com/AY2627S1-CS2103T-F15-2/tp/pull/107#discussion_r4321774838)

[Issue #110](https://github.com/AY2627S1-CS2103T-F15-2/tp/issues/110), assigned to
wtvlol for v1.4 with priority.High/type.Task, remains open independently of #106.
The [contract](../DataTransferContract.md) supplies a proposed version/default policy
and exact fixture/round-trip acceptance steps. Keegan/Arjo/Nick's agreement and
their lifecycle/condition adapters remain pending. Current tests prove existing
fields and completed history only; full v1.4 field compatibility is not claimed.

See [verification](DataTransferVerification.md) and the complete
[recorded session](DataTransferSession.txt) for measured results at this revision.
