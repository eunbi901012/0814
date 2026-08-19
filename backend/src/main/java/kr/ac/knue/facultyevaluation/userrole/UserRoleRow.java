package kr.ac.knue.facultyevaluation.userrole;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserRoleRow(
    String userRoleId,
    String userId,
    String userName,
    String roleCode,
    String roleName,
    LocalDate effectiveStartDate,
    LocalDate effectiveEndDate,
    String approverUserId,
    String approverName,
    String assignmentType,
    String status,
    LocalDateTime updatedAt
) {
    public UserRoleSummary toSummary() {
        return new UserRoleSummary(
            userRoleId,
            userId,
            userName,
            roleCode,
            roleName,
            effectiveStartDate,
            effectiveEndDate,
            approverUserId,
            approverName,
            assignmentType,
            status,
            updatedAt
        );
    }
}
