package de.fsr.mariokart_backend.user.controller.pub;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import de.fsr.mariokart_backend.config.AuthCookieConstants;
import de.fsr.mariokart_backend.config.CookieProperties;
import de.fsr.mariokart_backend.testsupport.AbstractWebMvcSliceTest;
import de.fsr.mariokart_backend.testsupport.TestDataFactory;
import de.fsr.mariokart_backend.user.UserProperties;
import de.fsr.mariokart_backend.user.model.dto.AuthenticationRequestDTO;
import de.fsr.mariokart_backend.user.model.dto.AuthenticationResult;
import de.fsr.mariokart_backend.user.service.AuthenticationService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebMvcTest(PublicUserAuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Tag("web")
class PublicUserAuthControllerTest extends AbstractWebMvcSliceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private UserProperties userProperties;

    @MockitoBean
    private CookieProperties cookieProperties;

    @Test
    void loginReturnsCookieAndUser() throws Exception {
        AuthenticationResult result = TestDataFactory.authResult(
                "token-123",
                TestDataFactory.user("admin", true));

        when(authenticationService.authenticateUser(any(AuthenticationRequestDTO.class))).thenReturn(result);
        when(userProperties.getExpiresAfter()).thenReturn(8);
        when(cookieProperties.getPath()).thenReturn("/");
        when(cookieProperties.isSecure()).thenReturn(false);
        when(cookieProperties.getSameSite()).thenReturn("Lax");

        AuthenticationRequestDTO request = TestDataFactory.authRequest("admin", "secret");

        mockMvc.perform(post("/public/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE,
                        containsString(AuthCookieConstants.AUTH_COOKIE_NAME + "=")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
                .andExpect(jsonPath("$.user.username").value("admin"));
    }

    @Test
    void loginRejectsMissingPassword() throws Exception {
        String payload = "{\"username\":\"admin\"}";

        mockMvc.perform(post("/public/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginReturnsUnauthorizedForBadCredentials() throws Exception {
        when(authenticationService.authenticateUser(any(AuthenticationRequestDTO.class)))
                .thenThrow(new BadCredentialsException("bad credentials"));

        AuthenticationRequestDTO request = TestDataFactory.authRequest("admin", "wrong");

        mockMvc.perform(post("/public/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("Invalid credentials")));
    }

    @Test
    void checkLoginReturnsUnauthorizedWhenCookieMissing() throws Exception {
        mockMvc.perform(get("/public/user/login/check"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("Not authenticated")));
    }

    @Test
    void checkLoginReturnsUnauthorizedForInvalidSession() throws Exception {
        when(authenticationService.authenticateUserByToken(eq("bad-token")))
                .thenThrow(new BadCredentialsException("invalid"));

        mockMvc.perform(get("/public/user/login/check")
                        .cookie(new org.springframework.mock.web.MockCookie(
                                AuthCookieConstants.AUTH_COOKIE_NAME, "bad-token")))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("Invalid session")));
    }

    @Test
    void checkLoginReturnsUserForValidCookie() throws Exception {
        when(authenticationService.authenticateUserByToken(eq("good-token")))
                .thenReturn(TestDataFactory.authResult("good-token", TestDataFactory.user("admin", true)).getResponse());

        mockMvc.perform(get("/public/user/login/check")
                        .cookie(new org.springframework.mock.web.MockCookie(
                                AuthCookieConstants.AUTH_COOKIE_NAME, "good-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.username").value("admin"));
    }

    @Test
    void logoutClearsCookie() throws Exception {
        when(cookieProperties.getPath()).thenReturn("/");
        when(cookieProperties.isSecure()).thenReturn(false);
        when(cookieProperties.getSameSite()).thenReturn("Lax");

        mockMvc.perform(post("/public/user/logout"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE,
                        containsString(AuthCookieConstants.AUTH_COOKIE_NAME + "=")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Path=/")));
    }
}
