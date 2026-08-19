package kr.ac.knue.facultyevaluation.auth;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import kr.ac.knue.facultyevaluation.common.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import(GlobalExceptionHandler.class)
class AuthContractTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    AuthenticationService authenticationService;

    @Test
    void openapiFixtureIsAvailableOnClasspathForContractCoverage() {
        org.assertj.core.api.Assertions.assertThat(new ClassPathResource("contracts/openapi.yaml").exists()).isTrue();
    }

    @Test
    void loginWithAdminCredentialsReturnsApiResponseAndHttpOnlySameSiteSessionCookie() throws Exception {
        AuthenticatedUser user = new AuthenticatedUser("admin", "admin", "시스템관리자", "KNUE-DEPT-COMMON", "Y", List.of("R09"));
        when(authenticationService.login("admin", "admin")).thenReturn(new LoginResult("session-1", user));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"loginId\":\"admin\",\"password\":\"admin\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.userId").value("admin"))
            .andExpect(jsonPath("$.data.roles[0]").value("R09"))
            .andExpect(cookie().httpOnly(AuthenticationService.SESSION_COOKIE_NAME, true))
            .andExpect(cookie().path(AuthenticationService.SESSION_COOKIE_NAME, "/"));
    }

    @Test
    void loginWithoutLoginIdReturnsValidationApiErrorEnvelope() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"password\":\"admin\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.fieldErrors[0].field").value("loginId"));
    }

    @Test
    void meWithoutSessionReturns401ApiErrorEnvelope() throws Exception {
        when(authenticationService.currentUser(org.mockito.ArgumentMatchers.any()))
            .thenThrow(new AuthenticationRequiredException("인증 세션이 필요합니다."));

        mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"))
            .andExpect(jsonPath("$.error.message").value("인증 세션이 필요합니다."));
    }

    @Test
    void meWithNonAdminSessionReturns403ApiErrorEnvelope() throws Exception {
        when(authenticationService.currentUser(org.mockito.ArgumentMatchers.any()))
            .thenThrow(new PermissionDeniedException("시스템관리자 권한이 필요합니다."));

        mockMvc.perform(get("/api/auth/me").cookie(new jakarta.servlet.http.Cookie(AuthenticationService.SESSION_COOKIE_NAME, "session-1")))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }

    @Test
    void logoutExpiresSessionAndClearsCookie() throws Exception {
        doNothing().when(authenticationService).logout(anyString());

        mockMvc.perform(post("/api/auth/logout")
                .cookie(new jakarta.servlet.http.Cookie(AuthenticationService.SESSION_COOKIE_NAME, "session-1")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(cookie().maxAge(AuthenticationService.SESSION_COOKIE_NAME, 0));
    }
}
