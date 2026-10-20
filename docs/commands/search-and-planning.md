# Search and inventory planning

Search commands show filtered directories without saving data or creating a mutation. `member list`
and `equipment list` reset their directory to all records and the default sort. Criteria are transient
and do not survive an application restart.

## Members

```text
member search [q/TEXT] [holding/any|yes|no] [sort/name|id|held] [order/asc|desc]
```

`q/` matches a case-insensitive substring of name, NUS ID, phone or email. Comparison normalizes
whitespace without rewriting stored/displayed values. `holding/yes` requires an open loan;
`holding/no` requires none. Closed loans do not count. All supplied filters combine with AND.

```text
member search q/alex
member search holding/yes sort/held order/desc
member search q/example.com holding/no sort/id
```

Default sorting is normalized name then NUS ID. `sort/id` orders NUS IDs; `sort/held` orders current
holding counts. Ties use default ordering. Descending reverses the complete deterministic order.
Equal names never merge distinct members.

## Equipment

```text
equipment search [q/TEXT] [category/TEXT] [condition/CONDITION]
                 [availability/AVAILABLE|ASSIGNED|UNAVAILABLE]
                 [sort/category|name|id|condition|availability] [order/asc|desc]
```

`q/` matches UUID, name, category or notes. `category/` matches a category substring, case-insensitively.
Condition and availability are exact values, case-insensitive. Conditions are GOOD, FAIR, DAMAGED
and UNDER_REPAIR, including the existing space/hyphen aliases. Filters combine with AND. Search
does not correct spelling or interpret regular expressions.

```text
equipment search q/camera category/photo availability/available
equipment search condition/under-repair sort/name
equipment search sort/availability order/desc
```

Default/category sorting is normalized category, name, then full UUID. Other sorts use their named
field followed by the default comparator. Conditions/availability sort alphabetically by enum name.
Every physical UUID remains a separate row, including units with identical descriptions.

## Input, refresh and navigation

Parameters are optional, but supplied values cannot be blank. Repeated/unknown prefixes, nonblank
preamble, invalid enum/order values and sorts for the other directory fail. `sort/default` explicitly
selects the default comparator. Parameter order is free. Existing reserved-prefix grammar applies:
whitespace-delimited `name/`-style tokens are parameters even inside free text, with no quoting or
escaping. Ordinary URLs/embedded slashes remain text.

The left list and Displayed count reflect the query. Total/active, overall availability and inventory
groups describe all inventory. Criteria and order are visible in the summary. Zero results shows a
reset hint. Opening details retains the filtered list. Refresh reevaluates queries against current
data, including loan-only changes, without executing row callbacks. Errors retain criteria and input
for correction. List commands clear filters.

## Inventory groups and duplicates

Equipment summaries group all inventory by normalized **category / equipment name** (equipment type).
Each group shows available, assigned and unavailable physical units. Open loans mean assigned;
otherwise GOOD/FAIR is available and DAMAGED/UNDER_REPAIR unavailable. Groups are independent of the
current filter, so narrowing a directory does not silently reduce planning totals.

Successful member additions warn about other NUS IDs sharing a normalized name, phone or email.
Successful equipment additions warn about other UUIDs sharing normalized category and name.
Warnings identify the existing records and are advisory: legitimate same-name members and multiple
physical units can still be added. Existing identity-duplicate rejection is unchanged.

## Integration contract

Baseline: team `c4e2001de6ec09389d827260d1a985093aa64bc0`. This baseline has no archive/deletion
metadata, so all stored entities are currently eligible. Never guess lifecycle flags from names.
`DirectoryQuery.members(data, eligible)`, `DirectoryQuery.equipment(data, lookup, eligible)` and
`InventoryGroups.summarize(data, lookup, eligible)` accept lifecycle predicates. Eligibility is applied
before availability lookup. When domain lifecycle changes merge, wire their actual active/eligible
predicates consistently into commands, panels, summaries and duplicate checks. This remains an
explicit cross-workstream dependency; no competing domain schema or reflection is introduced.

DisplayRequest optionally carries an immutable DirectoryQuery for MEMBERS/EQUIPMENT. Existing
factories/targets retain their behavior. Minimal MainWindow integration sets query criteria on lists
and summaries, recovers selection using displayed order, and retains criteria during details mode.
Keith remains the shell integrator; no shared theme/main FXML changes are needed.

Command.isReadOnly() defaults to false for compatibility and is true for searches. LogicManager
bypasses saving for these commands. Isaac's history pipeline must likewise exclude queries.
Existing list/legacy save behavior remains unchanged. Keith's in-flight transfer work also touches
Command/LogicManager/MainWindow: retain both transfer dispatch and query handling when merging.
No unmerged transfer/history/lifecycle implementation is copied into this branch.
