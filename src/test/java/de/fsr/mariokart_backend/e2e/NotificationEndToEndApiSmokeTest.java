package de.fsr.mariokart_backend.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockCookie;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import de.fsr.mariokart_backend.e2e.testsupport.AbstractEndToEndApiSmokeTest;
import de.fsr.mariokart_backend.registration.model.Team;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("integration")
@TestPropertySource(properties = {
        "app.user.secret-key=MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI="
})
class NotificationEndToEndApiSmokeTest extends AbstractEndToEndApiSmokeTest {

    @Test
    void notificationControllersSmoke() throws Exception {
        Team team = createTeamDirect("Push Team", "Mario");

        mockMvc.perform(get("/public/notification/public-key"))
                .andExpect(status().isOk())
                .andExpect(content().string("BGENDEGNHWKtakXmUiiZpqmxHgtK0wYMdsEv5ORs80JfFBP8GiZytnUNKKhUAXVain3cpkBa0UEK7GxdhMXTYWM"));

        mockMvc.perform(post("/public/notification/subscribe")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"endpoint":"https://example.com/push","p256dh":"p256dh-key","auth":"auth-key","teamId":%d}
                                """.formatted(team.getId())))
                .andExpect(status().isOk());

        assertThat(pushSubscriptionRepository.count()).isEqualTo(1);

        MockCookie adminCookie = loginAsAdmin();

        mockMvc.perform(post("/admin/notification/send")
                        .cookie(adminCookie)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"title":"Title","message":"Body"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/admin/notification/send/{teamId}", team.getId())
                        .cookie(adminCookie)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"title":"Team Title","message":"Team Body"}
                                """))
                .andExpect(status().isOk());
    }
}
