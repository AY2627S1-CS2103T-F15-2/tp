# Assign equipment: `loans add`

Integration status: `LoanAddCommand` and its individual parser are implemented for issue #76.
The current application route remains a placeholder until Duy connects `LoansCommandParser` to
`LoanAddCommandParser`. Keith's shell integration handles the resulting equipment-details request.

Format:

```text
loans add uuid/UUID nus/NUS_ID assigned/DATE expected/DATE
```

All four parameters are required and may appear in any order. Family and subcommand names are
lowercase and case-sensitive. Spaces and tabs separate parameters. Dates use `uuuu-MM-dd`,
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
