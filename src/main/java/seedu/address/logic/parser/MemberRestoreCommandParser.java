package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_NUS_ID;

import java.util.Map;
import java.util.Set;

import seedu.address.logic.commands.MemberRestoreCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses exactly one member identity using the shared strict argument grammar.
 */
public class MemberRestoreCommandParser implements Parser<MemberRestoreCommand> {
    @Override
    public MemberRestoreCommand parse(String args) throws ParseException {
        try {
            ArgumentMultimap arguments = LaplaceArgumentParser.parse(args, Set.of(PREFIX_NUS_ID), Set.of(),
                    Map.of(PREFIX_NUS_ID, ParserUtil::parseNusId));
            return new MemberRestoreCommand(ParserUtil.parseNusId(arguments.getValue(PREFIX_NUS_ID).orElseThrow()));
        } catch (ParseException exception) {
            throw new ParseException(exception.getMessage() + "\n" + MemberRestoreCommand.MESSAGE_USAGE, exception);
        }
    }
}
