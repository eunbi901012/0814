package kr.ac.knue.facultyevaluation.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kr.ac.knue.facultyevaluation.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthenticatedUser>> login(@Valid @RequestBody LoginRequest request) {
        LoginResult loginResult = authenticationService.login(request.loginId(), request.password());
        ResponseEntity.BodyBuilder builder = ResponseEntity.ok();
        SessionCookieSupport.addSessionCookie(builder, loginResult.sessionId());
        return builder.body(ApiResponse.ok(loginResult.user(), "로그인되었습니다."));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        authenticationService.logout(SessionCookieSupport.readSessionId(request));
        ResponseEntity.BodyBuilder builder = ResponseEntity.ok();
        SessionCookieSupport.addExpiredSessionCookie(builder);
        return builder.body(ApiResponse.ok(null, "로그아웃되었습니다."));
    }

    @GetMapping("/me")
    public ApiResponse<AuthenticatedUser> me(HttpServletRequest request) {
        return ApiResponse.ok(authenticationService.currentUser(request));
    }
}
