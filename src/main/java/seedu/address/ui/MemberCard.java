package seedu.address.ui;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import seedu.address.model.member.Member;

/**
 * Renders a snapshot of a member and a caller-supplied held-item count.
 */
public class MemberCard extends UiPart<Region> {

    private static final String FXML = "MemberCard.fxml";

    @FXML
    private Label name;
    @FXML
    private Label identityAndPhone;
    @FXML
    private Label email;
    @FXML
    private Label itemsHeld;

    /**
     * Creates a member row. Selection and activation belong to the parent list.
     *
     * @throws NullPointerException if the member is null.
     * @throws IllegalArgumentException if the count is negative.
     */
    public MemberCard(Member member, int itemsHeld) {
        super(validateInput(member, itemsHeld));
        name.setText(member.getName().fullName);
        identityAndPhone.setText(member.getNusId() + " · " + member.getPhone().value);
        email.setText(member.getEmail().value);
        this.itemsHeld.setText(itemsHeld + (itemsHeld == 1 ? " item held" : " items held"));
    }

    /**
     * Validates programmer arguments before loading JavaFX controls.
     */
    private static String validateInput(Member member, int itemsHeld) {
        requireNonNull(member);
        checkArgument(itemsHeld >= 0, "Items held must not be negative");
        return FXML;
    }
}
