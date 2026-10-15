# Laplace shell verification

This implements [issue #91](https://github.com/AY2627S1-CS2103T-F15-2/tp/issues/91) around the
components now available on team master `0efb388f8a0daa180481dc6f049705bbd9a93add` (merged details, loans and all ten routes).
The window mounts the real member/equipment lists, cards, summaries, MemberDetailsPanel,
EquipmentDetailsPanel, Loans panel and legacy Persons list. The shell does not modify those owned
components or change their command routers; it consumes the merged implementations. [ReviewResponse.md](ReviewResponse.md) records all
six review items and the open [final acceptance tracker #75](https://github.com/AY2627S1-CS2103T-F15-2/tp/issues/75).

## Behavior and ownership

The first screen is Members with the first sorted identity selected and the summary visible.
The header has Members, Equipment and Loans buttons. Production startup now measures mounted panels
before restoring the divider, matching UiManager's show-before-fill order. Directory columns start at 28%/72% and
retain that proportion on resize. Feedback and command input remain below the scrollable content.
The Menu exposes Help, legacy Persons and Exit; F1 opens Help even from command input.
Saved window geometry is retained within the current screen's usable bounds.

Members and Equipment dispatch `member list` and `equipment list` through the same command pipeline
as input. Loans opens its read-only panel directly. A row or loan identity link dispatches exactly
`member view nus/NUS_ID` or `equipment view uuid/UUID`, once per explicit activation.
Programmatic startup selection, selection restoration and refresh never dispatch commands. Row/link
activation preserves its focus until its key finishes; shifting focus during Space previously inserted
a stray space into command input. Navigation tabs still return focus to command input.

Navigation uses `DisplayRequest`, not feedback text. Help and Exit take precedence. An unchanged
request retains the screen. Legacy commands still have their own Persons screen. Blank input is
ignored; successful input clears; parse, command and save failures retain input for correction.
Both success and failure have readable text, a status label and a different feedback background.
Long feedback wraps and scrolls rather than expanding the command area offscreen.

The shell subscribes once to the live member, equipment, loan and filtered-person lists.
Changes coalesce into one FX-event-loop refresh after a model operation. Refresh rebuilds related
cards, holdings, availability, summaries and loan history, retaining identities and an explicitly
empty selection. Deleting a selection chooses the next row at its former sorted position, or the
last surviving row; an empty directory clears selection. Real detail fields refresh alongside cards/summaries/Loans, including while another tab is active.
Removed detail identities leave stale detail mode and return to the corresponding summary. Disposal removes subscriptions and event handlers, and cancels queued refresh work.

A save failure does **not** imply rollback: the existing pipeline can mutate memory before saving.
The shell shows the actual error, retains navigation/input and refreshes actual in-memory state.

## Real routed integration

All ten family routes are active on this baseline. The transitional unavailable-view branch and its
notice have been removed. Input, row activation and loan links now follow the same real typed results.
Controlled results are retained only to test the display contract and an impossible/stale target.

The full real-command lifecycle adds two same-name members and two otherwise identical items with
separate generated UUIDs, lists/views them, assigns both, verifies real holder/contact/notes/date and
holdings fields, reloads a fresh model/logic/window from saved JSON, returns items, rejects repeated
returns and open-loan deletion, deletes permitted identities, verifies deterministic selection/stale-detail
cleanup, reloads again and repeats views. Every successful submitted command dispatches/saves exactly once.

Rejection cases interleave valid commands with duplicate/unknown identities, missing/repeated/unknown
parameters, malformed dates/UUIDs, assigned/damaged/under-repair items, future assignment/return,
expected-before-assigned, return-before-assigned and no-open-loan return. Each snapshots all four
collections, current displayed panel, selected row and saved JSON bytes; failure preserves these,
retains correction input/focus and never saves. Separate real-route external-update tests verify the
coalesced refresh while member details, equipment details and Loans remain active without callback dispatches.

The supplied [MVP document](https://docs.google.com/document/d/1XzYR2IpGc__mSZEy5GKMmVK8809j3Ln42VA8_9-va48/edit)
was checked against its "Laplace MVP Feature Specification" section. Its save-failure guarantee
requires separate transaction work; the current test below remains a truthful report of the
existing behavior. Completed history is still shown by Isaac's current LoanPanel despite being
excluded by the newer MVP. Shared validation/normalization, UUID collision retry and the new
member-held-item sort order also need their owners' updates. These dependencies are recorded in
#75 and ReviewResponse.md; the shell tests do not certify full MVP compliance.

## Tests and repeatable checks

Use Java 25 (`sdk use java 25.0.3.fx-zulu` on the configured macOS machine). The existing test runtime
selects matching JavaFX 17.0.7 native jars. Run in a graphical session; Linux requires Xvfb.

```sh
./gradlew test --tests '*CommandBoxTest' --tests '*ResultDisplayTest' --tests '*MainWindowShellTest'
./gradlew check coverage shadowJar
./.github/run-checks.sh
```

The shell suite uses the actual MainWindow FXML, real components, ModelManager, LogicManager and
JSON storage. It covers one-shot submission/activation, failure recovery, tab commands and Loans,
typed requests, Help/Exit, selection recovery, root replacement, loan-only refresh, teardown,
legacy contrast, geometry and saved-data reload. Controlled typed-result and disk-failure fixtures
are identified separately in the test class. Tests fail on graphical errors and do not skip them.

The generated session record is `build/reports/laplace/shell-session.txt`. It records actual inputs,
feedback, retained input, dispatched commands and save counts. Generated screenshots include
normal/small Members, Equipment, both real details, Loans and legacy Persons, plus error, empty, missing and long-text states
under `build/reports/laplace/`. These are snapshots of the actual mounted shell, rather than the
earlier component preview fixture. The retained sessions are [ShellSession.txt](ShellSession.txt) and [RoutedShellSession.txt](RoutedShellSession.txt).

## Local evidence

Java 25 validation passed on ARM macOS with 692 tests and zero failures, errors or skips.
Checkstyle, coverage and the packaged `shadowJar` build passed. Repository-wide checks passed with
the existing trailing-whitespace warning in `docs/AboutUs.md:61`, outside this change's ownership.

A separate temporary Gradle verification task removed exploded main classes/resources from its
classpath and loaded the shell from `build/libs/addressbook.jar`. All 27 shell/input/feedback tests
passed. The startup test asserted that MainWindow's code source was that JAR and all shell and
mounted-panel FXML plus the shared stylesheet used `jar:` classpath resources. This starts real
MainWindow stages from the packaged code and includes the real routed lifecycle. It does not establish
standalone interactive acceptance or a production launch on every OS. The temporary task is not a build-system change.
MainWindow has 215/215 JaCoCo lines and 87/90 branches covered; tests now exercise the legacy menu,
equipment Space activation, selection event handlers and hidden-window disposal identified by
coverage review. No tests were skipped and no coverage exclusions were added.

A standalone `seedu.address.Main` launch of this packaged JAR now exposes a real "Laplace" window to
the desktop tool through a temporary native app launcher. Environment: ARM macOS, Zulu Java
25.0.3, matching JavaFX 17.0.7 ARM native jars, temporary data directory
`/private/tmp/laplace-interactive-review`. The temporary launcher's classpath puts the matching native
jars before the existing shaded JAR. An initial reversed order loaded x86 native resources and failed;
that launcher-only mistake was corrected. No launcher/runtime changes are shipped.

The final JAR's observed startup showed three sample members, first sorted selection, real summary,
ready feedback, focused command input and the expected temporary data path. The corrected directory
proportion was inspected in the native screenshot. Tab traversal to the selected member followed by
Space and Enter opened real details, holder/date fields and left row focus intact with empty command
input. The Equipment tab showed its real directory/counts. See [StandaloneAcceptance.md](StandaloneAcceptance.md)
for exact completed steps and limitations.

Native pointer targeting subsequently returned `noWindowsAvailable`; the desktop tool then reported
the Mac locked again. Full final-build mouse/link, Help/F1, Exit, small/long/empty interaction and
standalone write/reload acceptance therefore remain uncompleted. Earlier-build typing/error/Exit
observations and passing packaged FX tests are recorded separately, without treating them as complete
final-build standalone acceptance.

The local changes sit on integration merge `d5bd893701b8384db331e4f78a108387fd333490`, which incorporates
team master `0efb388f8a0daa180481dc6f049705bbd9a93add`. After publishing, record the exact final head and
fresh Linux/macOS/Windows team CI. Earlier fork checks for `a69514c9` are historical, not validation of
this revision. Final tracker #75 stays open for interactive acceptance, CI and the separate newer-MVP work.

The screenshots below were visually checked against `docs/images/Ui.png`. Their data and long-text
fixtures are supplied by tests; the window, cards, lists, summaries, details and Loans panel are production
components. Long stored-value fixtures check layout robustness; they do not establish acceptance of
values outside the newer MVP's typed-input limits. Normal content size is 1100x700, and the smaller content size is 760x500.

### Members

![Members application shell](images/shell-members-1100x700.png)

### Equipment

![Equipment application shell](images/shell-equipment-1100x700.png)

### Real details (actual routed view commands)

![Member details in the actual shell](images/shell-member-details-1100x700.png)

![Equipment details in the actual shell](images/shell-equipment-details-1100x700.png)

![Small member details](images/shell-member-details-760x500.png)

![Small equipment details](images/shell-equipment-details-760x500.png)

### Loans

![Loans application shell](images/shell-loans-1100x700.png)

### Small window

![Small Members application shell](images/shell-members-760x500.png)

### Error recovery

![Error with retained command input](images/shell-error-760x500.png)

## Manual acceptance record

| Aim | Input/action | Expected result |
| --- | --- | --- |
| Startup | Open shell with saved members | Members, first sorted selection, summary; no view command |
| Ignore empty input | Enter with spaces/tabs | No command/save; input remains available |
| List/navigation | `member list`, Equipment tab, Members tab | Correct real summary; one command/save each |
| Rejection and recovery | `equipment list extra`, then `equipment list` | Error retains input/view/data; valid input clears and navigates |
| Real view routes | Click/Enter/Space on selected row; activate loan identity links | Exact prefixed command once; real details and matching identity |
| Real details | Type real member/equipment add/view commands | Exact identity, contacts/notes/holder/dates; matching left row; one save per success |
| Loan refresh | Assign/return through real routes with each detail/Loans view active | Real fields/counts refresh; no activation callback; typed commands honor their display request |
| Selection after deletion | Type permitted delete for selected identity | Deterministic surviving selection or empty directory; no callback |
| Root reset | Replace root data with an empty book | Empty lists/counts and no stale detail target |
| Save error (current behavior, MVP prerequisite pending) | Inject disk failure during a real legacy add | Error/retained input; actual in-memory person exists; no false save or rollback claim |
| Keyboard/lifecycle | F1 from input; `help`; `exit`; reopen from saved JSON | Help without navigation; both stages close; reload preserves records |
| Long text/resize | Long member/equipment names, 760x500 window | Lists/summaries scroll; feedback/input stay accessible |
| Disposal | Queue data change, dispose, then mutate root | Old shell receives no queued or later refresh |
