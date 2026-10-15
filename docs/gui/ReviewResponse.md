# PR #93 completion-review response

This responds to Keegan4's six completion threads on
[PR #93](https://github.com/AY2627S1-CS2103T-F15-2/tp/pull/93). It expands the shell's implementation
using Nicholas's real components from merged PR #94. It does not treat the original closed issues
or the partial issue #91 as full sign-off. [Final tracker #75](https://github.com/AY2627S1-CS2103T-F15-2/tp/issues/75)
has been reopened and must stay open until the actual combined scenario passes.

## Review status

| Thread | Changes and evidence | Remaining acceptance dependency |
| --- | --- | --- |
| [1: Real details](https://github.com/AY2627S1-CS2103T-F15-2/tp/pull/93#discussion_r4277740425) | MainWindow constructs both real panels from the live root/availability lookup. Typed detail requests call showMember/showEquipment and select the exact identity. Real field, same-name/multiple-holdings, notes/date and narrow/missing-state assertions pass. | Actual add/view commands through the family routes still require Duy's #73. Component view results delivered by a controlled fixture are not routed acceptance. |
| [2: Details refresh](https://github.com/AY2627S1-CS2103T-F15-2/tp/pull/93#discussion_r4277740437) | Both real panels refresh in the coalesced path. Model assignment/return tests cover member details, equipment details and Loans active without extra dispatches. Deletion, reset, identity replacement and disposal regressions pass. | The full routed mutation scenario awaits #90/#73. |
| [3: Real routing/navigation](https://github.com/AY2627S1-CS2103T-F15-2/tp/pull/93#discussion_r4277740444) | Existing one-pipeline navigation and error behavior are retained. Equipment Space activation now exercises a real component result and exact detail identity; selection event and legacy menu tests are added. | Keep the transitional placeholder guard until #73 activates real view/write routes; then replace pending-route expectations and verify mouse/Enter/Space/loan links through real logic. |
| [4: Routed end-to-end](https://github.com/AY2627S1-CS2103T-F15-2/tp/pull/93#discussion_r4277740451) | Existing boundary, real list/legacy logic, real model and JSON tests remain honestly scoped. | Isaac's #90 is open and #73 has not activated the routes. Do not introduce a test-only replacement parser or injected success as proof of autosave, rejection snapshots or final reload. |
| [5: Newer MVP](https://github.com/AY2627S1-CS2103T-F15-2/tp/pull/93#discussion_r4277740455) | The supplied MVP was read; the matrix below records actual current gaps. Save-failure acceptance remains a report of actual behavior, not a rollback promise. | Separate owner work for transactions, history presentation and changed validation/component contracts; see #75. |
| [6: Final evidence](https://github.com/AY2627S1-CS2103T-F15-2/tp/pull/93#discussion_r4277740459) | 638 normal tests and 24 packaged-shell tests pass with zero failures/errors/skips; style, coverage, repository checks and JAR build pass. Actual-shell details screenshots were inspected at both sizes. | Full routed acceptance and an inspected standalone interactive launch/reload still await the prerequisites and an accessible desktop target. Record the exact final combined SHA then; do not close #75 with the component evidence. |

All six original threads retain a full-acceptance dependency. The implemented shell portions above
can be reviewed independently. These notes intentionally do not claim that the threads' entire
acceptance criteria are resolved.

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
| Full typed-command scenario | Only member/equipment list routes are active; other family cases still return placeholders. | Isaac #90 and Duy #73, followed by Keith's full GUI/persistence acceptance. |

Full MVP compliance is a cross-owner claim. Mounting and refreshing real details establishes the
shell implementation boundary only. No model/storage, family switch, panel, parser or command
owned by another contributor was edited for these review fixes.

## Evidence

- [Shell behavior, checked screenshots and acceptance plan](ShellVerification.md).
- [Recorded actual current-route command session](ShellSession.txt); a view command still reports its real placeholder result.
- Generated reports: `build/reports/tests/test/`, `build/reports/tests/packagedShellTest/`,
  `build/reports/jacoco/coverage/` and `build/reports/laplace/`.
- MainWindow coverage: 221/221 lines, 91/94 branches; no skips or coverage exclusions.
- The standalone launch attempts reached normal startup but the desktop tool could not select
  the Java window. Their limitations and cleanup are recorded in ShellVerification.md.
- The existing `docs/AboutUs.md:61` trailing-whitespace warning remains outside this change's ownership.
