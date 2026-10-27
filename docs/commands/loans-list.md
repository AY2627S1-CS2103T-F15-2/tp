# Open and filter the Loans tab

```text
loans list [nus/TEXT] [uuid/TEXT]
loans reset
```

Bare `loans list` opens the existing panel and retains both filters. Supplying either optional
parameter replaces both: omitted or blank fields are cleared. Values are case-insensitive identity
substrings with surrounding whitespace removed, rather than complete validated NUS IDs or UUIDs.
Both supplied criteria must match. Open and completed history share the same predicate, including
history retained for deleted members/equipment. Condition fields from older saves show "Not recorded".

`loans reset` clears both fields, matching **Reset filters**. `loans list nus/ uuid/` also clears them.
Navigation and mutations preserve intentional filters. Visible fields and displayed/total counts stay
synchronized when commands or direct field edits change the criteria. Invalid commands do not change them.

Both routes return a read-only `LoanListCommand`: no persistence or undo step occurs. A list without
parameters returns `DisplayRequest.loans()`; filtered/reset commands carry an immutable `LoanQuery`.
`MainWindow` applies that typed query, while `LoanPanel` uses the same predicate for direct GUI edits.
Unknown/duplicate markers and nonblank preamble are rejected with canonical usage guidance.
Feedback remains `Showing current loans.`

Examples: `loans list nus/a012`, `loans list uuid/400004`, `loans list nus/A012 uuid/400004`, `loans reset`.
