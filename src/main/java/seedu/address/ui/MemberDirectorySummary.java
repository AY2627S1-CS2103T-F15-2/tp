package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.member.Member;
import seedu.address.model.member.NusId;
import seedu.address.model.query.DirectoryQuery;

/** Read-only member directory summary. The shell owns listeners and calls refresh on the FX thread. */
public class MemberDirectorySummary extends UiPart<Region> {

    private final ReadOnlyAddressBook data;
    private DirectoryQuery query = DirectoryQuery.all();

    @FXML
    private Label sortOrder;
    @FXML
    private Label criteria;

    @FXML
    private Label countBadge;
    @FXML
    private Label activeCount;
    @FXML
    private Label deletedCount;
    @FXML
    private Label displayedCount;
    @FXML
    private Label selection;
    @FXML
    private Label statusTitle;
    @FXML
    private Label statusMessage;

    /** Creates a standalone summary from non-null read-only dependencies. */
    public MemberDirectorySummary(ReadOnlyAddressBook data) {
        super("MemberDirectorySummary.fxml");
        this.data = requireNonNull(data);
        countBadge.setMinWidth(Region.USE_PREF_SIZE);
        refresh(Optional.empty());
    }

    /** Sets transient criteria; the parent refreshes once after coordinating both directory components. */
    public void setQuery(DirectoryQuery query) {
        this.query = requireNonNull(query);
    }

    /** Refreshes current counts and selection without executing commands or retaining entity snapshots. */
    public void refresh(Optional<NusId> selected) {
        requireNonNull(selected);
        int count = data.getMemberList().size();
        long deleted = data.getMemberList().stream().filter(Member::isDeleted).count();
        long active = count - deleted;
        countBadge.setText(active + " ACTIVE");
        activeCount.setText(Long.toString(active));
        deletedCount.setText(Long.toString(deleted));
        int displayed = query.members(data).size();
        displayedCount.setText(Integer.toString(displayed));
        sortOrder.setText(query.sortDescription(true));
        criteria.setText(query.description());
        Optional<Member> member = selected.flatMap(nusId -> data.getMemberList().stream()
                .filter(candidate -> candidate.getNusId().equals(nusId)).findFirst());
        selection.setText(selected.isEmpty() ? "No member selected"
                : member.map(value -> value.getName() + (value.isDeleted() ? " (deleted)" : "")
                        + " - " + value.getNusId())
                        .orElse("Member no longer exists: " + selected.orElseThrow()));
        String title = "No members yet";
        if (count > 0) {
            title = deleted == 0 ? "All active members displayed" : "Active and deleted members displayed";
        }
        if (displayed != count) {
            title = "Showing " + displayed + " matching members";
        }
        statusTitle.setText(title);
        statusMessage.setText(count == 0
                ? "Add a member with member add nus/NUS_ID name/NAME phone/PHONE email/EMAIL."
                : "The member directory is ready for selection or another typed command.");
    }
}
