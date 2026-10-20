# Edit a member

```text
member edit nus/NUS_ID [name/NAME] [phone/PHONE] [email/EMAIL]
```

Example: `member edit nus/a0123456x name/Anne-Marie Lim phone/+65 9876-5432`.

Supply an exact NUS ID and at least one field to change. The ID is normalized to uppercase and
cannot be changed. Omitted details retain their stored values, including legacy saved values.
Supplied fields follow the same validation and normalization as [member add](member-add.md).
Unknown/repeated prefixes, blank values, legacy aliases and unexpected preamble are rejected.
An edit that leaves all values unchanged is rejected without saving.

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
the current stored identity. The JSON member format is unchanged and needs no migration.

Member soft-delete, restore and archival are deferred. Their later implementation must preserve
lifecycle metadata during edits and reject editing a deleted member until restoration. This branch
does not change deletion behavior or implement transfer/history services. Isaac's history work can
use the same successful transactional save/publish boundary as the existing member-add operation.
