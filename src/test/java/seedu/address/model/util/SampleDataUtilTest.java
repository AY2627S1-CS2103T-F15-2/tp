package seedu.address.model.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.equipment.Availability;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.loan.Loan;
import seedu.address.model.member.Member;
import seedu.address.storage.JsonAddressBookStorage;

public class SampleDataUtilTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-12T18:00:00Z"),
            ZoneId.of("Asia/Singapore"));

    @TempDir
    public java.nio.file.Path testFolder;

    @Test
    public void getSampleAddressBook_preservesLegacyPersonsAndAddsUniqueMembers() {
        ReadOnlyAddressBook book = SampleDataUtil.getSampleAddressBook(CLOCK);
        assertEquals(Arrays.asList(SampleDataUtil.getSamplePersons()), book.getPersonList());
        assertEquals(List.of("Alex Tan", "Jamie Lim", "Priya Nair"), book.getMemberList().stream()
                .map(member -> member.getName().fullName).toList());
        assertEquals(3, book.getMemberList().stream().map(Member::getNusId).distinct().count());
        assertEquals(3, book.getEquipmentList().stream().map(Equipment::getUuid).distinct().count());
    }

    @Test
    public void getSampleAddressBook_hasSeparateAssignmentAndReturnItemsAndValidReferences() {
        ModelManager model = new ModelManager(SampleDataUtil.getSampleAddressBook(CLOCK), new UserPrefs());
        Loan loan = model.getLoanList().get(0);
        assertTrue(loan.isOpen());
        assertTrue(model.hasMember(loan.getMemberNusId()));
        assertTrue(model.hasEquipment(loan.getEquipmentUuid()));
        assertEquals(Availability.ASSIGNED, model.getAvailability(loan.getEquipmentUuid()));
        assertTrue(model.getEquipmentList().stream()
                .anyMatch(item -> model.getAvailability(item.getUuid()) == Availability.AVAILABLE));
        assertTrue(model.getEquipmentList().stream()
                .anyMatch(item -> model.getAvailability(item.getUuid()) == Availability.UNAVAILABLE));
        Equipment available = model.getEquipmentList().get(0);
        Equipment assigned = model.getEquipmentList().get(1);
        assertEquals(available.getName(), assigned.getName());
        assertNotEquals(available.getUuid(), assigned.getUuid());
    }

    @Test
    public void getSampleAddressBook_clockControlsValidDatesInItsLocalZone() {
        for (String instant : List.of("2020-02-29T18:00:00Z", "2026-10-12T18:00:00Z", "2031-01-01T00:00:00Z")) {
            Clock clock = Clock.fixed(Instant.parse(instant), CLOCK.getZone());
            Loan loan = SampleDataUtil.getSampleAddressBook(clock).getLoanList().get(0);
            LocalDate today = LocalDate.now(clock);
            assertEquals(today.minusDays(2), loan.getAssignedDate());
            assertEquals(today.plusDays(5), loan.getExpectedReturnDate());
            assertFalse(loan.getAssignedDate().isAfter(today));
            assertFalse(loan.getExpectedReturnDate().isBefore(loan.getAssignedDate()));
        }
    }

    @Test
    public void getSampleAddressBook_freshBooksHaveDeterministicIdentitiesAndIndependentState() {
        AddressBook first = new AddressBook(SampleDataUtil.getSampleAddressBook(CLOCK));
        AddressBook second = new AddressBook(SampleDataUtil.getSampleAddressBook(CLOCK));
        assertEquals(first, second);
        first.closeLoan(first.getLoanList().get(0).getEquipmentUuid(), LocalDate.now(CLOCK));
        first.removeEquipment(first.getEquipmentList().get(0));
        assertTrue(second.getLoanList().get(0).isOpen());
        assertEquals(3, second.getEquipmentList().size());
    }

    @Test
    public void getSampleAddressBook_realStorageRoundTripPreservesAllSampleRelationships() throws Exception {
        ReadOnlyAddressBook book = SampleDataUtil.getSampleAddressBook(CLOCK);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(testFolder.resolve("sample.json"));
        storage.saveAddressBook(book);
        assertEquals(new AddressBook(book), new AddressBook(storage.readAddressBook().orElseThrow()));
    }

    @Test
    public void getSampleAddressBook_defaultFactoryUsesCurrentLocalDate() {
        LocalDate before = LocalDate.now();
        Loan loan = SampleDataUtil.getSampleAddressBook().getLoanList().get(0);
        LocalDate after = LocalDate.now();
        assertFalse(loan.getAssignedDate().isBefore(before.minusDays(2)));
        assertFalse(loan.getAssignedDate().isAfter(after.minusDays(2)));
        assertFalse(loan.getExpectedReturnDate().isBefore(before.plusDays(5)));
        assertFalse(loan.getExpectedReturnDate().isAfter(after.plusDays(5)));
    }

    @Test
    public void getSampleAddressBook_nullClock_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> SampleDataUtil.getSampleAddressBook(null));
    }
}
