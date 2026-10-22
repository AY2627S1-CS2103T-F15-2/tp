package seedu.address.logic.parser;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import seedu.address.logic.commands.DirectorySearchCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.equipment.Availability;
import seedu.address.model.equipment.Condition;
import seedu.address.model.query.DirectoryQuery;

/** Uses the shared strict grammar; search values are substrings, not exact identity parsers. */
public class DirectorySearchCommandParser implements Parser<DirectorySearchCommand> {
    public static final Prefix QUERY = new Prefix("q/");
    public static final Prefix CATEGORY = new Prefix("category/");
    public static final Prefix CONDITION = new Prefix("condition/");
    public static final Prefix AVAILABILITY = new Prefix("availability/");
    public static final Prefix HOLDING = new Prefix("holding/");
    public static final Prefix STATUS = new Prefix("status/");
    public static final Prefix SORT = new Prefix("sort/");
    public static final Prefix ORDER = new Prefix("order/");
    private final boolean members;

    public DirectorySearchCommandParser(boolean members) {
        this.members = members;
    }

    @Override
    public DirectorySearchCommand parse(String args) throws ParseException {
        try {
            Set<Prefix> prefixes = members
                    ? Set.of(QUERY, HOLDING, STATUS, SORT, ORDER)
                    : Set.of(QUERY, CATEGORY, CONDITION, AVAILABILITY, STATUS, SORT, ORDER);
            Map<Prefix, LaplaceArgumentParser.ValueValidator> validators = new HashMap<>();
            for (Prefix prefix : prefixes) {
                validators.put(prefix, value -> validate(prefix, value));
            }
            ArgumentMultimap values = LaplaceArgumentParser.parse(args, Set.of(), prefixes, validators);
            DirectoryQuery.Sort sort = enumValue(DirectoryQuery.Sort.class, value(values, SORT, "default"));
            Condition condition = values.getValue(CONDITION).isPresent()
                    ? ParserUtil.parseCondition(values.getValue(CONDITION).orElseThrow()) : null;
            Availability availability = values.getValue(AVAILABILITY).isPresent()
                    ? enumValue(Availability.class, values.getValue(AVAILABILITY).orElseThrow()) : null;
            DirectoryQuery.Holding holding = enumValue(DirectoryQuery.Holding.class, value(values, HOLDING, "any"));
            DirectoryQuery.Status status = enumValue(DirectoryQuery.Status.class, value(values, STATUS, "active"));
            return new DirectorySearchCommand(members, new DirectoryQuery(value(values, QUERY, ""),
                    value(values, CATEGORY, ""), condition, availability, holding, sort,
                    value(values, ORDER, "asc").equalsIgnoreCase("desc"), status));
        } catch (ParseException e) {
            throw new ParseException(e.getMessage() + "\n" + (members
                    ? DirectorySearchCommand.MEMBER_USAGE : DirectorySearchCommand.EQUIPMENT_USAGE), e);
        }
    }

    /** Runs at each field's input position, before scanning the following marker. */
    private void validate(Prefix prefix, String value) throws ParseException {
        if (value.isBlank()) {
            throw new ParseException("Search parameter must not be blank: " + prefix);
        }
        if (prefix.equals(ORDER) && !Set.of("asc", "desc").contains(value.toLowerCase(Locale.ROOT))) {
            throw new ParseException("order/ must be asc or desc.");
        }
        if (prefix.equals(CONDITION)) {
            ParserUtil.parseCondition(value);
        } else if (prefix.equals(AVAILABILITY)) {
            enumValue(Availability.class, value);
        } else if (prefix.equals(HOLDING)) {
            enumValue(DirectoryQuery.Holding.class, value);
        } else if (prefix.equals(STATUS)) {
            enumValue(DirectoryQuery.Status.class, value);
        } else if (prefix.equals(SORT)) {
            DirectoryQuery.Sort sort = enumValue(DirectoryQuery.Sort.class, value);
            Set<DirectoryQuery.Sort> allowed = members
                    ? Set.of(DirectoryQuery.Sort.DEFAULT, DirectoryQuery.Sort.NAME,
                            DirectoryQuery.Sort.ID, DirectoryQuery.Sort.HELD)
                    : Set.of(DirectoryQuery.Sort.DEFAULT, DirectoryQuery.Sort.NAME, DirectoryQuery.Sort.ID,
                            DirectoryQuery.Sort.CATEGORY, DirectoryQuery.Sort.CONDITION,
                            DirectoryQuery.Sort.AVAILABILITY);
            if (!allowed.contains(sort)) {
                throw new ParseException("Unsupported sort field for this directory.");
            }
        }
    }

    private static String value(ArgumentMultimap values, Prefix prefix, String fallback) {
        return values.getValue(prefix).orElse(fallback);
    }

    private static <T extends Enum<T>> T enumValue(Class<T> type, String value) throws ParseException {
        try {
            return Enum.valueOf(type, value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ParseException("Invalid " + type.getSimpleName() + ": " + value, e);
        }
    }
}
