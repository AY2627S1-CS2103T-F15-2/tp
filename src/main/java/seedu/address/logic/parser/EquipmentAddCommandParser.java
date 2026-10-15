package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_CATEGORY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CONDITION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EQUIPMENT_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTES;

import java.util.Map;
import java.util.Set;

import seedu.address.logic.commands.EquipmentAddCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses equipment descriptions without person-name restrictions. */
public class EquipmentAddCommandParser implements Parser<EquipmentAddCommand> {

    @Override
    public EquipmentAddCommand parse(String args) throws ParseException {
        ArgumentMultimap arguments;
        try {
            arguments = LaplaceArgumentParser.parse(args,
                    Set.of(PREFIX_EQUIPMENT_NAME, PREFIX_CATEGORY, PREFIX_CONDITION), Set.of(PREFIX_NOTES),
                    Map.of(PREFIX_EQUIPMENT_NAME, ParserUtil::parseEquipmentName,
                            PREFIX_CATEGORY, ParserUtil::parseCategory, PREFIX_CONDITION, ParserUtil::parseCondition,
                            PREFIX_NOTES, ParserUtil::parseNotes));
        } catch (ParseException e) {
            throw new ParseException(e.getMessage() + "\n" + EquipmentAddCommand.MESSAGE_USAGE, e);
        }
        return new EquipmentAddCommand(arguments.getValue(PREFIX_EQUIPMENT_NAME).orElseThrow(),
                arguments.getValue(PREFIX_CATEGORY).orElseThrow(),
                ParserUtil.parseCondition(arguments.getValue(PREFIX_CONDITION).orElseThrow()),
                arguments.getValue(PREFIX_NOTES).orElse(""));
    }
}
