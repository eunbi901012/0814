package kr.ac.knue.facultyevaluation.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;

public final class SessionCookieSupport {

    private SessionCookieSupport() {
    }

    public static String readSessionId(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (AuthenticationService.SESSION_COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public static void addSessionCookie(ResponseEntity.BodyBuilder builder, String sessionId) {
        ResponseCookie cookie = ResponseCookie.from(AuthenticationService.SESSION_COOKIE_NAME, sessionId)
            .httpOnly(true)
            .sameSite("Lax")
            .path("/")
            .maxAge(Duration.ofHours(8))
            .build();
        builder.header(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public static void addExpiredSessionCookie(ResponseEntity.BodyBuilder builder) {
        ResponseCookie cookie = ResponseCookie.from(AuthenticationService.SESSION_COOKIE_NAME, "")
            .httpOnly(true)
            .sameSite("Lax")
            .path("/")
            .maxAge(Duration.ZERO)
            .build();
        builder.header(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
