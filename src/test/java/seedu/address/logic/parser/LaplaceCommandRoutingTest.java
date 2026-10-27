package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.DirectorySearchCommand;
import seedu.address.logic.commands.EquipmentAddCommand;
import seedu.address.logic.commands.EquipmentDeleteCommand;
import seedu.address.logic.commands.EquipmentListCommand;
import seedu.address.logic.commands.EquipmentViewCommand;
import seedu.address.logic.commands.LoanAddCommand;
import seedu.address.logic.commands.LoanListCommand;
import seedu.address.logic.commands.LoanReturnCommand;
import seedu.address.logic.commands.MemberAddCommand;
import seedu.address.logic.commands.MemberDeleteCommand;
import seedu.address.logic.commands.MemberEditCommand;
import seedu.address.logic.commands.MemberListCommand;
import seedu.address.logic.commands.MemberRestoreCommand;
import seedu.address.logic.commands.MemberViewCommand;
import seedu.address.logic.parser.exceptions.ParseException;

public class LaplaceCommandRoutingTest {

    @Test
    public void parseCommand_missingOrUnknown_throwsParseException() {
        AddressBookParser parser = new AddressBookParser();
        for (String input : new String[] {"", " \t", "member", "equipment \t", "loans", "equipments list",
            "equipmentx list", "loan add", "member unknown", "equipment return", "loans unknown",
            "member lists", "equipment ADD", "loans Return", "loans LIST extra", "member addExtra"}) {
            assertThrows(ParseException.class, () -> parser.parseCommand(input));
        }
    }

    @Test
    public void parseFamilies_missingUnknownOrNull_rejectsInput() {
        for (Parser<Command> parser : List.<Parser<Command>>of(new MemberCommandParser(),
                new EquipmentCommandParser(), new LoansCommandParser())) {
            for (String input : new String[] {"", " \n\t", "unknown", "ADD", "nus/A0123456X"}) {
                assertThrows(ParseException.class, () -> parser.parse(input));
            }
            assertThrows(NullPointerException.class, () -> parser.parse(null));
        }
    }

    @Test
    public void parseCommand_integratedEditAndSearchRoutes_preservesBothAndListsBothInDiagnostic() throws Exception {
        AddressBookParser parser = new AddressBookParser();
        assertInstanceOf(MemberEditCommand.class, parser.parseCommand("member edit nus/A0123456X name/Jamie Lim"));
        assertInstanceOf(DirectorySearchCommand.class, parser.parseCommand("member search q/jamie"));
        assertInstanceOf(MemberRestoreCommand.class, parser.parseCommand("member restore nus/A0123456X"));
        String expected = "Expected member subcommand: add, edit, list, search, view, delete, restore.";
        assertThrows(ParseException.class, expected, () -> parser.parseCommand("member unknown"));
    }

    @Test
    public void parseCommand_listRoutes_returnsRealCommandsAndRejectsArguments() throws Exception {
        AddressBookParser parser = new AddressBookParser();
        assertInstanceOf(MemberListCommand.class, parser.parseCommand(" \tmember\t list \t"));
        assertInstanceOf(EquipmentListCommand.class, parser.parseCommand("equipment list"));
        assertInstanceOf(LoanListCommand.class, parser.parseCommand("loans list"));
        assertInstanceOf(MemberListCommand.class, new MemberCommandParser().parse("list \t"));
        assertInstanceOf(EquipmentListCommand.class, new EquipmentCommandParser().parse("\tlist"));
        for (String route : new String[] {"member list", "equipment list", "loans list"}) {
            for (String args : new String[] {"extra", "nus/A0123456X", "notes/ notes/", "unknown/value"}) {
                assertThrows(ParseException.class, () -> parser.parseCommand(route + "\t" + args));
            }
        }
    }

    @Test
    public void parseCommand_allTenRoutes_dispatchToConcreteParsers() throws Exception {
        Map<String, Class<? extends Command>> routes = Map.of(
                "member add nus/A0123456X name/Alex Tan phone/91234567 email/alex@example.com", MemberAddCommand.class,
                "member view nus/A0123456X", MemberViewCommand.class,
                "member delete nus/A0123456X", MemberDeleteCommand.class,
                "member list", MemberListCommand.class,
                "equipment add name/Camera category/Photography condition/good notes/", EquipmentAddCommand.class,
                "equipment view uuid/550e8400-e29b-41d4-a716-446655440000", EquipmentViewCommand.class,
                "equipment delete uuid/550e8400-e29b-41d4-a716-446655440000", EquipmentDeleteCommand.class,
                "equipment list", EquipmentListCommand.class,
                "loans add uuid/550e8400-e29b-41d4-a716-446655440000 nus/A0123456X"
                        + " assigned/2026-01-01 expected/2026-01-02", LoanAddCommand.class,
                "loans return uuid/550e8400-e29b-41d4-a716-446655440000 returned/2026-01-02", LoanReturnCommand.class);
        for (var route : routes.entrySet()) {
            String input = route.getKey();
            assertInstanceOf(route.getValue(), new AddressBookParser().parseCommand(input));
            String[] parts = input.split(" ", 2);
            assertInstanceOf(route.getValue(), new AddressBookParser().parseCommand(
                    " \t" + input.replaceFirst(" ", "\t ") + " \t"));
            Parser<Command> family = switch (parts[0]) {
                case "member" -> new MemberCommandParser();
                case "equipment" -> new EquipmentCommandParser();
                default -> new LoansCommandParser();
            };
            assertInstanceOf(route.getValue(), family.parse(" \t" + parts[1]));
        }
    }

    @Test
    public void parseCommand_invalidBusinessArguments_rejectsInsteadOfReturningPlaceholder() {
        AddressBookParser parser = new AddressBookParser();
        for (String input : new String[] {"member add", "member view", "member delete",
            "equipment add", "equipment view", "equipment delete", "member view nus/",
            "equipment delete uuid/not-a-uuid", "equipment add name/Camera category/Photography condition/broken",
            "loans add", "loans return", "loans return uuid/not-a-uuid returned/2026-01-01"}) {
            assertThrows(ParseException.class, () -> parser.parseCommand(input));
        }
    }
}
