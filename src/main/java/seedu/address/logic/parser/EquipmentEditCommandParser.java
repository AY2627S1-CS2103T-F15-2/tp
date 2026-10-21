package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_CATEGORY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CONDITION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EQUIPMENT_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EQUIPMENT_UUID;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTES;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import seedu.address.logic.commands.EquipmentEditCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.equipment.Condition;

/**
 * Parses optional equipment details with the same strict grammar and validation as equipment add.
 */
public class EquipmentEditCommandParser implements Parser<EquipmentEditCommand> {
    @Override
    public EquipmentEditCommand parse(String args) throws ParseException {
        try {
            ArgumentMultimap values = LaplaceArgumentParser.parse(args, Set.of(PREFIX_EQUIPMENT_UUID),
                    Set.of(PREFIX_EQUIPMENT_NAME, PREFIX_CATEGORY, PREFIX_CONDITION, PREFIX_NOTES),
                    Map.of(PREFIX_EQUIPMENT_UUID, ParserUtil::parseUuid,
                            PREFIX_EQUIPMENT_NAME, ParserUtil::parseEquipmentName,
                            PREFIX_CATEGORY, ParserUtil::parseCategory, PREFIX_CONDITION, ParserUtil::parseCondition,
                            PREFIX_NOTES, ParserUtil::parseNotes));
            Optional<String> name = values.getValue(PREFIX_EQUIPMENT_NAME);
            Optional<String> category = values.getValue(PREFIX_CATEGORY);
            Optional<String> notes = values.getValue(PREFIX_NOTES);
            Optional<Condition> condition = values.getValue(PREFIX_CONDITION).isPresent()
                    ? Optional.of(ParserUtil.parseCondition(values.getValue(PREFIX_CONDITION).orElseThrow()))
                    : Optional.empty();
            if (name.isEmpty() && category.isEmpty() && condition.isEmpty() && notes.isEmpty()) {
                throw new ParseException(EquipmentEditCommand.MESSAGE_NO_FIELDS);
            }
            return new EquipmentEditCommand(ParserUtil.parseUuid(values.getValue(PREFIX_EQUIPMENT_UUID).orElseThrow()),
                    name, category, condition, notes);
        } catch (ParseException exception) {
            throw new ParseException(exception.getMessage() + "\n" + EquipmentEditCommand.MESSAGE_USAGE, exception);
        }
    }
}
