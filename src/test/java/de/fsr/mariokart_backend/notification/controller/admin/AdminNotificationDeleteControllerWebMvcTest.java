package de.fsr.mariokart_backend.notification.controller.admin;

import static org.mockito.Mockito.verify;
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

import de.fsr.mariokart_backend.notification.service.admin.AdminNotificationDeleteService;
import de.fsr.mariokart_backend.testsupport.AbstractWebMvcSliceTest;

@WebMvcTest(AdminNotificationDeleteController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Tag("web")
class AdminNotificationDeleteControllerWebMvcTest extends AbstractWebMvcSliceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminNotificationDeleteService adminNotificationDeleteService;

    @Test
    void deleteAllSubscriptionsDelegatesToTheService() throws Exception {
        mockMvc.perform(delete("/admin/notification/subscriptions"))
                .andExpect(status().isOk());

        verify(adminNotificationDeleteService).deleteAllSubscriptions();
    }
}
