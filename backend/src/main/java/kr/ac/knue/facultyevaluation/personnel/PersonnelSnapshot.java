package kr.ac.knue.facultyevaluation.personnel;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PersonnelSnapshot(
    String snapshotId,
    String employeeNo,
    String name,
    String orgCode,
    String rankName,
    LocalDate retirementDate,
    LocalDateTime lastSyncedAt,
    String status
) {
}
