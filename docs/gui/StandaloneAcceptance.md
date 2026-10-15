> Historical #93/component evidence. The later v1.3.3 completion changes and current acceptance
> results are recorded in [MvpCompletion.md](MvpCompletion.md). Earlier save-error, validation,
> history-presentation and pending-route statements below describe their original baseline.

# Standalone acceptance record - 15 October 2026

Environment: ARM macOS, Zulu Java 25.0.3, JavaFX 17.0.7 matching ARM native jars.
Integration baseline: `d5bd893701b8384db331e4f78a108387fd333490`, incorporating team master
`0efb388f8a0daa180481dc6f049705bbd9a93add`. Final reviewed source edits are on top of that merge;
the published final head must be recorded after commit/push.

Final tested JAR SHA-256: `200423092e0bb83d2fd8f9fc787b2f7ca260859e9cf139ce7c080d2d8dad0f8c`.

Temporary launcher: `/private/tmp/laplace-native-launch/LaplaceReview.app`.
Temporary data directory: `/private/tmp/laplace-interactive-review`.
The native launcher loads the actual `addressbook.jar` and matching JavaFX jars; it changes no
application source or command handling. Matching native jars precede the shaded JAR. No launcher is shipped.

## Completed final-build observations

| Action | Observed result |
| --- | --- |
| Launch final packaged Main in isolated directory | Laplace window; real sample Members; first Alex identity selected; directory summary; ready feedback; command input focused. No FXML/resource failure. |
| Compare startup with reference | Charcoal header, light directory, blue selected row, green feedback and fixed bottom input; corrected 28% list/72% summary proportion in actual native screenshot. |
| Tab five times from command input, then Space | `member view nus/A0123456X` once in application log; real member details, phone/email, holdings, holder UUID and loan dates. Focus remains on selected row and input stays empty. |
| Enter on already selected row | One further exact member-view dispatch; same real details/identity; row focus preserved. |
| Activate Equipment tab | Actual equipment list/summary with 3 total, 1 available, 1 assigned and 1 unavailable; command input focused. |

## Earlier-build observations and fixes

Before the final keyboard/layout fixes, actual typed `equipment list extra` showed the parser error,
retained the input/current member details and focus. Replacing it with `equipment list` cleared input
and showed real Equipment. Enter/Space opened real item details including notes, and Loans showed the
real open loan and member/equipment identity links. Typed `exit` logged MainApp stop and the process
exited successfully. Those observations validate the earlier build only.

Interactive testing found two defects that are now fixed and regression-tested: Space's trailing key
was leaking into command input after immediate focus transfer, and production show-before-fill startup
reset the split wider than intended. Final native observations above confirm both corrections.

## Remaining final-build acceptance

The Mac locked during the first attempt. After it unlocked, final-build keyboard/startup checks
completed, but native pointer targeting later returned `noWindowsAvailable` and the next connection
reported the Mac locked again. This is an explicit tool/environment block, not completed acceptance.

- Final-build pointer click on rows/loan links; complete all navigation and legacy screen.
- Help/F1 and Exit on the final build.
- Native small-window, empty/long/error inspection and scrolling.
- Standalone typed add/assign/return/delete and fresh-process persistence reload.
- Exact published final-head Linux/macOS/Windows team CI, repository checks and coverage links.

The complete ten-route scenario, rejection/state/save snapshots, all mounted view layouts, Help/F1,
Exit, legacy, small/long/empty/missing states and fresh model/logic/window JSON reload do pass in the
27 packaged FX tests (and the full 692-test suite). That evidence is separate from the incomplete
standalone steps above. #75 remains open, as do the newer-MVP owner dependencies in ReviewResponse.md.
