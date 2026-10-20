# Assign equipment: `loans add`

Version 1.4 accepts a comma-separated list of equipment UUIDs in `uuid/`. For example,
`uuid/UUID1,UUID2` assigns two physical units to the same member with the same dates.
Each UUID must be unique and available. The whole request succeeds or fails together.
The condition of each unit at assignment is recorded in its loan history. The
existing single-UUID form continues to work.

Integration status: the assignment route dispatches to its real command parser through
`LoansCommandParser`. Successful execution requests the affected equipment details.
Keith's application-shell integration and final GUI acceptance are tracked separately in issue #75.

Format:

```text
loans add uuid/UUID nus/NUS_ID assigned/DATE expected/DATE
```

All four parameters are required and may appear in any order. Family and subcommand names are
lowercase and case-sensitive. Spaces and tabs separate parameters. Dates use `yyyy-MM-dd`,
for example `2026-10-13`; replace example dates with dates appropriate to your computer's local date.
NUS IDs are case-insensitive and normalize to uppercase. Use the item's UUID, rather than a row
number or equipment name, to distinguish identically named physical items.

Example for the available sample camera and Jamie Lim:

```text
loans add uuid/550e8400-e29b-41d4-a716-446655440000 nus/A0234567R assigned/2026-10-13 expected/2026-10-20
```

The member and equipment must exist at execution time. Equipment must be `AVAILABLE`: it has no
open loan and its condition is `GOOD` or `FAIR`. Already assigned, damaged and under-repair items
cannot be issued. The assignment date must be no later than today, and the expected return date
must be on or after assignment. An expected return date in the future is valid. Today is evaluated
using your computer's local date when the command executes.

Success records an open loan and displays the affected equipment's details. Holder, member
holdings and availability are derived from that loan. Invalid input or a failed validation leaves
all records unchanged.

Missing, blank, repeated, malformed or unknown parameters and text before the first parameter
are errors. A parameter marker starts at the beginning of input or after whitespace and matches
`[A-Za-z][A-Za-z0-9_-]*/`. These markers are reserved even inside free text; quoting and escaping
are unsupported. URLs such as `https://example.com` and slashes inside ordinary words are not
parameter markers.

The existing command pipeline saves successful mutations. If saving fails, the application reports
the error; it does not promise to undo an already completed change in memory.

Assignment is staged and atomically persisted before live publication. A failed save leaves all
collections and saved bytes unchanged. See [MVP fields](MvpFields.md) for canonical UUIDs, exact
conditions/dates and supplied-field error ordering. Loans presents open assignments and completed history.

Assignment refusal for an existing open loan names its current holder NUS ID, including when
the requested new holder differs. The current loan is read before attempting any mutation.
