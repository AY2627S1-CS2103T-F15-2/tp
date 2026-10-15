package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.AppPaths;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonUserPrefsStorage;

/** Verifies the real application startup binds both storage components to the runtime home. */
public class MainAppTest {
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
