# Laplace contributor instructions

Applies throughout this repository. Checked against CS2103T AY2026/27 S1 guidance on 21 October 2026.

## Authority and scope

- Follow the user's current assignment and preserve unrelated working changes.
- This file distinguishes course requirements from project engineering practices. It is a working checklist, not a guarantee of marks; the linked course pages remain authoritative for assessment.
- Read the relevant implementation, tests, and current feature specification before editing. `docs/ScaffoldContract.md` and `docs/GuiCommandContract.md` contain historical milestones and ownership tables. Do not treat their obsolete "pending", "frozen", or "sole editor" statements as overriding a newly authorized assignment. Identify actual API/behavior conflicts and resolve those explicitly.
- Keep each change focused and the application usable. An audit or PR plan does not authorize creating GitHub issues/PRs, publishing, or merging. Work locally until those actions are requested.
- Do not silently change user-visible behavior, data-retention semantics, or persistence compatibility to make a test pass.

## Architecture and existing contracts

These are project practices based on the current implementation, not extra course grading rules.

- Entry points: `Main`/`MainApp`; command path: `MainWindow` → `LogicManager` → `AddressBookParser` → family/leaf parser → command → `Model`/`ModelManager` → `AddressBook`.
- JavaFX components are read-only views of model data. Use `CommandResult`/`DisplayRequest` for navigation; do not parse feedback text to decide which screen to show.
- Reuse `LaplaceArgumentParser`, `ParserUtil`, `LaplaceSortOrder`, `DirectoryQuery`, and the existing JavaFX test helper instead of introducing competing implementations.
- A physical equipment unit has one stable UUID. Matching descriptions are allowed and do not make units interchangeable. Preserve UUIDs across edits, persistence, soft deletion, and restoration.
- Member identity is `NusId`; loan references use member identity and equipment UUID. Never break those references by changing identity during a details edit.
- Availability and current holders are derived from condition and loans. Do not add independently persisted availability, holder, or aggregate inventory counts.
- Preserve the transaction boundary: a Laplace mutation declares `isTransactional()`, executes on an isolated snapshot, persists atomically, then publishes to the existing observable root. Parsing, validation, or save failure must leave live data and the previous file intact.
- Pure queries declare `isReadOnly()` and do not save. Inspect inherited commands carefully: the base `Command` defaults are not transactional or read-only.
- Keep entity/collection/root responsibilities distinct: immutable values; identity uniqueness in collections; cross-record consistency in the root/model; serialization in storage.
- Use the current JSON adapters and atomic file writer. Decide migration/default handling for missing fields before changing serialized schemas. Test loading earlier valid files.
- Maintain FX-thread discipline, stable selection by identity, and refreshes after loan/member/equipment changes. `select`/`refresh` must not trigger duplicate user-command callbacks.
- Existing AB3 `Person` commands and the legacy screen are transitional debt. Their removal is an authorized roadmap item, not permission for unrelated changes. Preserve useful infrastructure and shared `Name`, `Phone`, and `Email` types until consumers are migrated.

## Java conventions

