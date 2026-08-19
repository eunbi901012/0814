package kr.ac.knue.facultyevaluation.role;

import java.time.LocalDateTime;

public record RoleRow(
    String roleCode,
    String roleName,
    String purpose,
    String grantCriteria,
    String dataScopeDefault,
    String useYn,
    LocalDateTime updatedAt
) {
    public RoleSummary toSummary() {
        return new RoleSummary(roleCode, roleName, purpose, grantCriteria, dataScopeDefault, useYn, updatedAt);
    }
}
