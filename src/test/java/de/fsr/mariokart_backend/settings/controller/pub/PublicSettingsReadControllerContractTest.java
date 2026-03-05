package de.fsr.mariokart_backend.settings.controller.pub;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import de.fsr.mariokart_backend.settings.model.dto.TournamentDTO;
import de.fsr.mariokart_backend.settings.service.pub.PublicSettingsReadService;
import de.fsr.mariokart_backend.testsupport.AbstractWebMvcSliceTest;
import de.fsr.mariokart_backend.testsupport.ContractSchemaSupport;

@WebMvcTest(PublicSettingsReadController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Tag("contract")
class PublicSettingsReadControllerContractTest extends AbstractWebMvcSliceTest {

    private static final String SCHEMA = "contracts/public/settings/public-settings-read-controller.schema.json";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PublicSettingsReadService publicSettingsReadService;

    @Test
    void getSettingsSuccessMatchesContract() throws Exception {
        when(publicSettingsReadService.getSettings()).thenReturn(new TournamentDTO(true, true, 6));

        MvcResult result = mockMvc.perform(get("/public/settings"))
                .andExpect(status().isOk())
                .andReturn();

        ContractSchemaSupport.assertJsonMatchesDefinition(
                SCHEMA,
                "get_root_success",
                result.getResponse().getContentAsString());
    }

    @Test
    void getSettingsNotFoundMatchesContract() throws Exception {
        when(publicSettingsReadService.getSettings()).thenThrow(new IllegalStateException("Settings do not exist."));

        MvcResult result = mockMvc.perform(get("/public/settings"))
                .andExpect(status().isNotFound())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(
                SCHEMA,
                "get_root_error_404",
                result.getResponse().getContentAsString());
    }
}
