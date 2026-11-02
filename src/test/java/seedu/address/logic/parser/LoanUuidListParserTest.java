package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.LoanAddCommand;
import seedu.address.logic.commands.LoanReturnCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.member.NusId;

/**
 * Checks batch identities through the shared parser and production command routing.
 */
class LoanUuidListParserTest {
    private static final UUID FIRST = new UUID(0, 1);
    private static final UUID SECOND = new UUID(0, 2);
    private static final List<UUID> IDENTIFIERS = List.of(FIRST, SECOND);
    private static final LocalDate DATE = LocalDate.of(2026, 10, 15);

    @Test
    void parse_unicodeWhitespaceAroundEntries_preservesOrderAndBatchCommands() throws Exception {
        for (String space : List.of(" ", "\t", "\u00a0", "\u2003", "\u202f", " \u00a0\t\u202f")) {
            String value = space + FIRST + space + "," + space + SECOND + space;
            assertEquals(IDENTIFIERS, LoanUuidListParser.parse(value));
            assertEquals(LoanAddCommand.batch(IDENTIFIERS, new NusId("A0123456X"), DATE, DATE),
                    new AddressBookParser().parseCommand("loans add uuid/" + value
                            + " nus/A0123456X assigned/2026-10-15 expected/2026-10-15"));
            assertEquals(new LoanReturnCommand(IDENTIFIERS, DATE, null),
                    new AddressBookParser().parseCommand("loans return uuid/" + value + " returned/2026-10-15"));
        }
    }

    @Test
    void parse_invalidOrDuplicateEntries_stillRejectsWithoutRemovingInternalWhitespace() {
        for (String value : List.of("", "\u00a0", FIRST + ",\u202f", "," + FIRST,
                FIRST + ",, " + SECOND, FIRST + ",\u00a0" + FIRST,
                FIRST + "," + SECOND.toString().replaceFirst("-", "-\u00a0"))) {
            assertThrows(ParseException.class, () -> LoanUuidListParser.parse(value));
        }
    }
}
