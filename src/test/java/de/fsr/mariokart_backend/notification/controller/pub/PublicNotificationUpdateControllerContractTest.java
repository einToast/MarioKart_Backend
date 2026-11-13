package de.fsr.mariokart_backend.notification.controller.pub;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

import de.fsr.mariokart_backend.notification.model.dto.PushSubscriptionLanguageDTO;
import de.fsr.mariokart_backend.notification.service.pub.PublicNotificationUpdateService;
import de.fsr.mariokart_backend.testsupport.AbstractWebMvcSliceTest;
import de.fsr.mariokart_backend.testsupport.ContractSchemaSupport;

@WebMvcTest(PublicNotificationUpdateController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Tag("contract")
class PublicNotificationUpdateControllerContractTest extends AbstractWebMvcSliceTest {

    private static final String SCHEMA = "contracts/public/notification/public-notification-update-controller.schema.json";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PublicNotificationUpdateService publicNotificationUpdateService;

    @Test
    void updateLanguageSuccessMatchesContract() throws Exception {
        MvcResult result = mockMvc.perform(put("/public/notification/subscription/language")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PushSubscriptionLanguageDTO("https://example.test/sub", "en"))))
                .andExpect(status().isOk())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(
                SCHEMA,
                "put_subscription_language_success",
                result.getResponse().getContentAsString());
    }
}
