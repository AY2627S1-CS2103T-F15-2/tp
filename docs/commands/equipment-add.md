# Equipment addition

```text
equipment add name/NAME category/CATEGORY condition/CONDITION [notes/NOTES] [count/COUNT]
```

Parameters may appear in any order. Name/category/condition are required; notes may be omitted or
explicitly empty. Names allow 1-100 printable Unicode characters, categories 1-50 and notes at most
300. Text whitespace is normalized; notes line breaks become spaces. Conditions accept exactly
GOOD, FAIR, DAMAGED and UNDER_REPAIR, ignoring case. `under repair` and `under-repair` are invalid.
See [MVP fields](MvpFields.md) for complete validation and first-error rules.

`count/` defaults to 1. An explicit count must use ASCII digits and represent an integer from 1 to 100;
leading zeros are accepted. Blank values, signs, fractions, overflow and repeated count prefixes fail.
The limit bounds accidental bulk input and result size. It is a product limit, not a course constraint.

```text
equipment add name/Camera Kit category/Photography condition/GOOD count/3 notes/Includes strap
```

Each unit is a full equipment record with its own UUID. UUIDs are generated at execution, using random
version 4 identifiers normally. All identities are reserved before adding records, checking both existing
UUIDs and others in this batch. Each unit has a 100-attempt collision safeguard. A null generator result
or exhaustion fails before any insertion. Duplicate collection errors abort the transaction with their cause.

GOOD/FAIR items begin AVAILABLE; DAMAGED/UNDER_REPAIR items begin UNAVAILABLE. Availability is derived
from condition and open loans, never persisted as an extra field. No quantity field or schema change is needed.

Count 1 retains the existing feedback and details navigation. A larger batch reports the count, name,
availability and all new UUIDs, then opens the full equipment directory with filters/sorting reset.
The duplicate warning is computed once against records that existed before the batch; matching units
created together are intentional and do not warn about one another.

LogicManager stages the addition in an isolated model, atomically saves once and then publishes through
the live root. A failure during insertion or saving commits none of the batch; a later successful command
cannot persist failed additions. Direct command execution is an internal operation on the supplied model;
call through LogicManager for the transaction guarantee. Individual commands/cards do not write storage.
