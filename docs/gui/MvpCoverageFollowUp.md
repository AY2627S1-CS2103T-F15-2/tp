# PR #97 coverage follow-up

Based on PR head `37ec27aa5543b1158f1e3757a94864c8ca039a39` and PR base
`3cd3947146bcda9976c2e4f8b2e24c00e79c3671`.

## Added coverage

- Atomic candidate cleanup failure preserves the original exception, attaches the cleanup exception,
  and leaves live file bytes intact. A nonempty temporary directory reproduces deletion failure
  without relying on operating-system permission differences. Unexpected runtime failures also
  remove the candidate without changing the live file.
- Unicode text validation rejects control, format, surrogate and unassigned characters. Supplementary
  printable characters count as one code point at length boundaries.
- Stored member names, phones and emails accept both legacy and MVP forms, preserve exact values,
  and reject values invalid under both formats. JSON round trips preserve MVP-only punctuation,
  international phone prefixes and email case.
- UUID boundaries cover complete zero/maximum hexadecimal values, case and surrounding whitespace,
  malformed separators, shortened/extended values, invalid hexadecimal characters and null input.
- Shell lifecycle tests cover successful Space release without duplicate dispatch, disposal while
  correction focus is pending, and disposal before component initialization.

## Small production simplifications

`parseUuid` no longer catches an exception that cannot occur after the strict canonical hexadecimal
format check. Invalid input still produces the same field-specific ParseException before conversion.

The Space-release handler no longer checks `disposed`: `dispose()` detaches that handler on the FX
thread. The new press/failure/dispose/release regression verifies that a pending release cannot steal
focus or dispatch another command after disposal.

## Local verification

Windows, Java 25.0.4, matching JavaFX 17.0.7 Windows natives:

```text
gradlew.bat check coverage --console=plain
```

- 725 tests; zero failures, errors or skipped tests.
- Main and test Checkstyle pass; changed-file readability reviewed.
- 250 of 250 changed executable production lines fully covered, including the date-guidance update.
- 92 of 92 branches on those changed lines covered.

The patch calculation intersects added lines from `git diff --unified=0 BASE -- src/main/java`
with the source-file line counters in `build/reports/jacoco/coverage/coverage.xml`. Every changed
executable line has zero missed instructions and zero missed branches. No production files or
branches were excluded from coverage; this is patch coverage, not 100% repository-wide coverage.
External Codecov and team CI must evaluate the published commit separately.

These automated regressions do not replace the packaged application's physical mouse and Help-window
acceptance described in the existing review report.
