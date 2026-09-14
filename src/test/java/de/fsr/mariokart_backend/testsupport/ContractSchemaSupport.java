package de.fsr.mariokart_backend.testsupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import org.springframework.core.io.ClassPathResource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

public final class ContractSchemaSupport {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final JsonSchemaFactory JSON_SCHEMA_FACTORY = JsonSchemaFactory.getInstance(
            SpecVersion.VersionFlag.V202012);

    private ContractSchemaSupport() {
    }

    public static void assertJsonMatchesDefinition(String schemaPath, String definitionKey, String responseBody)
            throws IOException {
        JsonNode responseNode = OBJECT_MAPPER.readTree(responseBody);
        assertNodeMatchesDefinition(schemaPath, definitionKey, responseNode);
    }

    public static void assertStringMatchesDefinition(String schemaPath, String definitionKey, String responseBody)
            throws IOException {
        assertNodeMatchesDefinition(schemaPath, definitionKey, OBJECT_MAPPER.getNodeFactory().textNode(responseBody));
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

        JsonSchema schema = JSON_SCHEMA_FACTORY.getSchema(schemaWrapper);
        Set<ValidationMessage> errors = schema.validate(responseNode);

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
