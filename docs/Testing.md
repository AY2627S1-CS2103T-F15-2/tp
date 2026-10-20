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
invalid input and current model data. `CompletionControllerTest` dispatches JavaFX key events and checks
cycling, modifier keys, selections and focus traversal. Its focus-field test double also drives JavaFX's
protected focus setter because macOS foreground activation is not deterministic in a test runner.

The mounted-shell test `MainWindowShellTest.autocomplete_keyboardSession_preservesDataAndExecutesOnlyOnEnter`
runs sequentially and stops at the first mismatch. It records expected and actual input text, then checks
dispatch counts, successful saves and every model collection after each Tab press. It also submits a
completed command, rejects an invalid input and verifies that a later valid command still succeeds.
The transcript is `build/reports/laplace/autocomplete-keyboard-session.txt`; the coverage report is
`build/reports/jacoco/coverage/html/index.html`.

`check` also runs `verifyPackagedAutocomplete` in a separate JVM and a disposable directory. The probe
verifies the production code source is `build/libs/Laplace.jar`, then drives its mounted shell with
JavaFX keyboard events. Matching native JavaFX dependencies precede the distributable JAR on the smoke
test classpath. Test output contains helpers only, with no unpackaged production classes. Results are
recorded in `build/reports/laplace/packaged-autocomplete-session.txt`.

Reviewable local evidence is recorded in [AutocompleteVerification.txt](gui/AutocompleteVerification.txt),
[AutocompleteKeyboardSession.txt](gui/AutocompleteKeyboardSession.txt), and
[AutocompletePackagedSession.txt](gui/AutocompletePackagedSession.txt).

## Types of tests

This project has three types of tests:

1. *Unit tests* target the lowest-level methods and classes.<br>
   For example: `seedu.address.commons.StringUtilTest`
1. *Integration tests* check how multiple code units work together; the individual units are assumed to work.<br>
   For example: `seedu.address.storage.StorageManagerTest`
1. *Hybrid tests* combine unit and integration testing. These tests check both the individual units and how they work together.<br>
   For example: `seedu.address.logic.LogicManagerTest`
