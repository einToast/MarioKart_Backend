package de.fsr.mariokart_backend.registration.controller.pub;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import de.fsr.mariokart_backend.registration.model.dto.CharacterReturnDTO;
import de.fsr.mariokart_backend.registration.model.dto.TeamReturnDTO;
import de.fsr.mariokart_backend.registration.service.pub.PublicRegistrationReadService;
import de.fsr.mariokart_backend.testsupport.AbstractWebMvcSliceTest;

@WebMvcTest(PublicRegistrationReadController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Tag("web")
class PublicRegistrationReadControllerWebMvcTest extends AbstractWebMvcSliceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PublicRegistrationReadService publicRegistrationReadService;

    @Test
    void contextLoads() {
        assertThat(mockMvc).isNotNull();
    }

    @Test
    void getTeamsReturnsList() throws Exception {
        when(publicRegistrationReadService.getTeams()).thenReturn(List.of(team(1L, "A", "Mario")));

        mockMvc.perform(get("/public/teams"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].teamName").value("A"));
    }

    @Test
    void getTeamsSortedByGroupPointsReturnsList() throws Exception {
        when(publicRegistrationReadService.getTeamsSortedByGroupPoints()).thenReturn(List.of(team(2L, "B", "Luigi")));

        mockMvc.perform(get("/public/teams/sortedByGroupPoints"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].teamName").value("B"));
    }

    @Test
    void getTeamsSortedByTeamNameReturnsList() throws Exception {
        when(publicRegistrationReadService.getTeamsSortedByTeamName()).thenReturn(List.of(team(3L, "C", "Peach")));

        mockMvc.perform(get("/public/teams/sortedByTeamName"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].teamName").value("C"));
    }

    @Test
    void getAvailableCharactersReturnsList() throws Exception {
        when(publicRegistrationReadService.getAvailableCharacters())
                .thenReturn(List.of(new CharacterReturnDTO(1L, "Mario")));

        mockMvc.perform(get("/public/teams/characters/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].characterName").value("Mario"));
    }

    @Test
    void getTeamsNotInRoundReturnsList() throws Exception {
        when(publicRegistrationReadService.getTeamsNotInRound(4L)).thenReturn(List.of(team(4L, "D", "Toad")));

        mockMvc.perform(get("/public/teams/notInRound/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].teamName").value("D"));
    }

    private static TeamReturnDTO team(Long id, String teamName, String characterName) {
        return new TeamReturnDTO(id, teamName, new CharacterReturnDTO(id, characterName), true, true, 0, 0, 0);
    }
}
