package de.fsr.mariokart_backend.settings.controller.admin;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.settings.model.dto.TournamentDTO;
import de.fsr.mariokart_backend.settings.service.admin.AdminSettingsUpdateService;
import de.fsr.mariokart_backend.testsupport.AbstractWebMvcSliceTest;
import de.fsr.mariokart_backend.testsupport.ContractSchemaSupport;

@WebMvcTest(AdminSettingsUpdateController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Tag("contract")
class AdminSettingsUpdateControllerContractTest extends AbstractWebMvcSliceTest {

    private static final String SCHEMA = "contracts/admin/settings/admin-settings-update-controller.schema.json";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminSettingsUpdateService adminSettingsUpdateService;

    @Test
    void updateSettingsSuccessMatchesContract() throws Exception {
        TournamentDTO input = new TournamentDTO(true, false, 8);
        when(adminSettingsUpdateService.updateSettings(input)).thenReturn(input);

        MvcResult result = mockMvc.perform(put("/admin/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andReturn();

        ContractSchemaSupport.assertJsonMatchesDefinition(
                SCHEMA,
                "put_root_success",
                result.getResponse().getContentAsString());
    }

    @Test
    void updateSettingsNotFoundMatchesContract() throws Exception {
        TournamentDTO input = new TournamentDTO(true, true, 4);
        when(adminSettingsUpdateService.updateSettings(input)).thenThrow(new IllegalStateException("Settings missing."));

        MvcResult result = mockMvc.perform(put("/admin/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isNotFound())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(
                SCHEMA,
                "put_root_error_404",
                result.getResponse().getContentAsString());
    }

    @Test
    void updateSettingsConflictMatchesContract() throws Exception {
        TournamentDTO input = new TournamentDTO(true, true, 4);
        when(adminSettingsUpdateService.updateSettings(input))
                .thenThrow(new RoundsAlreadyExistsException("Schedule already exists."));

        MvcResult result = mockMvc.perform(put("/admin/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isConflict())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(
                SCHEMA,
                "put_root_error_409",
                result.getResponse().getContentAsString());
    }
}
