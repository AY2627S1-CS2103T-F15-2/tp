# Laplace shared scaffold contract

This document is the authoritative contract for the member, equipment, and loan scaffold. The baseline was created from PR #54 commit `d27afec19dfd2d4cb156e6dff2f93731ba7b0562`. Existing AddressBook Level 3 persons, commands, and persisted person data must continue to work.

The declarations in this baseline deliberately throw `UnsupportedOperationException` where the assigned implementation is unfinished. A declaration proves that another contributor can compile against the signature; it does not prove that the feature works.

## Domain rules

- A `Member` is identified by its normalized `NusId`. Names do not establish identity.
- An `Equipment` item is identified by its `UUID`. Names do not establish identity.
- Identity comparison and full-value equality remain separate operations.
- Required constructor arguments reject `null`.
- Member name, phone, and email continue to use the existing AB3 value objects and validation.
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

The implemented root model must:

- Preserve the legacy persons collection and all working AB3 paths.
- Reject a loan whose member or equipment identity does not exist.
- Block deletion only while the member or equipment is referenced by an open loan.
- Remove related closed loans after a permitted member or equipment deletion.
- Throw `EquipmentNotFoundException` when availability is requested for an unknown UUID.
- Delegate the one-open-loan rule to `UniqueLoanList` and availability rules to `AvailabilityCalculator`.
- Include every collection in reset, equality, hashing, and string output.
- Copy collection containers and validate complete replacement data and references before committing a reset. A rejected reset leaves existing state unchanged.

The current root methods are explicit placeholders. They are not connected to fabricated empty entities or silent successful writes.

## JSON contracts

The adapter raw constructors and JSON fields are:

```java
JsonAdaptedMember(String nusId, String name, String phone, String email)
JsonAdaptedEquipment(String uuid, String name, String category, String condition, String notes)
JsonAdaptedLoan(String equipmentUuid, String memberNusId, String assignedDate,
        String expectedReturnDate, String returnedDate)
```

Each adapter also has an entity constructor and `toModelType() throws IllegalValueException`. Required null or malformed fields produce `IllegalValueException`. A missing or null equipment `notes` value becomes `""`. A missing or null loan `returnedDate` represents an open loan. Dates use ISO calendar text such as `2026-10-06`; implementations should use `LocalDate` ISO parsing rather than a `YYYY-MM-DD` formatter pattern. Enum names use the declared uppercase values. UUID text uses the standard hyphenated representation.

The future root schema is:

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

Existing `persons` data remains supported. A missing or null new root array means an empty collection. Root loading converts members and equipment before loans, then rejects invalid references and duplicate open loans. Availability is absent from JSON. These adapters are intentionally not connected to `JsonSerializableAddressBook`; Isaac owns that later integration.

## Parser contracts

The shared Laplace prefixes are fixed as follows. Legacy AB3 prefixes keep their existing tokens.

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

`ParserUtil` declares `parseNusId`, `parseUuid`, `parseDate`, and `parseCondition`. Each trims surrounding whitespace. NUS IDs retain their established validation and normalize to uppercase. UUID parsing accepts Java's standard UUID syntax. Dates accept ISO `uuuu-MM-dd`. Conditions are case-insensitive enum names; an implementation may convert spaces and hyphens to underscores. Malformed or blank values produce `ParseException` with a useful field-specific message.

`MemberCommandParser`, `EquipmentCommandParser`, and `LoansCommandParser` implement the existing `Parser<Command>` interface. The routes are `member add`, `member list`, `member view`, `member delete`, `equipment add`, `equipment list`, `equipment view`, `equipment delete`, `loans add`, and `loans return`. Missing or unknown families and subcommands are rejected with `ParseException`. A recognized route awaiting its business command may return `NotImplementedCommand`, which reports the unfinished feature without mutating `Model`. Top-level routing is not connected in this baseline, so legacy parsing remains functional.

## Ownership and handoff

Tab 8's instructions to create files now mean implement the supplied scaffold. Ownership of implementation remains:

| Owner | Files and responsibility | Independent verification | Integration dependency |
| --- | --- | --- | --- |
| Keegan Gan (`Keegan4`) | `model/member/UniqueMemberList.java`, member exceptions, `storage/JsonAdaptedMember.java`, focused tests | Collection and adapter unit tests | Root persistence later consumes the adapter |
| Arjo (`ArjoDas`) | `model/equipment/UniqueEquipmentList.java`, equipment exceptions, `AvailabilityCalculator.java`, `storage/JsonAdaptedEquipment.java`, focused tests | Collection, calculator, and adapter unit tests | Root model consumes the calculator |
| Isaac Choong (`isaaaxe`) | `model/loan/UniqueLoanList.java`, loan exceptions, `storage/JsonAdaptedLoan.java`, focused tests; later root JSON integration and storage tests | Collection and adapter unit tests | Root storage integration follows real collections and root model |
| Keith Chia (`wtvlol`) | `AddressBook.java`, `ReadOnlyAddressBook.java`, root tests and affected read-only test doubles | Root tests using controlled collection inputs | Real collection behavior is needed for final integration tests |
| Nicholas Vun (`kimjunkuno`) | `Model.java`, `ModelManager.java`, model tests and affected `Model` test doubles | Delegation tests using a controlled root model | Real root and calculator behavior is needed for final integration tests |
| Nguyen Anh Duy (`duynguyen21007`) | `CliSyntax.java`, `ParserUtil.java`, `AddressBookParser.java`, three family parsers, `NotImplementedCommand.java`, focused tests | Parser utilities, routing, and non-mutation tests | Real commands replace placeholders later |

Each owner should avoid editing another row's files without coordinating first. Test data should live in the owner's package-specific test utility instead of one shared fixture that all contributors must edit. Existing AB3 test doubles should gain only the methods needed by the interface compiler. No dependency-injection framework or mocking library is required.

## Completion milestones

**A. Shared baseline ready:** this document and the declarations compile, legacy checks pass, and all six contributors can start implementing against fixed contracts. Placeholder methods may still throw.

**B. Functional scaffold complete:** the collections, adapters, root model, and storage are implemented and connected. Only business-command placeholders may remain. A real integration test must add members and equipment through `ModelManager`, add and close loans, observe availability transitions, save and reload all identifiers, dates, and loan history, reject invalid references and prohibited deletions, and prove that rejected operations leave state unchanged.

This baseline targets milestone A only.
