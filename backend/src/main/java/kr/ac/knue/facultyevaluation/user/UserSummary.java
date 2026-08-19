package kr.ac.knue.facultyevaluation.user;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record UserSummary(
    String userId,
    String loginId,
    String name,
    String departmentCode,
    String departmentName,
    String rankName,
    String employmentStatus,
    List<String> roles,
    String systemUseYn,
    String positionName,
    LocalDate retirementDate,
    LocalDateTime lastSyncedAt
) {
}
