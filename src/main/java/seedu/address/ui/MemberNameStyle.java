package seedu.address.ui;

import javafx.beans.binding.Bindings;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import seedu.address.model.member.Member;

/**
 * Presents deleted names consistently without modifying stored member names or disabling selection.
 */
final class MemberNameStyle {
    private MemberNameStyle() {
    }

    /**
     * Applies a wrapping italic, struck-through name with a separate readable deletion suffix.
     */
    static void apply(Label label, Member member) {
        String name = member.getName().fullName;
        label.setText(name);
        label.setGraphic(null);
        label.setAccessibleText(name + (member.isDeleted() ? " (deleted)" : ""));
        if (!member.isDeleted()) {
            return;
        }
        Text value = new Text(name);
        value.setId("deletedMemberName");
        value.setStrikethrough(true);
        value.fontProperty().bind(Bindings.createObjectBinding(() -> Font.font(label.getFont().getFamily(),
                FontWeight.BOLD, FontPosture.ITALIC, label.getFont().getSize()), label.fontProperty()));
        value.fillProperty().bind(label.textFillProperty());
        Text suffix = new Text(" (deleted)");
        suffix.setId("deletedMemberSuffix");
        suffix.fontProperty().bind(label.fontProperty());
        suffix.fillProperty().bind(label.textFillProperty());
        TextFlow flow = new TextFlow(value, suffix);
        flow.setMinWidth(0);
        flow.prefWidthProperty().bind(label.widthProperty());
        label.setText(null);
        label.setGraphic(flow);
    }
}
