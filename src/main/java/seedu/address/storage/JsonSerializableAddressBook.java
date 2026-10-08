package seedu.address.storage;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.equipment.exceptions.DuplicateEquipmentException;
import seedu.address.model.equipment.exceptions.EquipmentNotFoundException;
import seedu.address.model.loan.exceptions.DuplicateOpenLoanException;
import seedu.address.model.member.exceptions.DuplicateMemberException;
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
        for (JsonAdaptedPerson adapted : persons) {
            requireEntry(adapted, "persons");
            Person person = adapted.toModelType();
            if (addressBook.hasPerson(person)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_PERSON);
            }
            addressBook.addPerson(person);
        }
        try {
            for (JsonAdaptedMember adapted : members) {
                requireEntry(adapted, "members");
                addressBook.addMember(adapted.toModelType());
            }
            for (JsonAdaptedEquipment adapted : equipment) {
                requireEntry(adapted, "equipment");
                addressBook.addEquipment(adapted.toModelType());
            }
            for (JsonAdaptedLoan adapted : loans) {
                requireEntry(adapted, "loans");
                addressBook.addLoan(adapted.toModelType());
            }
        } catch (DuplicateMemberException exception) {
            throw new IllegalValueException(MESSAGE_DUPLICATE_MEMBER, exception);
        } catch (DuplicateEquipmentException exception) {
            throw new IllegalValueException(MESSAGE_DUPLICATE_EQUIPMENT, exception);
        } catch (DuplicateOpenLoanException exception) {
            throw new IllegalValueException(MESSAGE_DUPLICATE_OPEN_LOAN, exception);
        } catch (MemberNotFoundException exception) {
            throw new IllegalValueException(MESSAGE_MISSING_MEMBER, exception);
        } catch (EquipmentNotFoundException exception) {
            throw new IllegalValueException(MESSAGE_MISSING_EQUIPMENT, exception);
        }
        return addressBook;
    }

    private static void requireEntry(Object entry, String collection) throws IllegalValueException {
        if (entry == null) {
            throw new IllegalValueException(collection + " list contains a null entry.");
        }
    }
}
