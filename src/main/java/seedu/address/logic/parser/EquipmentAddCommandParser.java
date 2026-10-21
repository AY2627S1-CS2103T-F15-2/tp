package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_CATEGORY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CONDITION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_COUNT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EQUIPMENT_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTES;

import java.util.Map;
import java.util.Set;

import seedu.address.logic.commands.EquipmentAddCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses equipment descriptions without person-name restrictions.
 */
public class EquipmentAddCommandParser implements Parser<EquipmentAddCommand> {

    @Override
    public EquipmentAddCommand parse(String args) throws ParseException {
        ArgumentMultimap arguments;
        try {
            arguments = LaplaceArgumentParser.parse(args,
                    Set.of(PREFIX_EQUIPMENT_NAME, PREFIX_CATEGORY, PREFIX_CONDITION),
                    Set.of(PREFIX_NOTES, PREFIX_COUNT),
                    Map.of(PREFIX_EQUIPMENT_NAME, ParserUtil::parseEquipmentName,
                            PREFIX_CATEGORY, ParserUtil::parseCategory, PREFIX_CONDITION, ParserUtil::parseCondition,
                            PREFIX_NOTES, ParserUtil::parseNotes, PREFIX_COUNT, EquipmentAddCommandParser::parseCount));
        } catch (ParseException e) {
            throw new ParseException(e.getMessage() + "\n" + EquipmentAddCommand.MESSAGE_USAGE, e);
        }
        return new EquipmentAddCommand(arguments.getValue(PREFIX_EQUIPMENT_NAME).orElseThrow(),
                arguments.getValue(PREFIX_CATEGORY).orElseThrow(),
                ParserUtil.parseCondition(arguments.getValue(PREFIX_CONDITION).orElseThrow()),
                arguments.getValue(PREFIX_NOTES).orElse(""),
                parseCount(arguments.getValue(PREFIX_COUNT).orElse("1")));
    }

    /**
     * Parses a digit-only batch count without allowing signs, fractions, or numeric overflow.
     */
    private static int parseCount(String value) throws ParseException {
        if (!value.matches("[0-9]+")) {
            throw new ParseException(EquipmentAddCommand.MESSAGE_INVALID_COUNT);
        }
        try {
            int count = Integer.parseInt(value);
            if (count < 1 || count > EquipmentAddCommand.MAX_COUNT) {
                throw new ParseException(EquipmentAddCommand.MESSAGE_INVALID_COUNT);
            }
            return count;
        } catch (NumberFormatException e) {
            throw new ParseException(EquipmentAddCommand.MESSAGE_INVALID_COUNT, e);
        }
    }
}
