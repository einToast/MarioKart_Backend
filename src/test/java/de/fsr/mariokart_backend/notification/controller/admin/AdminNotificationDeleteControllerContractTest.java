package de.fsr.mariokart_backend.notification.controller.admin;

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

import de.fsr.mariokart_backend.notification.service.admin.AdminNotificationDeleteService;
import de.fsr.mariokart_backend.testsupport.AbstractWebMvcSliceTest;
import de.fsr.mariokart_backend.testsupport.ContractSchemaSupport;

@WebMvcTest(AdminNotificationDeleteController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Tag("contract")
class AdminNotificationDeleteControllerContractTest extends AbstractWebMvcSliceTest {

    private static final String SCHEMA = "contracts/admin/notification/admin-notification-delete-controller.schema.json";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminNotificationDeleteService adminNotificationDeleteService;

    @Test
    void deleteAllSubscriptionsMatchesContract() throws Exception {
        MvcResult result = mockMvc.perform(delete("/admin/notification/subscriptions"))
                .andExpect(status().isOk())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(
                SCHEMA,
                "delete_subscriptions_success",
                result.getResponse().getContentAsString());
    }
}
