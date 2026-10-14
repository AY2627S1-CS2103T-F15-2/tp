package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static seedu.address.testutil.Assert.assertThrows;

import java.lang.reflect.Proxy;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.EquipmentListCommand;
import seedu.address.logic.commands.MemberListCommand;
import seedu.address.logic.commands.NotImplementedCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;

public class LaplaceCommandRoutingTest {

    private static final String[] UNFINISHED_ROUTES = {"member add", "member view", "member delete",
        "equipment add", "equipment view", "equipment delete", "loans add", "loans return"};

    // Any model access, including an unfinished scaffold operation, fails the test.
    private final Model model = (Model) Proxy.newProxyInstance(Model.class.getClassLoader(),
            new Class<?>[] {Model.class}, (proxy, method, args) -> {
                throw new AssertionError("Placeholder accessed Model: " + method.getName());
            });

    @Test
    public void parseCommand_unfinishedRoutes_reportUnfinishedWithoutModelAccess() throws Exception {
        AddressBookParser parser = new AddressBookParser();
        for (String route : UNFINISHED_ROUTES) {
            assertPlaceholder(parser.parseCommand(route), route);
            assertPlaceholder(parser.parseCommand(" \t" + route.replace(" ", "\t  ") + " \t"), route);
            assertPlaceholder(parser.parseCommand(route + " nus/A0123456X unknown/value"), route);
        }
    }

    @Test
    public void parseFamilies_unfinishedRoutes_recognizeSubcommandsIndependently() throws Exception {
        for (String route : UNFINISHED_ROUTES) {
            String[] parts = route.split(" ");
            Parser<Command> parser = switch (parts[0]) {
                case "member" -> new MemberCommandParser();
                case "equipment" -> new EquipmentCommandParser();
                default -> new LoansCommandParser();
            };
            assertPlaceholder(parser.parse(parts[1]), route);
            assertPlaceholder(parser.parse(" \n\t" + parts[1] + "\t ignored arguments"), route);
        }
    }

    @Test
    public void parseCommand_missingUnknownOrWrongCase_throwsParseException() {
        AddressBookParser parser = new AddressBookParser();
        for (String input : new String[] {"", " \t", "member", "equipment \t", "loans", "members list",
            "equipmentx list", "loan add", "member unknown", "equipment return", "loans list",
            "Member list", "member LIST", "equipment ADD", "loans Return", "member addExtra"}) {
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
    public void parseCommand_listRoutes_returnsRealCommandsAndRejectsArguments() throws Exception {
        AddressBookParser parser = new AddressBookParser();
        assertInstanceOf(MemberListCommand.class, parser.parseCommand(" \tmember\t list \t"));
        assertInstanceOf(EquipmentListCommand.class, parser.parseCommand("equipment list"));
        assertInstanceOf(MemberListCommand.class, new MemberCommandParser().parse("list \t"));
        assertInstanceOf(EquipmentListCommand.class, new EquipmentCommandParser().parse("\tlist"));
        for (String route : new String[] {"member list", "equipment list"}) {
            for (String args : new String[] {"extra", "nus/A0123456X", "notes/ notes/", "unknown/value"}) {
                assertThrows(ParseException.class, () -> parser.parseCommand(route + "\t" + args));
            }
        }
    }

    private void assertPlaceholder(Command command, String route) throws Exception {
        assertInstanceOf(NotImplementedCommand.class, command);
        CommandResult result = command.execute(model);
        assertEquals("The " + route + " command is not implemented yet.", result.getFeedbackToUser());
        assertFalse(result.isShowHelp());
        assertFalse(result.isExit());
    }
}
