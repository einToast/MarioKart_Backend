package de.fsr.mariokart_backend.registration.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.registration.model.dto.CharacterReturnDTO;
import de.fsr.mariokart_backend.registration.model.dto.TeamInputDTO;
import de.fsr.mariokart_backend.registration.model.dto.TeamReturnDTO;
import de.fsr.mariokart_backend.registration.service.admin.AdminRegistrationUpdateService;
import de.fsr.mariokart_backend.testsupport.AbstractWebMvcSliceTest;

@WebMvcTest(AdminRegistrationUpdateController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Tag("web")
class AdminRegistrationUpdateControllerWebMvcTest extends AbstractWebMvcSliceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminRegistrationUpdateService adminRegistrationUpdateService;

    @Test
    void contextLoads() {
        assertThat(mockMvc).isNotNull();
    }

    @Test
    void updateTeamReturnsUpdatedTeam() throws Exception {
        TeamInputDTO input = new TeamInputDTO("Speedsters", "Mario", true, true);
        TeamReturnDTO response = new TeamReturnDTO(1L, "Speedsters", new CharacterReturnDTO(1L, "Mario"), true, true, 0, 0, 0);

        when(adminRegistrationUpdateService.updateTeam(any(Long.class), any(TeamInputDTO.class))).thenReturn(response);

        mockMvc.perform(put("/admin/teams/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamName").value("Speedsters"));
    }

    @Test
    void updateTeamReturnsNotFoundWhenTeamMissing() throws Exception {
        TeamInputDTO input = new TeamInputDTO("Speedsters", "Mario", true, true);

        when(adminRegistrationUpdateService.updateTeam(any(Long.class), any(TeamInputDTO.class)))
                .thenThrow(new EntityNotFoundException("There is no team with this ID."));

        mockMvc.perform(put("/admin/teams/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("team with this ID")));
    }

    @Test
    void updateTeamReturnsBadRequestForValidationErrors() throws Exception {
        TeamInputDTO input = new TeamInputDTO("Speedsters", "Mario", true, true);

        when(adminRegistrationUpdateService.updateTeam(any(Long.class), any(TeamInputDTO.class)))
                .thenThrow(new IllegalArgumentException("Team name already exists"));

        mockMvc.perform(put("/admin/teams/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Team name already exists")));
    }

    @Test
    void resetEveryTeamFinalParticipationReturnsTeams() throws Exception {
        when(adminRegistrationUpdateService.resetEveryTeamFinalParticipation())
                .thenReturn(List.of(new TeamReturnDTO(1L, "A", new CharacterReturnDTO(1L, "Mario"), true, true, 0, 0, 0)));

        mockMvc.perform(put("/admin/teams/finalParticipation/reset"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].teamName").value("A"));
    }
}
