# Search and inventory planning

Search commands show filtered directories without saving data or creating a mutation. `member list`
and `equipment list` reset their directory to all records and the default sort. Criteria are transient
and do not survive an application restart.

## Members

```text
member search [q/TEXT] [holding/any|yes|no] [status/active|deleted|all]
              [sort/name|id|held] [order/asc|desc]
```

`q/` matches a case-insensitive substring of name, NUS ID, phone or email. Comparison normalizes
whitespace without rewriting stored/displayed values. `holding/yes` requires an open loan;
`holding/no` requires none. Closed loans do not count. All supplied filters combine with AND.
Member search defaults to `status/active`, excluding soft-deleted members. Use `status/deleted` to
find retained records for restoration or `status/all` to search both states. Status values are
case-insensitive. `member list` resets to all states and shows deleted cards greyed out, as specified
by the member lifecycle feature. Active and deleted totals are separate from the filtered Displayed count.

```text
member search q/alex
member search holding/yes sort/held order/desc
member search q/example.com holding/no sort/id
member search q/jamie status/deleted
```

Default sorting is normalized name then NUS ID. `sort/id` orders NUS IDs; `sort/held` orders current
holding counts. Ties use default ordering. Descending reverses the complete deterministic order.
Equal names never merge distinct members.

## Equipment

```text
equipment search [q/TEXT] [category/TEXT] [condition/CONDITION]
                 [availability/AVAILABLE|ASSIGNED|UNAVAILABLE] [status/active|deleted|all]
                 [sort/category|name|id|condition|availability] [order/asc|desc]
```

`q/` matches UUID, name, category or notes. `category/` matches a category substring, case-insensitively.
Condition and availability are exact values, case-insensitive. Conditions are GOOD, FAIR, DAMAGED
and UNDER_REPAIR; space/hyphen aliases are not accepted. Filters combine with AND. Search
does not correct spelling or interpret regular expressions.

```text
equipment search q/camera category/photo availability/available
equipment search condition/under_repair sort/name
equipment search sort/availability order/desc
```

Default/category sorting is normalized category, name, then full UUID. Other sorts use their named
field followed by the default comparator. Conditions/availability sort alphabetically by enum name.
Every physical UUID remains a separate row, including units with identical descriptions.
Search defaults to `status/active`; use `status/deleted` to find retained units or `status/all` for both.
Deleted units are UNAVAILABLE, labeled in the directory, and can be viewed/restored by UUID.
The directory total includes deleted units, but availability counts and inventory groups exclude them.

## Input, refresh and navigation

Parameters are optional, but supplied values cannot be blank. Repeated/unknown prefixes, nonblank
preamble, invalid enum/order values and sorts for the other directory fail. `sort/default` explicitly
selects the default comparator. Parameter order is free. Existing reserved-prefix grammar applies:
whitespace-delimited `name/`-style tokens are parameters even inside free text, with no quoting or
escaping. Ordinary URLs/embedded slashes remain text.

The left list and Displayed count reflect the query. Equipment Total includes all retained units;
overall availability and inventory groups describe all active equipment. Criteria and order are visible in the summary. Zero results shows a
reset hint. Opening details retains the filtered list. Refresh reevaluates queries against current
data, including loan-only changes, without executing row callbacks. Errors retain criteria and input
for correction. List commands clear filters.
Deleting a result keeps the active filter and sort, including when no matches remain. Only an explicit
list/reset command clears them; returning to a directory after a mutation does not reset the query.

## Inventory groups and duplicates

Equipment summaries group all inventory by normalized **category / equipment name** (equipment type).
Category and type are separate keys; separator characters never merge distinct pairs. Labels quote
each value and double embedded quotes so names containing `/` or quotation marks remain distinguishable.
Each group shows available, assigned and unavailable physical units. Open loans mean assigned;
otherwise GOOD/FAIR is available and DAMAGED/UNDER_REPAIR unavailable. Groups are independent of the
current filter, so narrowing a directory does not silently reduce planning totals.

Successful member additions warn about other active NUS IDs sharing a normalized name, phone or email.
Deleted members are excluded from similarity warnings; their retained NUS IDs still cannot be reused.
Successful equipment additions warn about other UUIDs sharing normalized category and name.
Warnings identify the existing records and are advisory: legitimate same-name members and multiple
physical units can still be added. Existing identity-duplicate rejection is unchanged.

## Integration contract

Integrated baseline: team `823efd5e`, including member editing, soft-delete and restoration. Queries
use the actual `Member.isDeleted()` contract. Search commands default to active members, explicit
status filters find retained records, and list/reset keeps both states visible. Global active counts
exclude deleted records even when a filtered view displays only deleted members. Delete/restore
reevaluates filters and counts without erasing identities or loan history. Existing seven-argument
DirectoryQuery callers retain all-records behavior; the status-aware search parser supplies the new
member criterion explicitly. No new domain schema or reflection is introduced.

`DirectoryQuery.members(data, eligible)` composes extra eligibility with the status filter.
`DirectoryQuery.equipment(data, lookup, eligible)` and `InventoryGroups.summarize(data, lookup, eligible)`
apply eligibility before availability lookup. Equipment lifecycle metadata is not present on this
baseline; its future domain contract must be applied consistently when merged. The revised Week 10
delegation removes archive/unarchive and the 30-day restoration limit; neither is assumed here.

DisplayRequest optionally carries an immutable DirectoryQuery for MEMBERS/EQUIPMENT. A plain directory
request preserves existing criteria; a request carrying a query replaces them. MemberListCommand and
EquipmentListCommand explicitly carry DirectoryQuery.all() to reset filters/sorting. Search commands
carry their supplied query; deletion and stale-detail fallback use plain navigation requests.
MainWindow integration sets query criteria on lists
and summaries, recovers selection using displayed order, and retains criteria during details mode.
Keith remains the shell integrator; no shared theme/main FXML changes are needed.

Command.isReadOnly() defaults to false for compatibility and is true for searches. LogicManager
bypasses saving for these commands. Isaac's history pipeline must likewise exclude queries.
Existing list/legacy save behavior remains unchanged. Transfer PR #107 was closed without merging;
team master has the integrated member lifecycle contracts. When later transfer/history changes touch
Command/LogicManager/MainWindow, retain query dispatch and the no-save/no-history query boundary.
The member parser and diagnostic retain `search`, `edit` and `restore`. No unmerged teammate
implementation is copied into this branch. Other owners' feature acceptance remains separate from
this search/planning PR.
