# Equipment addition

```text
equipment add name/NAME category/CATEGORY condition/CONDITION [notes/NOTES]
```

The brackets mark optional input; do not type them. Arguments may appear in any
order, separated by spaces or tabs. Names and categories are trimmed, required,
and may contain punctuation (for example `name/Canon EOS-R50 (kit)`). Notes are
trimmed and default to an empty string, including when `notes/` is explicitly empty.
Conditions are `GOOD`, `FAIR`, `DAMAGED`, and `UNDER_REPAIR`, ignoring case;
`under repair` and `under-repair` are also accepted.

Missing or blank required fields, repeated parameters (including notes), unknown
parameters, and text before the first parameter are rejected. Whitespace-delimited
or start-of-input tokens matching `[A-Za-z][A-Za-z0-9_-]*/` are reserved markers,
even within notes. There is no quoting or escaping. URLs such as
`https://example.com` and ordinary internal slashes remain text.

Each execution generates a UUID. Identical descriptions may represent different
physical units and receive separate identities. A generated UUID collision fails
without changing any collection; retry the command. GOOD/FAIR items begin AVAILABLE;
DAMAGED/UNDER_REPAIR items begin UNAVAILABLE. Availability comes from the model and
is never persisted as a separate field. Success identifies the item, UUID and
availability and requests the new equipment's details screen.

`EquipmentCard(Equipment, Availability, Optional<Member>)` renders a snapshot of
name, category, full UUID, condition, availability and holder name/NUS ID. An
unresolved holder on ASSIGNED equipment is explicitly marked as a missing reference.
Its fields wrap at narrow widths; its parent owns selection and callbacks.

## Integration boundary

This contribution supplies `EquipmentAddCommand`, `EquipmentAddCommandParser` and
`EquipmentCard`; it does not activate the family router or assemble the application
shell. At baseline `97ee5c3370fc517cdd3586c25b0ad0f08f874c22`, `equipment add` still
routes to a placeholder. Duy must route it to the individual parser and consume the
card in the equipment list. Keith must consume the typed display request in the
shell. Full command execution through LogicManager, automatic saving, and visible
selection/details must be verified after those integrations land.

Direct parser/command integration tests use a real ModelManager and temporary-file
JsonAddressBookStorage. They verify all four conditions, IDs, notes and derived
availability after reload. Saving remains the responsibility of LogicManager;
commands never write storage. The existing pipeline does not roll back in-memory
changes when saving fails, and this feature does not promise such rollback.
