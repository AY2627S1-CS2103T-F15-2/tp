package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_MEMBER_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MEMBER_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MEMBER_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NUS_ID;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import seedu.address.logic.commands.MemberEditCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.member.NusId;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;

/**
 * Parses an exact member identity and optional details using member-add validation rules.
 */
public class MemberEditCommandParser implements Parser<MemberEditCommand> {
    @Override
    public MemberEditCommand parse(String args) throws ParseException {
        try {
            ArgumentMultimap arguments = LaplaceArgumentParser.parse(args, Set.of(PREFIX_NUS_ID),
                    Set.of(PREFIX_MEMBER_NAME, PREFIX_MEMBER_PHONE, PREFIX_MEMBER_EMAIL), Map.of(
                            PREFIX_NUS_ID, ParserUtil::parseNusId,
                            PREFIX_MEMBER_NAME, ParserUtil::parseMemberName,
                            PREFIX_MEMBER_PHONE, ParserUtil::parseMemberPhone,
                            PREFIX_MEMBER_EMAIL, ParserUtil::parseMemberEmail));
            NusId nusId = ParserUtil.parseNusId(arguments.getValue(PREFIX_NUS_ID).orElseThrow());
            Optional<Name> name = arguments.getValue(PREFIX_MEMBER_NAME).map(Name::forMember);
            Optional<Phone> phone = arguments.getValue(PREFIX_MEMBER_PHONE).map(Phone::forMember);
            Optional<Email> email = arguments.getValue(PREFIX_MEMBER_EMAIL).map(Email::forMember);
            if (name.isEmpty() && phone.isEmpty() && email.isEmpty()) {
                throw new ParseException(MemberEditCommand.MESSAGE_NO_FIELDS);
            }
            return new MemberEditCommand(nusId, name, phone, email);
        } catch (ParseException exception) {
            throw new ParseException(exception.getMessage() + "\n" + MemberEditCommand.MESSAGE_USAGE, exception);
        }
    }
}
