package seedu.address.logic.completion;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ASSIGNED_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CATEGORY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CONDITION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EQUIPMENT_UUID;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EXPECTED_RETURN_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MEMBER_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MEMBER_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MEMBER_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTES;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NUS_ID;
import static seedu.address.logic.parser.CliSyntax.PREFIX_RETURNED_DATE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EquipmentAddCommand;
import seedu.address.logic.commands.EquipmentDeleteCommand;
import seedu.address.logic.commands.EquipmentListCommand;
import seedu.address.logic.commands.EquipmentViewCommand;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.LoanAddCommand;
import seedu.address.logic.commands.LoanListCommand;
import seedu.address.logic.commands.LoanReturnCommand;
import seedu.address.logic.commands.MemberAddCommand;
import seedu.address.logic.commands.MemberDeleteCommand;
import seedu.address.logic.commands.MemberEditCommand;
import seedu.address.logic.commands.MemberListCommand;
import seedu.address.logic.commands.MemberViewCommand;
import seedu.address.logic.parser.Prefix;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.equipment.Condition;

/** Computes suggestions from parser vocabulary and a live read-only root, without executing commands. */
public final class CompletionService {
    private static final Pattern WORD = Pattern.compile("\\S+");
    private static final Pattern MARKER = Pattern.compile("(?<!\\S)[A-Za-z][A-Za-z0-9_-]*/");
    private static final Map<String, List<Prefix>> COMMANDS = Map.ofEntries(
            Map.entry(MemberAddCommand.COMMAND_WORD,
                    List.of(PREFIX_NUS_ID, PREFIX_MEMBER_NAME, PREFIX_MEMBER_PHONE, PREFIX_MEMBER_EMAIL)),
            Map.entry(MemberEditCommand.COMMAND_WORD,
                    List.of(PREFIX_NUS_ID, PREFIX_MEMBER_NAME, PREFIX_MEMBER_PHONE, PREFIX_MEMBER_EMAIL)),
            Map.entry(MemberViewCommand.COMMAND_WORD, List.of(PREFIX_NUS_ID)),
            Map.entry(MemberDeleteCommand.COMMAND_WORD, List.of(PREFIX_NUS_ID)),
            Map.entry(MemberListCommand.COMMAND_WORD, List.of()),
            Map.entry(EquipmentAddCommand.COMMAND_WORD,
                    List.of(PREFIX_MEMBER_NAME, PREFIX_CATEGORY, PREFIX_CONDITION, PREFIX_NOTES)),
            Map.entry(EquipmentViewCommand.COMMAND_WORD, List.of(PREFIX_EQUIPMENT_UUID)),
            Map.entry(EquipmentDeleteCommand.COMMAND_WORD, List.of(PREFIX_EQUIPMENT_UUID)),
            Map.entry(EquipmentListCommand.COMMAND_WORD, List.of()),
            Map.entry(LoanAddCommand.COMMAND_WORD,
                    List.of(PREFIX_EQUIPMENT_UUID, PREFIX_NUS_ID, PREFIX_ASSIGNED_DATE, PREFIX_EXPECTED_RETURN_DATE)),
            Map.entry(LoanReturnCommand.COMMAND_WORD, List.of(PREFIX_EQUIPMENT_UUID, PREFIX_RETURNED_DATE)),
            Map.entry(LoanListCommand.COMMAND_WORD, List.of()),
            Map.entry(AddCommand.COMMAND_WORD, List.of()), Map.entry(EditCommand.COMMAND_WORD, List.of()),
            Map.entry(DeleteCommand.COMMAND_WORD, List.of()), Map.entry(ClearCommand.COMMAND_WORD, List.of()),
            Map.entry(FindCommand.COMMAND_WORD, List.of()), Map.entry(ListCommand.COMMAND_WORD, List.of()),
            Map.entry(ExitCommand.COMMAND_WORD, List.of()), Map.entry(HelpCommand.COMMAND_WORD, List.of()));

    /**
     * Completes the token or field value at the caret, preserving text outside the returned range.
     * Invalid caret offsets are programmer errors; malformed or unsupported input has no suggestions.
     */
    public Optional<Completion> complete(String text, int caret, ReadOnlyAddressBook data) {
        requireNonNull(text);
        requireNonNull(data);
        if (caret < 0 || caret > text.length()) {
            throw new IllegalArgumentException("Caret must be within the input.");
        }
        Matcher words = WORD.matcher(text);
        if (!words.find() || caret <= words.start()) {
            return Optional.empty();
        }
        String root = words.group();
        if (caret <= words.end()) {
            return suggest(words.start(), words.end(), text.substring(words.start(), caret),
                    COMMANDS.keySet().stream().map(command -> command.split(" ")[0]), false);
        }
        List<String> children = COMMANDS.keySet().stream().filter(command -> command.startsWith(root + " "))
                .map(command -> command.substring(root.length() + 1)).toList();
        if (children.isEmpty()) {
            return Optional.empty();
        }
        if (!words.find()) {
            return suggest(caret, caret, "", children.stream(), false);
        }
        if (caret <= words.end()) {
            if (caret < words.start()) {
                return suggest(caret, caret, "", children.stream(), false);
            }
            return suggest(words.start(), words.end(), text.substring(words.start(), caret),
                    children.stream(), false);
        }
        String command = root + " " + words.group();
        List<Prefix> prefixes = COMMANDS.getOrDefault(command, List.of());
        return fields(text, caret, words.end(), command, prefixes, data);
    }

