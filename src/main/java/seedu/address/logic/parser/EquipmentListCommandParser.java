package seedu.address.logic.parser;

import java.util.Set;

import seedu.address.logic.commands.EquipmentListCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses the no-argument {@code equipment list} command. */
public class EquipmentListCommandParser implements Parser<EquipmentListCommand> {

    @Override
    public EquipmentListCommand parse(String args) throws ParseException {
        try {
            LaplaceArgumentParser.parse(args, Set.of(), Set.of());
        } catch (ParseException e) {
            throw new ParseException(e.getMessage() + "\n" + EquipmentListCommand.MESSAGE_USAGE, e);
        }
        return new EquipmentListCommand();
    }
}
