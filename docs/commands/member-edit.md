# Edit a member

```text
member edit nus/NUS_ID [name/NAME] [phone/PHONE] [email/EMAIL]
```

Example: `member edit nus/a0123456x name/Anne-Marie Lim phone/+65 9876-5432`.

Supply an exact NUS ID and at least one field to change. The ID is normalized to uppercase and
cannot be changed. Omitted details retain their stored values, including legacy saved values.
Supplied fields follow the same validation and normalization as [member add](member-add.md).
Unknown/repeated prefixes, blank values, legacy aliases and unexpected preamble are rejected.
An edit that leaves all values unchanged is rejected without saving. Deleted members cannot be edited,
even when the supplied values are unchanged; run `member restore nus/NUS_ID` first.

Editing is allowed while a member holds equipment. Open and completed loans retain their existing
NUS-ID references, and no equipment, other member or legacy person record is changed. Different IDs
may still have matching names and contact details.

Success opens the edited member's details. The directory re-sorts by name/NUS ID and keeps that
identity selected; cards, summaries and loan displays refresh from the stored replacement.

`MemberEditCommand` is transactional. `LogicManager` edits an isolated model, atomically saves it,
then publishes the replacement through the existing observable root. Invalid requests and no-op
edits do not save. A failed save preserves live records, loan history and the previous file bytes.

## Integration contract

`Model.setMember(Member target, Member editedMember)` replaces details by normalized NUS ID.
The collection rejects an identity change and a missing target. It retains collection order and
the exposed observable view; the shell owns sorted presentation. An older target snapshot resolves
the current stored identity. Replacements preserve the deletion flag; changing lifecycle state
requires the dedicated delete/restore operations.

Member soft-delete and unlimited restore are integrated; archival remains outside scope.
Editing never restores a deleted member. Isaac's history work can
use the same successful transactional save/publish boundary as the existing member-add operation.
