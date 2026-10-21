> Legacy-command retirement amendment (21 October 2026): top-level `add`, `edit`, `delete`,
> `clear`, `find`, and `list` are rejected. Earlier requirements below to preserve public person-command
> routing are superseded. Stored persons, shared value types and legacy implementation/UI classes
> remain for compatibility and later cleanup. See the
> [current routing contract](DeveloperGuide.md#command-routing-and-legacy-compatibility).

> v1.3.3 amendment: [MVP fields](commands/MvpFields.md) and [completion evidence](gui/MvpCompletion.md)
> supersede older component handoff assumptions below. #93/#95 are merged; validation, transactions,
> holdings order and open-only Loans now follow the detailed MVP specification.

# Laplace GUI and command integration contract

## Status and delivery

These are v1.3 milestone additions. The Stage A display setup was merged in #80;
shared parsing/sorting in #83 and the theme/FX test helper in #81. The directory/list
components, cards and Loans panel are also merged in the v1.3.2 baseline:
team master `900cee6fe5ce3aadbf3fba3be5dbff346d481409` (PR #85).

Nicholas's remaining scope is tracked in [issue #92](https://github.com/AY2627S1-CS2103T-F15-2/tp/issues/92)
on branch `v1.3.2`, following the setup tracked in #72. Both individual view commands/parsers
and both detail panels are now implemented with tests and rendered component previews.
List routes are active; view-route activation remains with Duy (#73), and application-shell
assembly/navigation/refresh remains with Keith (#91). Independent components do not imply
that the application already renders or navigates to these panels.

## Display and read-only interfaces

`DisplayRequest` is final and immutable, with no public raw constructor. Its nested `Type`
and all factories/getters below are public. Detail factories reject null; other targets
carry neither identity. Equality and hash code include the type and identity payloads.

```java
public enum Type {
    UNCHANGED, LEGACY_PERSONS, MEMBERS, MEMBER_DETAILS, EQUIPMENT, EQUIPMENT_DETAILS, LOANS
}
public static DisplayRequest unchanged();
public static DisplayRequest legacyPersons();
public static DisplayRequest members();
public static DisplayRequest memberDetails(NusId nusId);
public static DisplayRequest equipment();
public static DisplayRequest equipmentDetails(UUID uuid);
public static DisplayRequest loans();
public Type getType();
public Optional<NusId> getMemberNusId();
public Optional<UUID> getEquipmentUuid();
```

`CommandResult` keeps these public constructors and accessors:

```java
public CommandResult(String feedbackToUser);
public CommandResult(String feedbackToUser, boolean showHelp, boolean exit);
public CommandResult(String feedbackToUser, DisplayRequest displayRequest);
public String getFeedbackToUser();
public boolean isShowHelp();
public boolean isExit();
public DisplayRequest getDisplayRequest();
```

Feedback and display requests are non-null. Legacy constructors use `legacyPersons()`.
Explicit display construction sets help/exit false. Equality and hash code include the
display request. Help/exit take precedence over display changes in the future shell.
No JavaFX nodes or mutable Model instances cross this bridge.

`Logic` preserves `execute`, `getFilteredPersonList`, `getGuiSettings` and `setGuiSettings`,
and adds only `ReadOnlyAddressBook getAddressBook()` and
`Availability getAvailability(UUID uuid)`. `LogicManager` delegates both directly to Model,
including null/unknown-UUID failures. Reading does not save. The root's observable lists
are live and unmodifiable; panels must never cast the root to mutate it.

`LogicManager.execute` stages the six Laplace mutations on an isolated Model snapshot. Atomic
JSON replacement must succeed before `setAddressBook` publishes through the original observable root.
Rejected parsing/business checks never save. A failed save preserves all records and the old file,
with its original cause retained. Legacy person-command behavior remains compatible. UI selection is not stored.

## View-feature handoff (components implemented; integration pending)

Duy will route `MemberViewCommandParser` to `MemberViewCommand(NusId)` and
`EquipmentViewCommandParser` to `EquipmentViewCommand(UUID)`. Parsers use
`LaplaceArgumentParser` and `ParserUtil`. Commands resolve exact identities and references
without mutation, then return the typed requests below. Panels perform read-only joins.
Keith consumes the panel signatures below and calls show/refresh on the FX thread.
Missing referenced data is an explicit inconsistency, never an invented holder or an
unassigned item. The concrete parsers, commands and panels are present. `DetailsPanelTest` verifies fields
and state changes; `DetailsPanelPreviewTest` renders normal, small, long-text, empty and
missing-state previews under `build/reports/laplace/details-*.png`. These previews pair real
directory/detail components without claiming the final shell or routed command execution.

Laplace parameter markers are start-of-input or whitespace-delimited
`[A-Za-z][A-Za-z0-9_-]*/` tokens, including in free text. Unknown markers are errors.
URLs such as `https://example.com` and slashes within ordinary words are not markers.
Spaces and tabs separate parameters; there is no quoting or escaping mechanism.
Required values cannot be blank; notes may be empty. List commands accept whitespace only.
Legacy tokenization remains unchanged.

## GUI reference and required behavior

Reproduce `docs/images/Ui.png` as a working JavaFX layout, not a bitmap background:

- Dark charcoal top bar with Laplace at the left and Members, Equipment and Loans navigation at the right. Indicate the active section in text/style as well as color.
- Light gray application background; white bordered left list and right content panels. Start with roughly 28 percent of the content width on the left and 72 percent on the right, with sensible minimum sizes and responsive resizing.
- Members mode: a count heading, selectable member rows, blue selected-row treatment, a small selection hint, and a Member Directory summary on the right. Include Active members, Displayed, Sort order and Selection rows, the count badge and a soft-green list-status panel shown in the reference.
- Equipment mode: use the same layout language for item rows and an Equipment Directory summary. Display UUID, category, condition, derived availability and current holder. Identically named physical items remain separate rows.
- Details mode keeps the corresponding list visible on the left and replaces the right summary with member/equipment details.
- Loans mode is a functional read-only tab showing open assignments only, with equipment/member identities and dates, using the same theme. The reference does not specify its exact interior; follow the shared style rather than inventing a new visual system.
- A full-width feedback strip appears above the bottom command input. Successful commands use the reference's soft green treatment; failures use clearly readable error text and an error treatment. The command label, full-width input, and Press Enter to execute hint appear at the bottom.
- Suggested theme palette: header `#263238`, background `#EEF1F3`, surfaces `#FFFFFF`, borders `#DCE3E8`, primary text `#37474F`, secondary text `#78909C`, accent `#1688E8`, selection `#E3F2FD`, success `#E8F5E9`, error `#FDECEC`. These are implementation targets approximating the reference, not measured pixel samples.
- Use system fonts, consistent spacing and restrained borders. Do not add dependencies, web views or generated image assets for this design. Preserve useful existing help/exit behavior and accessibility through keyboard focus and readable contrast.
- Target a comfortable initial window around 1100 by 700 and a usable minimum around 760 by 500. Content must scroll or wrap rather than overlap or hide identifiers. Inspect both normal and small sizes. Preserve saved window preferences within usable bounds.
- Every displayed count, name, holder, availability and date comes from current data. Do not hardcode the screenshot's three rows/counts as application state. All current stored members count as active; archiving is not implemented.

On startup show the Members directory with the first member selected, if any, while keeping the directory summary visible. A programmatic initial selection must not accidentally execute a view command. A user row click/activation executes the corresponding `member view` or `equipment view` command through the same application command callback. Members and Equipment navigation execute the existing list commands through that callback. The Loans button selects the read-only Loans view directly; the additional `loans list` command requests that same view through `DisplayRequest.loans()`.

Enter submits once. Ignore blank/whitespace-only input. Clear input only after success; retain invalid input for correction and return focus appropriately. Display errors without destroying the current screen/selection. Do not infer navigation by parsing feedback strings.

Preserve selection by NUS ID/UUID across refreshes. If the selected entity is deleted, choose the next valid selection deterministically or show an empty directory, and leave stale details mode. If a loan changes, refresh holder names, holdings counts, details, availability and loan rows even when the member/equipment collection itself did not change.

## Shared display contract owned by Nicholas

The following are NEW milestone contracts, not assertions that these declarations already exist. Nicholas must implement them in a small setup PR before consumers depend on them. No other contributor creates a competing type or modifies these shared files. This prompt assigns the new milestone work explicitly; preserve the existing domain contract while applying this new ownership table rather than treating the old scaffold ownership table as a reason to block authorized feature work.

Add `seedu.address.logic.commands.DisplayRequest`, an immutable value object with nested enum:

```java
enum Type { UNCHANGED, LEGACY_PERSONS, MEMBERS, MEMBER_DETAILS,
            EQUIPMENT, EQUIPMENT_DETAILS, LOANS }
static DisplayRequest unchanged();
static DisplayRequest legacyPersons();
static DisplayRequest members();
static DisplayRequest memberDetails(NusId nusId);
static DisplayRequest equipment();
static DisplayRequest equipmentDetails(UUID uuid);
static DisplayRequest loans();
Type getType();
Optional<NusId> getMemberNusId();
Optional<UUID> getEquipmentUuid();
```

Methods above are public. Factories taking an identity reject null. Detail requests carry exactly their relevant identity; other requests carry neither. Implement value equality/hashCode and useful string output. Do not expose invalid combinations through a public raw constructor.

Extend `CommandResult` with `CommandResult(String feedbackToUser, DisplayRequest displayRequest)` and non-null `getDisplayRequest()`. Preserve the existing one-argument and three-argument constructors and their feedback/help/exit behavior. For existing constructors use `legacyPersons()` as the display target so existing person commands still show the legacy person list. Help/exit handling takes precedence and does not unnecessarily navigate. Include the new value in equality/hashCode; update owned tests deliberately. Laplace commands use the explicit display-request constructor.

Add only these read-only methods to `Logic`, delegated by `LogicManager`:

```java
ReadOnlyAddressBook getAddressBook();
Availability getAvailability(UUID uuid);
```

Keep `Model` and `ReadOnlyAddressBook` signatures unchanged. UI components read immutable entities and the root's existing observable lists; they never write through the root. Preserve existing `Logic` methods and update affected Logic test doubles in Nicholas's setup PR.

Command display results are fixed:

| Successful command | Display request |
| --- | --- |
| member add | memberDetails(new member NUS ID) |
| member delete | members() |
| member view | memberDetails(requested NUS ID) |
| member list | members() |
| equipment add | Count 1: equipmentDetails(generated UUID); count > 1: equipment with DirectoryQuery.all() |
| equipment edit or equipment restore | equipmentDetails(affected UUID) |
| equipment delete | equipment(); retains records, current query and eligible selection |
| equipment view | equipmentDetails(requested UUID) |
| equipment list | equipment() |
| loans add or loans return | equipmentDetails(affected UUID) |

## Shared parsing and sorting contracts owned by Duy

Implement new `seedu.address.logic.parser.LaplaceArgumentParser` with:

```java
public static ArgumentMultimap parse(String args, Set<Prefix> required,
        Set<Prefix> optional) throws ParseException;
```

Use the prefix grammar and validation rules above, trim field values, reject null arguments, and reject overlapping required/optional sets as programmer misuse. Return the existing `ArgumentMultimap` so feature parsers reuse existing value parsers. Calling with both sets empty implements no-argument list parsing. This helper is Laplace-specific and must not change existing person-command parsing.

Add `CliSyntax.PREFIX_EQUIPMENT_NAME` as an alias of the existing `PREFIX_MEMBER_NAME` token (`name/`), preserving all existing constants. Do not use both aliases as different parameters in one parser.

Implement new `seedu.address.commons.util.LaplaceSortOrder` with public constants:

```java
Comparator<Member> MEMBERS;
Comparator<Equipment> EQUIPMENT;
```

Normalize sort text with `trim().toLowerCase(Locale.ROOT)` without changing stored/displayed text. Members sort by normalized full name then normalized NUS ID. Equipment sorts by normalized category, normalized name, then `UUID.toString()` lexicographically. Sort derived copies/streams, never the root's unmodifiable lists. Commands and GUI use these same comparators.

## GUI component contracts and ownership

All new GUI classes below live under `seedu.address.ui` and extend the existing `UiPart<Region>`. Their roots come from FXML under `src/main/resources/view/`; keep matching filenames. The existing loader supplies the controller, so do not add `fx:controller` to these FXML files. Keep behavior in Java and styling in CSS. All constructor parameters are non-null, including Optional/function arguments.

| Owner | Public component API | Responsibility |
| --- | --- | --- |
| Keegan | `MemberCard(Member member, int itemsHeld)` | Immutable rendered row with name, NUS ID, phone, email and held count; reject a negative count. Parent ListView owns selection styling and click handling. |
| Arjo | `EquipmentCard(Equipment equipment, Availability availability, Optional<Member> holder)` | Immutable rendered item row with name/category/UUID/condition/availability and holder name plus NUS ID, or No current holder. Parent owns selection. |
| Duy | `MemberListPanel(ReadOnlyAddressBook data, Consumer<NusId> onSelect)` | Sorted left list; `void refresh()`, `Optional<NusId> getSelection()`, `void select(Optional<NusId> selection)`. |
| Duy | `EquipmentListPanel(ReadOnlyAddressBook data, Function<UUID, Availability> availabilityLookup, Consumer<UUID> onSelect)` | Sorted left list; `void refresh()`, `Optional<UUID> getSelection()`, `void select(Optional<UUID> selection)`. |
| Duy | `MemberDirectorySummary(ReadOnlyAddressBook data)` | Right directory summary; `void refresh(Optional<NusId> selection)`. |
| Duy | `EquipmentDirectorySummary(ReadOnlyAddressBook data, Function<UUID, Availability> availabilityLookup)` | Right equipment summary; `void refresh(Optional<UUID> selection)`. |
| Nicholas | `MemberDetailsPanel(ReadOnlyAddressBook data, Function<UUID, Availability> availabilityLookup)` | `void showMember(NusId nusId)`, `void refresh()`; details and sorted held-equipment records. |
| Nicholas | `EquipmentDetailsPanel(ReadOnlyAddressBook data, Function<UUID, Availability> availabilityLookup)` | `void showEquipment(UUID uuid)`, `void refresh()`; details, holder contacts and current loan dates. |
| Isaac | `LoanPanel(ReadOnlyAddressBook data, Consumer<UUID> onEquipmentSelected, Consumer<NusId> onMemberSelected)` | `void refresh()`; open loans only; callbacks navigate to existing detail commands. |

List panels consume the corresponding real card components. `select` and `refresh` must not invoke `onSelect`; only user activation invokes it, once. A present selection absent from the data is cleared. Parent orchestration selects the first valid row when needed. Panels without an entity render a useful empty/missing state. `refresh()` before the first details selection renders an empty state. Rebuilding immutable cards during refresh is acceptable at this project scale; no reactive framework is needed.

Keith owns `MainWindow`, `MainWindow.fxml`, `CommandBox`/FXML, `ResultDisplay`/FXML, the shared `LaplaceTheme.css`, and necessary `UiManager` wiring. He assembles the components and handles typed DisplayRequests. Each feature owner may add a uniquely named component CSS file if needed; nobody else edits the shared theme or main FXML. Scope component style classes with `member-`, `equipment-`, `loan-`, `directory-` or `details-` prefixes. The theme supplies `.laplace-surface`, `.laplace-title`, `.laplace-muted`, `.laplace-count-badge`, `.laplace-success`, `.laplace-error` and `.laplace-row` styles and shared ListView selection styling.

Keith extends ResultDisplay with `void setFeedbackToUser(String message, boolean isError)` while preserving its existing one-argument method as a success/neutral-compatible delegate. He owns all callers. Do not put error navigation or domain writes in a panel.

Keith is the sole owner of minimal build/CI runtime adjustments required by the new graphical tests. Preserve the project's JDK/JavaFX/dependency versions unless a separately justified compatibility fix is essential. Select matching platform/architecture JavaFX natives if the existing classifier setup fails; do not make every contributor patch dependencies. On Linux CI provide a graphical test session, for example Xvfb, and run the normal tests within it. Verify the runner actually has the required executable or provision it explicitly. Keep the existing operating-system matrix and all tests/coverage checks; do not turn GUI tests into default skips. Add these changes to the small UI-foundation PR only if needed and document the evidence.

Keith also supplies the shared test utility `seedu.address.testutil.FxTestUtil.runAndWait(Runnable action)` in his setup PR. It initializes the JavaFX toolkit once, executes on the FX thread, propagates assertions/exceptions, and does not shut down the toolkit between tests. Do not create six conflicting toolkit helpers. Headless/native-runtime failures must be reported, not silently converted into passing/skipped acceptance tests.

The shell registers listeners once on members, equipment, loans and the existing filtered person list. Coalesce refreshes onto the JavaFX thread after the current mutation finishes, for example with one queued `Platform.runLater` refresh, so intermediate multi-list updates do not render inconsistent state. Detach listeners on disposal. Refresh the visible panel and related counts without duplicate callbacks or losing selection. Preserve a legacy person-list screen for existing person commands; maintain existing public UI hooks unless a coordinated change is necessary.

## Ownership of shared files

| Files | Sole editor in this milestone |
| --- | --- |
| DisplayRequest, CommandResult, Logic, LogicManager and associated tests/doubles | Nicholas |
| GuiCommandContract documentation and corrections to ScaffoldContract status text | Nicholas |
| LaplaceArgumentParser, LaplaceSortOrder, CliSyntax, ParserUtil if required, three family routers and routing/scaffold parser tests | Duy |
| MainWindow, main FXML, CommandBox, ResultDisplay, UiManager as needed, LaplaceTheme.css, FxTestUtil, narrowly necessary build/CI graphical-runtime changes, final GUI acceptance tests | Keith |
| SampleDataUtil and its dedicated tests | Isaac |
| MemberCard, its FXML/CSS/tests | Keegan |
| EquipmentCard, its FXML/CSS/tests | Arjo |
| Feature command/parser/component files and tests | Their assigned feature owner |

The existing Model, ModelManager, AddressBook, ReadOnlyAddressBook, collection and storage contracts are frozen. A discovered defect in someone else's module requires a small separate coordinated fix, not an opportunistic rewrite. Outside Keith's explicitly assigned minimal graphical-runtime work, do not alter build dependencies/CI. Do not alter another owner's global CSS, shared fixtures, or domain semantics to make one feature easier. Ask only for a concrete unresolved contract conflict; continue independent work meanwhile.


## Equipment maintenance update

Equipment cards mark deleted units with `[DELETED]`; details expose ACTIVE/DELETED and a restore hint.
`equipment list` is read-only and resets to all records. Equipment search defaults to active units and
accepts `status/active|deleted|all`. Total includes retained units; availability totals and inventory
groups count active units only. Edit/restore details resolve the same UUID even if it no longer matches
the retained directory query. All panels refresh from published model data, without extra commands.
