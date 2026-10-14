package seedu.address.logic.parser;

import java.util.Set;

import seedu.address.logic.commands.MemberListCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses the no-argument {@code member list} command. */
public class MemberListCommandParser implements Parser<MemberListCommand> {

    @Override
    public MemberListCommand parse(String args) throws ParseException {
        try {
            LaplaceArgumentParser.parse(args, Set.of(), Set.of());
        } catch (ParseException e) {
            throw new ParseException(e.getMessage() + "\n" + MemberListCommand.MESSAGE_USAGE, e);
        }
        return new MemberListCommand();
    }
}
