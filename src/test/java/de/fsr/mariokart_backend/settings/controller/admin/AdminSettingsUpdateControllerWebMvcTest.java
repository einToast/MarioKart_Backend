package de.fsr.mariokart_backend.settings.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.settings.model.dto.TournamentDTO;
import de.fsr.mariokart_backend.testsupport.AbstractWebMvcSliceTest;
import de.fsr.mariokart_backend.settings.service.admin.AdminSettingsUpdateService;

@WebMvcTest(AdminSettingsUpdateController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Tag("web")
class AdminSettingsUpdateControllerWebMvcTest extends AbstractWebMvcSliceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminSettingsUpdateService adminSettingsUpdateService;

    @Test
    void contextLoads() {
        assertThat(mockMvc).isNotNull();
    }

    @Test
    void updateSettingsReturnsUpdatedTournament() throws Exception {
        TournamentDTO response = new TournamentDTO(true, false, 8);
        when(adminSettingsUpdateService.updateSettings(new TournamentDTO(true, false, 8))).thenReturn(response);

        mockMvc.perform(put("/admin/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TournamentDTO(true, false, 8))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tournamentOpen").value(true))
                .andExpect(jsonPath("$.registrationOpen").value(false))
                .andExpect(jsonPath("$.maxGamesCount").value(8));
    }

    @Test
    void updateSettingsReturnsNotFoundWhenSettingsMissing() throws Exception {
        when(adminSettingsUpdateService.updateSettings(new TournamentDTO(true, true, 4)))
                .thenThrow(new IllegalStateException("Settings do not exist."));

        mockMvc.perform(put("/admin/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TournamentDTO(true, true, 4))))
                .andExpect(status().isNotFound())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Settings do not exist")));
    }

    @Test
    void updateSettingsReturnsConflictWhenRoundsExist() throws Exception {
        when(adminSettingsUpdateService.updateSettings(new TournamentDTO(true, true, 4)))
                .thenThrow(new RoundsAlreadyExistsException("Matches already exist."));

        mockMvc.perform(put("/admin/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TournamentDTO(true, true, 4))))
                .andExpect(status().isConflict())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Matches already exist")));
    }
}
