package seedu.address.commons.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

public class GuiSettingsTest {
    @Test
    public void appearance_keepsGeometryImmutableAndParticipatesInEquality() {
        GuiSettings light = new GuiSettings(1100, 700, 20, 30);
        GuiSettings dark = light.withDarkMode(true);
        assertFalse(light.isDarkMode());
        assertNotEquals(light, dark);
        assertEquals(light, dark.withDarkMode(false));
        assertEquals(dark.hashCode(), new GuiSettings(1100, 700, 20, 30).withDarkMode(true).hashCode());
        dark.getWindowCoordinates().setLocation(900, 900);
        assertEquals(light.getWindowCoordinates(), dark.getWindowCoordinates());
        assertEquals(null, new GuiSettings().withDarkMode(true).getWindowCoordinates());
    }

    @Test
    public void toStringMethod() {
        GuiSettings guiSettings = new GuiSettings();
        String expected = GuiSettings.class.getCanonicalName() + "{windowWidth=" + guiSettings.getWindowWidth()
                + ", windowHeight=" + guiSettings.getWindowHeight() + ", windowCoordinates="
                + guiSettings.getWindowCoordinates() + ", darkMode=false}";
        assertEquals(expected, guiSettings.toString());
    }
}
