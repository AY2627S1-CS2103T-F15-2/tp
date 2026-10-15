# PR #97 review follow-up

This supersedes the initial input/feedback acceptance in [MvpCompletion.md](MvpCompletion.md).
Review: [Keegan4's two findings](https://github.com/AY2627S1-CS2103T-F15-2/tp/pull/97#issuecomment-6187764797),
reproduced on `29e77e303b62cbb27e891ff9bd08792fb91970c1`. The PR description identifies the new published
head and its exact-head CI. Previous native-session files retain their original artifact hashes and messages.

## Multiline notes through the full input path

The top-level AddressBookParser now accepts multiline arguments instead of rejecting them before the
Laplace field validators run. The existing argument reader still enforces reserved markers and reports
unknown/repeated/missing fields normally. It does not treat separate lines as separate command executions.

CommandBox installs a TextFormatter before JavaFX's single-line TextField content filtering. Pasted tabs
and vertical-whitespace characters become spaces instead of being removed and joining words. Each input
character keeps its offset, including the two characters in CRLF. Existing field normalization collapses
those runs to one space before validation/storage. Selection replacement, caret position and undo/redo
remain correct. Rejected pasted commands remain visible/focused for correction.

New regressions first failed against the reviewed head:

- `MvpValidationTest.equipmentNotes_fullCommandWithLineBreaks_normalizesAndPersistsWithoutJoiningWords`
  sends LF/CRLF/CR/tab/vertical-tab/form-feed/NEL/Unicode line/paragraph separators through real
  LogicManager → ModelManager → JSON, checks normalized notes and fresh storage read, then rejects a
  newline-delimited reserved unknown marker without changing saved bytes.
- `MainWindowShellTest.pastedMultilineNotes_realInput_preservesSeparatorsPersistsAndRetainsRejectedCorrection`
  uses the real clipboard and `TextField.paste()` in the mounted application, preserving the previous
  clipboard contents in a finally block. It asserts actual notes, one dispatch/save, stored records and
  invalid-input/model/byte/selection/focus preservation. This also runs in the JAR-backed suite.
- `CommandBoxTest.multilineReplacement_preservesWordBoundariesCaretAndUndo` checks replacement at a
  selected range, stable caret/anchor and undo/redo.

## Feedback required by the detailed MVP specification

The existing commands capture the stored record and current open-loan context before attempting mutation.
The model still owns deletion/loan policy. LogicManager's snapshot/atomic-save/publish boundary is unchanged.
Expected model exceptions remain the original causes of CommandException, including a duplicate loan
rejected by the model after an availability check. Inconsistent injected model state gets an explicit
fallback rather than a fabricated holder ID.

Literal expected messages are asserted independently of production message constants:

| Case | Expected example |
| --- | --- |
| Member deletion blocked by two current holdings | `Cannot delete A0000001X because the member holds 2 item(s). Return the items first.` |
| Equipment deletion blocked by current holder | `Cannot delete UUID because it is assigned to A0000001X. Return the item first.` |
| Reassignment to a different member | `Equipment UUID is unavailable because it is assigned to A0000001X.` |
| Successful member deletion | `Deleted member A0000001X: Alice Tan.` |
| Successful equipment deletion | `Deleted equipment UUID: Camera 0.` |

`MvpValidationTest.feedback_realRoutes_reportsCurrentCountHolderAndDeletedNames` runs the full real
LogicManager/storage pipeline with two open loans and one closed loan, verifies the count excludes history,
then returns one item and verifies the count changes to one. It checks current-holder feedback when the
requested target differs, rejection bytes/records, permitted deletion names and fresh storage equality.
Existing deletion tests now use literal feedback assertions while retaining original-cause tests.
`LoanAddCommandTest.execute_modelDetectsDuplicate_reportsCurrentHolderAndPreservesCause` checks the model's
last-line duplicate protection with a different requested holder, unchanged collections and the actual cause.

## Verification

Java `25.0.3.fx-zulu`, macOS ARM64, matching JavaFX `17.0.7` native libraries:

```sh
./gradlew check coverage shadowJar -I /private/tmp/laplace-packaged-shell.init.gradle packagedShellTest
./.github/run-checks.sh
```

- Normal suite: **711 tests, 0 failures, 0 errors, 0 skipped**.
- Separate JAR-backed shell/input/feedback suite: **31 tests, 0 failures, 0 errors, 0 skipped**.
- Main/test Checkstyle, coverage generation and shadowJar pass. No test or coverage exclusions.
- Staged repository checks pass with only the existing AboutUs.md:61 whitespace warning.
- [Regenerated real routed input/output](MvpRoutedSession.txt) includes the current deletion feedback.

The three initial regressions reproduced both review findings before the fix. Two test-line wrapping
violations found by the full style check were corrected; the complete final run passed afterward.
CI results for the published revision are reported in the PR/#75, separately from local XML totals.

## Actual standalone review-build interaction

JAR SHA-256: `08517cc8db2907ed0227f91352d818e6741c858a57dec4b00bfeb85eda4cf957`.
The temporary native app/runtime/data environment is the same as the initial acceptance report.
On 15 October 2026, 16:44–16:47 SGT, native clipboard paste entered:

```text
equipment add name/Review Camera category/Test condition/GOOD notes/First line<CR><LF>Second line
```

Before Enter, the actual command field contained `notes/First line  Second line`, preserving a separator.
After Enter, equipment details displayed **First line Second line** and the command field cleared.
The new physical-item UUID was `9d7696ba-f986-4290-925c-44c087045923`.

A disposable Review Member (`A0000004X`) was added, and that item assigned to it. Actual observed feedback:

```text
member delete nus/A0000004X
Cannot delete A0000004X because the member holds 1 item(s). Return the items first.

equipment delete uuid/9d7696ba-f986-4290-925c-44c087045923
Cannot delete 9d7696ba-f986-4290-925c-44c087045923 because it is assigned to A0000004X. Return the item first.

loans add uuid/9d7696ba-f986-4290-925c-44c087045923 nus/A0000001X assigned/2026-10-15 expected/2026-10-16
Equipment 9d7696ba-f986-4290-925c-44c087045923 is unavailable because it is assigned to A0000004X.
```

Each refusal retained the input and command focus. Equipment details still showed the original holder,
notes and dates. After returning the item, actual successful deletion feedback was:

```text
Deleted member A0000004X: Review Member.
Deleted equipment 9d7696ba-f986-4290-925c-44c087045923: Review Camera.
```

The app exited through typed `exit`, logged MainApp.stop at 16:47:25 SGT and returned exit code 0.
Disposable data was restored to its prior record counts: 1 legacy person, 1 member, 2 equipment, 1 closed loan.
This targeted review-build check does not replace the earlier fresh-process acceptance or establish the
remaining physical pointer/final Help-return acceptance. Team CI and external coverage publication remain
separate gates in #75; this follow-up does not declare full milestone completion or authorize merging.
