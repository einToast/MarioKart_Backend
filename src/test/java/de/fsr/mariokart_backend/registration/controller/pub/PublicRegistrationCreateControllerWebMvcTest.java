package de.fsr.mariokart_backend.registration.controller.pub;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.registration.model.dto.CharacterReturnDTO;
import de.fsr.mariokart_backend.registration.model.dto.TeamInputDTO;
import de.fsr.mariokart_backend.registration.model.dto.TeamReturnDTO;
import de.fsr.mariokart_backend.registration.service.pub.PublicRegistrationCreateService;
import de.fsr.mariokart_backend.testsupport.AbstractWebMvcSliceTest;

@WebMvcTest(PublicRegistrationCreateController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Tag("web")
class PublicRegistrationCreateControllerWebMvcTest extends AbstractWebMvcSliceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PublicRegistrationCreateService publicRegistrationCreateService;

    @Test
    void contextLoads() {
        assertThat(mockMvc).isNotNull();
    }

    @Test
    void registerTeamReturnsCreatedTeam() throws Exception {
        TeamInputDTO input = new TeamInputDTO("Speedsters", "Mario", true, true);
        TeamReturnDTO response = new TeamReturnDTO(1L, "Speedsters", new CharacterReturnDTO(1L, "Mario"), true, true, 0, 0, 0);

        when(publicRegistrationCreateService.registerTeam(any(TeamInputDTO.class))).thenReturn(response);

        mockMvc.perform(post("/public/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.teamName").value("Speedsters"))
                .andExpect(jsonPath("$.character.characterName").value("Mario"));
    }

    @Test
    void registerTeamReturnsBadRequestForValidationErrors() throws Exception {
        TeamInputDTO input = new TeamInputDTO("Speedsters", "Mario", true, true);

        when(publicRegistrationCreateService.registerTeam(any(TeamInputDTO.class)))
                .thenThrow(new IllegalArgumentException("Team name already exists"));

        mockMvc.perform(post("/public/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Team name already exists")));
    }

    @Test
    void registerTeamReturnsNotFoundWhenCharacterMissing() throws Exception {
        TeamInputDTO input = new TeamInputDTO("Speedsters", "Unknown", true, true);

        when(publicRegistrationCreateService.registerTeam(any(TeamInputDTO.class)))
                .thenThrow(new EntityNotFoundException("There is no character with this name."));

        mockMvc.perform(post("/public/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("character with this name")));
    }

    @Test
    void registerTeamReturnsConflictWhenScheduleAlreadyExists() throws Exception {
        TeamInputDTO input = new TeamInputDTO("Speedsters", "Mario", true, true);

        when(publicRegistrationCreateService.registerTeam(any(TeamInputDTO.class)))
                .thenThrow(new RoundsAlreadyExistsException("Schedule already exists"));

        mockMvc.perform(post("/public/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("Schedule already exists")));
    }

    @Test
    void registerTeamReturnsConflictWhenTournamentStateDisallowsRegistration() throws Exception {
        TeamInputDTO input = new TeamInputDTO("Speedsters", "Mario", true, true);

        when(publicRegistrationCreateService.registerTeam(any(TeamInputDTO.class)))
                .thenThrow(new IllegalStateException("Registration is closed"));

        mockMvc.perform(post("/public/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("Registration is closed")));
    }
}
