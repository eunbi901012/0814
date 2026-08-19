package kr.ac.knue.facultyevaluation.codegroup;

import java.time.LocalDateTime;

public record CodeGroupRow(
    String groupId,
    String groupName,
    String description,
    String managingDepartment,
    String useYn,
    LocalDateTime updatedAt
) {
    public CodeGroupSummary toSummary() {
        return new CodeGroupSummary(groupId, groupName, description, managingDepartment, useYn, updatedAt);
    }
}
