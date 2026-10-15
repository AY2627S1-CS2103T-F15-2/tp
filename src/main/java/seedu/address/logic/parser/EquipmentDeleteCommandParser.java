package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_EQUIPMENT_UUID;

import java.util.Map;
import java.util.Set;

import seedu.address.logic.commands.EquipmentDeleteCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses exactly one required UUID using the shared Laplace argument grammar.
 */
public class EquipmentDeleteCommandParser implements Parser<EquipmentDeleteCommand> {

    @Override
    public EquipmentDeleteCommand parse(String args) throws ParseException {
        ArgumentMultimap arguments;
        try {
            arguments = LaplaceArgumentParser.parse(args, Set.of(PREFIX_EQUIPMENT_UUID), Set.of(),
                    Map.of(PREFIX_EQUIPMENT_UUID, ParserUtil::parseUuid));
        } catch (ParseException e) {
            throw new ParseException(e.getMessage() + "\n" + EquipmentDeleteCommand.MESSAGE_USAGE, e);
        }
        return new EquipmentDeleteCommand(
                ParserUtil.parseUuid(arguments.getValue(PREFIX_EQUIPMENT_UUID).orElseThrow()));
    }
}
