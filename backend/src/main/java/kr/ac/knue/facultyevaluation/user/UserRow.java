package kr.ac.knue.facultyevaluation.user;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public record UserRow(
    String userId,
    String loginId,
    String name,
    String departmentCode,
    String departmentName,
    String rankName,
    String employmentStatus,
    String roles,
    String systemUseYn,
    String positionName,
    LocalDate retirementDate,
    LocalDateTime lastSyncedAt
) {
    public UserSummary toSummary() {
        List<String> roleList = roles == null || roles.isBlank()
            ? List.of()
            : Arrays.stream(roles.split(",")).filter(role -> !role.isBlank()).toList();
        return new UserSummary(userId, loginId, name, departmentCode, departmentName, rankName, employmentStatus,
            roleList, systemUseYn, positionName, retirementDate, lastSyncedAt);
    }
}
