package kr.ac.knue.facultyevaluation.auth;

public record StoredAccount(
    String userId,
    String loginId,
    String passwordHash,
    String name,
    String departmentCode,
    String systemUseYn
) {
}
