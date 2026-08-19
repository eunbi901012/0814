package kr.ac.knue.facultyevaluation.role;

import java.time.LocalDateTime;

public record RoleSummary(
    String roleCode,
    String roleName,
    String purpose,
    String grantCriteria,
    String dataScopeDefault,
    String useYn,
    LocalDateTime updatedAt
) {
}
