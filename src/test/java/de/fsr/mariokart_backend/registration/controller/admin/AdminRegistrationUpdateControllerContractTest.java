package de.fsr.mariokart_backend.registration.controller.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import org.springframework.test.web.servlet.MvcResult;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.registration.model.dto.CharacterReturnDTO;
import de.fsr.mariokart_backend.registration.model.dto.TeamInputDTO;
import de.fsr.mariokart_backend.registration.model.dto.TeamReturnDTO;
import de.fsr.mariokart_backend.registration.service.admin.AdminRegistrationUpdateService;
import de.fsr.mariokart_backend.testsupport.AbstractWebMvcSliceTest;
import de.fsr.mariokart_backend.testsupport.ContractSchemaSupport;

@WebMvcTest(AdminRegistrationUpdateController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Tag("contract")
class AdminRegistrationUpdateControllerContractTest extends AbstractWebMvcSliceTest {

    private static final String SCHEMA =
            "contracts/admin/registration/admin-registration-update-controller.schema.json";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminRegistrationUpdateService adminRegistrationUpdateService;

    @Test
    void updateTeamSuccessMatchesContract() throws Exception {
        TeamInputDTO input = new TeamInputDTO("Speedsters", "Mario", true, true);
        TeamReturnDTO response = new TeamReturnDTO(1L, "Speedsters", new CharacterReturnDTO(1L, "Mario"), true, true,
                0, 0, 0);

        when(adminRegistrationUpdateService.updateTeam(any(Long.class), any(TeamInputDTO.class))).thenReturn(response);

        MvcResult result = mockMvc.perform(put("/admin/teams/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andReturn();

        ContractSchemaSupport.assertJsonMatchesDefinition(SCHEMA, "put_id_success",
                result.getResponse().getContentAsString());
    }

    @Test
    void updateTeamNotFoundMatchesContract() throws Exception {
        when(adminRegistrationUpdateService.updateTeam(any(Long.class), any(TeamInputDTO.class)))
                .thenThrow(new EntityNotFoundException("No team with this id"));

        MvcResult result = mockMvc.perform(put("/admin/teams/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TeamInputDTO("Speedsters", "Mario", true, true))))
                .andExpect(status().isNotFound())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(SCHEMA, "put_id_error_404",
                result.getResponse().getContentAsString());
    }

    @Test
    void updateTeamBadRequestMatchesContract() throws Exception {
        when(adminRegistrationUpdateService.updateTeam(any(Long.class), any(TeamInputDTO.class)))
                .thenThrow(new IllegalArgumentException("Team name already exists"));

        MvcResult result = mockMvc.perform(put("/admin/teams/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TeamInputDTO("Speedsters", "Mario", true, true))))
                .andExpect(status().isBadRequest())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(SCHEMA, "put_id_error_400",
                result.getResponse().getContentAsString());
    }

    @Test
    void resetFinalParticipationMatchesContract() throws Exception {
        when(adminRegistrationUpdateService.resetEveryTeamFinalParticipation())
                .thenReturn(List.of(new TeamReturnDTO(1L, "A", new CharacterReturnDTO(1L, "Mario"), true, true, 0, 0, 0)));

        MvcResult result = mockMvc.perform(put("/admin/teams/finalParticipation/reset"))
                .andExpect(status().isOk())
                .andReturn();

        ContractSchemaSupport.assertJsonMatchesDefinition(SCHEMA, "put_finalParticipation_reset_success",
                result.getResponse().getContentAsString());
    }
}
