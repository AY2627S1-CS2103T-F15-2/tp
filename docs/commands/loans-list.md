# Open the Loans tab

Version 1.4 also shows completed loan history. The panel has member NUS ID and
equipment UUID filters, with counts for both open and completed matches.
Condition fields from older saves are shown as "Not recorded".

```text
loans list
```

This no-argument navigation command opens the existing Loans tab and reports `Showing current loans.`
It displays current open loans and their existing member/equipment links, including the normal empty
state when no open loans exist. Completed loans appear in a separate history section.

`LoanListCommand` returns `DisplayRequest.loans()` without modifying domain records. The existing
LogicManager save/error pipeline applies, as it does for member/equipment list commands. A save
failure reports an error and preserves the current view. Extra text, prefixes and wrong-case command
words are rejected. `LoanListCommandParser` uses the shared no-argument validation rules.
