package kr.ac.knue.facultyevaluation.auth;

public interface AuthenticationPort {

    LoginResult login(String loginId, String password);

    AuthenticatedUser authenticate(String sessionId);

    void logout(String sessionId);
}
