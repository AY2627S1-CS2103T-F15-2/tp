# MVP fields and failure behavior

These rules apply to newly entered Laplace commands. The old top-level person commands are retired. Previously saved member/equipment values remain readable and are not normalized
or rewritten merely by loading the file.

| Field | New Laplace input |
| --- | --- |
| Member name | 1-100 Unicode code points after whitespace and NFC normalization, with at least one letter; letters with attached combining marks, spaces, straight (`'`) or curly (`’`) apostrophes, hyphens and periods only. |
| Phone | Optional leading `+`, then 8-15 digits after typed whitespace and hyphens are removed. |
| Email | 3-254 ASCII characters; exactly one `@`; nonempty local part and dotted domain; local characters are letters/digits/`.`/`_`/`%`/`+`/`-`; domain labels cannot begin/end with `-`. Stored lowercase. |
| Equipment name | 1-100 printable Unicode characters after normalization. |
| Category | 1-50 printable Unicode characters after normalization. |
| Notes | Optional, including explicit empty `notes/`; at most 300 printable Unicode characters. Line breaks become spaces through both full-command parsing and pasted command input. |
| Equipment count | Optional `count/`, default 1. ASCII digits representing an integer from 1 to 100; leading zeros accepted. Blank, signed, fractional, out-of-range and repeated values are rejected. |
| NUS ID | One ASCII letter, seven digits and an optional final ASCII letter. Stored uppercase; exact normalized lookup. |
| Equipment UUID | Exactly 8-4-4-4-12 hexadecimal characters with hyphens. Case-insensitive input; lowercase output. Abbreviated Java UUID forms are rejected. |
| Condition | Exactly `GOOD`, `FAIR`, `DAMAGED` or `UNDER_REPAIR`, ignoring case. Space/hyphen aliases are rejected. |
| Date | Exactly `YYYY-MM-DD`, a real Gregorian date with year 0001-9999. Assignment/return cannot be after the local date; expected return is at least assignment and may be in the future. |

Unicode whitespace runs in text are collapsed to an ordinary space and trimmed. Lengths for printable
text count Unicode code points. Other controls, invisible formatting characters and unpaired surrogates
are rejected. Phone formatting is removed before counting digits. Stored legacy values keep their exact
representation, including former short phones, repeated spaces, uppercase emails and longer descriptions.
Member names use canonical Unicode NFC composition before validation and length checking. Equivalent
accented spellings such as `José Tan` and `José Tan` therefore have the same new stored form.
Loading a saved name preserves its exact spelling; editing only another field does not normalize it.

## Error order

The shared argument reader processes supplied fields from left to right. Each field's value is validated
before the next marker. A blank required value occupies its supplied field's position. Unknown and repeated
markers fail when encountered; an earlier invalid field is therefore reported before a later marker error.
For example, `member add phone/123 nus/bad` reports the phone error; reversing these fields reports the NUS ID error.

An absent required field has no position in the input. Missing fields are reported after all supplied fields
have been validated, using lexicographic prefix order as the deterministic tie-breaker. List commands accept
whitespace only. Model/business checks run after parsing and before any records can be committed.

Command words ignore case; `members` and `loan` alias `member` and `loans`.
Prefixes remain lowercase and case-sensitive, and parameter order is free. The reserved
start/whitespace-delimited `[A-Za-z][A-Za-z0-9_-]*/` marker grammar is unchanged. Unknown markers are rejected
even inside text values. No quoting or escaping is introduced; embedded slashes and ordinary URLs remain text.

## Persistence and presentation

Laplace mutation commands execute against an isolated model snapshot. Storage serializes the complete
candidate before creating a private sibling file, flushes that file and atomically replaces the destination.
Only then does LogicManager publish the candidate through the existing live observable root. A write or
replacement failure preserves all records and the prior data file; a later successful command cannot persist
a failed operation. The original I/O cause is retained. Unsupported atomic replacement fails safely rather
than falling back to truncating/copying over the destination. Panels and individual commands never save.

Equipment creation generates UUIDs during execution. Collisions with existing records or identities in the
same batch are silently retried up to 100 attempts per unit. All identities are reserved before insertion;
a null source or exhausted bound fails without adding or saving anything. A later insertion or save failure
discards the isolated candidate, preserving the live state. Success returns all generated UUIDs. The normal
source remains random version 4 UUIDs; a batch is persisted once as separate equipment records.

Member holdings contain only open loans, ordered by expected return date, normalized equipment name and
canonical UUID. Equipment directories retain category/name/UUID ordering. Switching detail identity resets
the scroll position; background refresh preserves it. Inconsistent references produce an explicit error
instead of an incomplete holdings list.

Loans shows current assignments and completed history, with exact member/equipment navigation and dates.
Closed records remain in the model/JSON through deletion/restoration.

The command input replaces pasted tabs and line-break characters with spaces before JavaFX
can strip them. CRLF occupies two input spaces to preserve edit offsets; text value normalization
collapses those spaces before validation/storage. Full-command parsing also accepts raw multiline
arguments, preserving field boundaries and reserved-marker errors. Rejected pasted input stays
available for correction. Deletion refusal reports the actual open-item count/current holder;
reassignment names the current holder, and successful deletion includes the stored record name.

Equipment edits require an exact UUID and at least one changed detail. UUIDs and lifecycle state cannot
be edited. A blank `notes/` clears notes. Descriptive and condition edits are permitted during a loan;
ASSIGNED still takes precedence until return. Deleted units must be restored before editing or lending.
The persisted equipment `deleted` flag defaults to false when absent and rejects null/non-booleans.
Deletion retains closed history and blocks open loans; restoration has no time limit.
