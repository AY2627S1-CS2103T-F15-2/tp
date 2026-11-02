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
* [JavaFX (OpenJFX)](https://openjfx.io/) 17.0.7 provides the GUI. JavaFX libraries and native binaries for Windows, Linux, Intel macOS and Apple Silicon macOS are bundled in `Laplace.jar`, under [GPLv2 with the Classpath Exception](https://github.com/openjdk/jfx/blob/jfx17/LICENSE).
* [Jackson](https://github.com/FasterXML/jackson) provides JSON serialization and deserialization through `jackson-databind` and `jackson-datatype-jsr310`, both resolved to 2.7.4 at runtime. These runtime libraries are bundled in `Laplace.jar` under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).
* [JUnit 5](https://docs.junit.org/5.14.4/overview.html) (Jupiter 5.14.4) provides automated testing under the [Eclipse Public License 2.0](https://github.com/junit-team/junit-framework/blob/r5.14.4/LICENSE.md). It is a test dependency and is not bundled in `Laplace.jar`.
* Universal macOS packaging follows Apple's [Mach-O fat binary format](https://github.com/apple-oss-distributions/cctools/blob/main/include/mach-o/fat.h); the wrapper preserves the upstream JavaFX library slices.
* Command text uses [IBM Plex Mono](https://github.com/IBM/plex) 2.004, copyright IBM Corp., under the SIL Open Font License. The faces and license are in `src/main/resources/fonts`.

### Use of AI tools

Notable uses of AI in the team's project work:

* OpenAI Codex and Anthropic Claude were used for preliminary pull request reviews to identify
  potential defects and suggest improvements.
* @Keegan4 used OpenAI Codex and Claude Code to draft and revise member invariant assertions and
  regression tests, and to improve Java naming and Javadoc. They also used these tools to edit User
  Guide and Developer Guide prose so that the guides match shipped behavior, and to create and update
  PlantUML diagrams and their UML labels.
  They followed AI-suggested workflow steps for planning issues, branching, preparing pull requests
  and running checks before submission. AI also helped them craft commit messages and pull request
  descriptions, prepare PR review feedback, and run automated tests and documentation builds.
  All AI-assisted output was reviewed and edited before submission.
* @ArjoDas used OpenAI Codex and Claude Code to help implement and test the equipment features
  (UUID-unique equipment list, derived availability, equipment add including batch creation, and
  equipment maintenance), the retirement of legacy AddressBook commands, and the grouped equipment
  directory and other UI improvements. They were also used to draft User Guide and Developer Guide
  sections, PlantUML class and activity diagrams and manual testing steps, to draft PR review comments,
  and to maintain the contributor instructions in AGENTS.md. All AI-assisted output was reviewed,
  edited and tested before submission.
* @isaaaxe used OpenAI Codex to assist with implementing Undo and Redo, including command handling,
  session history, transaction and persistence behavior, and regression tests. They also used it to
  understand the project, triage GitHub issues, implement bug fixes, update the User Guide, Developer
  Guide and UML diagrams, prepare commits and pull requests, track feature-freeze code changes, and
  review teammates' pull requests. AI-assisted output was reviewed, and relevant automated tests,
  build checks and documentation checks were run before submission. They remain responsible for
  understanding, validating and ensuring the correctness of the submitted work.
* @wtvlol used OpenAI Codex and Claude Code to assist with understanding the codebase, implementing
  and testing features, investigating and fixing bugs, updating the User Guide, Developer Guide and
  UML diagrams, and reviewing pull requests. AI-assisted output was reviewed and edited, with relevant
  tests and checks run before submission. They remain responsible for understanding and validating
  the submitted work.
* @duynguyen21007 used OpenAI Codex to assist with implementing and testing the shared Laplace argument
  parser, member/equipment/loan command routing, member and equipment directory components, directory
  search/filter/sort and inventory planning, Tab-completion improvements, and Apple Silicon JAR
  packaging. It also helped investigate and fix bugs, including Unicode and patronymic member-name
  validation and no-argument command handling; draft and revise User Guide and Developer Guide prose,
  PlantUML diagrams and manual testing instructions; run automated checks; prepare commits and pull
  requests; track feature-freeze code changes; and review teammates' pull requests. They remain
  responsible for understanding, reviewing and validating the submitted work.
* @kimjunkuno used OpenAI Codex for codebase exploration and planning, drafting and modifying Java
  implementation and regression tests, automated smoke and beta testing, performance investigation,
  issue triage and release prioritization. They also used Codex to draft and revise User Guide and
  Developer Guide prose and PlantUML diagrams, prepare commit messages and pull requests, track
  feature-freeze LoC changes, and review teammates' pull requests, including drafting and posting
  review feedback. This assistance included generated code, tests, documentation and review text,
  as well as running automated tests, build checks and documentation builds. They remain responsible
  for understanding, reviewing and validating the submitted work.
* OpenAI Codex assisted with Developer Guide explanations and PlantUML diagrams, including the
  equipment directory summary section and its two diagrams. Codex also assisted with preparing
  this acknowledgement.

This consolidated declaration records the uses above in accordance with the
[CS2103/T policy on reuse](https://nus-cs2103-ay2627-s1.github.io/website/admin/appendixB-policies.html#policy-on-reuse).

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

The *Sequence Diagram* below summarizes component responsibilities for deleting an active member
without open loans. Model represents both the isolated pending state and the live records. Logic
coordinates validation, atomic saving and publication; failures before publication leave live records
unchanged. Messages summarize responsibilities rather than individual API calls. The
[Logic component](#logic-component) expands the parser, command and candidate-creation interaction.

<puml src="diagrams/ArchitectureSequenceDiagram.puml" alt="Component responsibilities across UI, Logic, Model and Storage" />

[Open full-size diagram](diagrams/ArchitectureSequenceDiagram.png)

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<puml src="diagrams/ComponentManagers.puml" width="300" />

The sections below give more details of each component.

### Diagram notation

Diagrams show selected APIs and relationships rather than every implementation detail.
Class diagrams distinguish stored references from dashed call, creation and parameter dependencies.
Sequence diagrams describe the stated scenario; legends identify abbreviated calls and omitted objects.
Notation follows the [course UML reference sheet](https://nus-cs2103-ay2627-s1.github.io/website/admin/uml-reference-sheet.html).
The corresponding editable sources are in `docs/diagrams`.

### UI component

The UI displays member, equipment and loan records and accepts commands from the operator.
`MainWindow` connects the command box, feedback, navigation and read-only record panels.
Commands reach Logic through one execution callback; panels do not mutate domain records directly.

The **API** is specified in [`Ui.java`](https://github.com/AY2627S1-CS2103T-F15-2/tp/tree/master/src/main/java/seedu/address/ui/Ui.java).

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

`CommandResult` contains a typed `DisplayRequest` telling the shell which view to show.
A directory request with a query applies those criteria; one without a query preserves the current
criteria. Member selection uses NUS ID and equipment selection uses UUID, so a refresh can recover
the selected record after an edit. Model notifications are combined into one queued refresh.
Programmatic selection and refresh do not submit another command.

Equipment groups are derived from category and name. Each group retains its distinct physical units;
selecting a group does not select an arbitrary unit. Member and equipment details link to related
records through identity-based commands. `LoanPanel` shows open and completed loans and shares its
identity filters with command-based navigation.
`MemberDetailsPanel` also uses `LoanRows` for current holdings: at most ten rows stay inline, while
larger sections use reusable list cells. The sorted loan list retains every UUID and its dates;
refresh resolves the selected member again without callbacks or record changes.

`CommandPreview` copies text or prepares the command box for review; execution requires Enter.
Open-loan rows can prepare a return with an optional observed condition. `ResultDisplay` presents a
compact summary with Details and Copy actions for long feedback. Help and Tab completion reuse the
command vocabulary. The inherited person panel is outside supported navigation.

`LaplaceTheme.css` and `LaplaceFonts` centralize appearance. `mode dark` and `mode light` update GUI
preferences without changing records or undo history; appearance and window geometry are saved on
shutdown. Layout tests cover the supported screen sizes, while font tests check the resolved faces.
See [Testing](Testing.md) for the shared JavaFX runtime and execution instructions.

Below 600 logical pixels of scene height, compact spacing and wider minimum directory panes preserve
record visibility. Collapsed feedback places its heading beside the text; Details and Copy retain the
complete result. `verifyPackagedLayout` repeats the layout tests with production classes/resources
loaded from the built JAR and checks that code source explicitly. Logical-size tests do not establish
actual operating-system scaling support.


#### Shell execution and refresh

The following views expand the command callback, typed navigation and refresh boundary.
`MainWindow` supplies a method-reference implementation of `CommandExecutor`; it does not implement
that interface itself.

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

The UI loads FXML, themes and fonts from bundled resources. The diagram also shows `FxTestUtil`,
which runs UI tests on the shared JavaFX thread. Stylesheets are resources, not domain objects.

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
and shared search prefix constants define the completion vocabulary, including the `undo`/`redo`
utilities. Help and explicit Ctrl+Space completion use `CommandCatalog`. Search enum options follow
`DirectoryQuery`; dates, numeric counts, new member IDs and free-form notes are entered manually.
Batch loan completion replaces only the active UUID entry and excludes neighboring identities.
`CompletionController` owns cycling and resets after input, caret, focus or model changes. Enter retains
the ordinary parser and transactional command path. Tests compare family completion to parser routes,
exercise real JavaFX events, and load production classes from the packaged JAR.
See [Testing](Testing.md) for execution and coverage checks.

<puml src="diagrams/AutocompleteClass.puml" alt="Tab completion service, controller and read-only data dependencies" />

[Open full-size diagram](diagrams/AutocompleteClass.png)

The first plain Tab selects the first candidate. Further Tab or Shift+Tab events wrap through the same
candidate list; Shift+Tab alone does not start a cycle. External edits, caret/anchor/focus changes and
Enter clear the cycle. Without a candidate the event remains unconsumed. Replacement preserves the
original suffix, including Unicode separators and neighboring batch UUIDs, and never executes or saves.

<puml src="diagrams/AutocompleteSequence.puml" alt="Tab suggestion, forward and reverse cycling, and range replacement" />

[Open full-size diagram](diagrams/AutocompleteSequence.png)

#### Completion execution details

`CompletionController`, described in [Tab completion](#tab-completion), is created by `CommandBox`
and retained through the field's registered callbacks, rather than a `CommandBox` member field.
Static vocabulary uses no model reads. Record-value suggestions read the live root synchronously on
the FX thread and observe member/equipment collection changes with weak listeners. These notifications
invalidate record candidates after mutations, undo/redo and reload; rejected mutations and pure queries
leave those collections unchanged. Cached record cycles check root identity cheaply, while unchanged
candidates cycle without copying or rescanning records. Loan history and transaction validation are
outside suggestion reads.

### Logic component

**API** : [`Logic.java`](https://github.com/AY2627S1-CS2103T-F15-2/tp/tree/master/src/main/java/seedu/address/logic/Logic.java)

The class diagram shows the `Logic` API, its manager, parsers and current Laplace commands.
`XYZCommand` stands for a concrete command such as `MemberDeleteCommand`.

<puml src="diagrams/LogicClassDiagram.puml" alt="Logic API, manager, parser and command dependencies" />

The sequence below expands `member delete nus/A0123456X`: `AddressBookParser` delegates to
`MemberCommandParser`, then `MemberDeleteCommandParser`, which validates the NUS ID and constructs
`MemberDeleteCommand`. The command soft-deletes the member on the candidate, preserving identity
and retained history. Deletion is blocked when the member has open loans. `LogicManager` records the
change and saves before publishing; the result requests the Members directory through `DisplayRequest`.
The storage lifeline abbreviates `StorageManager`, JSON serialization and `AtomicFileWriter`.
The candidate's lifeline starts when the live model constructs its isolated `ModelManager` snapshot.

<puml src="diagrams/DeleteSequenceDiagram.puml" alt="Member delete family and leaf parsing, candidate execution, save and publication" />

[Open full-size diagram](diagrams/DeleteSequenceDiagram.png)

#### Command routing and legacy compatibility

`LogicManager.execute()` first calls `AddressBookParser.parseCommand()`. The root parser accepts
`member`, `equipment`, and `loans`, delegating to their family parsers and then the appropriate leaf
parser (for example, `EquipmentCommandParser` and `EquipmentAddCommandParser`). It also accepts
`undo`, `redo`, `mode`, `help` and `exit`. Family and leaf parsers remain responsible for validating subcommands and arguments.

The retired top-level words `add`, `edit`, `delete`, `clear`, `find`, and `list` now reach the existing
unknown-command branch and throw `ParseException("Unknown command.")`. In particular, `list members`
is rejected; the supported command is `member list`. Rejection occurs before a command is constructed
or executed, before a transaction candidate is created, and before storage is called.

For supported commands, `LogicManager` executes transactional mutations on an isolated model candidate,
saves that candidate state, and publishes it to the live root only after saving succeeds. Commands
marked `isReadOnly()` return without saving. This retirement does not change those execution rules.

`AddressBook` is still the shared root for persons, members, equipment, and loans. Its name and the
`getAddressBook()`/`saveAddressBook()` APIs do not identify disposable person-only behavior. Existing
JSON `persons` data is retained and round-trips when supported commands save. Person value classes
used by members, legacy command/parser implementations and their unit tests, and the legacy UI panel
remain for a separate dependency-aware cleanup. Removing public routes does not remove stored records.

The parser diagram focuses on the active member-delete route and the shared argument grammar.
Other family, leaf and utility parsers follow the same delegation pattern and are omitted for readability:

<puml src="diagrams/ParserClasses.puml" alt="Current member family and delete leaf parsers with shared validation helpers" />

[Open full-size diagram](diagrams/ParserClasses.png)

### Model component

The Model owns the in-memory records and their consistency rules. A `Member` is identified by NUS ID,
an `Equipment` unit by UUID, and a `Loan` connects those identities. `AddressBook` checks relationships
across records; the individual collections enforce identity uniqueness. `ModelManager` exposes stable
observable lists, derives availability and coordinates transaction candidates and undo/redo history.
Views observe these lists without owning or editing their backing collections. Mutations run on an
isolated copy, called a candidate. Publication updates live records only after that copy is saved.

**API** : [`Model.java`](https://github.com/AY2627S1-CS2103T-F15-2/tp/tree/master/src/main/java/seedu/address/model/Model.java)

#### Root collection integration

`AddressBook` contains four record collections and exposes stable observable views. Each root has independent
collection containers; immutable entity values may be shared across independent roots. Domain containers are
created lazily, while callers always receive stable unmodifiable lists. Cross-record loan integrity
belongs to `AddressBook`, rather than the UI or serialization layer.

<puml src="diagrams/RootModelClass.puml" alt="Root collections, immutable values and stable observable wrappers" />

[Open full-size diagram](diagrams/RootModelClass.png)

`resetData` first copies and validates every replacement collection and loan reference, and resolves all
replacement backing views. Only then does it replace live fields and views, deferring notifications until
the complete dataset is visible. The current package overload also runs a model-state callback before
notifications so observers see matching records and session history. It runs that callback even when
records are unchanged, without list notifications; the public overload supplies a no-op callback.
Validation failure leaves records and history intact. The callback must not throw or mutate records;
the diagram does not promise recovery from arbitrary exceptions thrown by publication actions or
external observer callbacks.

<puml src="diagrams/RootModelResetSequence.puml" alt="Root reset validation and complete publication before notification" />

[Open full-size diagram](diagrams/RootModelResetSequence.png)

#### Equipment and loan model

The following diagram shows the equipment-related subset of the current model. Member/person
collections, preferences, JavaFX collection wrappers and interfaces are omitted to keep its scope clear.
Solid arrows show stored references, and filled diamonds mark containers owned by `AddressBook`. The
dashed loan-to-equipment arrow describes UUID resolution, not an object reference stored inside `Loan`.

<puml src="diagrams/EquipmentModel.puml" alt="Equipment model, retained identities and loan references" />

`AddressBook` creates equipment/loan containers lazily, hence the `0..1` multiplicities. A null container
means an empty collection to callers. The `0..*` multiplicity at each collection end of the value
associations accounts for immutable objects shared across transaction candidates; it does not mean
one physical unit has several UUIDs. Equipment collections hold distinct UUIDs, and editing replaces a
value at its existing position. Ordinary associations are used instead of composition because immutable
values can outlive or be shared by their containers.

| Relationship or rule | Meaning in a valid root dataset |
| --- | --- |
| Loan → equipment | Each loan's `equipmentUuid` resolves to exactly one retained equipment record. A unit can have zero or more historical loans, with at most one open loan. |
| Loan → member (omitted from diagram) | Each loan's `memberNusId` resolves to one retained member. A member may have multiple open or closed loans. |
| Identity and details | UUID identifies a physical unit. Name/category determine a derived inventory group; matching descriptions do not merge identities. |
| Lifecycle | Deleted records retain UUIDs, details and closed loans. Open loans cannot reference deleted equipment or deleted members. |
| Availability | `ModelManager` returns UNAVAILABLE for deleted units. For active units, `AvailabilityCalculator` returns ASSIGNED for an open loan, AVAILABLE for GOOD/FAIR, and UNAVAILABLE for DAMAGED/UNDER_REPAIR. |

Availability, holders and inventory totals are derived, never persisted as independent fields.

Historical `issueCondition` accepts all defined condition values, including DAMAGED/UNDER_REPAIR.
This preserves observations of events outside current command rules; it is not a new-assignment
eligibility check. Missing/null observations remain unknown. Loading and save/reload must preserve
these values independently of current equipment condition.

`Condition.fromText` defines the shared command/storage normalization: strip surrounding whitespace,
match the four enum names without case, and reject unknown or blank text. Equipment still requires a
condition; missing/null loan observations remain unknown. Adapters always serialize uppercase enum
names. See the [manual design contract checks](#design-contract-checks) for normalization scenarios.

`AvailabilityCalculator` only accepts condition and open-loan state; the deletion check belongs to
`ModelManager`. `AddressBook` validates cross-record references, while `UniqueLoanList` enforces the
one-open-loan-per-unit rule. `UniqueEquipmentList` checks UUID uniqueness and identity-preserving
replacement. These responsibilities keep editing and restoration from breaking loan references.

#### Loan workflow and history

Successful batch feedback carries a compact count/date summary followed by per-unit identity lines.
Return lines capture each borrower before mutation, avoiding attribution to only the first borrower.
The existing ResultDisplay Details/Copy controls expose long results without expanding the compact
feedback area by default.

The following class diagram focuses on the Model's loan responsibilities. `ModelManager` delegates
to `AddressBook`, which checks cross-record references and validates batches. `UniqueLoanList` retains
completed loans and rejects a second open loan for the same equipment UUID. Immutable loan values
can be shared across transaction candidates without sharing mutable collection containers.

<puml src="diagrams/LoanWorkflowClassDiagram.puml" alt="Model responsibilities for batch loans and retained history" />

[Open full-size diagram](diagrams/LoanWorkflowClassDiagram.png)

Command parsing and execution are described under [Domain mutation transactions](#domain-mutation-transactions);
JSON conversion belongs to [Storage](#storage-component). In the UI, `LoanPanel.refresh()` applies
`LoanQuery.matches` to retained loans. Member and equipment detail panels use `LoanHistoryContent.add`
for identity-scoped history. These views do not own or mutate the retained loan records.

#### Model overview

<puml src="diagrams/ModelClassDiagram.puml" alt="Root, collections, members, equipment and the loan identities that link them" />

The diagram shows how the root, its collections and the three record types fit together, with the
member details used by editing, soft-delete and restoration. `ModelManager` delegates to `AddressBook`,
which owns person, member, equipment and loan containers. `UniquePersonList` is always present; the
three domain containers are initialized on demand, so their multiplicities are `0..1`. A null container
represents an empty collection. Stable views and `resetData` are explained in
[Root collection integration](#root-collection-integration).

The collection-to-entity multiplicities allow immutable values to be shared across independent
transaction candidates. Loan identities resolve through the root; `Loan` does not store direct
`Member` or `Equipment` references. Other equipment and loan fields, and legacy Person details, are
omitted to keep this diagram focused.

`Member` is immutable and contains its NUS ID, name, phone, email and an `isDeleted` boolean. The existing
four-field constructor creates an active member; the five-field constructor accepts deletion state.
Equality and hashing include all five fields, while `isSameMember` compares only the NUS ID.
The diagram shows object-valued fields once, as navigable association lines with multiplicities.
Role labels are omitted for readability. The primitive `isDeleted` flag appears in the attributes
compartment. Interface classifiers use the `<<interface>>` keyword and dashed realization lines.

Editing replaces supplied details without changing the NUS ID or deletion state. Deleted members must
be restored before editing. Soft-delete and restoration create replacements through `setDeleted`,
retaining collection position, reserved identities and historical loan references. The exposed member
list remains observable and includes deleted records. The model also retains user preferences through
`UserPrefs` and `ReadOnlyUserPrefs`.

`UniqueMemberList.setMembers` copies its input before validating identities and replacing contents.
Passing its own observable view or a backed sublist therefore preserves the intended records instead
of clearing the replacement. Invalid replacements leave the existing collection unchanged.

`Name.forMember` collapses whitespace and applies Unicode NFC before validating new names. Validation
allows letters followed by combining marks, spaces, straight/curly apostrophes, hyphens, periods,
and standalone `s/o`, `d/o`, `a/l` or `a/p` tokens in any letter case. Each connector requires a
letter-containing name on both sides; connector-only, leading, trailing and adjacent connectors are
rejected for new input. Names require at least one letter and at most 100 Unicode code points. Add and
edit share this factory. `Name.isValidStoredMemberName` and `Name.fromStoredMember` preserve earlier
valid names, including formerly accepted connector positions, without changing their saved spelling.
The JSON adapter uses that storage-specific validation. Editing another field retains the existing
`Name`; no schema migration is needed.

Deletion is blocked while a member has outstanding loans. Deleted members remain visible and
recoverable indefinitely; editing and new loans require restoration first. Restoration has no expiry
or automatic purge. Mutations use the existing transactional save-before-publish pipeline.

The diagram follows the [course modeling notation](https://nus-cs2103-ay2627-s1.github.io/website/se-book-adapted/chapters/modeling.html)
and shows selected structure; lifecycle behavior is explained in the text above.

### Storage component

Storage loads records and preferences from local JSON and saves changes without exposing partial files.
`StorageManager` delegates to one JSON storage class for each file. Adapters convert stored text into
immutable domain values, and the root validates references before a loaded dataset is accepted.
Record mutations save an isolated candidate before the live Model is updated. Invalid loads and failed
saves preserve the existing data file.

**API** : [`Storage.java`](https://github.com/AY2627S1-CS2103T-F15-2/tp/tree/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="850" alt="Current storage adapters and relationships" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

`JsonAdaptedLoan` rejects every unrecognized loan property using a Jackson `JsonAnySetter`, even when
its value is null. The diagnostic names the field and lists supported spellings; Jackson retains the
underlying cause through the checked loading failure. This prevents unknown lifecycle keys from being
discarded before an intended completed loan is interpreted as open. Missing/null `returnedDate` still
denotes an open loan; missing/null issue/return conditions remain unknown historical observations.
Loan schema extensions require an updated reader, rather than silent forward-field loss. Other
adapters retain their existing unknown-property policy; global unknown-property handling is unchanged.

`JsonSerializableAddressBook` stores collections of member, equipment and loan adapters alongside
the retained legacy person adapters. Loan adapters store the member NUS ID and equipment UUID,
preserving references without duplicating either record. The diagram shows storage types and
adapter collections; value fields and conversion methods are omitted.

The legacy `persons` collection remains required: absent and explicit null are rejected; empty and
populated arrays are accepted and round-trip. Keep this existing contract until a versioned migration
can distinguish omission safely. Removing legacy command routes does not authorize dropping stored
persons. Compatibility and diagnostics are expanded under
[Loading and compatibility](#loading-and-compatibility); disposable-file scenarios are in the
[manual design contract checks](#design-contract-checks).

### Common classes

The `seedu.address.commons` package supplies shared configuration, logging, JSON and text utilities.
Keeping these services outside the four main components lets them apply consistent parsing and
diagnostics without depending on UI controls or domain commands.

Shared text utilities normalize new command input while stored legacy values keep their spelling.
JSON utilities reject ambiguous input, including repeated properties, before domain conversion.
See [Loading and compatibility](#loading-and-compatibility) for the detailed boundary.

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
is read-only, `LogicManager` returns without creating a transaction candidate or saving data.
Equipment queries additionally use `Model.getAvailability` to derive availability from current state.

`DirectoryQuery.members(data, eligible)` combines caller eligibility with the member status filter.
`DirectoryQuery.equipment(data, lookup, eligible)` applies eligibility and equipment status before
resolving availability. Active totals exclude deleted records, while retained identities and loan
history remain available for details and restoration. These query helpers do not change the stored schema.

`MainWindow` passes the same query to the directory list and its summary before refreshing them.
Both evaluate the query against current data, keeping displayed rows and matching counts consistent.
Sorting affects derived result lists rather than stored records. Navigation requests without a query
preserve the current criteria; explicit list commands pass `DirectoryQuery.all()` to reset them.
After mutations, the shell refreshes the views with their retained queries and recovers selection by
NUS ID or equipment UUID when possible.

`EquipmentDirectorySummary.refresh` builds inventory summaries with `InventoryGroups.group`, passing
`DirectoryQuery.all().equipment(data, availabilityLookup)` as the matching units. It formats each group
using `EquipmentPresentation.counts(group.units(), availabilityLookup)`. The activity below shows this
GUI path when availability can be resolved; it does not describe the retained `summarize` helper.

<puml src="diagrams/InventoryPlanningActivity.puml" alt="Grouping all equipment units and formatting active availability and retained deletion counts" />

Each group uses a pair of normalized category and name values, keeping the two keys separate even
when either contains a display separator. `InventoryGroups.Key` applies whitespace normalization and
the shared `LaplaceText.caseKey` to both fields, so Greek `Σ`, `σ` and `ς` share a group identity.
Lookup, grouping and the retained `summarize` helper use the same key without rewriting stored values.
Equipment duplicate advisories compare those group keys too. Member advisories use the same shared
case key as member search, preserving the existing identity and deletion eligibility rules.
`InventoryGroups.group` retains all physical units under each key and emits groups in first-match order.
For this caller, the all-records query supplies default
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

### Command navigation to equipment groups

`EquipmentViewCommandParser` accepts either `uuid/UUID` or the pair `category/CATEGORY name/NAME`.
The shared argument parser rejects duplicate/unknown markers and validates supplied values in input
order. Incomplete group forms and mixed UUID/group forms are rejected. Full UUID syntax stays unchanged.
Tab completion supplies full unit UUIDs; compact table labels have no command syntax.

`EquipmentBrowseCommand` resolves an exact normalized `InventoryGroups.Key` against all retained units
at execution time, including deleted records. It returns `DisplayRequest.equipmentGroup(key)` without
choosing a physical unit, even when several units share the same category and name. Missing groups
throw `CommandException` before navigation. Exact unit viewing remains in `EquipmentViewCommand(UUID)`.
Both commands are read-only, preserving data, UUIDs, loan references, persistence and undo/redo history.

<puml src="diagrams/EquipmentBrowseSequence.puml" alt="Read-only equipment group lookup followed by typed group navigation" />

`MainWindow` consumes the existing group/unit requests. Plain requests retain directory criteria.
Group entry starts with matching units and no selected unit, and resets clicked table sort to query
order. Unit entry preserves table sort and reveals a target outside the filter using Show all units.
A renamed group can disappear, while full UUID references remain stable.

### Identity-based deletion

The member/equipment deletion leaf parsers require one normalized NUS ID or UUID and construct commands that resolve the
stored entity. The root enforces the open-loan constraint. Member failures report the held-item count;
equipment failures report the current holder. Successful feedback uses the stored name and identity.
Commands capture that context before mutation and preserve original domain failure causes.

<puml src="diagrams/IdentityDeletionClass.puml" alt="Member and equipment deletion parsers, commands and model boundary" />

[Open full-size diagram](diagrams/IdentityDeletionClass.png)

This member example runs on the isolated candidate supplied by `LogicManager`. Equipment differences
are noted below the sequence. Successful command execution still requires the transaction's atomic save
before the live root changes. Soft deletion retains details and closed loans.

<puml src="diagrams/IdentityDeletionSequence.puml" alt="Identity lookup, deletion guards and contextual member feedback" />

[Open full-size diagram](diagrams/IdentityDeletionSequence.png)

### Domain mutation transactions

Laplace mutations use an explicit transaction boundary. Commands opt
in with `isTransactional()`. `LogicManager` executes on independent candidate containers, saves the
candidate atomically, then calls `publishTransaction` to publish records and matching session history
through the existing observable root. Commands marked `isReadOnly()` return before recording or saving.
This contract does not turn remaining legacy commands into transactional mutations automatically.
See [Undo/redo](#undo-redo-feature) for session history behavior.

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

The next activity connects equipment input normalization, reading-order validation, bounded UUID
retries and reservation of every batch UUID before insertion. Other parsers reuse the same argument-validation
boundary; exact field rules remain in the command documentation and User Guide.

<puml src="diagrams/MvpValidationActivity.puml" alt="Equipment input validation and bounded UUID reservation activity" />

[Open full-size diagram](diagrams/MvpValidationActivity.png)

Loan panels and entity details show retained completed history; see
[Loan workflow and history](#loan-workflow-and-history).

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
the chain. Neither a parser failure nor invalid count creates a transaction candidate or saves data.

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

Editing, soft deletion and restoration use the same candidate/save/publish boundary with different
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
All maintenance commands are transactional: execute against a candidate, save atomically, then publish.
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
reversible changes (eleven history states). Each history state has independent collection containers for
members, physical equipment, loans and retained legacy persons; immutable entity values may be shared.

The class diagram below shows only the command, persistence and record-history collaborators. Parser
classes, collection wrappers, domain entities and preferences are omitted. The live model and each
candidate own separate roots; multiple immutable history values may reference the same private stored
history states. Accordingly, sharing these states uses ordinary associations rather than exclusive composition.
The ordered `states` association has multiplicity `0..11`, including empty history before the first change.
An `AddressBook` history state has no `ModelManager`, and the package constructor can reuse a supplied root,
hence `0..*` at the model end of that association. Every model still refers to exactly one root. The logic
associations allow model/storage collaborators to be shared by more than one `LogicManager` instance.

<puml src="diagrams/UndoRedoClassDiagram.puml" alt="Undo and redo command, model, history states and storage dependencies" />

The first successful mutation records both the preceding state and the candidate state. Later mutations
append a history state and a short action description, such as `equipment add`, without retaining command
parameter values in the description. Once ten changes are retained, the oldest change and its preceding
history state are discarded. A batch creation, issue or return is one history entry regardless of unit count.

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
   history state directly, preserving UUIDs, loan dates, conditions and references without replaying commands.
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

The table follows one equipment workflow after each successful mutation is saved and published.
`u` denotes the UUID generated by the first add command; substitute that UUID in later commands.
The loaded baseline `s0` contains no equipment. `s1` contains Camera in GOOD condition, `s2` contains
the same unit in FAIR condition, and `s3` contains it in DAMAGED condition. Each stored history state
contains all records, although this example describes only that unit.

| Step / command | Retained history states | `position` | Effect |
| --- | --- | --- | --- |
| Launch | `[]` | `0` (no selected state) | The live baseline `s0` is not stored yet; undo/redo are unavailable. |
| `equipment add name/Camera category/Photo condition/GOOD` | `[s0, s1]` | `1` | The first change records the baseline and result; undo becomes available. |
| `equipment edit uuid/u condition/FAIR` | `[s0, s1, s2]` | `2` | Append the edited state, preserving the unit's UUID. |
| `undo` | `[s0, s1, s2]` | `1` | Select `s1`; retain `s2` for redo. |
| `equipment list` | `[s0, s1, s2]` | `1` | Open the directory without changing history or redo availability. |
| `redo` | `[s0, s1, s2]` | `2` | Select the recorded `s2` without replaying the edit. |
| `undo` | `[s0, s1, s2]` | `1` | Select `s1` again. |
| `equipment edit uuid/u condition/DAMAGED` | `[s0, s1, s3]` | `2` | Append `s3`; discard `s2` and its action description. Redo becomes unavailable. |

The representative object diagram shows the history immediately after the first `undo`. Solid links
represent the entries in the ordered `states` collection; `position = 1` selects `s1`.
The highlighted history state matches the live record values after publication, but is a separate
`AddressBook` object. Undo/redo create new immutable history values; they do not mutate earlier ones.
A failed save publishes neither the changed records nor the changed history.

<puml src="diagrams/UndoRedoState3.puml" alt="Undo retains three linked history states and selects position one" />

#### Retention limit and command flow

After twelve successful record changes, only history states `s2` through `s12` and the ten actions between
them remain in the current history. The earliest reachable state is `s2`: ten undos reach position 0,
and ten redos can return to `s12`. These limits apply to reversible commands, not the number of records.
Older history values referenced transiently during a transaction can share stored history states; the bound
applies to the history retained by each model. A failed save cannot evict entries from the live history.

<puml src="diagrams/UndoRedoLimit.puml" alt="Eleven retained history states permit at most ten undo or redo steps" />

The activity diagram summarizes `LogicManager.execute()`. Public directory, details and utility commands
take the `isReadOnly()` path without saving records or changing history. Only successful transactional
saves publish candidate records and history. The nontransactional saving path remains for legacy
internal commands that do not declare themselves read-only.

<puml src="diagrams/CommitActivityDiagram.puml" alt="Parsing, staged execution, bounded history recording, atomic save and publication" />

#### Design considerations and verification

Complete history states keep linked member/equipment/loan data consistent and make batch restoration atomic.
They cost more memory than per-command inverse operations, so retention is capped at ten changes.
Inverse operations would require separate reversal logic for every command and batch failure path.

`AddressBookHistoryTest` covers the cap, branching, descriptions, history state isolation and invalid navigation.
Command/parser tests cover syntax and empty history. `UndoRedoIntegrationTest` verifies identity and loan
restoration, lifecycle changes, exact saved states, ten undos/redos, restart behavior and failed-save retries.
Shell tests exercise the real command box, preserved filters, missing details, loans and error feedback.
`ModelManagerTest` checks history availability inside synchronous collection listeners, unchanged-record
history transitions, and rollback when replacement records are invalid.

### Loading and compatibility

`SampleDataUtil` populates only members, equipment and loans on first launch. Its legacy person
fixtures remain available to tests but are not inserted into fresh datasets. The serialized `persons`
array remains required, and existing legacy contacts continue to load and round-trip unchanged.

After successful conversion, storage compares recorded assignment/actual-return dates with its local
clock and logs an actionable warning containing equipment UUID, member ID and recorded dates. It does
not reject or rewrite records on this basis: a clock rollback can make previously valid history appear
future-dated. Expected dates remain unrestricted relative to today. Command date restrictions remain enforced.

Record conversion and root-reference failures include a zero-based collection index plus stored NUS ID/
UUID, even if that identity is invalid. Original checked/domain causes remain attached. Null records
report their position with identity unavailable; date errors include the rejected text. Diagnostics
avoid echoing contact details.

Startup passes the checked loading failure to `UiManager`. The recovery dialog shows its immediate
cause's diagnostic without the exception class prefix; Jackson failures omit the source dump. A wrapped,
read-only text area keeps long diagnostics scrollable and copyable. Startup still aborts without an
editable main window, and the rejected file remains unchanged.
Persistent startup logs record the file path and safe failure category or recovery guidance, without
concatenating parser/exception messages or attaching source-bearing causes. The checked cause remains
available to the recovery dialog and caller.
This includes semantic conversion failures, whose raw identity fields may contain arbitrary hand-edited text.

The supported unversioned schema treats omitted/null `members`, `equipment` and `loans` as empty.
Retain both forms for legacy compatibility; saves canonicalize them to `[]` without touching populated
collections. No migration or warning distinguishes null from absence in this schema. Missing member/
equipment `deleted` flags still mean active; explicit null flags remain invalid.

`JsonUtil` enables Jackson's strict duplicate-property detection for JSON reads, including data and
preferences. Repeated names in one object fail parsing before model conversion, even when their values
are identical; the same field names in separate records remain valid. The checked loading exception
retains the parser's duplicate-field name and location. Reading a rejected file does not rewrite it.

`JsonUtil.fromJsonString` accepts one leading UTF-8 byte-order mark before parsing, without removing
markers elsewhere. Strict root and duplicate-property validation still apply. Stored string contents
are preserved. See the [manual design contract checks](#design-contract-checks) for disposable-file checks.

### Known validation and feedback limits

This release intentionally retains a narrower validation boundary for manually edited files than
for commands. The loader checks JSON structure, stored field validity, identity uniqueness, loan
references and duplicate open loans. It does not rerun every command-time normalization or chronology
check. This preserves supported legacy values and avoids treating storage conversion as a replay of
user commands; it does not establish that every accepted dataset is internally consistent.

The following issues are documented without changing production behavior:

| Issue | Retained behavior and operator guidance |
| --- | --- |
| [#403](https://github.com/AY2627S1-CS2103T-F15-2/tp/issues/403): formatted stored phones | `Phone.forMember` removes spaces/hyphens for commands, whereas `JsonAdaptedMember` and `Phone.fromStoredMember` expect a supported stored representation. Use normalized phone strings when editing JSON; retain acceptance of previously valid legacy strings. |
| [#405](https://github.com/AY2627S1-CS2103T-F15-2/tp/issues/405): email local-part dots | `Email.isValidMemberEmail` accepts leading and consecutive dots in the local part. This affects command input as well as loading; it is not solely a manual-edit limitation. Operators must check contact addresses. Tightening validation would also require a deliberate compatibility policy for saved records. |
| [#406](https://github.com/AY2627S1-CS2103T-F15-2/tp/issues/406): compact feedback | The collapsed Last result box can partially hide multiline usage guidance at the default size. Scrolling or Details exposes the complete message. This normal-use display limitation is separate from loading validation. |
| [#408](https://github.com/AY2627S1-CS2103T-F15-2/tp/issues/408): overlapping history | `UniqueLoanList` rejects duplicate open loans but does not validate overlap between open/completed or completed/completed loans for a UUID. Commands enforce availability and the latest-return boundary. Manual edits require chronological review, allowing return and reassignment on the same day. |

These are explicit release limitations, not equivalent to implementing the requested fixes or proving
that accepted data is correct. Keep the existing rejection checks and whole-load failure behavior.
See the UG's [manual editing guidance](UserGuide.md#advanced-manual-editing-and-compatibility) and
[feedback guidance](UserGuide.md#read-command-feedback).

Manual documentation checks:

1. Compare formatted phone input through `member edit` with the normalized phone stored in JSON;
   verify that the UG distinguishes command normalization from supported stored values.
2. Check that the email limitation explicitly applies to commands as well as manual edits.
3. Read a multiline rejected-command result through scrolling or Details.
4. Review the overlap example in #408 against the stated loading boundary. Do not treat successful
   loading as validation of manually edited history; retain same-day return/reassignment guidance.

### Atomic-save access policy

`AtomicFileWriter` creates an empty sibling candidate and restricts its POSIX mode or ACL before
writing any serialized data. On ACL filesystems, inherited grants are replaced with an owner-only ACL.
Only after writing/flushing succeeds does it copy the final destination policy before the atomic move.
Existing POSIX owner/group/mode and ACL-view owner/ACL are retained; failure
to read or apply that policy aborts without replacing the destination. POSIX files with no write bits
and DOS read-only files explicitly reject saving. This policy is separate from effective access checks:
no preliminary `isWritable()` check can guarantee a later replacement. New files retain the restricted
owner-only policy where POSIX or ACL views are supported. Other metadata is not copied. No serialization
schema changes are involved.

### Argument markers and no-argument validation

Standalone `s/o`, `d/o`, `a/l` and `a/p` tokens, ignoring case, remain literal field text in argument
parsing and completion. Other markers at the start of the argument string or after whitespace remain
reserved, including unknown markers in notes. A token immediately after another prefix's slash is
value text: `notes/unknown/value` is accepted, while `notes/ unknown/value` rejects `unknown/`.
The shared argument parser rejects extra input to no-argument commands with
`This command takes no arguments.` and the leaf parser appends its usage. Help and Exit apply the
same validation before returning a command, so invalid input cannot open Help or terminate Laplace.

### Command-word aliases and casing

`CommandWords.canonicalFamily` maps only `members` to `member` and `loan` to `loans`, using
`Locale.ROOT` casing. Family parsers normalize only their action token; leaf parsers still receive
unchanged parameter text. Top-level command words and appearance actions also ignore case. The same
family vocabulary drives `CompletionService` and the Ctrl+Space `CommandCatalog`. Both offer canonical
command words while preserving parameter text, prefix casing and existing Unicode whitespace behavior. Help describes the aliases; command examples remain canonical.

### Batch loan validation diagnostics

Assignment and return commands validate shared prerequisites first, then inspect every supplied unit
without mutating the candidate. Each unit contributes its first blocking reason, including its UUID;
return-date errors retain both dates and the unit name. Historical-availability checks run within
that same per-unit validation path, so an overlap and another invalid unit appear together. `LoanBatchValidation` rejects before the model
mutation: a sole error retains its original exception, while multiple errors produce an ordered summary
and full details, retaining causes. The existing `ResultDisplay` Details/Copy actions expose long lists.
Transaction, persistence and domain relationships are unchanged; no serialization or UML changes apply.

### Command-based Loans filters

`loans list [nus/TEXT] [uuid/TEXT]` is a read-only view command. Bare list preserves existing filters;
any explicit field replaces both (omitted/blank fields clear). `loans reset` applies `LoanQuery.all()`.
`LoanQuery` is an immutable value sharing case-insensitive identity-substring matching between CLI
and GUI across open and completed history. It does not validate partial text as a complete identity.
It uses `LaplaceText.stripWhitespace` for Unicode boundary trimming while preserving internal spaces.
`DisplayRequest` optionally carries that query; `MainWindow` applies it without interpreting feedback.
`LoanPanel.setQuery` updates both fields together and refreshes once; direct edits use the same predicate.
No filter state is persisted or placed in undo history, and refresh/select never invokes command callbacks.
The displayed/total summary explains retained records hidden by the filters.

<puml src="diagrams/LoanFilterCommandSequence.puml" alt="Read-only loan filter command and synchronized panel fields" />

--------------------------------------------------------------------------------------------------------------------

## **Documentation, logging, testing, dev-ops**

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [DevOps guide](DevOps.md)
* [Single-JAR distribution and release checks](DevOps.md#single-jar-distribution)

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

The requirements below distinguish the delivered product from proposals. A future story records an
intended capability without committing the release to implementing it. Current commands and limitations
are specified in the User Guide; implementation sections describe the shipped design.

* **MVP baseline:** Add, list, view, and delete members and equipment; identify members by NUS ID and equipment by a generated UUID; issue and return individual items; show holdings and holders; validate input and preserve consistency on failure.
* **Delivered beyond the baseline:** Member/equipment editing and restoration, substring search, filters and sorting, inventory group summaries, batch creation/issue/return, handover condition recording, and retained loan histories are implemented. Undo/redo retains up to ten changes per session.
* **Future proposals:** Import/export with preview, restoring a complete exported backup, archival, approximate-name matching, and a browsable action audit remain unimplemented. They are labeled Future in the stories and use cases below.
* **Deletion and retention:** Members and equipment are soft-deleted only without open loans. Records and closed history are retained; restoration has no time limit. This supersedes the original permanent-deletion and proposed 30-day recovery policies. The Loans tab and entity details show completed loan history.
* **Single-user boundary:** Proposed import/export would support the same user's local records and backups. Sharing an exported dataset with another operator is outside regular product operations. Shared datasets, concurrent users, and member self-service are outside scope.

### User stories

Priorities: High (must have) — `* * *`; Medium (nice to have) — `* *`; Low (lower-priority candidate) — `*`.
Priority expresses product importance, not implementation status. **MVP** marks delivered baseline capabilities; **Implemented** marks delivered additions. **Future** marks an unimplemented proposal without a delivery commitment. Existing story IDs are retained for traceability; new stories are appended rather than renumbering earlier requirements.

| ID | Priority | Scope | As a … | I want to … | So that I can … |
|----|----------|-------|--------|-------------|----------------|
| US01 | `* *` | Future | newly appointed CCA EXCO member | import member and equipment records from a local file | avoid re-entering existing lists manually. |
| US02 | `* *` | Future | cautious CCA EXCO member | preview additions, changes, and rejected records before confirming an import | correct problems before changing my current records. |
| US03 | `* *` | Future | CCA EXCO member | export all Laplace data to one portable backup snapshot file | keep a complete backup of my records. |
| US04 | `* * *` | MVP | CCA EXCO member | add a member with their NUS ID, name, phone number, and email | keep track of a person who has joined the CCA. |
| US05 | `* * *` | MVP | CCA EXCO member | list member records, including retained deleted records | review membership and recognize records that are no longer eligible for new loans. |
| US06 | `* * *` | MVP | CCA EXCO member | view one member's full contact details | verify how to contact that person before an operation. |
| US07 | `* *` | Implemented | CCA EXCO member | correct or update a member's details | keep contact information accurate while retaining their loan history. |
| US08 | `* *` | Future | CCA EXCO member | archive an inactive member | keep the active list relevant while retaining historical records. |
| US09 | `* * *` | MVP | CCA EXCO member | mark a mistaken member record as deleted | prevent further use of that record while retaining its history. |
| US10 | `* *` | Implemented | CCA EXCO member | restore an accidentally deleted member record | resume using it without re-entering the member details. |
| US11 | `* *` | Implemented | CCA EXCO member | filter and sort members by a chosen field | focus on the people relevant to my current task. |
| US12 | `* *` | Implemented | busy CCA EXCO member | find members using part of their recorded identity or contact details | retrieve relevant records without knowing the complete value. |
| US13 | `* * *` | MVP | CCA EXCO member adding data | have duplicate NUS IDs rejected with an explanation | avoid conflicting records for the same member. |
| US14 | `* * *` | MVP | CCA EXCO member | register a newly acquired physical equipment unit | track it separately from other units with matching descriptions. |
| US15 | `*` | Implemented | CCA EXCO member receiving a batch | add several units of the same equipment type in one operation | record a delivery without repeating the same entry process. |
| US16 | `* * *` | MVP | CCA EXCO member | list equipment records and their availability, including retained deleted units | review the inventory and distinguish units eligible for issue from retained records. |
| US17 | `* * *` | Implemented | CCA EXCO member | inspect an equipment group and one of its physical units | verify the unit's identity, condition, and recorded information. |
| US18 | `* *` | Implemented | CCA EXCO member | edit an equipment record while retaining its UUID | keep item information accurate without breaking loan references. |
| US19 | `* * *` | MVP | CCA EXCO member | mark a mistaken equipment record as deleted | prevent further issue against it while retaining its history. |
| US20 | `* *` | Implemented | CCA EXCO member | restore an accidentally deleted equipment record | resume tracking the same unit without re-entering its details. |
| US21 | `* *` | Implemented | CCA EXCO member | filter and sort equipment by category, condition, or availability | identify suitable items for my current task. |
| US22 | `* *` | Implemented | busy CCA EXCO member | find equipment using part of its recorded identity or description | locate relevant units without knowing a complete UUID or description. |
| US23 | `* *` | Implemented | CCA EXCO member planning an activity | see counts of available, assigned, and unavailable units of an equipment type | decide whether there is enough equipment without counting manually. |
| US24 | `* * *` | MVP | CCA EXCO member issuing equipment | assign an available item to a member and record its assignment date | track who is responsible for the item and when it was issued. |
| US25 | `* * *` | MVP | CCA EXCO member issuing equipment | record an expected return date | know when to follow up on the loan. |
| US26 | `* * *` | MVP | CCA EXCO member receiving equipment | record an assigned item as returned with its actual return date | keep the holder and availability information accurate. |
| US27 | `* * *` | MVP | CCA EXCO member | view all equipment currently held by one member | check that member's outstanding responsibilities in one place. |
| US28 | `* * *` | MVP | CCA EXCO member | see an assigned item's current holder and contact details | identify whom to approach when the item is needed. |
| US29 | `* * *` | MVP | CCA EXCO member issuing equipment | have assignments of already assigned, damaged, or under-repair items rejected | avoid recording multiple holders or issuing unsuitable equipment. |
| US30 | `*` | Implemented | CCA EXCO member distributing a kit | assign several items to one member in one operation | process a bundled issue quickly. |
| US31 | `*` | Implemented | CCA EXCO member receiving a kit | record several items as returned in one operation | process a bundled return quickly, including units borrowed by different members. |
| US32 | `* *` | Implemented | CCA EXCO member issuing equipment | retain a record of each item's condition at issue | compare it with the condition observed at return. |
| US33 | `* *` | Implemented | CCA EXCO member receiving equipment | record an item's observed condition at return | identify changes while the handover is still fresh. |
| US34 | `* *` | Implemented | CCA EXCO member investigating an item | view its past equipment loans | trace who held it and when. |
| US35 | `* *` | Implemented | CCA EXCO member reviewing a member | view that member's past equipment loans | resolve questions about previous loans without searching separate records. |
| US36 | `* *` | Future | CCA EXCO member archiving a member | be warned and prevented from archiving a member with outstanding equipment | resolve responsibility before removing the member from active records. |
| US37 | `* *` | Implemented | CCA EXCO member who made a mistake | undo my most recent successful data-changing action | recover without reconstructing the previous state manually. |
| US38 | `* *` | Implemented | CCA EXCO member | redo an action I just undid | recover when the undo itself was a mistake. |
| US39 | `*` | Future | CCA EXCO member investigating a discrepancy | view a recent history of data-changing actions | understand how the current records came about. |
| US40 | `* *` | Future | CCA EXCO member recovering from data loss | restore a previously exported complete backup snapshot | resume operations without rebuilding every record. |
| US41 | `* * *` | MVP | CCA EXCO member entering invalid data | receive an actionable error with no partial change applied | correct my input while trusting that the records remain consistent. |
| US42 | `* *` | Implemented | newly appointed CCA EXCO member | access command instructions and examples | learn operations and check syntax when I forget it. |
| US43 | `* * *` | MVP | CCA EXCO member | identify a member unambiguously when names match | choose the correct borrower for a loan or follow-up. |
| US44 | `* * *` | MVP | CCA EXCO member | retrieve equipment by its exact UUID | distinguish physical units even when their descriptions match. |
| US45 | `* * *` | MVP | CCA EXCO member | have successful changes saved locally and loaded on the next launch | continue managing records across sessions without re-entering them. |
| US46 | `* *` | Implemented | CCA EXCO member investigating a discrepancy | filter and reset loan history by member or equipment identity from the command box | inspect the relevant open and completed loans using the keyboard. |
| US47 | `* * *` | Implemented | CCA EXCO member following up on loans | see all outstanding loans and identify overdue or due-today returns | prioritize reminders and recover equipment on time. |
| US48 | `* *` | Future | CCA EXCO member unsure of a member's spelling | find a member using an approximate name | recover from spelling mistakes in a query. |
| US49 | `* *` | Future | CCA EXCO member unsure of an equipment description | find equipment using an approximate name | locate a unit despite spelling mistakes in a query. |

**Domain rules supporting these stories:** Member NUS IDs and equipment UUIDs remain stable. Deletion retains records and history and is blocked by open loans. Restoration has no deadline. Expected return cannot precede assignment. New issues record the unit's current condition; legacy history can have unknown observations. A new successful record change after undo invalidates redo; read-only actions and rejected commands do not. These rules constrain the delivered operations rather than changing their user goals.

### Use cases

For all use cases, the **system** is Laplace and the **actor** is the CCA EXCO member operating their local installation. The application is running with a valid dataset unless stated otherwise. The member borrowing equipment is a recorded entity, not a second system actor. **MSS** means main success scenario. An extension labeled `Na` occurs just after step N has completed; each extension states where execution resumes or ends.

For every data-changing use case, a validation failure leaves all records unchanged. If saving fails, Laplace reports that the operation was not saved and retains the preceding consistent state; the use case ends unsuccessfully. Success is reported only after persistence succeeds. A batch succeeds or fails as a whole. These rules also apply to proposed future operations.

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
* 3a. The new member shares a name, phone number, or email address with an active member under another NUS ID.
    * 3a1. Laplace also shows a possible-duplicate advisory identifying the existing records. The new member stays saved under their distinct identity.
    * Use case ends.

#### UC02: Register physical equipment units

**Scope:** Implemented. **Related stories:** US14, US15, US17, US41, US45.

**MSS**

1. The EXCO member provides the units' name, category, condition, optional notes, and optional count.
2. Laplace validates the details and count (default 1, range 1–100), then reserves a distinct UUID per unit.
3. Laplace saves all units together and reports their UUIDs and derived availability. Units recorded as `DAMAGED` or `UNDER_REPAIR` are unavailable for issue.

Use case ends.

**Extensions**

* 2a. A required detail is missing, or a supplied detail or count is invalid or repeated.
    * 2a1. Laplace explains what needs correcting without adding any units.
    * Use case resumes at step 1.
* 2b. Laplace cannot generate a unique identifier.
    * 2b1. Laplace reports the failure without adding any units.
    * Use case ends.
* 3a. The new units' category and name match equipment already recorded before this request.
    * 3a1. Laplace also shows a possible-duplicate advisory identifying those records. Every new physical unit stays saved under its own UUID; units in the same new batch do not warn about each other.
    * Use case ends.

#### UC03: Issue equipment to a member

**Scope:** Implemented for individual and batch assignments. **Related stories:** US06, US16, US24, US25, US27–US30, US32, US41, US43, US44.

**MSS**

1. The EXCO member requests the intended member's details by NUS ID.
2. Laplace shows that member's details and current holdings.
3. The EXCO member requests the equipment inventory.
4. Laplace shows the equipment, its availability, and its UUIDs.
5. The EXCO member requests assignment of one or more distinct equipment units to that member, supplying the assignment and expected return dates.
6. Laplace validates all references, availability, and dates, then saves every open loan together, recording each unit's issue condition and showing the updated holder and holdings.

Use case ends.

**Extensions**

* 1a. The member identifier is invalid or no matching member exists.
    * 1a1. Laplace explains the problem and retains the previous view.
    * Use case resumes at step 1.
* 5a. A supplied identifier is invalid, repeated, or does not identify an existing record.
    * 5a1. Laplace rejects the request and explains the invalid or missing reference without creating any loans.
    * Use case resumes at step 5.
* 5b. The selected member is deleted.
    * 5b1. Laplace refuses assignment and explains that the member must first be restored.
    * Use case resumes at step 1.
* 5c. Any selected equipment unit is deleted.
    * 5c1. Laplace refuses the entire assignment and identifies the unit that must be restored before issue.
    * Use case resumes at step 5.
* 5d. Any selected unit already has an open loan or is `DAMAGED` or `UNDER_REPAIR`.
    * 5d1. Laplace refuses the entire assignment and reports the affected units and reasons, including the current holder when applicable.
    * Use case resumes at step 5.
* 5e. A date is invalid, assignment is after today or before a selected unit's last recorded return, or expected return precedes assignment.
    * 5e1. Laplace explains the violated date rule and creates no loans, including for otherwise valid units in a batch.
    * Use case resumes at step 5.

* 6a. The assignment is more than ten calendar years before today, or the expected return is more than ten
  calendar years after today, while all validation rules pass.
    * 6a1. Laplace appends a date-check warning to the normal success feedback; the assignment is saved.
    * Use case ends successfully.

#### UC04: Record an equipment return

**Scope:** Implemented for individual and batch returns. **Related stories:** US17, US26–US28, US31, US33, US41, US44.

**MSS**

1. The EXCO member requests an item's details by UUID.
2. Laplace shows the item and its current loan details, if any.
3. The EXCO member requests that one or more distinct units be recorded as returned, supplies the actual return date, and optionally supplies their observed return condition.
4. Laplace validates and saves all returns together, closes their loans, and updates holders and holdings. A supplied return condition is recorded in history and becomes each unit's current condition; otherwise the current condition is retained. Units in `GOOD` or `FAIR` condition become available, while `DAMAGED` or `UNDER_REPAIR` units remain unavailable. A return after the expected date is valid; reaching the due date alone never records a return.

Use case ends.

**Extensions**

* 1a. The UUID is invalid or no item matches it.
    * 1a1. Laplace explains why it cannot show the requested item.
    * Use case resumes at step 1.
* 3a. A return identifier is invalid or repeated, a unit is missing, or any selected unit has no open loan, including an already recorded return.
    * 3a1. Laplace rejects the entire return request and explains the affected references or units without changing any records.
    * Use case resumes at step 3.
* 3b. The actual return date is invalid, before any selected loan's assignment, or after today.
    * 3b1. Laplace explains the allowed date range and makes no returns.
    * Use case resumes at step 3.
* 3c. The observed condition is not a recognized condition value.
    * 3c1. Laplace explains the accepted values: `GOOD`, `FAIR`, `DAMAGED`, and `UNDER_REPAIR`.
    * Use case resumes at step 3.
* 3d. An open loan refers to a missing member because the stored relationships are inconsistent.
    * 3d1. Laplace reports the affected equipment and missing-member relationship and makes no returns.
    * Use case ends unsuccessfully; the operator must recover consistent data before retrying.

* 4a. The actual return is more than ten calendar years before today while all validation rules pass.
    * 4a1. Laplace appends a date-check warning to the normal success feedback; the return is saved.
    * Use case ends successfully.

#### UC05: Delete an erroneous member or equipment record

**Scope:** MVP. **Related stories:** US05, US09, US16, US19, US41.

The EXCO member chooses the record kind before starting. A member is identified by NUS ID; a physical equipment unit is identified by UUID. The selected kind determines which directory and identifier are used in the steps below.

**MSS**

1. The EXCO member requests the directory for the selected record kind.
2. Laplace shows the records and their identifiers, including retained deleted records.
3. The EXCO member requests deletion of one record using the identifier for that kind.
4. Laplace verifies deletion eligibility and marks the record deleted, retaining its identity, details, and closed loans, then saves and confirms the deletion.

Use case ends.

**Extensions**

* 2a. The requested directory is empty.
    * Use case ends.
* 3a. The identifier is invalid or no matching record exists.
    * 3a1. Laplace reports the problem without deleting anything.
    * Use case resumes at step 3.
* 3b. The selected member still holds equipment.
    * 3b1. Laplace refuses member deletion and explains that the outstanding equipment must first be returned.
    * Use case ends without deletion; the EXCO member may complete UC04 and restart UC05.
* 3c. The selected equipment unit has an open loan.
    * 3c1. Laplace refuses equipment deletion and identifies its holder.
    * Use case ends without deletion; the EXCO member may complete UC04 and restart UC05.
* 3d. The selected member or equipment unit is already deleted.
    * 3d1. Laplace rejects the repeated deletion without saving. For a member, it also explains how to restore the retained record.
    * Use case ends.

#### UC06: Import records after reviewing a preview

**Scope:** Future; not available in this release. **Related stories:** US01, US02, US13, US41.

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
* 2c. The EXCO member cancels instead of confirming the preview.
    * Use case ends with current data unchanged.
* 3a. The source file or current dataset has changed since the preview.
    * 3a1. Laplace invalidates the old preview without applying it.
    * Use case resumes at step 2.

#### UC07: Restore a complete backup

**Scope:** Future; not available in this release. **Related stories:** US03, US40, US41.
**Precondition:** The EXCO member has a previously exported complete backup snapshot. The proposed operation must support recovery even if the normal data file cannot be loaded.

**MSS**

1. The EXCO member selects a local backup snapshot for restoration.
2. Laplace validates the complete backup snapshot and explains that restoration will replace the current dataset.
3. The EXCO member confirms replacement.
4. Laplace restores and saves the backup snapshot as one operation, then shows the restored records.

Use case ends.

**Extensions**

* 2a. The backup snapshot is unreadable, incompatible, incomplete, or contains invalid records or broken loan references.
    * 2a1. Laplace rejects it and explains the problem without replacing current data.
    * Use case resumes at step 1.
* 2b. The EXCO member cancels instead of confirming replacement.
    * Use case ends with current data unchanged.
* 3a. The backup snapshot has changed since validation.
    * 3a1. Laplace discards the previous validation result without replacing current data.
    * Use case resumes at step 2.

#### UC08: Undo a mistaken change

**Scope:** Implemented for up to ten changes per session; restarting clears session history. **Related stories:** US37, US41.

**MSS**

1. The EXCO member requests undo of the most recent successful data-changing action.
2. Laplace restores and saves the preceding consistent state, including linked records, and identifies the undone action.
3. The EXCO member inspects the restored records.

Use case ends.

**Extensions**

* 1a. There is no action available to undo.
    * 1a1. Laplace explains that no undo is available and leaves the data unchanged.
    * Use case ends.
* 3a. The EXCO member decides the undo was a mistake and requests redo.
    * 3a1. The EXCO member performs UC11.
    * Use case ends.

#### UC09: Review and filter loan history

**Scope:** Implemented. **Related stories:** US34, US35, US46, US47.

**MSS**

1. The EXCO member requests loan history, optionally filtering by member and/or equipment identity text.
2. Laplace shows matching open and completed loans and their displayed/total counts. Open loans show Overdue or Due today status when applicable.

Use case ends.

**Extensions**

* 1a. The request contains an unknown or repeated parameter.
    * 1a1. Laplace reports the error and retains the previous view, criteria, and records.
    * Use case resumes at step 1.
* 2a. No retained loans match the supplied criteria.
    * 2a1. Laplace shows empty matching results with the displayed/total counts and preserves the criteria.
    * Use case continues at step 2b1 if the EXCO member requests a reset; otherwise it ends.
* 2b. The EXCO member wants to see all retained loans again.
    * 2b1. The EXCO member requests a filter reset.
    * 2b2. Laplace shows all retained loans.
    * Use case ends.
* 2b1a. The reset request has extra arguments.
    * 2b1a1. Laplace rejects it without changing criteria or records.
    * Use case resumes at step 2b1.

#### UC10: Edit equipment details

**Scope:** Implemented. **Related stories:** US18, US41, US44, US45.

**MSS**

1. The EXCO member supplies a unit's UUID and one or more corrected details.
2. Laplace validates the fields and requires an existing active unit and an actual change.
3. Laplace replaces the details under the same UUID, saves, and displays the updated unit. Existing loan references are retained, including when the unit is on loan.

Use case ends.

**Extensions**

* 2a. Fields are missing, invalid, repeated, or not supported, or the UUID is invalid or unknown.
    * 2a1. Laplace explains the problem without changing records.
    * Use case resumes at step 1.
* 2b. The unit is deleted.
    * 2b1. Laplace explains that the unit must first be restored.
    * Use case ends.
* 2c. The supplied details make no change.
    * 2c1. Laplace reports that there is no change to save.
    * Use case resumes at step 1.

#### UC11: Redo an undone change

**Scope:** Implemented within the retained session history. **Related stories:** US38, US41.

**MSS**

1. The EXCO member requests redo of an undone action.
2. Laplace restores and saves the corresponding recorded state, including linked records, and identifies the redone action.
3. The EXCO member inspects the resulting records.

Use case ends.

**Extensions**

* 1a. No redo is available, including after a new successful data-changing action has discarded the undone branch or after restarting.
    * 1a1. Laplace explains that no redo is available and leaves the current data unchanged.
    * Use case ends.

#### UC12: Edit member details

**Scope:** Implemented. **Related stories:** US06, US07, US41, US43, US45.

**MSS**

1. The EXCO member supplies the member's NUS ID and one or more corrected details.
2. Laplace validates the fields and requires an existing active member and an actual change.
3. Laplace saves the corrected details under the same NUS ID and shows the updated member. Current and completed loans remain attached to that identity.

Use case ends.

**Extensions**

* 2a. Fields are missing, invalid, repeated, or not supported, or the NUS ID is invalid or unknown.
    * 2a1. Laplace explains the problem without changing records.
    * Use case resumes at step 1.
* 2b. The member is deleted.
    * 2b1. Laplace explains that the member must first be restored.
    * Use case ends.
* 2c. The supplied details make no change.
    * 2c1. Laplace reports that there is no change to save.
    * Use case resumes at step 1.
* 3a. The corrected member shares a name, phone number, or email address with an active member under another NUS ID.
    * 3a1. Laplace also shows a possible-duplicate advisory identifying those records. The member stays saved under the same NUS ID.
    * Use case ends.

#### UC13: Restore a deleted member or equipment unit

**Scope:** Implemented without a restoration deadline. **Related stories:** US10, US20, US41, US45.

The record kind determines the directory and identifier: NUS ID for a member, UUID for an equipment unit.

**MSS**

1. The EXCO member requests retained deleted records of the selected kind.
2. Laplace shows matching retained records and their identities.
3. The EXCO member requests restoration of one record using its identity.
4. Laplace clears its deletion flag, saves, and shows the restored record with the same identity, details, and closed loan history. An equipment unit retains its condition and becomes available only if that condition permits issue.

Use case ends.

**Extensions**

* 2a. No deleted records match the request.
    * Use case ends.
* 3a. The identity is malformed or unknown.
    * 3a1. Laplace explains the problem without changing records.
    * Use case resumes at step 3.
* 3b. The record is already active.
    * 3b1. Laplace rejects the restoration without saving.
    * Use case ends.

#### UC14: Search, filter, and sort members or equipment

**Scope:** Implemented for substring search and supported filters/sorts; approximate-name matching remains future work. **Related stories:** US11, US12, US21–US23, US41, US48, US49.

The EXCO member chooses the member or equipment directory before starting. Member criteria include current holdings; equipment criteria include category, condition, and availability. Both support identity/description text, lifecycle status, and their supported sort fields.

**MSS**

1. The EXCO member requests records of the selected kind with search text, filters, and a sort order.
2. Laplace shows matching records in that order with their counts and the applied criteria. Omitted lifecycle status selects active records.
3. The EXCO member inspects a matching record or refines the criteria.

Use case ends.

**Extensions**

* 1a. A criterion or sort field is invalid, unsupported for the selected directory, unknown, or repeated.
    * 1a1. Laplace explains the error and preserves the previous criteria, view, and records.
    * Use case resumes at step 1.
* 2a. No records match.
    * 2a1. Laplace shows an empty result with the applied criteria; stored records remain unchanged.
    * Use case resumes at step 1.
* 3a. The EXCO member requests all records of the selected kind instead of refining the criteria.
    * 3a1. Laplace clears the search criteria and shows all retained records, including deleted ones, in the default order.
    * Use case ends.

### Non-Functional Requirements

These are acceptance targets for the intended product, not measurements of the current implementation. Product constraints are adapted from the [course project constraints](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-constraints.html); development-process requirements such as incremental delivery are not product NFRs.

1. **NFR01 — Platform compatibility:** The release shall launch and support its documented operations on Windows, Linux, and macOS with Java 25 as the only installed Java version.
2. **NFR02 — Portable distribution:** The application shall run from a single downloadable JAR without an installer or separate installation of dependencies other than Java. The JAR shall not exceed 100 MB.
3. **NFR03 — Local operation:** Member, equipment, and loan operations shall work without a network connection, remote server, or external account. Each dataset belongs to one local operator; shared or concurrent access is unsupported.
4. **NFR04 — Editable storage:** Persistent records shall use local, human-editable text files without a DBMS. Edits made while the application is closed that meet the documented loading rules shall load on restart; failures of those checks shall produce a diagnostic rather than be silently accepted. Loading is not a complete audit of manually edited history; see [known validation and feedback limits](#known-validation-and-feedback-limits).
5. **NFR05 — Keyboard operation:** Every documented record-management operation shall be completable with keyboard input alone. Users shall not need mouse actions to submit commands, inspect results, or correct input.
6. **NFR06 — Display compatibility:** At 1920×1080 with 100% or 125% scaling, essential controls and feedback shall not overlap or be clipped. At 1280×720 with 150% scaling, every operation and its result shall remain accessible, using scrolling where necessary.
7. **NFR07 — Capacity and response time:** With 300 members, 1,000 equipment items, and 2,000 loans on a computer with a four-core CPU, 8 GB RAM, and an SSD, at least 95 of 100 executions of each core add, list, exact lookup, delete, issue, and return operation shall display a result within two seconds of submission. Measure after startup, with valid data and no competing intensive workload; include local saving for mutations. Startup with the same dataset shall complete within ten seconds. Larger datasets are accepted, but response times may exceed this target.
8. **NFR08 — Atomicity and durability:** After a successful mutation and normal shutdown, restarting shall recover the same records and loan relationships. Validation or reported save failures shall not leave partially updated member, equipment, or loan data. A failed file replacement shall preserve the last valid saved dataset. The same guarantee applies to batch and undo/redo operations as a whole, and to future import/restore operations.
9. **NFR09 — Consistent feedback:** Every rejected command shall identify the first detected problem and provide an accepted format or corrective action. Input errors shall not terminate the application, and the submitted command shall remain available for correction. Success and error messages shall be distinguishable by text, without relying on color alone.
10. **NFR10 — Data confidentiality:** Laplace shall not transmit member details or inventory records over the network. Diagnostic logs shall omit member names, phone numbers, email addresses, and raw commands containing those values. Local text data rely on the operating system's file access controls; application-level encryption is not promised.
11. **NFR11 — Read-only navigation:** Directory, detail, loan-filter, Help and Exit operations shall remain available when record storage cannot be written. They shall not save domain records or consume undo steps.

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
| Equipment type | A particular kind or model of equipment, such as a Canon EOS R50 camera. Physical units with the same normalized category and name form an inventory group, which inventory summaries use. |
| Condition | The recorded physical state of an item: `GOOD`, `FAIR`, `DAMAGED`, or `UNDER_REPAIR` in the MVP. |
| Availability | A derived state, never an independently stored value. Deleted units are UNAVAILABLE. For active units, an open loan means ASSIGNED; otherwise GOOD/FAIR means AVAILABLE and DAMAGED/UNDER_REPAIR means UNAVAILABLE. |
| Equipment loan / assignment | A relationship between one member and one equipment item, with assignment, expected return, and optional actual return dates. A member may have several loans; an item has at most one open loan. |
| Open loan / outstanding equipment | A loan with no actual return date, or the item covered by that loan. The associated member remains responsible until return is recorded. |
| Closed loan | A loan whose actual return date has been recorded. It no longer determines a current holder. |
| Holder | The member referenced by an item's open loan. An item without an open loan has no current holder. |
| Assignment date | The date the item was issued; it must be a real calendar date no later than the computer's local date. |
| Expected return date | The due date, on or after assignment. Reaching it does not automatically record a return. |
| Actual return date | The recorded date of return, from the assignment date through the computer's local date, inclusive. |
| Overdue loan | An open loan whose expected return date is before the computer's local date. This definition does not imply automatic notifications. |
| Active record | A member or equipment record whose deletion flag is false. All-record lists also display retained deleted records; active-only searches exclude them. Active equipment can still be assigned or unavailable. |
| Archive | A future proposal for setting aside an inactive member while preserving history. Archival is not implemented and is distinct from the shipped soft-delete/restore operations. |
| Delete | Soft deletion: marking a retained record deleted, allowed only without open loans. Its identity remains reserved, and its details and closed history stay stored. |
| Restore | Reactivating a retained deleted member or equipment unit without a time limit, preserving identity and history. |
| Retained record | An active or soft-deleted member/equipment record still stored in the dataset. Soft-deleted records can be restored without changing their identity or closed loan history. |
| Inventory group | A derived group of physical equipment units with matching normalized category and name. Group totals distinguish available, assigned, unavailable, and deleted units; a group is not a separately persisted equipment identity. |
| Transaction candidate / candidate | In transaction discussions, the isolated model copy on which a mutation runs, created by `Model.createTransactionSnapshot()`. It has independent mutable collection containers and is discarded on failure. Autocomplete uses completion candidates to mean suggested input values instead. |
| Transaction | A record-changing operation staged on a candidate, persisted atomically, then published to the live model. Batches and undo/redo succeed or fail as a whole. |
| Publication | Updating the existing live observable model with the successfully saved candidate's records and matching session history. It happens after persistence succeeds. |
| History state | A private stored copy of the records retained for undo/redo. A selected history state is copied into a candidate for navigation; it is not the live model or a user-exported file. |
| Backup snapshot | A proposed complete local export of persistent records and their relationships for later recovery. Export and restore-backup are Future capabilities (US03, US40, UC07); this term does not denote a transaction candidate or a session history state. |
| Display request | An immutable navigation intent returned with command feedback. The UI uses it to choose a directory, group, or detail view without parsing message text. It is not persisted record data. |
| Compact UUID label | A shortened UUID shown as a visual aid in an inventory group. It can collide with another label and is not a command identifier; exact-unit commands use the full UUID. |
| Atomic operation | An operation whose changes succeed together or leave the preceding consistent state intact. |
| MVP | Minimum viable product; the delivered baseline capabilities, which are a subset of the full requirements recorded here. |

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Use Java 25 and the released `Laplace.jar` in a separate writable folder. Never use real inventory.
Start each section in its own disposable folder unless a step explicitly continues the preceding one.
Launch with `java -jar Laplace.jar`. A first run supplies sample records; it does not save them until
a successful mutation. The status area identifies the data file, normally `data/addressbook.json`.
Close Laplace before editing JSON, keep an untouched backup, and restore it between invalid-file cases.

Replace `UUID`, `NUS_ID` and `DATE` placeholders with full displayed identities and actual ISO dates.
For loan examples, use the computer's current local date unless a fixed historical date is specified.
After a rejected command, verify that records, selection and criteria remain unchanged. If a data file
already exists, compare its bytes with a backup taken immediately before the rejection.

These scenarios are a starting point for exploratory testing. Source-level failure injection and
large-fixture capacity checks belong in the [Testing guide](Testing.md#capacity-and-failure-injection).
They are not prerequisites for testing the released JAR.

### Launch and shutdown

1. Copy the JAR into an empty folder and launch it.
   **Expected:** Light-mode GUI with sample members and equipment, three open loans and two completed
   loans. The Loans tab shows `Showing 3 of 3 open loans; 2 of 2 completed loans`.
2. Inspect the sample histories. **Expected:** The speaker records FAIR at issue and return; the tripod
   records GOOD at issue and DAMAGED on return and is UNAVAILABLE. The assigned camera has an unknown
   issue condition. Samples are not appended when an existing valid dataset is loaded.
3. Create a saved records fixture with
   `member add nus/A8400098X name/Preference Probe phone/91112222 email/preferences@example.com`.
   **Expected:** Success and an existing data file at the status area's path. Resize/move the window,
   switch to dark mode, enter `exit`, and relaunch from the same folder.
   **Expected:** Normal exit and restored geometry/appearance. Test `help`, close Help, then exit using
   the window control too. For rejected startup files, follow [Invalid saved data](#invalid-saved-data).
4. With Laplace closed, back up both the saved data file and the valid dark-mode `preferences.json`.
   Keep these backups unchanged for comparison and restoration. Replace the working `preferences.json`
   with `{"guiSettings":null}`.
   Relaunch. **Expected:** Default light appearance/geometry, a preference-loading log diagnostic,
   default settings saved to preferences, and domain-data bytes matching the records backup. Close Laplace.
5. While Laplace is closed, replace the working `preferences.json` with `{}` and relaunch.
   **Expected:** Missing settings use defaults; domain-data bytes still match the records backup.
   Close Laplace, restore the valid preferences backup from step 4, and relaunch.
   **Expected:** The backed-up dark mode and geometry return; records remain unchanged.

### Member registration, viewing, deletion and restoration

1. In fresh sample data, enter
   `member add nus/A8400001X name/Ravi s/o Kumar phone/91112222 email/ravi@example.com`.
   **Expected:** A new member, selected by NUS ID, with the exact displayed name and contacts. Repeat
   the same NUS ID with different details. **Expected:** Duplicate-ID rejection and no second member.
2. Run `member view nus/A8400001X`, then `member view nus/a8400001x`.
   **Expected:** The same member details; exact lookup ignores ID case. A malformed ID or a well-formed
   nonexistent ID is rejected. A partial ID is not a member-view shortcut.
3. Edit with `member edit nus/A8400001X name/Mary D/O Tan`. **Expected:** New name, same NUS ID and
   contacts. Edit with `member edit nus/A8400001X phone/91234567`. **Expected:** The edit is saved and
   followed by `Possible duplicate(s): A0123456X`. Add another member with `name/Ravi S/o Kumar` and a distinct valid ID/contact set.
   Repeat the edit with `name/Muthu a/l Raju` and `name/Priya A/P Suresh`.
   **Expected:** Case-preserved patronymic tokens. `name/s/o`, `name/Tan s/o`, `name/d/o Devi`,
   `name/a/l` and `name/Priya a/p` reject without saving. `name/Ravi w/o Kumar` remains invalid;
   only standalone `s/o`, `d/o`, `a/l` and `a/p` are exceptions.
   Try `equipment add name/Mic category/Audio condition/GOOD notes/Use w/ XLR`;
   **Expected:** `Unknown parameter: w/` and no equipment added.
4. Add a GOOD test unit with `equipment add name/Member Test Unit category/Testing condition/GOOD`.
   Copy its full UUID, then enter
   `loans add uuid/UUID nus/A8400001X assigned/DATE expected/DATE` with today's date in both fields.
   **Expected:** One open loan and one item held by this member.
5. Run `member delete nus/A8400001X` while the loan is open.
   **Expected:** Rejection stating the member holds 1 item; member, loan and saved file remain unchanged.
6. Return the unit with `loans return uuid/UUID returned/DATE condition/GOOD`, then run the deletion again.
   **Expected:** A retained deleted member with the same NUS ID, contacts and completed history. The
   record remains in `member list` and `member search status/deleted`. New loans to it are rejected.
   Deleting a member does not select them automatically; an existing selection is retained if still
   in the results. An active-only search removes the deleted member from its results.
7. Repeat deletion, then try an edit while deleted. **Expected:** Both reject without changing data.
   Run `member restore nus/A8400001X`. **Expected:** The same identity, details and history become active;
   a new loan to the member is permitted. Restoring again or restoring a nonexistent ID rejects.
8. Exit and restart after a successful change. **Expected:** Exact names, identity, deletion state and
   open/completed loan references survive. Restore never creates a replacement identity.
   An earlier valid file containing `s/o`, `D/O` or `Tan s/o` as a saved name still loads unchanged;
   editing another field preserves that name, while entering it as a new name is rejected.

### Member search, filters and sorting

1. In fresh sample data, add
   `member add nus/A8400001X name/Ravi s/o Kumar phone/91112222 email/ravi@example.com` and
   `member add nus/A8400002X name/Mary d/o Tan phone/93334444 email/mary@example.com`.
   Add a GOOD test unit and assign it to Ravi using today's date. Keep Mary without loans.
   Run `member search q/840000 holding/yes sort/held`.
   **Expected:** Ravi is the only matching holder. Held-count sorting uses name/NUS ID tie-breakers.
2. Run `member search q/840000 holding/no`, then `member search q/Ravi`.
   **Expected:** Mary alone, then Ravi alone. The second search replaces the previous holding filter;
   it matches a substring within a member field rather than accumulating old criteria.
3. Run `member search q/9111-2222`, then `member search q/9111 2222` for the member registered above.
   **Expected:** The same phone match. `q/-` must not match every member through an empty phone query.
4. Delete Mary with `member delete nus/A8400002X`. Run `member search q/840000 status/deleted`,
   `member search q/840000 status/active`, and `member search q/840000 status/all sort/held order/desc`.
   **Expected:** Mary alone, Ravi alone, then Ravi before Mary. Name/NUS ID ties are reversed with
   descending order. Deleted members have no open loans.
5. Run `member search holding/maybe`, `member search sort/condition`, and `member search q/a q/b`.
   **Expected:** Accurate errors and no criteria/data change. `member list` resets the search and
   displays active and deleted members together in default name/NUS ID order.

### Member email validation

1. Copy and paste
   `member add nus/A8400099X name/Kelvin Probe phone/91234567 email/K@example.com`.
   The first email character is **U+212A KELVIN SIGN (`K`)**, not ASCII capital K (U+004B).
   **Expected:** Non-ASCII email rejection and no new member or save. Typing an ordinary K tests a
   different, valid input; use the literal pasted command for this rejection case.
2. Replace that character with ASCII `K` and resubmit. **Expected:** Success, stored `k@example.com`.
   Try `member edit nus/A8400099X email/K.updated@example.com`, again pasting U+212A.
   **Expected:** Rejection with unchanged contacts and bytes.
3. Edit using `email/K.UPDATED@EXAMPLE.COM`, exit and relaunch.
   **Expected:** Lowercase email, same NUS ID and unchanged loan references.

### No-argument commands

1. Enter `member list extra`, `equipment list extra`, `loans reset extra`, `undo 2`, `redo count/2`,
   `help extra` and `exit extra`. **Expected:** Each reports `This command takes no arguments.` with
   its accepted usage. Help stays closed; invalid Exit does not close the application.
2. Enter `loans list extra`. **Expected:** `Unexpected text before parameters.` with loan-list usage;
   this command accepts optional filters and therefore retains its parameter-specific diagnostic.
3. Enter the bare commands from step 1, using `exit` last. **Expected:** List/reset navigate normally,
   undo/redo operate or report empty history, and Help opens. Close Help before entering bare `exit`.
   **Expected:** Laplace closes after the final command; there are no subsequent commands in this scenario.

### Unicode command separators and aliases

1. Paste `member view nus/A0123456X` with EM SPACE (U+2003) or nonbreaking space (U+00A0) replacing
   each space, including surrounding whitespace. **Expected:** The existing member opens. Repeat with
   member/equipment lists and `loans list`; whitespace-only extra input remains accepted.
2. Enter `Member LIST`, `LOAN LIST`, and `Members View nus/A0123456X`.
   **Expected:** Canonical commands execute; command words and actions ignore case. Parameter prefixes
   retain their own rules: `MEMBERS ADD NAME/Camera` rejects the uppercase `NAME/` marker.
3. Try `equipments list`, `loan returns`, duplicate parameters and unknown parameters.
   **Expected:** Their normal unknown-command/action/parameter diagnostics, with no mutation.
4. Type `LOAN LI` and press Tab; type `Members` and press Tab; invoke Ctrl+Space after `members `.
   **Expected:** Canonical `list`, `member` and member-command suggestions without execution.

### Grouped equipment, command assistance and appearance

1. Run `equipment add name/UI Camera category/Photo condition/GOOD count/4`. **Expected:** one group containing
   four distinct UUIDs. Expand/copy Last result to capture all four. Open the group and select one unit;
   Copy UUID must copy its complete UUID. Register another matching unit; **Expected:** five units in the same group.
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
3. Rename the damaged unit, delete it, then restore it with the documented equipment commands. **Expected:**
   regrouping without UUID changes. Check deleted records never inflate available counts.
   Open a unit with long notes/history and scroll to the bottom. View a different UUID; **Expected:** the group
   heading and unit identity at the top. Refresh or reopen the same UUID; **Expected:** its scroll position retained.
4. Open a member's details, copy contact fields, and use a related equipment link. Switch tabs and use
   Back to results. The previous filter persists until Reset search or a list command.
5. Use a command preview. **Expected:** only input preparation; review and press Enter. Test unmodified
   history up/down with an unfinished draft. Shift/Ctrl/Alt/Meta+Up/Down should retain normal text editing
   without recalling history. Test Ctrl+Space completion, Escape dismissal and keyboard focus navigation.
6. Open Loans. Verify due/overdue labels against the computer's local date and the stored due date.
   Prepare a return; verify the UUID/date and that the loan remains open until submission.
7. Run `mode dark`, open Help, then switch back using the header appearance button. Repeat list/table selection, previews and errors
   in each appearance. Exit normally and relaunch; the selected mode persists. `mode sepia` is rejected.
8. Review at 1920×1080 with 100%/125% scaling and 1280×720 with 150% scaling on supported systems.
   Check wrapping, scrolling, visible focus and contrast. Logical-size snapshots alone do not verify OS scaling.
   At 1280×720 with 150% scaling, run `loans list`, `member list`, and `equipment list` with sample data.
   Without scrolling, check one complete open-loan card, including status, both identities, all dates,
   issue condition, observed return condition and Prepare return command; also check two directory rows.
   Open an equipment group with multiple units and check two unit rows are visible before scrolling.
   Scroll to all remaining rows and expand/collapse a long Last result; Copy must
   retain the complete feedback. Resize taller and confirm normal spacing returns.
   At 1280×720 with 150% scaling, open Help and scroll to the end; the last example's Use command button
   must be visible without moving the window.

### Retained inventory totals during filtering

1. In a disposable folder, close Laplace and create `data/addressbook.json` containing
   `{"persons":[],"members":[],"equipment":[],"loans":[]}`. Launch, add two GOOD units named `Mixed`
   in category `Photo` and one GOOD unit named `Retired` in category `Legacy`. Delete one Mixed unit
   and the Retired unit by full UUID; create no loans.
   **Expected:** Three retained units, one active AVAILABLE unit and two deleted units.
2. Run `equipment search status/active`, `equipment search status/deleted`, and `equipment list`.
   **Expected:** Displayed counts of one, two and three respectively. Both retained groups remain in
   the summary: Mixed has one available and one deleted unit; Retired has one deleted unit.
3. Inspect the totals after each filter. **Expected:** Equipment Total stays three and active
   availability stays one available, zero assigned and zero unavailable. Filters change matching
   units, not retained totals. Deleted records never inflate active availability.
4. Add `equipment add name/ΟΣ category/Audio condition/GOOD` and
   copy its full UUID. Add `equipment add name/οσ category/Audio condition/GOOD`.
   **Expected:** The second add succeeds with `Possible duplicate(s): FIRST_UUID`, where `FIRST_UUID`
   is the copied identity; the two physical units retain distinct UUIDs. Run
   `equipment view category/Audio name/οσ`, then repeat with `name/ος` and `name/ΟΣ`.
   **Expected:** All spellings open one group containing both distinct UUIDs. Repeat
   `equipment search category/Audio q/ΟΣ` after restart; the same group and stored spellings remain.

### Tab completion across current commands

1. In a disposable dataset, type `mem` and press Tab. **Expected:** `member`, with the caret at its end.
   Add a space and cycle with Tab, then Shift+Tab. **Expected:** alphabetical subcommands and reverse cycling.
2. Try `member restore n`, `equipment edit co`, `equipment add cou`, and `loans return co`, pressing
   Tab after each. **Expected:** `nus/`, `condition/`, `count/`, and `condition/` respectively.
3. Try `member search holding/y`, `equipment search availability/av` and `equipment search order/de`.
   **Expected:** `YES`, `AVAILABLE` and `desc`. Enter the completed search; check the filtered directory.
4. In `loans return uuid/FIRST_UUID,  PARTIAL_SECOND_UUID returned/DATE`, position the caret after the
   partial UUID and press Tab. **Expected:** only that UUID is replaced; the first UUID, spacing and return
   date remain intact. Candidates omit UUIDs already present. Repeat with a space after the comma.
5. Repeat a name completion with an em space (`U+2003`) or nonbreaking space (`U+00A0`) before the
   following `phone/` field. **Expected:** the phone field and original separator remain, with the caret
   immediately after the completed name. Check the same behavior in a comma-separated UUID list.
6. Check an untouched following field, no matching input, selected text and Shift+Tab outside a cycle.
   **Expected:** surrounding input remains intact and ordinary focus traversal remains available.
7. Add or edit a record, then retry completion. **Expected:** current values are suggested. Tab alone never
   changes records or saves; Enter uses normal parser validation. Restart after a valid mutation and
   verify the saved records. New IDs, dates, numeric counts and notes remain manually entered.
8. With an existing member named `Alex Tan`, type `member edit name/Alex  T phone/91234567`
   using two spaces between `Alex` and `T`. Place the caret after `T` and press Tab.
   **Expected:** `name/Alex Tan`, retaining the following phone field and its separator. Repeat with
   an em space (`U+2003`) and nonbreaking space (`U+00A0`) inside the partial name. The caret ends
   immediately after the stored name; Tab does not execute the command or rewrite surrounding input.
9. Register equipment named `Camera Cover` in category `Photo Cover`. Type
   `equipment add name/Camera co category/Photo`, place the caret after `co`, and press Tab.
   **Expected:** `name/Camera Cover`, with the following category field unchanged and no command executed.
   Repeat with `equipment add category/Photo co`; **Expected:** `category/Photo Cover`. Check `Camera Case`
   and `Camera Notes` too. When no stored value matches, an unfinished token such as `notes/Keep co`
   still offers unused field prefixes; a trailing space starts a new field.
10. After one or several valid comma-separated UUIDs, complete `loans add uuid/UUID nu`,
    `loans return uuid/UUID re`, and `loans return uuid/UUID co`. **Expected:** `nus/`, `returned/` and
    `condition/` without changing the UUID list or later fields. Recheck UUID completion immediately
    after a comma; it must still offer unused UUIDs rather than a new parameter.
11. Type `loans return co` and press Ctrl+Space. **Expected:** `loans return condition/` is offered.
    Repeat after an existing `uuid/UUID` field; that field remains unchanged. After a used
    `condition/GOOD` field, Ctrl+Space must not offer `condition/` again. Compare with Tab and the
    loan-return template in Help; both expose the same optional return condition. Preparing a template
    or accepting a suggestion must not execute a return.

12. Add `member add nus/A8400001X name/ΝΙΚΟΣ phone/91234567 email/nikos@example.com`.
    Search with `member search q/Σ` and `member search q/νικοσ` before and after restart.
    **Expected:** Both find the member. `member edit name/νικοσ` followed by Tab suggests `ΝΙΚΟΣ`
    without executing or saving. Forward/reverse cycling still works.
13. Register a member named `Ravi s/o Kumar`. Type `member edit name/Ravi s/o K phone/91112222`,
    put the caret after K and press Tab. **Expected:** The stored name completes while the phone field
    and caret placement are preserved. Repeat with a stored `Mary D/O Tan` name and a partial D/O name.

### Rejection of retired commands

1. Run the JAR from a separate empty writable folder to get sample members, equipment and loans.
   Use only this temporary dataset for these checks. Run `member list`, `equipment list`, and
   `loans list`; record the identities and counts. Copy the saved data file for comparison if present.
2. Enter each of `add`, `edit 1 name/Changed`, `delete 1`, `clear`, `clear 3`, `find Alex`, `list`,
   and `list members`. **Expected:** `Unknown command.`; the current view/selection and all records stay
   unchanged. An existing data file must remain byte-for-byte unchanged; rejected input must not
   create a data file if none existed.
3. Run `member list`, `equipment list`, and `loans list` again. **Expected:** normal directory navigation
   and the same records and loan identities as before. These supported controls are separate from
   the rejected-input file comparison above.
4. Restart the application. **Expected:** saved records still load normally. Existing JSON files with a
   `persons` array remain readable; no migration or removal of that array is required by this change.

### Unicode member names

1. In disposable data, add members named `அருண்`, `Anne O’Connor`, and `José Tan` (the `e` followed
   by combining acute accent U+0301), using a different valid NUS ID for each. **Expected:** success; the last
   name is stored/displayed as NFC `José Tan`. Ordinary names such as `Anne-Marie O'Connor` still work.
2. Give a member an open loan, then use `member edit nus/NUS_ID name/அருண்` and another valid Unicode
   name. **Expected:** the same NUS ID, contacts and loan references after each edit and after restarting.
3. Try a leading combining mark, a mark after a space, a punctuation-only name and a name longer than
   100 code points after normalization. **Expected:** an error without saved-data changes. Equivalent names
   at the limit must be counted after NFC composition, not by UTF-16 units or the raw pasted length.
4. Load an existing file with a decomposed accented member name and repeated spaces. **Expected:** exact saved
   spelling to remain when loading, saving, and editing only the phone/email. New name input uses NFC.
5. Search using both `member search q/José Tan` and `member search q/José Tan`, then press Tab after
   `member edit nus/NUS_ID name/José Ta`. **Expected:** both searches find the same member; Tab inserts
   the stored name and preserves following fields. Repeat with a legacy decomposed name. Queries and
   completion must preserve the saved file and original stored spelling; unaccented `Jose Tan` does not match.

### Adding a batch of equipment

1. In a separate temporary dataset, run
   `equipment add name/Batch Test Camera category/Photography condition/GOOD count/3 notes/Shared kit`.
   **Expected:** one new group for Batch Test Camera (or an extension of an existing matching group);
   feedback lists all three UUIDs and the full equipment directory opens. Open the group and verify
   that it now contains N + 3 physical-unit rows, where N is the number of units already in a matching
   group (zero for a new group). Confirm every row has a distinct UUID, the three new units are
   AVAILABLE with identical supplied details, and the three feedback UUIDs are present.
2. Repeat without `count/`, then with `count/1`. Each command adds one unit and opens its details.
3. Try `count/0`, `count/-1`, `count/101`, `count/1.5`, a blank `count/`, and `count/2 count/3`.
   **Expected:** an error, no new records, and no save. Correct the input and retry.
4. Run `equipment search q/NoMatchingBatch`, then add a valid batch. **Expected:** the old filter resets
   and the directory includes the new units. Repeating a batch retains distinct UUIDs and warns about
   pre-existing matching units, not the new units within the same batch.
5. Exit and relaunch from the same folder. **Expected:** all units retain their original UUIDs and details.
   Use `equipment view uuid/UUID` with each actual generated UUID to verify independently.

### Editing, deleting and restoring equipment

1. Add two matching units with `equipment add name/Maintenance Camera category/Photography condition/GOOD count/2`.
   Record both UUIDs as A and B. Replace these placeholders in all following commands.
2. Run `equipment edit uuid/A name/Updated Camera condition/FAIR notes/Checked` then `equipment edit uuid/A notes/`.
   **Expected:** A's UUID unchanged, updated details and cleared notes; B remains unchanged. Restart and verify.
3. Try `equipment edit uuid/A`, `equipment edit uuid/A name/`, and a no-change edit. **Expected:** an error and no save.
4. Assign A to an active test member with valid dates. Edit its condition to DAMAGED: **Expected:** ASSIGNED until
   return, then UNAVAILABLE. Attempt deletion while assigned: **Expected:** refusal and no record/history changes.
5. Return A, then `equipment delete uuid/A`. **Expected:** the retained unit contributes to its group
   deleted count in `equipment list`; open the group and include deleted units to see `Deleted` in the Status column.
   Details identify deleted equipment and show the restore command. No new loans are permitted, and the unit is
   excluded from active search/inventory totals. `equipment search status/deleted`
   should find it. Restart: the flag, UUID, details and closed loan records must remain in the saved data.
6. Try editing/deleting A again: **Expected:** rejection. Run `equipment restore uuid/A`: **Expected:** the same UUID
   and details, still DAMAGED/UNAVAILABLE. Repeating restore is rejected. Repair with an edit to GOOD and
   verify that assigning a new loan succeeds. B must remain unchanged throughout.
7. Check `equipment search status/all`, direct UUID navigation and query refresh after rename/restore.
   Automated transaction tests separately inject partial writes/failed atomic moves; do not simulate
   filesystem failures against a real user dataset.

8. Assign two disposable units on different past days and return both on a date between their assignment
   dates. **Expected:** The error names the offending unit, full UUID and both dates; neither loan closes
   or changes condition. Repeat for one unit. Assignment-day and today returns are valid boundaries.

### Loan chronology and retained history

1. Add an available test unit and two active members. Assign the unit to the first member on
   `2026-01-10`, with expected return `2026-01-20`, then return it on `2026-01-20`.
   **Expected:** One completed loan, no current holder, and the unit available again.
2. Try assigning that UUID to the second member on `2026-01-15`.
   **Expected:** Rejection naming the last actual return date, unchanged records/file and unchanged
   undo/redo availability. Retry on `2026-01-20`; same-day handoff succeeds.
3. Repeat with a two-unit batch whose last unit has the conflicting completed history.
   **Expected:** No partial assignment of the first unit. Correct the date and verify both succeed.
4. On a separate unit, repeat assignment and return twelve times using today's date, then open its
   details and Loans and scroll to the oldest record. Filter Loans to no matches, then reset.
   **Expected:** All twelve completed records remain accessible and counts return after reset.
   This small manual case checks browsing; it does not establish the NFR07 capacity/timing target.
5. Assign eleven distinct available units to one test member, then open the member's details.
   **Expected:** scroll within Current holdings to reach all eleven UUIDs and their View equipment
   actions, ordered by expected date, name and UUID. Return one unit and reopen the member: ten
   holdings appear inline, with the returned unit retained in history. Changing members shows only
   the selected member's holdings. These navigation operations do not change the saved data.

### Stored loan date validation

1. In separate copies of a saved file, change each loan date field to `0000-01-01`, `-0001-01-01`,
   or `+10000-01-01`, then relaunch.
   **Expected:** Invalid-data recovery without rewriting source bytes. Restore the backup each time.
2. Test valid years `0001` and `9999` in internally consistent loan intervals, including an open loan
   with no condition observations. **Expected:** Loading remains supported; storage does not reject
   otherwise valid historical records solely because their dates differ from today.

### Saving data

#### First save and repeated loan cycles

1. Launch in a fresh empty folder. **Expected:** Sample members, equipment and **five loans: three open
   and two completed**. Add a uniquely identified member, exit and restart. **Expected:** That member
   and all sample records persist. A valid empty file instead loads empty, without injecting samples.
2. Add a fresh available unit U. Assign it to member A with today's date for assignment/expected return,
   return it today with `condition/FAIR`, and assign it to member B today. Restart.
   **Expected:** A's completed history, B's current holding, the same UUID and ASSIGNED availability.
3. Return U with `condition/DAMAGED` and restart.
   **Expected:** Two completed loans for U, preserved issue/return observations, no holder or current
   holding, and UNAVAILABLE equipment. Missing historical observations remain unknown.
4. Batch-assign a duplicate UUID, then a valid UUID followed by a nonexistent UUID.
   **Expected:** Whole-command rejection and unchanged file. Assign two units successfully, then return
   one alongside a unit with no open loan. **Expected:** Both original loans stay open. A corrected
   return closes both; repeating it fails. Restart and verify the resulting records and conditions.

#### Read-only navigation and command-log privacy

1. Add a test unit to save the sample dataset, then close Laplace and record the data bytes/time.
   Relaunch and run member/equipment/loans lists, a member view and an equipment view by full UUID.
   **Expected:** Correct navigation and holders, with unchanged domain-data bytes/time.
2. Run `help`, close Help, and `exit`. **Expected:** Normal utilities, unchanged domain-data file;
   preferences may save. Repeat in a fresh folder without mutations. **Expected:** Samples remain
   browsable and no `data/addressbook.json` is created.
3. Add/edit a synthetic member, search for their name, and submit a malformed command containing
   a synthetic phone/email. Inspect `addressbook.log.*` in the JAR folder.
   **Expected:** Input-received diagnostics without raw commands or member contact values.
4. In a fresh disposable JAR folder, save a synthetic member, close Laplace, and replace an equipment
   array value with the unquoted token `INVALID`. Relaunch and inspect the new log entries.
   **Expected:** recovery guidance and unchanged data bytes; logs identify the file/failure without the
   member's name, phone, email or a JSON source excerpt. The main editable window must not open.
5. Restore the valid synthetic file, then replace a member's `nusId` with `privacy-audit@example.com`
   while keeping the JSON valid. Repeat separately with that value in an equipment `uuid`.
   **Expected:** the same log privacy and unchanged-file guarantees; recovery details still identify
   the invalid record and field. No editable main window opens.

#### Invalid saved data

1. Save synthetic members, equipment and both open/completed loans. Close Laplace; keep an untouched
   byte-for-byte backup. Test separate copies containing malformed JSON (`{broken`), a JSON `null`
   root, a valid object followed by another object/garbage, an invalid email, a missing loan reference,
   two open loans for one UUID, and an explicit `"deleted": null` flag.
   **Expected:** A recovery error names the data file and explains preservation, backup and repair.
   After acknowledgment, startup terminates unsuccessfully without an editable main window; source
   bytes remain unchanged. Ordinary trailing whitespace after a valid object is accepted.
2. Restore the untouched backup while Laplace is closed and relaunch.
   **Expected:** Original identities, contacts, references and retained history. A genuinely missing
   file starts samples; `{"persons":[]}` loads an empty domain dataset.
3. Remove `deleted` from member/equipment records and remove loan `issueCondition`/`returnCondition`.
   **Expected:** Records load as active, missing observations show Not recorded, and identities,
   dates and references survive a successful save/reload.
4. Insert a second `"loans": []` property at the root, then separately a second `returnedDate` in a loan.
   **Expected:** Duplicate-field diagnostic and unchanged source bytes. Remove the duplicate and
   verify successful loading/save/reload with the original open/completed history.
5. In separate copies, set a member's `name` to `""` or `"R@chel"`, then their `phone` to
   `"+65 8000-0001"` or `"+651234"`.
   **Expected:** The recovery diagnostic identifies `members[0]` (for the first member) and its NUS ID,
   and gives the same member name or phone rules as `member add`/`member edit`. Source bytes remain
   unchanged. Restore valid values and verify loading succeeds. Older saved names containing digits
   and digit-only phones of at least three digits remain accepted for compatibility.
6. Rename a completed loan's `returnedDate` to `returnDate`, then separately `returnCondition` to
   `returnCondtion`. **Expected:** `Unknown loan field` and the rejected field name; unchanged bytes.
   Restore correct spellings and verify the completed loan and other records load unchanged.

Save-failure rollback can be checked using the disposable read-only-file scenario under
[Atomic-save access checks](#atomic-save-access-checks). Deterministic partial-write and replacement
failures are developer tests documented in the [Testing guide](Testing.md#capacity-and-failure-injection).

### Loan filter visibility

1. In saved sample data, record the data bytes and run `loans list nus/a023 uuid/400004`.
   **Expected:** Both filter fields show the criteria, matches use identity substrings in open/completed
   sections, and displayed/total counts explain hidden records.
2. Open details, then run bare `loans list`. Edit a filter using the GUI and run bare list again.
   **Expected:** Criteria persist in both cases. Run `loans list nus/a012`; the omitted UUID field clears.
3. Try repeated `nus/` markers and `loans reset extra`.
   **Expected:** Error/input remain for correction; previous view, criteria, records and file stay unchanged.
4. While a borrower/UUID filter is active, assign and return a batch for another borrower.
   **Expected:** Refresh retains both filters and explains newly hidden loans in displayed/total counts.
5. Run `loans reset`, then apply a filter and press Reset filters in the GUI.
   **Expected:** Each clears both fields and reveals total retained history without changing records.
6. Repeat valid filter/reset commands with a disposable saved data file set read-only using the next
   section's method. **Expected:** Filters work with unchanged file bytes and undo state. Restore write
   access afterward. Bare list still preserves criteria; explicit fields replace both criteria.
7. Paste a valid borrower ID and UUID with surrounding nonbreaking spaces (`U+00A0` and `U+202F`)
   into the panel fields, then repeat through `loans list nus/TEXT uuid/TEXT`. **Expected:** identical
   matching open/completed rows. Whitespace-only filters reveal all loans; internal spaces do not disappear.

### Undo and redo

Use temporary data. Do not test against a real inventory.

1. Add a member, then enter `undo`. **Expected:** `Undid: member add.`, removal of that member and a refreshed
   directory. Enter `redo`; **Expected:** `Redid: member add.` and the identical member restored.
2. Register three units with one `equipment add ... count/3` command. Capture all UUIDs. One undo must
   remove all three; one redo must restore exactly those UUIDs.
3. Issue and return a unit with a recorded return condition. Undo the return: **Expected:** the original open
   loan, issue condition and holder restored. Redo: **Expected:** the recorded return date and condition.
4. Make twelve successful changes. Only ten undos are available; the next reports `Nothing to undo.`.
   Redo ten times; the next reports `Nothing to redo.`.
5. Undo, run queries and `mode dark`, then redo. It must remain available and appearance must stay dark.
   Undo again and make a new successful mutation; redo must now be unavailable. Rejected/no-change
   edits must leave redo available. `undo 2` and `redo count/2` must be rejected without changing data.
6. Restart after an undo. The restored records must still load, but undo and redo must both be unavailable.
   Automated tests inject failed saves and verify records, history and prior file bytes survive for retry.
7. Open Help and locate the `undo` and `redo` examples. Use each example; **Expected:** the exact command
   prepared without execution. Type `und`/`red` and test both Tab and Ctrl+Space; **Expected:** `undo`/`redo`
   suggestions. Press Enter separately to invoke history. Both no-argument utilities must have
   command-name highlighting in previews.

### Command-only equipment browsing

Use disposable records or the fresh bundled samples.

1. Search for an available equipment group. Run `equipment view category/CATEGORY name/NAME` with its
   displayed fields. **Expected:** the matching unit table without an implicit unit selection, including when
   several units share both category and name.
2. Type `equipment view uuid/` followed by the beginning of a displayed unit's UUID. Press Tab to cycle
   through full UUIDs, then Enter separately. **Expected:** the intended unit's details and full UUID.
3. Assign that UUID with `loans add`, view it again, return it with `loans return`, and view it again.
   **Expected:** the same UUID, correct holder/dates, then AVAILABLE (for GOOD/FAIR), and retained history.
   Filters and sorting remain active. Full UUIDs can be completed with Tab in loan commands too.
4. Filter out this unit and repeat its full UUID view command. **Expected:** its exact details with Show all
   units enabled and unchanged search criteria. Missing groups and incomplete/mixed forms preserve the
   previous view, selection, data and history. Try `equipment view uuid/UUID unknown/value` with a real
   UUID. **Expected:** `Unknown parameter: unknown/`.
5. Edit the last unit's group name/category and retry the old group pair. **Expected:** no matching group;
   its full UUID still resolves. Help, Tab and Ctrl+Space offer category/, name/ and uuid/.

### Atomic-save access checks

1. In a disposable JAR folder, save a member change, exit and back up the data file.
   On POSIX, run `chmod 644 data/addressbook.json`, make another valid record change and exit.
   **Expected:** The saved file retains mode `0644`. On Windows, inspect the file's Security tab and
   record its existing owner/access entries; a successful change must preserve those entries.
2. With Laplace closed, run `chmod 444 data/addressbook.json` on POSIX. On Windows, run
   `attrib +R data\addressbook.json` from the JAR folder. Relaunch and attempt a valid member edit.
   **Expected:** Save failure, unchanged member/loan data and byte-for-byte unchanged saved file.
3. Run a query or open Help while the file is read-only.
   **Expected:** Read-only operations work and do not save. Use `exit` normally.
4. Restore write access using `chmod 644 data/addressbook.json` or `attrib -R data\addressbook.json`.
   Relaunch, retry the edit, exit and restart. **Expected:** The change now persists, with original
   file access settings. Tests for inherited Windows ACLs require Windows and are separate from this
   DOS read-only-attribute check; see the [Testing guide](Testing.md#capacity-and-failure-injection).

### Batch loan rejection checks

1. Add one GOOD unit and one DAMAGED unit. Record both UUIDs, and choose two valid-format UUIDs that
   do not occur in `equipment list`. Assign all four to an active member with valid dates.
   **Expected:** Three per-unit errors, including each invalid UUID; no loan created for the valid unit.
   Expand Details and Copy; the complete diagnostics remain available.
2. Assign the GOOD unit alone, then return it together with the unassigned DAMAGED unit and one
   nonexistent UUID. **Expected:** Two errors; the valid loan stays open and its condition is unchanged.
3. Supply a future shared assignment/actual-return date.
   **Expected:** Its shared date error is reported before per-unit diagnostics. For historical overlap,
   use [Loan chronology and retained history](#loan-chronology-and-retained-history).

### Design contract checks

Use isolated synthetic records and close Laplace before editing JSON. Keep the original fixture for
comparison after each rejection.

1. **Prepared return condition:** On an open loan, choose DAMAGED and prepare the return. **Expected:**
   `condition/DAMAGED` in the input. Choose Not recorded and prepare again; **Expected:** no condition field.
   Submit separate examples and verify recorded versus unknown historical conditions.
   With 100 open loans, select DAMAGED on the first row, scroll to the bottom and back, then prepare
   again; **Expected:** DAMAGED is still selected and included in the command. Other loans keep their
   own choices. Filter the row out and reset filters; its choice must remain. Submit the return and
   restart; **Expected:** the saved observation and equipment condition are DAMAGED. Mark the repaired
   unit GOOD with `equipment edit uuid/UUID condition/GOOD`, then reassign it; **Expected:** the new loan
   starts at Not recorded. Scrolling and preparing alone must not save data.
2. **Condition compatibility:** Load loans with each defined issue/return condition, including DAMAGED
   and UNDER_REPAIR. Save and reload; identities and observations must remain unchanged. Repeat with
   `good`, ` FaIr ` and `under_repair`; output must use uppercase names. `BROKEN` must reject loading.
3. **Batch return feedback:** Return two units borrowed by different members. Expand and copy feedback;
   each name, full UUID and NUS ID must match that unit's retained loan.
4. **Clock rollback:** Load an assignment/actual return dated in 2099. **Expected:** a warning in the log and
   unchanged recorded dates. A future expected date alone must not produce this warning.
5. **Conversion diagnostics:** In separate fixtures, insert an invalid loan date, missing reference,
   duplicate identity and invalid condition. **Expected:** a collection index and available record identity,
   with rejected source bytes preserved.
6. **Legacy collections:** Test `{"persons":[]}` with members/equipment/loans individually omitted,
   null and empty. Each must load as an empty domain collection and save as an array. For `persons`,
   absent/null must reject; empty/populated arrays must load and retain their records on save/reload.
7. **Editor marker:** Prepend one UTF-8 byte-order mark (EF BB BF) to valid JSON. **Expected:** successful
   loading and the same records after saving/reloading. A second leading marker, a null root or
   trailing content must reject without changing source bytes. A marker inside a string remains data.
