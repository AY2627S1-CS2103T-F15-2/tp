package seedu.address.logic.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import seedu.address.logic.parser.exceptions.ParseException;

/** Parses a comma-separated set of equipment identities for loan commands. */
final class LoanUuidListParser {
    private LoanUuidListParser() {
    }

    /** Returns UUIDs in the user's order, rejecting empty and repeated entries. */
    static List<UUID> parse(String value) throws ParseException {
        String[] entries = value.split(",", -1);
        List<UUID> identifiers = new ArrayList<>();
        Set<UUID> seen = new HashSet<>();
        for (String entry : entries) {
            UUID uuid = ParserUtil.parseUuid(entry.strip());
            if (!seen.add(uuid)) {
                throw new ParseException("Repeated equipment UUID: " + uuid);
            }
            identifiers.add(uuid);
        }
        return List.copyOf(identifiers);
    }
}
