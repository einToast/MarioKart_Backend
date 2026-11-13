package de.fsr.mariokart_backend.notification.controller.pub;

import static org.mockito.Mockito.verify;
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

import de.fsr.mariokart_backend.notification.model.dto.PushSubscriptionLanguageDTO;
import de.fsr.mariokart_backend.notification.service.pub.PublicNotificationUpdateService;
import de.fsr.mariokart_backend.testsupport.AbstractWebMvcSliceTest;

@WebMvcTest(PublicNotificationUpdateController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Tag("web")
class PublicNotificationUpdateControllerWebMvcTest extends AbstractWebMvcSliceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PublicNotificationUpdateService publicNotificationUpdateService;

    @Test
    void updateLanguagePassesTheRequestToTheService() throws Exception {
        PushSubscriptionLanguageDTO update = new PushSubscriptionLanguageDTO("https://example.test/sub", "en");

        mockMvc.perform(put("/public/notification/subscription/language")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk());

        verify(publicNotificationUpdateService).updateLanguage(update);
    }
}
