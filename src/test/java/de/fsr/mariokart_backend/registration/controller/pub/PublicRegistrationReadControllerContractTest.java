package de.fsr.mariokart_backend.registration.controller.pub;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.test.web.servlet.MvcResult;

import de.fsr.mariokart_backend.registration.model.dto.CharacterReturnDTO;
import de.fsr.mariokart_backend.registration.model.dto.TeamReturnDTO;
import de.fsr.mariokart_backend.registration.service.pub.PublicRegistrationReadService;
import de.fsr.mariokart_backend.testsupport.AbstractWebMvcSliceTest;
import de.fsr.mariokart_backend.testsupport.ContractSchemaSupport;

@WebMvcTest(PublicRegistrationReadController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Tag("contract")
class PublicRegistrationReadControllerContractTest extends AbstractWebMvcSliceTest {

    private static final String SCHEMA = "contracts/public/registration/public-registration-read-controller.schema.json";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PublicRegistrationReadService publicRegistrationReadService;

    @Test
    void getTeamsMatchesContract() throws Exception {
        when(publicRegistrationReadService.getTeams()).thenReturn(List.of(team(1L, "A", "Mario")));

        MvcResult result = mockMvc.perform(get("/public/teams"))
                .andExpect(status().isOk())
                .andReturn();

        ContractSchemaSupport.assertJsonMatchesDefinition(SCHEMA, "get_root_success",
                result.getResponse().getContentAsString());
    }

    @Test
    void getTeamsSortedByGroupPointsMatchesContract() throws Exception {
        when(publicRegistrationReadService.getTeamsSortedByGroupPoints()).thenReturn(List.of(team(2L, "B", "Luigi")));

        MvcResult result = mockMvc.perform(get("/public/teams/sortedByGroupPoints"))
                .andExpect(status().isOk())
                .andReturn();

        ContractSchemaSupport.assertJsonMatchesDefinition(SCHEMA, "get_sortedByGroupPoints_success",
                result.getResponse().getContentAsString());
    }

    @Test
    void getTeamsSortedByTeamNameMatchesContract() throws Exception {
        when(publicRegistrationReadService.getTeamsSortedByTeamName()).thenReturn(List.of(team(3L, "C", "Peach")));

        MvcResult result = mockMvc.perform(get("/public/teams/sortedByTeamName"))
                .andExpect(status().isOk())
                .andReturn();

        ContractSchemaSupport.assertJsonMatchesDefinition(SCHEMA, "get_sortedByTeamName_success",
                result.getResponse().getContentAsString());
    }

    @Test
    void getAvailableCharactersMatchesContract() throws Exception {
        when(publicRegistrationReadService.getAvailableCharacters())
                .thenReturn(List.of(new CharacterReturnDTO(1L, "Mario")));

        MvcResult result = mockMvc.perform(get("/public/teams/characters/available"))
                .andExpect(status().isOk())
                .andReturn();

        ContractSchemaSupport.assertJsonMatchesDefinition(SCHEMA, "get_characters_available_success",
                result.getResponse().getContentAsString());
    }

    @Test
    void getTeamsNotInRoundMatchesContract() throws Exception {
        when(publicRegistrationReadService.getTeamsNotInRound(4L)).thenReturn(List.of(team(4L, "D", "Toad")));

        MvcResult result = mockMvc.perform(get("/public/teams/notInRound/4"))
                .andExpect(status().isOk())
                .andReturn();

        ContractSchemaSupport.assertJsonMatchesDefinition(SCHEMA, "get_notInRound_roundId_success",
                result.getResponse().getContentAsString());
    }

    private static TeamReturnDTO team(Long id, String teamName, String characterName) {
        return new TeamReturnDTO(id, teamName, new CharacterReturnDTO(id, characterName), true, true, 0, 0, 0);
    }
}
