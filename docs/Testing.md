---
  layout: default.md
  title: "Testing guide"
  pageNav: 3
---

# Testing guide

<!-- * Table of Contents -->
<page-nav-print />

<!-- -------------------------------------------------------------------------------------------------------------------- -->

## Running tests

You can run tests in two ways.

* **Method 1: Using IntelliJ JUnit test runner**
  * To run all tests, right-click on the `src/test/java` folder and choose `Run 'All Tests'`
  * To run a subset of tests, you can right-click on a test package,
    test class, or a test and choose `Run 'ABC'`
* **Method 2: Using Gradle**
  * Open a console and run the command `gradlew clean test` (Mac/Linux: `./gradlew clean test`)

<box type="info" seamless>

**Link**: Read [this Gradle Tutorial from the se-edu/guides](https://se-education.org/guides/tutorials/gradle.html) to learn more about using Gradle.
</box>

--------------------------------------------------------------------------------------------------------------------

## Autocomplete checks

Use Java 25 and run `./gradlew test check coverage shadowJar`. The `check` task enforces 100% line,
instruction and branch coverage for all completion classes, the complete `CommandBox`, and the shell's
modified mounting method. Missing classes, methods, reports or line counters fail the verification.
Straight-line code has no branch counter in JaCoCo; no executable feature code is excluded.

`CompletionServiceTest` checks command vocabulary, prefix/value contexts, multiword replacement,
invalid input and current model data. A parser-route contract test checks every accepted family subcommand.
Search option suggestions are parsed by the actual command parser; tests cover lifecycle fields, batch
UUID neighbors and duplicates, searchable values and member-only name suggestions. Direct service, JavaFX
input and packaged-JAR regressions verify that Tab at the start of an existing field marker leaves its
marker and value unchanged. Unicode field-separator regressions cover em spaces, nonbreaking spaces,
narrow nonbreaking spaces, next-line characters, ideographic spaces and tabs. They preserve later fields,
partial prefixes and batch UUID neighbors, including duplicate suppression. Mounted and packaged keyboard
sessions check Unicode separators and caret placement without executing or saving on Tab.
`CompletionControllerTest` dispatches JavaFX key events and checks cycling, modifier keys, selections
and focus traversal. Its focus-field test double also drives JavaFX's protected focus setter because
macOS foreground activation is not deterministic in a test runner.

Structural performance regressions assert zero static-vocabulary model reads, no person/loan access,
and no value-source reads while cycling cached record candidates. Root-replacement tests check listener
detachment. `CompletionRefreshIntegrationTest` covers committed changes, undo/redo, lifecycle, loans
and reload, plus unchanged cycles and file bytes after queries/rejected operations. These tests check
bounded work rather than asserting machine-dependent timing thresholds.

The mounted-shell test `MainWindowShellTest.autocomplete_keyboardSession_preservesDataAndExecutesOnlyOnEnter`
runs sequentially and stops at the first mismatch. It records expected and actual input text, then checks
dispatch counts, successful saves and every model collection after each Tab press. It also submits a
completed command, rejects an invalid input and verifies that a later valid command still succeeds.
The transcript is `build/reports/laplace/autocomplete-keyboard-session.txt`; the coverage report is
`build/reports/jacoco/coverage/html/index.html`.

`check` also runs `verifyPackagedAutocomplete` in a separate JVM and a disposable directory. The probe
verifies the production code source is the selected candidate JAR, then drives its mounted shell
with JavaFX keyboard events. The smoke test uses the JAR's bundled native libraries without prepending
matching test dependencies. CI uses a plain Java 25 JDK. Test output contains helpers only, with no
unpackaged production classes. Results are
recorded in `build/reports/laplace/packaged-autocomplete-session.txt`.

Build one distribution with `./gradlew check coverage shadowJar`. `verifyUniversalJar`, included in
`check`, verifies both macOS slices byte-for-byte against JavaFX's original libraries, Windows/Linux
native presence, resource uniqueness and the 100 MB limit. All platforms use `build/libs/Laplace.jar`.
To probe a downloaded candidate, run `./gradlew verifyUniversalJar verifyPackagedAutocomplete
-PpackagedJar=build/candidate/Laplace.jar` on each target architecture. CI downloads the same candidate
for Windows, Linux and both macOS architectures; selecting a candidate does not emulate another processor.
See the [distribution checklist](DevOps.md#single-jar-distribution) for release verification.
The [universal packaging verification record](gui/UniversalPackagingVerification.txt) lists the local
candidate hash, tested environment and outstanding platform coverage.

Reviewable local evidence is recorded in [AutocompleteVerification.txt](gui/AutocompleteVerification.txt),
[AutocompleteKeyboardSession.txt](gui/AutocompleteKeyboardSession.txt), and
[AutocompletePackagedSession.txt](gui/AutocompletePackagedSession.txt).

## Packaged display-scaling checks

`./gradlew verifyPackagedLayout` runs the shell layout and feedback tests with production classes,
FXML, stylesheets and native libraries loaded from the candidate JAR. The shipped-sample regression
requires the entire first open-loan card at 853×400 logical pixels: status, identity links, dates,
issue condition, return-condition selection and Prepare return command. Directory and unit-table
regressions also require complete rows. Additional records remain reachable by scrolling.

On Linux with Java 25, GTK and Xvfb installed, reproduce the reported display configuration with:

```sh
xvfb-run --auto-servernum --server-args="-screen 0 1280x720x24" \
  ./gradlew verifyPackagedLayout -PgtkScale=1.5
```

For an existing candidate add `-PpackagedJar=build/candidate/Laplace.jar`. `gtkScale` passes the actual
`glass.gtk.uiScale` setting to the test JVM, and the tests require an output scale of 1.5 and screen
bounds corresponding to a 1280×720 display. This verifies GTK scaling under Xvfb; it does not establish
compatibility with every desktop compositor or Windows/macOS display setting. Snapshots are written
under `build/reports/laplace`, including `shell-complete-sample-loan-853x400.png`.
The [Linux before/after verification record](gui/CompactLayoutVerification.txt) lists the exact JAR
hashes, environment and checks. [Before](gui/CompactLoanLinuxBefore.png) and
[after](gui/CompactLoanLinuxAfter.png) snapshots use shipped fictional sample data.

## Capacity and failure injection

These checks require a source checkout and Java 25. They are separate from the released-JAR manual
scenarios in the Developer Guide; testers do not need to hand-create a large dataset.

`LoanHistoryCapacityTest` builds its synthetic fixture in code: 1,000 members, 5,000 equipment units
and 10,000 completed loans. This fixture deliberately exceeds the NFR07 dataset and serves as a stress
check. It checks retained counts and bounded history controls in the Loans and
detail panels. Completion controller/service tests check that static vocabulary does not read record
collections and that unchanged cycles reuse candidates. These structural checks do not establish the
NFR07 timing target; that benchmark needs its own repeatable fixture and measured platform results.

`MemberHoldingsCapacityTest` separately creates 1,000 simultaneous current loans for one member.
It checks bounded controls, complete identity ordering, scrolling to early/middle/last units, navigation,
refresh after return/reassignment, and the transition between eleven virtualized and ten inline rows.
The completed-history capacity fixture does not exercise this current-holdings path.

`LaplaceTransactionTest` injects partial temporary-file writes and failed atomic replacements for real
mutation commands, including assignment and return. It checks unchanged live records, history and
previous file bytes, then successful retry after the injected failure is removed. Run it with
`./gradlew test --tests 'seedu.address.storage.LaplaceTransactionTest'`.
`AtomicFileWriterTest` checks access-policy ordering, metadata preservation and cleanup;
`AtomicFileWriterAclTest` checks real Windows ACL behavior and is platform-gated.
`ReadOnlyCommandIntegrationTest` checks that navigation, Help and Exit do not save domain records.
`CommandLogPrivacyTest` checks logs with the application logger hierarchy enabled at all levels.
These test descriptions document verification scope, not a claim that a particular run passed.

## Types of tests

This project has three types of tests:

1. *Unit tests* target the lowest-level methods and classes.<br>
   For example: `seedu.address.commons.StringUtilTest`
1. *Integration tests* check how multiple code units work together; the individual units are assumed to work.<br>
   For example: `seedu.address.storage.StorageManagerTest`
1. *Hybrid tests* combine unit and integration testing. These tests check both the individual units and how they work together.<br>
   For example: `seedu.address.logic.LogicManagerTest`
