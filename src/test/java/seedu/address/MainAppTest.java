package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.AppPaths;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonUserPrefsStorage;

/**
 * Verifies application startup paths and preference recovery.
 */
public class MainAppTest {
    @TempDir
    public Path testFolder;

    @Test
    public void init_nullGuiSettings_recoversAndSavesDefaults() throws Exception {
        assertStartupPreferences("{\"guiSettings\":null}", new UserPrefs());
    }

    @Test
    public void init_missingGuiSettings_preservesDefaultPreferences() throws Exception {
        assertStartupPreferences("{}", new UserPrefs());
    }

    @Test
    public void init_validGuiSettings_preservesGeometryAndDarkMode() throws Exception {
        UserPrefs expected = new UserPrefs();
        expected.setGuiSettings(new GuiSettings(1200, 800, 50, 70).withDarkMode(true));
        assertStartupPreferences(JsonUtil.toJsonString(expected), expected);
    }

    /**
     * Verifies the real startup constructs a model and rewrites usable preferences.
     */
    private void assertStartupPreferences(String preferencesJson, UserPrefs expected) throws Exception {
        Path prefsFile = testFolder.resolve("preferences.json");
        Files.writeString(prefsFile, preferencesJson);
        JsonUserPrefsStorage prefsStorage = new JsonUserPrefsStorage(prefsFile);
        MainApp app = new MainApp() {
            @Override
            protected UserPrefs initPrefs(JsonUserPrefsStorage storage) {
                return super.initPrefs(prefsStorage);
            }
        };

        app.init();

        assertEquals(expected, app.model.getUserPrefs());
        assertEquals(expected, prefsStorage.readUserPrefs().orElseThrow());
        assertNotNull(app.logic);
        assertNotNull(app.ui);
    }

    @Test
    public void init_defaultStorage_usesRuntimeHomeForPreferencesAndData() throws Exception {
        MainApp app = new MainApp() {
            @Override
            protected UserPrefs initPrefs(JsonUserPrefsStorage storage) {
                // Inspect the real startup path without writing preferences in the development folder.
                assertEquals(AppPaths.getPreferencesFilePath(), storage.getUserPrefsFilePath());
                return new UserPrefs();
            }
        };
        app.init();
        assertEquals(AppPaths.getPreferencesFilePath(), app.storage.getUserPrefsFilePath());
        assertEquals(AppPaths.getDataFilePath(), app.storage.getAddressBookFilePath());
        assertNotNull(app.model);
        assertNotNull(app.logic);
        assertNotNull(app.ui);
    }
}
