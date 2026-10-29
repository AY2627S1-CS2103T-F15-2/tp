# Laplace command routing reference

## Integration status

The ten MVP routes, `loans list` navigation, `member edit` and `member restore` dispatch to concrete parsers.
Command execution uses the
LogicManager persistence boundary. Merged #93 integrates the real shell and #95 activates all ten routes.
Current MVP completion evidence is in [MvpCompletion.md](../gui/MvpCompletion.md); #75 remains the final gate.

Member editing extends the MVP through an identity-preserving model replacement API. Its parser,
transactional behavior and shell-refresh contract are documented in [Edit member](member-edit.md).
Member soft-delete and unlimited restoration are supported. Archival remains outside scope.

| Command | Syntax | Reference |
| --- | --- | --- |
| member add | `member add nus/NUS_ID name/NAME phone/PHONE email/EMAIL` | [Add member](member-add.md) |
| member edit | `member edit nus/NUS_ID [name/NAME] [phone/PHONE] [email/EMAIL]` | [Edit member](member-edit.md) |
| member restore | `member restore nus/NUS_ID` | [Restore member](member-restore.md) |
| member delete | `member delete nus/NUS_ID` | [Delete member](member-delete.md) |
| member view | `member view nus/NUS_ID` | [View member](member-view.md) |
| member list | `member list` | [List members](member-list.md) |
| member search | `member search [q/TEXT] [holding/VALUE] [status/active\|deleted\|all] [sort/FIELD] [order/VALUE]` | [Search and planning](search-and-planning.md) |
| equipment add | `equipment add name/NAME category/CATEGORY condition/CONDITION [notes/NOTES] [count/COUNT]` | [Add equipment](equipment-add.md) |
| equipment edit | `equipment edit uuid/UUID [name/NAME] [category/CATEGORY] [condition/CONDITION] [notes/NOTES]` | [Equipment maintenance](equipment-maintenance.md) |
| equipment restore | `equipment restore uuid/UUID` | [Equipment maintenance](equipment-maintenance.md) |
| equipment delete | `equipment delete uuid/UUID` | [Delete equipment](equipment-delete.md) |
| equipment view | `equipment view uuid/UUID` | [View equipment](equipment-view.md) |
| equipment list | `equipment list` | [List equipment](equipment-list.md) |
| equipment search | `equipment search [q/TEXT] [category/TEXT] [condition/VALUE] [availability/VALUE] [status/active\|deleted\|all] [sort/FIELD] [order/VALUE]` | [Search and planning](search-and-planning.md) |
| loans add | `loans add uuid/UUID nus/NUS_ID assigned/DATE expected/DATE` | [Assign equipment](loans-add.md) |
| loans return | `loans return uuid/UUID returned/DATE` | [Return equipment](loans-return.md) |
| loans list | `loans list [nus/TEXT] [uuid/TEXT]` | [Open Loans tab](loans-list.md) |
| loans reset | `loans reset` | [Reset Loans filters](loans-list.md) |
| undo | `undo` | [Undo record change](undo.md) |
| redo | `redo` | [Redo record change](redo.md) |

Families/subcommands ignore case; `members` aliases `member` and `loan` aliases `loans`.
Help and completion use canonical spellings. Field prefixes remain lowercase and case-sensitive. Parameter order is free, and
spaces/tabs separate arguments. Member/equipment list and loans reset accept whitespace only.
Loans list accepts optional identity filters. Other implemented
routes reject missing/blank required values, repeated parameters, unknown markers and
nonblank preamble. Optional notes may be empty. Markers matching the agreed
start/whitespace-delimited `[A-Za-z][A-Za-z0-9_-]*/` grammar are reserved even inside free
text; there is no quoting or escaping. Ordinary embedded slashes and URLs remain text.

The seven Laplace mutation commands execute on an isolated snapshot. LogicManager atomically saves
the candidate before publishing it through the existing live observable root. Parser/business
rejection and save failures preserve all records; rejected operations do not save. Partial candidate
writes and failed replacements preserve the prior data file and useful I/O causes. See
[MVP fields and failure behavior](MvpFields.md) for normalization and ordered diagnostics.

`loans list` opens Loans and retains filters; optional identity substrings replace both filters.
`loans reset` clears both. These commands are read-only and do not save.

## Loan rules

Assignments require an existing active member, existing active AVAILABLE equipment, an assigned date
on or before today, and an expected return on or after assignment. GOOD/FAIR equipment
is available when it has no open loan; DAMAGED/UNDER_REPAIR items cannot be assigned.
Expected returns may be after today. Returns require an open loan and its existing member,
with assigned date <= returned date <= today. Same-day, early and late returns are allowed;
a second return is rejected. Today uses the computer's local date when execution occurs.

Successful operations request the corresponding typed directory/details view. Holder,
holdings and availability are derived from current loans; closing retains immutable loan
history. Open loans block deletion; permitted deletion retains the record and its closed history for restoration.

## Integration verification

`FullCommandRoutingIntegrationTest` runs all ten routes through real LogicManager,
ModelManager and temporary-file JSON storage. It checks automatic saving/reload, distinct
same-name identities, loan lifecycle/holdings/availability, invalid parameter and business
rejection without writes, and save-error propagation. The member/equipment routing and
legacy person-command compatibility scenarios are also retained. Full GUI navigation, refresh and
packaged-window acceptance remain tracked separately in milestone integration issue #75.

Member deletion now retains records and history. See [Restore member](member-restore.md) for lifecycle,
persistence and feature-integration guarantees. Equipment deletion remains unchanged.

## Undo and redo

`undo` and `redo` are no-argument top-level commands. They navigate up to ten successful record changes
per session. Batches are single steps; new successful mutations discard redo. Queries, appearance changes
and failed commands do not affect history. Candidates contain both records and immutable history;
LogicManager publishes both only after the existing atomic save succeeds. Restart clears history while
retaining the last saved records. See [Undo](undo.md) and [Redo](redo.md).
