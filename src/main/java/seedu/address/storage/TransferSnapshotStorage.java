package seedu.address.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.ReadOnlyAddressBook;

/** Strict, versioned interchange snapshots; ordinary application loading remains backward compatible. */
public class TransferSnapshotStorage {
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules()
            .enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION)
            .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

    /** Reads one complete JSON object and validates every entity and cross-collection reference. */
    public ReadOnlyAddressBook read(Path source) throws IOException, IllegalValueException {
        try (JsonParser parser = mapper.getFactory().createParser(source.toFile())) {
            JsonNode tree = mapper.readTree(parser);
            if (!(tree instanceof ObjectNode root)) {
                throw new IOException("A snapshot must be a JSON object.");
            }
            if (parser.nextToken() != null) {
                throw new IOException("A snapshot must contain exactly one JSON object.");
            }
            JsonNode version = root.remove("schemaVersion");
            if (version != null && !version.equals(IntNode.valueOf(1))) {
                throw new IOException("Unsupported snapshot schemaVersion; expected integer 1.");
            }
            for (String collection : List.of("persons", "members", "equipment", "loans")) {
                JsonNode entries = root.get(collection);
                if (entries != null && !entries.isArray()) {
                    throw new IOException("Snapshot " + collection + " must be an array.");
                }
            }
            return mapper.treeToValue(root, JsonSerializableAddressBook.class).toModelType();
        } catch (NullPointerException exception) {
            // Legacy adapters assume individual nested entries (such as person tags) are non-null.
            throw new IOException("Snapshot contains a null required value.", exception);
        }
    }

    /** Writes a complete version-1 snapshot without replacing any existing destination. */
    public void writeNew(Path destination, ReadOnlyAddressBook data) throws IOException {
        ObjectNode root = (ObjectNode) mapper.readTree(JsonUtil.toJsonString(new JsonSerializableAddressBook(data)));
        root.put("schemaVersion", 1);
        Path target = destination.toAbsolutePath().normalize();
        Files.createDirectories(target.getParent());
        Path temporary = Files.createTempFile(target.getParent(), ".laplace-transfer-", ".tmp");
        try {
            Files.writeString(temporary, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root));
            // No REPLACE_EXISTING: a destination created after preview/export starts must also be protected.
            Files.move(temporary, target);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
