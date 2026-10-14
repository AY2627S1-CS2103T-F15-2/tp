package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MEMBER_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MEMBER_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MEMBER_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NUS_ID;

import java.util.Set;

import seedu.address.logic.commands.MemberAddCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;

/**
 * Parses the four required member-add fields using the shared Laplace grammar.
 */
public class MemberAddCommandParser implements Parser<MemberAddCommand> {

    private static final Set<Prefix> REQUIRED_PREFIXES = Set.of(PREFIX_NUS_ID, PREFIX_MEMBER_NAME,
            PREFIX_MEMBER_PHONE, PREFIX_MEMBER_EMAIL);

    @Override
    public MemberAddCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap arguments;
        try {
            arguments = LaplaceArgumentParser.parse(args, REQUIRED_PREFIXES, Set.of());
        } catch (ParseException e) {
            throw new ParseException(e.getMessage() + "\n" + MemberAddCommand.MESSAGE_USAGE, e);
        }

        NusId nusId = ParserUtil.parseNusId(arguments.getValue(PREFIX_NUS_ID).orElseThrow());
        Name name = ParserUtil.parseName(arguments.getValue(PREFIX_MEMBER_NAME).orElseThrow());
        Phone phone = ParserUtil.parsePhone(arguments.getValue(PREFIX_MEMBER_PHONE).orElseThrow());
        Email email = ParserUtil.parseEmail(arguments.getValue(PREFIX_MEMBER_EMAIL).orElseThrow());
        return new MemberAddCommand(new Member(nusId, name, phone, email));
    }
}
