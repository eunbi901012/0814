package kr.ac.knue.facultyevaluation.auth;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import kr.ac.knue.facultyevaluation.auth.mapper.AuthenticationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService implements AuthenticationPort {

    public static final String SESSION_COOKIE_NAME = "KNUE_SESSION";
    private static final int SESSION_HOURS = 8;

    private final AuthenticationMapper authenticationMapper;

    public AuthenticationService(AuthenticationMapper authenticationMapper) {
        this.authenticationMapper = authenticationMapper;
    }

    @Override
    @Transactional
    public LoginResult login(String loginId, String password) {
        StoredAccount account = authenticationMapper.findByLoginId(loginId)
            .orElseThrow(() -> new InvalidCredentialsException("아이디 또는 비밀번호가 올바르지 않습니다."));
        if (!"Y".equals(account.systemUseYn()) || !matches(password, account.passwordHash())) {
            throw new InvalidCredentialsException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        List<String> roles = authenticationMapper.findActiveRoleCodes(account.userId());
        if (!roles.contains("R09")) {
            throw new PermissionDeniedException("시스템관리자 권한이 필요합니다.");
        }
        String sessionId = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(SESSION_HOURS);
        authenticationMapper.insertSession(sessionId, account.userId(), expiresAt);
        return new LoginResult(sessionId, toUser(account, roles));
    }

    @Override
    public AuthenticatedUser authenticate(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new AuthenticationRequiredException("인증 세션이 필요합니다.");
        }
        StoredAccount account = authenticationMapper.findActiveSessionAccount(sessionId, LocalDateTime.now())
            .orElseThrow(() -> new AuthenticationRequiredException("인증 세션이 만료되었거나 유효하지 않습니다."));
        List<String> roles = authenticationMapper.findActiveRoleCodes(account.userId());
        return toUser(account, roles);
    }

    @Transactional
    @Override
    public void logout(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new AuthenticationRequiredException("인증 세션이 필요합니다.");
        }
        authenticationMapper.expireSession(sessionId);
    }

    public AuthenticatedUser currentUser(HttpServletRequest request) {
        Object value = request.getAttribute(AuthenticatedUser.class.getName());
        if (value instanceof AuthenticatedUser user) {
            return user;
        }
        return authenticate(SessionCookieSupport.readSessionId(request));
    }

    private AuthenticatedUser toUser(StoredAccount account, List<String> roles) {
        return new AuthenticatedUser(
            account.userId(),
            account.loginId(),
            account.name(),
            account.departmentCode(),
            account.systemUseYn(),
            roles
        );
    }

    private boolean matches(String rawPassword, String storedHash) {
        if (storedHash == null || !storedHash.startsWith("sha256:")) {
            return false;
        }
        return ("sha256:" + sha256(rawPassword)).equals(storedHash);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 digest is unavailable", ex);
        }
    }
}
