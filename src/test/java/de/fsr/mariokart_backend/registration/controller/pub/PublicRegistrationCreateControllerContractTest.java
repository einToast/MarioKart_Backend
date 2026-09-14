package de.fsr.mariokart_backend.registration.controller.pub;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.registration.model.dto.CharacterReturnDTO;
import de.fsr.mariokart_backend.registration.model.dto.TeamInputDTO;
import de.fsr.mariokart_backend.registration.model.dto.TeamReturnDTO;
import de.fsr.mariokart_backend.registration.service.pub.PublicRegistrationCreateService;
import de.fsr.mariokart_backend.testsupport.AbstractWebMvcSliceTest;
import de.fsr.mariokart_backend.testsupport.ContractSchemaSupport;

@WebMvcTest(PublicRegistrationCreateController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Tag("contract")
class PublicRegistrationCreateControllerContractTest extends AbstractWebMvcSliceTest {

    private static final String SCHEMA =
            "contracts/public/registration/public-registration-create-controller.schema.json";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PublicRegistrationCreateService publicRegistrationCreateService;

    @Test
    void registerTeamSuccessMatchesContract() throws Exception {
        TeamInputDTO input = new TeamInputDTO("Speedsters", "Mario", true, true);
        TeamReturnDTO response = new TeamReturnDTO(1L, "Speedsters", new CharacterReturnDTO(1L, "Mario"), true, true, 0,
                0, 0);

        when(publicRegistrationCreateService.registerTeam(any(TeamInputDTO.class))).thenReturn(response);

        MvcResult result = mockMvc.perform(post("/public/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andReturn();

        ContractSchemaSupport.assertJsonMatchesDefinition(
                SCHEMA,
                "post_root_success",
                result.getResponse().getContentAsString());
    }

    @Test
    void registerTeamBadRequestMatchesContract() throws Exception {
        when(publicRegistrationCreateService.registerTeam(any(TeamInputDTO.class)))
                .thenThrow(new IllegalArgumentException("Team name already exists"));

        MvcResult result = mockMvc.perform(post("/public/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TeamInputDTO("Speedsters", "Mario", true, true))))
                .andExpect(status().isBadRequest())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(
                SCHEMA,
                "post_root_error_400",
                result.getResponse().getContentAsString());
    }

    @Test
    void registerTeamNotFoundMatchesContract() throws Exception {
        when(publicRegistrationCreateService.registerTeam(any(TeamInputDTO.class)))
                .thenThrow(new EntityNotFoundException("Character missing"));

        MvcResult result = mockMvc.perform(post("/public/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TeamInputDTO("Speedsters", "Unknown", true, true))))
                .andExpect(status().isNotFound())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(
                SCHEMA,
                "post_root_error_404",
                result.getResponse().getContentAsString());
    }

    @Test
    void registerTeamConflictMatchesContract() throws Exception {
        when(publicRegistrationCreateService.registerTeam(any(TeamInputDTO.class)))
                .thenThrow(new RoundsAlreadyExistsException("Schedule already exists"));

        MvcResult result = mockMvc.perform(post("/public/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TeamInputDTO("Speedsters", "Mario", true, true))))
                .andExpect(status().isConflict())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(
                SCHEMA,
                "post_root_error_409",
                result.getResponse().getContentAsString());
    }
}
