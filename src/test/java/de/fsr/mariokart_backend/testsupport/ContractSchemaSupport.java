package de.fsr.mariokart_backend.testsupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.core.io.ClassPathResource;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import com.networknt.schema.Error;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;

public final class ContractSchemaSupport {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final SchemaRegistry SCHEMA_REGISTRY = SchemaRegistry.withDefaultDialect(
            SpecificationVersion.DRAFT_2020_12);

    private ContractSchemaSupport() {
    }

    public static void assertJsonMatchesDefinition(String schemaPath, String definitionKey, String responseBody)
            throws IOException {
        JsonNode responseNode = OBJECT_MAPPER.readTree(responseBody);
        assertNodeMatchesDefinition(schemaPath, definitionKey, responseNode);
    }

    public static void assertStringMatchesDefinition(String schemaPath, String definitionKey, String responseBody)
            throws IOException {
        assertNodeMatchesDefinition(schemaPath, definitionKey, OBJECT_MAPPER.getNodeFactory().stringNode(responseBody));
    }

    public static void assertNodeMatchesDefinition(String schemaPath, String definitionKey, JsonNode responseNode)
            throws IOException {
        JsonNode schemaRoot = loadSchema(schemaPath);

        JsonNode definitions = schemaRoot.path("$defs");
        if (!definitions.isObject() || definitions.path(definitionKey).isMissingNode()) {
            fail("Schema definition '%s' not found in %s", definitionKey, schemaPath);
        }

        ObjectNode schemaWrapper = OBJECT_MAPPER.createObjectNode();
        schemaWrapper.put("$schema", "https://json-schema.org/draft/2020-12/schema");
        schemaWrapper.set("$defs", definitions);
        schemaWrapper.put("$ref", "#/$defs/" + definitionKey);

        Schema schema = SCHEMA_REGISTRY.getSchema(schemaWrapper);
        List<Error> errors = schema.validate(responseNode);

        assertThat(errors)
                .as("Contract validation errors for %s -> %s: %s", schemaPath, definitionKey, errors)
                .isEmpty();
    }

    private static JsonNode loadSchema(String schemaPath) throws IOException {
        try (InputStream schemaStream = new ClassPathResource(schemaPath).getInputStream()) {
            return OBJECT_MAPPER.readTree(schemaStream);
        }
    }
}
