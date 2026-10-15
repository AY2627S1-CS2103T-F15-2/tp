# Laplace command routing reference

## Integration status

All ten routes dispatch to their concrete individual parsers. Command execution uses the
LogicManager persistence boundary. Merged #93 integrates the real shell and #95 activates all ten routes.
Current MVP completion evidence is in [MvpCompletion.md](../gui/MvpCompletion.md); #75 remains the final gate.

| Command | Syntax | Reference |
| --- | --- | --- |
| member add | `member add nus/NUS_ID name/NAME phone/PHONE email/EMAIL` | [Add member](member-add.md) |
| member delete | `member delete nus/NUS_ID` | [Delete member](member-delete.md) |
| member view | `member view nus/NUS_ID` | [View member](member-view.md) |
| member list | `member list` | [List members](member-list.md) |
| equipment add | `equipment add name/NAME category/CATEGORY condition/CONDITION [notes/NOTES]` | [Add equipment](equipment-add.md) |
| equipment delete | `equipment delete uuid/UUID` | [Delete equipment](equipment-delete.md) |
| equipment view | `equipment view uuid/UUID` | [View equipment](equipment-view.md) |
| equipment list | `equipment list` | [List equipment](equipment-list.md) |
| loans add | `loans add uuid/UUID nus/NUS_ID assigned/DATE expected/DATE` | [Assign equipment](loans-add.md) |
| loans return | `loans return uuid/UUID returned/DATE` | [Return equipment](loans-return.md) |

Families/subcommands are lowercase and case-sensitive. Parameter order is free, and
spaces/tabs separate arguments. List commands accept whitespace only. Other implemented
routes reject missing/blank required values, repeated parameters, unknown markers and
nonblank preamble. Optional notes may be empty. Markers matching the agreed
start/whitespace-delimited `[A-Za-z][A-Za-z0-9_-]*/` grammar are reserved even inside free
text; there is no quoting or escaping. Ordinary embedded slashes and URLs remain text.

The six Laplace mutation commands execute on an isolated snapshot. LogicManager atomically saves
the candidate before publishing it through the existing live observable root. Parser/business
rejection and save failures preserve all records; rejected operations do not save. Partial candidate
writes and failed replacements preserve the prior data file and useful I/O causes. See
[MVP fields and failure behavior](MvpFields.md) for normalization and ordered diagnostics.

Existing person commands and data remain supported. No `loans list` CLI command is
introduced; the Loans tab is a read-only presentation of current open loans only. Closed records remain stored without a history presentation.

## Loan rules

Assignments require an existing member, existing AVAILABLE equipment, an assigned date
on or before today, and an expected return on or after assignment. GOOD/FAIR equipment
is available when it has no open loan; DAMAGED/UNDER_REPAIR items cannot be assigned.
Expected returns may be after today. Returns require an open loan and its existing member,
with assigned date <= returned date <= today. Same-day, early and late returns are allowed;
a second return is rejected. Today uses the computer's local date when execution occurs.

Successful operations request the corresponding typed directory/details view. Holder,
holdings and availability are derived from current loans; closing retains immutable loan
history. Open loans block deletion; permitted deletion removes associated closed history.

## Integration verification

`FullCommandRoutingIntegrationTest` runs all ten routes through real LogicManager,
ModelManager and temporary-file JSON storage. It checks automatic saving/reload, distinct
same-name identities, loan lifecycle/holdings/availability, invalid parameter and business
rejection without writes, and save-error propagation. The member/equipment routing and
legacy person-command compatibility scenarios are also retained. Full GUI navigation, refresh and
packaged-window acceptance remain tracked separately in milestone integration issue #75.
