# Edit and restore equipment

```text
equipment edit uuid/UUID [name/NAME] [category/CATEGORY] [condition/CONDITION] [notes/NOTES]
equipment delete uuid/UUID
equipment search status/deleted
equipment restore uuid/UUID
```

Edit requires one or more changed fields and preserves the UUID. It uses equipment-add field validation;
blank `notes/` clears notes, while omitted fields remain unchanged. Duplicate descriptions are valid.
An open loan allows edits (including condition), but blocks deletion. Availability remains ASSIGNED until
return; thereafter condition controls availability. Editing deleted equipment requires restoration first.

Delete sets a persisted flag and retains details, UUID and closed history. Restore clears it without a
time limit, opening the same unit's details. Restoring does not repair condition or reopen old loans.
Unknown identities, repeated lifecycle operations, no-change edits and invalid parameters are rejected.
All three commands use the existing atomic save/publish transaction; failed saves leave live state and
the previous file unchanged. Older records without `deleted` load as active; explicit flags must be booleans.

`equipment list` shows all records, including labeled deleted units. Search defaults to active records;
`status/deleted` and `status/all` make retained units discoverable. Deleted units cannot be loaned and do
not contribute to active inventory totals. Grouped directory rows remain a separate planned UI feature.
