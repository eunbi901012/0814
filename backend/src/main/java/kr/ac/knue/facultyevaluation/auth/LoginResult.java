package kr.ac.knue.facultyevaluation.auth;

public record LoginResult(String sessionId, AuthenticatedUser user) {
}
