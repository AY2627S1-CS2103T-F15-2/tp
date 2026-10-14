package seedu.address.ui;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Optional;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import seedu.address.model.equipment.Availability;
import seedu.address.model.equipment.Equipment;
import seedu.address.model.member.Member;

/** Immutable equipment row snapshot. The parent owns selection and activation. */
public class EquipmentCard extends UiPart<Region> {

    private static final String FXML = "EquipmentCard.fxml";

    @FXML
    private Label name;
    @FXML
    private Label category;
    @FXML
    private Label uuid;
    @FXML
    private Label condition;
    @FXML
    private Label availability;
    @FXML
    private Label holder;

    /** Renders caller-supplied availability and holder without querying or mutating the model. */
    public EquipmentCard(Equipment equipment, Availability availability, Optional<Member> holder) {
        super(validateInput(equipment, availability, holder));
        name.setText(equipment.getName());
        category.setText("Category: " + equipment.getCategory());
        uuid.setText("UUID: " + equipment.getUuid());
        condition.setText("Condition: " + equipment.getCondition());
        this.availability.setText("Availability: " + availability);
        this.holder.setText(holder.map(member -> "Holder: " + member.getName() + " (" + member.getNusId() + ")")
                .orElse(availability == Availability.ASSIGNED
                        ? "Holder reference missing (assigned equipment)" : "No current holder"));
    }

    private static String validateInput(Equipment equipment, Availability availability, Optional<Member> holder) {
        requireAllNonNull(equipment, availability, holder);
        return FXML;
    }
}
