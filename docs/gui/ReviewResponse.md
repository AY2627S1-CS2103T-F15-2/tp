> Historical #93/component evidence. The later v1.3.3 completion changes and current acceptance
> results are recorded in [MvpCompletion.md](MvpCompletion.md). Earlier save-error, validation,
> history-presentation and pending-route statements below describe their original baseline.

# PR #93 completion-review response

This responds to Keegan4's six completion threads on
[PR #93](https://github.com/AY2627S1-CS2103T-F15-2/tp/pull/93). It expands the shell's implementation
using Nicholas's real components from merged PR #94. It does not treat the original closed issues
or the partial issue #91 as full sign-off. [Final tracker #75](https://github.com/AY2627S1-CS2103T-F15-2/tp/issues/75)
has been reopened and must stay open until the actual combined scenario passes.

## Review status after merged routing

The branch now incorporates team master `0efb388f8a0daa180481dc6f049705bbd9a93add`, including
loan PR #90 and final routing PR #95. The integration merge is `d5bd893701b8384db331e4f78a108387fd333490`;
the following shell changes are verified on top of that merge. Record the published final head and its
new CI runs after push; neither the earlier `a69514c9` checks nor the integration merge alone validates
these subsequent edits.

| Thread | Changes and completed evidence | Remaining acceptance |
| --- | --- | --- |
| [1: Real details](https://github.com/AY2627S1-CS2103T-F15-2/tp/pull/93#discussion_r4277740425) | Real add/view routes now mount the existing panels and select exact identities. The lifecycle verifies contacts, notes, dates, holders, multiple holdings, same-name members and identical items with distinct IDs. Actual mounted screenshots cover normal/small, empty/long/missing states. | Final standalone interactive inspection remains separate from the passing routed FX tests. |
| [2: Details refresh](https://github.com/AY2627S1-CS2103T-F15-2/tp/pull/93#discussion_r4277740437) | Real routed assignment/return while member details, equipment details or Loans is active refreshes holdings, availability and loan rows without extra activation callbacks. Typed loan commands honor their equipment-detail request. Routed deletion recovers surviving selection and removes stale details; reset/disposal regressions remain. | No remaining command/component prerequisite; standalone interaction remains below. |
| [3: Real navigation](https://github.com/AY2627S1-CS2103T-F15-2/tp/pull/93#discussion_r4277740444) | Removed the temporary unavailable-view branch and notice; fixed Space focus leakage and production startup split, with regressions. Real click/Enter/Space/loan-link activations use LogicManager exactly once and mount real details. Invalid input retains screen, selection, input and focus. Help/F1, Exit and legacy tests pass. | Final published-head team CI and standalone interaction. |
| [4: Routed end-to-end](https://github.com/AY2627S1-CS2103T-F15-2/tp/pull/93#discussion_r4277740451) | New full lifecycle uses actual MainWindow input, LogicManager, ModelManager and temporary JSON. All ten routes run with exact per-command dispatch/save assertions; fresh model/logic/window reloads verify persisted records. Rejections cover duplicate/unknown IDs, parameters, assignment conditions, dates, repeated returns and blocked deletion, preserving all collections, view, selection, input/focus and saved bytes with no save. | Automated routed acceptance is complete; it does not stand in for standalone interaction. |
| [5: Newer MVP](https://github.com/AY2627S1-CS2103T-F15-2/tp/pull/93#discussion_r4277740455) | Existing behavior remains honestly documented. Original requirements allow closed history and post-save-error in-memory mutation. Newer MVP follow-ups stay in #75 and the owner matrix below. | Coordinated transactions/history/validation/UUID retry/holdings order remain owner work; no full MVP claim. |
| [6: Final evidence](https://github.com/AY2627S1-CS2103T-F15-2/tp/pull/93#discussion_r4277740459) | 692 normal tests and 27 packaged-shell tests pass; zero failures/errors/skips. Checkstyle, coverage, repository checks and JAR build pass. Actual routed transcript and inspected normal/small screenshots are linked below. MainWindow coverage is 215/215 lines, 87/90 branches. | Standalone interaction was interrupted by the locked Mac; fresh team CI must run for the final published head. #75 remains open. |

The controlled typed-result tests remain contract-boundary tests. The new routed lifecycle and
rejection tests never inject `nextResult`. No replacement parser, owner command or detail panel is shipped.
Review threads with outstanding interactive, CI or newer-MVP requirements remain unresolved.

## MVP reconciliation

Source: the "Laplace MVP Feature Specification" section of the
[supplied Google document](https://docs.google.com/document/d/1XzYR2IpGc__mSZEy5GKMmVK8809j3Ln42VA8_9-va48/edit),
checked on 15 October 2026. Earlier schema notes and repeated implementation prompts in that same
document describe older contracts; this matrix follows the detailed feature specification and the
review's newer acceptance requirements. It records observed gaps, rather than silently changing
another owner's implementation.

| Requirement | Observed current gap | Owner boundary |
| --- | --- | --- |
| Failed saves leave records unchanged | LogicManager executes against live Model before saving; current save-failure test observes in-memory mutation. | Separate Logic/storage/model transaction change, coordinated with the storage owner. |
| Completed history excluded | LoanPanel still displays returned history. | Isaac's LoanPanel scope change; shell then verifies the real presentation. |
| Updated value validation/normalization | Shared AB3 Name/Phone/Email and existing equipment parsers do not implement the new punctuation, length, international-phone, lowercase-email and whitespace rules. | Shared/model and parser owners must coordinate; retain legacy compatibility. |
| UUID collision retry | EquipmentAddCommand currently rejects the first collision. | Arjo's command change, followed by routing/acceptance tests. |
| Held-equipment ordering | MemberDetailsPanel currently uses the shared equipment category/name/UUID order; the MVP specifies expected return, name, then UUID. | Nicholas's detail-panel change; shell then verifies the merged order. |
| Full typed-command scenario | All ten merged routes now pass the real shell lifecycle and fresh JSON reload tests. | Standalone interactive inspection and final-head team CI remain in Keith's #75 acceptance. |

Full MVP compliance is a cross-owner claim. Mounting and refreshing real details establishes the
shell implementation boundary only. No model/storage, family switch, panel, parser or command
owned by another contributor was edited for these review fixes.

## Evidence

- [Shell behavior, checked screenshots and acceptance plan](ShellVerification.md).
- [Recorded actual command session](ShellSession.txt) and [complete routed lifecycle](RoutedShellSession.txt).
- Generated reports: `build/reports/tests/test/`, `build/reports/tests/packagedShellTest/`,
  `build/reports/jacoco/coverage/` and `build/reports/laplace/`.
- MainWindow coverage: 215/215 lines, 87/90 branches; no skips or coverage exclusions.
- [Standalone acceptance record](StandaloneAcceptance.md) distinguishes final-build keyboard/startup
  observations from unfinished pointer/lifecycle checks and earlier-build evidence.
- The existing `docs/AboutUs.md:61` trailing-whitespace warning remains outside this change's ownership.
