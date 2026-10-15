# Equipment addition

```text
equipment add name/NAME category/CATEGORY condition/CONDITION [notes/NOTES]
```

Parameters may appear in any order. Name/category/condition are required; notes may be omitted or
explicitly empty. Names allow 1-100 printable Unicode characters, categories 1-50 and notes at most
300. Text whitespace is normalized; notes line breaks become spaces. Conditions accept exactly
GOOD, FAIR, DAMAGED and UNDER_REPAIR, ignoring case. `under repair` and `under-repair` are invalid.
See [MVP fields](MvpFields.md) for complete validation and first-error rules.

UUIDs are generated at execution, using random version 4 identifiers normally. Identical descriptions
are distinct physical units. Existing UUID collisions are silently retried, with a 100-attempt safeguard
for a pathological generator. Failure leaves existing equipment unchanged; only a successful addition
is saved. Feedback and the typed display request contain the actual new UUID.

GOOD/FAIR items begin AVAILABLE; DAMAGED/UNDER_REPAIR items begin UNAVAILABLE. Availability is derived
from condition and open loans, never persisted as an extra field. Success selects the new row and opens
the existing details panel. The card preserves the complete UUID, condition, availability and holder.

The route and real shell are integrated through merged #95/#93. LogicManager stages the addition,
atomically saves and then commits through the live root. A failed save does not create an in-memory
item and cannot be persisted by a later command. Individual commands/cards do not write storage.
