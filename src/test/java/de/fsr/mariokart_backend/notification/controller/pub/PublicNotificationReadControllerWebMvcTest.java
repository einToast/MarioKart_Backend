package de.fsr.mariokart_backend.notification.controller.pub;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import de.fsr.mariokart_backend.notification.service.pub.PublicNotificationReadService;
import de.fsr.mariokart_backend.testsupport.AbstractWebMvcSliceTest;

@WebMvcTest(PublicNotificationReadController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Tag("web")
class PublicNotificationReadControllerWebMvcTest extends AbstractWebMvcSliceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PublicNotificationReadService publicNotificationReadService;

    @Test
    void contextLoads() {
        assertThat(mockMvc).isNotNull();
    }

    @Test
    void getPublicKeyReturnsConfiguredKey() throws Exception {
        when(publicNotificationReadService.getPublicKey()).thenReturn("PUBLIC_KEY");

        mockMvc.perform(get("/public/notification/public-key"))
                .andExpect(status().isOk())
                .andExpect(content().string("PUBLIC_KEY"));
    }
}
