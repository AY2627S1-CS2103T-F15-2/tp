package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_CONDITION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EQUIPMENT_UUID;
import static seedu.address.logic.parser.CliSyntax.PREFIX_RETURNED_DATE;

import java.util.Map;
import java.util.Set;

import seedu.address.logic.commands.LoanReturnCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.equipment.Condition;

/** Parses the equipment identity and actual return date using the shared strict argument grammar. */
public class LoanReturnCommandParser implements Parser<LoanReturnCommand> {
    @Override
    public LoanReturnCommand parse(String args) throws ParseException {
        try {
            ArgumentMultimap arguments = LaplaceArgumentParser.parse(args,
                    Set.of(PREFIX_EQUIPMENT_UUID, PREFIX_RETURNED_DATE), Set.of(PREFIX_CONDITION),
                    Map.of(PREFIX_EQUIPMENT_UUID, LoanUuidListParser::parse,
                            PREFIX_RETURNED_DATE, ParserUtil::parseDate,
                            PREFIX_CONDITION, ParserUtil::parseCondition));
            Condition condition = arguments.getValue(PREFIX_CONDITION).isPresent()
                    ? ParserUtil.parseCondition(arguments.getValue(PREFIX_CONDITION).orElseThrow()) : null;
            return new LoanReturnCommand(
                    LoanUuidListParser.parse(arguments.getValue(PREFIX_EQUIPMENT_UUID).orElseThrow()),
                    ParserUtil.parseDate(arguments.getValue(PREFIX_RETURNED_DATE).orElseThrow()),
                    condition);
        } catch (ParseException e) {
            throw new ParseException(e.getMessage() + "\n" + LoanReturnCommand.MESSAGE_USAGE, e);
        }
    }
}
