package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_NUS_ID;

import java.util.Set;

import seedu.address.logic.commands.MemberViewCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses exactly one required identity using the shared Laplace argument grammar. */
public class MemberViewCommandParser implements Parser<MemberViewCommand> {
    @Override
    public MemberViewCommand parse(String args) throws ParseException {
        try {
            ArgumentMultimap arguments = LaplaceArgumentParser.parse(args, Set.of(PREFIX_NUS_ID), Set.of());
            return new MemberViewCommand(ParserUtil.parseNusId(
                    arguments.getValue(PREFIX_NUS_ID).orElseThrow()));
        } catch (ParseException e) {
            throw new ParseException(e.getMessage() + "\n" + MemberViewCommand.MESSAGE_USAGE, e);
        }
    }
}
