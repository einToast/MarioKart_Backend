package de.fsr.mariokart_backend.user.controller.pub;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import de.fsr.mariokart_backend.config.AuthCookieConstants;
import de.fsr.mariokart_backend.config.CookieProperties;
import de.fsr.mariokart_backend.testsupport.AbstractWebMvcSliceTest;
import de.fsr.mariokart_backend.testsupport.ContractSchemaSupport;
import de.fsr.mariokart_backend.testsupport.TestDataFactory;
import de.fsr.mariokart_backend.user.UserProperties;
import de.fsr.mariokart_backend.user.model.dto.AuthenticationRequestDTO;
import de.fsr.mariokart_backend.user.model.dto.AuthenticationResult;
import de.fsr.mariokart_backend.user.service.AuthenticationService;

@WebMvcTest(PublicUserAuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Tag("contract")
class PublicUserAuthControllerContractTest extends AbstractWebMvcSliceTest {

    private static final String SCHEMA = "contracts/public/user/public-user-auth-controller.schema.json";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private UserProperties userProperties;

    @MockitoBean
    private CookieProperties cookieProperties;

    @Test
    void loginSuccessMatchesContractAndCookieHeader() throws Exception {
        AuthenticationResult result = TestDataFactory.authResult(
                "token-123",
                TestDataFactory.user("admin", true));

        when(authenticationService.authenticateUser(any(AuthenticationRequestDTO.class))).thenReturn(result);
        when(userProperties.getExpiresAfter()).thenReturn(8);
        when(cookieProperties.getPath()).thenReturn("/");
        when(cookieProperties.isSecure()).thenReturn(false);
        when(cookieProperties.getSameSite()).thenReturn("Lax");

        MvcResult response = mockMvc.perform(post("/public/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TestDataFactory.authRequest("admin", "secret"))))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE,
                        containsString(AuthCookieConstants.AUTH_COOKIE_NAME + "=")))
                .andReturn();

        ContractSchemaSupport.assertJsonMatchesDefinition(SCHEMA, "post_login_success",
                response.getResponse().getContentAsString());
    }

    @Test
    void loginUnauthorizedMatchesContract() throws Exception {
        when(authenticationService.authenticateUser(any(AuthenticationRequestDTO.class)))
                .thenThrow(new BadCredentialsException("bad credentials"));

        MvcResult response = mockMvc.perform(post("/public/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TestDataFactory.authRequest("admin", "wrong"))))
                .andExpect(status().isUnauthorized())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(SCHEMA, "post_login_error_401",
                response.getResponse().getContentAsString());
    }

    @Test
    void checkLoginUnauthorizedWhenCookieMissingMatchesContract() throws Exception {
        MvcResult response = mockMvc.perform(get("/public/user/login/check"))
                .andExpect(status().isUnauthorized())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(SCHEMA, "get_login_check_error_401",
                response.getResponse().getContentAsString());
    }

    @Test
    void checkLoginUnauthorizedForInvalidSessionMatchesContract() throws Exception {
        when(authenticationService.authenticateUserByToken(eq("bad-token")))
                .thenThrow(new BadCredentialsException("invalid"));

        MvcResult response = mockMvc.perform(get("/public/user/login/check")
                        .cookie(new org.springframework.mock.web.MockCookie(
                                AuthCookieConstants.AUTH_COOKIE_NAME, "bad-token")))
                .andExpect(status().isUnauthorized())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(SCHEMA, "get_login_check_error_401",
                response.getResponse().getContentAsString());
    }

    @Test
    void checkLoginSuccessMatchesContract() throws Exception {
        when(authenticationService.authenticateUserByToken(eq("good-token")))
                .thenReturn(TestDataFactory.authResult("good-token", TestDataFactory.user("admin", true)).getResponse());

        MvcResult response = mockMvc.perform(get("/public/user/login/check")
                        .cookie(new org.springframework.mock.web.MockCookie(
                                AuthCookieConstants.AUTH_COOKIE_NAME, "good-token")))
                .andExpect(status().isOk())
                .andReturn();

        ContractSchemaSupport.assertJsonMatchesDefinition(SCHEMA, "get_login_check_success",
                response.getResponse().getContentAsString());
    }

    @Test
    void logoutSuccessMatchesContractAndCookieHeader() throws Exception {
        when(cookieProperties.getPath()).thenReturn("/");
        when(cookieProperties.isSecure()).thenReturn(false);
        when(cookieProperties.getSameSite()).thenReturn("Lax");

        MvcResult response = mockMvc.perform(post("/public/user/logout"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE,
                        containsString(AuthCookieConstants.AUTH_COOKIE_NAME + "=")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")))
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(SCHEMA, "post_logout_success",
                response.getResponse().getContentAsString());
    }
}
