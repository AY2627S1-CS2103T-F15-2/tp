# Add a member

`member add nus/NUS_ID name/NAME phone/PHONE email/EMAIL`

Example: `member add nus/a0123456x name/Alex Tan phone/91234567 email/alex@example.com`

All four fields are required and may appear in any order, separated by spaces or tabs.
NUS IDs contain one letter, seven digits and an optional final letter, and are stored
in uppercase. Names, phones and emails follow the existing AB3 field validation.
Different NUS IDs may share a name. An existing NUS ID cannot be added again, even
with different casing or contact details.

Missing, blank, repeated and unknown parameters, and text before the first parameter,
are rejected. Legacy `n/`, `p/` and `e/` are not member-add aliases. Whitespace-delimited
tokens matching `[A-Za-z][A-Za-z0-9_-]*/` are reserved parameter markers, including
inside names. Quoting and escaping are not supported. URLs and slashes inside ordinary
words are not markers, but each field still has its own validation rules.

Success feedback includes the name and normalized NUS ID and requests member details.
Duplicate rejection leaves the model unchanged and preserves the domain failure cause.
The command uses the existing application persistence pipeline; it does not save itself.
An I/O save failure is reported by that pipeline and does not promise in-memory rollback.

## Component handoff and integration boundary

`MemberAddCommand(Member)` and `MemberAddCommandParser.parse(String)` depend on
Nicholas's display contract and Duy's `LaplaceArgumentParser`. The parser passes exactly
`PREFIX_NUS_ID`, `PREFIX_MEMBER_NAME`, `PREFIX_MEMBER_PHONE`, `PREFIX_MEMBER_EMAIL`
as required prefixes and an empty optional set.

`MemberCard(Member, int)` loads `MemberCard.fxml` and `MemberCard.css`, renders all
contact fields and the supplied count, and rejects null members and negative counts.
It wraps text, uses variable row height, and registers no activation handlers or
listeners. Duy owns sorting, selection and current holdings calculation; Keith owns
the shared theme and shell. Parent lists must allow taller rows for long values.

At baseline `6b85337bce1664ec2774f0aca86b9d51a749019b`, the shared parser, display
contract, theme and FX test helper are not merged. Family routing remains a placeholder.
Direct parser/command/storage verification is component integration. Full command
execution through `LogicManager` and shell navigation require their owners' integration.

## Validation evidence

The display and parser contracts are now merged and incorporated from team baseline
`cf479a15091483ffaf74118c9558d11b0511c3af` (display PR #80, parser PR #83).
The normal `gradlew.bat check coverage --console=plain` now compiles production code
but cannot compile `MemberCardTest` until Keith's UI foundation PR #81 supplies `FxTestUtil`.
No replacement helper or committed test exclusion has been introduced.

All 12 new command/parser/storage tests pass with zero failures, errors or skips using
an uncommitted init script excluding only `MemberCardTest` from test compilation:
`gradlew.bat -I build/member-add-work/command-tests.gradle test --tests '*MemberAddCommandTest' --tests '*MemberAddCommandParserTest' --tests '*MemberAddCommandIntegrationTest' checkstyleMain checkstyleTest --console=plain`.
Both Checkstyle tasks pass. This is component validation, not full CI acceptance.

At the original baseline, all 428 existing tests passed with the dependency-blocked new
files excluded by an uncommitted scoped build. The normal full suite and card tests
must be rerun once the UI foundation merges.

A standalone JavaFX preview runs the real card FXML and verifies fields, supplied counts,
distinct same-name IDs, misuse rejection, absent activation handlers and wrapping at
308px and 210px. It also passes using resources loaded from the scoped packaged JAR.
Visually inspected screenshots are available locally at
`build/member-add-work/card-308.png` and `build/member-add-work/card-210.png`.
They show the component CSS on a white surface; verification with Keith's actual shared
theme and parent ListView selection remains pending. Generated output is not committed.
