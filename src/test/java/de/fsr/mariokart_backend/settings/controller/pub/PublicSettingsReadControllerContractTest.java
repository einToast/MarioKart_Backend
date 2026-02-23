package de.fsr.mariokart_backend.settings.controller.pub;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.InputStream;
import java.util.Set;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import de.fsr.mariokart_backend.settings.model.dto.TournamentDTO;
import de.fsr.mariokart_backend.settings.service.pub.PublicSettingsReadService;
import de.fsr.mariokart_backend.testsupport.AbstractWebMvcSliceTest;

@WebMvcTest(PublicSettingsReadController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Tag("contract")
class PublicSettingsReadControllerContractTest extends AbstractWebMvcSliceTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private PublicSettingsReadService publicSettingsReadService;

    @Test
    void responseMatchesSchema() throws Exception {
        when(publicSettingsReadService.getSettings()).thenReturn(new TournamentDTO(true, true, 6));

        String responseBody = mockMvc.perform(get("/public/settings"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(responseBody);
        Set<ValidationMessage> errors;
        try (InputStream schemaStream = new ClassPathResource(
                "contracts/public-settings-response.schema.json").getInputStream()) {
            JsonSchema schema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012)
                    .getSchema(schemaStream);
            errors = schema.validate(json);
        }

        assertThat(errors).isEmpty();
    }
}
