package seedu.address.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.equipment.exceptions.DuplicateEquipmentException;
import seedu.address.model.equipment.exceptions.EquipmentDeletedException;
import seedu.address.model.equipment.exceptions.EquipmentNotFoundException;
import seedu.address.model.loan.exceptions.DuplicateOpenLoanException;
import seedu.address.model.member.exceptions.DuplicateMemberException;
import seedu.address.model.member.exceptions.MemberDeletedException;
import seedu.address.model.member.exceptions.MemberNotFoundException;
import seedu.address.model.person.Person;

/**
 * A snapshot of all address book collections in the persisted JSON schema.
 */
@JsonRootName(value = "addressbook")
class JsonSerializableAddressBook {

    public static final String MESSAGE_DUPLICATE_PERSON = "Persons list contains duplicate person(s).";
    public static final String MESSAGE_DUPLICATE_MEMBER = "Members list contains duplicate NUS IDs.";
    public static final String MESSAGE_DUPLICATE_EQUIPMENT = "Equipment list contains duplicate UUIDs.";
    public static final String MESSAGE_DUPLICATE_OPEN_LOAN = "Loans list contains duplicate open equipment loans.";
    public static final String MESSAGE_MISSING_MEMBER = "Loan references a member that does not exist.";
    public static final String MESSAGE_MISSING_EQUIPMENT = "Loan references equipment that does not exist.";

    private final List<JsonAdaptedPerson> persons;
    private final List<JsonAdaptedMember> members;
    private final List<JsonAdaptedEquipment> equipment;
    private final List<JsonAdaptedLoan> loans;

    /**
     * Copies persisted arrays. Missing or null new arrays represent empty collections.
     * The legacy persons array remains required and is validated during conversion.
     */
    @JsonCreator
    public JsonSerializableAddressBook(@JsonProperty("persons") List<JsonAdaptedPerson> persons,
            @JsonProperty("members") List<JsonAdaptedMember> members,
            @JsonProperty("equipment") List<JsonAdaptedEquipment> equipment,
            @JsonProperty("loans") List<JsonAdaptedLoan> loans) {
        this.persons = persons == null ? null : new ArrayList<>(persons);
        this.members = members == null ? new ArrayList<>() : new ArrayList<>(members);
        this.equipment = equipment == null ? new ArrayList<>() : new ArrayList<>(equipment);
        this.loans = loans == null ? new ArrayList<>() : new ArrayList<>(loans);
    }

    /** Creates a snapshot unaffected by later changes to the source collections. */
    public JsonSerializableAddressBook(ReadOnlyAddressBook source) {
        persons = source.getPersonList().stream().map(JsonAdaptedPerson::new).toList();
        members = source.getMemberList().stream().map(JsonAdaptedMember::new).toList();
        equipment = source.getEquipmentList().stream().map(JsonAdaptedEquipment::new).toList();
        loans = source.getLoanList().stream().map(JsonAdaptedLoan::new).toList();
    }

    /**
     * Restores entities before loans and rejects the complete load on invalid data.
     *
     * @throws IllegalValueException if persisted entities, uniqueness, or references are invalid.
     */
    public AddressBook toModelType() throws IllegalValueException {
        if (persons == null) {
            throw new IllegalValueException("Persons list is missing.");
        }
        AddressBook addressBook = new AddressBook();
        restoreEntries("persons", persons, ignored -> "legacy person", adapted -> {
            Person person = adapted.toModelType();
            if (addressBook.hasPerson(person)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_PERSON);
            }
            addressBook.addPerson(person);
        });
        restoreEntries("members", members, JsonAdaptedMember::describeIdentity,
                adapted -> addressBook.addMember(adapted.toModelType()));
        restoreEntries("equipment", equipment, JsonAdaptedEquipment::describeIdentity,
                adapted -> addressBook.addEquipment(adapted.toModelType()));
        restoreEntries("loans", loans, JsonAdaptedLoan::describeIdentity,
                adapted -> addressBook.addLoan(adapted.toModelType()));
        return addressBook;
    }

    /**
     * Adds record context to conversion and root-validation failures without losing their causes.
     */
    private static <T> void restoreEntries(String collection, List<T> records, Function<T, String> identity,
            RecordRestorer<T> restorer) throws IllegalValueException {
        for (int index = 0; index < records.size(); index++) {
            T record = records.get(index);
            String context = collection + "[" + index + "] ("
                    + (record == null ? "identity unavailable" : identity.apply(record)) + "): ";
            try {
                requireEntry(record, collection);
                restorer.restore(record);
            } catch (IllegalValueException | DuplicateMemberException | DuplicateEquipmentException
                    | DuplicateOpenLoanException | MemberNotFoundException | EquipmentNotFoundException
                    | EquipmentDeletedException | MemberDeletedException exception) {
                throw new IllegalValueException(context + failureReason(exception), exception);
            }
        }
    }

    /**
     * Retains actionable root-validation wording while exposing the original exception as the cause.
     */
    private static String failureReason(Exception exception) {
        if (exception instanceof DuplicateMemberException) {
            return MESSAGE_DUPLICATE_MEMBER;
        }
        if (exception instanceof DuplicateEquipmentException) {
            return MESSAGE_DUPLICATE_EQUIPMENT;
        }
        if (exception instanceof DuplicateOpenLoanException) {
            return MESSAGE_DUPLICATE_OPEN_LOAN;
        }
        if (exception instanceof MemberNotFoundException) {
            return MESSAGE_MISSING_MEMBER;
        }
        if (exception instanceof EquipmentNotFoundException) {
            return MESSAGE_MISSING_EQUIPMENT;
        }
        if (exception instanceof EquipmentDeletedException) {
            return "Open loan references deleted equipment.";
        }
        if (exception instanceof MemberDeletedException) {
            return "Open loan references a deleted member.";
        }
        return exception.getMessage();
    }

    /**
     * Restores one serialized record, allowing checked value-validation failures.
     */
    @FunctionalInterface
    private interface RecordRestorer<T> {
        void restore(T record) throws IllegalValueException;
    }

    private static void requireEntry(Object entry, String collection) throws IllegalValueException {
        if (entry == null) {
            throw new IllegalValueException(collection + " list contains a null entry.");
        }
    }
}
