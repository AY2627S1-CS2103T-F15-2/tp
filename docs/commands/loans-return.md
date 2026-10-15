# Record a return: `loans return`

Integration status: the return route dispatches to its real command parser through
`LoansCommandParser`. Successful execution requests the affected equipment details.
Keith's application-shell integration and final GUI acceptance are tracked separately in issue #75.

Format:

```text
loans return uuid/UUID returned/DATE
```

Both parameters are required and may appear in either order. Family and subcommand names are
lowercase and case-sensitive. Spaces and tabs separate parameters. Use the equipment UUID and
a real calendar date in `uuuu-MM-dd` format.

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

Success closes the loan using an immutable replacement, preserves returned history and displays
the affected equipment's details. The member no longer holds that item. Equipment in `GOOD` or
`FAIR` condition becomes available; damaged or under-repair equipment stays unavailable.

The read-only Loans tab shows open loans and returned history with identities and dates. Its
equipment/member links request their detail views. It has no loan-editing or deletion actions.

The existing command pipeline saves successful mutations. A save error is reported and does not
promise rollback of an already completed change in memory.
