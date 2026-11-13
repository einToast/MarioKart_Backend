package de.fsr.mariokart_backend.notification.controller.pub;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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

import de.fsr.mariokart_backend.notification.model.dto.PushSubscriptionEndpointDTO;
import de.fsr.mariokart_backend.notification.service.pub.PublicNotificationDeleteService;
import de.fsr.mariokart_backend.testsupport.AbstractWebMvcSliceTest;
import de.fsr.mariokart_backend.testsupport.ContractSchemaSupport;

@WebMvcTest(PublicNotificationDeleteController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Tag("contract")
class PublicNotificationDeleteControllerContractTest extends AbstractWebMvcSliceTest {

    private static final String SCHEMA = "contracts/public/notification/public-notification-delete-controller.schema.json";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PublicNotificationDeleteService publicNotificationDeleteService;

    @Test
    void unsubscribeSuccessMatchesContract() throws Exception {
        MvcResult result = mockMvc.perform(delete("/public/notification/subscription")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PushSubscriptionEndpointDTO("https://example.test/sub"))))
                .andExpect(status().isOk())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(
                SCHEMA,
                "delete_subscription_success",
                result.getResponse().getContentAsString());
    }
}
