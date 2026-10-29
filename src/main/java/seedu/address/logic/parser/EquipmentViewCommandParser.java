package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_CATEGORY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EQUIPMENT_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EQUIPMENT_UUID;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SHORT_UUID;

import java.util.Map;
import java.util.Set;

import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.EquipmentBrowseCommand;
import seedu.address.logic.commands.EquipmentViewCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses mutually exclusive full UUID, compact UUID and category/name navigation forms.
 */
public class EquipmentViewCommandParser implements Parser<Command> {
    @Override
    public Command parse(String args) throws ParseException {
        try {
            ArgumentMultimap arguments = LaplaceArgumentParser.parse(args, Set.of(),
                    Set.of(PREFIX_EQUIPMENT_UUID, PREFIX_SHORT_UUID, PREFIX_CATEGORY, PREFIX_EQUIPMENT_NAME),
                    Map.of(PREFIX_EQUIPMENT_UUID, ParserUtil::parseUuid,
                            PREFIX_SHORT_UUID, EquipmentViewCommandParser::validateShortUuid,
                            PREFIX_CATEGORY, ParserUtil::parseCategory,
                            PREFIX_EQUIPMENT_NAME, ParserUtil::parseEquipmentName));
            boolean hasUuid = arguments.getValue(PREFIX_EQUIPMENT_UUID).isPresent();
            boolean hasShortUuid = arguments.getValue(PREFIX_SHORT_UUID).isPresent();
            boolean hasCategory = arguments.getValue(PREFIX_CATEGORY).isPresent();
            boolean hasName = arguments.getValue(PREFIX_EQUIPMENT_NAME).isPresent();
            if (hasUuid && !hasShortUuid && !hasCategory && !hasName) {
                return new EquipmentViewCommand(ParserUtil.parseUuid(
                        arguments.getValue(PREFIX_EQUIPMENT_UUID).orElseThrow()));
            }
            if (hasShortUuid && !hasUuid && !hasCategory && !hasName) {
                return EquipmentBrowseCommand.forShortUuid(arguments.getValue(PREFIX_SHORT_UUID).orElseThrow());
            }
            if (hasCategory && hasName && !hasUuid && !hasShortUuid) {
                return EquipmentBrowseCommand.forGroup(
                        ParserUtil.parseCategory(arguments.getValue(PREFIX_CATEGORY).orElseThrow()),
                        ParserUtil.parseEquipmentName(arguments.getValue(PREFIX_EQUIPMENT_NAME).orElseThrow()));
            }
            throw new ParseException("Supply uuid/UUID, short/PREFIX[-SUFFIX],"
                    + " or both category/CATEGORY and name/NAME.");
        } catch (ParseException e) {
            throw new ParseException(e.getMessage() + "\n" + EquipmentViewCommand.MESSAGE_USAGE, e);
        }
    }

    /**
     * Rejects malformed compact references before command execution.
     */
    private static void validateShortUuid(String value) throws ParseException {
        try {
            EquipmentBrowseCommand.forShortUuid(value);
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage(), e);
        }
    }
}
