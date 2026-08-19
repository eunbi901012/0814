package kr.ac.knue.facultyevaluation.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.ac.knue.facultyevaluation.menupermission.MenuAccessDecision;
import kr.ac.knue.facultyevaluation.menupermission.MenuAuthorizationService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class SessionAuthenticationInterceptor implements HandlerInterceptor {

    private final AuthenticationPort authenticationPort;
    private final MenuAuthorizationService menuAuthorizationService;

    public SessionAuthenticationInterceptor(AuthenticationPort authenticationPort, MenuAuthorizationService menuAuthorizationService) {
        this.authenticationPort = authenticationPort;
        this.menuAuthorizationService = menuAuthorizationService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!request.getRequestURI().startsWith("/api/") || isPublicPath(request)) {
            return true;
        }
        AuthenticatedUser user = authenticationPort.authenticate(SessionCookieSupport.readSessionId(request));
        if (!user.hasRole("R09")) {
            throw new PermissionDeniedException("시스템관리자 권한이 필요합니다.");
        }
        MenuAccessDecision decision = menuAuthorizationService.decide(user, request.getRequestURI(), request.getMethod());
        if (decision.matched() && !decision.allowed()) {
            throw new PermissionDeniedException("메뉴 권한이 없습니다.");
        }
        request.setAttribute(AuthenticatedUser.class.getName(), user);
        return true;
    }

    private boolean isPublicPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        return "OPTIONS".equalsIgnoreCase(request.getMethod())
            || "/api/health".equals(path)
            || "/api/auth/login".equals(path)
            || path.startsWith("/v3/api-docs")
            || path.startsWith("/swagger-ui")
            || "/swagger-ui.html".equals(path);
    }
}
