# v1.3.3 MVP completion and acceptance

## Revision and scope

Baseline: team master `3cd3947146bcda9976c2e4f8b2e24c00e79c3671`, after shell #93 merged.
Implementation source: `a2c349b9d627e368ff664cde7a6b9071c7237f59`, branch `v1.3.3`, authored/published by
`wtvlol`. Later commits in this branch record documentation/evidence only. The PR description records
its final published head and current CI links. Tracking: [#96](https://github.com/AY2627S1-CS2103T-F15-2/tp/issues/96)
for these implementation changes; [#75](https://github.com/AY2627S1-CS2103T-F15-2/tp/issues/75) remains the final gate.

The acceptance source is the detailed “Laplace MVP Feature Specification” and “MVP work delegation” in
[the requirements document](https://docs.google.com/document/d/1XzYR2IpGc__mSZEy5GKMmVK8809j3Ln42VA8_9-va48/edit).
Older embedded implementation prompts are reference material. Existing components and routes are extended;
no contributor's branch or replacement panel/router is used. #76's work exists in merged #82/#90;
#87's equipment work exists in merged #88. Their open issue status did not mean those implementations were missing.
No overlapping open PR existed at baseline or at the pre-publication refresh. No merge is authorized/performed.

## Implementation and regressions

| Requirement | Result and evidence |
| --- | --- |
| Failed-save atomicity | Six Laplace mutations run on a model snapshot; complete JSON goes to a flushed private sibling file and atomic replacement; only success publishes through the original observable root. `LaplaceTransactionTest` exercises all six mutations with partial candidate-write and replacement failures, every collection, prior bytes, live views/notifications, later commands and fresh reload. It also checks initial-write failure and unsupported atomic replacement. Original causes survive. Legacy person-command semantics remain compatible. |
| Validation and normalization | `LaplaceText`, member-specific factories in Name/Phone/Email, equipment creation helpers and ParserUtil enforce the detailed field rules. Legacy constructors/AB3 commands remain unchanged; stored legacy member values use compatibility readers without rewriting. `MvpValidationTest` covers punctuation/Unicode/lengths, phone formatting/bounds, ASCII email/domain/lowercase, printable equipment/notes, canonical UUID, exact condition/calendar, normalized new records and exact old-value reload. See [field rules](../commands/MvpFields.md). |
| Reading-order errors | The existing shared reader validates each supplied field before the next marker. Blank/unknown/repeated inputs keep their encounter order; absent required fields are checked last in lexicographic prefix order. All value-bearing routes use the reader's validators; both list routes reject arguments. Reordered multi-error tests cover every route. Business checks follow parsing. |
| Holdings order | Existing MemberDetailsPanel uses expected return, normalized equipment name and UUID. Equipment-directory category/name/UUID order is unchanged. `DetailsPanelTest` checks ties, correct associated dates, return/reassignment refresh, exclusion of closed loans and explicit sorted missing references without partial holdings. |
| UUID retry | EquipmentAddCommand generates on execution, retries collisions up to 100 attempts and returns the actual successful ID. Injectable-source tests cover immediate/collision/repeated success, exhausted/null generators and preserved existing items/cause. Only the successful addition reaches persistence. |
| Open-only Loans | Existing LoanPanel/FXML/CSS no longer render history. Closed records remain in model/JSON and deletion cleanup. Open counts, identities, dates and callbacks remain tested. Obsolete history-comparator tests were replaced by open-only/history-retention assertions. |
| Correction/navigation | Failed Space activation defers correction focus until key release, preventing an injected space. Detail identity changes reset scrolling; background refresh preserves position. Storage permission errors show the configured destination while retaining their original cause. Focused shell/detail regressions cover these cases. |

The persistence guarantee covers the six Laplace mutations. Read commands still use the existing save-after-success
pipeline; individual panels/commands never write storage. Atomic replacement is required: unsupported filesystems
fail safely without a non-atomic fallback. The saved-data schema and identity relationships are unchanged.

## Automated checks

Environment: macOS ARM64, Java `25.0.3.fx-zulu`, JavaFX `17.0.7` matching ARM native libraries.
The final source was checked with:

```sh
./gradlew check coverage shadowJar -I /private/tmp/laplace-packaged-shell.init.gradle packagedShellTest
./.github/run-checks.sh
```

| Suite/check | Final local result |
| --- | --- |
| Normal JUnit suite, including mounted JavaFX shell tests | **706 tests, 0 failures, 0 errors, 0 skipped** |
| Separate tests loading production classes/resources from addressbook.jar | **29 tests, 0 failures, 0 errors, 0 skipped** |
| Main/test Checkstyle, coverage report and shadowJar | Passed |
| Repository-wide checks | Passed; existing AboutUs.md line 61 trailing-whitespace warning remains |

No tests were disabled or coverage excluded. Normal reports are under `build/test-results/test`; separate packaged
reports are under `build/test-results/packagedShellTest`. The extra packaged task was supplied by a temporary init
script rather than a permanent workflow change. Its classpath is matching `javafxTestRuntime` native jars first,
then `shadowJar`, test output and non-JavaFX test dependencies; it excludes exploded main output. It uses
`laplace.packagedShell=true` and filters MainWindowShellTest, CommandBoxTest and ResultDisplayTest.
The shell fixture asserts its production code source is the JAR and resolves every FXML/CSS resource from it.
The normal CI suite already includes these shell behaviors; the additional 29-test JAR run is local evidence.

Real routed tests pass all ten routes through MainWindow → LogicManager → ModelManager → temporary JSON.
They include same-name identities, identical descriptions, lifecycle/reload, duplicate/unknown IDs,
malformed/missing/repeated/unknown fields, unavailable items/date boundaries, blocked deletion and repeated
returns, all-collection/saved-byte preservation, exact dispatch/save counts, refresh on every active view,
selection recovery/reset and listener disposal. Mouse events, Enter/Space and loan-link actions are exercised
in mounted FX tests. Those synthetic events alone do not establish native pointer interaction.

- [Recorded routed input/output and save counts](MvpRoutedSession.txt)
- [Recorded real shell input/output](MvpShellSession.txt)
- [Observed standalone session](MvpNativeSession.txt)
- [Final-build application's own input/start/stop log](MvpNativeApplication.txt)

## Packaged standalone interaction

Temporary acceptance app: `/private/tmp/laplace-mvp-native/LaplaceMvp.app`, bundle ID
`org.laplace.mvp.acceptance`; temporary working/data directory `/private/tmp/laplace-mvp-interactive`.
It launches the built shadow JAR with Java 25 and matching JavaFX 17 ARM native jars first on its classpath.
A temporary jpackage app image supplies the runtime and launch configuration; this is not an installed release
or a claim that the existing fat JAR contains every architecture's native library.

Launch options: `--limit-modules=java.se,jdk.unsupported --enable-native-access=ALL-UNNAMED`,
`-Duser.dir=/private/tmp/laplace-mvp-interactive` and a disposable JavaFX cache. The app was launched directly
through its `Contents/MacOS/LaplaceMvp` executable. No real user data was loaded or modified.

Final-source JAR SHA-256:
`a059b5256414a74f68f37b07e2e92714a244ffa423ef9a84a41871f71a0c64ca`.

Final-build process 4 (15:34–15:43 SGT, 15 October 2026) verified the 760×500 window, long-detail scrolling
and reset on identity change, genuine write-permission failure, retained correction input/focus, no leaked
addition on the next successful list, then typed add/assign/view/blocked-delete/return/permitted-delete for
both entities. Equipment list and direct Loans navigation showed 2 available items and no open loans.
Typing `exit` invoked MainApp.stop and the process returned exit code 0.

Final-build process 5 (15:44–15:45 SGT) started fresh against that saved file. It showed the original member's
normalized phone/email and zero holdings, rejected the deleted test member, displayed persisted Camera Kit
notes/availability/no holder, and showed no open loans/history. It also exited normally with code 0.
Every data byte after this reload/read/exit cycle matched process 4's saved file.

For the real failed-save check, only the disposable data directory was temporarily set to mode 0555.
A valid new member command failed with the configured `data/addressbook.json` path, retained its input/focus,
and preserved the exact saved bytes and one-member collection. Permissions were restored to 0755 immediately.
A successful member list still showed one member. After the successful temporary lifecycle and cleanup,
the data file again matched the pre-failure bytes, SHA-256
`4ef8246a2401877bf3044b77bf4b5743cb2b2ffdc54ff3c71ef797c81b9d9f92`.
Final data: 1 legacy person, 1 member, 2 equipment and 1 retained closed loan; no open loans.

Earlier standalone processes 1–3 used JAR SHA-256
`d1d78abde30098bdd67dacb84a68f51deff0b435f923eaa64f6028c3871723c8` before the final scroll/path-message fixes.
They additionally verified same-name members/identical equipment, differing due dates and holdings order,
Enter/Space row activation, actual accessibility loan-link buttons, Help/F1, legacy input, 1100×700 and
760×500, 100/50/300-character new equipment, failed deletion and normal-exit/fresh-process reload.
Process 1 was terminated after the desktop tool lost its main-window binding following Help; that process
is not counted as a normal Exit pass. Processes 2 and 3 exited normally. Details/limitations are recorded
in the standalone session, rather than attributing all observations to the final JAR.

## Screenshots

These are regenerated automated **mounted real-shell** captures from the final source/JAR test run, not
native desktop screenshots. Temporary JSON fixtures drive counts and fields. The desktop session's native
captures were also inspected, but the desktop tool exposes those inline rather than as committed PNG files.
Long fixtures intentionally include previously permitted oversized records to verify saved-data compatibility.
They are not examples of accepted new oversized input.

The current Members capture was inspected against `docs/images/Ui.png`: header/navigation, selected blue
row, summary/counts, green feedback strip and bottom input are present. At 760×500, content scrolls while
feedback/input stay accessible. Error, empty, missing-identity and long-content captures were inspected.

| State | Evidence |
| --- | --- |
| Members, normal and small | [1100×700](images/mvp/shell-members-1100x700.png), [760×500](images/mvp/shell-members-760x500.png) |
| Member details | [1100×700](images/mvp/shell-member-details-1100x700.png), [760×500](images/mvp/shell-member-details-760x500.png) |
| Open-only Loans | [1100×700](images/mvp/shell-loans-1100x700.png), [760×500](images/mvp/shell-loans-760x500.png) |
| Error and empty | [Error](images/mvp/shell-error-760x500.png), [Empty](images/mvp/shell-empty-760x500.png) |
| Long and missing | [Long saved equipment](images/mvp/shell-equipment-details-long-760x500.png), [Missing identity](images/mvp/shell-member-details-missing-760x500.png) |

## Remaining final acceptance

1. **Physical pointer acceptance — Keith/manual desktop tester.** Native coordinate clicks consistently
   return `noWindowsAvailable` even while accessibility reads, button actions and keyboard input work.
   Accessibility row-click actions did not dispatch a view command. Actual loan-link accessibility actions
   and Enter/Space worked, and synthetic mouse/once-only regressions pass, but physical mouse row/loan-link
   activation is still unverified. On an interactive desktop, click each member/equipment row and loan link;
   confirm one exact-identity dispatch, retained correction focus on failure and no duplicate callbacks.
   Recheck Help close/return on the final JAR; opening Help/F1 worked on the earlier build but lost the tool binding.
2. **Exact published-head team CI — repository maintainer.** The PR description/#75 record live results after
   publication. Fork runs and team checks must be distinguished. If opening the PR produces no team run,
   a maintainer must inspect repository Actions/fork-workflow policy, approve an awaiting run if GitHub creates
   one, or arrange an authorized team-repository push event for the exact published commit. The workflow has
   push/pull_request triggers and no workflow_dispatch entry; old-head reruns are not final-head verification.
3. **Review/sign-off — team reviewer.** Review coordinated model/storage/parser/panel changes against the
   detailed spec. #75 remains open until the remaining acceptance above and team CI are actually satisfied.

Implementation fixes are ready for review with passing local checks. They are not a declaration that the
completion PR is merge-ready, that every native interaction is verified, or that the full v1.3 milestone is
complete. Original shell integration has passing combined and packaged tests plus substantial standalone
verification; final physical pointer/Help-return acceptance remains explicit. No PR was merged.
