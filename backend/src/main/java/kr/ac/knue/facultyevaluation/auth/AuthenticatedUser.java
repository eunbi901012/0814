package kr.ac.knue.facultyevaluation.auth;

import java.util.List;

public record AuthenticatedUser(
    String userId,
    String loginId,
    String name,
    String departmentCode,
    String systemUseYn,
    List<String> roles
) {

    public boolean hasRole(String roleCode) {
        return roles != null && roles.contains(roleCode);
    }
}
