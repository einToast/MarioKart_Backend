package de.fsr.mariokart_backend.user.controller.pub;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpHeaders;
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
}
