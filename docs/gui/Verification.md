# Laplace GUI verification

## Stage A foundation

The foundation branch supplies shared styles and a JavaFX test helper. It does not implement
the application shell, deletion commands, directory panels, or final milestone acceptance.
Those require the other contributors' merged contracts and components.

Use Java 25 and the repository Gradle wrapper. On macOS with SDKMAN, the configured JDK is
`sdk use java 25.0.3.fx-zulu`. Tests deliberately load the declared JavaFX **17.0.7** runtime
instead of any JavaFX version bundled with the JDK. The test-only configuration chooses
`mac-aarch64`/`mac`, `linux-aarch64`/`linux`, or `win` according to the host OS and architecture.
The existing production dependencies and dependency versions are preserved.

A desktop graphical session is required on macOS and Windows. Linux CI uses Xvfb:

```sh
xvfb-run --auto-servernum ./gradlew check coverage
```

For local focused checks and the complete validation:

```sh
./gradlew test --tests seedu.address.testutil.FxTestUtilTest --tests seedu.address.ui.LaplaceThemeTest
./gradlew check coverage shadowJar
./.github/run-checks.sh
```

There are no graphical-test skips or headless-success substitutions. Native/display failures
are failures. The Linux, macOS and Windows workflow jobs remain in place, including coverage.
Local macOS validation alone does not establish that the other workers passed.

## Shared component APIs

Use `FxTestUtil.runAndWait(Runnable)` for FXML creation, Node updates and GUI assertions.
It starts one toolkit per JVM, awaits real completion signals, propagates original failures,
supports nested calls on the FX thread, and keeps the toolkit alive after a window closes.
Concurrent callers share startup. Interruptions preserve the interrupt flag and cancel a
queued action; an already running action cannot be rolled back. The completion timeout is
30 seconds. Close each test's windows in `finally`; never call `Platform.exit()` between tests.

Load `/view/LaplaceTheme.css` from the classpath and add `laplace-root` to the scene root.
Standalone previews should use that same root class. Do not add the old global dark theme to
the same scene; load dark styles locally for legacy/help content when the shell is integrated.

| Style class | Purpose |
| --- | --- |
| `laplace-surface` | White bordered panel |
| `laplace-title` | Main heading |
| `laplace-muted` | Secondary information with readable contrast |
| `laplace-count-badge` | Rounded count/status badge |
| `laplace-header` | Charcoal navigation header |
| `laplace-navigation` | Keyboard-focusable navigation button |
| `laplace-navigation-active` | Active navigation text weight and underline |
| `laplace-success` | Green feedback/status container |
| `laplace-error` | Red feedback container and readable error text |
| `laplace-row` | Padding/spacing for immutable row content |

ListView selection styling is scoped below `laplace-root`, including selected rows when the
list loses focus. The parent ListView owns selection and callbacks; a card uses `laplace-row`
for its content. Feature-specific classes retain the agreed member/equipment/loan/directory/
details prefixes. Text fields support the existing `error` class. Labels with long content
must use wrapping, and each component remains responsible for scrollable layout.

## Foundation evidence

Baseline: `6b85337bce1664ec2774f0aca86b9d51a749019b` (team master, verified 13 October 2026).

On the ARM macOS validation machine, the baseline image-only AppUtilTest passed, but actual
toolkit startup with JavaFX 17 classes and the existing mixed-platform classpath failed with
`NoSuchMethodError`: the JDK supplied incompatible JavaFX 25 native libraries. The new helper
tests had 5 failures out of 6 before the runtime correction. To isolate the declared runtime
from the bundled modules during that diagnosis, a temporary Gradle init script added
`--limit-modules=java.se,jdk.unsupported` to Test JVMs. The committed configuration now supplies
matching JavaFX 17 ARM jars and uses that same module restriction, without an init script.

The foundation tests verify concurrent calls, nested calls, failure propagation, interruption
and queued-action cancellation, last-window lifecycle, null rejection, real UiPart/FXML loading,
the computed CSS palette and ListView selection. They render style probes at 1100 by 700 and
760 by 500. The generated files are untracked:

- `build/reports/laplace/theme-1100x700.png`
- `build/reports/laplace/theme-760x500.png`
- `build/reports/tests/test/index.html`
- `build/reports/checkstyle/`
- `build/reports/coverage/`

The normal local validation completed with 437 tests, zero failures and zero skips; Checkstyle,
coverage generation and `shadowJar` passed. Repository-wide checks passed with one pre-existing
trailing-whitespace warning in `docs/AboutUs.md:61`, outside this assignment's ownership.
An additional temporary-init-script run removed exploded main classes/resources from the test
classpath and loaded UiPart and LaplaceTheme.css from `build/libs/addressbook.jar`; both theme
tests passed. This checks packaged foundation resources, not the final application-shell launch.

The style probes explicitly identify themselves as previews and contain test labels only.
They are not screenshots of a working Members directory. Compare their palette and shared
states with `docs/images/Ui.png`; assess the real directory layout only after shell integration.

## Final shell gate (pending dependencies)

Once all required components and routes are merged, record the exact combined SHA and test
actual typing, row activation (including an already selected row), Members/Equipment/Loans
navigation, summaries, both detail views, assignment/return refresh, deletion selection recovery,
empty/missing states, long identifiers, errors, resizing, keyboard focus and persistence reload.
Check Help/Exit and legacy persons. Inspect the Members directory against `docs/images/Ui.png`
and capture every additional view, small-window and error state. Build `shadowJar` and launch
the packaged application from a temporary data directory to verify classpath resources.

Keep the integration issue open until this evidence exists. Do not report the foundation's
passing tests, stylesheet probes or JAR build as completed command/GUI milestone acceptance.

## Stage B deletion handoff

The deletion component PR is based on team master
`6230007d781478bc2a740a73fce6036e8a534996`, after display #80, parsing #83, UI foundation
#81 and member-add/card #79 merged. It adds MemberDeleteCommand/EquipmentDeleteCommand and
their individual parsers. It does not change the shell, family routers, cards, domain or storage.

Both commands resolve stored identities through Model and delegate open-loan restrictions
and related closed-history removal to the root. Success requests the corresponding directory.
Unknown identities and forbidden deletions become CommandException with the original domain
cause. Direct parser/command/model/temporary-file storage tests cover rejected-state integrity,
same-name identities, cleanup boundaries and fresh-model reloads. Saving in those tests is
explicit; they do not establish the as-yet unactivated LogicManager routes.

Normal Java 25 validation: `./gradlew check coverage shadowJar` passed with 510 tests and
zero failures, errors or skips. Both Checkstyle tasks and coverage generation passed.
`./.github/run-checks.sh` passed with the existing AboutUs whitespace warning. All 28 new
deletion tests ran. No GUI behavior changed, so the foundation's existing graphical tests
remain accurate; no new shell screenshot or full-application acceptance is claimed.

After Duy activates both routes, verify that successful deletion saves through LogicManager
and rejected parsing/execution never saves. Exercise the actual GUI: blocked deletion keeps
the current screen, selection and input; permitted deletion shows the directory, chooses a
deterministic surviving identity or empty state, and removes stale details. Refresh holdings,
availability and loan history after cleanup. Final shell and milestone issues remain open.
