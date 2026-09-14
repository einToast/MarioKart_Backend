package de.fsr.mariokart_backend.e2e;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockCookie;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;

import de.fsr.mariokart_backend.config.AuthCookieConstants;
import de.fsr.mariokart_backend.e2e.testsupport.AbstractEndToEndApiSmokeTest;
import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("integration")
@TestPropertySource(properties = {
        "app.user.secret-key=MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI="
})
class HealthAndAuthEndToEndApiSmokeTest extends AbstractEndToEndApiSmokeTest {

    @Test
    void publicHealthcheckControllerSmoke() throws Exception {
        mockMvc.perform(get("/public/healthcheck"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }

    @Test
    void publicUserAuthControllerSmoke() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/public/user/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"username":"admin","password":"secret"}
                                """))
                .andExpect(status().isOk())
                .andExpect(cookie().exists(AuthCookieConstants.AUTH_COOKIE_NAME))
                .andExpect(jsonPath("$.user.username").value("admin"))
                .andReturn();

        MockCookie authCookie = authCookieFrom(loginResult);

        mockMvc.perform(get("/public/user/login/check").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.username").value("admin"));

        MvcResult logoutResult = mockMvc.perform(post("/public/user/logout"))
                .andExpect(status().isOk())
                .andExpect(cookie().exists(AuthCookieConstants.AUTH_COOKIE_NAME))
                .andReturn();

        Cookie deleteCookie = logoutResult.getResponse().getCookie(AuthCookieConstants.AUTH_COOKIE_NAME);
        org.assertj.core.api.Assertions.assertThat(deleteCookie).isNotNull();
        org.assertj.core.api.Assertions.assertThat(deleteCookie.getMaxAge()).isZero();
    }
}
