package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_EQUIPMENT_UUID;

import java.util.Set;

import seedu.address.logic.commands.EquipmentViewCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses exactly one required identity using the shared Laplace argument grammar. */
public class EquipmentViewCommandParser implements Parser<EquipmentViewCommand> {
    @Override
    public EquipmentViewCommand parse(String args) throws ParseException {
        try {
            ArgumentMultimap arguments = LaplaceArgumentParser.parse(args, Set.of(PREFIX_EQUIPMENT_UUID), Set.of());
            return new EquipmentViewCommand(ParserUtil.parseUuid(
                    arguments.getValue(PREFIX_EQUIPMENT_UUID).orElseThrow()));
        } catch (ParseException e) {
            throw new ParseException(e.getMessage() + "\n" + EquipmentViewCommand.MESSAGE_USAGE, e);
        }
    }
}
