package kr.ac.knue.facultyevaluation.userrole;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserRoleSummary(
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
}
