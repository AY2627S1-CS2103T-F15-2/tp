---
  layout: default.md
  title: "User Guide"
  pageNav: 3
---

# Laplace User Guide

Laplace helps a CCA EXCO member manage **members, equipment and equipment loans** on one computer.
Type commands to register records, find an exact member or item, issue equipment, record returns and
remove erroneous records. Lists and details show who holds each item and what each member holds.

This guide describes the **MVP release**. Use it with the matching release supplied by the team;
earlier builds may not support every behavior described here. Laplace is a local, single-user application.

<page-nav-print />

## Quick start

1. Install Java **25** for your operating system and processor architecture.
2. Obtain the Laplace MVP JAR supplied by the team. Published builds are listed on the
   [team Releases page](https://github.com/AY2627S1-CS2103T-F15-2/tp/releases).
3. Put the JAR in a folder you can write to. Keep the application and its data in this folder.
4. Open a terminal in that folder and run `java -jar addressbook.jar`. Substitute the actual filename
   if your supplied JAR has a different name. Keep the terminal open while using the application.
5. On the first launch without a saved data file, Laplace provides sample records. Existing saved data
   takes precedence; restarting does not add another set of samples.
6. Type `member list` in the command box and press **Enter**. Try `equipment list` next.
7. Use `help` or **F1** to open Help, and `exit` to close the application normally.

![Laplace directory layout reference](images/Ui.png)

The image is a layout reference. Your rows, counts and selected record depend on your data.
If startup fails, check the JAR filename and Java version and keep the terminal error for the team.
Do not delete your data file to troubleshoot startup.

## Using the window

- **Members** opens the member directory. **Equipment** opens the equipment directory.
- The left panel contains selectable records. The right panel shows a directory summary or record
  details. Startup selects the first member, when present, while leaving the summary visible.
- Click a record, or focus its row and press **Enter** or **Space**, to open its details.
  Identically named records remain distinguishable by NUS ID or equipment UUID.
- The **Loans** tab shows current open assignments and links to their member and equipment.
  Completed loan history is not a user-facing MVP feature. There is no `loans list` command.
- The feedback area reports the last command's result. The command box and Enter hint appear below it.
- At smaller sizes, use the list/detail scrollbars to reach long identifiers, notes and holdings.

## Command format and feedback

Commands and prefixes are **lowercase and case-sensitive**. Use `member add`, not `Member Add`.

- Replace uppercase placeholders with your values: replace `NUS_ID` with `A0123456X`, for example.
- Square brackets mean an optional parameter; do not type them. Only equipment notes are optional.
- Put spaces between command words and parameters. Parameters may appear in any order; tabs also separate fields.
- Each prefix may appear **at most once**, including `notes/`.
- Leading/trailing whitespace is ignored. Runs of whitespace in text values become one ordinary space.
- A value continues to the next parameter marker. A start-of-value or whitespace-delimited token such as
  `notes/` or `unknown/` is reserved as a marker. Unknown markers are errors, even inside free text.
  There is no quoting or escaping. An ordinary URL such as `https://example.com` is not a marker.
- Use the full field names: `nus/`, `name/`, `phone/` and `email/`. Short alternatives are not accepted.
- `member list` and `equipment list` take no arguments. Additional text or prefixes are rejected.
- An empty or whitespace-only submission does nothing.

On success, the input clears and the requested list/details view appears. Newly added records are selected
and open in details. On failure, the command stays in the input box, focus returns there, and the current
view and selection remain unchanged. Correct it and press Enter again. Only the first detected error is
shown; another error may appear after correction. Rejected commands do not change records.

### Shared field rules

| Field | Accepted input and stored form |
| --- | --- |
| NUS ID `nus/` | One ASCII letter, exactly seven digits, and an optional final ASCII letter. Case-insensitive; stored/displayed uppercase, such as `A0123456X` or `E0123456`. |
| Member name `name/` | 1–100 characters after normalization. Unicode letters, spaces, apostrophes, hyphens and periods only; must contain at least one letter. Example: `Anne-Marie O'Connor`. |
| Phone `phone/` | Optional leading `+`, then 8–15 digits. Spaces and hyphens are removed: `+65 9123-4567` becomes `+6591234567`. |
| Email `email/` | 3–254 ASCII characters, one `@`, no spaces and at least one domain dot. Local-part characters: letters, digits, `.`, `_`, `%`, `+`, `-`. Domain labels contain letters/digits/hyphens and cannot begin/end with a hyphen. Stored lowercase. |
| Equipment UUID `uuid/` | Full `8-4-4-4-12` hexadecimal form, such as `550e8400-e29b-41d4-a716-446655440000`. Case-insensitive input; lowercase display. Shortened UUIDs are rejected. |
| Equipment name `name/` | 1–100 printable Unicode characters after whitespace normalization. Punctuation and model numbers are allowed; control characters are rejected. |
| Category `category/` | 1–50 printable Unicode characters after normalization. Free text; control characters are rejected. |
| Condition `condition/` | Exactly `GOOD`, `FAIR`, `DAMAGED` or `UNDER_REPAIR`, ignoring case. Stored uppercase. Use the underscore in `UNDER_REPAIR`. |
| Notes `notes/` | Optional, at most 300 printable Unicode characters after trimming. Line breaks become spaces. Omitted or empty notes mean no notes. |
| Dates | Exactly `YYYY-MM-DD` and a real Gregorian calendar date, such as `2026-10-15`. Assignment/actual-return dates cannot be later than the computer's local date. Expected return may be in the future. |

Copy the actual UUID displayed by Laplace when issuing, viewing, returning or deleting equipment.
The UUID in this guide is an example; it does not identify every newly added item.

### Availability

| Item state | Displayed availability | Can be assigned? |
| --- | --- | --- |
| Has an open loan, regardless of condition | `ASSIGNED` | No |
| No open loan and condition `GOOD` or `FAIR` | `AVAILABLE` | Yes |
| No open loan and condition `DAMAGED` or `UNDER_REPAIR` | `UNAVAILABLE` | No |

You do not enter availability or a holder manually. Laplace derives both from the item and its open loan.
A member can hold multiple different items; one physical item can have only one open loan.

### Correcting common errors

| Problem | What to do |
| --- | --- |
| Missing or blank required field | Supply a nonempty value for every required prefix shown in the command's usage. |
| Repeated prefix | Keep one occurrence of that prefix, including optional notes. |
| Unknown parameter or unexpected text | Remove the unrecognized marker or text before the first parameter. Check the full prefix names. |
| Invalid field | Follow the field rule above and the displayed field-specific explanation. |
| Unknown identity | Copy the full NUS ID or UUID from its list. Names and row numbers are not identifiers. |
| Command rejected by a loan rule | Return an assigned item before deleting its member/item or assigning that item again. |
| Data cannot be saved | Check that the application folder/data file are writable, correct the file-access problem, then retry. Failed MVP mutations leave records unchanged. |

## Features

### Add a member: `member add`

Create one active member with a unique, immutable NUS ID.

```text
member add nus/NUS_ID name/NAME phone/PHONE email/EMAIL
```

All four parameters are required and follow the shared field rules. No address is required.

Examples:

```text
member add nus/A0123456X name/Alex Tan phone/91234567 email/alex.tan@u.nus.edu
member add email/siti@u.nus.edu name/Siti Nur phone/+65 8123-4567 nus/E0123456
```

Success adds the member, selects the new row and opens details showing the normalized values.
For the first example, the result is `Added member A0123456X: Alex Tan.`

Two members are duplicates **only when their normalized NUS IDs are equal**. Different IDs can share
a name, phone or email. A duplicate is rejected with a message such as:
`Member A0123456X already exists. Use member view to inspect it.`

Missing, blank, malformed, repeated or unknown parameters are rejected. Failure creates no member and
preserves the current view/selection. If saving fails, correct file access and retry the retained command.

### List members: `member list`

```text
member list
```

Show every active member, ordered by normalized name and then NUS ID. Each row shows the member's name,
NUS ID, phone, email and **Items Held** count. All stored members are active; archiving is not supported.
The directory summary shows the active/displayed counts, sorting and selection.

The result is `Showing N member(s).` If there are no records, the list displays:
`No members found. Add one with member add.`

This command takes no parameters. For example, `member list nus/A0123456X` is rejected; use
`member view nus/A0123456X` for an exact lookup. A syntax failure leaves the current screen unchanged.
Inconsistent stored data produces an error rather than a partial list.

### View a member and their equipment: `member view`

```text
member view nus/NUS_ID
```

Example: `member view nus/a0123456x` finds the same identity as `member view nus/A0123456X`.

Details show the NUS ID, name, phone, email and current held-item count. The **Held Equipment** section
shows each open loan's full equipment UUID, name, assignment date and expected return date. Items are
ordered by **expected return date, equipment name, then UUID**. Returned items are not current holdings.
The matching member row is selected when the member list is visible.

Example feedback: `Showing member A0123456X: Alex Tan. Currently holding N item(s).`
With no current loans, the holdings section says `This member currently holds no equipment.`

| Situation | Result or correction |
| --- | --- |
| ID exists | Open that exact member's details; equal names do not affect lookup. |
| ID is well formed but does not exist | `No member found with NUS ID NUS_ID.` Copy an existing ID from `member list`. |
| ID is incomplete or malformed | Correct it using the shared NUS ID rule. A well-formed shorter ID is still an exact identity, not a partial-name or partial-ID search. |
| Required parameter is missing, repeated or unknown | Correct the command to `member view nus/NUS_ID`. |
| A current loan refers to missing equipment | Report inconsistent stored loan data; do not show an incomplete holdings list. |

Failed viewing preserves the previous details and selection. This command covers both exact-ID lookup
and viewing a member's equipment; no separate member-find or member-loans command is needed.

### Delete a member: `member delete`

```text
member delete nus/NUS_ID
```

Example: `member delete nus/A0123456X`.

Delete one exact member **only if they currently hold no equipment**. Equal-name members with other
IDs remain. Success removes the member from lists/details and permanently removes closed loans that
refer to that member. Unrelated members, equipment and loans are preserved.

Example feedback: `Deleted member A0123456X: Alex Tan.` The directory appears with a surviving selection,
or an empty state when no members remain. Stale details for the deleted member are removed.

| Situation | Result or correction |
| --- | --- |
| Member holds equipment | `Cannot delete NUS_ID because the member holds N item(s). Return the items first.` Use `member view` to identify them. |
| Member does not exist | `No member found with NUS ID NUS_ID.` Check `member list`. |
| Malformed/missing/repeated/unknown parameter | Correct the identity or syntax and retry. |
| Saving fails | No records change. Correct file access before retrying. |

Deletion is permanent: the MVP has no undo, restore or archive operation. Return all outstanding items
with `loans return` before retrying a blocked deletion. A failed deletion preserves the member and loans.
