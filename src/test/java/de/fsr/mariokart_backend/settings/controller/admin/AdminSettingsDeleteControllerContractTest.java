package de.fsr.mariokart_backend.settings.controller.admin;

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

import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.settings.service.admin.AdminSettingsDeleteService;
import de.fsr.mariokart_backend.testsupport.AbstractWebMvcSliceTest;
import de.fsr.mariokart_backend.testsupport.ContractSchemaSupport;

@WebMvcTest(AdminSettingsDeleteController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Tag("contract")
class AdminSettingsDeleteControllerContractTest extends AbstractWebMvcSliceTest {

    private static final String SCHEMA = "contracts/admin/settings/admin-settings-delete-controller.schema.json";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminSettingsDeleteService adminSettingsDeleteService;

    @Test
    void resetSuccessMatchesContract() throws Exception {
        MvcResult result = mockMvc.perform(delete("/admin/settings/reset"))
                .andExpect(status().isOk())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(
                SCHEMA,
                "delete_reset_success",
                result.getResponse().getContentAsString());
    }

    @Test
    void resetConflictMatchesContract() throws Exception {
        doThrow(new RoundsAlreadyExistsException("Schedule already exists"))
                .when(adminSettingsDeleteService)
                .reset();

        MvcResult result = mockMvc.perform(delete("/admin/settings/reset"))
                .andExpect(status().isConflict())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(
                SCHEMA,
                "delete_reset_error_409",
                result.getResponse().getContentAsString());
    }
}
