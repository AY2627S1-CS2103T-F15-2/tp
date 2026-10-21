# Search/planning review follow-up

This follow-up addresses both inline findings in PR #109's review of `a7f752e7`.
The branch integrates team master `9885bb1e` through merge commit `8a110cce`.

## Preserve queries after deletion

Plain directory requests from deletion and stale-detail fallback now preserve the active query.
Only requests carrying criteria replace them. Member/equipment list commands explicitly carry
`DirectoryQuery.all()` to reset filters and sorting. This keeps mutation navigation distinct from
the user's deliberate reset command without introducing navigation inferred from feedback text.

Two real-shell regressions failed before the fix and passed afterwards:

- Member deletion retains text/holding filters and descending ID sort, recovers selection when one
  match remains, and shows zero matches after the last matching member is removed.
- Equipment deletion retains text/category/condition/availability filters and descending ID sort
  after the only matching unit is removed.

Both tests verify one dispatch/save per deletion, current JSON readback, summary counts/criteria,
empty selection and the later explicit list reset. Existing list-command and routing tests now
assert the explicit reset payload rather than a plain navigation payload.

## Retain integrated edit and search routing

The upstream merge conflict is resolved by retaining both `member edit` and `member search`.
The diagnostic lists both names. A top-level regression checks the two concrete command types
and the complete invalid-subcommand diagnostic. Incoming member-edit persistence/model tests
are preserved; no unmerged teammate implementation is copied into this branch.

## Verification

- Integrated parser/route validation before the merge commit: passed with both Checkstyle checks.
- Review-focused run: **34 tests, zero failures/errors/skips**, main/test Checkstyle, coverage
  generation and `shadowJar` passed. The command selects `MainWindowShellTest.search*`, `LaplaceList*`,
  `LaplaceCommandRoutingTest`, `MemberEquipmentRoutingIntegrationTest`,
  `FullCommandRoutingIntegrationTest` and `MemberEditCommandParserTest`.
- Packaged-JAR probe: passed actual CommandBox → LogicManager → model → temporary JSON execution
  for member search/delete/reset, member edit, equipment search/delete and fresh JSON readback.
  MainWindow's code source is asserted to be `Laplace.jar`; only a disposable probe entrypoint is
  outside the JAR. Initial probe inspection needed a CSS/layout pass before reading swapped panels.
- Full local suite attempted twice without exclusions. Both attempts exited with Windows native
  callback status `0xC000041D` and `Null pointer:data`, before a complete test report could be produced.
  These runs are not reported as passes or assigned a completed test count. Final hosted CI is linked
  separately in the PR. Existing tests and runtime/build settings remain unchanged.

## Git convention audit

Checked against the [SE-EDU Git conventions](https://se-education.org/guides/conventions/git.html).
The two published feature subjects have 49 and 48 characters; both start with an imperative verb,
are capitalized and have no final period. They are subject-only commits and were not rewritten
after review. SE-EDU recommends explanatory bodies for non-trivial commits; both new review commits
include rationale, a blank line after the subject and bodies wrapped to at most 72 columns.

The merge subject has 46 characters. The deletion-fix subject has 50 characters. The upstream merge
and deletion correction are separate logical commits, and the fork branch name remains unchanged.
