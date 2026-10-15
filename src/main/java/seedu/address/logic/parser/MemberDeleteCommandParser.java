package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_NUS_ID;

import java.util.Map;
import java.util.Set;

import seedu.address.logic.commands.MemberDeleteCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses exactly one required NUS ID using the shared Laplace argument grammar.
 */
public class MemberDeleteCommandParser implements Parser<MemberDeleteCommand> {

    @Override
    public MemberDeleteCommand parse(String args) throws ParseException {
        ArgumentMultimap arguments;
        try {
            arguments = LaplaceArgumentParser.parse(args, Set.of(PREFIX_NUS_ID), Set.of(),
                    Map.of(PREFIX_NUS_ID, ParserUtil::parseNusId));
        } catch (ParseException e) {
            throw new ParseException(e.getMessage() + "\n" + MemberDeleteCommand.MESSAGE_USAGE, e);
        }
        return new MemberDeleteCommand(ParserUtil.parseNusId(arguments.getValue(PREFIX_NUS_ID).orElseThrow()));
    }
}
