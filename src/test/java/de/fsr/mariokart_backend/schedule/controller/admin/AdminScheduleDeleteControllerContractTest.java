package de.fsr.mariokart_backend.schedule.controller.admin;

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

import de.fsr.mariokart_backend.schedule.service.admin.AdminScheduleDeleteService;
import de.fsr.mariokart_backend.testsupport.AbstractWebMvcSliceTest;
import de.fsr.mariokart_backend.testsupport.ContractSchemaSupport;

@WebMvcTest(AdminScheduleDeleteController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Tag("contract")
class AdminScheduleDeleteControllerContractTest extends AbstractWebMvcSliceTest {

    private static final String SCHEMA = "contracts/admin/schedule/admin-schedule-delete-controller.schema.json";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminScheduleDeleteService adminScheduleDeleteService;

    @Test
    void deleteScheduleMatchesContract() throws Exception {
        MvcResult result = mockMvc.perform(delete("/admin/schedule/create/schedule"))
                .andExpect(status().isOk())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(SCHEMA, "delete_create_schedule_success",
                result.getResponse().getContentAsString());
    }

    @Test
    void deleteFinalScheduleMatchesContract() throws Exception {
        MvcResult result = mockMvc.perform(delete("/admin/schedule/create/final_schedule"))
                .andExpect(status().isOk())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(SCHEMA, "delete_create_final_schedule_success",
                result.getResponse().getContentAsString());
    }
}
