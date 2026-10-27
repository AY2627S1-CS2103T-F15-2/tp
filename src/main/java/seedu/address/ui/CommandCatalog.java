package seedu.address.ui;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Command examples shared by Help and explicit command completion.
 */
final class CommandCatalog {
    static final List<String> EXAMPLES = List.of(
            "member add nus/NUS_ID name/NAME phone/PHONE email/EMAIL",
            "member edit nus/NUS_ID name/NAME phone/PHONE email/EMAIL",
            "member list", "member view nus/NUS_ID", "member delete nus/NUS_ID", "member restore nus/NUS_ID",
            "member search q/TEXT holding/any status/active sort/name order/asc",
            "equipment add name/NAME category/CATEGORY condition/GOOD notes/NOTES count/1",
            "equipment edit uuid/UUID name/NAME category/CATEGORY condition/GOOD notes/NOTES",
            "equipment list", "equipment view uuid/UUID", "equipment delete uuid/UUID", "equipment restore uuid/UUID",
            "equipment search q/TEXT category/CATEGORY condition/GOOD availability/AVAILABLE"
                    + " status/active sort/name order/asc",
            "loans list", "loans list nus/TEXT uuid/TEXT", "loans reset",
            "loans add uuid/UUID nus/NUS_ID assigned/YYYY-MM-DD expected/YYYY-MM-DD",
            "loans return uuid/UUID returned/YYYY-MM-DD condition/GOOD", "mode dark", "mode light", "help", "exit");
    private static final Pattern PREFIX = Pattern.compile("\\b[a-z]+/");

    private CommandCatalog() {
    }

    /**
     * Returns replacements for the full input, completing command names or an unfinished prefix.
     */
    static List<String> complete(String input) {
        List<String> commands = EXAMPLES.stream().map(CommandCatalog::commandName).distinct().toList();
        List<String> names = commands.stream().filter(name -> name.startsWith(input)).map(name -> name + " ").toList();
        if (!names.isEmpty()) {
            return names;
        }
        int boundary = input.lastIndexOf(' ') + 1;
        String partial = input.substring(boundary);
        if (partial.contains("/")) {
            return List.of();
        }
        return EXAMPLES.stream().filter(example -> input.startsWith(commandName(example) + " "))
                .flatMap(example -> PREFIX.matcher(example).results().map(match -> match.group()))
                .filter(prefix -> prefix.startsWith(partial))
                .filter(prefix -> !Pattern.compile("(?:^|\\s)" + Pattern.quote(prefix))
                        .matcher(input.substring(0, boundary)).find())
                .distinct().map(prefix -> input.substring(0, boundary) + prefix).toList();
    }

    private static String commandName(String example) {
        String[] words = example.split(" ", 3);
        return words.length == 1 ? words[0] : words[0] + " " + words[1];
    }
}
