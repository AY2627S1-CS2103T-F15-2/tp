package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.Model;
import seedu.address.model.query.DirectoryQuery;

/** Read-only search whose immutable criteria travel with the typed directory request. */
public class DirectorySearchCommand extends Command {
    public static final String MEMBER_USAGE = "member search [q/TEXT] [holding/any|yes|no]"
            + " [sort/name|id|held] [order/asc|desc]";
    public static final String EQUIPMENT_USAGE = "equipment search [q/TEXT] [category/TEXT]"
            + " [condition/CONDITION] [availability/AVAILABLE|ASSIGNED|UNAVAILABLE]"
            + " [sort/category|name|id|condition|availability] [order/asc|desc]";

    private final boolean members;
    private final DirectoryQuery query;

    /** Creates a directory query command without retaining the mutable model. */
    public DirectorySearchCommand(boolean members, DirectoryQuery query) {
        this.members = members;
        this.query = requireNonNull(query);
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        int count = members ? query.members(model.getAddressBook()).size()
                : query.equipment(model.getAddressBook(), model::getAvailability).size();
        return new CommandResult("Found " + count + (members ? " members." : " equipment items."),
                members ? DisplayRequest.members(query) : DisplayRequest.equipment(query));
    }
}
