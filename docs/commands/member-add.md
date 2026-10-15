# Add a member

```text
member add nus/NUS_ID name/NAME phone/PHONE email/EMAIL
```

Example: `member add nus/a0123456x name/Anne-Marie Lim phone/+65 9123-4567 email/ANNE@EXAMPLE.COM`

All four fields are required in any order. NUS IDs are stored uppercase and identify a member exactly.
Names allow 1-100 Unicode letters/spaces/apostrophes/hyphens/periods with at least one letter. Phone
whitespace/hyphens are removed, leaving 8-15 digits and an optional leading `+`. Emails follow the MVP
ASCII syntax and are stored lowercase. Text whitespace is collapsed. See [MVP fields](MvpFields.md).

Different NUS IDs may share names and contacts. A duplicate normalized ID is rejected. Supplied fields
are validated in reading order; unknown/repeated markers, blank required values and unexpected preamble
are rejected. Missing fields are reported afterward in lexicographic prefix order. Legacy `n/`, `p/`
and `e/` are not member aliases; their separate person commands and saved values remain compatible.

Success selects the new identity and opens real member details. LogicManager stages the addition,
atomically saves the candidate, then publishes it through the existing live root. A failed save leaves
memory and saved records unchanged. The command and member card do not write storage themselves.

The route and shell are integrated through merged #95/#93. The member card still uses its existing
snapshot/callback contract; parent lists own selection, sorting and refresh. Final milestone acceptance
remains tracked separately in #75.
