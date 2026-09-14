package de.fsr.mariokart_backend.registration.controller.admin;

import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.registration.service.admin.AdminRegistrationDeleteService;
import de.fsr.mariokart_backend.testsupport.AbstractWebMvcSliceTest;
import de.fsr.mariokart_backend.testsupport.ContractSchemaSupport;

@WebMvcTest(AdminRegistrationDeleteController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Tag("contract")
class AdminRegistrationDeleteControllerContractTest extends AbstractWebMvcSliceTest {

    private static final String SCHEMA =
            "contracts/admin/registration/admin-registration-delete-controller.schema.json";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminRegistrationDeleteService adminRegistrationDeleteService;

    @Test
    void deleteTeamSuccessMatchesContract() throws Exception {
        MvcResult result = mockMvc.perform(delete("/admin/teams/1"))
                .andExpect(status().isOk())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(SCHEMA, "delete_id_success",
                result.getResponse().getContentAsString());
    }

    @Test
    void deleteTeamConflictMatchesContract() throws Exception {
        doThrow(new RoundsAlreadyExistsException("Schedule already exists"))
                .when(adminRegistrationDeleteService).deleteTeam(1L);

        MvcResult result = mockMvc.perform(delete("/admin/teams/1"))
                .andExpect(status().isConflict())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(SCHEMA, "delete_id_error_409",
                result.getResponse().getContentAsString());
    }

    @Test
    void deleteTeamNotFoundMatchesContract() throws Exception {
        doThrow(new EntityNotFoundException("No team with this id"))
                .when(adminRegistrationDeleteService).deleteTeam(1L);

        MvcResult result = mockMvc.perform(delete("/admin/teams/1"))
                .andExpect(status().isNotFound())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(SCHEMA, "delete_id_error_404",
                result.getResponse().getContentAsString());
    }

    @Test
    void deleteAllTeamsSuccessMatchesContract() throws Exception {
        MvcResult result = mockMvc.perform(delete("/admin/teams"))
                .andExpect(status().isOk())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(SCHEMA, "delete_root_success",
                result.getResponse().getContentAsString());
    }

    @Test
    void deleteAllTeamsConflictMatchesContract() throws Exception {
        doThrow(new RoundsAlreadyExistsException("Schedule already exists"))
                .when(adminRegistrationDeleteService).deleteAllTeams();

        MvcResult result = mockMvc.perform(delete("/admin/teams"))
                .andExpect(status().isConflict())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(SCHEMA, "delete_root_error_409",
                result.getResponse().getContentAsString());
    }
}
