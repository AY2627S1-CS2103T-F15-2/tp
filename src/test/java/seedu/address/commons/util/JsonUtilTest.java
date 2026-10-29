package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.testutil.SerializableTestClass;
import seedu.address.testutil.TestUtil;

/**
 * Tests JSON Read and Write
 */
public class JsonUtilTest {

    private static final Path SERIALIZATION_FILE = TestUtil.getFilePathInSandboxFolder("serialize.json");

    @TempDir
    public Path directory;

    @Test
    public void serializeObjectToJsonFile_noExceptionThrown() throws IOException {
        SerializableTestClass serializableTestClass = new SerializableTestClass();
        serializableTestClass.setTestValues();

        JsonUtil.serializeObjectToJsonFile(SERIALIZATION_FILE, serializableTestClass);

        assertEquals(FileUtil.readFromFile(SERIALIZATION_FILE), SerializableTestClass.JSON_STRING_REPRESENTATION);
    }

    @Test
    public void deserializeObjectFromJsonFile_noExceptionThrown() throws IOException {
        FileUtil.writeToFile(SERIALIZATION_FILE, SerializableTestClass.JSON_STRING_REPRESENTATION);

        SerializableTestClass serializableTestClass = JsonUtil
                .deserializeObjectFromJsonFile(SERIALIZATION_FILE, SerializableTestClass.class);

        assertEquals(serializableTestClass.getName(), SerializableTestClass.getNameTestValue());
        assertEquals(serializableTestClass.getListOfLocalDateTimes(), SerializableTestClass.getListTestValues());
        assertEquals(serializableTestClass.getMapOfIntegerToString(), SerializableTestClass.getHashMapTestValues());
    }

    @Test
    public void readJsonFile_nullRootAndTrailingContent_rejectsWithoutChangingBytes() throws Exception {
        Path data = directory.resolve("data.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(data);
        String valid = "{\"persons\":[],\"members\":[],\"equipment\":[],\"loans\":[]}";
        for (String marker : List.of("", "\ufeff")) {
            for (String input : List.of("null", "{", valid + " THIS IS NOT JSON", valid + "]",
                    valid + " {}", valid + " null")) {
                Files.writeString(data, marker + input);
                byte[] before = Files.readAllBytes(data);
                assertThrows(DataLoadingException.class, storage::readAddressBook, marker + input);
                assertArrayEquals(before, Files.readAllBytes(data));
            }
            Files.writeString(data, marker + valid + " \t\r\n");
            assertEquals(0, storage.readAddressBook().orElseThrow().getMemberList().size());
        }
    }

    @Test
    public void fromJsonString_trailingRoot_rejectsButAllowsWhitespace() throws Exception {
        for (String marker : List.of("", "\ufeff")) {
            assertThrows(IOException.class, () ->
                    JsonUtil.fromJsonString(marker + "{} {}", SerializableTestClass.class));
            JsonUtil.fromJsonString(marker + "{} \t\n", SerializableTestClass.class);
        }
    }

    //TODO: @Test jsonUtil_writeThenReadObjectToJson_correctObject()
}
