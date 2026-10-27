# Record a return: `loans return`

Version 1.4 accepts `uuid/UUID1,UUID2` and an optional `condition/CONDITION`.
The supplied condition applies to every returned unit and updates each unit's
current condition. Use separate requests when units have different conditions.
The whole return batch succeeds or fails together. Omitting `condition/` keeps
the existing equipment condition and records the return condition as unknown.
Closed loans, their issue conditions, and their return conditions appear in
the Loans panel and member/equipment details.

Integration status: the return route dispatches to its real command parser through
`LoansCommandParser`. A single return opens equipment details; a batch opens the Loans tab.
Keith's application-shell integration and final GUI acceptance are tracked separately in issue #75.

Format:

```text
loans return uuid/UUID[,UUID...] returned/DATE [condition/CONDITION]
```

Both parameters are required and may appear in either order. Family and subcommand names are
case-insensitive; `loan` aliases `loans`. Prefixes remain lowercase and case-sensitive.
Spaces and tabs separate parameters. Use the equipment UUID and
a real calendar date in `yyyy-MM-dd` format.

Example for the sample camera already held by Alex Tan:

```text
loans return uuid/550e8400-e29b-41d4-a716-446655440001 returned/2026-10-13
```

Replace the example date with the actual return date, which must be on or after the loan's
assignment date and no later than your computer's local date. Same-day, early and late returns
relative to the expected return date are valid. Reaching the expected date never automatically
closes a loan. Today is evaluated using your computer's local date when the command executes.

The equipment, open loan and referenced member must exist. An unknown UUID, missing reference,
item with no open loan, repeated return, or invalid date is rejected without changing any records.
Missing, blank, repeated, malformed or unknown parameters and text before the first parameter
are also rejected. Parameter-marker rules and the lack of quoting/escaping are the same as
for [assignment](loans-add.md).

Success closes each loan using an immutable replacement and preserves returned history. A single
return names its borrower and opens equipment details. Batch feedback lists the returned UUIDs
without attributing all units to one borrower, and opens the Loans tab. Members no longer hold
the returned units. Equipment in `GOOD` or `FAIR` condition becomes available; damaged or
under-repair equipment stays unavailable.

The read-only Loans tab shows open assignments and completed history, with identities, dates,
and recorded conditions. Its
equipment/member links request their detail views. It has no loan-editing or deletion actions.

LogicManager stages the return, atomically saves and then publishes the committed snapshot.
A save failure preserves the open loan, holder, availability and prior saved bytes. See
[MVP fields and failure behavior](MvpFields.md).
