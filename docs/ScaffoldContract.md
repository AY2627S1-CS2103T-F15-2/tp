> v1.3.3 amendment: schema and existing constructor/getter contracts remain unchanged. New member
> input uses member-specific value factories; saved legacy values remain readable without rewriting.
> Model now supplies an isolated transaction snapshot; Logic commits its saved data through the existing
> root. See [MVP fields and persistence](commands/MvpFields.md).

# Laplace shared scaffold contract

This document is the authoritative contract for the member, equipment, and loan scaffold. The baseline was created from PR #54 commit `d27afec19dfd2d4cb156e6dff2f93731ba7b0562`. Existing person records, commands, and persisted person data must continue to work.

Implementation status on the v1.3.2 branch, based on team master `900cee6fe5ce3aadbf3fba3be5dbff346d481409` (PR #85): domain collections, root integrity, command-facing Model operations and JSON persistence are implemented. Top-level Laplace family routing is connected; member/equipment list routes are active, while the other family routes still return explicit `NotImplementedCommand` results. Concrete add/delete commands and the v1.3.2 view commands/panels are implemented independently; route activation and shell integration remain separate work. The new v1.3 display and GUI contracts are documented separately in [GuiCommandContract.md](GuiCommandContract.md).

## Domain rules

- A `Member` is identified by its normalized `NusId`. Names do not establish identity.
- An `Equipment` item is identified by its `UUID`. Names do not establish identity.
- Identity comparison and full-value equality remain separate operations.
- Required constructor arguments reject `null`.
- Member name, phone, and email continue to use the existing person value objects and validation.
- Equipment `name` and `category` are required command inputs. Their parsers trim surrounding whitespace and reject a blank result. `notes` is optional; an omitted value becomes `""`. Domain constructors do not trim text.
- A loan is open when `returnedDate` is absent. The Java representation of an absent return date is `null`, exposed through `Optional`.
- `expectedReturnDate` must be on or after `assignedDate`. A present `returnedDate` must be on or after `assignedDate`. Same-day, early, and late returns are valid. `Loan` owns these intrinsic ordering rules. Whether a date is in the future relative to today is a command-level rule.
- Closing a loan replaces the immutable object using `Loan.withReturnedDate(LocalDate)`.
- Availability is derived and is never stored. An open loan means `ASSIGNED`. Without an open loan, `GOOD` and `FAIR` mean `AVAILABLE`; `DAMAGED` and `UNDER_REPAIR` mean `UNAVAILABLE`. `AvailabilityCalculator.calculate` rejects a null condition.

## Collection contracts

`UniqueMemberList` exposes:

```java
boolean contains(NusId nusId);
Optional<Member> findByNusId(NusId nusId);
void add(Member member);
void remove(Member member);
void setMembers(List<Member> members);
ObservableList<Member> asUnmodifiableObservableList();
```

`UniqueEquipmentList` exposes:

```java
boolean contains(UUID uuid);
Optional<Equipment> findByUuid(UUID uuid);
void add(Equipment equipment);
void remove(Equipment equipment);
void setEquipment(List<Equipment> equipment);
ObservableList<Equipment> asUnmodifiableObservableList();
```

`UniqueLoanList` exposes:

```java
Optional<Loan> findOpenLoan(UUID equipmentUuid);
List<Loan> findOpenLoansForMember(NusId nusId);
void add(Loan loan);
void closeLoan(UUID equipmentUuid, LocalDate returnedDate);
void setLoans(List<Loan> loans);
ObservableList<Loan> asUnmodifiableObservableList();
```

All arguments reject `null`. Member and equipment removal uses identity, even if other fields differ. A member NUS ID and equipment UUID must each be unique. `DuplicateMemberException`, `MemberNotFoundException`, `DuplicateEquipmentException`, and `EquipmentNotFoundException` report those failures.

At most one open loan may exist for an equipment UUID. Closed history may contain multiple loans for the same equipment, and one member may hold several different equipment items. Adding a second open loan throws `DuplicateOpenLoanException`; closing an equipment item without an open loan throws `OpenLoanNotFoundException`.

Failed writes leave the collection unchanged. Bulk setters validate the complete replacement before changing state and copy caller-owned lists. Observable views cannot be mutated by callers. `findOpenLoansForMember` returns an unmodifiable snapshot.

## Root model contracts

`AddressBook` owns the three collections and exposes member, equipment, and loan operations declared in the baseline. `ReadOnlyAddressBook` preserves `getPersonList()` and adds `getMemberList()`, `getEquipmentList()`, and `getLoanList()`. `Model` exposes the corresponding command-facing operations, using `deleteMember` and `deleteEquipment`, and adds `getAvailability(UUID)`.

The implemented root model enforces the following contracts:

- Preserve the legacy persons collection and all working person-command paths.
- Reject a loan whose member or equipment identity does not exist.
- Block deletion only while the member or equipment is referenced by an open loan.
- Retain soft-deleted members and all their loan history; reserve their identities.
- Permit unlimited member restoration; reject open loans assigned to deleted members.
- Remove related closed loans only after permitted equipment deletion.
- Throw `EquipmentNotFoundException` when availability is requested for an unknown UUID.
- Delegate the one-open-loan rule to `UniqueLoanList` and availability rules to `AvailabilityCalculator`.
- Include every collection in reset, equality, hashing, and string output.
- Copy collection containers and validate complete replacement data and references before committing a reset. A rejected reset leaves existing state unchanged.

The root operations are implemented and delegated through ModelManager. Read-only observable collection views remain live across model updates; failed operations preserve consistent state.

## JSON contracts

The adapter raw constructors and JSON fields are:

```java
JsonAdaptedMember(String nusId, String name, String phone, String email)
JsonAdaptedEquipment(String uuid, String name, String category, String condition, String notes)
JsonAdaptedLoan(String equipmentUuid, String memberNusId, String assignedDate,
        String expectedReturnDate, String returnedDate)
```

Each adapter also has an entity constructor and `toModelType() throws IllegalValueException`. Required null or malformed fields produce `IllegalValueException`. A missing or null equipment `notes` value becomes `""`. A missing or null loan `returnedDate` represents an open loan. Dates use ISO calendar text such as `2026-10-06`; implementations should use `LocalDate` ISO parsing rather than a `YYYY-MM-DD` formatter pattern. Enum names use the declared uppercase values. UUID text uses the standard hyphenated representation.

The implemented root schema is:

```json
{
  "persons": [],
  "members": [
    {"nusId":"A0123456X","name":"Alex Tan","phone":"91234567","email":"alex@example.com"}
  ],
  "equipment": [
    {"uuid":"550e8400-e29b-41d4-a716-446655440000","name":"Camera","category":"Photography","condition":"GOOD","notes":""}
  ],
  "loans": [
    {"equipmentUuid":"550e8400-e29b-41d4-a716-446655440000","memberNusId":"A0123456X","assignedDate":"2026-10-06","expectedReturnDate":"2026-10-13","returnedDate":null}
  ]
}
```

Existing `persons` data remains supported. A missing or null new root array means an empty collection. Root loading converts members and equipment before loans, then rejects invalid references and duplicate open loans. Availability is absent from JSON. These adapters are connected to `JsonSerializableAddressBook`, including root reference validation and legacy-person compatibility.

## Parser contracts

The shared Laplace prefixes are fixed as follows. Existing person-command prefixes keep their existing tokens.

| Constant | Token |
| --- | --- |
| `PREFIX_NUS_ID` | `nus/` |
| `PREFIX_MEMBER_NAME` | `name/` |
| `PREFIX_MEMBER_PHONE` | `phone/` |
| `PREFIX_MEMBER_EMAIL` | `email/` |
| `PREFIX_EQUIPMENT_UUID` | `uuid/` |
| `PREFIX_CATEGORY` | `category/` |
| `PREFIX_CONDITION` | `condition/` |
| `PREFIX_NOTES` | `notes/` |
| `PREFIX_ASSIGNED_DATE` | `assigned/` |
| `PREFIX_EXPECTED_RETURN_DATE` | `expected/` |
| `PREFIX_RETURNED_DATE` | `returned/` |

`ParserUtil` declares `parseNusId`, `parseUuid`, `parseDate`, and `parseCondition`. Each trims surrounding whitespace. NUS IDs retain their established validation and normalize to uppercase. UUID parsing accepts Java's standard UUID syntax. Dates accept ISO `yyyy-MM-dd`. Conditions are case-insensitive enum names; an implementation may convert spaces and hyphens to underscores. Malformed or blank values produce `ParseException` with a useful field-specific message.

`MemberCommandParser`, `EquipmentCommandParser`, and `LoansCommandParser` implement the existing `Parser<Command>` interface. The routes are `member add`, `member list`, `member view`, `member delete`, `equipment add`, `equipment list`, `equipment view`, `equipment delete`, `loans add`, and `loans return`. Missing or unknown families and subcommands are rejected with `ParseException`. A recognized route awaiting its business command may return `NotImplementedCommand`, which reports the unfinished feature without mutating `Model`. Top-level routing is connected in `AddressBookParser`; member/equipment list routes are active, while the other recognized routes currently return placeholders. Legacy parsing remains functional. Duy owns activation of the feature routes after their implementations merge.

## Ownership and handoff

The table below records ownership of the completed scaffold foundation. For the new command and GUI milestone, use the owner matrix in [GuiCommandContract.md](GuiCommandContract.md):

| Owner | Files and responsibility | Independent verification | Integration dependency |
| --- | --- | --- | --- |
| Keegan Gan (`Keegan4`) | `model/member/UniqueMemberList.java`, member exceptions, `storage/JsonAdaptedMember.java`, focused tests | Collection and adapter unit tests | Root persistence later consumes the adapter |
| Arjo (`ArjoDas`) | `model/equipment/UniqueEquipmentList.java`, equipment exceptions, `AvailabilityCalculator.java`, `storage/JsonAdaptedEquipment.java`, focused tests | Collection, calculator, and adapter unit tests | Root model consumes the calculator |
| Isaac Choong (`isaaaxe`) | `model/loan/UniqueLoanList.java`, loan exceptions, `storage/JsonAdaptedLoan.java`, focused tests; later root JSON integration and storage tests | Collection and adapter unit tests | Root storage integration follows real collections and root model |
| Keith Chia (`wtvlol`) | `AddressBook.java`, `ReadOnlyAddressBook.java`, root tests and affected read-only test doubles | Root tests using controlled collection inputs | Real collection behavior is needed for final integration tests |
| Nicholas Vun (`kimjunkuno`) | `Model.java`, `ModelManager.java`, model tests and affected `Model` test doubles | Delegation tests using a controlled root model | Real root and calculator behavior is needed for final integration tests |
| Nguyen Anh Duy (`duynguyen21007`) | `CliSyntax.java`, `ParserUtil.java`, `AddressBookParser.java`, three family parsers, `NotImplementedCommand.java`, focused tests | Parser utilities, routing, and non-mutation tests | Real commands replace placeholders later |

Each owner should avoid editing another row's files without coordinating first. Test data should live in the owner's package-specific test utility instead of one shared fixture that all contributors must edit. Existing compatibility test doubles should gain only the methods needed by the interface compiler. No dependency-injection framework or mocking library is required.

## Completion milestones

**A. Shared baseline ready:** this document and the declarations compile, legacy checks pass, and all six contributors can start implementing against fixed contracts. Placeholder methods may still throw.

**B. Functional scaffold complete:** the collections, adapters, root model, and storage are implemented and connected. Only business-command placeholders may remain. A real integration test must add members and equipment through `ModelManager`, add and close loans, observe availability transitions, save and reload all identifiers, dates, and loan history, reject invalid references and prohibited deletions, and prove that rejected operations leave state unchanged.

The integrated foundation now implements milestone B; its existing tests cover real model/storage behavior. This status does not imply that the v1.3 business commands, display consumers or GUI acceptance are complete. Those additions follow the staged handoff in [GuiCommandContract.md](GuiCommandContract.md).

## Member lifecycle extension

The member soft-delete branch supersedes earlier permanent-member-deletion behavior. A boolean
`deleted` field retains the record indefinitely; missing legacy flags mean active, while explicit
null and non-boolean values are rejected. `member restore` has no time limit. Details are in
[the lifecycle contract](commands/member-restore.md). The separate member-edit branch remains independent.
