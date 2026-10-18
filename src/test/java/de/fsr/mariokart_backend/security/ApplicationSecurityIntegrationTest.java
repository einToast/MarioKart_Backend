package de.fsr.mariokart_backend.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockCookie;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import de.fsr.mariokart_backend.config.AuthCookieConstants;
import de.fsr.mariokart_backend.schedule.service.admin.AdminScheduleReadService;
import de.fsr.mariokart_backend.user.model.User;
import de.fsr.mariokart_backend.user.repository.UserRepository;
import de.fsr.mariokart_backend.user.service.JWTManagerService;
import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("integration")
@TestPropertySource(properties = {
        "app.user.secret-key=MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI="
})
class ApplicationSecurityIntegrationTest {

    private static final String CSRF_COOKIE_NAME = "XSRF-TOKEN";
    private static final String CSRF_HEADER_NAME = "X-XSRF-TOKEN";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JWTManagerService jwtManagerService;

    @MockitoBean
    private AdminScheduleReadService adminScheduleReadService;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        when(adminScheduleReadService.getRounds()).thenReturn(List.of());
    }

    @Test
    void adminEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/admin/schedule/rounds"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void publicEndpointIsAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/public/healthcheck"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }

    @Test
    void adminEndpointAcceptsValidBearerToken() throws Exception {
        User user = new User("admin-integration", true);
        user.setPassword("secret");
        userRepository.save(user);

        String token = jwtManagerService.generateJWT(user);

        mockMvc.perform(get("/admin/schedule/rounds")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void adminEndpointAcceptsValidAuthCookie() throws Exception {
        User user = new User("admin-cookie", true);
        user.setPassword("secret");
        userRepository.save(user);

        String token = jwtManagerService.generateJWT(user);
        MockCookie cookie = new MockCookie(AuthCookieConstants.AUTH_COOKIE_NAME, token);

        mockMvc.perform(get("/admin/schedule/rounds")
                        .cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void adminEndpointRejectsInvalidToken() throws Exception {
        mockMvc.perform(get("/admin/schedule/rounds")
                        .header("Authorization", "Bearer invalid.token.value"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminEndpointRejectsTokenForUnknownUser() throws Exception {
        User ghost = new User("ghost-user", true);
        String token = jwtManagerService.generateJWT(ghost);

        mockMvc.perform(get("/admin/schedule/rounds")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void responsesIssueCsrfCookieReadableByFrontend() throws Exception {
        Cookie csrfCookie = fetchCsrfCookie();

        assertThat(csrfCookie.getValue()).isNotBlank();
        assertThat(csrfCookie.isHttpOnly()).isFalse();
        assertThat(csrfCookie.getPath()).isEqualTo("/");
    }

    @Test
    void stateChangingRequestWithoutCsrfTokenIsForbidden() throws Exception {
        mockMvc.perform(post("/public/user/logout"))
                .andExpect(status().isForbidden());
    }

    @Test
    void stateChangingRequestWithMismatchedCsrfTokenIsForbidden() throws Exception {
        Cookie csrfCookie = fetchCsrfCookie();

        mockMvc.perform(post("/public/user/logout")
                        .cookie(csrfCookie)
                        .header(CSRF_HEADER_NAME, "not-the-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void stateChangingRequestWithCsrfCookieAndHeaderIsAccepted() throws Exception {
        Cookie csrfCookie = fetchCsrfCookie();

        mockMvc.perform(post("/public/user/logout")
                        .cookie(csrfCookie)
                        .header(CSRF_HEADER_NAME, csrfCookie.getValue()))
                .andExpect(status().isOk());
    }

    @Test
    void stateChangingRequestWithBearerTokenSkipsCsrf() throws Exception {
        User user = new User("admin-bearer", true);
        user.setPassword("secret");
        userRepository.save(user);

        String token = jwtManagerService.generateJWT(user);

        mockMvc.perform(post("/public/user/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    private Cookie fetchCsrfCookie() throws Exception {
        MvcResult result = mockMvc.perform(get("/public/healthcheck"))
                .andExpect(status().isOk())
                .andReturn();

        Cookie csrfCookie = result.getResponse().getCookie(CSRF_COOKIE_NAME);
        assertThat(csrfCookie).isNotNull();
        return csrfCookie;
    }
}
