package kr.ac.knue.facultyevaluation.codegroup;

import java.time.LocalDateTime;

public record CodeGroupSummary(
    String groupId,
    String groupName,
    String description,
    String managingDepartment,
    String useYn,
    LocalDateTime updatedAt
) {
}
