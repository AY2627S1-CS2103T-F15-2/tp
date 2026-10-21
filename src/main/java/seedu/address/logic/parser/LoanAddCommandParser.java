package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_ASSIGNED_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EQUIPMENT_UUID;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EXPECTED_RETURN_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NUS_ID;

import java.util.Map;
import java.util.Set;

import seedu.address.logic.commands.LoanAddCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses loan assignment fields; current availability and date ordering are checked at execution. */
public class LoanAddCommandParser implements Parser<LoanAddCommand> {
    @Override
    public LoanAddCommand parse(String args) throws ParseException {
        try {
            ArgumentMultimap arguments = LaplaceArgumentParser.parse(args,
                    Set.of(PREFIX_EQUIPMENT_UUID, PREFIX_NUS_ID, PREFIX_ASSIGNED_DATE, PREFIX_EXPECTED_RETURN_DATE),
                    Set.of(), Map.of(PREFIX_EQUIPMENT_UUID, LoanUuidListParser::parse,
                            PREFIX_NUS_ID, ParserUtil::parseNusId,
                            PREFIX_ASSIGNED_DATE, ParserUtil::parseDate,
                            PREFIX_EXPECTED_RETURN_DATE, ParserUtil::parseDate));
            return LoanAddCommand.batch(
                    LoanUuidListParser.parse(arguments.getValue(PREFIX_EQUIPMENT_UUID).orElseThrow()),
                    ParserUtil.parseNusId(arguments.getValue(PREFIX_NUS_ID).orElseThrow()),
                    ParserUtil.parseDate(arguments.getValue(PREFIX_ASSIGNED_DATE).orElseThrow()),
                    ParserUtil.parseDate(arguments.getValue(PREFIX_EXPECTED_RETURN_DATE).orElseThrow()));
        } catch (ParseException e) {
            throw new ParseException(e.getMessage() + "\n" + LoanAddCommand.MESSAGE_USAGE, e);
        }
    }
}