    /** Identifies reserved field boundaries using the same marker grammar as LaplaceArgumentParser. */
    private Optional<Completion> fields(String text, int caret, int argumentsStart, String command,
            List<Prefix> prefixes, ReadOnlyAddressBook data) {
        Set<String> allowed = new HashSet<>();
        prefixes.forEach(prefix -> allowed.add(prefix.getPrefix()));
        List<Integer> starts = new ArrayList<>();
        List<Integer> ends = new ArrayList<>();
        List<String> markers = new ArrayList<>();
        Matcher matcher = MARKER.matcher(text).region(argumentsStart, text.length());
        Set<String> seen = new HashSet<>();
        while (matcher.find()) {
            String marker = matcher.group();
            if (!allowed.contains(marker) || !seen.add(marker)) {
                return Optional.empty();
            }
            starts.add(matcher.start());
            ends.add(matcher.end());
            markers.add(marker);
        }
        // An empty fragment at a following marker must not replace that untouched field.
        if (starts.contains(caret)) {
            return Optional.empty();
        }
        int tokenStart = caret;
        while (!Character.isWhitespace(text.charAt(tokenStart - 1))) {
            tokenStart--;
        }
        int tokenEnd = caret;
        while (tokenEnd < text.length() && !Character.isWhitespace(text.charAt(tokenEnd))) {
            tokenEnd++;
        }
        int markerIndex = starts.indexOf(tokenStart);
        if (markerIndex >= 0) {
            tokenEnd = ends.get(markerIndex);
        }
        String fragment = text.substring(tokenStart, caret);
        int replacementStart = tokenStart;
        Stream<String> available = allowed.stream().filter(prefix -> !prefix.equals(fragment))
                .filter(prefix -> !seen.contains(prefix) || text.startsWith(prefix, replacementStart));
        Optional<Completion> prefix = suggest(tokenStart, tokenEnd, fragment, available, false);
        if (prefix.isPresent()) {
            return prefix;
        }
        for (int i = 0; i < markers.size(); i++) {
            int end = i + 1 < starts.size() ? starts.get(i + 1) : text.length();
            if (caret >= ends.get(i) && caret <= end) {
                int start = ends.get(i);
                while (start < caret && Character.isWhitespace(text.charAt(start))) {
                    start++;
                }
                while (end > caret && Character.isWhitespace(text.charAt(end - 1))) {
                    end--;
                }
                return suggest(start, end, text.substring(start, caret),
                        values(command, markers.get(i), data), true);
            }
        }
        return Optional.empty();
    }

    /** New identities, dates, and free-form notes deliberately have no value suggestions. */
    private Stream<String> values(String command, String prefix, ReadOnlyAddressBook data) {
        switch (prefix) {
            case "nus/":
                if (command.equals(MemberAddCommand.COMMAND_WORD)) {
                    return Stream.empty();
                }
                return data.getMemberList().stream().map(member -> member.getNusId().value);
            case "uuid/":
                return data.getEquipmentList().stream().map(equipment -> equipment.getUuid().toString());
            case "name/":
                if (command.startsWith("member ")) {
                    return data.getMemberList().stream().map(member -> member.getName().fullName);
                }
                return data.getEquipmentList().stream().map(equipment -> equipment.getName());
            case "phone/":
                return data.getMemberList().stream().map(member -> member.getPhone().value);
            case "email/":
                return data.getMemberList().stream().map(member -> member.getEmail().value);
            case "category/":
                return data.getEquipmentList().stream().map(equipment -> equipment.getCategory());
            case "condition/":
                return Arrays.stream(Condition.values()).map(Condition::name);
            default:
                return Stream.empty();
        }
    }

    /** Filters and freezes an ordered candidate list, including an exact match as an idempotent completion. */
    private Optional<Completion> suggest(int start, int end, String fragment, Stream<String> candidates,
            boolean ignoreCase) {
        String comparison = ignoreCase ? fragment.toLowerCase(Locale.ROOT) : fragment;
        List<String> matches = candidates.filter(candidate -> {
            String value = ignoreCase ? candidate.toLowerCase(Locale.ROOT) : candidate;
            return value.startsWith(comparison);
        }).distinct().sorted().toList();
        if (matches.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new Completion(start, end, matches));
    }
}
