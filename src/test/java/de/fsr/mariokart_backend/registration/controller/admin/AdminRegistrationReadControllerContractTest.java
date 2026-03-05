package de.fsr.mariokart_backend.registration.controller.admin;

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
import de.fsr.mariokart_backend.registration.service.admin.AdminRegistrationReadService;
import de.fsr.mariokart_backend.testsupport.AbstractWebMvcSliceTest;
import de.fsr.mariokart_backend.testsupport.ContractSchemaSupport;

@WebMvcTest(AdminRegistrationReadController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Tag("contract")
class AdminRegistrationReadControllerContractTest extends AbstractWebMvcSliceTest {

    private static final String SCHEMA = "contracts/admin/registration/admin-registration-read-controller.schema.json";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminRegistrationReadService adminRegistrationReadService;

    @Test
    void getTeamsSortedByFinalPointsMatchesContract() throws Exception {
        when(adminRegistrationReadService.getTeamsSortedByFinalPoints())
                .thenReturn(List.of(team(1L, "Finalists", "Mario")));

        MvcResult result = mockMvc.perform(get("/admin/teams/sortedByFinalPoints"))
                .andExpect(status().isOk())
                .andReturn();

        ContractSchemaSupport.assertJsonMatchesDefinition(SCHEMA, "get_sortedByFinalPoints_success",
                result.getResponse().getContentAsString());
    }

    @Test
    void getFinalTeamsMatchesContract() throws Exception {
        when(adminRegistrationReadService.getFinalTeamsReturnDTO())
                .thenReturn(List.of(team(2L, "Top4", "Luigi")));

        MvcResult result = mockMvc.perform(get("/admin/teams/finalTeams"))
                .andExpect(status().isOk())
                .andReturn();

        ContractSchemaSupport.assertJsonMatchesDefinition(SCHEMA, "get_finalTeams_success",
                result.getResponse().getContentAsString());
    }

    private static TeamReturnDTO team(Long id, String teamName, String characterName) {
        return new TeamReturnDTO(id, teamName, new CharacterReturnDTO(id, characterName), true, true, 0, 0, 0);
    }
}
