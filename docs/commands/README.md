# Laplace command routing reference

## Integration status

On the local `v1.3-final-routing` branch, all eight member/equipment routes dispatch to
real individual parsers. Loan routes remain explicit placeholders until Isaac's loan
commands and parsers merge. This branch is not published as a final routing PR yet.
The application shell is integrated separately by Keith.

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
| loans add (pending) | `loans add uuid/UUID nus/NUS_ID assigned/DATE expected/DATE` | [Assign equipment](loans-add.md) |
| loans return (pending) | `loans return uuid/UUID returned/DATE` | [Return equipment](loans-return.md) |

Families/subcommands are lowercase and case-sensitive. Parameter order is free, and
spaces/tabs separate arguments. List commands accept whitespace only. Other implemented
routes reject missing/blank required values, repeated parameters, unknown markers and
nonblank preamble. Optional notes may be empty. Markers matching the agreed
start/whitespace-delimited `[A-Za-z][A-Za-z0-9_-]*/` grammar are reserved even inside free
text; there is no quoting or escaping. Ordinary embedded slashes and URLs remain text.

Commands retain the existing LogicManager save pipeline. Parser/business validation
failures do not save or modify domain data. An I/O save failure is reported with its
cause and does not promise rollback of a preceding in-memory mutation.

Existing AB3 person commands and data remain supported. Loan records used by the current
routing integration tests are valid model fixtures, not evidence of completed loan CLI
routing. Final route activation and all-ten-command acceptance remain pending Isaac's
merged parsers. No `loans list` CLI command is introduced.
