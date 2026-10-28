---
  layout: default.md
  title: "User Guide"
  pageNav: 3
---

# Laplace User Guide

Week 10: [search, filters, selectable sorting, inventory planning and duplicate warnings](commands/search-and-planning.md).

Laplace helps a CCA EXCO member manage **members, equipment and equipment loans** on one computer.
Type commands to register records, find an exact member or item, issue equipment, record returns and
remove erroneous records. Lists and details show who holds each item and what each member holds.

This guide describes **seventeen member, equipment and loan commands**, including member and equipment
editing/restoration, batch equipment registration and search, with Tab autocomplete throughout:
seven member commands, seven equipment commands and three loan commands.
Use it with the matching release supplied by the team;
earlier builds may not support every behavior described here. Laplace is a local, single-user application.

<page-nav-print />

## Quick start

1. Install Java **25** for your operating system and processor architecture.
2. Download the matching JAR from the
   [team Releases page](https://github.com/AY2627S1-CS2103T-F15-2/tp/releases):
   **Laplace.jar** for Windows, Linux or Intel macOS; **Laplace-mac-arm64.jar** for Apple Silicon macOS.
   Each download is self-contained; you need only one JAR and Java 25.
3. Put the JAR in a folder you can write to. Keep the application and its data in this folder.
4. Open a terminal in that folder and run `java -jar Laplace.jar`. Substitute the actual filename
   if your supplied JAR has a different name. Keep the terminal open while using the application.
5. On the first launch without a saved data file, Laplace provides sample records. Existing saved data
   takes precedence; restarting does not add another set of samples.
6. Type `member list` in the command box and press **Enter**. Try `equipment list`, then `loans list`
   to open each directory and the current-loans tab.
7. Use `help` or **F1** to open Help, and `exit` to close the application normally.

![Laplace equipment groups in light mode](images/BetterUi.png)

Command text uses [IBM Plex Mono](https://github.com/IBM/plex), distributed under the SIL Open Font License.

Your rows, counts and selected record depend on your data. Both light and dark appearance are supported.
If startup fails, check the JAR filename and Java version and keep the terminal error for the team.
Do not delete your data file to troubleshoot startup.

### Completing commands and fields with Tab

Type part of a command in the command box and press **Tab** to complete it. For example,
`mem` becomes `member`, `member ad` becomes `member add`, and `member add na` becomes
`member add name/`. Member editing is supported too: `member ed` becomes `member edit`, with
`nus/`, `name/`, `phone/` and `email/` field and value suggestions. Member names always come from the
member directory, including during edits. Press Space yourself to begin the next word or field.

When several suggestions match, press Tab repeatedly to cycle through them in alphabetical order.
During that cycle, **Shift+Tab** selects the previous suggestion. Editing the input, moving the caret,
submitting a command, leaving the input, or changing the records starts a fresh cycle.

Laplace also completes existing field values: member names, phone numbers and emails; member IDs in
reference fields; equipment UUIDs, names and categories; and the four equipment conditions. For example,
`equipment add condition/g` completes to `equipment add condition/GOOD`. Values match without regard
to case and retain their stored spelling. Suggestions use the current records, including records added
since startup. Identical values appear once.

A matching stored value takes priority over an unfinished field prefix within that value. For example,
with a stored equipment name `Camera Cover`, `equipment add name/Camera co` completes to
`equipment add name/Camera Cover`. If no stored value matches, the final token can still complete
to a new field prefix. A trailing space offers the unused fields for the command.

Value matching treats repeated spaces and Unicode whitespace within a value as ordinary single spaces.
For example, `member edit name/Alex  T` can complete the stored name `Alex Tan`. The replacement keeps
the stored spelling, and all text and separators outside that value remain unchanged.

Command words and field prefixes are case-sensitive. Field suggestions depend on the command and omit
non-repeatable prefixes already present. All seventeen member, equipment and loan commands plus `help`,
`exit`, `mode dark` and `mode light` have command completion. Search supports `q/`, the applicable filters, sorting and ordering;
finite options such as `status/de` complete to `status/DELETED`. Query text can reuse searchable stored
values. Equipment editing and both restoration commands support their fields and existing references.
Batch equipment creation includes `count/`; batch loan UUID completion fills the active comma-separated
entry, preserves the others and excludes UUIDs already listed. Return conditions are supported too.
New member IDs, dates, numeric counts and free-form notes remain manual. Completion does not guarantee that a command
can execute: ordinary validation still runs when you press Enter.

Tab changes only the active token or field value and preserves later fields and their separators,
including Unicode spaces copied into the input. It never executes a command or saves records. Press **Enter** to submit the completed input. With blank input, selected text, an
unsupported context, at the start of an existing field marker, or with no matching suggestion,
normal Tab navigation remains available. An untouched following marker and its value are preserved. Shift+Tab also
keeps its normal navigation behavior outside an active completion cycle.

### Sample records on first launch

When there is no saved data file, Laplace starts with five fictional members and seven equipment items.
The samples include two cameras, a damaged tripod, a microphone, a speaker, a projector under repair
and an event laptop. Three open loans illustrate current holders, one overdue item and a member holding
multiple items. One closed loan appears in the Loans tab history.

Dates are relative to the day of first launch. Try `member list`, `equipment list` and `loans list`,
then copy displayed identities to view details or try a return. These are ordinary records:
additions, returns and deletions are saved normally. Existing saved data always takes precedence;
samples are not appended to it. To explore a fresh sample set, run the JAR from a separate empty folder
instead of removing your existing data file.

The equipment identities below are stable in a fresh sample set. The states and holders change as you
issue, return or delete records.

| Sample equipment | UUID | Initial availability | Current holder |
| --- | --- | --- | --- |
| Camera, case included | `550e8400-e29b-41d4-a716-446655440000` | AVAILABLE | None |
| Camera, strap included | `550e8400-e29b-41d4-a716-446655440001` | ASSIGNED | Alex Tan (`A0123456X`) |
| Tripod, damaged | `550e8400-e29b-41d4-a716-446655440002` | UNAVAILABLE | None |
| Wireless Microphone | `550e8400-e29b-41d4-a716-446655440003` | ASSIGNED | Jamie Lim (`A0234567R`), overdue |
| Portable Speaker | `550e8400-e29b-41d4-a716-446655440004` | AVAILABLE | None; previously returned |
| Projector, under repair | `550e8400-e29b-41d4-a716-446655440005` | UNAVAILABLE | None |
| Event Laptop | `550e8400-e29b-41d4-a716-446655440006` | ASSIGNED | Alex Tan (`A0123456X`) |

To explore without changing the sample records:

1. Run `loans list`. The Loans tab shows three open loans and the closed speaker loan in history.
2. Run `member view nus/A0123456X`. Alex's details show the camera and laptop under Held Equipment.
3. Run `equipment view uuid/550e8400-e29b-41d4-a716-446655440003`. The microphone's details identify
   Jamie as the holder and show its assignment and expected return dates.
4. Run `loans list` again to return to the Loans tab.

## Using the window

- **Members**, **Equipment** and **Loans** switch views. Member/equipment search criteria remain active
  when switching tabs or opening details. **Back to results** returns to the current filtered summary;
  **Reset search**, `member list` or `equipment list` clears that directory's criteria and sorting.
- The left equipment directory groups units with the same normalized category and name, including units
  registered in different batches. Open a group with a click, **Enter** or **Space**, then select a row in
  its physical-unit table. Each unit still has its own UUID, condition, notes, holder and lifecycle state.
- Group counts distinguish **matching** units from **retained** units. Availability counts exclude deleted
  units. **Show all units** reveals group members outside the active search, including deleted records.
  The right-hand **All retained groups** summary includes deleted-only groups regardless of the active
  filter. Such a group shows zero active availability counts and its retained deleted count.
  Direct `equipment view uuid/UUID` opens that exact unit, enabling Show all units if needed.
  Unchecking it narrows the table again while retaining the selected unit's details; a message explains
  when that unit is outside the table filter.
  Short table identifiers are display labels only: hover for the full UUID, or copy it from unit details.
- Member rows open member details. Copy buttons preserve full identifiers and contact fields. Related
  member/equipment links open the exact record. **Commands for this unit/member** contains command previews:
  **Copy** copies plain text; **Use command** prepares the input and never executes it. Replace the marked
  uppercase placeholders and press Enter yourself. Availability and validation rules still apply.
- The **Loans** tab shows current open assignments, exact-identity links, assigned/due dates and an
  **Overdue** or **Due today** label when applicable. Rows reflow at narrower widths. **Prepare return command**
  fills the input with the unit UUID and today's local date; review it before pressing Enter.
  Urgency updates when you open Loans, execute a command or change records. Reopen Loans after midnight
  to refresh the labels. Completed loans show return dates and recorded issue/return conditions, without
  urgency labels or return actions. Filter both sections by NUS ID and equipment UUID. `loans list` opens
  this same tab; member and equipment details also show the selected record's loan history.
  Sections with more than ten loans use their own scrollable list. Scroll inside that list, or focus
  it and use the arrow, Page Up/Down, Home and End keys, to reach all entries without loading every row at once.
- **Last result** sits above the command feedback and stays after switching tabs. A green box is a
  successful command, a red box is an error, and the box is neutral before the first command.
  The heading displays only **Last result**; assistive text includes the status and full message.
  **Details** expands the complete feedback and **Copy** includes all UUIDs from batch creation.
- The command input, identifiers and command examples use IBM Plex Mono. Unmodified **↑/↓** browse up to
  100 recent submissions in this session, preserving an unfinished draft. Modified arrows retain normal
  text selection and caret navigation. **Ctrl+Space** offers command-name or parameter-prefix completion at
  the end of the input. Choose an entry to insert it; **Escape** dismisses suggestions. Tab completes the
  active token or value as described above; with no applicable suggestion it retains normal focus navigation. History is cleared when the application closes.
- **F1** or `help` opens a scrollable command reference with copyable templates and the user-guide link.
  The loan-return template includes `condition/GOOD` as an example of the optional observed return
  condition. Ctrl+Space after `loans return co` also offers `condition/`; omit it to retain the existing
  equipment condition, or supply one of the conditions documented in the loan-return section.
- Drag the directory divider to resize its panels. Scroll long lists/details vertically; the data filename
  appears in the footer, with the full path available on hover.

### Appearance: `mode dark` and `mode light`

```text
mode dark
mode light
```

Use these commands or the header button labeled **Dark mode** or **Light mode**. The button names the appearance it will apply. **Menu** contains Help and Exit. The main window and Help switch together.
Light is the default for new and older preference files. The chosen mode is saved with window preferences
on normal exit and restored on the next launch. Changing mode preserves the current search and records.
Only `dark` and `light` are accepted; missing/extra arguments are errors. These are appearance utilities,
in addition to the seventeen member, equipment and loan commands.

## Command format and feedback

Commands and prefixes are **lowercase and case-sensitive**. Use `member add`, not `Member Add`.

- Replace uppercase placeholders with your values: replace `NUS_ID` with `A0123456X`, for example.
- Square brackets mean an optional parameter; do not type them. Optional parameters are marked in each command's syntax.
- Put spaces between command words and parameters. Parameters may appear in any order; tabs also separate fields.
- Each prefix may appear **at most once**, including `notes/`.
- Unicode whitespace, including EM SPACE and nonbreaking space, separates command words and parameters.
  Leading/trailing whitespace is ignored. Runs of whitespace in text values become one ordinary space.
- A value continues to the next parameter marker. A start-of-value or whitespace-delimited token such as
  `notes/` or `unknown/` is reserved as a marker. Unknown markers are errors, even inside free text.
  There is no quoting or escaping. An ordinary URL such as `https://example.com` is not a marker.
- Use the full field names: `nus/`, `name/`, `phone/` and `email/`. Short alternatives are not accepted.
- `member list`, `equipment list` and `loans list` take no arguments. Additional text or prefixes are rejected.
- An empty or whitespace-only submission does nothing.

On success, the input clears and the requested list/details view appears. Newly added records are selected
and open in details. On failure, the command stays in the input box, focus returns there, and the current
view and selection remain unchanged. Correct it and press Enter again. Only the first detected error is
shown; another error may appear after correction. Rejected commands do not change records.

In feedback examples, `N` is the current count and uppercase placeholders such as `UUID`, `NUS_ID`
and `EQUIPMENT_NAME` stand for the actual record values. `TODAY` is the computer's local date.

### Unsupported AddressBook commands

The old top-level commands `add`, `edit`, `delete`, `clear`, `find`, and `list` are unsupported.
They return `Unknown command.` and do not change records or save the data file, even when arguments
are supplied. For example, use `member list` instead of `list members`, `equipment list` to view
inventory, and `member search q/Alex` to search members. There is no supported bulk-clear command.
Use the member, equipment and loan commands documented below, or `undo`, `redo`, `mode`, `help` and `exit`.

### Shared field rules

| Field | Accepted input and stored form |
| --- | --- |
| NUS ID `nus/` | One ASCII letter, exactly seven digits, and an optional final ASCII letter. Case-insensitive; stored/displayed uppercase, such as `A0123456X` or `E0123456`. |
| Member name `name/` | 1–100 Unicode code points after whitespace and NFC normalization. Unicode letters with combining marks, spaces, straight (`'`) or curly (`’`) apostrophes, hyphens and periods; must contain at least one letter. Combining marks must follow a letter or its marks. Examples: `அருண்`, `Anne O’Connor`. |
| Phone `phone/` | Optional leading `+`, then 8–15 digits. Spaces and hyphens are removed: `+65 9123-4567` becomes `+6591234567`. |
| Email `email/` | 3–254 ASCII characters, one `@`, no spaces and at least one domain dot. Local-part characters: letters, digits, `.`, `_`, `%`, `+`, `-`. Domain labels contain letters/digits/hyphens and cannot begin/end with a hyphen. ASCII validation occurs before conversion to lowercase; non-ASCII lookalikes such as `K` are rejected. Stored lowercase. |
| Equipment UUID `uuid/` | Full `8-4-4-4-12` hexadecimal form, such as `550e8400-e29b-41d4-a716-446655440000`. Case-insensitive input; lowercase display. Shortened UUIDs are rejected. |
| Equipment name `name/` | 1–100 printable Unicode characters after whitespace normalization. Punctuation and model numbers are allowed; control characters are rejected. |
| Category `category/` | 1–50 printable Unicode characters after normalization. Free text; control characters are rejected. |
| Condition `condition/` | Exactly `GOOD`, `FAIR`, `DAMAGED` or `UNDER_REPAIR`, ignoring case. Stored uppercase. Use the underscore in `UNDER_REPAIR`. |
| Notes `notes/` | Optional, at most 300 printable Unicode characters after trimming. Line breaks become spaces. Omitted or empty notes mean no notes. |
| Dates | Exactly `yyyy-MM-dd` with a Gregorian year from `0001` through `9999`, such as `2026-10-15`. Assignment/actual-return dates cannot be later than the computer's local date. Expected return may be in the future. |

Saved loan dates must also use the four-digit Gregorian range `0001`–`9999`. Year zero, negative years,
and five-digit years are invalid stored data. Loading does not compare historical dates with today's clock.

Copy the actual UUID displayed by Laplace when issuing, viewing, returning or deleting equipment.
The UUID in this guide is an example; it does not identify every newly added item.

Newly added or edited member names use Unicode NFC normalization: `José Tan` and `José Tan`
(with a separate combining acute accent) are stored as the same canonical spelling. Existing saved
names retain their exact spelling when loaded; editing another field does not rewrite the name.

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
member add nus/A0678901D name/Jordan Lee phone/93456789 email/jordan.lee@u.nus.edu
member add email/siti@u.nus.edu name/Siti Nur phone/+65 8123-4567 nus/E0123456
```

Success adds the member, selects the new row and opens details showing the normalized values.
For the first example, the result is `New member added: Jordan Lee (A0678901D)`.
This example uses an ID absent from the bundled samples. If you have already added it, use another unused NUS ID.

Two members are duplicates **only when their normalized NUS IDs are equal**. Different IDs can share
a name, phone or email. A duplicate is rejected with a message such as:
`A member with NUS ID A0123456X already exists.`

Missing, blank, malformed, repeated or unknown parameters are rejected. Failure creates no member and
preserves the current view/selection. If saving fails, correct file access and retry the retained command.

### List members: `member list`

```text
member list
```

Show active and deleted members together, ordered by normalized name and then NUS ID. Each row shows
name, NUS ID, phone, email and **Items Held** count. Deleted cards are grey, with an italic,
struck-through name and a separate `(deleted)` suffix. They remain selectable. The summary shows
active, deleted and displayed counts. Archival is not supported.

The result is `Listed N active members.` Deleted records are counted separately when present.
If there are no records, the list displays:
`No members yet`

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

Example feedback: `Viewing member: Alex Tan (A0123456X). Currently holds N item(s).`
With no current loans, the holdings section says `This member currently holds no equipment.`

| Situation | Result or correction |
| --- | --- |
| ID exists | Open that exact member's details; equal names do not affect lookup. |
| ID is well formed but does not exist | `No member with NUS ID NUS_ID exists.` Copy an existing ID from `member list`. |
| ID is incomplete or malformed | Correct it using the shared NUS ID rule. A well-formed shorter ID is still an exact identity, not a partial-name or partial-ID search. |
| Required parameter is missing, repeated or unknown | Correct the command to `member view nus/NUS_ID`. |
| A current loan refers to missing equipment | Report inconsistent stored loan data; do not show an incomplete holdings list. |

Failed viewing preserves the previous details and selection. This command covers both exact-ID lookup
and viewing a member's equipment; no separate member-find or member-loans command is needed.

### Edit a member: `member edit`

```text
member edit nus/NUS_ID [name/NAME] [phone/PHONE] [email/EMAIL]
```

Example: `member edit nus/a0123456x name/Anne-Marie Lim phone/+65 9876-5432`.

Supply the exact NUS ID and at least one detail to change. Names, phones and emails follow the
same validation and normalization as `member add`. Omitted fields keep their stored values.
The NUS ID cannot be changed; open and completed loans remain attached to the same member.
You can edit contact details while the member holds equipment. Matching another member's name
or contacts does not merge records because NUS IDs identify members.

Success reports `Edited member NUS_ID: NAME.` and opens the updated details. The directory
re-evaluates its current filters and sort order. Name then NUS ID is the default ordering; an explicit
sort such as `sort/id order/desc` remains active. The edited member stays selected when included in
the filtered results; their details still open if the edit moves them outside those results.
Changes survive restart.

| Situation | Result or correction |
| --- | --- |
| No editable fields supplied | Supply at least one of `name/`, `phone/` or `email/`. |
| Supplied values make no change | `The supplied details do not change this member.` No save occurs. |
| Member is deleted | Run `member restore nus/NUS_ID` before editing. |
| Member does not exist | `No member with NUS ID NUS_ID exists.` Check `member list`. |
| Blank, invalid, repeated or unknown field | Correct the field using the `member add` rules. |
| Saving fails | The previous details, loan history and saved file remain unchanged. |

### Delete a member: `member delete`

```text
member delete nus/NUS_ID
```

Example: `member delete nus/A0123456X`.

Delete one exact member **only if they currently hold no equipment**. Equal-name members with other
IDs remain. Success marks the member deleted, preserving their details, reserved NUS ID and all loan
history. They remain visible and selected in the directory with a grey card, italic struck-through
name and `(deleted)` suffix. A deleted member cannot receive new loans; restore them first.

Example feedback: `Deleted member A0123456X: Alex Tan.` The directory keeps the same member selected.
Viewing deleted details shows restore guidance. Repeating deletion is rejected without saving.

| Situation | Result or correction |
| --- | --- |
| Member holds equipment | `Cannot delete NUS_ID because the member holds N item(s). Return the items first.` Use `member view` to identify them. |
| Member is already deleted | Restore them first; repeated deletion is rejected without saving. |
| Member does not exist | `No member with NUS ID NUS_ID exists.` Check `member list`. |
| Malformed/missing/repeated/unknown parameter | Correct the identity or syntax and retry. |
| Saving fails | No records change. Correct file access before retrying. |

Deletion has no expiry and never clears a member record automatically. Return all outstanding items
with `loans return` before retrying blocked deletion. A failed save changes neither records nor saved bytes.

### Restore a member: `member restore`

```text
member restore nus/NUS_ID
```

Example: `member restore nus/a0123456x`.

Restore any deleted member at any time. There is no time limit, timestamp check, purge or separate
archive state. The same NUS ID, details and loan history are retained. Success reports
`Restored member NUS_ID: NAME.`, opens their details and removes the deleted styling. New loans
can then be assigned normally. An unknown identity, an already-active member or invalid arguments
are rejected without saving. A failed save leaves the member deleted and permits a later retry.

### Add equipment: `equipment add`

Register one or more physical units with the same details. Laplace generates a separate immutable UUID for each unit.

```text
equipment add name/NAME category/CATEGORY condition/CONDITION [notes/NOTES] [count/COUNT]
```

`name/`, `category/` and `condition/` are required. Notes are optional; `notes/` with no value also means
no notes. `count/` is optional and defaults to **1**. Supply a whole number from **1 to 100**, using
ASCII digits only; leading zeros are allowed. Blank counts, signs, fractions, repeated `count/` fields
and values outside this range are rejected. Do not supply `uuid/`, a holder or availability.
Follow the shared text limits and condition rules.

Examples:

```text
equipment add name/Canon EOS R50 category/Camera condition/GOOD notes/Includes battery and strap
equipment add name/Tripod category/Camera Support condition/fair
equipment add name/Camera Kit category/Photography condition/GOOD count/3 notes/Includes strap
```

For a count of 1, success creates one item, selects its row and opens its details. Feedback includes the generated UUID
and name, for example:
`New equipment added: Canon EOS R50 (550e8400-e29b-41d4-a716-446655440000), availability: AVAILABLE`.
Copy the **actual generated UUID**, which will normally differ from that example, for later commands.

For larger counts, success creates the entire batch, resets equipment search filters/sorting, and opens
the full equipment directory. Feedback reports the created count, name, availability and all generated
UUIDs, for example `Added 3 equipment units: Camera Kit, availability: AVAILABLE.` followed by `UUIDs:`.
Every unit has the same supplied details and its own UUID. Existing similar records may produce one
advisory duplicate warning; the new batch does not warn about its own intentionally matching units.

GOOD/FAIR items start as AVAILABLE. DAMAGED/UNDER_REPAIR items can be registered but start UNAVAILABLE.
Otherwise identical items are allowed: two cameras with the same description are separate physical units
with different UUIDs. Laplace silently retries a generated UUID collision; if it cannot obtain a unique
identifier, it reports an error without adding any units. The batch is saved as one operation: a failure
during creation or saving commits no new units. There is no stored quantity replacing individual records.

### List equipment: `equipment list`

```text
equipment list
```

Show all retained physical units, including deleted units, grouped by normalized **category and name**.
Groups follow category/name order; select a group to inspect its distinct units. The table shows condition,
availability, holder and lifecycle status, with the full UUID in a tooltip and the selected unit's details.
Matching descriptions share a directory row but retain separate identities. Default unit order is UUID;
a search's sorting applies to its matching units. Clicking a table heading temporarily sorts the table.

Feedback is `Listed N equipment items.` An empty list displays:
`No equipment yet. Use equipment add to get started.`

No parameters are accepted. Use `equipment view uuid/UUID` to look up one item. Syntax errors preserve
the current view. Inconsistent loan references produce an error rather than a partial or misleading
availability list.

### View equipment and its holder: `equipment view`

```text
equipment view uuid/UUID
```

Example: `equipment view uuid/550e8400-e29b-41d4-a716-446655440000`.
Uppercase hexadecimal letters identify the same UUID. You must use the complete UUID; shortened forms
and partial matching are not supported.

Details show the full UUID, name, category, condition, notes, lifecycle status and derived availability. An open loan also
shows the holder's NUS ID, name, phone and email, plus assignment and expected return dates. The matching
group and physical-unit rows are selected and scrolled into view when included in the current filters.
If the unit is outside those filters, its group opens with **Show all units** enabled. Current search
filters and any clicked table sort remain unchanged. Long notes wrap and scroll with the details.

| Item state | Example feedback |
| --- | --- |
| Has an open loan | `Viewing equipment: EQUIPMENT_NAME (UUID). Availability: ASSIGNED.` |
| Has no open loan and condition is GOOD/FAIR | `Viewing equipment: EQUIPMENT_NAME (UUID). Availability: AVAILABLE.` |
| Has no open loan and condition is DAMAGED/UNDER_REPAIR | `Viewing equipment: EQUIPMENT_NAME (UUID). Availability: UNAVAILABLE.` |

With no open loan, there is no holder and no current loan dates; old holder details are cleared after
return. A missing UUID produces `No equipment with UUID UUID exists.` Malformed UUIDs show the shared
validation error. Missing/repeated/unknown parameters are rejected. An open loan referring to a missing
member produces an inconsistent-data error instead of incomplete holder information. Failure leaves the
previous view and selection unchanged.

### Edit equipment: `equipment edit`

```text
equipment edit uuid/UUID [name/NAME] [category/CATEGORY] [condition/CONDITION] [notes/NOTES]
```

Supply the exact UUID and at least one field to change. Unspecified fields keep their existing values;
`notes/` with no value clears notes. Names, categories, conditions and notes follow the same validation
as equipment add. Duplicate/unknown parameters and unchanged edits are rejected without saving.

Example: `equipment edit uuid/550e8400-e29b-41d4-a716-446655440000 condition/DAMAGED notes/Lens needs repair`.

Only that physical unit changes. Its UUID and loan references remain intact; matching siblings are
unaffected. Changing the name/category changes its inventory group. You may edit a loaned unit,
including its condition: it remains ASSIGNED until returned, after which its new condition determines
availability. This does not record a separate condition-at-handover history. Deleted units must first
be restored. Success opens the edited unit's details; failed validation or saving changes nothing.

### Delete equipment: `equipment delete`

```text
equipment delete uuid/UUID
```

Example: `equipment delete uuid/550e8400-e29b-41d4-a716-446655440000`.

Soft-delete one exact item **only when it has no open loan**. Its condition does not prevent deletion.
The unit, UUID, details and closed loans are retained. Feedback includes the identity and name:
`Deleted equipment UUID: EQUIPMENT_NAME.` The equipment directory appears with its existing filters.
Deleted units remain visible in all-record lists, labeled `[DELETED]`; active-only searches exclude them.
They cannot be assigned and are excluded from active availability totals. Group retained counts include them.

| Situation | Result or correction |
| --- | --- |
| Item is on loan | `Cannot delete UUID because it is assigned to NUS_ID. Return the item first.` View it to confirm the holder. |
| UUID does not exist | `No equipment with UUID UUID exists.` Copy an existing UUID from `equipment list`. |
| UUID or syntax is invalid | Correct the full UUID/required prefix and retry. |
| Saving fails | The item and loan records remain unchanged. Correct file access and retry. |

Deleting one UUID never deletes other items with the same description. Repeated deletion is rejected.

### Restore equipment: `equipment restore`

```text
equipment restore uuid/UUID
```

Find retained units with `equipment search status/deleted` or `equipment list`, then restore one exact
UUID. There is no restoration time limit. The unit retains its identity, condition, notes and closed loan
history. A restored DAMAGED or UNDER_REPAIR unit remains UNAVAILABLE; GOOD/FAIR units become AVAILABLE.
Success opens its details. Unknown UUIDs, already-active units, invalid syntax and save failures do not
change records. Records permanently removed by older versions require an earlier backup to recover.

Equipment search defaults to active units. Use `status/active`, `status/deleted` or `status/all` alongside
other search filters; values are case-insensitive. Deleted units have derived UNAVAILABLE status, so
`equipment search status/deleted availability/available` returns no matches. `equipment list` resets
filters and includes both states. Total counts include retained units; availability counts and inventory
availability summaries cover active units only; retained group counts include deleted units. Deleted equipment can still be opened with `equipment view uuid/UUID`.

### Open the Loans tab: `loans list`

```text
loans list
```

Open the existing Loans tab, just as clicking **Loans** does. It shows current and completed loans,
with links to the corresponding member and equipment details. Use the NUS ID and UUID fields to
filter both sections. Each section has its own count and empty state.

Feedback is `Showing current loans.` This command does not create, return, delete or change any
member, equipment or loan records. It does not save the data file.
Extra text or parameters, such as `loans list uuid/UUID`, are rejected and leave the current view unchanged.

### Assign equipment: `loans add`

Assign one or more AVAILABLE physical items to an existing member.

```text
loans add uuid/UUID[,UUID...] nus/NUS_ID assigned/ASSIGNMENT_DATE expected/EXPECTED_RETURN_DATE
```

All four parameters are required. Use the actual UUID from `equipment list` and the NUS ID from
`member list`. Both identities must exist. Assignment must be on or before the computer's local date;
expected return must be on or after assignment. Expected return may be in the future.

Example, assuming these identities exist and the assignment date is not in the future:

```text
loans add uuid/550e8400-e29b-41d4-a716-446655440000 nus/A0123456X assigned/2026-09-14 expected/2026-09-21
```

Success creates an open loan and immediately changes availability to ASSIGNED. Equipment details show
the holder/dates; the member's current holdings and item count update. Feedback is:
`Assigned EQUIPMENT_NAME (UUID) to MEMBER_NAME (NUS_ID) on ASSIGNMENT_DATE; expected return: EXPECTED_RETURN_DATE.`

A member may hold several different items. Separate multiple UUIDs with commas; every item shares the
member and dates, and the request succeeds or fails as a whole. The condition of each item at issue is
recorded in its loan history. Assigning an already assigned item is rejected, even if the new loan names
the same member. Reaching the expected return date does not automatically return an item.

A new assignment must be on or after that unit's latest recorded actual return date. Same-day handoffs
are allowed. A conflicting UUID rejects the entire batch; expected-return dates do not determine
historical availability. This check applies to new assignments and does not rewrite retained history.

| Situation | Result or correction |
| --- | --- |
| Equipment/member does not exist | `No equipment with UUID UUID exists.` or `No member with NUS ID NUS_ID exists.` Check the corresponding list. |
| Equipment or member is deleted | Restore that record before assigning it; an invalid unit rejects the entire batch. |
| Item already has an open loan | `Equipment UUID is unavailable because it is assigned to CURRENT_NUS_ID.` Return it before assigning it again. |
| Condition is DAMAGED/UNDER_REPAIR | `Equipment UUID is unavailable (condition: CONDITION).` Choose an AVAILABLE item. |
| Expected date precedes assignment | `Expected return date cannot be before assigned date` |
| Assignment precedes a retained actual return | `Equipment UUID cannot be assigned before its last recorded return date DATE.` Use that date or later. |
| Assignment is in the future | `Assigned date cannot be after today (TODAY).` Check your entry and the computer's date. |
| Field syntax/date is invalid | Correct the first reported parameter and retry; use a real `yyyy-MM-dd` date. |
| Saving fails | No loan is recorded and no records change; correct file access before retrying. |

On failure, no loan is created. Availability, holder, holdings, view and selection stay unchanged.

### Return equipment: `loans return`

```text
loans return uuid/UUID[,UUID...] returned/ACTUAL_RETURN_DATE [condition/CONDITION]
```

UUID and date are required. Equipment must exist and have open loans. Referenced members
must also exist. The return date must be **from the assignment date through the computer's local date**,
inclusive. Same-day, early and late returns are valid; the expected return date does not limit returns.

Example, for the assignment above and a computer date on or after 20 September 2026:

```text
loans return uuid/550e8400-e29b-41d4-a716-446655440000 returned/2026-09-20
```

Success records the actual return date and closes that item's open loan. Equipment details clear its
holder/loan dates, the item leaves the member's current holdings, and open-loan counts refresh.
Availability becomes AVAILABLE for GOOD/FAIR, or UNAVAILABLE for DAMAGED/UNDER_REPAIR.
Feedback is `Returned EQUIPMENT_NAME (UUID) from MEMBER_NAME (NUS_ID) on ACTUAL_RETURN_DATE.`

Multiple UUIDs return in one request. If `condition/` is supplied, it is recorded for every item
and updates each item's current condition. Use separate commands for different observed conditions.
If omitted, the current equipment condition stays unchanged and history shows the return condition
as "Not recorded". A failed item prevents the entire batch from being returned.
For a batch, feedback lists the number of units and their UUIDs without naming one borrower for all of them.

| Situation | Result or correction |
| --- | --- |
| Equipment does not exist | `No equipment with UUID UUID exists.` Check the full UUID. |
| No open loan, including an already returned item | `Equipment UUID has no open loan to return.` A repeated return is rejected. |
| Return is before assignment | `Returned date cannot be before assigned date` |
| Return is in the future | `Returned date cannot be after today (TODAY).` |
| Missing/repeated/unknown field or invalid date | Correct the reported parameter and use the full command syntax. |
| Open loan refers to a missing member | `Cannot return equipment UUID: its open loan refers to a missing member.` Preserve the data and report the error to the team. |
| Saving fails | No return is recorded and no records change; correct file access and retry. |

A rejected return leaves the loan, return date, holder and availability unchanged. Completed loans
appear in the Loans tab and in member and equipment details.

### Undo a record change: `undo`

Both `undo` and `redo` appear in Help. Type `und` or `red` and use Tab or Ctrl+Space to complete
the command name. Help's Use command action only prepares it; press Enter to execute.

```text
undo
```

Undo the most recent successful record-changing command. Laplace retains **up to ten changes in the
current session**. An eleventh successful change discards the oldest undo step. Each command counts as
one step: a batch equipment creation, loan assignment or return is undone together.

Supported changes are member/equipment addition, editing, deletion and restoration, plus loan assignment
and return. Undo restores linked records, original UUIDs, loan dates and conditions together. For example,
undoing a loan return reopens the loan and restores its previous holder and equipment condition.
Undoing equipment creation removes those units; redoing it restores their original UUIDs.

Success reports the action, for example `Undid: equipment add.`. If none is available, the error is
`Nothing to undo.`. Undo takes no arguments: `undo 2` is rejected. The current screen refreshes while
preserving search criteria; if the displayed record no longer exists, its directory appears instead.
Active filters can hide restored records; use the directory's list command to reset the search.

### Redo an undone change: `redo`

```text
redo
```

Restore the most recently undone change. Up to ten redo steps are available after ten undos. Success
reports, for example, `Redid: equipment add.`. If none is available, the error is `Nothing to redo.`.
Redo takes no arguments. It restores the recorded state without generating new UUIDs or dates.

A **new successful record-changing command after undo clears redo**. Listing, searching, viewing, Help,
appearance changes, rejected commands and no-change edits neither add history nor clear redo. Window
preferences, appearance and search criteria are not undone. The command input's arrow-key history is
separate from record undo/redo.

Every successful undo/redo is saved automatically. A save failure leaves records and undo/redo history
unchanged; correct the file-access problem and retry. History is kept **only until Laplace closes**.
Restarting loads the most recently saved records with no undo/redo steps available. The ten-step limit
also means this feature does not replace a backup.

Example:

```text
equipment add name/Camera category/Photo condition/GOOD count/3
undo
redo
```

This creates three units, removes all three, then restores the same three units. Type these commands in
the existing command input; there are no dedicated undo/redo buttons or keyboard shortcuts.

## A complete issue-and-return workflow

Use a new member ID if the example ID already exists. The dates below are examples for a computer date
on or after 20 September 2026; choose valid actual dates for your own operation.

1. Register the member:
   `member add nus/E0678901C name/Mei Tan phone/91234567 email/mei@example.com`.
2. Register an item:
   `equipment add name/Camera Kit category/Photography condition/GOOD notes/Includes strap`.
3. Copy the UUID generated in step 2. **Replace `UUID` in the remaining steps with that value.**
4. Assign it:
   `loans add uuid/UUID nus/E0678901C assigned/2026-09-14 expected/2026-09-21`.
5. Run `member view nus/E0678901C` and `equipment view uuid/UUID`. Check the holdings, holder and dates.
6. Return it: `loans return uuid/UUID returned/2026-09-20`.
7. View both records again. The member has no current holding for this item; the GOOD item is AVAILABLE.
8. If these were erroneous/test records, remove them with `equipment delete uuid/UUID` and
   `member delete nus/E0678901C`. Both deletions retain records; use `equipment restore` or `member restore` to recover them.

Unicode name search and value completion treat `Σ`, `σ` and `ς` as case-equivalent.
For example, searching for `Σ` or `νικοσ` finds a stored member named `ΝΙΚΟΣ`;
Tab completion of `member edit name/νικοσ` suggests the original spelling `ΝΙΚΟΣ`.

## Saving, file access and recovery

Successful changes save automatically; there is no manual-save command. For the packaged application,
data is stored in `data/addressbook.json` inside the folder containing `Laplace.jar`, even if you launch
the JAR from another terminal folder. `preferences.json` and rotating `addressbook.log.*` files also
stay beside the JAR. The window's status area identifies the data-file path.

Command diagnostics record that input was received and, at detailed log levels, the recognized command
type. They omit raw input and contact values, including rejected input. Existing logs from older
versions may still contain commands recorded before this change.

List, search and view commands, `help`, and `exit` do not save member, equipment or loan records.
They remain usable when the loaded data cannot be saved, and do not rewrite the data file or create
one on first launch. Normal exit still saves window and appearance preferences separately.

If you previously launched from another folder, close Laplace and back up the old data and preferences
before copying them into the matching locations beside the JAR. Data is not automatically read or moved
from the old working folder. Moving only the JAR to a new empty folder starts a separate sample dataset.
When running development classes with Gradle, runtime files remain relative to the project working folder.

For Laplace mutation commands, a failed save leaves the live records and previously saved data
unchanged. An error is not a successful addition, deletion, assignment or return. The command remains
available for correction. Fix the folder/file access problem, then retry. Do not repeatedly resubmit a
successful command: duplicate members, already-assigned items and repeated returns are rejected.

Data includes members, equipment and open/closed loan records. Availability and holders are derived;
do not try to maintain separate holder/availability fields. Member deletion retains the record,
its `deleted` boolean and closed loans for unlimited restoration. Equipment follows the same policy.
Older member/equipment records without a `deleted` field load as active;
explicit null or non-boolean values are invalid. Do not open lifecycle-aware data in an older release
that may discard deletion state.

Back up the data file while Laplace is closed. Do not edit it while the app is running or run multiple
instances against the same file. Manual data editing is not an MVP operation. If startup or a view
reports invalid/inconsistent data, preserve the original file and report the message; do not overwrite
it with an empty file or assume that an incomplete view is safe to use.

If an existing data file cannot be loaded, Laplace shows recovery guidance and its path, then stops
startup after you dismiss the error. No editable directory or command box opens. Invalid files include
a JSON `null` root and any non-whitespace content after the root object. The data file remains unchanged.
While Laplace is closed, back it up and repair it before restarting. Moving the file to a safe location
allows a separate sample dataset to start; it does not recover the original records.

## FAQ

**Why are two identical equipment items shown?** Each physical unit has its own UUID. Equal names,
categories, conditions and notes are allowed. Use the UUID to choose the unit you mean.

**How do I find a member or current holder?** Use `member view nus/NUS_ID` for exact member details and
holdings, or `equipment view uuid/UUID` for an exact item and its current holder. Partial/fuzzy searches
and separate member-find/holder commands are outside the MVP.

**Why can I not delete a record?** A member with an open loan or equipment currently on loan cannot be
deleted. Return the relevant items first. Closed loans do not block deletion. They are retained for
both soft-deleted members and equipment.

**Can I register broken equipment?** Yes. DAMAGED/UNDER_REPAIR records are UNAVAILABLE and cannot be
assigned. Use `equipment edit uuid/UUID condition/GOOD` after repairing a unit. Member contact details can be changed with `member edit`.

**Does an overdue loan close itself?** No. Record an actual return using `loans return`. A return later
than expected is valid if it is no earlier than assignment and no later than the computer's local date.

**Why do I not see the sample records?** Laplace loads your existing saved file instead of adding samples
to it, even if that file contains no records. To try the bundled examples, launch the JAR from a separate
empty folder. Keep your existing data file and backup intact.

**How do I move my data?** Close Laplace, back up the data file, copy it to the other computer's chosen
Laplace folder at the configured data path, and launch the matching release there. Keep the original
backup until you have checked members, equipment and current loans on the other computer.

**Where is Help?** Enter `help` or press F1. Use `exit` for normal shutdown; saved window preferences are
reused within usable screen bounds. These utilities are separate from the seventeen member, equipment
and loan commands, including `loans list`.

The **Menu** contains **Help** and **Exit**. Help displays the address of the
[Laplace User Guide](https://ay2627s1-cs2103t-f15-2.github.io/tp/UserGuide.html);
use **Copy URL** to copy its address and open it in your browser.

## MVP limits

The MVP supports member and equipment editing, soft deletion and restoration, plus search with filters
and user-selected sorting. Batch equipment registration supports 1–100 physical units per
`equipment add` command using `count/COUNT`; each unit receives its own UUID. Loan assignment and return
also accept multiple equipment UUIDs in one command. Bulk editing, deletion and restoration are not supported.

The MVP excludes separate find/member-loans/holder commands, import/export, fuzzy search
and automatic overdue notifications. Copying a backup file while the app
is closed is a file-management task, not an import/export command. Data is local to one operator;
authentication and concurrent multi-user operation are not provided.

## Command summary

| Task | Format |
| --- | --- |
| Add member | `member add nus/NUS_ID name/NAME phone/PHONE email/EMAIL` |
| Edit member | `member edit nus/NUS_ID [name/NAME] [phone/PHONE] [email/EMAIL]` |
| List members | `member list` |
| Search, filter and sort members | `member search [q/TEXT] [holding/HOLDING] [status/STATUS] [sort/FIELD] [order/ORDER]` |
| View member and current holdings | `member view nus/NUS_ID` |
| Soft-delete member | `member delete nus/NUS_ID` |
| Restore member | `member restore nus/NUS_ID` |
| Add equipment | `equipment add name/NAME category/CATEGORY condition/CONDITION [notes/NOTES] [count/COUNT]` |
| List equipment | `equipment list` |
| Search, filter and sort equipment | `equipment search [q/TEXT] [category/TEXT] [condition/CONDITION] [availability/AVAILABILITY] [status/STATUS] [sort/FIELD] [order/ORDER]` |
| View equipment and current holder | `equipment view uuid/UUID` |
| Edit equipment | `equipment edit uuid/UUID [name/NAME] [category/CATEGORY] [condition/CONDITION] [notes/NOTES]` |
| Restore equipment | `equipment restore uuid/UUID` |
| Delete equipment | `equipment delete uuid/UUID` |
| Open the Loans tab | `loans list` |
| Assign equipment | `loans add uuid/UUID[,UUID...] nus/NUS_ID assigned/ASSIGNMENT_DATE expected/EXPECTED_RETURN_DATE` |
| Return equipment | `loans return uuid/UUID[,UUID...] returned/ACTUAL_RETURN_DATE [condition/CONDITION]` |
| Undo the last record change | `undo` |
| Redo the last undone change | `redo` |

See [search, filters and sorting](commands/search-and-planning.md) for accepted search parameter values.

Application utilities: `undo` and `redo` navigate up to ten record changes per session; `mode dark` / `mode light` change appearance; `help` (or F1) opens Help; `exit` closes Laplace.
