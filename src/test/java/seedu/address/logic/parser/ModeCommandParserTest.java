package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.commands.DisplayRequest;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;

class ModeCommandParserTest {
    @Test
    void parse_supportedModes_preserveDataGeometryAndNavigation() throws Exception {
        ModelManager model = new ModelManager();
        GuiSettings original = new GuiSettings(1100, 700, 20, 30);
        model.setGuiSettings(original);
        AddressBook before = new AddressBook(model.getAddressBook());
        for (String input : List.of("mode dark", "mode light", "MODE DARK", "Mode Light", " mode \t dark \n")) {
            var command = new AddressBookParser().parseCommand(input);
            assertTrue(command.isReadOnly());
            assertEquals(DisplayRequest.unchanged(), command.execute(model).getDisplayRequest());
            assertEquals(input.toLowerCase(Locale.ROOT).contains("dark"), model.getGuiSettings().isDarkMode());
            assertEquals(original, model.getGuiSettings().withDarkMode(false));
            assertEquals(before, model.getAddressBook());
        }
    }

    @Test
    void parse_invalidModes_rejectMissingExtraAndUnsupportedArguments() {
        for (String input : List.of("mode", "mode sepia", "mode dark light", "mode dark extra/yes")) {
            assertThrows(ParseException.class, () -> new AddressBookParser().parseCommand(input));
        }
    }
}