Apply the [course Java standard](https://se-education.org/guides/conventions/java/index.html) and `config/checkstyle/checkstyle.xml`.

- Four-space indentation; continuation indentation eight spaces beyond the parent; 120-character maximum, preferably under 110.
- PascalCase types, camelCase methods/variables, uppercase underscore constants; boolean names read as predicates. Use descriptive English names and plural collection names.
- Braces around conditional/loop bodies; small scopes; one declaration/statement per line. Keep imports explicit, alphabetized within the configured groups, with static imports first. Remove unused imports.
- Use Javadoc for classes, public methods, and non-trivial private methods. Getters/setters, tests, and overrides with exactly inherited contracts have the documented exceptions.
- Method summaries describe behavior: “Returns”, “Adds”, or “Parses”, rather than an imperative instruction. Use multiline Javadoc with `/**` on its own line, meaningful contracts, and accurate failure descriptions. Avoid boilerplate parameters that add no information.
- Use American English in comments. Explain non-obvious reasons/invariants; remove obsolete handoff comments.

Checkstyle does not enforce every requirement. Manually review naming, abstraction, documentation accuracy, and duplication.

## Code quality and testing

The [grading criteria](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-grading.html) assess attributed code, defensive techniques, coding standards, abstraction levels, and duplication.

Project practices:

- Keep methods at one abstraction level. Extract meaningful shared behavior, including repetitive test setup, without creating needless frameworks.
- Use exceptions for invalid user input or recoverable failures; preserve causes. Use assertions for internal invariants, not as the only input validation. Add useful logging through `LogsCenter`; avoid logging sensitive user data unnecessarily.
- Check assertion enablement for the execution path being tested; Java `assert` is different from JUnit assertions.
- New behavior needs meaningful tests of outcomes, boundaries, invalid inputs, identity/reference integrity, and relevant persistence failures. Do not inflate test counts with redundant cases.
- For equipment lifecycle changes, test UUID preservation, active-loan blocking, retained history, deleted-record eligibility, save/reload, and rollback.
- For multi-unit creation, test unique IDs, collisions, invalid counts, and failure partway through creation. Decide and test batch atomicity.
- For parser changes, test missing/duplicate/unknown parameters, whitespace, extra arguments, and error-message accuracy.
- Do not weaken CI, suppress checks, delete valid tests, or silently skip GUI tests to make a build green. Update tests deliberately when approved behavior changes.
- Report what actually ran, what failed, and what was not checked. A passing suite does not establish compliance or absence of product defects.

The course expects automated tests but sets no minimum coverage percentage. Functional LoC is an effort estimate, not a target to inflate. See [expectations](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-expectations.html).

## Verification commands

Run from the repository root with Java 25:

```sh
java -version
./gradlew test --tests 'seedu.address.logic.commands.EquipmentAddCommandTest'
./gradlew check coverage
./gradlew shadowJar
```

- Choose targeted tests for the change; run `check coverage` for implementation integration. The existing Linux CI runs this under `xvfb-run --auto-servernum`; JavaFX tests need a graphical environment.
- `shadowJar` produces `build/libs/Laplace.jar`. Smoke-test releases from an empty writable folder with `java -jar Laplace.jar`.
- `.github/run-checks.sh` checks the Git index for whitespace/line-ending/EOF issues. It does not validate unstaged content; review the working diff too. Do not stage user changes merely to run these checks.
- For published documentation changes, use the existing lockfile and MarkBind setup: `npm --prefix docs ci`, then `npm --prefix docs run build`; inspect affected pages and rendered diagrams.
- Do not run an application against a user's real dataset during testing. Use isolated temporary data.
- Documentation-only instruction edits need link/content/diff review, not an unrelated application test suite.

## Product constraints

Source: [course constraints](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-constraints.html).

- Evolve AB3 incrementally; primarily OO; single-user product for fast typists.
- Support Java 25 on Windows, macOS, and Linux; no installer or dependency on an own remote server.
- Store human-editable local text data; no DBMS.
- Runtime file access stays within the JAR's folder/subfolders; do not change OS configuration.
- Deliver one JAR, or the permitted ZIP fallback, within 100 MB and without needless bloat.
- Third-party frameworks/libraries/services must be free, have permissive terms, require no user installation, and satisfy the other constraints; frameworks/libraries must also be open-source. Obtain teaching-team approval before first use unless already approved: approval for another team applies to this team too. Preserve the existing dependency setup unless a justified change is necessary.
- GUI must work well at 1920×1080+ with 100%/125% scaling, and remain usable at 1280×720+ with 150% scaling.
- CLI-first, testable features and minimal network dependency are course recommendations. Preserve command equivalents for important mouse actions.

The brownfield constraint allows incremental removal/replacement of AB3 functionality. It does not require retaining every legacy command. Do not perform a wholesale rewrite or rename packages merely to remove the “AddressBook” name.

## Documentation and UML

Source: [deliverables](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-deliverables.html).

- Keep main user documentation in `docs/UserGuide.md`, and developer documentation in `docs/DeveloperGuide.md`. Supplemental `docs/commands/` notes do not replace them.
- Match documented commands, validation, errors, screenshots, and manual-testing instructions to shipped behavior. Clearly distinguish proposals from implemented behavior.
- Store PlantUML sources in `docs/diagrams`; include diagrams in the DG and verify the rendered result. Use notation taught in the course, correct multiplicities/directions, and readable scopes.
- Update relevant user stories/use cases/NFRs and the manual-testing appendix.
- Cite reused ideas/code/docs/assets accurately. Maintain DG acknowledgements.
- PPP is optional this semester unless requested for contribution clarification; demo video is removed. Do not introduce either as a mandatory release gate.

## Attribution and Git workflow

Sources: [reuse/AI policy](https://nus-cs2103-ay2627-s1.github.io/website/admin/appendixB-policies.html), [RepoSense](https://nus-cs2103-ay2627-s1.github.io/website/admin/tools.html#tool-reposense-for-authorship-tracking), [GitHub workflow](https://nus-cs2103-ay2627-s1.github.io/website/admin/appendixE-gitHub.html).

- Generated work must be understood, reviewed and tested. Follow the user's attribution-placement instructions; do not add AI-agent usage acknowledgements to repository files unless explicitly requested.
- Do not fabricate authorship, reviews, test results, or contributions. Merging another person's PR is integration work, not authorship of all its code.
- Do not add unsolicited AI commit-message trailers. Preserve third-party source, library and asset credits.
- Check RepoSense attribution before feature freeze. Use truthful `@@author` corrections only where necessary; externally reused code follows the course's reuse marking rules.
- When authorized to create work on GitHub: use meaningful assigned issues and separate branches; assign both issues and PRs to their actual milestone. PR ownership is determined by author.
- From v1.4, branches directly in the team repository are allowed. Earlier iterations use forks.
- Retain PR branches. Avoid squash/rebase/history rewriting that distorts course progress timestamps. Do not backdate work.
- Reviews should contain specific, useful code-location comments. Merely merging or posting a conversation comment is not equivalent.

Commit messages follow [Git conventions](https://se-education.org/guides/conventions/git.html): imperative, capitalized subject, no terminal period, preferably ≤50 characters and at most 72. For non-trivial changes, explain the problem/reason and resulting behavior in a body; this is a project practice, not a claim that every course commit requires a body.

## Deadline-sensitive changes

Use Singapore time and recheck the current course schedule before deadline-sensitive work.

- v1.4: 22 October 2026, 23:59. [Week 10](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week10/project.html)
- v1.5 / feature freeze: 29 October 2026, 23:59. Finish substantial functional work, refactoring, and attribution corrections before then. [Week 11](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week11/project.html)
- v1.6: 3 November 2026, 14:00. [Week 12](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week12/project.html)

During feature freeze, per-person changed functional code is limited to `max(150 lines, 15% of final attributed functional LoC)`. This covers code files under `src/main`, including comments/blank lines; tests/docs/non-code files do not count. Use the freeze dashboard and allow a buffer; do not treat the published percentage tolerance as a development budget. Cosmetic refactors count too.

After final submission, the course prohibits code updates for 30 days without its permission, except approved late-submission work. Local user authorization does not itself establish course permission. During that period, identify the restriction and prepare a reviewable proposal without altering the frozen submitted code.

## Before handing work back

State the behavior changed, relevant files, actual verification, remaining risks, and required follow-up. Confirm that source, tests, UG/DG, UML, and acknowledgements are consistent where affected. Leave unrelated edits intact. Never claim that these instructions or green checks guarantee a grade.
