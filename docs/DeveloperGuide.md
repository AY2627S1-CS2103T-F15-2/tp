---
  layout: default.md
  title: "Developer Guide"
  pageNav: 3
---

# Laplace Developer Guide

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

* Laplace evolves the course-provided [AddressBook-Level3 starter](https://github.com/NUS-CS2103-AY2627-S1/tp).
* Command text uses [IBM Plex Mono](https://github.com/IBM/plex) 2.004, copyright IBM Corp., under the SIL Open Font License. The faces and license are in `src/main/resources/fonts`.

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## **Design**

### Architecture

<puml src="diagrams/ArchitectureDiagram.puml" width="280" />

The ***Architecture Diagram*** given above explains the high-level design of the App.

The following provides a quick overview of the main components and their interactions.

**Main components of the architecture**

**`Main`** (consisting of classes [`Main`](https://github.com/AY2627S1-CS2103T-F15-2/tp/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/AY2627S1-CS2103T-F15-2/tp/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The *Sequence Diagram* below is an inherited AB3 example using `delete 1`. That command is now rejected;
see [command routing and legacy compatibility](#command-routing-and-legacy-compatibility) for current behavior.

<puml src="diagrams/ArchitectureSequenceDiagram.puml" width="574" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<puml src="diagrams/ComponentManagers.puml" width="300" />

The sections below give more details of each component.

### Contribution diagram index

The following diagrams document the current implementation of Keith's (`wtvlol`) merged contributions.
The PR and authored-commit links establish provenance; supporting classes and subsequent changes by
other contributors are included only where needed to explain the current design. Merging a teammate's
work into a feature branch does not make that work part of Keith's authorship.

| Contribution | Authored implementation commits | Current diagrams |
| --- | --- | --- |
| [#62: Root integration](https://github.com/AY2627S1-CS2103T-F15-2/tp/pull/62) | [147ba902](https://github.com/AY2627S1-CS2103T-F15-2/tp/commit/147ba902), [392b4840](https://github.com/AY2627S1-CS2103T-F15-2/tp/commit/392b4840) | RootModelClass, RootModelResetSequence |
| [#81: UI foundation](https://github.com/AY2627S1-CS2103T-F15-2/tp/pull/81) | [65b39d4a](https://github.com/AY2627S1-CS2103T-F15-2/tp/commit/65b39d4a) | UiFoundationClass, FxTestExecutionSequence |
| [#86: Identity deletion](https://github.com/AY2627S1-CS2103T-F15-2/tp/pull/86) | [4112c4de](https://github.com/AY2627S1-CS2103T-F15-2/tp/commit/4112c4de), [118fed7b](https://github.com/AY2627S1-CS2103T-F15-2/tp/commit/118fed7b) | IdentityDeletionClass, IdentityDeletionSequence |
| [#93: Application shell](https://github.com/AY2627S1-CS2103T-F15-2/tp/pull/93) | [2e99cc08](https://github.com/AY2627S1-CS2103T-F15-2/tp/commit/2e99cc08), [a69514c9](https://github.com/AY2627S1-CS2103T-F15-2/tp/commit/a69514c9), [4ce33358](https://github.com/AY2627S1-CS2103T-F15-2/tp/commit/4ce33358) | ShellNavigationClass, ShellCommandSequence, ShellExecuteSequence, ShellRefreshSequence |
| [#97: MVP completion](https://github.com/AY2627S1-CS2103T-F15-2/tp/pull/97) | [992988ad](https://github.com/AY2627S1-CS2103T-F15-2/tp/commit/992988ad), [2906d380](https://github.com/AY2627S1-CS2103T-F15-2/tp/commit/2906d380), [a2c349b9](https://github.com/AY2627S1-CS2103T-F15-2/tp/commit/a2c349b9), [37ec27aa](https://github.com/AY2627S1-CS2103T-F15-2/tp/commit/37ec27aa) | MutationTransactionClass, MutationTransactionSequence, MvpValidationActivity; deletion feedback and shell navigation above |
| [#112: Tab completion](https://github.com/AY2627S1-CS2103T-F15-2/tp/pull/112) | [59beff9b](https://github.com/AY2627S1-CS2103T-F15-2/tp/commit/59beff9b), [c4517ac0](https://github.com/AY2627S1-CS2103T-F15-2/tp/commit/c4517ac0), [2df80011](https://github.com/AY2627S1-CS2103T-F15-2/tp/commit/2df80011) | AutocompleteClass, AutocompleteSequence |

Names in the last column refer to the `.puml` sources in `docs/diagrams`, embedded in the relevant
sections below. This set excludes deployment-permission PR #89, profile/photo PRs #22/#27/#35/#36,
and unmerged remark/data-transfer PRs #7/#107. It does not replace other contributors' member, equipment,
loan, directory-search or undo/redo diagrams.

Notation follows the [course UML reference sheet](https://nus-cs2103-ay2627-s1.github.io/website/admin/uml-reference-sheet.html).
Class diagrams show selected APIs and stored-reference roles; dashed dependencies describe calls,
creation or parameter use. Sequence messages abbreviate Java calls where noted. Notes distinguish
omitted infrastructure and current behavior from the original PR's historical behavior.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/AY2627S1-CS2103T-F15-2/tp/tree/master/src/main/java/seedu/address/ui/Ui.java)

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

`MainWindow` owns navigation, the command pipeline and coalesced model refresh subscriptions. Its
member/equipment directories, summaries, details and loan panel are read-only views. Most panels extend
`UiPart` and load FXML; `EquipmentGroupPanel` and `CommandPreview` compose JavaFX controls directly.
The inherited person panel remains an internal compatibility component, outside the supported navigation.

`DisplayRequest` carries typed navigation intent. Directory requests with no query preserve criteria;
explicit list commands reset them. `InventoryGroups.Group` derives normalized category/name groups and
keeps separate retained and matching unit lists. Groups are never persisted. Selecting a group does not
choose an arbitrary UUID; physical-unit selection routes `equipment view uuid/...` through the same
pipeline as typed input. Refreshes preserve exact identities and recompute group membership after edits,
loans, deletion and restoration. The group panel shares one scrolling surface with unit details.

`CommandPreview` copies exact plain text or prepares `CommandBox`; neither action executes a mutation.
The command catalog supplies examples and explicit completion. Input history is bounded to 100 session
entries. `ResultDisplay` keeps full feedback behind a compact, expandable view. Related-record links use
identity-based commands. `LoanPanel` filters open and completed loans by stable member/equipment IDs.
Only open loans derive urgency from expected return dates and an injectable clock, and offer a return
shortcut for explicit review and submission. Completed rows show recorded conditions and return dates.
Loan urgency refreshes on commands, data changes and opening the Loans tab; there is no background
midnight timer.

`LaplaceTheme.css` defines shared looked-up colors; component styles refer to these tokens. A `dark` style
class changes the palette for both windows, including table selection, feedback and command previews.
`ModeCommand` updates only `GuiSettings.darkMode` and returns an unchanged display request. Its
`isReadOnly()` flag means no **domain-record** persistence; preferences are saved by the existing shutdown
path. Missing `darkMode` in earlier preference JSON defaults to false. Window geometry and appearance are
preserved together; equipment/member/loan schemas are unchanged.

Tests exercise actual FXML/CSS on the shared JavaFX thread, command/storage routing, identity preservation,
filter scope, date boundaries, clipboard text, theme changes and preference compatibility. UI snapshots
are written under `build/reports/laplace` for visual review. Command text and identifiers use bundled
IBM Plex Mono. `LaplaceFonts` registers those faces once and applies interface fonts by full face name:
Helvetica/Helvetica Bold, then Arial/Arial Bold or Segoe UI/Segoe UI Bold when unavailable, with a system
fallback. This bypasses JavaFX family/weight substitution that can clip bold interface glyphs.
The helper owns interface sizes and weights; CSS owns colors, layout and monospace text. It observes
new children, graphics and label style changes so dynamic rows and headings receive the same treatment.
Tests check resolved face names as well as rendered layouts; checking only a Bold style is insufficient.

The feedback heading displays only Last result; the box carries neutral/success/error color, and assistive
text contains the status plus the entire message. Ready command previews say `Press Enter to execute (ready)`;
templates say `Replace uppercase placeholders (template)`. Copy and Use command still never execute a command.

#### Shell execution and refresh

The following focused views complement `UiClassDiagram`: they explain the command callback and typed
navigation boundary introduced in #93, rather than claiming ownership of every panel shown. The current
equipment group panel, query criteria, previews and loan-history presentation are supporting context
from later work. `MainWindow` supplies a method-reference implementation of `CommandExecutor`; it does
not implement that interface itself.

<puml src="diagrams/ShellNavigationClass.puml" alt="Shell ownership, command callback and typed display requests" />

[Open full-size diagram](diagrams/ShellNavigationClass.png)

Typed Enter submission and explicit member/equipment identity links share `executeCommand`. Failures
retain correction text; a Space-activated link defers correction focus until key release. Group selection
and navigation tabs display views directly, while command previews only prepare text.

<puml src="diagrams/ShellCommandSequence.puml" alt="Shared typed-command and identity-link execution sequence" />

[Open full-size diagram](diagrams/ShellCommandSequence.png)

Both entry paths call the same `executeCommand` method, shown once below. MainWindow returns the result
or rethrows the original exception after updating feedback and refresh state.

<puml src="diagrams/ShellExecuteSequence.puml" alt="Shared executeCommand result handling sequence" />

[Open full-size diagram](diagrams/ShellExecuteSequence.png)

Root invalidations queue one refresh. A synchronous successful-command refresh clears that pending
work, and disposed windows ignore queued callbacks. Programmatic selection never dispatches another
user command. A request without a query retains existing criteria; only an explicit reset supplies an
all-record query. Missing identities fall back to a directory, but a soft-deleted retained record still
exists and can remain visible in details.

<puml src="diagrams/ShellRefreshSequence.puml" alt="Coalesced refresh, selection recovery and disposal sequence" />

[Open full-size diagram](diagrams/ShellRefreshSequence.png)

#### UI foundation and JavaFX test execution

PR #81 supplied shared theme resources, `FxTestUtil` and the matching JavaFX test runtime. The production
classes below show how the current UI loads those resources; `UiPart`, JavaFX and later font handling
are context, not new classes attributed to that PR. Stylesheets are resources, not domain objects.

<puml src="diagrams/UiFoundationClass.puml" alt="UI resource loading and shared JavaFX test-support dependencies" />

[Open full-size diagram](diagrams/UiFoundationClass.png)

`runAndWait` runs inline on the FX thread or awaits shared startup and queues a `FutureTask`. Both waits
have a 30-second bound. Original runtime failures and errors propagate; interruption preserves the flag
and cancels an unfinished queued task. Cancellation cannot undo an action already running. Tests own
their windows and must not shut down the shared toolkit.

<puml src="diagrams/FxTestExecutionSequence.puml" alt="JavaFX toolkit startup, execution and test failure propagation" />

[Open full-size diagram](diagrams/FxTestExecutionSequence.png)

### Tab completion

`MainWindow` supplies the live read-only address book to `CommandBox`. `CompletionService` computes
replacement ranges and ordered candidates without calling logic, history or storage. Command constants
and shared search prefix constants define the completion vocabulary. The newly merged `undo`/`redo`
parser routes are not currently registered in `CompletionService`. Search enum options follow
`DirectoryQuery`; dates, numeric counts, new member IDs and free-form notes are entered manually.
Batch loan completion replaces only the active UUID entry and excludes neighboring identities.
`CompletionController` owns cycling and resets after input, caret, focus or model changes. Enter retains
the ordinary parser and transactional command path. Tests compare family completion to parser routes,
exercise real JavaFX events, and load production classes from the packaged JAR. Gradle enforces 100%
instruction, line and branch coverage for the feature, including its UI wiring. See [Testing](Testing.md).

The controller is created by `CommandBox` and retained through the field's registered callbacks, rather
than a `CommandBox` member field. Static vocabulary uses no model reads. Record-value suggestions
read the live root synchronously on the FX thread and observe member/equipment collection changes
with weak listeners. These notifications invalidate record candidates after mutations, undo/redo
and reload; rejected mutations and pure queries leave those collections unchanged. Cached record
cycles check root identity cheaply, while unchanged candidates cycle without copying or rescanning
records. Loan history and transaction validation are outside suggestion reads.

<puml src="diagrams/AutocompleteClass.puml" alt="Tab completion service, controller and read-only data dependencies" />

[Open full-size diagram](diagrams/AutocompleteClass.png)

The first plain Tab selects the first candidate. Further Tab or Shift+Tab events wrap through the same
candidate list; Shift+Tab alone does not start a cycle. External edits, caret/anchor/focus changes and
Enter clear the cycle. Without a candidate the event remains unconsumed. Replacement preserves the
original suffix, including Unicode separators and neighboring batch UUIDs, and never executes or saves.

<puml src="diagrams/AutocompleteSequence.puml" alt="Tab suggestion, forward and reverse cycling, and range replacement" />

[Open full-size diagram](diagrams/AutocompleteSequence.png)

### Logic component

**API** : [`Logic.java`](https://github.com/AY2627S1-CS2103T-F15-2/tp/tree/master/src/main/java/seedu/address/logic/Logic.java)

The following inherited class diagram shows the AB3 `Logic` structure; its person-command routes are retired:

<puml src="diagrams/LogicClassDiagram.puml" width="550"/>

The sequence diagram below is a historical AB3 example of `execute("delete 1")`, not a supported Laplace flow.

<puml src="diagrams/DeleteSequenceDiagram.puml" alt="Interactions Inside the Logic Component for the `delete 1` Command" />

<box type="info" seamless>

**Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, the lifeline continues till the end of diagram.
</box>


#### Command routing and legacy compatibility

`LogicManager.execute()` first calls `AddressBookParser.parseCommand()`. The root parser accepts
`member`, `equipment`, and `loans`, delegating to their family parsers and then the appropriate leaf
parser (for example, `EquipmentCommandParser` and `EquipmentAddCommandParser`). It also accepts
`undo`, `redo`, `mode`, `help` and `exit`. Family and leaf parsers remain responsible for validating subcommands and arguments.

The retired top-level words `add`, `edit`, `delete`, `clear`, `find`, and `list` now reach the existing
unknown-command branch and throw `ParseException("Unknown command.")`. In particular, `list members`
is rejected; the supported command is `member list`. Rejection occurs before a command is constructed
or executed, before a transaction snapshot is created, and before storage is called.

For supported commands, `LogicManager` executes transactional mutations on an isolated model snapshot,
saves that candidate state, and publishes it to the live root only after saving succeeds. Commands
marked `isReadOnly()` return without saving. This retirement does not change those execution rules.

`AddressBook` is still the shared root for persons, members, equipment, and loans. Its name and the
`getAddressBook()`/`saveAddressBook()` APIs do not identify disposable person-only behavior. Existing
JSON `persons` data is retained and round-trips when supported commands save. Person value classes
used by members, legacy command/parser implementations and their unit tests, and the legacy UI panel
remain for a separate dependency-aware cleanup. Removing public routes does not remove stored records.

The inherited parser diagram below documents the remaining AB3 classes, not the active root dispatch:

<puml src="diagrams/ParserClasses.puml" width="600"/>

### Model component
**API** : [`Model.java`](https://github.com/AY2627S1-CS2103T-F15-2/tp/tree/master/src/main/java/seedu/address/model/Model.java)

#### Root collection integration

PR #62 integrated the four root collections and stable observable views. Each root has independent
collection containers; immutable entity values may be shared across snapshots. Domain containers are
created lazily, while callers always receive stable unmodifiable lists. Cross-record loan integrity
belongs to `AddressBook`, rather than the UI or serialization layer.

<puml src="diagrams/RootModelClass.puml" alt="Root collections, immutable values and stable observable wrappers" />

[Open full-size diagram](diagrams/RootModelClass.png)

`resetData` first copies and validates every replacement collection and loan reference, and resolves all
replacement backing views. Only then does it replace live fields and views, deferring notifications until
the complete dataset is visible. The current package overload also runs a model-state callback before
notifications so observers see matching records and session history. It runs that callback even when
records are unchanged, without list notifications; the public overload supplies a no-op callback.
This coordination is supporting behavior from #137/#138. Validation failure leaves records/history
intact. The callback must not throw or mutate records; the diagram does not promise recovery from
arbitrary exceptions thrown by publication actions or external observer callbacks.

<puml src="diagrams/RootModelResetSequence.puml" alt="Root reset validation and complete publication before notification" />

[Open full-size diagram](diagrams/RootModelResetSequence.png)

#### Equipment and loan model

The following diagram shows the equipment-related subset of the current model. Member/person
collections, preferences, JavaFX collection wrappers and interfaces are omitted to keep its scope clear.
Solid arrows show stored references; the dashed loan-to-equipment arrow describes UUID resolution,
not an object reference stored inside `Loan`.

<puml src="diagrams/EquipmentModel.puml" alt="Equipment model, retained identities and loan references" />

`AddressBook` creates equipment/loan containers lazily, hence the `0..1` multiplicities. A null container
means an empty collection to callers. The `0..*` multiplicity at each collection end of the value
associations accounts for immutable objects shared across transaction snapshots; it does not mean
one physical unit has several UUIDs. Equipment collections hold distinct UUIDs, and editing replaces a
value at its existing position. Ordinary associations are used instead of composition because immutable
values can outlive or be shared by their containers.

| Relationship or rule | Meaning in a valid root dataset |
| --- | --- |
| Loan → equipment | Each loan's `equipmentUuid` resolves to exactly one retained equipment record. A unit can have zero or more historical loans, with at most one open loan. |
| Loan → member (omitted from diagram) | Each loan's `memberNusId` resolves to one retained member. A member may have multiple open or closed loans. |
| Identity and details | UUID identifies a physical unit. Name/category determine a derived inventory group; matching descriptions do not merge identities. |
| Lifecycle | Deleted records retain UUIDs, details and closed loans. Open loans cannot reference deleted equipment or deleted members. |
| Availability | `ModelManager` returns UNAVAILABLE for deleted units; otherwise the calculator returns ASSIGNED for an open loan, AVAILABLE for GOOD/FAIR, and UNAVAILABLE for DAMAGED/UNDER_REPAIR. |

Availability, holders and inventory totals are derived, never persisted as independent fields.
`AvailabilityCalculator` only accepts condition and open-loan state; the deletion check belongs to
`ModelManager`. `AddressBook` validates cross-record references, while `UniqueLoanList` enforces the
one-open-loan-per-unit rule. `UniqueEquipmentList` checks UUID uniqueness and identity-preserving
replacement. These responsibilities keep editing and restoration from breaking loan references.

#### Loan workflow and history

The following class diagram traces batch assignment and return from their parsers to the
model, JSON adapter and history views. It lists the loan-facing methods relevant to this
workflow; shared infrastructure and unrelated methods are omitted. `AddressBook` validates
each batch in a candidate root, while `UniqueLoanList` retains completed loans and rejects
a second open loan for the same equipment UUID. `MemberDetailsPanel` and
`EquipmentDetailsPanel` both call `LoanHistoryContent.add` for their selected identity.

<puml src="diagrams/LoanWorkflowClassDiagram.puml" alt="Loan workflow and history class diagram" />

#### Root and member model

<puml src="diagrams/ModelClassDiagram.puml" alt="Root collections, stable views and immutable member details" />

The diagram combines the root collection structure with the member model used by editing, soft-delete
and restoration. `ModelManager` delegates to `AddressBook`, which maintains person, member, equipment
and loan containers. `UniquePersonList` is always present; the three domain containers are initialized
on demand, so their multiplicities are `0..1`. A null container represents an empty collection.

Each root maintains four stable `CollectionView<E>` objects, one per collection. This private nested
class extends JavaFX `SimpleListProperty<E>`. Public unmodifiable wrappers and collection factories
are omitted. `resetData` validates replacement containers and loan references before installing new
backing lists. Notifications are suspended until every view reflects the replacement dataset.
The package-private overload runs a publication callback before notifications resume, coordinating
matching records and immutable history. The callback also runs when records are unchanged.

The collection-to-entity multiplicities allow immutable values to be shared across independent
transaction snapshots. Loan identities resolve through the root; `Loan` does not store direct
`Member` or `Equipment` references. Equipment, loan and legacy Person details are omitted to keep this diagram focused.

The root structure and stable-view portion adapts Keith's
[PR #141 diagram](https://github.com/wtvlol/tp/blob/3a8b932635cee7489a277fa6c895d32c1894e84e/docs/diagrams/RootModelClass.puml).
Member editing and retained deletion come from [#114](https://github.com/AY2627S1-CS2103T-F15-2/tp/pull/114)
and [#116](https://github.com/AY2627S1-CS2103T-F15-2/tp/pull/116); record/history publication is supporting
behavior from [#137](https://github.com/AY2627S1-CS2103T-F15-2/tp/pull/137)
and [#138](https://github.com/AY2627S1-CS2103T-F15-2/tp/pull/138).

`Member` is immutable and contains its NUS ID, name, phone, email and a `deleted` boolean. The existing
four-field constructor creates an active member; the five-field constructor accepts deletion state.
Equality and hashing include all five fields, while `isSameMember` compares only the NUS ID.
The diagram shows object-valued fields once, as navigable association lines with multiplicities.
Role labels are omitted for readability. The primitive `deleted` flag appears in the attributes
compartment. Interface classifiers use the `<<interface>>` keyword and dashed realization lines.

Editing replaces supplied details without changing the NUS ID or deletion state. Deleted members must
be restored before editing. Soft-delete and restoration create replacements through `setDeleted`,
retaining collection position, reserved identities and historical loan references. The exposed member
list remains observable and includes deleted records. The model also retains user preferences through
`UserPrefs` and `ReadOnlyUserPrefs`.

`Name.forMember` collapses whitespace and applies Unicode NFC before validating new names. Validation
allows letters followed by combining marks, spaces, straight/curly apostrophes, hyphens and periods,
requires at least one letter, and limits the result to 100 Unicode code points. Add and edit share this
factory. `Name.fromStoredMember` accepts the expanded rule and legacy values while preserving their
exact saved spelling. Editing another field retains the existing `Name`; no schema migration is needed.

Deletion is blocked while a member has outstanding loans. Deleted members remain visible and
recoverable indefinitely; editing and new loans require restoration first. Restoration has no expiry
or automatic purge. Mutations use the existing transactional save-before-publish pipeline.

The diagram follows the [course modeling notation](https://nus-cs2103-ay2627-s1.github.io/website/se-book-adapted/chapters/modeling.html)
and shows selected structure; lifecycle behavior is explained in the text above.

### Storage component

**API** : [`Storage.java`](https://github.com/AY2627S1-CS2103T-F15-2/tp/tree/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="850" alt="Current storage adapters and relationships" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

`JsonSerializableAddressBook` stores collections of member, equipment and loan adapters alongside
the retained legacy person adapters. Loan adapters store the member NUS ID and equipment UUID,
preserving references without duplicating either record. The diagram shows storage types and
adapter collections; value fields and conversion methods are omitted.

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### Directory search and inventory planning

`MemberCommandParser` and `EquipmentCommandParser` delegate `search` arguments to
`DirectorySearchCommandParser`. It validates the allowed prefixes and sort fields, then constructs
an immutable `DirectoryQuery` and a `DirectorySearchCommand` for the requested directory.
Search defaults to active records; `status/deleted` and `status/all` expose retained records.

The sequence below shows successful execution of `member search q/alice sort/name`.
Parsing, collection iteration, and result-object construction are omitted to focus on
the read-only command path. `data` is the `ReadOnlyAddressBook` returned by the model.

<puml src="diagrams/DirectorySearchSequence.puml" alt="Read-only member search execution and query-carrying result" />

`DirectorySearchCommand` counts matches using `DirectoryQuery`, then returns a `CommandResult`
containing `DisplayRequest.members(query)` or `DisplayRequest.equipment(query)`. Since the command
is read-only, `LogicManager` returns without creating a transaction snapshot or saving data.
Equipment queries additionally use `Model.getAvailability` to derive availability from current state.

`MainWindow` passes the same query to the directory list and its summary before refreshing them.
Both evaluate the query against current data, keeping displayed rows and matching counts consistent.
Sorting affects returned snapshots rather than stored records. Navigation requests without a query
preserve the current criteria; explicit list commands pass `DirectoryQuery.all()` to reset them.
After mutations, the shell refreshes the views with their retained queries and recovers selection by
NUS ID or equipment UUID when possible.

`EquipmentDirectorySummary.refresh` builds inventory summaries with `InventoryGroups.group`, passing
`DirectoryQuery.all().equipment(data, availabilityLookup)` as the matching units. It formats each group
using `EquipmentPresentation.counts(group.units(), availabilityLookup)`. The activity below shows this
GUI path when availability can be resolved; it does not describe the retained `summarize` helper.

<puml src="diagrams/InventoryPlanningActivity.puml" alt="Grouping all equipment units and formatting active availability and retained deletion counts" />

Each group uses a pair of normalized category and name values, keeping the two keys separate even
when either contains a display separator. `InventoryGroups.group` retains all physical units under each
key and emits groups in first-match order. For this caller, the all-records query supplies default
category/name/UUID order, so groups appear in category/name order. Other callers can pass a filtered or
sorted matching list; `group.matching()` records those matches while `group.units()` retains the full
group. The summary uses the all-records query, so its group counts are independent of search filters.

`EquipmentPresentation.counts` looks up each active unit's availability by UUID and counts deleted
units separately without an availability lookup. It displays zero for absent availability states and
adds a deleted count only when nonzero. The summary prefixes these counts with the category and name
of the first unit in the group, preserving its stored spelling rather than displaying the normalized
key. If its initial scan cannot resolve an active unit's availability, the summary displays
`Inventory groups unavailable: inconsistent equipment data.` instead of group lines. These derived
summaries preserve individual equipment identities and do not introduce a stored quantity field.

<!-- Nguyen Anh Duy used OpenAI Codex to assist with this section and its two PlantUML diagrams. -->

### Identity-based deletion

The #86 leaf parsers require one normalized NUS ID or UUID and construct commands that resolve the
stored entity. The root enforces the open-loan constraint. Member failures report the held-item count;
equipment failures report the current holder. Successful feedback uses the stored name and identity.
The #97 follow-up captures that context before mutation and preserves original domain failure causes.

<puml src="diagrams/IdentityDeletionClass.puml" alt="Member and equipment deletion parsers, commands and model boundary" />

[Open full-size diagram](diagrams/IdentityDeletionClass.png)

This member example runs on the isolated candidate supplied by `LogicManager`. Equipment differences
are noted below the sequence. Successful command execution still requires the transaction's atomic save
before the live root changes. Current soft deletion retains details and closed loans; it supersedes the
permanent deletion and related-history cleanup in the original #62/#86 implementation.

<puml src="diagrams/IdentityDeletionSequence.puml" alt="Identity lookup, deletion guards and contextual member feedback" />

[Open full-size diagram](diagrams/IdentityDeletionSequence.png)

### Domain mutation transactions

PR #97 introduced the transaction boundary used by current Laplace mutations. Commands explicitly opt
in with `isTransactional()`. `LogicManager` executes on independent candidate containers, saves the
candidate atomically, then calls `publishTransaction` to publish records and matching session history
through the existing observable root. Commands marked `isReadOnly()` return before recording or saving.
This contract does not turn remaining legacy commands into transactional mutations automatically.
The history steps are supporting context from #137/#138; see [Undo/redo](#undo-redo-feature).

<puml src="diagrams/MutationTransactionClass.puml" alt="Domain transaction and atomic storage class relationships" />

[Open full-size diagram](diagrams/MutationTransactionClass.png)

Serialization precedes writing. The writer flushes a sibling temporary file and requires an atomic
replacement, without a non-atomic overwrite fallback. Parse/execution failures and handled persistence
failures do not publish candidate state. A partial write or failed replacement preserves the previous
save file. Original I/O causes remain attached to the reported command failure. Ordinary transactional
mutations stage a history entry after execution and before saving; undo/redo navigate existing history
instead. Failed saving leaves live records and history untouched, and successful publication adopts
matching history before record observers run.

<puml src="diagrams/MutationTransactionSequence.puml" alt="Isolated mutation, atomic persistence, failure and live publication" />

[Open full-size diagram](diagrams/MutationTransactionSequence.png)

The next activity uses equipment creation to connect #97's input normalization, reading-order validation
and bounded UUID retries to the current batch-add implementation. Batch counts and reserving every UUID
before insertion are later supporting behavior. Other parsers reuse the same argument-validation
boundary; exact field rules remain in the command documentation and User Guide.

<puml src="diagrams/MvpValidationActivity.puml" alt="Equipment input validation and bounded UUID reservation activity" />

[Open full-size diagram](diagrams/MvpValidationActivity.png)

The original #97 presentation hid completed loans. Current loan panels and entity details show retained
completed history; see [Loan workflow and history](#loan-workflow-and-history). The shell diagrams above
show the current navigation boundary without reinstating that historical presentation policy.

### Adding multiple physical equipment units

`EquipmentAddCommandParser` accepts optional `count/` through `LaplaceArgumentParser`, retaining its
left-to-right validation and duplicate-prefix checks. Counts default to 1 and must be ASCII digits with
a numeric value from 1 to `EquipmentAddCommand.MAX_COUNT` (100). The command constructor also validates
the range for direct callers. The cap bounds accidental bulk creation and feedback size.

`EquipmentAddCommand` reserves all identities before inserting units. A local set prevents duplicate
UUIDs within the batch; `Model.hasEquipment` excludes already-stored UUIDs. Each unit has a bounded
100-attempt generation budget. The command computes an advisory duplicate-description warning against
the pre-batch records, then inserts units into the candidate model supplied by `LogicManager`.

The existing transactional path saves that candidate once and publishes only after success. Generation
failure leaves the candidate untouched; insertion or persistence failure discards the candidate without
changing live records. No quantity field, batch entity, persistence migration, or new model API is added.

Count 1 preserves existing feedback and `equipmentDetails(uuid)` navigation. Larger batches report all
new UUIDs and return `DisplayRequest.equipment(DirectoryQuery.all())`, resetting prior filters/sorting
so the newly created units can be found. Availability remains derived from each unit's condition/loans.

#### Equipment-add parsing

The parsing sequence below traces a valid `equipment add ... count/2` request and leaf-parser
rejection. `AddressBookParser` selects the family, `EquipmentCommandParser` selects the operation,
and `EquipmentAddCommandParser` applies the shared grammar and validators before constructing the
command. Parsing is separate from UUID generation and does not access or mutate the live model.

<puml src="diagrams/EquipmentAddSequence.puml" alt="Equipment add parsing and validation sequence" />

The diagram omits shared-validator lifelines and command-constructor normalization calls. The parser
returns a command; it does not execute it. Missing/unknown family or subcommand errors stop earlier in
the chain. Neither a parser failure nor invalid count creates a transaction snapshot or saves data.

#### Equipment-add execution and publication

The next sequence starts after successful parsing. `Model.createTransactionSnapshot()` creates a new
`ModelManager` with independent collection containers and copied preferences. It initially shares the
live model's immutable `AddressBookHistory`. Immutable entity values may be shared until a command
replaces them. `EquipmentAddCommand.execute(candidate)` prepares the complete batch before inserting any unit. The diagram abbreviates the root/collection delegation and the repeated read-only
UUID checks; these occur on the candidate, not on the live model.

<puml src="diagrams/EquipmentTransactionSequence.puml" alt="Equipment batch execution, atomic save and failure path" />

The `CommandResult` exists before saving, but `LogicManager` returns it to the UI only after successful
persistence and publication. Count 1 requests exact-unit details; larger batches request the full
equipment directory. Observable refreshes resolve current data by identity after publication.

The shown failure alternative is an `IOException` during saving. Serialization/write/replacement
failure prevents publication; partial writes affect only a private temporary file, and unsupported
atomic replacement fails rather than falling back to truncation. Temporary-file cleanup is attempted
on failure. The existing data file remains intact; a first failed save need not leave a data file.
Validation, UUID-generation and command-execution failures occur before the save call and are omitted
from the successful execution path shown. A failure after candidate insertion still discards that
candidate, so a later successful command cannot accidentally save a failed batch.

Editing, soft deletion and restoration use the same snapshot/save/publish boundary with different
command bodies. Member/equipment list, search and view commands, `loans list`, `help` and `exit` declare
`isReadOnly()`. They return without saving records or creating history entries, so they do not follow
this mutation sequence. Normal shutdown still saves user preferences separately.
These diagrams describe a single-user command transaction, including history recording. They do not
provide concurrent-writer coordination. [Undo/redo](#undo-redo-feature) uses the same save/publication
boundary but navigates existing history instead of recording a new change.

### Equipment editing, deletion and restoration

Each `Equipment` is an immutable physical unit. `EquipmentEditCommand` builds a replacement with the
stored UUID and any supplied details. `AddressBook.setEquipment` permits only active-to-active edits;
`UniqueEquipmentList.setEquipment` checks UUID equality and replaces the value at the same index.
Name/category changes affect derived inventory groups, never sibling units. Unspecified legacy text
is retained verbatim; new text uses equipment-add validation. Notes can be cleared explicitly.

`AddressBook.removeEquipment` now marks a unit deleted after checking that no open loan exists.
`restoreEquipment` clears the flag. Both retain UUID, details and closed loans. This supersedes the
historical scaffold's permanent deletion. Member and equipment restoration have no time limit.

<puml src="diagrams/EquipmentLifecycle.puml" alt="Equipment lifecycle with stable UUID and retained history" />

Lifecycle is separate from condition and loan status. Editing condition while loaned is allowed:
ASSIGNED remains derived from the open loan; on return the edited condition controls availability.
Deleted units derive UNAVAILABLE, cannot receive open loans, and are excluded from active availability
counts. Inventory groups retain them and report their deleted count separately.
Root loan validation applies this invariant both to new loans and complete dataset replacement/loading.

`JsonAdaptedEquipment` writes a boolean `deleted`. An absent flag defaults to false for earlier files;
explicit null and non-boolean values fail loading. Closed loans may reference deleted equipment.
All maintenance commands are transactional: execute against a snapshot, save atomically, then publish.
No-change/rejected edits and failed writes leave live state and previously saved bytes unchanged.

Directory queries share `DirectoryQuery.Status` across members and equipment. Equipment search defaults
to active; `status/deleted|all` exposes retained units. List resets to all states without recording history.
Cards label deleted units; details show lifecycle state and a restore command. Edit/restore navigate to
the same UUID, retaining query criteria even if that unit no longer matches. Total counts include deleted
units; availability counts include active units only, while groups also report retained deletions.
The Loans tab and entity
details show the retained completed loan history.

### Undo/redo feature

The CLI commands `undo` and `redo` restore complete record states within the current session.
`AddressBookHistory` is an immutable value owned by `ModelManager`, composed with the existing
`AddressBook` rather than replacing or subclassing its observable root. It retains at most ten
reversible changes (eleven snapshots). Each snapshot has independent collection containers for
members, physical equipment, loans and retained legacy persons; immutable entity values may be shared.

The class diagram below shows only the command, persistence and record-history collaborators. Parser
classes, collection wrappers, domain entities and preferences are omitted. The live model and each
candidate own separate roots; multiple immutable history values may reference the same private stored
snapshots. Accordingly, snapshot sharing uses ordinary associations rather than exclusive composition.
The ordered `states` association has multiplicity `0..11`, including empty history before the first change.
An `AddressBook` snapshot has no `ModelManager` owner, hence `0..1` at the model end of root composition;
every model still owns exactly one root. The logic associations allow model/storage collaborators to
be shared by more than one `LogicManager` instance.

<puml src="diagrams/UndoRedoClassDiagram.puml" alt="Undo and redo command, model, snapshot history and storage dependencies" />

The first successful mutation records both the preceding state and the candidate state. Later mutations
append a snapshot and a short action description, such as `equipment add`, without retaining command
parameter values in the description. Once ten changes are retained, the oldest change and its preceding
snapshot are discarded. A batch creation, issue or return is one history entry regardless of unit count.

#### Transaction and persistence boundary

1. `AddressBookParser` routes bare `undo`/`redo` through their no-argument parsers. Both reject extra
   text and prefixes using `LaplaceArgumentParser`.
2. `LogicManager` creates an isolated candidate through `Model.createTransactionSnapshot()`. Candidate
   records and preferences are copied; the immutable history can initially be shared safely.
3. An ordinary transactional command changes candidate records. `Command.recordsHistory()` tells
   `LogicManager` to call `recordAddressBook(previous, description)` on that candidate. A real change
   discards any redo branch; an unchanged candidate leaves history untouched.
4. `UndoCommand`/`RedoCommand` instead check availability and navigate the candidate's existing history.
   They return `DisplayRequest.unchanged()` and do not record another action. Redo restores the recorded
   snapshot directly, preserving UUIDs, loan dates, conditions and references without replaying commands.
5. The existing atomic JSON save persists the candidate records once. Only after success does
   `Model.publishTransaction()` publish the candidate records and history to the live model. It preserves
   the live observable root and current preferences. Parse, validation and save failures leave live data,
   history position, redo availability and the previously saved file unchanged.

Record replacement validates all collections before publishing. `AddressBook.resetData()` installs the
new records and runs the model's history-publication action before resuming list notifications. This
keeps synchronous observers' record values and undo/redo availability consistent during transaction
publication, explicit whole-model replacement, and direct undo/redo navigation. Validation failures leave
the previous history intact; replacing identical records can still update history without list events.

The logic sequence traces one `undo` through parsing, availability checks, candidate execution,
atomic saving and publication. `AddressBookParser` delegates argument validation and command construction
to `UndoCommandParser`. The `Storage` lifeline abbreviates `StorageManager`, JSON serialization
and `AtomicFileWriter`. Invalid arguments stop during parsing; unavailable undo stops during execution.
Neither reaches saving. A save error discards the staged result without consuming a live history step.

<puml src="diagrams/UndoSequenceDiagram-Logic.puml" alt="Undo command parsing, candidate restoration, atomic save and publication or failure" />

The next two sequences expand only the model operation inside that candidate. They distinguish the
candidate's stable root from the defensive copy returned by `getCurrentState()`. `before` and `restored`
are separate immutable history values; position changes never mutate a history shared with the live model.
Field assignment is shown as a note, rather than as a fictitious method call.

<puml src="diagrams/UndoSequenceDiagram-Model.puml" alt="Candidate undo creates earlier history and restores an independent record copy" />

<puml src="diagrams/RedoSequenceDiagram-Model.puml" alt="Candidate redo creates later history and restores recorded identifiers and dates" />

`redo` follows the same logic/save/publication sequence with `RedoCommandParser`, `RedoCommand`,
`canRedoAddressBook()` and `redoAddressBook()`. Its unavailable error is `Nothing to redo.`.

The model exposes `canUndoAddressBook()`, `canRedoAddressBook()`, `undoAddressBook()` and
`redoAddressBook()`. Direct history navigation requires an available step; commands translate unavailable
history into `Nothing to undo.` or `Nothing to redo.`. Successful feedback identifies the action:
`Undid: equipment add.` or `Redid: equipment add.`. The history object guards invalid direct navigation
with `IllegalStateException` and returns defensive copies of stored record states.

Read-only queries, navigation, help, exit and appearance changes do not add history entries or invalidate
redo. Their `isReadOnly()` declarations also bypass domain-record saving. A failed record save does not
prevent navigation, Help or normal exit; shutdown preference saving remains separate.
A new successful record mutation after undo discards the undone branch. Explicit whole-model replacement
with `setAddressBook()` starts fresh history; transaction publication uses its separate API to retain it.

History is session-only and is not serialized. Restarting loads the most recently saved record state
with no undo or redo available. Preferences, search filters, window geometry and command-input history
are outside record history. The existing shell refreshes summaries, groups, holdings and loan history;
when undo removes a displayed exact record, it returns to the corresponding filtered directory.
No undo/redo buttons or new shortcuts are provided.

#### Example history states

These object diagrams show a small equipment workflow after each successful command is saved and
published. `u` denotes the UUID generated by the first add command. At startup, `s0` denotes the loaded
live baseline; it becomes a stored snapshot on the first successful mutation. From that point onward,
each `sN` is a complete stored record snapshot whose label shows only the relevant unit.
The highlighted stored snapshot has the same record values
as the live model; it is not the live `AddressBook` object. The integer `position` selects a snapshot,
so the dashed `states[position]` arrow is a derived selection, not a separately stored pointer field.

1. On launch, history `h0` has no snapshots or descriptions. The loaded record state is the baseline `s0`;
   this example assumes it contains no equipment. Undo and redo are unavailable.

<puml src="diagrams/UndoRedoState0.puml" alt="Startup with empty history and no recorded snapshots" />

2. Run `equipment add name/Camera category/Photo condition/GOOD`. After saving, `h1` retains `s0` and
   `s1`, with position 1 and one action description. A batch would still create only one history entry.

<puml src="diagrams/UndoRedoState1.puml" alt="First equipment addition records before and after snapshots" />

3. Run `equipment edit uuid/u condition/FAIR`, substituting the real UUID for `u`. History `h2` appends
   `s2` at position 2. The unit keeps its identity.

<puml src="diagrams/UndoRedoState2.puml" alt="Equipment edit appends a third snapshot with the same UUID" />

4. Run `undo`. New history `h3` selects `s1` at position 1; `s2` remains available for redo.

<puml src="diagrams/UndoRedoState3.puml" alt="Undo selects the preceding snapshot without discarding redo" />

5. Run `equipment list`. The directory opens, but the same history `h3`, position and redo branch remain.

<puml src="diagrams/UndoRedoState4.puml" alt="List navigation preserves the current immutable history" />

6. Run `redo`. New history `h4` selects the recorded `s2` again without executing the edit command.

<puml src="diagrams/UndoRedoState6.puml" alt="Redo selects the existing edited snapshot without replaying a command" />

7. Run `undo` again, creating `h5` at position 1, then `equipment edit uuid/u condition/DAMAGED`.
   Successful saving publishes `h6` with a new `s3`. It retains `s0` and `s1` and drops the old redo state
   `s2` and its corresponding action description. Redo is now unavailable. Failed saves publish none of this.

<puml src="diagrams/UndoRedoState5.puml" alt="A new successful edit after undo replaces the redo branch" />

#### Retention limit and command flow

After twelve successful record changes, only snapshots `s2` through `s12` and the ten actions between
them remain in the current history. The earliest reachable state is `s2`: ten undos reach position 0,
and ten redos can return to `s12`. These limits apply to reversible commands, not the number of records.
Older history values referenced transiently during a transaction can share stored snapshots; the bound
applies to the history retained by each model. A failed save cannot evict entries from the live history.

<puml src="diagrams/UndoRedoLimit.puml" alt="Eleven retained snapshots permit at most ten undo or redo steps" />

The activity diagram summarizes `LogicManager.execute()`. Public directory, details and utility commands
take the `isReadOnly()` path without saving records or changing history. Only successful transactional
saves publish candidate records and history. The nontransactional saving path remains for legacy
internal commands that do not declare themselves read-only.

<puml src="diagrams/CommitActivityDiagram.puml" alt="Parsing, staged execution, bounded history recording, atomic save and publication" />

#### Design considerations and verification

Complete snapshots keep linked member/equipment/loan data consistent and make batch restoration atomic.
They cost more memory than per-command inverse operations, so retention is capped at ten changes.
Inverse operations would require separate reversal logic for every command and batch failure path.

`AddressBookHistoryTest` covers the cap, branching, descriptions, snapshot isolation and invalid navigation.
Command/parser tests cover syntax and empty history. `UndoRedoIntegrationTest` verifies identity and loan
restoration, lifecycle changes, exact saved states, ten undos/redos, restart behavior and failed-save retries.
Shell tests exercise the real command box, preserved filters, missing details, loans and error feedback.
`ModelManagerTest` checks history availability inside synchronous collection listeners, unchanged-record
history transitions, and rollback when replacement records are invalid.

### \[Proposed\] Data archiving

_{Explain here how the data archiving feature will be implemented}_


--------------------------------------------------------------------------------------------------------------------

## **Documentation, logging, testing, dev-ops**

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [DevOps guide](DevOps.md)

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Requirements**

### Product scope

Laplace is a local, single-user desktop application for a CCA EXCO member to manage the CCA's members, equipment inventory, and equipment loans.

**Target user profile**:

* Is a CCA EXCO member responsible for maintaining member records and tracking equipment issued to members.
* Needs to distinguish individual equipment units, identify their current holders, and record issue and return dates.
* Maintains the records on their own computer; members are recorded entities, not additional application users.
* Can type quickly, prefers commands to repeated mouse interactions, and is comfortable learning a small command vocabulary.
* Wants a consolidated view of membership, inventory, and equipment responsibility during day-to-day club operations.

**Value proposition**: Laplace helps a CCA EXCO member keep member details, equipment records, and loans in one place. Commands support quick record entry and lookup, while linked member and equipment views show who holds each item. Availability checks prevent double assignments, reducing confusion and manual cross-checking during equipment issue and return.

**Requirements baseline and scope**:

These requirements consolidate the [team's project notes](https://docs.google.com/document/d/1XzYR2IpGc__mSZEy5GKMmVK8809j3Ln42VA8_9-va48/edit?usp=sharing): Week 4 (direction), Week 5 (all 41 user stories), and Week 6 (MVP and feature specifications). They describe intended behavior, including requirements beyond the MVP; they do not claim that every feature is implemented. Where the earlier schema differs from the later Feature Specifications, the latter defines the MVP baseline.

* **MVP baseline:** Add, list, view, and delete members and equipment; identify members by NUS ID and equipment by a generated UUID; issue and return individual items; show holdings and holders; validate input and preserve consistency on failure.
* **Broader requirements:** Editing, archiving, recovery, import/export, search and filtering, summaries, batch operations, condition recording at handover, and histories remain documented below even if they are not delivered this semester. Undo/redo is implemented with a ten-change session limit.
* **Deletion and retention:** Members and equipment are soft-deleted only without open loans. Records and closed history are retained; restoration has no time limit. This supersedes the original permanent-deletion and proposed 30-day recovery policies. The Loans tab and entity details show completed loan history.
* **Single-user boundary:** Import/export supports the same user's local records and backups. The earlier idea of handing a snapshot to another EXCO member is retained as a considered use of export, but is excluded from the course product's regular operations. Shared datasets, concurrent users, and member self-service are outside scope.

### User stories

Priorities: High (must have) — `* * *`; Medium (nice to have) — `* *`; Low (lower-priority candidate) — `*`.
Priority expresses product importance, not implementation status. **MVP** denotes the Week 6 baseline; **Future** denotes a requirement outside that baseline, without a delivery commitment. **Implemented** marks an addition now available beyond that baseline. US01–US41 retain the numbering of the Week 5 stories; US42–US45 make supporting requirements explicit.

| ID | Priority | Scope | As a … | I want to … | So that I can … |
|----|----------|-------|--------|-------------|----------------|
| US01 | `* *` | Future | newly appointed CCA EXCO member | import member and equipment records from a local file | avoid re-entering existing lists manually. |
| US02 | `* *` | Future | cautious CCA EXCO member | preview additions, changes, and rejected records before confirming an import | correct problems before changing my current records. |
| US03 | `* *` | Future | CCA EXCO member | export all Laplace data to one portable snapshot file | keep a complete backup of my records. |
| US04 | `* * *` | MVP | CCA EXCO member | add a member with their NUS ID, name, phone number, and email | keep track of a person who has joined the CCA. |
| US05 | `* * *` | MVP | CCA EXCO member | list all active members | review the current membership at a glance. |
| US06 | `* * *` | MVP | CCA EXCO member | view one member's full details | verify their information before an operation. |
| US07 | `* *` | Future | CCA EXCO member | edit a member's details while keeping their identifier stable | keep corrected or changed information accurate without breaking loan references. |
| US08 | `* *` | Future | CCA EXCO member | archive an inactive member | keep the active list relevant while retaining historical records. |
| US09 | `* * *` | MVP | CCA EXCO member | delete a member record created in error when it has no outstanding equipment | remove unwanted records without losing track of a current holder. |
| US10 | `* *` | Implemented | CCA EXCO member | restore a retained deleted member without a time limit | recover from an accidental deletion. |
| US11 | `* *` | Future | CCA EXCO member | filter and sort members by a chosen field | focus on the people relevant to my current task. |
| US12 | `* *` | Future | busy CCA EXCO member | find members using incomplete queries or approximate names | retrieve the right record quickly even when I do not know the exact identifier. |
| US13 | `* * *` | MVP | CCA EXCO member adding data | have duplicate NUS IDs rejected with an explanation | avoid conflicting records for the same member. |
| US14 | `* * *` | MVP | CCA EXCO member | add one equipment item with an automatically generated unique UUID | track each newly acquired physical unit separately. |
| US15 | `*` | Implemented | CCA EXCO member receiving a batch | add several units of the same equipment type in one operation | record a delivery without repeating the same entry process. |
| US16 | `* * *` | MVP | CCA EXCO member | list all active equipment and its availability | review the current inventory at a glance. |
| US17 | `* * *` | MVP | CCA EXCO member | view one equipment item's full details | verify its identity, condition, and recorded information. |
| US18 | `* *` | Implemented | CCA EXCO member | edit an equipment record while retaining its UUID | keep item information accurate without breaking loan references. |
| US19 | `* * *` | MVP | CCA EXCO member | delete an equipment record created in error when it is not on loan | remove unwanted entries without losing track of an issued item. |
| US20 | `* *` | Implemented | CCA EXCO member | restore a retained deleted equipment record without a time limit | recover from an accidental deletion. |
| US21 | `* *` | Future | CCA EXCO member | filter and sort equipment by category, condition, or availability | identify suitable items for my current task. |
| US22 | `* *` | Future | busy CCA EXCO member | find equipment using incomplete queries or approximate names | locate an item even when I do not know its UUID. |
| US23 | `* *` | Future | CCA EXCO member planning an activity | see counts of available, assigned, and unavailable units of an equipment type | decide whether there is enough equipment without counting manually. |
| US24 | `* * *` | MVP | CCA EXCO member issuing equipment | assign an available item to a member and record its assignment date | track who is responsible for the item and when it was issued. |
| US25 | `* * *` | MVP | CCA EXCO member issuing equipment | record an expected return date on or after the assignment date | know when the item should come back. |
| US26 | `* * *` | MVP | CCA EXCO member receiving equipment | record an assigned item as returned with its actual return date | keep the holder and availability information accurate. |
| US27 | `* * *` | MVP | CCA EXCO member | view all equipment currently held by one member | check that member's outstanding responsibilities in one place. |
| US28 | `* * *` | MVP | CCA EXCO member | see an assigned item's current holder and contact details | identify whom to approach when the item is needed. |
| US29 | `* * *` | MVP | CCA EXCO member issuing equipment | have assignments of already assigned, damaged, or under-repair items rejected | avoid recording multiple holders or issuing unsuitable equipment. |
| US30 | `*` | Future | CCA EXCO member distributing a kit | assign several items to one member in one operation | process a bundled issue quickly. |
| US31 | `*` | Future | CCA EXCO member receiving a kit | record several items from one member as returned in one operation | process a bundled return quickly. |
| US32 | `* *` | Future | CCA EXCO member issuing equipment | record an item's condition at issue | establish its starting condition for later comparison. |
| US33 | `* *` | Future | CCA EXCO member receiving equipment | record an item's condition at return | identify changes while the handover is still fresh. |
| US34 | `* *` | Future | CCA EXCO member investigating an item | view its past equipment loans | trace who held it and when. |
| US35 | `* *` | Future | CCA EXCO member reviewing a member | view that member's past equipment loans | resolve questions about previous loans without searching separate records. |
| US36 | `* *` | Future | CCA EXCO member archiving a member | be warned and prevented from archiving a member with outstanding equipment | resolve responsibility before removing the member from active records. |
| US37 | `* *` | Implemented | CCA EXCO member who made a mistake | undo my most recent successful data-changing action | recover without reconstructing the previous state manually. |
| US38 | `* *` | Implemented | CCA EXCO member | redo an action I just undid | recover when the undo itself was a mistake. |
| US39 | `*` | Future | CCA EXCO member investigating a discrepancy | view a recent history of data-changing actions | understand how the current records came about. |
| US40 | `* *` | Future | CCA EXCO member recovering from data loss | restore a previously exported complete snapshot | resume operations without rebuilding every record. |
| US41 | `* * *` | MVP | CCA EXCO member entering invalid data | receive an actionable error with no partial change applied | correct my input while trusting that the records remain consistent. |
| US42 | `* *` | Future | newly appointed CCA EXCO member | access command instructions and examples | learn operations and check syntax when I forget it. |
| US43 | `* * *` | MVP | CCA EXCO member | retrieve a member by their exact NUS ID | distinguish members even when they share a name. |
| US44 | `* * *` | MVP | CCA EXCO member | retrieve equipment by its exact UUID | distinguish physical units even when their descriptions match. |
| US45 | `* * *` | MVP | CCA EXCO member | have successful changes saved locally and loaded on the next launch | continue managing records across sessions without re-entering them. |

### Use cases

For all use cases, the **system** is Laplace and the **actor** is the CCA EXCO member operating their local installation. The application is running with a valid dataset unless stated otherwise. The member borrowing equipment is not a second system actor. **MSS** means main success scenario. Extension labels refer to the MSS step at which the alternative occurs; each extension states where execution resumes or ends.

For every data-changing use case, a validation failure leaves all records unchanged. If saving fails at the final update step, Laplace reports that the operation was not saved and retains the preceding consistent state; the use case ends unsuccessfully. Success is reported only after persistence succeeds. These rules also apply to proposed future operations.

#### UC01: Register a member

**Scope:** MVP. **Related stories:** US04, US13, US41, US45.

**MSS**

1. The EXCO member provides the new member's NUS ID, name, phone number, and email.
2. Laplace validates the details and checks that the normalized NUS ID is not already registered.
3. Laplace saves the member and shows the stored details.

Use case ends.

**Extensions**

* 2a. Required details are missing or invalid.
    * 2a1. Laplace identifies the invalid input and explains the accepted format.
    * Use case resumes at step 1.
* 2b. The NUS ID already exists, ignoring letter case.
    * 2b1. Laplace rejects the duplicate and identifies the existing member.
    * Use case resumes at step 1.

Names, phone numbers, or email addresses shared by different NUS IDs do not by themselves constitute duplicates.

#### UC02: Register physical equipment units

**Scope:** Implemented. **Related stories:** US14, US15, US17, US41, US45.

**MSS**

1. The EXCO member provides the units' name, category, condition, optional notes, and optional count.
2. Laplace validates the details and count (default 1, range 1-100), then reserves a distinct UUID per unit.
3. Laplace saves all units in one transaction and reports their UUIDs and derived availability.
   One unit opens details; a batch opens the unfiltered equipment directory.

Use case ends.

**Extensions**

* 2a. Required details are missing or invalid.
    * 2a1. Laplace explains what needs correcting.
    * Use case resumes at step 1.
* 2b. Laplace cannot generate a unique identifier.
    * 2b1. Laplace reports the failure without adding any units.
    * Use case ends.

Matching descriptions are permitted: each physical unit receives its own UUID. An item in `DAMAGED` or `UNDER_REPAIR` condition may be registered but is unavailable for issue.

#### UC03: Issue equipment to a member

**Scope:** MVP. **Related stories:** US06, US16, US24, US25, US27–US29, US43, US44.

**MSS**

1. The EXCO member requests the intended member's details by NUS ID and the equipment inventory.
2. Laplace shows the member's details and current holdings, and the equipment with its availability and UUIDs.
3. The EXCO member requests assignment of one item to that member, supplying the assignment and expected return dates.
4. Laplace validates the references, availability, and dates, then saves an open loan and shows the updated holder and holdings.

Use case ends.

**Extensions**

* 2a. The member identifier is invalid or no matching member exists.
    * 2a1. Laplace explains the problem.
    * Use case resumes at step 1.
* 2b. No equipment is available for issue.
    * 2b1. Laplace shows that no suitable item is available.
    * Use case ends.
* 4a. A supplied identifier is invalid or does not identify an existing record.
    * 4a1. Laplace identifies the invalid or missing reference.
    * Use case resumes at step 3.
* 4b. The item already has an open loan, or its condition is `DAMAGED` or `UNDER_REPAIR`.
    * 4b1. Laplace refuses the assignment and gives the reason, including the current holder when applicable.
    * Use case resumes at step 3.
* 4c. A date is invalid, the assignment date is after today, or the expected return date precedes assignment.
    * 4c1. Laplace explains the violated date rule.
    * Use case resumes at step 3.

#### UC04: Record an equipment return

**Scope:** MVP. **Related stories:** US17, US26–US28, US41, US44.

**MSS**

1. The EXCO member requests an item's details by UUID.
2. Laplace shows the item, current holder, assignment date, and expected return date.
3. The EXCO member requests that the item be recorded as returned and supplies the actual return date.
4. Laplace validates and saves the return, closes the loan, removes the current holder, and updates availability and the member's holdings.

Use case ends.

**Extensions**

* 2a. The UUID is invalid or no item matches it.
    * 2a1. Laplace explains the problem.
    * Use case resumes at step 1.
* 2b. The item has no open loan.
    * 2b1. Laplace reports that the item is not currently assigned.
    * Use case ends.
* 4a. The supplied item reference is invalid, missing, or has no open loan, including an already recorded return.
    * 4a1. Laplace rejects the request and explains the problem.
    * Use case resumes at step 1.
* 4b. The actual return date is invalid, before assignment, or after today.
    * 4b1. Laplace explains the allowed date range.
    * Use case resumes at step 3.

Passing the expected return date never closes a loan automatically. A return after the expected date is valid. After return, only items in `GOOD` or `FAIR` condition become available.

#### UC05: Delete an erroneous member or equipment record

**Scope:** MVP. **Related stories:** US05, US09, US16, US19, US41.

**MSS**

1. The EXCO member requests the member list or equipment list, according to the kind of record to remove.
2. Laplace shows the requested records and their identifiers.
3. The EXCO member requests deletion of one record using its NUS ID or UUID, respectively.
4. Laplace verifies that the record has no open loans, marks it deleted while retaining its details and closed loans, and confirms the deletion.

Use case ends.

**Extensions**

* 2a. The requested list is empty.
    * Use case ends.
* 4a. The identifier is invalid or no matching record exists.
    * 4a1. Laplace reports the problem without deleting anything.
    * Use case resumes at step 3.
* 4b. The member still holds equipment, or the equipment item is currently assigned.
    * 4b1. Laplace refuses deletion and explains that the outstanding items must first be returned.
    * Use case ends without deletion. The EXCO member may complete UC04 and start UC05 again.

Already-deleted records are rejected without a save. To recover a record, find it with a `status/deleted`
search, then issue `member restore nus/NUS_ID` or `equipment restore uuid/UUID`. Laplace clears the
flag, saves and opens its details. Unknown/already-active records fail without changes. Recovery has no
time limit; equipment condition is preserved. Closed history remains retained throughout.

#### UC05a: Edit equipment details

**Scope:** Implemented. **Related story:** US18.

1. The EXCO member views a physical unit and supplies its UUID with one or more edited details.
2. Laplace validates the fields and requires an active unit and an actual change.
3. Laplace replaces the details under the same UUID, saves and displays the updated unit.

Invalid fields, unknown UUIDs, deleted units and no-change edits leave the dataset unchanged. An open
loan permits details/condition edits and retains its reference; save failure preserves the previous state.

#### UC06: Import records after reviewing a preview

**Scope:** Future. **Related stories:** US01, US02, US13, US41.

**MSS**

1. The EXCO member requests import from a local member and equipment data file.
2. Laplace reads and validates the file, showing a preview of records to add, change, or reject without altering current data.
3. The EXCO member reviews a preview with no unresolved errors and confirms the import.
4. Laplace applies and saves the previewed changes as one operation and reports the result.

Use case ends.

**Extensions**

* 2a. The file is missing, unreadable, or in an unsupported format.
    * 2a1. Laplace explains why it cannot produce a preview.
    * Use case resumes at step 1.
* 2b. Records are invalid, identifiers conflict, or the proposed changes would break loan references.
    * 2b1. Laplace identifies the affected records and reasons and prevents confirmation until they are resolved.
    * Use case resumes at step 1 after the EXCO member corrects or chooses another file.
* 3a. The EXCO member cancels.
    * Use case ends with current data unchanged.
* 4a. The source file or current dataset has changed since the preview.
    * 4a1. Laplace invalidates the old preview without applying it.
    * Use case resumes at step 2.

#### UC07: Restore a complete backup

**Scope:** Future. **Related stories:** US03, US40, US41.
**Precondition:** The EXCO member has a previously exported complete snapshot. Laplace can accept a restore request even if its normal data file cannot be loaded.

**MSS**

1. The EXCO member selects a local snapshot for restoration.
2. Laplace validates the complete snapshot and explains that restoration will replace the current dataset.
3. The EXCO member confirms replacement.
4. Laplace restores and saves the snapshot as one operation, then shows the restored records.

Use case ends.

**Extensions**

* 2a. The snapshot is unreadable, incompatible, incomplete, or contains invalid records or broken loan references.
    * 2a1. Laplace rejects it and explains the problem without replacing current data.
    * Use case resumes at step 1.
* 3a. The EXCO member cancels replacement.
    * Use case ends with current data unchanged.
* 4a. The snapshot has changed since validation.
    * 4a1. Laplace discards the previous validation result without replacing current data.
    * Use case resumes at step 2.

#### UC08: Undo a mistaken change

**Scope:** Implemented for up to ten changes per session. **Related stories:** US37, US38, US41.

**MSS**

1. The EXCO member requests undo of the most recent successful data-changing action.
2. Laplace restores and saves the preceding consistent state, including linked records, and identifies the undone action.
3. The EXCO member inspects the restored records.

Use case ends.

**Extensions**

* 2a. There is no action available to undo.
    * 2a1. Laplace explains that no undo is available and leaves the data unchanged.
    * Use case ends.
* 3a. The EXCO member decides the undo was a mistake and requests redo before any new data-changing action.
    * 3a1. Laplace reapplies and saves the undone action and shows the resulting records.
    * Use case ends.
* 3b. The EXCO member requests redo after a new successful data-changing action has invalidated it.
    * 3b1. Laplace explains that no redo is available and leaves the current data unchanged.
    * Use case ends.

A new successful data-changing action after undo invalidates redo. Read-only actions and failed commands do not add undo entries or invalidate redo. History retains at most ten changes per session, including the deleted records covered by those changes. Older changes are discarded; restarting clears undo and redo history.

### Non-Functional Requirements

These are acceptance targets for the intended product, not measurements of the current implementation. Product constraints are adapted from the [course project constraints](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-constraints.html); development-process requirements such as incremental delivery are not product NFRs.

1. **NFR01 — Platform compatibility:** The release shall launch and support its documented operations on Windows, Linux, and macOS with Java 25 as the only installed Java version.
2. **NFR02 — Portable distribution:** The application shall run from a single downloadable JAR without an installer or separate installation of dependencies other than Java. The JAR shall not exceed 100 MB.
3. **NFR03 — Local operation:** Member, equipment, loan, and local backup operations shall work without a network connection, remote server, or external account. Each dataset belongs to one local operator; shared or concurrent access is unsupported.
4. **NFR04 — Editable storage:** Persistent records shall use local, human-editable text files without a DBMS. Valid edits made while the application is closed shall load on restart; invalid files shall produce a diagnostic rather than be silently accepted.
5. **NFR05 — Keyboard operation:** Every documented record-management operation shall be completable with keyboard input alone. Users shall not need mouse actions to submit commands, inspect results, or correct input.
6. **NFR06 — Display compatibility:** At 1920×1080 with 100% or 125% scaling, essential controls and feedback shall not overlap or be clipped. At 1280×720 with 150% scaling, every operation and its result shall remain accessible, using scrolling where necessary.
7. **NFR07 — Capacity and response time:** With 1,000 members, 5,000 equipment items, and 10,000 loans on a computer with a four-core CPU, 8 GB RAM, and an SSD, at least 95 of 100 executions of each core add, list, exact lookup, delete, issue, and return operation shall display a result within two seconds of submission. Measure after startup, with valid data and no competing intensive workload; include local saving for mutations. Startup with the same dataset shall complete within ten seconds.
8. **NFR08 — Atomicity and durability:** After a successful mutation and normal shutdown, restarting shall recover the same records and loan relationships. Validation or reported save failures shall not leave partially updated member, equipment, or loan data. A failed file replacement shall preserve the last valid saved dataset. The same guarantee applies to batch and undo/redo operations as a whole, and to future import/restore operations.
9. **NFR09 — Consistent feedback:** Every rejected command shall identify the first detected problem and provide an accepted format or corrective action. Input errors shall not terminate the application, and the submitted command shall remain available for correction. Success and error messages shall be distinguishable by text, without relying on color alone.
10. **NFR10 — Data confidentiality:** Laplace shall not transmit member details or inventory records over the network. Diagnostic logs shall omit member names, phone numbers, email addresses, and raw commands containing those values. Local text data and user-created exports rely on the operating system's file access controls; application-level encryption is not promised.

### Glossary

| Term | Definition |
|------|------------|
| CCA | Co-Curricular Activity; the club or student organization whose records the operator manages. |
| EXCO member / operator | A member of the CCA's executive committee who operates their own Laplace installation. |
| Member record | The stored identity and contact details of a person in the CCA. Being recorded does not give that person application access. |
| NUS ID | A member's unique, immutable identifier in Laplace. The MVP accepts one ASCII letter, seven digits, and an optional final ASCII letter; comparison ignores case and storage uses uppercase. |
| Equipment item / unit | One individually tracked physical object. Identical models are separate items, each with its own identifier. |
| UUID | Universally unique identifier. Laplace generates one immutable UUID for each equipment item; names and descriptions are not equipment identifiers. |
| Category | A broad grouping recorded on an equipment item, such as cameras or sports equipment. One category can contain different equipment types. |
| Equipment type | A particular kind or model of equipment, such as a Canon EOS R50 camera. Several physical units can share a type; the future batch and summary stories refer to these units, rather than every item in a broad category. |
| Condition | The recorded physical state of an item: `GOOD`, `FAIR`, `DAMAGED`, or `UNDER_REPAIR` in the MVP. |
| Availability | A derived state: `ASSIGNED` when an open loan exists; otherwise `AVAILABLE` for `GOOD` or `FAIR` condition, and `UNAVAILABLE` for `DAMAGED` or `UNDER_REPAIR`. It is not entered independently. |
| Equipment loan / assignment | A relationship between one member and one equipment item, with assignment, expected return, and optional actual return dates. A member may have several loans; an item has at most one open loan. |
| Open loan / outstanding equipment | A loan with no actual return date, or the item covered by that loan. The associated member remains responsible until return is recorded. |
| Closed loan | A loan whose actual return date has been recorded. It no longer determines a current holder. |
| Holder | The member referenced by an item's open loan. An item without an open loan has no current holder. |
| Assignment date | The date the item was issued; it must be a real calendar date no later than the computer's local date. |
| Expected return date | The due date, on or after assignment. Reaching it does not automatically record a return. |
| Actual return date | The recorded date of return, from the assignment date through the computer's local date, inclusive. |
| Overdue loan | An open loan whose expected return date is before the computer's local date. This definition does not imply automatic notifications. |
| Active record | A member or equipment record included in normal lists, rather than archived or deleted. Active equipment can still be assigned or unavailable. |
| Archive | A proposed operation that removes an inactive member from the active list while preserving their record and history. |
| Delete | Marking a retained record deleted, allowed only without open loans. Its identity, details and closed history remain stored. |
| Restore | Reactivating a retained deleted member or equipment unit without a time limit, preserving identity and history. |
| Snapshot | A complete local export of Laplace's persistent records and their relationships at a point in time, used as a backup. It differs from importing selected member or equipment records. |
| Fuzzy search | Proposed matching of incomplete or approximate text against records; distinct from the MVP's exact identifier lookup. |
| Atomic operation | An operation whose changes succeed together or leave the preceding consistent state intact. |
| MVP | Minimum viable product; the Week 6 baseline, which is a subset of the full requirements recorded here. |

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<box type="info" seamless>

**Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.
</box>

### Member email validation

In a disposable dataset, try `member add nus/A8000099X name/Kelvin Probe phone/91234567 email/K@example.com`
and `member edit nus/A0123456X email/K.updated@example.com` for an existing member. Both must reject
the non-ASCII email without changing records or the data file. An uppercase ASCII email must save
lowercase and retain the member identity and loan references after restart.

### Launch and shutdown

1. Initial launch

    1. Download the JAR file and copy it into an empty folder.

    1. In that folder, run `java -jar Laplace.jar` with Java 25.<br>
       Expected: The GUI opens in light mode with sample members, grouped equipment and open loans.

1. Invalid saved data

    1. In a disposable JAR folder, save a synthetic dataset containing members and closed loans.
       Close Laplace and keep a byte-for-byte backup.

    1. Test a malformed file, a JSON `null` root, an invalid member email, and a valid object followed
       by another object or garbage. Relaunch for each case.<br>
       Expected: A recovery error names the data file and explains preservation, backup and repair.
       After acknowledgment, startup terminates unsuccessfully without an editable main window.
       The original data bytes remain unchanged, including after shutdown.

    1. Restore the valid backup and restart.<br>
       Expected: All members and retained loan history load. Ordinary trailing whitespace is accepted.
       A genuinely missing data file still starts the existing sample dataset.

1. Saving window preferences

    1. Resize the window to an optimal size. Move the window to a different location. Close the window.

    1. Relaunch with `java -jar Laplace.jar`.<br>
       Expected: The most recent window size and location are retained.

1. Recovering invalid window preferences

    1. In a disposable JAR folder, close Laplace and back up `data/addressbook.json`.
       Replace `preferences.json` with `{"guiSettings":null}`.

    1. Relaunch with `java -jar Laplace.jar`.<br>
       Expected: The GUI opens with default light appearance and window geometry. The log reports
       that preferences could not be loaded, and `preferences.json` contains default settings.
       `data/addressbook.json` remains unchanged.

    1. Repeat with `{}` in `preferences.json`.<br>
       Expected: Missing settings continue to use defaults. Valid saved geometry and dark mode
       still restore when present.

**Architecture-specific packaging:** Download `Laplace.jar` for Windows, Linux or Intel macOS, or
`Laplace-mac-arm64.jar` for Apple Silicon macOS. Substitute the selected filename in launch commands.
Both are self-contained; macOS native libraries cannot be combined by ordinary JAR merging because
their resource names are identical. Build with `./gradlew shadowJar -PjavafxMacPlatform=mac` or
`./gradlew shadowJar -PjavafxMacPlatform=mac-aarch64`, respectively. The default follows the build
machine's processor architecture. JavaFX versions and application behavior are identical in both builds.
The packaged keyboard probe uses only the JAR's production classes and native libraries. CI uses a
plain JDK, so neither matching test dependencies nor a JavaFX-enabled JDK can mask missing native libraries.
Run the probe on the target architecture and smoke-test the selected JAR before publication.

### Unicode command separators

With disposable data, replace each space in `member view nus/A0123456X` with an EM SPACE (U+2003)
or nonbreaking space (U+00A0), including surrounding whitespace. The same existing member must open.
Repeat with `member list`, `equipment list` and `loans list`. Unknown actions, uppercase command words,
duplicate parameters and unknown parameters must retain their normal diagnostics.

### Grouped equipment, command assistance and appearance

Historical assignment regression: assign an available UUID on `2026-01-10`, return it on
`2026-01-20`, then try assigning it to a second member on `2026-01-15`. Expect rejection with the
last actual return date and unchanged records, file and undo/redo history. Repeat with a batch whose
last UUID conflicts; no earlier UUID may be assigned. A new assignment on `2026-01-20` is allowed.
`LoanAddCommand` validates each new open interval against retained actual returns before applying any
batch. Existing stored history remains readable; this change adds no storage migration or repair.

Use a separate empty writable JAR folder. Keep any real dataset untouched.

Large-history regression: use 1,000 members, 5,000 equipment units and 10,000 completed loans,
including a member/unit with a long history. Open both details views and Loans, scroll within the
history list to its oldest entry, then filter Loans to no matches and clear the filter. Expect full
counts and accessible records without creating every row's controls. `LoanHistoryCapacityTest`
checks this capacity structurally; it does not replace the 100-trial NFR07 timing benchmark.
`LoanRows` renders up to ten rows inline and uses bounded JavaFX `ListView` cells above that size
for both detail timelines and loan-directory sections, including panels refreshed in the background.

1. Run `equipment add name/UI Camera category/Photo condition/GOOD count/4`. Expect one group containing
   four distinct UUIDs. Expand/copy Last result to capture all four. Open the group and select one unit;
   Copy UUID must copy its complete UUID. Register another matching unit; expect five units in the same group.
2. Edit one UUID with `equipment edit uuid/UUID condition/DAMAGED`, then search with
   `equipment search availability/available`. The group must show matching versus retained counts;
   Show all units reveals the damaged sibling. Directly view that UUID and check exact selection.
   Uncheck Show all units: the table narrows again, the exact unit details remain visible, and a message
   explains that the selected unit is outside the table filter.
   With enough groups and units to scroll, leave the directory and unit table at the top, then directly
   view a UUID near the end. Both selected rows must scroll into view. Repeat after clicking a table
   heading to sort: the sort and search filters must remain. Scroll away from the selected rows and
   run the same view command again; both rows must reappear. A background refresh without navigation
   must preserve browsing position and must not execute another command.
3. Rename the damaged unit, delete it, then restore it with the documented equipment commands. Expect
   regrouping without UUID changes. Check deleted records never inflate available counts.
   Open a unit with long notes/history and scroll to the bottom. View a different UUID; expect the group
   heading and unit identity at the top. Refresh or reopen the same UUID; expect its scroll position retained.
4. Open a member's details, copy contact fields, and use a related equipment link. Switch tabs and use
   Back to results. The previous filter persists until Reset search or a list command.
5. Use a command preview. Expect only input preparation; review and press Enter. Test unmodified
   history up/down with an unfinished draft. Shift/Ctrl/Alt/Meta+Up/Down should retain normal text editing
   without recalling history. Test Ctrl+Space completion, Escape dismissal and keyboard focus navigation.
6. Open Loans. Verify due/overdue labels against the computer's local date and the stored due date.
   Prepare a return; verify the UUID/date and that the loan remains open until submission.
7. Run `mode dark`, open Help, then switch back using the header appearance button. Repeat list/table selection, previews and errors
   in each appearance. Exit normally and relaunch; the selected mode persists. `mode sepia` is rejected.
8. Review at 1920×1080 with 100%/125% scaling and 1280×720 with 150% scaling on supported systems.
   Check wrapping, scrolling, visible focus and contrast. Logical-size snapshots alone do not verify OS scaling.

### Retained inventory totals during filtering

In a separate empty dataset, add two GOOD units named `Mixed` in category `Photo`, and one GOOD unit
named `Retired` in category `Legacy`. Delete one `Mixed` unit and the only `Retired` unit by UUID.
Do not create loans. Expected: three retained units, one active available unit, and two deleted units.

Run `equipment search status/active`, `equipment search status/deleted`, and `equipment list`.
The Displayed count must be one, two and three respectively. Throughout, the right-hand summary must
retain both groups: `Photo` / `Mixed` has one available and one deleted unit; `Legacy` / `Retired` has
zero active availability counts and one deleted unit. Equipment Total stays three and overall active
availability stays one available, zero assigned and zero unavailable. Filters change matching units,
not retained totals. Check the UG and linked search guide describe this same distinction.

### Tab completion across current commands

Capacity regression: with 10,000 valid retained loans, complete `member ` with Tab and cycle forward
and backward. Expect no history-dependent pause. Repeat a record-name completion, edit or restore a
record, undo/redo, and reload; expect current values. Structural controller tests reject person/loan
collection reads, assert zero static-model reads and reuse of prepared record candidates. Measure
responsiveness separately; structural checks do not claim a cross-platform timing benchmark.

Add `member add nus/A8400001X name/ΝΙΚΟΣ phone/91234567 email/nikos@example.com` in a disposable folder.
Search with `member search q/Σ` and `member search q/νικοσ`: both must find the member before and after
restart. Type `member edit name/νικοσ` and press Tab: it must suggest `ΝΙΚΟΣ` without changing or saving
the record. Existing forward/reverse Tab cycling must continue to work.

1. In a disposable dataset, type `mem` and press Tab. Expected: `member`, with the caret at its end.
   Add a space and cycle with Tab, then Shift+Tab. Expected: alphabetical subcommands and reverse cycling.
2. Try `member restore n`, `equipment edit co`, `equipment add cou`, and `loans return co`, pressing
   Tab after each. Expected: `nus/`, `condition/`, `count/`, and `condition/` respectively.
3. Try `member search holding/y`, `equipment search availability/av` and `equipment search order/de`.
   Expected: `YES`, `AVAILABLE` and `desc`. Enter the completed search; check the filtered directory.
4. In `loans return uuid/FIRST_UUID,  PARTIAL_SECOND_UUID returned/DATE`, position the caret after the
   partial UUID and press Tab. Expected: only that UUID is replaced; the first UUID, spacing and return
   date remain intact. Candidates omit UUIDs already present. Repeat with a space after the comma.
5. Repeat a name completion with an em space (`U+2003`) or nonbreaking space (`U+00A0`) before the
   following `phone/` field. Expected: the phone field and original separator remain, with the caret
   immediately after the completed name. Check the same behavior in a comma-separated UUID list.
6. Check an untouched following field, no matching input, selected text and Shift+Tab outside a cycle.
   Expected: surrounding input remains intact and ordinary focus traversal remains available.
7. Add or edit a record, then retry completion. Expected: current values are suggested. Tab alone never
   changes records or saves; Enter uses normal parser validation. Restart after a valid mutation and
   verify the saved records. New IDs, dates, numeric counts and notes remain manually entered.
8. With an existing member named `Alex Tan`, type `member edit name/Alex  T phone/91234567`
   using two spaces between `Alex` and `T`. Place the caret after `T` and press Tab.
   Expected: `name/Alex Tan`, retaining the following phone field and its separator. Repeat with
   an em space (`U+2003`) and nonbreaking space (`U+00A0`) inside the partial name. The caret ends
   immediately after the stored name; Tab does not execute the command or rewrite surrounding input.
9. Register equipment named `Camera Cover` in category `Photo Cover`. Type
   `equipment add name/Camera co category/Photo`, place the caret after `co`, and press Tab.
   Expected: `name/Camera Cover`, with the following category field unchanged and no command executed.
   Repeat with `equipment add category/Photo co`; expect `category/Photo Cover`. Check `Camera Case`
   and `Camera Notes` too. When no stored value matches, an unfinished token such as `notes/Keep co`
   still offers unused field prefixes; a trailing space starts a new field.
10. After one or several valid comma-separated UUIDs, complete `loans add uuid/UUID nu`,
    `loans return uuid/UUID re`, and `loans return uuid/UUID co`. Expect `nus/`, `returned/` and
    `condition/` without changing the UUID list or later fields. Recheck UUID completion immediately
    after a comma; it must still offer unused UUIDs rather than a new parameter.
11. Type `loans return co` and press Ctrl+Space. Expected: `loans return condition/` is offered.
    Repeat after an existing `uuid/UUID` field; that field remains unchanged. After a used
    `condition/GOOD` field, Ctrl+Space must not offer `condition/` again. Compare with Tab and the
    loan-return template in Help; both expose the same optional return condition. Preparing a template
    or accepting a suggestion must not execute a return.

### Rejection of retired commands

1. Run the JAR from a separate empty writable folder to get sample members, equipment and loans.
   Use only this temporary dataset for these checks. Run `member list`, `equipment list`, and
   `loans list`; record the identities and counts. Copy the saved data file for comparison if present.
2. Enter each of `add`, `edit 1 name/Changed`, `delete 1`, `clear`, `clear 3`, `find Alex`, `list`,
   and `list members`. Expected: `Unknown command.`; the current view/selection and all records stay
   unchanged. An existing data file must remain byte-for-byte unchanged; rejected input must not
   create a data file if none existed.
3. Run `member list`, `equipment list`, and `loans list` again. Expected: normal directory navigation
   and the same records and loan identities as before. These supported controls are separate from
   the rejected-input file comparison above.
4. Restart the application. Expected: saved records still load normally. Existing JSON files with a
   `persons` array remain readable; no migration or removal of that array is required by this change.

### Unicode member names

1. In disposable data, add members named `அருண்`, `Anne O’Connor`, and `José Tan` (the `e` followed
   by combining acute accent U+0301), using a different valid NUS ID for each. Expect success; the last
   name is stored/displayed as NFC `José Tan`. Ordinary names such as `Anne-Marie O'Connor` still work.
2. Give a member an open loan, then use `member edit nus/NUS_ID name/அருண்` and another valid Unicode
   name. Expect the same NUS ID, contacts and loan references after each edit and after restarting.
3. Try a leading combining mark, a mark after a space, a punctuation-only name and a name longer than
   100 code points after normalization. Expect an error without saved-data changes. Equivalent names
   at the limit must be counted after NFC composition, not by UTF-16 units or the raw pasted length.
4. Load an existing file with a decomposed accented member name and repeated spaces. Expect exact saved
   spelling to remain when loading, saving, and editing only the phone/email. New name input uses NFC.

### Adding a batch of equipment

1. In a separate temporary dataset, run
   `equipment add name/Batch Test Camera category/Photography condition/GOOD count/3 notes/Shared kit`.
   Expected: three new rows with distinct UUIDs, AVAILABLE status and identical supplied details;
   feedback lists all three UUIDs and the full equipment directory opens.
2. Repeat without `count/`, then with `count/1`. Each command adds one unit and opens its details.
3. Try `count/0`, `count/-1`, `count/101`, `count/1.5`, a blank `count/`, and `count/2 count/3`.
   Expected: an error, no new records, and no save. Correct the input and retry.
4. Run `equipment search q/NoMatchingBatch`, then add a valid batch. Expected: the old filter resets
   and the directory includes the new units. Repeating a batch retains distinct UUIDs and warns about
   pre-existing matching units, not the new units within the same batch.
5. Exit and relaunch from the same folder. Expected: all units retain their original UUIDs and details.
   Use `equipment view uuid/UUID` with each actual generated UUID to verify independently.

### Editing, deleting and restoring equipment

1. Add two matching units with `equipment add name/Maintenance Camera category/Photography condition/GOOD count/2`.
   Record both UUIDs as A and B. Replace these placeholders in all following commands.
2. Run `equipment edit uuid/A name/Updated Camera condition/FAIR notes/Checked` then `equipment edit uuid/A notes/`.
   Expect A's UUID unchanged, updated details and cleared notes; B remains unchanged. Restart and verify.
3. Try `equipment edit uuid/A`, `equipment edit uuid/A name/`, and a no-change edit. Expect an error and no save.
4. Assign A to an active test member with valid dates. Edit its condition to DAMAGED: expect ASSIGNED until
   return, then UNAVAILABLE. Attempt deletion while assigned: expect refusal and no record/history changes.
5. Return A, then `equipment delete uuid/A`. Expect a retained `[DELETED]` row in `equipment list`, no new
   loans permitted, and exclusion from active search/inventory totals. `equipment search status/deleted`
   should find it. Restart: the flag, UUID, details and closed loan records must remain in the saved data.
6. Try editing/deleting A again: expect rejection. Run `equipment restore uuid/A`: expect the same UUID
   and details, still DAMAGED/UNAVAILABLE. Repeating restore is rejected. Repair with an edit to GOOD and
   verify that assigning a new loan succeeds. B must remain unchanged throughout.
7. Check `equipment search status/all`, direct UUID navigation and query refresh after rename/restore.
   Automated transaction tests separately inject partial writes/failed atomic moves; do not simulate
   filesystem failures against a real user dataset.

### Stored loan date validation

In a disposable saved file, change each loan date field in turn to `0000-01-01`, `-0001-01-01`,
or `+10000-01-01`. Expect loading to report invalid data without rewriting that file. Valid years
`0001` and `9999` and valid legacy condition omissions remain supported; storage does not impose a
new restriction relative to today's date.

### Saving data

1. Command-log privacy: in a disposable folder, add/edit a synthetic member, search for their name,
   and submit a malformed command containing a synthetic phone/email. Inspect `addressbook.log.*`;
   expect input-received diagnostics without raw commands or contact values. `CommandLogPrivacyTest`
   also verifies persisted output with the application logger hierarchy enabled at all levels,
   including transaction snapshots and model initialization from reloaded records. Initialization logs
   report the event without printing record or preference contents.

1. Read-only navigation and utilities

    1. Use a separate writable JAR folder. Add one equipment unit and record its UUID so the sample
       dataset is saved. Close Laplace and record the data file's bytes and modification time.
    2. Relaunch. Run `member list`, `equipment list`, `loans list`,
       `member view nus/A0123456X`, and `equipment view uuid/UUID`, using the recorded UUID.
       Expected: each directory/details view opens and the assigned sample units retain their holders.
    3. Run `help`, close Help, and run `exit`. Expected: Help opens and normal exit closes the application.
       The data file's bytes and modification time remain unchanged. Window preferences may be saved.
    4. Repeat list/view/Help/Exit in another empty JAR folder without performing a mutation.
       Expected: sample records are browsable, but no `data/addressbook.json` is created.
    5. Automated `ReadOnlyCommandIntegrationTest` and `MainWindowShellTest` inject record-save failures
       to check navigation, Help and Exit remain available. They also check unchanged records and redo
       history, and that real mutations still save. Do not change permissions on a real dataset.

1. Dealing with missing/corrupted data files

    1. _{Explain how to simulate missing or corrupted data files and state the expected behavior.}_

1. _{ more test cases … }_

### Undo and redo

Use temporary data. Do not test against a real inventory.

1. Add a member, then enter `undo`. Expect `Undid: member add.`, removal of that member and a refreshed
   directory. Enter `redo`; expect `Redid: member add.` and the identical member restored.
2. Register three units with one `equipment add ... count/3` command. Capture all UUIDs. One undo must
   remove all three; one redo must restore exactly those UUIDs.
3. Issue and return a unit with a recorded return condition. Undo the return: expect the original open
   loan, issue condition and holder restored. Redo: expect the recorded return date and condition.
4. Make twelve successful changes. Only ten undos are available; the next reports `Nothing to undo.`.
   Redo ten times; the next reports `Nothing to redo.`.
5. Undo, run queries and `mode dark`, then redo. It must remain available and appearance must stay dark.
   Undo again and make a new successful mutation; redo must now be unavailable. Rejected/no-change
   edits must leave redo available. `undo 2` and `redo count/2` must be rejected without changing data.
6. Restart after an undo. The restored records must still load, but undo and redo must both be unavailable.
   Automated tests inject failed saves and verify records, history and prior file bytes survive for retry.
